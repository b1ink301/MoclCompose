package kr.b1ink.common.util

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.hardware.display.DisplayManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.View
import android.view.View.OVER_SCROLL_NEVER
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebView.setWebContentsDebuggingEnabled
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import com.google.gson.Gson
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.b1ink.common.R
import kr.b1ink.common.ui.base.BaseWebViewActivity
import kr.b1ink.common.ui.base.BaseWebViewActivity.Companion.EXTRA_URL
import org.json.JSONObject
import timber.log.Timber
import java.util.Base64
import java.util.Locale

@SuppressLint("MissingPermission")
fun AppCompatActivity.isNetworkAvailable() =
    (getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager).run {
        getNetworkCapabilities(activeNetwork)?.run {
            hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    hasTransport(NetworkCapabilities.TRANSPORT_VPN)
        } ?: false
    }

fun Context.getScreenSize(): Rect {
    val rect = Rect()
    val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val windowMetrics = windowManager.currentWindowMetrics
        rect.set(0, 0, windowMetrics.bounds.width(), windowMetrics.bounds.height())
    } else {
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getMetrics(metrics)
        rect.set(0, metrics.widthPixels, 0, metrics.heightPixels)
    }
    Timber.tag("getScreenSize")
        .d("getScreenSize = $rect")
    return rect
}

@SuppressLint("ObsoleteSdkInt")
fun Context.getSystemLanguage(): String {
    val systemLocale: Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        resources.configuration.locales.get(0)
    } else {
        @Suppress("DEPRECATION")
        resources.configuration.locale
    }
    println("Current language : ${systemLocale.language}")

    return when (systemLocale.language) {
        Locale.KOREAN.language -> "ko"
        Locale.JAPANESE.language -> "ja"
        Locale.SIMPLIFIED_CHINESE.language -> "zh_CN"
        Locale.TRADITIONAL_CHINESE.language -> "zh_TW"
        else -> "en"
    }
}

fun Context.isFoldScreenSize(): Boolean {
    if (getScreenSize().width() > 1500) {
        //if(DeviceModel.startsWith("SM-F9")){ //갤럭시폴드 모델명 F9 로 시작 / 플립 F7
        val dm = getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
        // mode: 1 처음부터 펼친상태 / 3 접었다 펼친 상태
        // mode: 8 접힌 상태
        return dm?.displays?.any { it.mode.modeId < 8 } == true
    }
    return false
}

fun View.getBitmapFromView(): Bitmap {
    val bitmap = createBitmap(width, height)
    val canvas = Canvas(bitmap)
    draw(canvas)
    return bitmap
}

fun String.decodeBase64String(): String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    String(
        Base64.getDecoder()
            .decode(this)
    )
} else {
    String(android.util.Base64.decode(this, android.util.Base64.NO_WRAP))
}

fun String.getStringForKey(key: String): String? = try {
    val jObject = JSONObject(this)
    val data = jObject.getString("data")
    val jObject2 = JSONObject(data)
    jObject2.getString(key)
} catch (e: Exception) {
    e.printStackTrace()
    null
}

fun String.encodeBase64String(): String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    Base64.getEncoder()
        .encodeToString(toByteArray())
} else {
    android.util.Base64.encodeToString(toByteArray(), android.util.Base64.NO_WRAP)
}

fun <T> Context.openWebViewActivity(
    url: String,
    clazz: Class<T>
) = try {
    val intent = Intent(this, clazz::class.java)
    intent.putExtra(EXTRA_URL, url)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
} catch (e: Exception) {
    e.printStackTrace()
}

fun Context.goAppSettings() = try {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        .setData("package:$packageName".toUri())
    startActivity(intent)
} catch (e: Exception) {
    e.printStackTrace()
}

suspend fun Context.requestRequiredPermission(
    permissions: List<String>,
): Boolean = withContext(Dispatchers.Main) {
    val completableDeferred = CompletableDeferred<Boolean>()

    try {
        val tedPermission = TedPermission.create()
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                    completableDeferred.complete(true)
                }

                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    completableDeferred.complete(false)
                }
            })
            .setDeniedMessage(R.string.permission_reject)

        tedPermission.setPermissions(*permissions.toTypedArray())
        tedPermission.check()
    } catch (e: Exception) {
        e.printStackTrace()
        completableDeferred.complete(false)
    }

    completableDeferred.await()
}


