package io.limao996.hoohoolib.jm18

import io.limao996.hoohoolib.utils.httpGet
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
import kotlin.time.Duration.Companion.seconds

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


            Jm18BookListParser(searchType.type, soup)?.forEach {
                emit(SearchResult.MultipleBook(it))
            } ?: run {
                emit(SearchResult.Empty())
                return@flow
            }

            currentPage++

            delay(2.seconds)
        }
        emit(SearchResult.End())
    }.flowOn(Dispatchers.IO)

}