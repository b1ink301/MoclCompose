package kr.b1ink.data.etc

import android.webkit.CookieManager
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kr.b1ink.data.di.coroutine.DefaultDispatcher
import kr.b1ink.domain.data.CookieRepository
import kr.b1ink.domain.model.SiteType
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import timber.log.Timber
import java.net.HttpCookie
import javax.inject.Inject

class CookieRepositoryImpl @Inject constructor(
    private val persistentCookieJar: PersistentCookieJar,
) : CookieRepository {

    override suspend fun saveCookies(siteType: SiteType) {
        val url = when (siteType) {
            SiteType.Clien -> "https://m.clien.net"
            SiteType.Damoang -> "https://damoang.net"
            else -> return
        }

        val cookies = (CookieManager.getInstance()
            .getCookie(url) ?: return)
            .split(";")
            .mapNotNull { cookieString ->
                try {
                    HttpCookie.parse(cookieString)
                } catch (_: Exception) {
                    null
                }
            }
            .flatMap {
                it.mapNotNull { cookie ->
                    Timber.d("====> cookie=$cookie")
                    try {
                        Cookie.Builder()
                            .name(cookie.name)
                            .value(cookie.value)
                            .domain(url)
                            .build()
                    } catch (_: Exception) {
                        null
                    }
                }
            }

        val httpUrl = url.toHttpUrlOrNull() ?: return
        if (cookies.isNotEmpty()) {
            persistentCookieJar.clear()
            persistentCookieJar.saveFromResponse(httpUrl, cookies)
        }
    }

    override suspend fun clearCookies(siteType: SiteType) {
        CookieManager.getInstance()
            .removeAllCookies {}
        //        CookieManager.getInstance().removeSessionCookies {}
    }
}