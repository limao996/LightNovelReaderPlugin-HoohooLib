package io.limao996.hoohoolib.jm18

import android.net.Uri
import androidx.core.net.toUri
import io.limao996.hoohoolib.jm18.utils.buildDecryptedImageUrl
import io.limao996.hoohoolib.utils.httpGet
import io.limao996.hoohoolib.utils.infoLog
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.ChapterInformation
import io.nightfish.lightnovelreader.api.book.MutableBookInformation
import io.nightfish.lightnovelreader.api.book.Volume
import io.nightfish.lightnovelreader.api.book.WordCount
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.collections.emptyList


suspend fun Jm18BookInformation(
    id: String
): BookInformation {
    val (type, rawId) = id.split(':')

    val soup = httpGet("$JM18_HOST/$type/detail/$rawId")?.selectFirst("div.detail-page__inner")
        ?: return BookInformation.empty()

    val title = soup.selectFirst(".dx-title.detail-page__title")?.text() ?: ""
    val coverUrl = soup.selectFirst("img.detail-page__poster-img")?.attr("data-src")
        ?.let(::buildDecryptedImageUrl)?.toUri() ?: Uri.EMPTY
    val author =
        soup.selectFirst("div.detail-page__meta-row")?.child(1)?.child(1)?.child(0)?.text() ?: ""
    val description = soup.selectFirst("div.detail-intro__body")?.children()?.joinToString("\n\n") {
        it.wholeText().trim().split("\n").filter { it.isNotBlank() }.joinToString("\n") { p ->
            "ㅤㅤ${p.trim()}"
        }
    } ?: ""
    val lastUpdated = soup.selectFirst("div.detail-page__meta-row")?.child(3)?.child(1)?.text()
        ?.removeSuffix(" 更新")?.let {
            try {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                LocalDateTime.parse("$it 00:00", formatter)
            } catch (e: Exception) {
                LocalDateTime.now()
            }
        } ?: LocalDateTime.now()

    val isComplete =
        soup.selectFirst("span.detail-page__cover-corner-badge--muted")?.text() != "连载中"

    val source = soup.selectFirst("div.detail-page__meta-row")?.child(0)?.child(1)?.child(0)?.text()
        ?.let(::listOf) ?: emptyList()
    val primary =
        soup.selectFirst("span.detail-page__cover-corner-badge--primary")?.text()?.let(::listOf)
            ?: emptyList()
    val tags = soup.selectFirst("div.detail-page__tags")?.children()?.map {
        "#" + it.text()
    } ?: emptyList()

    return MutableBookInformation(
        id = id,
        title = title,
        subtitle = "",
        coverUrl = coverUrl,
        author = author,
        description = description,
        tags = source + primary + tags,
        publishingHouse = if (type == "novel") "禁漫天堂-小说🔞" else "禁漫天堂-漫画🔞",
        wordCount = WordCount(0),
        lastUpdated = lastUpdated,
        isComplete = isComplete
    )
}