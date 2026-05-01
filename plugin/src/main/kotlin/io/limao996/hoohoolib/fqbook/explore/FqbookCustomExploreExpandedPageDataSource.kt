package io.limao996.hoohoolib.fqbook.explore

import io.limao996.hoohoolib.fqbook.explore.FqbookExploreLoader.Parameters
import io.nightfish.lightnovelreader.api.util.LocalString
import io.nightfish.lightnovelreader.api.web.explore.ExploreExpandedPageDataSource
import io.nightfish.lightnovelreader.api.web.explore.filter.SingleChoiceFilter
import io.nightfish.lightnovelreader.api.web.search.SearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlin.time.Duration.Companion.seconds

class FqbookCustomExploreExpandedPageDataSource(
    override val title: String, val parameters: Parameters
) : ExploreExpandedPageDataSource {
    override val filters: List<SingleChoiceFilter> = Parameters.run {
        listOf(
            SingleChoiceFilter(
                title = LocalString("分类"),
                dialogTitle = LocalString("选择分类"),
                description = LocalString("选择作品分类"),
                choices = category.keys.toList(),
                defaultChoice = category.entries.first { it.value == parameters.catId }.key
            ), SingleChoiceFilter(
                title = LocalString("作品字数"),
                dialogTitle = LocalString("选择字数范围"),
                description = LocalString("筛选作品字数"),
                choices = wordCount.keys.toList(),
                defaultChoice = wordCount.entries.first { it.value == parameters.size }.key
            ), SingleChoiceFilter(
                title = LocalString("是否完结"),
                dialogTitle = LocalString("选择完结状态"),
                description = LocalString("筛选是否完结"),
                choices = finishStatus.keys.toList(),
                defaultChoice = finishStatus.entries.first { it.value == parameters.isFinish }.key
            ), SingleChoiceFilter(
                title = LocalString("更新时间"),
                dialogTitle = LocalString("选择更新时间"),
                description = LocalString("筛选最近更新"),
                choices = updateTime.keys.toList(),
                defaultChoice = updateTime.entries.first { it.value == parameters.updT }.key
            ), SingleChoiceFilter(
                title = LocalString("排序方式"),
                dialogTitle = LocalString("选择排序方式"),
                description = LocalString("作品排序方式"),
                choices = orderBy.keys.toList(),
                defaultChoice = orderBy.entries.first { it.value == parameters.orderBy }.key
            )
        )
    }

    var targetPage = 1
    override fun loadMore() {
        targetPage += 1
    }

    override fun getResultFlow(): Flow<SearchResult> = flow {
        targetPage = 1
        var currentPage = 1
        val newParameters = Parameters.run {
            Parameters(
                catId = category[filters[0].value]!!,
                size = wordCount[filters[1].value]!!,
                isFinish = finishStatus[filters[2].value]!!,
                updT = updateTime[filters[3].value]!!,
                orderBy = orderBy[filters[4].value]!!
            )
        }
        while (currentCoroutineContext().isActive) {
            if (targetPage < currentPage) {
                delay(100)
                continue
            }
            val bookList =
                FqbookExploreLoader.get(pageSize = 30, pageNum = currentPage, parameters = newParameters)
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