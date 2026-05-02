package io.limao996.hoohoolib.bcshuku

import android.content.Context
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpStatusCode
import io.limao996.hoohoolib.bcshuku.explore.BcshukuExplorePageProvider
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

const val BCSHUKU_HOST = "https://bcshuku.com"

@Suppress("unused")
@WebDataSource(
    name = "八叉书库🔞", provider = "HoohooLib from bcshuku.com"
)
class BcshukuWebDataSource(
    val context: Context,
    val userDataDaoApi: UserDataDaoApi,
    val userDataRepositoryApi: UserDataRepositoryApi,
    val webBookDataSourceManagerApi: WebBookDataSourceManagerApi,
    val textProcessingRepositoryApi: TextProcessingRepositoryApi,
    val localBookDataSourceApi: LocalBookDataSourceApi,
    val bookRepositoryApi: BookRepositoryApi,
    val bookshelfRepositoryApi: BookshelfRepositoryApi,
) : WebBookDataSource {
    val tag = "io.limao996.hoohoolib:bcshuku"
    override val id = tag.hashCode()

    private var coroutineScope = CoroutineScope(Dispatchers.IO)

    override var offLine: Boolean = false
    override val isOffLineFlow = MutableStateFlow(false)
    override suspend fun isOffLine(): Boolean = withContext(Dispatchers.IO) {
        try {
            httpClient.get(BCSHUKU_HOST) {
                header(
                    "user-agent", UserAgentGenerator().generateAndroidUA()
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

    override val searchProvider = BcshukuSearchProvider(context)
    override val explorePageProvider = BcshukuExplorePageProvider

    override suspend fun getBookInformation(id: String) = ifCache("$tag:info:$id") { BcshukuBookInformation(id) }

    override suspend fun getBookVolumes(id: String) = ifCache("$tag:volumes:$id") { BcshukuBookVolumes(id) }

    override suspend fun getChapterContent(chapterId: String, bookId: String) =
        ifCache("$tag:content:$bookId:$chapterId") {
            BcshukuChapterContent(chapterId, bookId, localBookDataSourceApi)
        }
}
