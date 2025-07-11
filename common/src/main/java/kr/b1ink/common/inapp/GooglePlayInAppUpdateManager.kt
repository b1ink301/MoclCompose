package kr.b1ink.common.inapp

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kr.b1ink.common.BuildConfig
import timber.log.Timber

class GooglePlayInAppUpdateManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) : InAppUpdateManager {
    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>

    override fun initialize(activity: Activity) {
        appUpdateManager = AppUpdateManagerFactory.create(context)
        val act = activity as? AppCompatActivity ?: activity as? ComponentActivity
        ?: throw RuntimeException("Activity must be AppCompatActivity or ComponentActivity")

        activityResultLauncher = act.registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // 업데이트 성공 처리
            } else {
                // 업데이트 실패 처리
            }
        }

        act.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) = checkOngoingUpdate()
            override fun onDestroy(owner: LifecycleOwner) = try {
                act.lifecycle.removeObserver(this)
                activityResultLauncher.unregister()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
    }

    private fun checkOngoingUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                startUpdateFlow(appUpdateInfo)
            }
        }
    }

    override fun checkForUpdate(callback: (Boolean) -> Unit) {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->

            val log =
                "updateAvailability=${appUpdateInfo.updateAvailability()},updatePriority=${appUpdateInfo.updatePriority()},availableVersionCode=${appUpdateInfo.availableVersionCode()}"

            Timber.tag("InAppUpdateManager")
                .i(log)

            if (BuildConfig.DEBUG) {
                Toast.makeText(context, log, Toast.LENGTH_LONG)
                    .show()
            }

            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(
                    AppUpdateType.IMMEDIATE
                )
            ) {
                startUpdateFlow(appUpdateInfo)
                callback(true)
            } else {
                callback(false)
            }
        }
            .addOnFailureListener { e ->
                Timber.e(e, "Update check failed")
                callback(false)
            }
    }

    private fun startUpdateFlow(appUpdateInfo: AppUpdateInfo, updateType: Int = AppUpdateType.IMMEDIATE) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                activityResultLauncher,
                AppUpdateOptions.newBuilder(updateType)
                    .build()
            )
        } catch (e: IntentSender.SendIntentException) {
            Timber.e(e, "Error launching update flow")
        }
    }
}