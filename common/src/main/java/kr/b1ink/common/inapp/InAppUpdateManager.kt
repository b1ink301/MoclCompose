package kr.b1ink.common.inapp

import android.app.Activity

interface InAppUpdateManager {
    fun initialize(activity: Activity)
    fun checkForUpdate(callback: (Boolean) -> Unit)
//    fun startUpdate()
}