package kr.b1ink.common.ui.base

import android.annotation.SuppressLint
import android.webkit.WebView
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import kr.b1ink.common.R
import kr.b1ink.common.util.JAVASCRIPT_BRIDGE_NAME
import kr.b1ink.common.util.destroyWebView

abstract class BaseWebViewActivity<T : ViewBinding>(@LayoutRes contentLayoutId: Int) :
    BaseActivity<T>(contentLayoutId) {
    abstract val webView: WebView
    abstract val javaInterface: BaseJavaInterface
    open val doubleBackToExitPressedOnce: Boolean = false

    private val imageUploadHandler: ImageUploadHandler by lazy { ImageUploadHandler(this@BaseWebViewActivity) }
    private var backKeyPressedTime: Long = 0

    @CallSuper
    override fun initView() = with(webView) {
        addJavascriptInterface(javaInterface, JAVASCRIPT_BRIDGE_NAME)
        webChromeClient = imageUploadHandler.setupChromeClient()
        webViewClient = BaseWebViewClient().also {
            lifecycle.addObserver(it)
        }
        initWebView()
    }

    abstract fun initWebView()

    @Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            if (doubleBackToExitPressedOnce) {
                if (System.currentTimeMillis() > backKeyPressedTime + QUIT_INTERVAL) {
                    backKeyPressedTime = System.currentTimeMillis()
                    Toast.makeText(baseContext, R.string.back_str, Toast.LENGTH_LONG)
                        .show()
                    return
                }
                if (System.currentTimeMillis() <= backKeyPressedTime + QUIT_INTERVAL) {
                    finishAffinity()
                }
            } else {
                super.onBackPressed()
            }
        }
    }

    @CallSuper
    override fun onDestroyActivity() {
        imageUploadHandler.onDestroy()
        webView.destroyWebView()
    }

    companion object {
        const val EXTRA_URL = "extra_url"
        const val QUIT_INTERVAL: Int = 2_000
    }
}