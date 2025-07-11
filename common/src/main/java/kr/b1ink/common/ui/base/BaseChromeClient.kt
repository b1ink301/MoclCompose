package kr.b1ink.common.ui.base

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.net.Uri
import android.os.Build
import android.os.Message
import android.view.WindowManager
import android.webkit.*
import androidx.annotation.RequiresApi
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kr.b1ink.common.R
import kr.b1ink.common.ui.widget.CoreWebView
import kr.b1ink.common.util.destroyWebView
import java.lang.ref.WeakReference

open class BaseChromeClient(
    private val fileChooserCallback: ((ValueCallback<Array<Uri>>?) -> Unit)? = null,
) : WebChromeClient(), DefaultLifecycleObserver {

    private lateinit var childWebView: WeakReference<WebView>
    private var dialog: AlertDialog? = null

    override fun onDestroy(owner: LifecycleOwner) {
        close()
    }

    @SuppressLint("SetJavaScriptEnabled")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateWindow(
        view: WebView,
        isDialog: Boolean,
        isUserGesture: Boolean,
        resultMsg: Message?
    ): Boolean {
        try {
            val webView = CoreWebView(view.context)
            childWebView = WeakReference(webView)

            with(webView) {
                webViewClient = view.webViewClient
                layoutParams = view.layoutParams
                webViewClient = view.webViewClient
                webChromeClient = view.webChromeClient
            }

            val dialog = Dialog(view.context, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
            dialog.setContentView(webView)

            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(dialog.window?.attributes)
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
            dialog.window?.attributes = layoutParams
            dialog.show()

            webView.webChromeClient = object : WebChromeClient() {
                override fun onCloseWindow(window: WebView?) {
                    try {
                        dialog.dismiss()
                        webView.destroy()
                        window?.destroyWebView()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            dialog.setOnDismissListener {
                try {
                    webView.destroy()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            (resultMsg?.obj as WebView.WebViewTransport).webView = webView
            resultMsg.sendToTarget()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    override fun onCloseWindow(window: WebView?) {
        super.onCloseWindow(window)

        try {
            childWebView.get()
                ?.destroyWebView(window)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onGeolocationPermissionsShowPrompt(
        origin: String?,
        callback: GeolocationPermissions.Callback?
    ) {
        callback?.invoke(origin, true, false)
    }

    override fun onPermissionRequest(request: PermissionRequest?) {
        val resources = request?.resources ?: return
        val permissions = arrayListOf<String>()

        resources.forEach { resource ->
            when (resource) {
                PermissionRequest.RESOURCE_VIDEO_CAPTURE ->
                    permissions.add(PermissionRequest.RESOURCE_VIDEO_CAPTURE)

                PermissionRequest.RESOURCE_AUDIO_CAPTURE ->
                    permissions.add(PermissionRequest.RESOURCE_AUDIO_CAPTURE)

                PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID ->
                    permissions.add(PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID)

                PermissionRequest.RESOURCE_MIDI_SYSEX ->
                    permissions.add(PermissionRequest.RESOURCE_MIDI_SYSEX)
            }
        }

        if (permissions.isEmpty()) {
            request.deny()
        } else {
            request.grant(permissions.toTypedArray())
        }
    }

    override fun onJsAlert(
        view: WebView,
        url: String,
        message: String,
        result: JsResult
    ): Boolean {
        try {
            dialog?.dismiss()
            dialog = AlertDialog.Builder(view.context)
                .setTitle(R.string.app_name)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok) { _, _ -> result.confirm() }
                .setCancelable(false)
                .create()
            dialog?.show()
        } catch (e: Exception) {
            e.printStackTrace()
            result.confirm()
        }
        return true
    }

    override fun onJsConfirm(
        view: WebView,
        url: String,
        message: String,
        result: JsResult,
    ): Boolean {
        try {
            dialog?.dismiss()
            dialog = AlertDialog.Builder(view.context)
                .setTitle(R.string.app_name)
                .setMessage(message)
                .setNegativeButton(android.R.string.cancel) { _, _ -> result.cancel() }
                .setPositiveButton(android.R.string.ok) { _, _ -> result.confirm() }
                .setCancelable(false)
                .create()
            dialog?.show()
        } catch (e: Exception) {
            result.cancel()
            e.printStackTrace()
        }
        return true
    }

    fun close() {
        dialog?.dismiss()
    }

    override fun onShowFileChooser(
        webView: WebView?,
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: FileChooserParams?
    ): Boolean {
        fileChooserCallback?.invoke(filePathCallback)
        return true
    }
}