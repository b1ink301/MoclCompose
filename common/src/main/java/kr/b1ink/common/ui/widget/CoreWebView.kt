package kr.b1ink.common.ui.widget

import android.Manifest
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.util.AttributeSet
import android.webkit.MimeTypeMap
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.withStyledAttributes
import androidx.core.net.toUri
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermissionUtil
import com.gun0912.tedpermission.normal.TedPermission
import kr.b1ink.common.R
import kr.b1ink.common.util.initSettings
import java.net.URLDecoder

class CoreWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.webViewStyle,
) : WebView(context, attrs, defStyleAttr) {

    private var customUserAgent: String? = null

    init {
        initAttrs(attrs, defStyleAttr)
        initSettings(customUserAgent)
    }

    private fun initAttrs(
        attrs: AttributeSet?,
        defStyle: Int
    ) {
        attrs?.let {
            context
                .withStyledAttributes(
                    it,
                    R.styleable.CoreWebView,
                    defStyle,
                    0
                ) {
                    customUserAgent = getString(R.styleable.CoreWebView_customUserAgent)
                }
        }
    }

    fun setCustomUserAgent(customUserAgentString: String) = with(settings) {
        userAgentString += customUserAgentString
    }


    fun setDebuggingEnabled(enabled: Boolean) = with(settings) {
        setWebContentsDebuggingEnabled(enabled)
    }

    fun onDownloadComplete(downloadId: Long, activity: AppCompatActivity) = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id: Long = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: return

            if (downloadId == id) {
            }
        }
    }

    fun addDownloadListener(callback: ((String, Long) -> Unit)? = null) {
        setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
            fun download() = try {
                val decodedContentDisposition = URLDecoder.decode(contentDisposition, "UTF-8")
                var fileName = decodedContentDisposition.replace("attachment;filename*=utf-8''", "")
                val mimeTypeNew = if (fileName.isNotEmpty()) {
                    if (fileName.endsWith(";")) {
                        fileName = fileName.substring(0, fileName.length - 1)
                    }
                    if (fileName.startsWith("\"") && fileName.endsWith("\"")) {
                        fileName = fileName.substring(1, fileName.length - 1)
                    }
                    MimeTypeMap.getSingleton()
                        .getMimeTypeFromExtension(mimeType)
                } else {
                    mimeType
                }

                val request = DownloadManager.Request(url.toUri())
                    .setMimeType(mimeTypeNew)
                    .addRequestHeader("User-Agent", userAgent)
                    .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                    .setTitle(fileName)
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(false)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

                val downloadManager =
                    context.getSystemService(AppCompatActivity.DOWNLOAD_SERVICE) as DownloadManager
                val downloadId = downloadManager.enqueue(request)
                callback?.invoke(url, downloadId)
            } catch (e: Exception) {
                e.printStackTrace()
                callback?.invoke(url, -1)
            }

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    download()
                } else {
                    if (TedPermissionUtil.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        download()
                    } else {
                        TedPermission.create()
                            .setPermissionListener(object : PermissionListener {
                                override fun onPermissionGranted() {
                                    download()
                                }

                                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) = Unit
                            })
                            .setDeniedMessage(R.string.permission_reject)
                            .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            .check()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun canGoBack(): Boolean {
        val path = url?.toUri()?.path
        return path != "/" && super.canGoBack()
    }
}