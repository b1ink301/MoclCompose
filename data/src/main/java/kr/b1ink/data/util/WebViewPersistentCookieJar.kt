package kr.b1ink.data.util

import android.content.Context
import android.webkit.CookieManager
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.internal.platform.PlatformRegistry

/**
 * https://gist.github.com/johannes-staehlin/63a72467bd1f21829d11bc55456c5836
 */
class WebViewPersistentCookieJar(
    context: Context
) : CookieJar {
    init {
        PlatformRegistry.applicationContext = context
    }

    private val cookieManager = CookieManager.getInstance()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) =
        cookies.forEach { cookie ->
            cookieManager.setCookie(url.toString(), cookie.toString())
        }

    override fun loadForRequest(url: HttpUrl): List<Cookie> =
        cookieManager.getCookie(url.toString())
            ?.let { cookie ->
                cookie
                    .split(";")
                    .map { header ->
                        Cookie.parse(url, header)!!
                    }
            } ?: emptyList()
}