package io.limao996.hoohoolib.utils

import kotlin.random.Random

class UserAgentGenerator {
    private val windowsVersions = listOf(
        "Windows NT 10.0; Win64; x64",
        "Windows NT 10.0; WOW64",
        "Windows NT 6.1; Win64; x64",
        "Windows NT 6.3; Win64; x64",
        "Windows NT 6.2; Win64; x64"
    )

    private val androidVersions = listOf(
        "Android 14; K",
        "Android 13; SM-S918B",
        "Android 12; Pixel 6",
        "Android 11; RMX2202",
        "Android 10; MI 9",
        "Android 13; SM-G998B",
        "Android 12; M2102J20SG",
        "Android 11; Pixel 5"
    )

    private val webkitVersions = listOf(
        "537.36", "537.35", "537.34", "537.33"
    )

    private val chromeVersions = listOf(
        "120.0.6099.210", "119.0.6045.163", "118.0.5993.111", "117.0.5938.152", "116.0.5845.172"
    )

    private val firefoxVersions = listOf(
        "121.0", "120.0.1", "119.0.1", "118.0.2", "117.0.1"
    )

    private val edgeVersions = listOf(
        "120.0.2210.91", "119.0.2151.72", "118.0.2088.76", "117.0.2045.60"
    )

    sealed class Platform {
        object Windows : Platform()
        object Android : Platform()
    }

    sealed class Browser(val name: String) {
        object Chrome : Browser("Chrome")
        object Firefox : Browser("Firefox")
        object Edge : Browser("Edge")
    }

    /**
     * 生成随机User-Agent
     * @param platform 平台类型，如果为null则随机选择平台
     * @param browser 浏览器类型，如果为null则随机选择浏览器
     */
    fun generate(platform: Platform? = null, browser: Browser? = null): String {
        val selectedPlatform =
            platform ?: if (Random.nextBoolean()) Platform.Windows else Platform.Android
        val selectedBrowser = browser ?: when (Random.nextInt(3)) {
            0 -> Browser.Chrome
            1 -> Browser.Firefox
            else -> Browser.Edge
        }

        return when (selectedPlatform) {
            is Platform.Windows -> generateWindowsUA(selectedBrowser)
            is Platform.Android -> generateAndroidUA(selectedBrowser)
        }
    }

    /**
     * 生成Windows平台的随机User-Agent（浏览器随机）
     */
    fun generateWindowsUA(browser: Browser? = null): String {
        val selectedBrowser = browser ?: when (Random.nextInt(3)) {
            0 -> Browser.Chrome
            1 -> Browser.Firefox
            else -> Browser.Edge
        }

        val osVersion = windowsVersions.random()
        val webkitVersion = webkitVersions.random()

        return when (selectedBrowser) {
            Browser.Chrome -> {
                val chromeVersion = chromeVersions.random()
                "Mozilla/5.0 ($osVersion) AppleWebKit/$webkitVersion (KHTML, like Gecko) Chrome/$chromeVersion Safari/$webkitVersion"
            }

            Browser.Firefox -> {
                val firefoxVersion = firefoxVersions.random()
                "Mozilla/5.0 ($osVersion; rv:${firefoxVersion.split(".")[0]}.0) Gecko/20100101 Firefox/$firefoxVersion"
            }

            Browser.Edge -> {
                val edgeVersion = edgeVersions.random()
                val chromeVersion = chromeVersions.random()
                "Mozilla/5.0 ($osVersion) AppleWebKit/$webkitVersion (KHTML, like Gecko) Chrome/$chromeVersion Safari/$webkitVersion Edg/$edgeVersion"
            }
        }
    }

    /**
     * 生成Android平台的随机User-Agent（浏览器随机）
     */
    fun generateAndroidUA(browser: Browser? = null): String {
        val selectedBrowser = browser ?: when (Random.nextInt(3)) {
            0 -> Browser.Chrome
            1 -> Browser.Firefox
            else -> Browser.Edge
        }

        val androidVersion = androidVersions.random()
        val webkitVersion = webkitVersions.random()

        return when (selectedBrowser) {
            Browser.Chrome -> {
                val chromeVersion = chromeVersions.random()
                "Mozilla/5.0 (Linux; $androidVersion) AppleWebKit/$webkitVersion (KHTML, like Gecko) Chrome/$chromeVersion Mobile Safari/$webkitVersion"
            }

            Browser.Firefox -> {
                val firefoxVersion = firefoxVersions.random()
                "Mozilla/5.0 (Android; $androidVersion; Mobile) Gecko/20100101 Firefox/$firefoxVersion"
            }

            Browser.Edge -> {
                val edgeVersion = edgeVersions.random()
                val chromeVersion = chromeVersions.random()
                "Mozilla/5.0 (Linux; $androidVersion) AppleWebKit/$webkitVersion (KHTML, like Gecko) Chrome/$chromeVersion Safari/$webkitVersion EdgA/$edgeVersion"
            }
        }
    }

    /**
     * 批量生成User-Agent
     * @param count 生成数量
     * @param platform 平台类型（可选）
     * @param browser 浏览器类型（可选）
     */
    fun generateBatch(
        count: Int, platform: Platform? = null, browser: Browser? = null
    ): List<String> {
        return List(count) { generate(platform, browser) }
    }
}
