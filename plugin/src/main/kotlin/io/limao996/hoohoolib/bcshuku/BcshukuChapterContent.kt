package io.limao996.hoohoolib.bcshuku

import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.parameters
import io.limao996.hoohoolib.utils.UserAgentGenerator
import io.limao996.hoohoolib.utils.httpClient
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.book.LocalBookDataSourceApi
import io.nightfish.lightnovelreader.api.book.MutableChapterContent
import io.nightfish.lightnovelreader.api.content.builder.ContentBuilder
import io.nightfish.lightnovelreader.api.content.builder.simpleText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.text.trim


suspend fun BcshukuChapterContent(
    chapterId: String,
    bookId: String,
    localBookDataSourceApi: LocalBookDataSourceApi,
): ChapterContent {
    val ua = UserAgentGenerator().generateAndroidUA()

    val pageResponse = withContext(Dispatchers.IO) {
        httpClient.get(chapterId) {
            header(
                "user-agent", ua
            )
        }
    }
    val pageHtml = pageResponse.bodyAsText()

    val jsonMatch = Regex("""\{"url"[^}]*"chapter"[^}]*\}""").find(pageHtml)
    val configJson = jsonMatch?.value ?: return ChapterContent.empty(chapterId)
    val config = JSONObject(configJson)
    val url = config.optString("url", "")
    val mobile = config.optString("mobile", "")
    val isk = config.optString("isk", "")
    val novel = config.optString("novel", "")
    val chapter = config.optString("chapter", "")

    val contentJson = withContext(Dispatchers.IO) {
        httpClient.post("$BCSHUKU_HOST/conapi.php") {
            header("user-agent", ua)
            header("origin", BCSHUKU_HOST)
            header("x-requested-with", "XMLHttpRequest")
            header("referer", chapterId)

            setBody(FormDataContent(parameters {
                append("url", url)
                append("mobile", mobile)
                append("isk", isk)
                append("novel", novel)
                append("chapter", chapter)
            }))
        }.bodyAsText()
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
            simpleText(content.trim().split("\n").filter { it.isNotBlank() }.joinToString("\n\n") {
                "ㅤㅤ${it.trim()}"
            })
        }.build(), lastChapter = prevId ?: "", nextChapter = nextId ?: ""
    )
}
