package io.limao996.hoohoolib.alicesw

import androidx.core.net.toUri
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.MutableBookInformation
import io.nightfish.lightnovelreader.api.book.WordCount
import io.limao996.hoohoolib.utils.httpGet
import java.time.LocalDateTime

suspend fun AliceswBookInformation(
    id: String
): BookInformation {
    val soup = httpGet("${ALICESW_HOST}/novel/$id.html")

    val detailBox = soup?.selectFirst(".detail-box")

    val title =
        detailBox?.selectFirst(".top .xs-title")?.text()?.removeSuffix("全文阅读") ?: "暂无标题"
    val description = soup?.selectFirst(".jianjie")?.text() ?: "暂无简介"
    val coverDoc = detailBox?.selectFirst(".imgbox img")
    val coverUrl = (coverDoc?.attr("src") ?: "https://img.321cdn.com/img/01.png").toUri()
    val author = detailBox?.selectFirst(".fix")?.child(1)?.selectFirst("a")?.text() ?: "未知"
    val classified =
        detailBox?.selectFirst(".fix")?.child(2)?.selectFirst("a")?.text()?.let { listOf(it) }
            ?: emptyList()
    val tags = (soup?.select(".tags .tg span a")?.map {
        it.ownText()
    } ?: emptyList())
    val subTitle = detailBox?.selectFirst(".fix")?.child(3)?.text() ?: ""
    val state = detailBox?.selectFirst(".fix")?.child(5)?.text() ?: ""
    val wordCountText = detailBox?.selectFirst(".fix")?.child(4)?.text() ?: ""
    val wordCount = Regex("字数：(\\S+)").find(wordCountText)?.groupValues?.get(1)?.replace(",", "")
        ?.removeSuffix("万")?.toFloatOrNull()?.times(10000)?.toInt() ?: 0

    return MutableBookInformation(
        id = id,
        title = title,
        subtitle = subTitle,
        coverUrl = coverUrl,
        author = author,
        description = description,
        tags = classified + tags,
        publishingHouse = "爱丽丝书屋🎓",
        wordCount = WordCount(wordCount),
        lastUpdated = LocalDateTime.now(),
        isComplete = state.contains("已完结")
    )
}