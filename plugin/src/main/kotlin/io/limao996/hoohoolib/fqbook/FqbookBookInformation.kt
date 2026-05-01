package io.limao996.hoohoolib.fqbook

import android.net.Uri
import androidx.core.net.toUri
import io.limao996.hoohoolib.utils.httpGet
import io.limao996.hoohoolib.utils.infoLog
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.MutableBookInformation
import io.nightfish.lightnovelreader.api.book.WordCount
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

suspend fun FqbookBookInformation(
    id: String
): BookInformation {
    val soup = httpGet("$FQBOOK_HOST/m/book-$id.html")?.selectFirst(".container")
        ?: return MutableBookInformation.empty()

    val title = soup.selectFirst(".detail .column-2 .right h2")?.text() ?: ""
    val coverUrl = soup.selectFirst(".detail .column-2 .left a img")?.attr("src")?.let {
        FQBOOK_HOST + it.removePrefix("..")
    }?.toUri() ?: Uri.EMPTY
    val info = soup.selectFirst(".detail .column-2 .info")?.textNodes()
        ?: return MutableBookInformation.empty()

    val author = info[0].text().trim().removePrefix("作者：")
    val wordCount =
        info[1].text().trim().removePrefix("字数：").removeSuffix(" 字").toIntOrNull() ?: 0

    val description = soup.selectFirst(".book-intro .bd")?.wholeText()?.trim()?.split("\n")
        ?.joinToString("\n") {
            "ㅤㅤ${it.trim()}"
        } ?: ""

    val lastUpdated = soup.selectFirst("div.mod.block.update span.time")?.text()?.let {
        val formatter = DateTimeFormatter.ofPattern("yyyy/M/d H:m")
        LocalDateTime.parse(it, formatter)
    } ?: LocalDateTime.now()

    val isComplete = soup.selectFirst(".detail .column-2 .right span.status")?.text() == "已完结"

    return MutableBookInformation(
        id = id,
        title = title,
        subtitle = "",
        coverUrl = coverUrl,
        author = author,
        description = description,
        tags = emptyList(),
        publishingHouse = "疯情书库🔞",
        wordCount = WordCount(wordCount),
        lastUpdated = lastUpdated,
        isComplete = isComplete
    )
}
