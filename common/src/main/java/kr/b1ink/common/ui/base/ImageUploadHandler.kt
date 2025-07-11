package kr.b1ink.common.ui.base

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import kr.b1ink.common.R
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ImageUploadHandler(private val activity: FragmentActivity) {
    private var valueCallback: ValueCallback<Array<Uri>>? = null
    private var cameraPhotoUri: Uri? = null
    private var dialog: AlertDialog? = null

    private val getFile =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            handleActivityResult(result)
        }

    fun setupChromeClient(): WebChromeClient = object : WebChromeClient() {
        override fun onShowFileChooser(
            webView: WebView?,
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {
//            valueCallback?.onReceiveValue(null)
            valueCallback = filePathCallback
            showImageSourceDialog()
            return true
        }
    }

    private fun showImageSourceDialog() {
        dialog?.dismiss()
        dialog = AlertDialog.Builder(activity)
            .setTitle(R.string.upload_image_dialog_title)
            .setItems(activity.resources.getTextArray(R.array.upload_image_options)) { _, which ->
                when (which) {
                    0 -> takePhoto()
                    1 -> chooseFromGallery()
                }
            }
            .setCancelable(false)
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                clearValueCallback()
            }
            .show()
    }

    private fun clearValueCallback() {
        valueCallback?.onReceiveValue(null)
        valueCallback = null
    }

    private fun takePhoto() {
        cameraPhotoUri = null
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(activity.packageManager) != null) {
            val photoFile = createImageFile()
            if (photoFile != null) {
                cameraPhotoUri = FileProvider.getUriForFile(
                    activity,
                    "${activity.packageName}.fileprovider",
                    photoFile
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraPhotoUri)
                getFile.launch(takePictureIntent)
            } else {
                clearValueCallback()
            }
        } else {
            clearValueCallback()
        }
    }

    private fun createImageFile(): File? = try {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        File.createTempFile(imageFileName, ".jpg", storageDir)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    private fun chooseFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        getFile.launch(Intent.createChooser(intent, activity.getString(R.string.select_picture)))
    }

    private fun handleActivityResult(result: ActivityResult) {
        try {
            val result = if (result.resultCode == Activity.RESULT_OK) {
                if (result.data?.data != null) {
                    WebChromeClient.FileChooserParams.parseResult(Activity.RESULT_OK, result.data)
                } else if (cameraPhotoUri != null) {
                    WebChromeClient.FileChooserParams.parseResult(Activity.RESULT_OK, Intent().apply {
                        data = cameraPhotoUri
                    })
                } else {
                    null
                }
            } else {
                null
            }

            Timber.d("handleActivityResult: $result, valueCallback=$valueCallback")

            if (result != null) {
                valueCallback?.onReceiveValue(result)
                valueCallback = null
            } else {
                throw Exception("handleActivityResult result is null")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error handling activity result")
            clearValueCallback()
        }
    }

    fun onDestroy() {
        dialog?.dismiss()
        clearValueCallback()
    }
}