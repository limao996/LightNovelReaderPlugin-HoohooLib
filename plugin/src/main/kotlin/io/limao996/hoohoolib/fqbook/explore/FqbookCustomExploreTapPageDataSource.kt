package io.limao996.hoohoolib.fqbook.explore

import io.limao996.hoohoolib.fqbook.explore.FqbookExploreLoader.Parameters
import io.nightfish.lightnovelreader.api.explore.ExploreBooksRow
import io.nightfish.lightnovelreader.api.explore.ExploreDisplayBook
import io.nightfish.lightnovelreader.api.web.explore.ExploreTapPageDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class FqbookCustomExploreTapPageDataSource(
    override val title: String, val parametersMap: Map<String, Parameters>
) : ExploreTapPageDataSource {

    override fun getRowsFlow(): Flow<List<ExploreBooksRow>> {

        val categoryFlows = parametersMap.map { (name, order) ->
            flow {
                emit(
                    ExploreBooksRow(
                        title = name,
                        bookList = emptyList(),
                        expandable = true,
                        expandedPageDataSourceId = name
                    )
                )
                val bookList = FqbookExploreLoader.get(pageSize = 5, parameters = order)
                emit(
                    ExploreBooksRow(
                        title = name, bookList = bookList.map {
                            ExploreDisplayBook(
                                id = it.id,
                                title = it.title,
                                author = it.author,
                                coverUri = it.coverUri
                            )
                        }, expandable = true, expandedPageDataSourceId = name
                    )
                )
            }.flowOn(Dispatchers.IO)
        }

        return combine(categoryFlows) { rowsArray ->
            rowsArray.toList()
        }.flowOn(Dispatchers.IO)
    }
}