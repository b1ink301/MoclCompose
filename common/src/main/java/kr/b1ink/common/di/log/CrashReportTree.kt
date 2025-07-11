package kr.b1ink.common.di.log

import android.util.Log
import kr.b1ink.common.di.qualifier.FireBaseCrashReport
import timber.log.Timber
import javax.inject.Inject

@FireBaseCrashReport
class CrashReportTree @Inject constructor() : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
           return
        }
    }
}