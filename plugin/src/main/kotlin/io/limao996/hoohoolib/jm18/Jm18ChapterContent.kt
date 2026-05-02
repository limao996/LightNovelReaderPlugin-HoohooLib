package io.limao996.hoohoolib.jm18

import androidx.core.net.toUri
import io.limao996.hoohoolib.jm18.utils.buildDecryptedImageUrl
import io.limao996.hoohoolib.utils.httpGet
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.book.LocalBookDataSourceApi
import io.nightfish.lightnovelreader.api.book.MutableChapterContent
import io.nightfish.lightnovelreader.api.content.builder.ContentBuilder
import io.nightfish.lightnovelreader.api.content.builder.image
import io.nightfish.lightnovelreader.api.content.builder.simpleText

suspend fun Jm18ChapterContent(
    chapterId: String,
    bookId: String,
    localBookDataSourceApi: LocalBookDataSourceApi,
): ChapterContent {
    val bookType = bookId.split(':').first()

    if (bookType == "novel") return Jm18NovelChapterContent(
        chapterId, bookId, localBookDataSourceApi
    )

    return Jm18ComicChapterContent(chapterId, bookId, localBookDataSourceApi)
}

suspend fun Jm18NovelChapterContent(
    chapterId: String,
    bookId: String,
    localBookDataSourceApi: LocalBookDataSourceApi,
): ChapterContent {
    val rawBookId = bookId.split(':').last()
    val rawChapterId = chapterId.split(':').last()

    val soup = httpGet(
        "$JM18_HOST/novel_chapter/$rawBookId/$rawChapterId.html", true
    ) ?: return ChapterContent.empty()

    val volumes = localBookDataSourceApi.getBookVolumes(bookId)!!.volumes
    val flatChapter = volumes.flatMap { volume -> volume.chapters }
    val flatChapterIds = flatChapter.map { it.id }
    val currentIndex = flatChapterIds.indexOf(chapterId)
    val prevId = flatChapterIds.getOrNull(currentIndex - 1)
    val nextId = flatChapterIds.getOrNull(currentIndex + 1)

    val title = flatChapter[currentIndex].title
    val content = soup.selectFirst("main div.article")?.children() ?: return ChapterContent.empty()

    return MutableChapterContent(
        id = chapterId, title = title, content = ContentBuilder().apply {
            val buffer = ArrayList<String>()
            content.forEach {
                when (it.tag().name) {
                    "p", "div" -> it.wholeText().trim().split("\n").filter { it.isNotBlank() }
                        .also { if (it.isEmpty()) return@forEach }.joinToString("\n\n") {
                            "ㅤㅤ${it.trim()}"
                        }.let(buffer::add)

                    "img" -> {
                        if (buffer.isNotEmpty()) {
                            simpleText(buffer.joinToString("\n\n"))
                            buffer.clear()
                        }
                        image(it.attr("data-src").let(::buildDecryptedImageUrl).toUri())
                    }
                }

            }
            if (buffer.isNotEmpty()) {
                simpleText(buffer.joinToString("\n\n"))
                buffer.clear()
            }
        }.build(), lastChapter = prevId ?: "", nextChapter = nextId ?: ""
    )
}

suspend fun Jm18ComicChapterContent(
    chapterId: String,
    bookId: String,
    localBookDataSourceApi: LocalBookDataSourceApi,
): ChapterContent {
    val rawBookId = bookId.split(':').last()
    val rawChapterId = chapterId.split(':').last()

    val soup = httpGet(
        "$JM18_HOST/comic/chapter/$rawBookId/$rawChapterId.html", true
    ) ?: return ChapterContent.empty()

    val volumes = localBookDataSourceApi.getBookVolumes(bookId)!!.volumes
    val flatChapter = volumes.flatMap { volume -> volume.chapters }
    val flatChapterIds = flatChapter.map { it.id }
    val currentIndex = flatChapterIds.indexOf(chapterId)
    val prevId = flatChapterIds.getOrNull(currentIndex - 1)
    val nextId = flatChapterIds.getOrNull(currentIndex + 1)

    val title = flatChapter[currentIndex].title
    val content = soup.selectFirst("main section")?.children() ?: return ChapterContent.empty()

    return MutableChapterContent(
        id = chapterId, title = title, content = ContentBuilder().apply {
            val buffer = ArrayList<String>()
            content.forEach {
                when (it.tag().name) {
                    "p" -> it.wholeText().trim().split("\n").filter { it.isNotBlank() }
                        .also { if (it.isEmpty()) return@forEach }.joinToString("\n\n") {
                            "ㅤㅤ${it.trim()}"
                        }.let(buffer::add)

                    "img" -> {
                        if (buffer.isNotEmpty()) {
                            simpleText(buffer.joinToString("\n\n"))
                            buffer.clear()
                        }
                        image(it.attr("data-src").let(::buildDecryptedImageUrl).toUri())
                    }
                }

            }
            if (buffer.isNotEmpty()) {
                simpleText(buffer.joinToString("\n\n"))
                buffer.clear()
            }
        }.build(), lastChapter = prevId ?: "", nextChapter = nextId ?: ""
    )
}