fun String.urlFromDeepLink(baseUrl: String): String = if (startsWith("http").not()) {
    baseUrl + if (startsWith("/")) {
        substring(1)
    } else {
        this
    }
} else {
    this
}

//fun Context.getNotification(
//    title: String,
//    content: String,
//    landingPage: String?
//): Notification {
//    val notificationIntent = if ((applicationContext as? UlsanApp)?.hasMainActivity==false) {
//        Intent(this, IntroActivity::class.java).apply {
//            if (landingPage.isNullOrEmpty()) {
//                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
//                        Intent.FLAG_ACTIVITY_CLEAR_TASK or
//                        Intent.FLAG_ACTIVITY_CLEAR_TOP
//            } else {
//                action = Intent.ACTION_VIEW
//                data = Uri.parse("whaleapp://link?path=$landingPage")
//                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
//            }
//        }
//    } else {
//        if (landingPage.isNullOrEmpty()) {
//            Intent(this, IntroActivity::class.java).apply {
//                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
//                        Intent.FLAG_ACTIVITY_CLEAR_TASK or
//                        Intent.FLAG_ACTIVITY_CLEAR_TOP
//            }
//        } else {
//            Intent(this, SubWebViewActivity::class.java).apply {
//                putExtra("url", "/path".urlFromDeepLink())
//                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
//            }
//        }
//    }
//    val pendingIntent: PendingIntent = PendingIntent.getActivity(
//        applicationContext,
//        1000,
//        notificationIntent,
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
//        } else {
//            PendingIntent.FLAG_UPDATE_CURRENT
//        }
//    )
//
//    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//        createNotificationChannel()
//        NotificationCompat.Builder(applicationContext, CHANNEL_ID)
//    } else {
//        @Suppress("DEPRECATION")
//        NotificationCompat.Builder(applicationContext)
//    }
//        .setSmallIcon(R.mipmap.ic_launcher)
//        .setContentTitle(title)
//        .setContentText(content)
//        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//        .setContentIntent(pendingIntent)
//        .setAutoCancel(true)
//        .build()
//}

//fun Context.createNotificationChannel() {
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//        val notificationManager: NotificationManager =
//            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//        var channel = notificationManager.getNotificationChannel(CHANNEL_ID)
//        if (channel==null) {
//            val name = getString(R.string.channel_name)
//            val descriptionText = getString(R.string.channel_description)
//            val importance = NotificationManager.IMPORTANCE_DEFAULT
//            channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
//                description = descriptionText
//            }
//            notificationManager.createNotificationChannel(channel)
//        }
//    }
//}

fun Context.getLocaleString(
    @StringRes res: Int,
    locale: Locale
): String {
    val newConfiguration = Configuration(resources.configuration).apply { setLocale(locale) }
    return createConfigurationContext(newConfiguration).getString(res)
}

@Suppress("UNCHECKED_CAST")
fun <T : Application> Context.getApp() = applicationContext as? T

fun WebView.resume() {
    onResume()
    resumeTimers()
}

fun WebView.pause() {
    onPause()
    pauseTimers()
}

fun Context.logout() {
    CookieManager.getInstance()
        .removeAllCookies(null)
    CookieManager.getInstance()
        .flush()
    CookieManager
        .getInstance()
//        .setCookie(
//            Global.BASE_URL,
//            "lang=${Prefs.getString(PREF_USER_LANG, getSystemLanguage())}"
//        )
}

