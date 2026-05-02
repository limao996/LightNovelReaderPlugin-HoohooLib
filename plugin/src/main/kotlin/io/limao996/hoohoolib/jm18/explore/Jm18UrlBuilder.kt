package io.limao996.hoohoolib.jm18.explore

import io.limao996.hoohoolib.jm18.JM18_HOST

interface Jm18Url {
    fun toUrl(pageNum: Int, limit: Int): String
}

data class Jm18NovelUrl(
    val tag: String = "all",      // 题材
    val status: String = "0",            // 状态: 0-全部, 1-已完结, 2-连载中
    val length: String = "0",            // 篇幅: 0-全部, 1-短篇(<10w), 2-中篇(10-100w), 3-长篇(>100w)
    val sortBy: String = "publish_at", // 排序字段: publish_at, view_count, collect_count
    val sortOrder: String = "2",         // 排序方向: 1-升序, 2-降序 (表单默认按上架时间降序)
) : Jm18Url {

    override fun toUrl(pageNum: Int, limit: Int) =
        "$JM18_HOST/novel/$tag?page=$pageNum&limit=$limit&sort_field=$sortBy&sort_order=$sortOrder&status=$status&length=$length"


    companion object {
        // 排序类型 (参考原代码格式)
        val sortBys = listOf(
            "上架时间" to "publish_at", "浏览量" to "view_count", "收藏量" to "collect_count"
        )

        // 排序方向 (参考原代码格式)
        val sortOrders = listOf(
            "升序" to "1", "降序" to "2"
        )

        // 状态列表
        val statusList = listOf(
            "不限" to "0", "已完结" to "1", "连载中" to "2"
        )

        // 篇幅列表
        val lengthList = listOf(
            "不限" to "0", "短篇" to "1", "中篇" to "2", "长篇" to "3"
        )

        val tags = listOf(
            // ==================== 一、内容类型（1-9）====================
            // 0
            "不限" to "all",
            // 1
            "都市" to "all/dsjq",
            // 2
            "校园" to "all/xycs",
            // 3
            "乡村" to "all/xcaq",
            // 4
            "武侠" to "all/jdwx",
            // 5
            "玄幻" to "all/dfxh",
            // 6
            "科幻" to "all/kxhx",
            // 7
            "历史" to "all/lsjk",
            // 8
            "魔幻" to "all/xfmh",

            // ==================== 二、剧情设定（9-19）====================
            // 9
            "系统" to "all/xtyn",
            // 10
            "穿越" to "all/cycs",
            // 11
            "同人" to "all/trgb",
            // 12
            "娱乐" to "all/ylmx",
            // 13
            "贤者" to "all/xzxs",
            // 14
            "末世" to "tag/末世",
            // 15
            "快穿" to "tag/快穿",
            // 16
            "异世界" to "tag/异世界",
            // 17
            "种田" to "tag/种田",
            // 18
            "官场" to "tag/官场",
            // 19
            "灵异" to "tag/灵异",

            // ==================== 三、情感关系（20-38）====================
            // 20
            "乱伦" to "all/jtll",
            // 21
            "纯爱" to "tag/纯爱",
            // 22
            "后宫" to "tag/后宫",
            // 23
            "百合" to "tag/百合",
            // 24
            "耽美" to "tag/耽美",
            // 25
            "骨科" to "tag/骨科",
            // 26
            "母子" to "tag/母子",
            // 27
            "父女" to "tag/父女",
            // 28
            "人妻" to "tag/人妻",
            // 29
            "淫妻" to "tag/淫妻",
            // 30
            "绿母" to "tag/绿母",
            // 31
            "绿奴" to "tag/绿奴",
            // 32
            "公媳" to "tag/公媳",
            // 33
            "姐妹花" to "tag/姐妹花",
            // 34
            "母女花" to "tag/母女花",
            // 35
            "全家桶" to "tag/全家桶",
            // 36
            "交换伴侣" to "tag/交换伴侣",
            // 37
            "出轨" to "tag/出轨",
            // 38
            "目前犯" to "tag/目前犯",

            // ==================== 四、人物属性（39-54）====================
            // 39
            "萝莉" to "tag/萝莉",
            // 40
            "熟女" to "tag/熟女",
            // 41
            "人妖" to "tag/人妖",
            // 42
            "伪娘" to "tag/伪娘",
            // 43
            "Futa" to "tag/Futa",
            // 44
            "性转" to "tag/性转",
            // 45
            "校花" to "tag/校花",
            // 46
            "痴女" to "tag/痴女",
            // 47
            "病娇" to "tag/病娇",
            // 48
            "性奴" to "tag/性奴",
            // 49
            "肉便器" to "tag/肉便器",
            // 50
            "种马" to "tag/种马",
            // 51
            "白虎" to "tag/白虎",
            // 52
            "异种族" to "tag/异种族",
            // 53
            "异国" to "tag/异国",
            // 54
            "小马拉大车" to "tag/小马拉大车",

            // ==================== 五、性行为/玩法（55-74）====================
            // 55
            "1v1" to "tag/1v1",
            // 56
            "NP" to "tag/NP",
            // 57
            "NTR" to "tag/NTR",
            // 58
            "NTL" to "tag/NTL",
            // 59
            "逆NTR" to "tag/逆NTR",
            // 60
            "SM" to "tag/SM",
            // 61
            "调教" to "tag/调教",
            // 62
            "催眠" to "tag/催眠",
            // 63
            "强奸" to "tag/强奸",
            // 64
            "凌辱" to "tag/凌辱",
            // 65
            "露出" to "tag/露出",
            // 66
            "监禁" to "tag/监禁",
            // 67
            "捆绑" to "tag/捆绑",
            // 68
            "群交" to "tag/群交",
            // 69
            "足交" to "tag/足交",
            // 70
            "恋足" to "tag/恋足",
            // 71
            "榨精" to "tag/榨精",
            // 72
            "受孕" to "tag/受孕",
            // 73
            "产奶" to "tag/产奶",
            // 74
            "触手" to "tag/触手",

            // ==================== 六、道具/装扮（75-80）====================
            // 75
            "丝袜" to "tag/丝袜",
            // 76
            "制服" to "tag/制服",
            // 77
            "变装" to "tag/变装",
            // 78
            "道具" to "tag/道具",
            // 79
            "药物" to "tag/药物",
            // 80
            "改造" to "tag/改造",

            // ==================== 七、情感/剧情风格（81-94）====================
            // 81
            "甜文" to "tag/甜文",
            // 82
            "爽文" to "tag/爽文",
            // 83
            "虐心" to "tag/虐心",
            // 84
            "虐主" to "tag/虐主",
            // 85
            "复仇" to "tag/复仇",
            // 86
            "搞笑" to "tag/搞笑",
            // 87
            "浪漫" to "tag/浪漫",
            // 88
            "经典" to "tag/经典",
            // 89
            "好文笔" to "tag/好文笔",
            // 90
            "剧情" to "tag/剧情",
            // 91
            "反差" to "tag/反差",
            // 92
            "狗血" to "tag/狗血",
            // 93
            "暗黑" to "tag/暗黑",
            // 94
            "暴虐" to "tag/暴虐",

            // ==================== 八、阅读体验/标签（95-108）====================
            // 95
            "BE" to "tag/BE",
            // 96
            "HE" to "tag/HE",
            // 97
            "SC" to "tag/SC",
            // 98
            "无绿" to "tag/无绿",
            // 99
            "手枪文" to "tag/手枪文",
            // 100
            "微肉" to "tag/微肉",
            // 101
            "微重口" to "tag/微重口",
            // 102
            "重口" to "tag/重口",
            // 103
            "适合女生" to "tag/适合女生",
            // 104
            "女性视角" to "tag/女性视角",
            // 105
            "猎艳" to "tag/猎艳",
            // 106
            "神豪" to "tag/神豪",
            // 107
            "AI辅助" to "tag/AI辅助",
            // 108
            "红帽" to "tag/红帽",

            // ==================== 九、特殊/未分组（109-110）====================
            // 109
            "下克上" to "tag/下克上",
            // 110
            "有父" to "tag/有父"
        )
    }
}


