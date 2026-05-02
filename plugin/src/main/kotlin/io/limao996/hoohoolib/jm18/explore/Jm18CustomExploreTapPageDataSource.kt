package io.limao996.hoohoolib.jm18.explore

import io.limao996.hoohoolib.jm18.Jm18BookListParser
import io.limao996.hoohoolib.utils.httpGet
import io.nightfish.lightnovelreader.api.explore.ExploreBooksRow
import io.nightfish.lightnovelreader.api.explore.ExploreDisplayBook
import io.nightfish.lightnovelreader.api.web.explore.ExploreTapPageDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn


class Jm18CustomExploreTapPageDataSource(
    override val title: String, val urlMap: Map<String, Jm18Url>
) : ExploreTapPageDataSource {

    override fun getRowsFlow(): Flow<List<ExploreBooksRow>> {

        val categoryFlows = urlMap.map { (name, url) ->
            flow {
                emit(
                    ExploreBooksRow(
                        title = name,
                        bookList = emptyList(),
                        expandable = true,
                        expandedPageDataSourceId = name
                    )
                )
                val url = url.toUrl(1, 10)
                val soup = httpGet(
                    url, true
                ) ?: return@flow

                val bookList = if (url.contains("/comic/")) Jm18BookListParser("comic", soup)
                    ?: return@flow else Jm18BookListParser("novel", soup) ?: return@flow
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