package io.limao996.hoohoolib.alicesw.explore

import io.nightfish.lightnovelreader.api.web.explore.ExploreExpandedPageDataSource
import io.nightfish.lightnovelreader.api.web.explore.ExplorePageProvider
import io.nightfish.lightnovelreader.api.web.explore.ExploreTapPageDataSource

data class ExploreCategory(
    val name: String, val url: String, val supportOrder: Boolean, val supportMultiPage: Boolean
) {
    fun getUrl(page: Int = 1, order: Order = Order.UpdateTime): String {
        var url = this.url
        if (supportOrder) url += order.suffix
        if (supportMultiPage) url += "?page=$page"
        return url
    }

    enum class Order(val tag: String, val suffix: String) {
        UpdateTime("更新时间", "/order/update_time+desc.html"), Word(
            "总字数", "/order/word+desc.html"
        ),
        Hits("人气值", "/order/hits+desc.html"),
    }
}

object AliceswExplorePageProvider : ExplorePageProvider.DefaultExplorePageProvider {
    val categories = listOf(
        ExploreCategory( // 索引 0
            name = "🕹️ 原创精选",
            url = "/original.html",
            supportOrder = false,
            supportMultiPage = true
        ),
        ExploreCategory( // 索引 1
            name = "📓 海量书库", url = "/all", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 2
            name = "🔥 今日排行",
            url = "/other/rank_hits/order/hits_day.html",
            supportOrder = false,
            supportMultiPage = false
        ),
        ExploreCategory( // 索引 3
            name = "📅 本周排行",
            url = "/other/rank_hits/order/hits_week.html",
            supportOrder = false,
            supportMultiPage = false
        ),
        ExploreCategory( // 索引 4
            name = "🌙 本月排行",
            url = "/other/rank_hits/order/hits_month.html",
            supportOrder = false,
            supportMultiPage = false
        ),
        ExploreCategory( // 索引 5
            name = "🏆 年度总榜",
            url = "/other/rank_hits/order/hits.html",
            supportOrder = false,
            supportMultiPage = false
        ),
        ExploreCategory( // 索引 6
            name = "🚀 科幻", url = "/all/id/71", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 7
            name = "🏫 校园", url = "/all/id/61", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 8
            name = "✨ 玄幻", url = "/all/id/62", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 9
            name = "🌾 乡村", url = "/all/id/63", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 10
            name = "🏙️ 都市", url = "/all/id/64", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 11
            name = "⚠️ 乱伦", url = "/all/id/65", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 12
            name = "📜 历史", url = "/all/id/67", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 13
            name = "⚔️ 武侠", url = "/all/id/68", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 14
            name = "🕹️ 系统", url = "/all/id/69", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 15
            name = "⭐ 明星", url = "/all/id/72", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 16
            name = "👥 同人", url = "/all/id/73", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 17
            name = "⛓️ 强奸", url = "/all/id/74", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 18
            name = "🔮 奇幻", url = "/all/id/75", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 19
            name = "🏛️ 经典", url = "/all/id/79", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 20
            name = "⏳ 穿越", url = "/all/id/70", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 21
            name = "💢 凌辱", url = "/all/id/46", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 22
            name = "🎭 反差", url = "/all/id/22", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 23
            name = "📉 堕落", url = "/all/id/18", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 24
            name = "💖 纯爱", url = "/all/id/19", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 25
            name = "🎀 伪娘", url = "/all/id/52", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 26
            name = "🍭 萝莉", url = "/all/id/48", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 27
            name = "👠 熟女", url = "/all/id/56", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 28
            name = "🚫 禁忌", url = "/all/id/51", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 29
            name = "💔 NTR", url = "/all/id/54", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 30
            name = "🌍 媚黑", url = "/all/id/53", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 31
            name = "🎩 绿帽", url = "/all/id/55", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 32
            name = "🔞 调教", url = "/all/id/58", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 33
            name = "👩 女主", url = "/all/id/59", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 34
            name = "🧒 正太", url = "/all/id/50", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 35
            name = "⬆️ 下克上", url = "/all/id/43", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 36
            name = "🌸 百合", url = "/all/id/47", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 37
            name = "💀 重口", url = "/all/id/21", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 38
            name = "📦 其他", url = "/all/id/57", supportOrder = true, supportMultiPage = true
        ),
    )

    override val explorePageIdList: List<String> = listOf(
        "Home", "Rankings", "Categories"
    ) + categories.map { it.name }
    override val exploreTapPageDataSourceMap: Map<String, ExploreTapPageDataSource> = mapOf(
        "Home" to CustomExploreTapPageDataSource("首页", 0, 2),
        "Rankings" to CustomExploreTapPageDataSource("排行", 2, 6),
        "Categories" to CustomExploreTapPageDataSource("分类", 6, 39),
    )
    override val exploreExpandedPageDataSourceMap: Map<String, ExploreExpandedPageDataSource> =
        run {
            val map = LinkedHashMap<String, ExploreExpandedPageDataSource>()
            categories.forEach {
                map[it.name] = AliceswExploreExpandedPageDataSource(it)
            }
            map.toMap()
        }
}