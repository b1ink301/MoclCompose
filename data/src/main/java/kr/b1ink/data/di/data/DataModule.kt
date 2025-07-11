package kr.b1ink.data.di.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.b1ink.data.etc.CookieRepositoryImpl
import kr.b1ink.data.setting.SettingRepositoryImpl
import kr.b1ink.data.site.SiteRepositoryImpl
import kr.b1ink.domain.data.CookieRepository
import kr.b1ink.domain.data.SettingRepository
import kr.b1ink.domain.data.SiteRepository
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class DataModule {
    @Singleton
    @Binds
    abstract fun bindSettingRepository(
        settingRepository: SettingRepositoryImpl
    ): SettingRepository

    @Singleton
    @Binds
    abstract fun bindSiteRepository(
        siteRepository: SiteRepositoryImpl
    ): SiteRepository

    @Singleton
    @Binds
    abstract fun bindCookieRepository(
        cookieRepository: CookieRepositoryImpl
    ): CookieRepository
}