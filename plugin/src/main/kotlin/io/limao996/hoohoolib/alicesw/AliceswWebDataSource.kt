package io.limao996.hoohoolib.alicesw

import android.content.Context
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpStatusCode
import io.limao996.hoohoolib.alicesw.explore.AliceswExplorePageProvider
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

// 数据源主机地址
const val ALICESW_HOST = "https://www.alicesw.com"

@Suppress("unused")
@WebDataSource(
    name = "爱丽丝书屋🔞🛩", provider = "HoohooLib from alicesw.com"
)
class AliceswWebDataSource(
    val context: Context,
    val userDataDaoApi: UserDataDaoApi,
    val userDataRepositoryApi: UserDataRepositoryApi,
    val webBookDataSourceManagerApi: WebBookDataSourceManagerApi,
    val textProcessingRepositoryApi: TextProcessingRepositoryApi,
    val localBookDataSourceApi: LocalBookDataSourceApi,
    val bookRepositoryApi: BookRepositoryApi,
    val bookshelfRepositoryApi: BookshelfRepositoryApi,
) : WebBookDataSource {
    // 数据源唯一id
    val tag = "io.limao996.hoohoolib:alicesw"
    override val id = tag.hashCode()

    // 协程作用域
    private var coroutineScope = CoroutineScope(Dispatchers.IO)

    // 网络检测
    override var offLine: Boolean = false
    override val isOffLineFlow = MutableStateFlow(false)
    override suspend fun isOffLine(): Boolean = withContext(Dispatchers.IO) {
        try {
            httpClient.get(ALICESW_HOST) {
                header(
                    "user-agent", UserAgentGenerator().generateAndroidUA()
                )
            }.status != HttpStatusCode.OK
        } catch (_: Exception) {
            true
        }
    }

    // 初始化缓存机制
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


    // 初始化
    override fun onLoad() {

        // 心跳请求
        coroutineScope.launch {
            while (currentCoroutineContext().isActive) {
                offLine = isOffLine()
                isOffLineFlow.emit(offLine)
                delay(if (offLine) 1000 else 60000)
            }
        }
    }

    override val explorePageProvider = AliceswExplorePageProvider

    override val searchProvider = AliceswSearchProvider

    override suspend fun getBookInformation(id: String) =
        ifCache("$tag:info:$id") { AliceswBookInformation(id) }

    override suspend fun getBookVolumes(id: String) =
        ifCache("$tag:volumes:$id") { AliceswBookVolumes(id) }

    override suspend fun getChapterContent(chapterId: String, bookId: String) =
        ifCache("$tag:content:$bookId:$chapterId") {
            AliceswChapterContent(context, chapterId, bookId, localBookDataSourceApi)
        }

}