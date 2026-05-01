package io.limao996.hoohoolib.fqbook

import android.net.Uri
import androidx.core.net.toUri
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
import java.net.URLEncoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.seconds

object FqbookSearchProvider : SearchProvider {
    override val searchTypes: List<SearchType> = listOf(
        SearchType("_all", "综合搜索".local(), "请输入关键词".local()),
    )

    override fun search(
        searchType: SearchType, keyword: String
    ): Flow<SearchResult> = flow {
        val q = URLEncoder.encode(keyword, "utf-8")

        var currentPage = 0
        while (currentCoroutineContext().isActive) {
            val soup = httpGet(
                "$FQBOOK_HOST/m/search.html?page=${currentPage + 1}&searchword=$q"
            )

            if (soup == null) {
                emit(SearchResult.Error("网页请求失败！"))
                return@flow
            }

            val items = soup.selectFirst(".book-list")?.children() ?: emptyList()

            if (items.isEmpty()) {
                emit(SearchResult.Empty())
                return@flow
            }

            for (item in items) {
                val link = item.selectFirst(".column-2 .left a") ?: continue
                val id = link.attr("href").removePrefix("book-").removeSuffix(".html")
                val coverUrl =
                    link.child(0).attr("src").let { FQBOOK_HOST + it.removePrefix("..") }.toUri()
                val title = item.selectFirst(".right h5 .name")?.text() ?: continue
                val author = item.selectFirst(".right .author")?.text() ?: ""
                val description = item.selectFirst(".info .summary")?.wholeText()?.trim() ?: ""
                val lastUpdated =
                    item.selectFirst(".right .time")?.text()?.removePrefix("最后更新：")?.let {
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        LocalDateTime.parse(it, formatter)
                    } ?: LocalDateTime.now()
                val isComplete = item.selectFirst(".right .status")?.text() == "已完结"

                emit(
                    SearchResult.MultipleBook(
                        MutableBookInformation(
                            id = id,
                            title = title,
                            subtitle = "",
                            coverUrl = coverUrl,
                            author = author,
                            description = description,
                            tags = emptyList(),
                            publishingHouse = "疯情书库🔞",
                            wordCount = WordCount(0),
                            lastUpdated = lastUpdated,
                            isComplete = isComplete
                        )
                    )
                )
            }
            currentPage++


            val pageCount = soup.selectFirst("div.results div.hd h4 span")?.text()?.let {
                Regex("共计(\\d+)页").find(it)?.groupValues[1]?.toIntOrNull()
            } ?: Int.MAX_VALUE

            if (currentPage + 1 > pageCount) break

            delay(2.seconds)
        }
        emit(SearchResult.End())
    }.flowOn(Dispatchers.IO)
}