//suspend fun AppCompatActivity.showMarketingAgreeDialog() {
//    if (Prefs.getBoolean(PREF_PUSH_CHECK_DONE, false)) {
//        return
//    }
//    withContext(Dispatchers.IO) {
//        delay(500)
//    }
//    val message = getString(R.string.push_title) + "\n\n" +
//            getString(R.string.push_detail)
//    AlertDialog
//        .Builder(this@showMarketingAgreeDialog)
//        .setPositiveButton(R.string.push_agree) { dialog, _ ->
//            Prefs.putString(PREF_PUSH_YN, "YES")
//            dialog.dismiss()
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
//                TedPermissionUtil.isDenied(Manifest.permission.POST_NOTIFICATIONS)
//            ) {
//                TedPermission.create()
//                    .setPermissionListener(object : PermissionListener {
//                        override fun onPermissionGranted() =
//                            Prefs.putBoolean(PREF_PUSH_CHECK_DONE, true)
//                        override fun onPermissionDenied(deniedPermissions: MutableList<String>?) =
//                            Prefs.putBoolean(PREF_PUSH_CHECK_DONE, true)
//                    })
//                    .setDeniedMessage(R.string.permission_desc)
//                    .setPermissions(Manifest.permission.POST_NOTIFICATIONS)
//                    .check()
//            } else {
//                Prefs.putBoolean(PREF_PUSH_CHECK_DONE, true)
//            }
//        }
//        .setNegativeButton(R.string.push_deny) { dialog, _ ->
//            Prefs.putString(PREF_PUSH_YN, "NO")
//            dialog.dismiss()
//        }
//        .setTitle(R.string.app_name)
//        .setMessage(message)
//        .setCancelable(false)
//        .create()
//        .show()
//}

fun <T : BaseWebViewActivity<*>> Context.newActivity(clazz: Class<T>) {
    val activity = this as? BaseWebViewActivity<*> ?: throw Exception("Context is not BaseWebViewActivity")
    val intent = Intent(this, clazz)
        .apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_NO_ANIMATION
            )
        }
    startActivity(intent)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        activity.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE, R.anim.none, R.anim.none)
    } else {
        @Suppress("DEPRECATION")
        activity.overridePendingTransition(0, 0)
    }
    activity.finishAffinity()
}

fun <T : BaseWebViewActivity<*>> Context.reActivity(
    clazz: Class<T>,
    url: String
) {
    val activity = this as? BaseWebViewActivity<*> ?: throw Exception("Context is not BaseWebViewActivity")
    val intent = Intent(this, clazz)
        .apply {
            addFlags(
                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
            )
            putExtra(EXTRA_DEEPLINK_PATH, url)
            putExtra(EXTRA_DEEPLINK_IS_MAIN_URL, true)
        }
    startActivity(intent)
    activity.finishAfterTransition()
}

fun WebView.destroyWebView(parent: ViewGroup? = null) = try {
    parent?.removeView(this)
    webChromeClient = null
    removeJavascriptInterface(JAVASCRIPT_BRIDGE_NAME)
    stopLoading()
    loadUrl("about:blank")
    removeAllViews()
} catch (e: Exception) {
    e.printStackTrace()
} finally {
    try {
        destroy()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Context.callAppSettings() {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    )
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}

@SuppressLint("SetJavaScriptEnabled")
fun WebView.initSettings(
    customUserAgent: String? = null,
    isDebuggingEnabled: Boolean = false
) {
    overScrollMode = OVER_SCROLL_NEVER

    CookieManager.getInstance()
        .setAcceptCookie(true)
    CookieManager.getInstance()
        .setAcceptThirdPartyCookies(this, true)

    with(settings) {
        setSupportZoom(false)
        builtInZoomControls = false
        displayZoomControls = false
        domStorageEnabled = true
        javaScriptEnabled = true
        loadWithOverviewMode = true
        setGeolocationEnabled(true)
        setSupportMultipleWindows(true)
        cacheMode = WebSettings.LOAD_NO_CACHE
        useWideViewPort = true
        javaScriptCanOpenWindowsAutomatically = true
//            databaseEnabled = true
        if (customUserAgent.isNullOrBlank()
                .not()
        ) {
            userAgentString += customUserAgent
        }
        mediaPlaybackRequiresUserGesture = false
        allowFileAccess = true
        allowContentAccess = true
        textZoom = 100
        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        setWebContentsDebuggingEnabled(isDebuggingEnabled)
    }

    //clearCache(true)
}

inline fun <reified T> String.fromJson(): T? = try {
    Gson().fromJson(Uri.decode(this), T::class.java)
} catch (e: Exception) {
    e.printStackTrace()
    null
}

inline fun <reified T> Any.toJson(): String? = try {
    Uri.encode(Gson().toJson(this as T))
} catch (e: Exception) {
    e.printStackTrace()
    null
}

fun Activity.splashScreenRemove() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            splashScreenView.remove()
        }
    }
}