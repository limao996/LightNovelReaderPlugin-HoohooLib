package io.limao996.hoohoolib.fqbook

import android.content.Context
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpStatusCode
import io.limao996.hoohoolib.fqbook.explore.FqbookExplorePageProvider
import io.limao996.hoohoolib.utils.UserAgentGenerator
import io.limao996.hoohoolib.utils.httpClient
import io.nightfish.lightnovelreader.api.book.BookRepositoryApi
import io.nightfish.lightnovelreader.api.book.CanBeEmpty
import io.nightfish.lightnovelreader.api.book.LocalBookDataSourceApi
import io.nightfish.lightnovelreader.api.bookshelf.BookshelfRepositoryApi
import io.nightfish.lightnovelreader.api.text.TextProcessingRepositoryApi
import io.nightfish.lightnovelreader.api.userdata.UserDataDaoApi
import io.nightfish.lightnovelreader.api.userdata.UserDataRepositoryApi
import io.nightfish.lightnovelreader.api.util.Cache
import io.nightfish.lightnovelreader.api.web.WebBookDataSource
import io.nightfish.lightnovelreader.api.web.WebBookDataSourceManagerApi
import io.nightfish.lightnovelreader.api.web.WebDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val FQBOOK_HOST = "https://fqbook.cc"

@Suppress("unused")
@WebDataSource(
    name = "疯情书库🔞🛩", provider = "HoohooLib from fqbook.cc"
)
class FqbookWebDataSource(
    val context: Context,
    val userDataDaoApi: UserDataDaoApi,
    val userDataRepositoryApi: UserDataRepositoryApi,
    val webBookDataSourceManagerApi: WebBookDataSourceManagerApi,
    val textProcessingRepositoryApi: TextProcessingRepositoryApi,
    val localBookDataSourceApi: LocalBookDataSourceApi,
    val bookRepositoryApi: BookRepositoryApi,
    val bookshelfRepositoryApi: BookshelfRepositoryApi,
) : WebBookDataSource {
    val tag = "io.limao996.hoohoolib:fqbook"
    override val id = tag.hashCode()

    private var coroutineScope = CoroutineScope(Dispatchers.IO)

    override var offLine: Boolean = false
    override val isOffLineFlow = MutableStateFlow(false)
    override suspend fun isOffLine(): Boolean = withContext(Dispatchers.IO) {
        try {
            httpClient.get(FQBOOK_HOST) {
                header(
                    "user-agent", UserAgentGenerator().generateWindowsUA()
                )
            }.status != HttpStatusCode.OK
        } catch (_: Exception) {
            true
        }
    }

    override val cache = Cache(
        timeout = 2 * 60 * 60 * 1000
    )

    private inline fun <reified T : CanBeEmpty> ifCache(id: String, block: () -> T): T {
        val cacheData = cache.getCache<T>(id.hashCode())
        if (cacheData == null) {
            val data = block.invoke()
            if (data.isEmpty()) return data
            cache.cache(id.hashCode(), data)
            return data
        }
        return cacheData
    }

    override fun onLoad() {
        coroutineScope.launch {
            while (currentCoroutineContext().isActive) {
                offLine = isOffLine()
                isOffLineFlow.emit(offLine)
                delay(if (offLine) 1000 else 60000)
            }
        }
    }

    override val searchProvider = FqbookSearchProvider
    override val explorePageProvider = FqbookExplorePageProvider

    override suspend fun getBookInformation(id: String) =
        ifCache("$tag:info:$id") { FqbookBookInformation(id) }

    override suspend fun getBookVolumes(id: String) =
        ifCache("$tag:volumes:$id") { FqbookBookVolumes(id) }

    override suspend fun getChapterContent(chapterId: String, bookId: String) =
        ifCache("$tag:content:$bookId:$chapterId") {
            FqbookChapterContent(
                chapterId, bookId, localBookDataSourceApi
            )
        }
}
