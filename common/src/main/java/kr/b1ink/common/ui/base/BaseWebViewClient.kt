package kr.b1ink.common.ui.base

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.webkit.*
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kr.b1ink.common.R
import timber.log.Timber
import java.net.URISyntaxException

class BaseWebViewClient() : WebViewClient(), DefaultLifecycleObserver {
    private var alertDialog: AlertDialog? = null

    fun close() = alertDialog?.dismiss()

    override fun onDestroy(owner: LifecycleOwner) {
        close()
    }

    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {
        Timber.d("shouldOverrideUrlLoading = ${view?.url}")

        try {
            val url = request?.url ?: return false
            val scheme = url.scheme
            val urlString = url.toString()

            when {
                scheme == "tel" -> {
                    val intent = Intent(Intent.ACTION_DIAL, url)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    view?.context?.startActivity(intent)
                    return true
                }

                scheme == "sms" -> {
                    val intent = Intent(Intent.ACTION_SENDTO, url)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    view?.context?.startActivity(intent)
                    return true
                }

                scheme == "mailto" -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        selector = Intent(Intent.ACTION_SENDTO).apply {
                            data = "mailto:".toUri()
                        }
                        putExtra(Intent.EXTRA_EMAIL, arrayOf("ulsansmarttour@gmail.com"))
                        putExtra(Intent.EXTRA_SUBJECT, "")
                        putExtra(Intent.EXTRA_TEXT, "")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    view?.context?.startActivity(intent)
                    return true
                }

                scheme == "intent" || scheme == "market" -> {
                    val intent: Intent = Intent
                        .parseUri(
                            urlString,
                            Intent.URI_INTENT_SCHEME
                        )
                        .apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        } ?: return false
                    try {
                        view?.context?.startActivity(intent)
                    } catch (e: URISyntaxException) {
                        e.printStackTrace()
                        return false
                    } catch (e: ActivityNotFoundException) {
                        e.printStackTrace()
                        val pkgName = intent.`package` ?: return false
                        view?.context?.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkgName"))
                                .apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    return false
                }

                urlString.contains("google&redirect") or
                        (urlString.contains("facebook")) or
                        urlString.startsWith("https://accounts.google.com/o/oauth2")
                    -> {
                    view?.settings?.userAgentString =
                        "Mozilla/5.0 AppleWebKit/535.19 Chrome/56.0.0 Mobile Safari/535.19"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)

        @Suppress("NAME_SHADOWING")
        val error = error ?: return
        val errorCode = error.errorCode
        val description = error.description
        val host = view?.url?.toUri()?.host

        Timber.w("onReceivedError errorCode=$errorCode, description=$description, url=${view?.url}")

        if (ERROR_UNKNOWN == errorCode) return
//        if (ERROR_CONNECT == errorCode) return
//        if (host?.endsWith(".whataulsan.com") == false) return
//        view?.loadUrl("file:///android_asset/error.html?url=${Global.BASE_URL}")
    }

    @SuppressLint("WebViewClientOnReceivedSslError")
    override fun onReceivedSslError(
        view: WebView?,
        handler: SslErrorHandler?,
        error: SslError?
    ) {
        val context = view?.context ?: return

        val message = when (error?.primaryError) {
            SslError.SSL_UNTRUSTED -> context.getString(R.string.ssl_certificate_untrusted)
            SslError.SSL_EXPIRED -> context.getString(R.string.ssl_certificate_expired)
            SslError.SSL_IDMISMATCH -> context.getString(R.string.ssl_certificate_id_mismatch)
            SslError.SSL_NOTYETVALID -> context.getString(R.string.ssl_certificate_notyet_valid)
            else -> context.getString(R.string.ssl_certificate_error_title)
        }

        alertDialog?.dismiss()
        alertDialog = AlertDialog.Builder(context)
            .setCancelable(false)
            .setTitle(R.string.app_name)
            .setMessage(message)
            .setPositiveButton(R.string.conti) { _, _ ->
                handler?.proceed()
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                handler?.cancel()
            }
            .show()
    }
}