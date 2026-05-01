package io.limao996.hoohoolib.jm18

import android.content.Context
import android.net.Uri
import cxhttp.CxHttp
import cxhttp.CxHttpHelper
import io.limao996.hoohoolib.jm18.utils.ImageDecryptServer
import io.limao996.hoohoolib.utils.KotlinSerializationCborConverter
import io.limao996.hoohoolib.utils.UserAgentGenerator
import io.limao996.hoohoolib.utils.infoLog
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.BookRepositoryApi
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.CanBeEmpty
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.book.LocalBookDataSourceApi
import io.nightfish.lightnovelreader.api.book.Volume
import io.nightfish.lightnovelreader.api.bookshelf.BookshelfRepositoryApi
import io.nightfish.lightnovelreader.api.text.TextProcessingRepositoryApi
import io.nightfish.lightnovelreader.api.userdata.UserDataDaoApi
import io.nightfish.lightnovelreader.api.userdata.UserDataRepositoryApi
import io.nightfish.lightnovelreader.api.util.Cache
import io.nightfish.lightnovelreader.api.web.WebBookDataSource
import io.nightfish.lightnovelreader.api.web.WebBookDataSourceManagerApi
import io.nightfish.lightnovelreader.api.web.WebDataSource
import io.nightfish.lightnovelreader.api.web.explore.ExploreExpandedPageDataSource
import io.nightfish.lightnovelreader.api.web.explore.ExplorePageProvider
import io.nightfish.lightnovelreader.api.web.explore.ExploreTapPageDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

const val JM18_HOST = "https://18mh.net"
var JM18_HTTP_PORT = Random.nextInt(10000, 65535)


@Suppress("unused")
@WebDataSource(
    name = "禁漫天堂🔞", provider = "HoohooLib from 18mh.net"
)
class Jm18WebDataSource(
    val context: Context,
    val userDataDaoApi: UserDataDaoApi,
    val userDataRepositoryApi: UserDataRepositoryApi,
    val webBookDataSourceManagerApi: WebBookDataSourceManagerApi,
    val textProcessingRepositoryApi: TextProcessingRepositoryApi,
    val localBookDataSourceApi: LocalBookDataSourceApi,
    val bookRepositoryApi: BookRepositoryApi,
    val bookshelfRepositoryApi: BookshelfRepositoryApi,
) : WebBookDataSource {
    override val id = "io.limao996.hoohoolib:jm18".hashCode()

    private var coroutineScope = CoroutineScope(Dispatchers.IO)

    override var offLine: Boolean = false
    override val isOffLineFlow = MutableStateFlow(false)
    override suspend fun isOffLine(): Boolean = withContext(Dispatchers.IO) {
        !CxHttp.get(JM18_HOST) {
            header("user-agent", UserAgentGenerator().generateWindowsUA())
        }.await().isSuccessful
    }

    override val cache = Cache(
        timeout = 0//2 * 60 * 60 * 1000
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
        @Suppress("OPT_IN_USAGE") CxHttpHelper.init(
            scope = MainScope(), debugLog = true, converter = KotlinSerializationCborConverter()
        )

        coroutineScope.launch {
            while (currentCoroutineContext().isActive) {
                offLine = isOffLine()
                isOffLineFlow.emit(offLine)
                delay(if (offLine) 1000 else 60000)
            }
        }

        coroutineScope.launch {
            while (true) {
                val server = ImageDecryptServer(JM18_HTTP_PORT)
                server.start()

                if (CxHttp.get("http://127.0.0.1:$JM18_HTTP_PORT/ping")
                        .await().body?.string() == "OK"
                ) break
                JM18_HTTP_PORT = Random.nextInt(10000, 65535)
                server.stop()
            }
        }
    }

    override suspend fun getCoverUriInVolume(
        bookId: String,
        volume: Volume,
        volumeChapterContentMap: MutableMap<String, ChapterContent>,
        context: Context
    ): Uri? {
        return super.getCoverUriInVolume(bookId, volume, volumeChapterContentMap, context)
    }

    override val searchProvider = Jm18SearchProvider
    override val explorePageProvider = object : ExplorePageProvider.DefaultExplorePageProvider {
        override val explorePageIdList: List<String> = emptyList()
        override val exploreTapPageDataSourceMap: Map<String, ExploreTapPageDataSource> = emptyMap()
        override val exploreExpandedPageDataSourceMap: Map<String, ExploreExpandedPageDataSource> =
            emptyMap()
    }//Jm18ExplorePageProvider

    override suspend fun getBookInformation(id: String): BookInformation = ifCache(id) {
        Jm18BookInformation(id)
    }

    override suspend fun getBookVolumes(id: String): BookVolumes = ifCache(id) {
        Jm18BookVolumes(id)
    }

    override suspend fun getChapterContent(chapterId: String, bookId: String): ChapterContent =
        ifCache(chapterId + bookId) {
            ChapterContent.empty()
        }
}
