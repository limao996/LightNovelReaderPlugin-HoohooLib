package io.limao996.hoohoolib.jm18

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import io.limao996.hoohoolib.fqbook.FQBOOK_HOST
import io.limao996.hoohoolib.jm18.utils.buildDecryptedImageUrl
import io.limao996.hoohoolib.utils.browserGet
import io.limao996.hoohoolib.utils.httpGet
import io.limao996.hoohoolib.utils.infoLog
import io.nightfish.lightnovelreader.api.book.MutableBookInformation
import io.nightfish.lightnovelreader.api.book.WordCount
import io.nightfish.lightnovelreader.api.util.local
import io.nightfish.lightnovelreader.api.web.search.SearchProvider
import io.nightfish.lightnovelreader.api.web.search.SearchResult
import io.nightfish.lightnovelreader.api.web.search.SearchType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import org.jsoup.nodes.Element
import java.net.URLEncoder
import java.time.LocalDateTime
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.time.Duration.Companion.seconds

fun decryptAES(result: ByteArray): ByteArray {
    val key = "f5d965df75336270".toByteArray(Charsets.UTF_8)
    val iv = "97b60394abc2fbe1".toByteArray(Charsets.UTF_8)

    val secretKeySpec = SecretKeySpec(key, "AES")
    val ivParameterSpec = IvParameterSpec(iv)

    val cipher = Cipher.getInstance("AES/CBC/NoPadding")
    cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)

    return cipher.doFinal(result)
}

object Jm18SearchProvider : SearchProvider {
    override val searchTypes: List<SearchType> = listOf(
        SearchType("novel", "搜索小说作品".local(), "请输入小说关键词".local()),
        SearchType("comic", "搜索漫画作品".local(), "请输入漫画关键词".local()),
    )

    override fun search(
        searchType: SearchType, keyword: String
    ): Flow<SearchResult> = flow {
        val q = URLEncoder.encode(keyword, "utf-8")
        var currentPage = 0
        while (currentCoroutineContext().isActive) {
            val soup = httpGet(
                "$JM18_HOST/${searchType.type}/search/${currentPage + 1}?key_word=$q", true
            ) ?: run {
                emit(SearchResult.Error("网页请求失败！"))
                return@flow
            }


            val items = soup.selectFirst("ul.dx-novel-list")?.children() ?: emptyList()

            if (items.isEmpty()) {
                emit(SearchResult.Empty())
                return@flow
            }

            for (item in items) {
                emit(
                    SearchResult.MultipleBook(
                        if (searchType.type == "novel") getNovelInfo(
                            item
                        ) else getComicInfo(item)
                    )
                )
            }
            currentPage++

            delay(2.seconds)
        }
        emit(SearchResult.End())
    }.flowOn(Dispatchers.IO)

    fun getComicInfo(item: Element): MutableBookInformation {
        val box = item.child(0)
        val id = box.attr("data-click_item_id")

        val top = box.child(0)

        val poster = top.selectFirst("div.poster")
        val coverUrl =
            poster?.child(0)?.attr("data-src")?.let(::buildDecryptedImageUrl)?.toUri() ?: Uri.EMPTY
        val info2 = poster?.child(1)
        val original = info2?.child(0)?.text()?.trim()?.let { listOf(it) } ?: emptyList()
        val isComplete = info2?.child(1)?.text()?.trim() == "完结"

        val bottom = box.child(1)

        val title = bottom.selectFirst("div h3")?.text() ?: ""
        val info = bottom.child(1)
        val author = info.child(0).text()
        val category = info.child(1).text().let { listOf(it) }
        return MutableBookInformation(
            id = "comic:$id",
            title = title,
            subtitle = "",
            coverUrl = coverUrl,
            author = author,
            description = "",
            tags = category + original,
            publishingHouse = "禁漫天堂-漫画🔞",
            wordCount = WordCount(0),
            lastUpdated = LocalDateTime.now(),
            isComplete = isComplete
        )
    }

    fun getNovelInfo(item: Element): MutableBookInformation {
        val top = item.child(0)

        val id = top.attr("data-click_item_id")
        val poster = top.selectFirst("div.poster")
        val coverUrl =
            poster?.child(0)?.attr("data-src")?.let(::buildDecryptedImageUrl)?.toUri() ?: Uri.EMPTY
        val info2 = poster?.child(1)
        val original = info2?.child(0)?.text()?.trim()?.let { listOf(it) } ?: emptyList()
        val isComplete = info2?.child(1)?.text()?.trim() == "已完结"

        val bottom = item.child(1)

        val title = bottom.selectFirst("div a h3")?.text() ?: ""
        val info = bottom.child(1)
        val author = info.child(0).text()
        val category = info.child(1).child(0).text().let { listOf(it) }
        return MutableBookInformation(
            id = "novel:$id",
            title = title,
            subtitle = "",
            coverUrl = coverUrl,
            author = author,
            description = "",
            tags = category + original,
            publishingHouse = "禁漫天堂-小说🔞",
            wordCount = WordCount(0),
            lastUpdated = LocalDateTime.now(),
            isComplete = isComplete
        )
    }
}