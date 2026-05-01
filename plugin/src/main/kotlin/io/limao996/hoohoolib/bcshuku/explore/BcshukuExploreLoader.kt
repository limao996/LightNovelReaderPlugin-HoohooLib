package io.limao996.hoohoolib.bcshuku.explore

import android.net.Uri
import androidx.core.net.toUri
import io.limao996.hoohoolib.alicesw.ALICESW_HOST
import io.limao996.hoohoolib.alicesw.explore.ExploreCategory
import io.limao996.hoohoolib.bcshuku.BCSHUKU_HOST
import io.limao996.hoohoolib.utils.httpGet
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.MutableBookInformation
import io.nightfish.lightnovelreader.api.book.WordCount
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

suspend fun loadSimpleBookList(
    category: io.limao996.hoohoolib.bcshuku.explore.ExploreCategory,
    page: Int = 1,
): List<BookInformation> {
    val soup = httpGet(category.getUrl(page), false)

    return soup?.select("div.one-row > div.col-md-3.col-sm-6.col-xs-6.home-truyendecu")
        ?.map { item ->

            val link = item.selectFirst(".each_truyen a")
            val bookUrl = link?.attr("href") ?: return@map MutableBookInformation.empty()
            val title =
                item.selectFirst(".caption a h3")?.text() ?: link.attr("title")
            val coverUrl = item.selectFirst(".each_truyen a img")?.attr("src")?.let {
                if (it.startsWith("http")) it.toUri() else "$BCSHUKU_HOST$it".toUri()
            } ?: Uri.EMPTY

            val lastUpdated = item.selectFirst(".caption")?.child(4)?.text()?.let {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                LocalDateTime.parse("$it 00:00", formatter)
            } ?: LocalDateTime.now()

            val isComplete = item.selectFirst(".caption .hoan-thanh-mau")?.text() != "连载"

            MutableBookInformation(
                id = bookUrl,
                title = title,
                subtitle = "",
                coverUrl = coverUrl,
                author = "",
                description = "",
                tags = emptyList(),
                publishingHouse = "八叉书库🔞",
                wordCount = WordCount(0),
                lastUpdated = lastUpdated,
                isComplete = isComplete
            )
        } ?: emptyList()
}
