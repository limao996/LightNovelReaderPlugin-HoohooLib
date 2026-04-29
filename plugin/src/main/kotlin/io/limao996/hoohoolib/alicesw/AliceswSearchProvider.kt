package io.limao996.hoohoolib.alicesw

import android.net.Uri
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
import io.limao996.hoohoolib.utils.httpGet
import java.net.URLEncoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.seconds

object AliceswSearchProvider : SearchProvider {
    override val searchTypes: List<SearchType> = listOf(
        SearchType("_all", "模糊搜索".local(), "请输入关键词".local()),
        SearchType("title", "按书名搜索".local(), "请输入书本名称".local()),
        SearchType("author", "按作者名搜索".local(), "请输入作者名称".local()),
        SearchType("tag", "按标签搜索".local(), "请输入标签名称".local()),
    )

    override fun search(
        searchType: SearchType, keyword: String
    ): Flow<SearchResult> = flow {
        val q = URLEncoder.encode(keyword, "utf-8")
        var currentPage = 0
        while (currentCoroutineContext().isActive) {
            val soup = httpGet("${ALICESW_HOST}/search.html?q=$q&p=${++currentPage}&f=${searchType.type}")
            if (soup == null) {
                emit(SearchResult.Error("网页请求失败！"))
                return@flow
            }

            if (soup.text().contains("找不到和 $keyword 相符的内容。")) {
                emit(SearchResult.Empty())
                return@flow
            }

            val items = soup.selectFirst(".list-group")?.children() ?: break
            for (item in items) {
                val titleLink = item.selectFirst("h5 a")
                val title =
                    titleLink?.text()?.replace(Regex("^\\d+\\.\\s*"), "")?.removeSuffix("全文阅读")
                        ?: "暂无标题"
                val id = titleLink?.attr("href")?.removePrefix("/novel/")?.removeSuffix(".html")
                    ?: continue

                val statusElem = item.selectFirst("h5 small")
                val status = statusElem?.text()?.trim('[', ']') == "已完结"

                val author = item.selectFirst(".mb-1 a")?.text() ?: "未知"
                val description = item.selectFirst(".content-txt")?.text() ?: "暂无简介"

                val tagLinks = item.child(3).select("a")
                val tags = tagLinks.map { tag ->
                    tag.text().removePrefix("#")
                }

                val infoText = item.selectFirst(".mb-1")?.text() ?: ""
                val wordCount =
                    Regex("字数：(\\S+)").find(infoText)?.groupValues?.get(1)?.replace(",", "")
                        ?.removeSuffix("万")?.replace(",", "")?.toFloatOrNull()?.times(10000)
                        ?.toInt() ?: 0
                val timeText = item.child(4)?.text() ?: ""
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                val updateTime =
                    Regex("更新时间：(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2})").find(timeText)?.groupValues?.get(
                        1
                    )?.let { LocalDateTime.parse(it, formatter) } ?: LocalDateTime.now()

                emit(
                    SearchResult.MultipleBook(
                        MutableBookInformation(
                            id = id,
                            title = title,
                            subtitle = "",
                            coverUrl = Uri.EMPTY,
                            author = author,
                            description = description,
                            tags = tags,
                            publishingHouse = "",
                            wordCount = WordCount(wordCount),
                            lastUpdated = updateTime,
                            isComplete = status
                        )
                    )
                )
            }
            val pageDoc = soup.selectFirst(".page")?.text() ?: ""
            val pattern = Regex("第\\d+页/共(\\d+)页")
            val pageCount =
                pattern.find(pageDoc)?.groupValues?.get(1)?.toIntOrNull() ?: Int.MAX_VALUE
            if (currentPage >= pageCount) {
                break
            }
            delay(2.seconds)
        }
        emit(SearchResult.End())
    }.flowOn(Dispatchers.IO)

}