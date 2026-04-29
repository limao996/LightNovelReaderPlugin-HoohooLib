package io.limao996.hoohoolib.alicesw.explore

import io.nightfish.lightnovelreader.api.util.local
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

class AliceswExploreExpandedPageDataSource(val category: ExploreCategory) :
    ExploreExpandedPageDataSource {
    override val title = category.name
    override val filters: List<SingleChoiceFilter> = if (category.supportOrder) listOf(
        SingleChoiceFilter(
            title = "排序方式".local(),
            dialogTitle = "排序方式".local(),
            description = "选择书籍列表的排序方式".local(),
            choices = ExploreCategory.Order.entries.map {
                it.tag
            },
            defaultChoice = ExploreCategory.Order.UpdateTime.tag
        )
    ) else emptyList()

    override fun loadMore() {
    }

    override fun getResultFlow(): Flow<SearchResult> = flow {
        var currentPage = 0
        val filter =
            filters.firstOrNull()?.value?.let { tag -> ExploreCategory.Order.entries.find { it.tag == tag } }
                ?: ExploreCategory.Order.UpdateTime
        do {
            val bookList = loadSimpleBookList(category, ++currentPage, filter)
            if (bookList.isEmpty()) break
            bookList.forEach {
                emit(SearchResult.MultipleBook(it))
            }
            currentPage++
            delay(2.seconds)
        } while (category.supportMultiPage && currentCoroutineContext().isActive)
        emit(SearchResult.End())
    }.flowOn(Dispatchers.IO)

}