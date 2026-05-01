package io.limao996.hoohoolib.fqbook.explore

import io.nightfish.lightnovelreader.api.web.explore.ExploreExpandedPageDataSource
import io.nightfish.lightnovelreader.api.web.explore.ExplorePageProvider
import io.nightfish.lightnovelreader.api.web.explore.ExploreTapPageDataSource

object FqbookExplorePageProvider : ExplorePageProvider.DefaultExplorePageProvider {
    object ExplorePageMap {
        val home = FqbookExploreLoader.Parameters.run {
            mapOf(
                "📚 海量书库" to FqbookExploreLoader.Parameters(),
                "🔥 热门完本" to FqbookExploreLoader.Parameters(
                    updT = updateTime["三月内"]!!,
                    isFinish = finishStatus["已完结"]!!,
                    orderBy = orderBy["点击"]!!
                ),
                "⏳ 限时追更" to FqbookExploreLoader.Parameters(
                    updT = updateTime["七日内"]!!,
                    isFinish = finishStatus["连载中"]!!,
                    orderBy = orderBy["更新"]!!
                ),
                "💎 口碑神作" to FqbookExploreLoader.Parameters(
                    isFinish = finishStatus["已完结"]!!,
                    orderBy = orderBy["点击"]!!
                ),
                "🌱 新书速递" to FqbookExploreLoader.Parameters(
                    size = wordCount["30万以下"]!!,
                    updT = updateTime["一月内"]!!,
                    isFinish = finishStatus["连载中"]!!,
                    orderBy = orderBy["发布"]!!
                ),
            )
        }
        val categories = FqbookExploreLoader.Parameters.run {
            mapOf(
                "📖 穿越" to FqbookExploreLoader.Parameters(catId = category["穿越"]!!),
                "⚡ 异能" to FqbookExploreLoader.Parameters(catId = category["异能"]!!),
                "💖 言情" to FqbookExploreLoader.Parameters(catId = category["言情"]!!),
                "✨ 玄幻" to FqbookExploreLoader.Parameters(catId = category["玄幻"]!!),
                "🏫 校园" to FqbookExploreLoader.Parameters(catId = category["校园"]!!),
                "⛰️ 仙侠" to FqbookExploreLoader.Parameters(catId = category["仙侠"]!!),
                "🌾 乡土" to FqbookExploreLoader.Parameters(catId = category["乡土"]!!),
                "⚔️ 武侠" to FqbookExploreLoader.Parameters(catId = category["武侠"]!!),
                "🎮 网游" to FqbookExploreLoader.Parameters(catId = category["网游"]!!),
                "🎭 同人" to FqbookExploreLoader.Parameters(catId = category["同人"]!!),
                "👑 女尊" to FqbookExploreLoader.Parameters(catId = category["女尊"]!!),
                "🏯 历史" to FqbookExploreLoader.Parameters(catId = category["历史"]!!),
                "👻 惊悚" to FqbookExploreLoader.Parameters(catId = category["惊悚"]!!),
                "🏺 古典" to FqbookExploreLoader.Parameters(catId = category["古典"]!!),
                "🏛️ 官场" to FqbookExploreLoader.Parameters(catId = category["官场"]!!),
                "🏙️ 都市" to FqbookExploreLoader.Parameters(catId = category["都市"]!!),
                "📄 单篇" to FqbookExploreLoader.Parameters(catId = category["单篇"]!!),
                "🌈 耽美" to FqbookExploreLoader.Parameters(catId = category["耽美"]!!),
                "💼 职场" to FqbookExploreLoader.Parameters(catId = category["职场"]!!),
            )
        }
    }

    override val explorePageIdList = listOf(
        "Home", "Categories"
    )
    override val exploreTapPageDataSourceMap: Map<String, ExploreTapPageDataSource> = mapOf(
        "Home" to FqbookCustomExploreTapPageDataSource(
            "首页", ExplorePageMap.home
        ),
        "Categories" to FqbookCustomExploreTapPageDataSource(
            "分类", ExplorePageMap.categories
        ),
    )
    override val exploreExpandedPageDataSourceMap: Map<String, ExploreExpandedPageDataSource> =
        run {
            val map = ExplorePageMap.home + ExplorePageMap.categories
            map.map { (title, order) ->
                title to FqbookCustomExploreExpandedPageDataSource(title, order)
            }.toMap()
        }

}