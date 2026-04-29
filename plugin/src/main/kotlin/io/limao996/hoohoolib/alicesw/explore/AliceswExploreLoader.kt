package io.limao996.hoohoolib.alicesw.explore

import androidx.core.net.toUri
import io.limao996.hoohoolib.alicesw.ALICESW_HOST
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.MutableBookInformation
import io.nightfish.lightnovelreader.api.book.WordCount
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import io.limao996.hoohoolib.alicesw.AliceswBookInformation
import io.limao996.hoohoolib.utils.httpGet
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

suspend fun loadSimpleBookList(
    category: ExploreCategory,
    page: Int = 1,
    order: ExploreCategory.Order = ExploreCategory.Order.UpdateTime
): List<BookInformation> {
    val soup = httpGet(ALICESW_HOST + category.getUrl(page, order), true)
    return soup?.selectFirst(".rec_rullist")?.children()?.map { item ->
        val titleDoc = item?.selectFirst(".two a")
        val title = titleDoc?.text()?.removeSuffix("全文阅读") ?: ""
        val id = titleDoc?.attr("href")?.removePrefix("/novel/")?.removeSuffix(".html") ?: ""
        val author = item.selectFirst(".four")?.text() ?: "未知"
        val wordCount =
            item.selectFirst(".five")?.text()?.replace(",", "")?.removeSuffix("万")?.toFloatOrNull()
                ?.times(10000)?.toInt() ?: 0
        val lastUpdated = item.selectFirst(".six")?.text()?.let {
            val time = if (it.contains(':')) it else "$it 00:00"
            LocalDateTime.parse(
                time, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            )
        } ?: LocalDateTime.now()

        MutableBookInformation(
            id = id,
            title = title,
            author = author,
            subtitle = "",
            coverUrl = "https://img.321cdn.com/img/01.png".toUri(),
            description = "",
            tags = emptyList(),
            publishingHouse = "爱丽丝书屋🎓",
            wordCount = WordCount(wordCount),
            lastUpdated = lastUpdated,
            isComplete = false,
        )
    } ?: emptyList()
}

suspend fun loadCompleteBookList(
    category: ExploreCategory,
    page: Int = 1,
    order: ExploreCategory.Order = ExploreCategory.Order.UpdateTime
): List<Deferred<BookInformation>> = withContext(Dispatchers.IO) {
    val soup = httpGet(ALICESW_HOST + category.getUrl(page, order), true)

    soup?.selectFirst(".rec_rullist")?.children()?.mapNotNull { item ->
        item?.selectFirst(".two a")?.attr("href")?.removePrefix("/novel/")?.removeSuffix(".html")
    }?.map { id ->
        async {
            AliceswBookInformation(id)
        }
    } ?: emptyList()
}