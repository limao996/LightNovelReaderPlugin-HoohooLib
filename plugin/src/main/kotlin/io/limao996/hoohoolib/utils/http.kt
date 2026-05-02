package io.limao996.hoohoolib.utils

import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
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

val httpClient = HttpClient(OkHttp)

// 常量提取
private const val MAX_RETRY_TIMES = 3
private const val INITIAL_RETRY_DELAY_MS = 2500L
private const val BROWSER_TIMEOUT_MS = 30000L
private const val MAX_CONCURRENT_REQUESTS = 3
private const val WEBVIEW_IDLE_TIMEOUT_MS = 5000L

private val requestLimiter = Semaphore(MAX_CONCURRENT_REQUESTS)

private data class PooledWebView(
    val webView: WebView, var idleSince: Long = 0L, var inUse: Boolean = false
)

private val webViewPool = mutableListOf<PooledWebView>()

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
            try {
                executeHttpGet(url, useWindowsUA)
            } catch (_: Exception) {
                return@executeWithRetry null
            }
        }?.bodyAsText()?.let(Jsoup::parse)
    }


/**
 * 执行单次 HTTP GET 请求
 */
private suspend fun executeHttpGet(url: String, useWindowsUA: Boolean): HttpResponse =
    withContext(Dispatchers.IO) {
        httpClient.get(url) {
            header(
                "user-agent", userAgentGenerator.generate(
                    if (useWindowsUA) UserAgentGenerator.Platform.Windows
                    else UserAgentGenerator.Platform.Android
                )
            )
        }
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

private fun acquireWebView(context: Context): WebView {
    val pooled = webViewPool.find { !it.inUse }
    if (pooled != null) {
        pooled.inUse = true
        pooled.idleSince = 0L
        pooled.webView.apply {
            stopLoading()
        }
        cleanupExpiredWebViews()
        infoLog("WebView pool: 复用实例", "poolSize=", webViewPool.size)
        return pooled.webView
    }
    val webView = WebView(context.applicationContext).apply {
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
        }
        webViewClient = BrowserWebViewClient()
    }
    webViewPool.add(PooledWebView(webView, inUse = true))
    infoLog("WebView pool: 新建实例", "poolSize=", webViewPool.size)
    return webView
}

private fun releaseWebView(webView: WebView) {
    webView.apply {
        stopLoading()
        removeJavascriptInterface("Bridge")
        //webViewClient = object : WebViewClient() {}
    }
    webViewPool.find { it.webView == webView }?.let {
        it.inUse = false
        it.idleSince = System.currentTimeMillis()
        infoLog("WebView pool: 归还实例", "poolSize=", webViewPool.size)
    }
}

private fun cleanupExpiredWebViews() {
    val now = System.currentTimeMillis()
    val iterator = webViewPool.iterator()
    while (iterator.hasNext()) {
        val pooled = iterator.next()
        if (!pooled.inUse && pooled.idleSince > 0 && (now - pooled.idleSince) > WEBVIEW_IDLE_TIMEOUT_MS) {
            infoLog("WebView pool: 销毁过期实例", "idle=", now - pooled.idleSince, "ms")
            pooled.webView.destroy()
            iterator.remove()
        }
    }
}

/**
 * 执行单次浏览器请求
 */
private suspend fun executeBrowserGet(
    context: Context, url: String, useWindowsUA: Boolean
): String? = withContext(Dispatchers.Main) {
    withTimeoutOrNull(BROWSER_TIMEOUT_MS) {
        val webView = acquireWebView(context).apply {
            settings.userAgentString = userAgentGenerator.generate(
                if (useWindowsUA) UserAgentGenerator.Platform.Windows
                else UserAgentGenerator.Platform.Android
            )
        }
        suspendCancellableCoroutine { continuation ->
            webView.addJavascriptInterface(JavaScriptBridge {
                continuation.resume(it)
            }, "Bridge")

            webView.loadUrl(url)

            continuation.invokeOnCancellation { releaseWebView(webView) }

        }.also { releaseWebView(webView) }
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
        }

        retryCount++
        if (retryCount <= maxRetries) {
            delay(currentDelay)
            currentDelay *= 2
        }
    }

    return null
}