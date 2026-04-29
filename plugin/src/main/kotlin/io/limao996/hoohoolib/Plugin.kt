package io.limao996.hoohoolib

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import io.nightfish.lightnovelreader.api.book.BookRepositoryApi
import io.nightfish.lightnovelreader.api.book.LocalBookDataSourceApi
import io.nightfish.lightnovelreader.api.bookshelf.BookshelfRepositoryApi
import io.nightfish.lightnovelreader.api.plugin.LightNovelReaderPlugin
import io.nightfish.lightnovelreader.api.plugin.Plugin
import io.nightfish.lightnovelreader.api.text.TextProcessingRepositoryApi
import io.nightfish.lightnovelreader.api.userdata.UserDataDaoApi
import io.nightfish.lightnovelreader.api.userdata.UserDataRepositoryApi
import io.nightfish.lightnovelreader.api.web.WebBookDataSourceManagerApi



@Suppress("unused")
@Plugin(
    version = BuildConfig.VERSION_CODE,
    name = "HoohooLib",
    versionName = BuildConfig.VERSION_NAME,
    author = "limao996",
    description = "齁哦哦文库——老司机合集🔞",
    updateUrl = "",
    apiVersion = 2
)
class Plugin(
    val userDataDaoApi: UserDataDaoApi,
    val userDataRepositoryApi: UserDataRepositoryApi,
    val webBookDataSourceManagerApi: WebBookDataSourceManagerApi,
    val textProcessingRepositoryApi: TextProcessingRepositoryApi,
    val localBookDataSourceApi: LocalBookDataSourceApi,
    val bookRepositoryApi: BookRepositoryApi,
    val bookshelfRepositoryApi: BookshelfRepositoryApi,
) : LightNovelReaderPlugin {

    override fun onLoad() {
    }

    @Composable
    override fun PageContent(paddingValues: PaddingValues) {
        val content = LocalContext.current
    }
}

class PluginDiscoveryReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {}
}
