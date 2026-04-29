package io.limao996.hoohoolib.alicesw

import android.content.Context
import androidx.core.net.toUri
import io.limao996.hoohoolib.utils.browserGet
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.book.LocalBookDataSourceApi
import io.nightfish.lightnovelreader.api.book.MutableChapterContent
import io.nightfish.lightnovelreader.api.content.builder.ContentBuilder
import io.nightfish.lightnovelreader.api.content.builder.image
import io.nightfish.lightnovelreader.api.content.builder.simpleText

suspend fun AliceswChapterContent(
    context: Context,
    chapterId: String,
    bookId: String,
    localBookDataSourceApi: LocalBookDataSourceApi
): ChapterContent {

    val volumeId = localBookDataSourceApi.getBookVolumes(bookId)?.volumes?.find {
        it.chapters.find { chapter -> chapter.id == chapterId } != null
    }?.volumeId ?: return ChapterContent.empty(chapterId)
    val soup = browserGet(context, "${ALICESW_HOST}/book/$volumeId/$chapterId.html", true)
    val title = soup?.selectFirst(".j_chapterName")?.text() ?: "未知"
    val content = soup?.selectFirst(".read-content")?.children()

    val lastChapter = Regex("/book/[^/]+/([^/]+)\\.html").find(
        soup?.selectFirst("#j_chapterPrev")?.attr("href") ?: ""
    )?.groupValues?.get(1) ?: ""

    val nextChapter = Regex("/book/[^/]+/([^/]+)\\.html").find(
        soup?.selectFirst("#j_chapterNext")?.attr("href") ?: ""
    )?.groupValues?.get(1) ?: ""

    return MutableChapterContent(
        id = chapterId, title = title, content = ContentBuilder().apply {
            val buffer = ArrayList<String>()
            content?.forEach {
                when (it.tag().name) {
                    "p" -> buffer.add("\u3000\u3000" + it.text().trim())
                    "img" -> {
                        if (buffer.isNotEmpty()) {
                            simpleText(buffer.joinToString("\n"))
                            buffer.clear()
                        }
                        image(it.attr("src").toUri())
                    }
                }

            }
            if (buffer.isNotEmpty()) {
                simpleText(buffer.joinToString("\n"))
                buffer.clear()
            }
        }.build(), lastChapter = lastChapter, nextChapter = nextChapter
    )
}