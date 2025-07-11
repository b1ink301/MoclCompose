package kr.b1ink.mocl

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import com.google.firebase.appdistribution.FirebaseAppDistribution
import com.google.firebase.appdistribution.FirebaseAppDistributionException
import dagger.hilt.android.HiltAndroidApp
import kr.b1ink.coil.useProgressInterceptor
import kr.b1ink.common.di.qualifier.FireBaseCrashReport
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider, SingletonImageLoader.Factory {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @FireBaseCrashReport
    @Inject
    lateinit var crashReportTree: Timber.Tree

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(crashReportTree)
        }

        initFirebaseAppDistribution()
    }

    private fun initFirebaseAppDistribution() {
        val firebaseAppDistribution = FirebaseAppDistribution.getInstance()

        if (firebaseAppDistribution.isTesterSignedIn) {
            firebaseAppDistribution.updateIfNewReleaseAvailable()
                .addOnProgressListener { updateProgress ->
                    // (Optional) Implement custom progress updates in addition to
                    // automatic NotificationManager updates.
                    Timber.d("Update progress: %s", updateProgress)
                }
                .addOnFailureListener { e ->
                    Timber.e(e, "Update failed")
                    // (Optional) Handle errors.
                    if (e is FirebaseAppDistributionException) {
                        when (e.errorCode) {
                            FirebaseAppDistributionException.Status.NOT_IMPLEMENTED -> {
                                // SDK did nothing. This is expected when building for Play.
                            }

                            else -> {
                                // Handle other errors.
                            }
                        }
                    }
                }
        } else {
            firebaseAppDistribution.signInTester()
                .addOnSuccessListener {
                    Timber.d("Tester signed in")
                }
                .addOnFailureListener {
                    Timber.e(it, "Tester sign-in failed")
                }
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setWorkerFactory(workerFactory)
            .build()

    override fun newImageLoader(context: PlatformContext): ImageLoader = ImageLoader
        .Builder(context)
        .useProgressInterceptor()
        .build()
}