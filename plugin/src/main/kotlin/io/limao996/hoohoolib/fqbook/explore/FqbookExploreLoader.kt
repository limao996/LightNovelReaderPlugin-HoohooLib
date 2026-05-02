package io.limao996.hoohoolib.fqbook.explore

import androidx.core.net.toUri
import io.limao996.hoohoolib.fqbook.FQBOOK_HOST
import io.limao996.hoohoolib.utils.httpGet
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.MutableBookInformation
import io.nightfish.lightnovelreader.api.book.WordCount
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private fun buildCategoryUrl(
    pageNum: Int = 1,
    pageSize: Int = 30,
    catId: String = "-1",
    size: String = "-1",
    isFinish: String = "-1",
    updT: String = "-1",
    orderBy: String = "-1"
): String {
    return "$FQBOOK_HOST/m/category.html?pageNum=$pageNum&pageSize=$pageSize&catId=$catId&size=$size&isFinish=$isFinish&updT=$updT&orderBy=$orderBy"
}

object FqbookExploreLoader {
    data class Parameters(
        val catId: String = "-1",
        val size: String = "-1",
        val isFinish: String = "-1",
        val updT: String = "-1",
        val orderBy: String = "-1"
    ) {
        companion object {

            // 分类 (catId)
            val category = mapOf(
                "不限" to "-1",
                "穿越" to "chuanyue",
                "异能" to "yineng",
                "言情" to "yanqing",
                "玄幻" to "xuanhuan",
                "校园" to "xiaoyuan",
                "仙侠" to "xianxia",
                "乡土" to "xiangtu",
                "武侠" to "wuxia",
                "网游" to "wangyou",
                "同人" to "tongren",
                "女尊" to "nvzun",
                "历史" to "lishi",
                "惊悚" to "jingsong",
                "古典" to "gudian",
                "官场" to "guanchang",
                "都市" to "dushi",
                "单篇" to "danpian",
                "耽美" to "danmei",
                "职场" to "zhichang"
            )

            // 作品字数 (size)
            val wordCount = mapOf(
                "不限" to "-1",
                "30万以下" to "30",
                "30~50万" to "50",
                "50~100万" to "100",
                "100~200万" to "200",
                "200万以上" to "999"
            )

            // 是否完结 (isFinish)
            val finishStatus = mapOf(
                "不限" to "-1", "连载中" to "0", "已完结" to "1"
            )

            // 更新时间 (updT)
            val updateTime = mapOf(
                "不限" to "-1",
                "三日内" to "3",
                "七日内" to "7",
                "半月内" to "15",
                "一月内" to "30",
                "三月内" to "90"
            )

            // 排序方式 (orderBy)
            val orderBy = mapOf(
                "综合" to "-1",
                "字数" to "wordcount",
                "点击" to "click",
                "更新" to "update",
                "发布" to "public"
            )
        }
    }

    suspend fun get(
        pageNum: Int = 1, pageSize: Int = 30, params: Parameters = Parameters()
    ): List<BookInformation> {
        val url = params.run {
            buildCategoryUrl(
                pageNum, pageSize, catId, size, isFinish, updT, orderBy
            )
        }
        val items = httpGet(url)?.selectFirst("div.book-all-list div.bd ul")?.children()
            ?: return emptyList()
        return items.map {
            val item = it.selectFirst(".right") ?: return@map BookInformation.empty()

            val titleDoc = item.selectFirst("a.name") ?: return@map BookInformation.empty()
            val id = titleDoc.attr("href").removePrefix("book-").removeSuffix(".html")
            val title = titleDoc.text()

            val author = item.selectFirst("p.info")?.ownText()?.trim()?.removePrefix("作者：") ?: ""
            val wordCount =
                item.selectFirst(".words")?.text()?.removePrefix("字数：")?.toIntOrNull() ?: 0


            val lastUpdated = item.selectFirst("p.update span.time")?.text()?.let {
                try {
                    val formatter = DateTimeFormatter.ofPattern("(yyyy-MM-dd HH:mm:ss)")
                    LocalDateTime.parse(it, formatter)
                } catch (e: Exception) {
                    LocalDateTime.now()
                }
            } ?: LocalDateTime.now()

            MutableBookInformation(
                id = id,
                title = title,
                subtitle = "",
                coverUrl = "$FQBOOK_HOST/img-$id.jpg".toUri(),
                author = author,
                description = "",
                tags = emptyList(),
                publishingHouse = "疯情书库🔞",
                wordCount = WordCount(wordCount),
                lastUpdated = lastUpdated,
                isComplete = false
            )
        }
    }
}