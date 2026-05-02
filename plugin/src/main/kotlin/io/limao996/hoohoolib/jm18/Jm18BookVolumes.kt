package io.limao996.hoohoolib.jm18

import io.limao996.hoohoolib.utils.httpGet
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.ChapterInformation
import io.nightfish.lightnovelreader.api.book.Volume


suspend fun Jm18BookVolumes(
    id: String
): BookVolumes {
    val (type, rawId) = id.split(':')

    val soup = httpGet("$JM18_HOST/$type/detail/$rawId")?.selectFirst("div.detail-page__inner")
        ?: return BookVolumes.empty()

    val chapters =
        soup.selectFirst(".detail-page__catalog-list")?.children() ?: return BookVolumes.empty()
    return BookVolumes(
        id, listOf(
            Volume(
                volumeId = id,
                volumeTitle = "正文",
                chapters = chapters.mapIndexed { index, elements ->
                    ChapterInformation(
                        id = "$id:" + (index + 1).toString(),
                        title = elements.selectFirst(".detail-page__catalog-left span.detail-page__chapter-title")
                            ?.text() ?: ""
                    )
                })
        )
    )
}