data class Jm18ComicUrl(
    val tag: String = "all",
    val sortBy: String = "refreshed_at",
    val sortOrder: String = "1",
) : Jm18Url {

    override fun toUrl(pageNum: Int, limit: Int) =
        "$JM18_HOST/comic/$tag/$pageNum?sort_order=$sortOrder&sort_field=$sortBy"


    companion object {
        // 排序类型
        val sortBys = listOf(
            "上架时间" to "refreshed_at", "浏览量" to "view_num", "收藏量" to "collect_num"
        )

        // 排序方向
        val sortOrders = listOf(
            "升序" to "1", "降序" to "2"
        )

        // 标签
        val tags = listOf(
            // 0
            "不限" to "all",
            // 1
            "日漫" to "all/rb",
            // 2
            "韩漫" to "all/hg",

            // ========== 剧情/主题 ==========
            // 3
            "剧情" to "all/jq",
            // 4
            "校园" to "all/xy",
            // 5
            "爱情" to "all/aq",
            // 6
            "奇幻" to "all/qh",
            // 7
            "乱伦" to "all/ll",
            // 8
            "同人" to "all/tr",
            // 9
            "后宫" to "tag/后宫",
            // 10
            "同性" to "tag/同性",

            // ========== 人物/角色 ==========
            // 11
            "M男" to "tag/M男",
            // 12
            "单男" to "tag/单男",
            // 13
            "单女" to "tag/单女",
            // 14
            "扶她" to "tag/扶她futa",
            // 15
            "双性" to "tag/双性人",
            // 16
            "伪娘" to "tag/药娘伪娘",
            // 17
            "性转" to "tag/性转换",
            // 18
            "正太" to "tag/正太控",
            // 19
            "萝莉" to "tag/萝莉",
            // 20
            "御姐" to "tag/御姐女王",
            // 21
            "姐姐" to "tag/姐姐",
            // 22
            "母亲" to "tag/母亲",
            // 23
            "老师" to "tag/老师",
            // 24
            "护士" to "tag/护士",
            // 25
            "猫娘" to "tag/猫女",
            // 26
            "兔女郎" to "tag/兔女郎",
            // 27
            "妖精" to "tag/妖精",

            // ========== 身体/外貌 ==========
            // 28
            "黑皮" to "tag/暗黑皮肤",
            // 29
            "巨尻" to "tag/巨尻",
            // 30
            "大屁股" to "tag/大屁股",
            // 31
            "大鸡巴" to "tag/大阴茎",
            // 32
            "兽耳" to "tag/兽耳",
            // 33
            "毛茸茸" to "tag/毛茸茸",
            // 34
            "眼睛" to "tag/眼睛",
            // 35
            "马尾辫" to "tag/马尾辫",
            // 36
            "衣领" to "tag/衣领",
            // 37
            "处女" to "tag/处女",

            // ========== 服装/道具 ==========
            // 38
            "比基尼" to "tag/比基尼",
            // 39
            "泳装" to "tag/泳装",
            // 40
            "水手服" to "tag/水手服",
            // 41
            "制服" to "tag/制服",
            // 42
            "JK" to "tag/女学生制服",
            // 43
            "内衣" to "tag/内衣",
            // 44
            "丝袜" to "tag/丝袜",
            // 45
            "黑丝" to "tag/黑丝丝袜",
            // 46
            "连裤袜" to "tag/连裤袜",
            // 47
            "扮演" to "tag/cosplay",
            // 48
            "兽交" to "tag/兽交",

            // ========== 性行为/玩法 ==========
            // 49
            "3P" to "tag/3P",
            // 50
            "BL" to "tag/BL",
            // 51
            "NTR" to "tag/NTR",
            // 52
            "PUA" to "tag/PUA",
            // 53
            "调教" to "tag/调教",
            // 54
            "束缚" to "tag/束缚",
            // 55
            "露出" to "tag/露出",
            // 56
            "强奸" to "tag/强奸",
            // 57
            "群交" to "tag/群P",
            // 58
            "百合" to "tag/女同百合",
            // 59
            "口交" to "tag/口交",
            // 60
            "肛交" to "tag/肛交",
            // 61
            "肛门" to "tag/肛门",
            // 62
            "双穴" to "tag/两穴同时插入",
            // 63
            "乳交" to "tag/乳交",
            // 64
            "足交" to "tag/足交",
            // 65
            "足控" to "tag/足控",
            // 66
            "中出" to "tag/内射中出",
            // 67
            "潮吹" to "tag/高潮潮吹",
            // 68
            "受精" to "tag/受精",
            // 69
            "怀孕" to "tag/怀孕",
            // 70
            "母乳" to "tag/母乳",
            // 71
            "卖淫" to "tag/卖淫",
            // 72
            "援交" to "tag/援交",
            // 73
            "破处" to "tag/破处",
            // 74
            "恋父" to "tag/恋父"
        )

    }
}