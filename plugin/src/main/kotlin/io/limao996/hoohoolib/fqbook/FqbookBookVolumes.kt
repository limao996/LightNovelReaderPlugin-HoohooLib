package io.limao996.hoohoolib.fqbook

import io.limao996.hoohoolib.utils.httpGet
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.ChapterInformation
import io.nightfish.lightnovelreader.api.book.Volume

suspend fun FqbookBookVolumes(id: String): BookVolumes {
    val soup = httpGet(
        "$FQBOOK_HOST/chapterList-$id.html", true
    )?.selectFirst("div.page_main") ?: return BookVolumes.empty()

    val volumeTitles = soup.select("p.section_title")
    val volumeLists = soup.select("ul.section_list")

    return BookVolumes(
        id, volumeLists.mapIndexed { index, elements ->
            Volume(
                volumeId = index.toString(),
                volumeTitle = volumeTitles[index].text(),
                chapters = elements.select("li").map {
                    val info = it.selectFirst("a")
                    ChapterInformation(
                        id = info?.attr("href")?.removePrefix("read-")?.removeSuffix(".html") ?: "",
                        title = info?.text() ?: ""
                    )
                })
        })
}
