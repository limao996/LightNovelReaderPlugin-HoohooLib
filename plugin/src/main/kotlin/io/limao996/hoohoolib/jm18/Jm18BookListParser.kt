package io.limao996.hoohoolib.jm18

import android.net.Uri
import androidx.core.net.toUri
import io.limao996.hoohoolib.jm18.utils.buildDecryptedImageUrl
import io.nightfish.lightnovelreader.api.book.MutableBookInformation
import io.nightfish.lightnovelreader.api.book.WordCount
import io.nightfish.lightnovelreader.api.web.search.SearchResult
import org.jsoup.nodes.Element
import java.time.LocalDateTime

fun Jm18BookListParser(type: String, soup: Element): List<MutableBookInformation>? {
    val items = soup.selectFirst("ul.dx-novel-list")?.children() ?: emptyList()

    if (items.isEmpty()) {
        return null
    }

    return items.map {
        if (type == "novel") getNovelInfo(
            it
        ) else getComicInfo(it)
    }
}


private fun getComicInfo(item: Element): MutableBookInformation {
    val box = item.child(0)
    val id = box.attr("href").removePrefix("/comic/detail/")

    val top = box.child(0)

    val poster = top.selectFirst("div.poster")
    val coverUrl =
        poster?.child(0)?.attr("data-src")?.let(::buildDecryptedImageUrl)?.toUri() ?: Uri.EMPTY
    val info2 = poster?.child(1)
    val original = info2?.child(0)?.text()?.trim()?.let { listOf(it) } ?: emptyList()
    val isComplete = info2?.child(1)?.text()?.trim() == "完结"

    val bottom = box.child(1)

    val title = bottom.selectFirst("div h3")?.text() ?: ""
    val info = bottom.child(1)
    val author = info.child(0).text()
    val category = info.child(1).text().let { listOf(it) }
    return MutableBookInformation(
        id = "comic:$id",
        title = title,
        subtitle = "",
        coverUrl = coverUrl,
        author = author,
        description = "",
        tags = category + original,
        publishingHouse = "禁漫天堂-漫画🔞",
        wordCount = WordCount(0),
        lastUpdated = LocalDateTime.now(),
        isComplete = isComplete
    )
}

private fun getNovelInfo(item: Element): MutableBookInformation {
    val top = item.child(0)

    val id = top.attr("href").removePrefix("/novel/detail/")
    val poster = top.selectFirst("div.poster")
    val coverUrl =
        poster?.child(0)?.attr("data-src")?.let(::buildDecryptedImageUrl)?.toUri() ?: Uri.EMPTY
    val info2 = poster?.child(1)
    val original = info2?.child(0)?.text()?.trim()?.let { listOf(it) } ?: emptyList()
    val isComplete = info2?.child(1)?.text()?.trim() == "已完结"

    val bottom = item.child(1)

    val title = bottom.selectFirst("div a h3")?.text() ?: ""
    val info = bottom.child(1)
    val author = info.child(0).text()
    val category = info.child(1).child(0).text().let { listOf(it) }
    return MutableBookInformation(
        id = "novel:$id",
        title = title,
        subtitle = "",
        coverUrl = coverUrl,
        author = author,
        description = "",
        tags = category + original,
        publishingHouse = "禁漫天堂-小说🔞",
        wordCount = WordCount(0),
        lastUpdated = LocalDateTime.now(),
        isComplete = isComplete
    )
}