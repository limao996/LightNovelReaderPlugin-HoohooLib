package io.limao996.hoohoolib.bcshuku

import cxhttp.CxHttp
import io.limao996.hoohoolib.utils.UserAgentGenerator
import io.limao996.hoohoolib.utils.infoLog
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.book.LocalBookDataSourceApi
import io.nightfish.lightnovelreader.api.book.MutableChapterContent
import io.nightfish.lightnovelreader.api.content.builder.ContentBuilder
import io.nightfish.lightnovelreader.api.content.builder.simpleText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

private val okHttpClient = OkHttpClient()

suspend fun BcshukuChapterContent(
    chapterId: String,
    bookId: String,
    localBookDataSourceApi: LocalBookDataSourceApi,
): ChapterContent {
    val ua = UserAgentGenerator().generateAndroidUA()

    // Step 1: Fetch chapter page to extract the JSON config
    val pageResponse = withContext(Dispatchers.IO) {
        CxHttp.get(chapterId) {
            header("user-agent", ua)
        }.await()
    }
    val pageHtml = pageResponse.body?.string() ?: return ChapterContent.empty(chapterId)

    // Extract JSON config: {"url":"...","mobile":"...","isk":"...","novel":"...","chapter":"..."}
    val jsonMatch = Regex("""\{"url"[^}]*"chapter"[^}]*\}""").find(pageHtml)
    val configJson = jsonMatch?.value ?: return ChapterContent.empty(chapterId)
    val config = JSONObject(configJson)
    val url = config.optString("url", "")
    val mobile = config.optString("mobile", "")
    val isk = config.optString("isk", "")
    val novel = config.optString("novel", "")
    val chapter = config.optString("chapter", "")

    val postBody =
        FormBody.Builder().add("url", url).add("mobile", mobile).add("isk", isk).add("novel", novel)
            .add("chapter", chapter).build()

    val postRequest = Request.Builder().url("$BCSHUKU_HOST/conapi.php").header("user-agent", ua)
        .header("origin", BCSHUKU_HOST).header("x-requested-with", "XMLHttpRequest")
        .header("referer", chapterId).post(postBody).build()

    val contentJson = withContext(Dispatchers.IO) {
        okHttpClient.newCall(postRequest).execute().body.string()
    }

    val result = JSONObject(contentJson)
    val content = result.optString("content", "")

    val volumes = localBookDataSourceApi.getBookVolumes(bookId)!!.volumes
    val flatChapter = volumes.flatMap { volume -> volume.chapters }
    val flatChapterIds = flatChapter.map { it.id }
    val currentIndex = flatChapterIds.indexOf(chapterId)
    val prevId = flatChapterIds.getOrNull(currentIndex - 1)
    val nextId = flatChapterIds.getOrNull(currentIndex + 1)

    val title = flatChapter[currentIndex].title

    return MutableChapterContent(
        id = chapterId, title = title, content = ContentBuilder().apply {
            simpleText(
                content.replace("                ", "ㅤㅤ")
                    .replace(Regex("ㅤㅤ\nㅤㅤ\n"), "\n")
            )
        }.build(), lastChapter = prevId ?: "", nextChapter = nextId ?: ""
    )
}
