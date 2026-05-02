package io.limao996.hoohoolib.jm18.explore

import io.nightfish.lightnovelreader.api.web.explore.ExploreExpandedPageDataSource
import io.nightfish.lightnovelreader.api.web.explore.ExplorePageProvider
import io.nightfish.lightnovelreader.api.web.explore.ExploreTapPageDataSource

object Jm18ExplorePageProvider : ExplorePageProvider.DefaultExplorePageProvider {
    object Libs {
        val Home = listOf(
            "🏷️ 绅士小说" to Jm18NovelUrl() to Jm18NovelUrl.tags.subList(0, 1),
            "📚 涩涩漫画" to Jm18ComicUrl() to Jm18ComicUrl.tags.subList(0, 1),
        )
        val Novel = listOf(
            "📖 内容类型" to Jm18NovelUrl(Jm18NovelUrl.tags[1].second) to Jm18NovelUrl.tags.subList(
                1, 9
            ),        // 索引1-8：都市、校园、乡村、武侠、玄幻、科幻、历史、魔幻
            "⚙️ 剧情设定" to Jm18NovelUrl(Jm18NovelUrl.tags[9].second) to Jm18NovelUrl.tags.subList(
                9, 20
            ),       // 索引9-19：系统、穿越、同人、娱乐、贤者、末世、快穿、异世界、种田、官场、灵异
            "💕 情感关系" to Jm18NovelUrl(Jm18NovelUrl.tags[20].second) to Jm18NovelUrl.tags.subList(
                20, 39
            ),      // 索引20-38：乱伦、纯爱、后宫、百合、耽美、骨科、母子、父女、人妻、淫妻、绿母、绿奴、公媳、姐妹花、母女花、全家桶、交换伴侣、出轨、目前犯
            "👤 人物属性" to Jm18NovelUrl(Jm18NovelUrl.tags[39].second) to Jm18NovelUrl.tags.subList(
                39, 55
            ),      // 索引39-54：萝莉、熟女、人妖、伪娘、Futa、性转、校花、痴女、病娇、性奴、肉便器、种马、白虎、异种族、异国、小马拉大车
            "🎭 玩法行为" to Jm18NovelUrl(Jm18NovelUrl.tags[55].second) to Jm18NovelUrl.tags.subList(
                55, 75
            ),      // 索引55-74：1v1、NP、NTR、NTL、逆NTR、SM、调教、催眠、强奸、凌辱、露出、监禁、捆绑、群交、足交、恋足、榨精、受孕、产奶、触手
            "👘 装扮道具" to Jm18NovelUrl(Jm18NovelUrl.tags[75].second) to Jm18NovelUrl.tags.subList(
                75, 81
            ),      // 索引75-80：丝袜、制服、变装、道具、药物、改造
            "🎭 风格情感" to Jm18NovelUrl(Jm18NovelUrl.tags[81].second) to Jm18NovelUrl.tags.subList(
                81, 95
            ),      // 索引81-94：甜文、爽文、虐心、虐主、复仇、搞笑、浪漫、经典、好文笔、剧情、反差、狗血、暗黑、暴虐
            "📊 阅读标签" to Jm18NovelUrl(Jm18NovelUrl.tags[95].second) to Jm18NovelUrl.tags.subList(
                95, 109
            ),     // 索引95-108：BE、HE、SC、无绿、手枪文、微肉、微重口、重口、适合女生、女性视角、猎艳、神豪、AI辅助、红帽
            "🔖 特殊分类" to Jm18NovelUrl(Jm18NovelUrl.tags[109].second) to Jm18NovelUrl.tags.subList(
                109, 111
            )    // 索引109-110：下克上、有父
        )
        val Comic = listOf(
            "🏳️ 地区来源" to Jm18ComicUrl(Jm18ComicUrl.tags[1].second) to Jm18ComicUrl.tags.subList(
                1, 3
            ),   // 索引1-2：日漫、韩漫
            "📚 故事题材" to Jm18ComicUrl(Jm18ComicUrl.tags[3].second) to Jm18ComicUrl.tags.subList(
                3, 11
            ),  // 索引3-10：剧情、校园、爱情、奇幻、乱伦、后宫、同性、同人
            "👥 角色扮演" to Jm18ComicUrl(Jm18ComicUrl.tags[11].second) to Jm18ComicUrl.tags.subList(
                11, 28
            ), // 索引11-27：M男、单男、单女、扶她、双性、伪娘、性转换、正太、萝莉、御姐、姐姐、母亲、老师、护士、猫娘、兔女郎、妖精
            "🔥 外貌身体" to Jm18ComicUrl(Jm18ComicUrl.tags[28].second) to Jm18ComicUrl.tags.subList(
                28, 38
            ), // 索引28-37：黑皮、巨尻、大腚、大屌、兽耳、毛茸茸、眼睛、马尾、衣领、处女
            "👗 服装穿搭" to Jm18ComicUrl(Jm18ComicUrl.tags[38].second) to Jm18ComicUrl.tags.subList(
                38, 49
            ), // 索引38-48：比基尼、泳装、水手服、制服、女学生制服、内衣、丝袜、黑丝、裤袜、扮演、兽交
            "💋 玩法行为" to Jm18ComicUrl(Jm18ComicUrl.tags[49].second) to Jm18ComicUrl.tags.subList(
                49, 75
            )  // 索引49-74：3P、BL、NTR、PUA、调教、束缚、露出、强奸、群P、百合、口交、肛交、肛门、双穴、乳交、足交、足控、中出、潮吹、受精、怀孕、母乳、卖淫、援交、破处、恋父
        )
    }

    override val explorePageIdList: List<String> = listOf(
        "Home", "Novel", "Comic"
    ) + Libs.Home.map { it.first.first } + Libs.Comic.map { it.first.first }

    override val exploreTapPageDataSourceMap: Map<String, ExploreTapPageDataSource> = mapOf(
        "Home" to Jm18CustomExploreTapPageDataSource(
            "首页", Libs.Home.associate { (k, v) ->
                k.first to k.second
            }),
        "Novel" to Jm18CustomExploreTapPageDataSource(
            "小说", Libs.Novel.associate { (k, v) ->
                k.first to k.second
            }),
        "Comic" to Jm18CustomExploreTapPageDataSource(
            "漫画", Libs.Comic.associate { (k, v) ->
                k.first to k.second
            }),
    )
    override val exploreExpandedPageDataSourceMap: Map<String, ExploreExpandedPageDataSource> =
        (Libs.Home + Libs.Novel + Libs.Comic).associate { (k, v) ->
            if (k.second is Jm18NovelUrl) k.first to Jm18CustomExploreExpandedPageDataSource.Novel(
                k.first, k.second as Jm18NovelUrl, v
            )
            else k.first to Jm18CustomExploreExpandedPageDataSource.Comic(
                k.first, k.second as Jm18ComicUrl, v
            )
        }
}