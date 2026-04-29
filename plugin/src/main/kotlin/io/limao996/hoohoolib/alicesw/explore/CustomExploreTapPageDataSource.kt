package io.limao996.hoohoolib.alicesw.explore

import io.nightfish.lightnovelreader.api.explore.ExploreBooksRow
import io.nightfish.lightnovelreader.api.explore.ExploreDisplayBook
import io.nightfish.lightnovelreader.api.web.explore.ExploreTapPageDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.collections.emptyList

class CustomExploreTapPageDataSource(
    override val title: String, val fromIndex: Int, val toIndex: Int
) : ExploreTapPageDataSource {

    override fun getRowsFlow(): Flow<List<ExploreBooksRow>> {
        val categoryRange = AliceswExplorePageProvider.categories.subList(fromIndex, toIndex)

        val categoryFlows = categoryRange.map { category ->
            flow {
                emit(
                    ExploreBooksRow(
                        title = category.name,
                        bookList = emptyList(),
                        expandable = true,
                        expandedPageDataSourceId = category.name
                    )
                )
                val bookList = loadSimpleBookList(category)
                emit(
                    ExploreBooksRow(
                        title = category.name, bookList = bookList.map {
                            ExploreDisplayBook(
                                id = it.id,
                                title = it.title,
                                author = it.author,
                                coverUri = it.coverUri
                            )
                        }, expandable = true, expandedPageDataSourceId = category.name
                    )
                )
            }.flowOn(Dispatchers.IO)
        }

        return combine(categoryFlows) { rowsArray ->
            rowsArray.toList()
        }.flowOn(Dispatchers.IO)
    }
}