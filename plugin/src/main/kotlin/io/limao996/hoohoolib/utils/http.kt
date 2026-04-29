package io.limao996.hoohoolib.utils

import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import cxhttp.CxHttp
import cxhttp.response.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import kotlin.coroutines.resume

// 常量提取
private const val MAX_RETRY_TIMES = 3
private const val INITIAL_RETRY_DELAY_MS = 2500L
private const val BROWSER_TIMEOUT_MS = 5000L
private const val MAX_CONCURRENT_REQUESTS = 3

private val requestLimiter = Semaphore(MAX_CONCURRENT_REQUESTS)

/**
 * User-Agent 生成器缓存实例，避免重复创建
 */
private val userAgentGenerator by lazy { UserAgentGenerator() }

/**
 * HTTP GET 请求，返回解析后的 HTML Document
 * @param url 请求地址
 * @param useWindowsUA 是否使用 Windows 平台的 User-Agent
 * @return Document 对象，失败时返回 null
 */
suspend fun httpGet(url: String, useWindowsUA: Boolean = false): Document? =
    requestLimiter.withPermit {
        executeWithRetry {
            executeHttpGet(url, useWindowsUA)
        }?.body?.string()?.let(Jsoup::parse)
    }


/**
 * 执行单次 HTTP GET 请求
 */
private suspend fun executeHttpGet(url: String, useWindowsUA: Boolean): Response =
    withContext(Dispatchers.IO) {
        CxHttp.get(url) {
            headers(
                mapOf(
                    "user-agent" to userAgentGenerator.generate(
                        if (useWindowsUA) UserAgentGenerator.Platform.Windows
                        else UserAgentGenerator.Platform.Android
                    )
                )
            )
        }.scope(this).await()
    }

/**
 * 浏览器渲染方式获取页面（适用于 JavaScript 渲染的页面）
 * @param context Android Context
 * @param url 请求地址
 * @param useWindowsUA 是否使用 Windows 平台的 User-Agent
 * @return Document 对象，失败时返回 null
 */
suspend fun browserGet(context: Context, url: String, useWindowsUA: Boolean = false): Document? =

    requestLimiter.withPermit {
        executeWithRetry {
            executeBrowserGet(context, url, useWindowsUA)
        }?.let {
            withContext(Dispatchers.IO) { Jsoup.parse(it) }
        }
    }

/**
 * 执行单次浏览器请求
 */
private suspend fun executeBrowserGet(
    context: Context, url: String, useWindowsUA: Boolean
): String? = withContext(Dispatchers.Main) {
    withTimeoutOrNull(BROWSER_TIMEOUT_MS) {
        suspendCancellableCoroutine { continuation ->
            val webView = WebView(context).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    userAgentString = userAgentGenerator.generate(
                        if (useWindowsUA) UserAgentGenerator.Platform.Windows
                        else UserAgentGenerator.Platform.Android
                    )
                }

                addJavascriptInterface(JavaScriptBridge {
                    continuation.resume(it)
                    destroy()
                }, "Bridge")

                webViewClient = BrowserWebViewClient()

                loadUrl(url)
            }

            continuation.invokeOnCancellation {
                webView.destroy()
            }
        }
    }
}

/**
 * JavaScript 桥接类
 */
private class JavaScriptBridge(
    val block: (String) -> Unit
) {
    @JavascriptInterface
    fun processHTML(html: String) = block(html)
}

/**
 * 自定义 WebViewClient，在页面加载完成后注入 JavaScript 获取 HTML
 */
private class BrowserWebViewClient : WebViewClient() {
    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        // 使用更健壮的 JavaScript 代码获取完整 HTML
        view?.loadUrl(
            "javascript:(function() {" + "var html = document.documentElement.outerHTML;" + "window.Bridge.processHTML(html);" + "})()"
        )
    }
}

/**
 * 通用重试逻辑
 * @param maxRetries 最大重试次数
 * @param initialDelay 初始延迟时间（毫秒）
 * @param block 需要执行的挂起函数
 * @return 执行结果，失败时返回 null
 */
private suspend fun <T> executeWithRetry(
    maxRetries: Int = MAX_RETRY_TIMES,
    initialDelay: Long = INITIAL_RETRY_DELAY_MS,
    block: suspend () -> T?
): T? {
    var retryCount = 0
    var currentDelay = initialDelay

    while (retryCount <= maxRetries) {
        try {
            val result = block()
            if (result != null) {
                return result
            }
        } catch (e: Exception) {
            // 记录日志（可选）
            // Log.w("HttpUtils", "Request failed (attempt ${retryCount + 1}/$maxRetries)", e)
        }

        retryCount++
        if (retryCount <= maxRetries) {
            delay(currentDelay)
            currentDelay *= 2
        }
    }

    return null
}