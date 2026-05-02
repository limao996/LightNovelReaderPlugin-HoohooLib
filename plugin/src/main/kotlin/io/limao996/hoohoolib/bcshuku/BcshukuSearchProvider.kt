package io.limao996.hoohoolib.bcshuku

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.limao996.hoohoolib.utils.UserAgentGenerator
import io.limao996.hoohoolib.utils.browserGet
import io.limao996.hoohoolib.utils.errorLog
import io.limao996.hoohoolib.utils.httpClient
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
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.net.URLEncoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.seconds

class BcshukuSearchProvider(val context: Context) : SearchProvider {
    override val searchTypes: List<SearchType> = listOf(
        SearchType("_all", "综合搜索".local(), "请输入关键词".local()),
    )

    override fun search(
        searchType: SearchType, keyword: String
    ): Flow<SearchResult> = flow {
        val q = URLEncoder.encode(keyword, "utf-8")
        val ua = UserAgentGenerator().generateAndroidUA()

        val initResponse = browserGet(
            context,
            "$BCSHUKU_HOST/e/search/index.php?show=title,writer,byr&searchget=1&keyboard=$q"
        )
        val html = initResponse?.html() ?: ""
        val match = Regex("searchid=(\\d+)").find(html)
        val searchid = match?.groupValues?.get(1)

        if (searchid == null) {
            emit(SearchResult.Error("获取搜索ID失败！"))
            return@flow
        }
        searchAndParse(searchid, html, ua)
    }.flowOn(Dispatchers.IO)

    private suspend fun kotlinx.coroutines.flow.FlowCollector<SearchResult>.searchAndParse(
        searchid: String, html: String, ua: String
    ) {
        var currentPage = 0
        while (currentCoroutineContext().isActive) {
            val soup = if (currentPage == 0) Jsoup.parse(html) else {
                val response = try {
                    withContext(Dispatchers.IO) {
                        httpClient.get("$BCSHUKU_HOST/e/search/result/index.php") {
                            parameter("page", currentPage)
                            parameter("searchid", searchid)
                            header("user-agent", ua)
                            header("referer", BCSHUKU_HOST)
                        }
                    }
                } catch (e: Exception) {
                    errorLog(e)
                    break
                }
                if (response.status != HttpStatusCode.OK) break
                Jsoup.parse(response.bodyAsText())
            }
            val items = soup.select(".one-row .col-md-3.col-sm-6.col-xs-6.home-truyendecu")

            if (items.isEmpty() && currentPage == 0) {
                emit(SearchResult.Empty())
                break
            }
            if (items.isEmpty()) break

            for (item in items) {
                val link = item.selectFirst(".each_truyen a")
                val bookUrl = link?.attr("href") ?: continue
                val title = item.selectFirst(".caption a h3")?.text() ?: link.attr("title")
                val coverUrl = item.selectFirst(".each_truyen a img")?.attr("src")?.let {
                    if (it.startsWith("http")) it.toUri() else "$BCSHUKU_HOST$it".toUri()
                } ?: Uri.EMPTY

                val lastUpdated = item.selectFirst(".caption .chuyen-muc")?.text()?.let {
                    try {
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                        LocalDateTime.parse("$it 00:00", formatter)
                    } catch (e: Exception) {
                        LocalDateTime.now()
                    }
                } ?: LocalDateTime.now()

                val isComplete = item.selectFirst(".caption .hoan-thanh-mau")?.text() != "连载"

                emit(
                    SearchResult.MultipleBook(
                        MutableBookInformation(
                            id = bookUrl,
                            title = title,
                            subtitle = "",
                            coverUrl = coverUrl,
                            author = "",
                            description = "",
                            tags = emptyList(),
                            publishingHouse = "八叉书库🔞",
                            wordCount = WordCount(0),
                            lastUpdated = lastUpdated,
                            isComplete = isComplete
                        )
                    )
                )
            }
            currentPage++
            delay(2.seconds)
        }
        emit(SearchResult.End())
    }
}
