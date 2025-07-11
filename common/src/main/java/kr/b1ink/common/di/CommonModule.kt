package kr.b1ink.common.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.b1ink.common.di.log.CrashReportTree
import kr.b1ink.common.di.qualifier.FireBaseCrashReport
import kr.b1ink.common.inapp.GooglePlayInAppUpdateManager
import kr.b1ink.common.inapp.InAppUpdateManager
import timber.log.Timber
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class CommonModule {
    @Singleton
    @Binds
    abstract fun bindInAppUpdateManager(
        inAppUpdateManager: GooglePlayInAppUpdateManager
    ): InAppUpdateManager
}

@Module
@InstallIn(SingletonComponent::class)
interface CommonBindsModule {
    @Singleton
    @FireBaseCrashReport
    @Binds
    fun bindCrashTimberTree(tree: CrashReportTree): Timber.Tree
}