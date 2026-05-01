package io.limao996.hoohoolib.alicesw.explore

import io.limao996.hoohoolib.alicesw.ALICESW_HOST
import io.nightfish.lightnovelreader.api.web.explore.ExploreExpandedPageDataSource
import io.nightfish.lightnovelreader.api.web.explore.ExplorePageProvider
import io.nightfish.lightnovelreader.api.web.explore.ExploreTapPageDataSource

data class ExploreCategory(
    val name: String, val path: String, val supportOrder: Boolean, val supportMultiPage: Boolean
) {
    fun getUrl(page: Int = 1, order: Order = Order.UpdateTime): String {
        var path = this.path
        if (supportOrder) path += order.suffix
        if (supportMultiPage) path += "?page=$page"
        return "$ALICESW_HOST$path"
    }

    enum class Order(val tag: String, val suffix: String) {
        UpdateTime("更新时间", "/parameters/update_time+desc.html"), Word(
            "总字数", "/parameters/word+desc.html"
        ),
        Hits("人气值", "/parameters/hits+desc.html"),
    }
}

object AliceswExplorePageProvider : ExplorePageProvider.DefaultExplorePageProvider {
    val categories = listOf(
        ExploreCategory( // 索引 0
            name = "🕹️ 原创精选",
            path = "/original.html",
            supportOrder = false,
            supportMultiPage = true
        ),
        ExploreCategory( // 索引 1
            name = "📓 海量书库", path = "/all", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 2
            name = "🔥 今日排行",
            path = "/other/rank_hits/parameters/hits_day.html",
            supportOrder = false,
            supportMultiPage = false
        ),
        ExploreCategory( // 索引 3
            name = "📅 本周排行",
            path = "/other/rank_hits/parameters/hits_week.html",
            supportOrder = false,
            supportMultiPage = false
        ),
        ExploreCategory( // 索引 4
            name = "🌙 本月排行",
            path = "/other/rank_hits/parameters/hits_month.html",
            supportOrder = false,
            supportMultiPage = false
        ),
        ExploreCategory( // 索引 5
            name = "🏆 年度总榜",
            path = "/other/rank_hits/parameters/hits.html",
            supportOrder = false,
            supportMultiPage = false
        ),
        ExploreCategory( // 索引 6
            name = "🚀 科幻", path = "/all/id/71", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 7
            name = "🏫 校园", path = "/all/id/61", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 8
            name = "✨ 玄幻", path = "/all/id/62", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 9
            name = "🌾 乡村", path = "/all/id/63", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 10
            name = "🏙️ 都市", path = "/all/id/64", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 11
            name = "⚠️ 乱伦", path = "/all/id/65", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 12
            name = "📜 历史", path = "/all/id/67", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 13
            name = "⚔️ 武侠", path = "/all/id/68", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 14
            name = "🕹️ 系统", path = "/all/id/69", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 15
            name = "⭐ 明星", path = "/all/id/72", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 16
            name = "👥 同人", path = "/all/id/73", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 17
            name = "⛓️ 强奸", path = "/all/id/74", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 18
            name = "🔮 奇幻", path = "/all/id/75", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 19
            name = "🏛️ 经典", path = "/all/id/79", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 20
            name = "⏳ 穿越", path = "/all/id/70", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 21
            name = "💢 凌辱", path = "/all/id/46", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 22
            name = "🎭 反差", path = "/all/id/22", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 23
            name = "📉 堕落", path = "/all/id/18", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 24
            name = "💖 纯爱", path = "/all/id/19", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 25
            name = "🎀 伪娘", path = "/all/id/52", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 26
            name = "🍭 萝莉", path = "/all/id/48", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 27
            name = "👠 熟女", path = "/all/id/56", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 28
            name = "🚫 禁忌", path = "/all/id/51", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 29
            name = "💔 NTR", path = "/all/id/54", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 30
            name = "🌍 媚黑", path = "/all/id/53", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 31
            name = "🎩 绿帽", path = "/all/id/55", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 32
            name = "🔞 调教", path = "/all/id/58", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 33
            name = "👩 女主", path = "/all/id/59", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 34
            name = "🧒 正太", path = "/all/id/50", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 35
            name = "⬆️ 下克上", path = "/all/id/43", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 36
            name = "🌸 百合", path = "/all/id/47", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 37
            name = "💀 重口", path = "/all/id/21", supportOrder = true, supportMultiPage = true
        ),
        ExploreCategory( // 索引 38
            name = "📦 其他", path = "/all/id/57", supportOrder = true, supportMultiPage = true
        ),
    )

    override val explorePageIdList: List<String> = listOf(
        "Home", "Rankings", "Categories"
    ) + categories.map { it.name }
    override val exploreTapPageDataSourceMap: Map<String, ExploreTapPageDataSource> = mapOf(
        "Home" to AliceswCustomExploreTapPageDataSource("首页", 0, 2),
        "Rankings" to AliceswCustomExploreTapPageDataSource("排行", 2, 6),
        "Categories" to AliceswCustomExploreTapPageDataSource("分类", 6, 39),
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