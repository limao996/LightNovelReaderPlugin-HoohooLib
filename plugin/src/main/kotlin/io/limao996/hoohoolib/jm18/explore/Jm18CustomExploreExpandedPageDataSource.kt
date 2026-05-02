package io.limao996.hoohoolib.jm18.explore

import io.limao996.hoohoolib.jm18.Jm18BookListParser
import io.limao996.hoohoolib.utils.httpGet
import io.limao996.hoohoolib.utils.infoLog
import io.nightfish.lightnovelreader.api.util.LocalString
import io.nightfish.lightnovelreader.api.web.explore.ExploreExpandedPageDataSource
import io.nightfish.lightnovelreader.api.web.explore.filter.SingleChoiceFilter
import io.nightfish.lightnovelreader.api.web.explore.filter.SwitchFilter
import io.nightfish.lightnovelreader.api.web.search.SearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlin.time.Duration.Companion.seconds

sealed class Jm18CustomExploreExpandedPageDataSource {

    class Novel(
        override val title: String,
        val url: Jm18NovelUrl,
        val tags: List<Pair<String, String>> = listOf("不限" to "all")
    ) : ExploreExpandedPageDataSource {
        override val filters: List<SingleChoiceFilter> = listOf(
            SingleChoiceFilter(
                title = LocalString("标签"),
                dialogTitle = LocalString("选择标签"),
                description = LocalString("选择作品标签"),
                choices = tags.map { it.first },
                defaultChoice = tags.first { it.second == url.tag }.first,
            ), SingleChoiceFilter(
                title = LocalString("状态"),
                dialogTitle = LocalString("状态"),
                description = LocalString("选择作品状态"),
                choices = Jm18NovelUrl.statusList.map { it.first },
                defaultChoice = Jm18NovelUrl.statusList.first { it.second == url.status }.first,
            ), SingleChoiceFilter(
                title = LocalString("篇幅"),
                dialogTitle = LocalString("篇幅"),
                description = LocalString("选择作品篇幅"),
                choices = Jm18NovelUrl.lengthList.map { it.first },
                defaultChoice = Jm18NovelUrl.lengthList.first { it.second == url.length }.first,
            ), SingleChoiceFilter(
                title = LocalString("排序字段"),
                dialogTitle = LocalString("排序字段"),
                description = LocalString("选择作品排序字段"),
                choices = Jm18NovelUrl.sortBys.map { it.first },
                defaultChoice = Jm18NovelUrl.sortBys.first { it.second == url.sortBy }.first,
            ), SingleChoiceFilter(
                title = LocalString("排序方式"),
                dialogTitle = LocalString("选择排序方式"),
                description = LocalString("选择作品排序方式"),
                choices = Jm18NovelUrl.sortOrders.map { it.first },
                defaultChoice = Jm18NovelUrl.sortOrders.first { it.second == url.sortOrder }.first,
            )
        )

        var targetPage = 1
        override fun loadMore() {
            targetPage += 1
        }

        override fun getResultFlow(): Flow<SearchResult> = flow {
            targetPage = 1
            var currentPage = 1

            val url = Jm18NovelUrl(
                tag = tags.first { it.first == filters[0].value }.second,
                status = Jm18NovelUrl.statusList.first { it.first == filters[1].value }.second,
                length = Jm18NovelUrl.lengthList.first { it.first == filters[2].value }.second,
                sortBy = Jm18NovelUrl.sortBys.first { it.first == filters[3].value }.second,
                sortOrder = Jm18NovelUrl.sortOrders.first { it.first == filters[4].value }.second
            )

            while (currentCoroutineContext().isActive) {
                if (targetPage < currentPage) {
                    delay(100)
                    continue
                }

                val url = url.toUrl(currentPage, 30)
                val soup = httpGet(
                    url, true
                ) ?: return@flow

                val bookList = Jm18BookListParser("novel", soup) ?: return@flow

                if (bookList.isEmpty()) break
                bookList.forEach {
                    emit(SearchResult.MultipleBook(it))
                }
                currentPage++
                delay(1.seconds)
            }
            emit(SearchResult.End())
        }.flowOn(Dispatchers.IO)
    }

    class Comic(
        override val title: String,
        val url: Jm18ComicUrl,
        val tags: List<Pair<String, String>> = listOf("不限" to "all")
    ) : ExploreExpandedPageDataSource {
        override val filters: List<SingleChoiceFilter> = listOf(
            SingleChoiceFilter(
                title = LocalString("标签"),
                dialogTitle = LocalString("选择标签"),
                description = LocalString("选择作品标签"),
                choices = tags.map { it.first },
                defaultChoice = tags.first { it.second == url.tag }.first,
            ), SingleChoiceFilter(
                title = LocalString("排序字段"),
                dialogTitle = LocalString("排序字段"),
                description = LocalString("选择作品排序字段"),
                choices = Jm18ComicUrl.sortBys.map { it.first },
                defaultChoice = Jm18ComicUrl.sortBys.first { it.second == url.sortBy }.first,
            ), SingleChoiceFilter(
                title = LocalString("排序方式"),
                dialogTitle = LocalString("选择排序方式"),
                description = LocalString("选择作品排序方式"),
                choices = Jm18ComicUrl.sortOrders.map { it.first },
                defaultChoice = Jm18ComicUrl.sortOrders.first { it.second == url.sortOrder }.first,
            )
        )

        var targetPage = 1
        override fun loadMore() {
            targetPage += 1
        }

        override fun getResultFlow(): Flow<SearchResult> = flow {
            targetPage = 1
            var currentPage = 1

            val url = Jm18ComicUrl(
                tag = tags.first { it.first == filters[0].value }.second,
                sortBy = Jm18ComicUrl.sortBys.first { it.first == filters[1].value }.second,
                sortOrder = Jm18ComicUrl.sortOrders.first { it.first == filters[2].value }.second
            )

            while (currentCoroutineContext().isActive) {
                if (targetPage < currentPage) {
                    delay(100)
                    continue
                }

                val url = url.toUrl(currentPage, 30)
                val soup = httpGet(
                    url, true
                ) ?: return@flow

                val bookList = Jm18BookListParser("comic", soup) ?: return@flow

                if (bookList.isEmpty()) break
                bookList.forEach {
                    emit(SearchResult.MultipleBook(it))
                }
                currentPage++
                delay(1.seconds)
            }
            emit(SearchResult.End())
        }.flowOn(Dispatchers.IO)
    }
}