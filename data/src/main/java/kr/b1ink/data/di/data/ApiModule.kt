package kr.b1ink.data.di.data

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.b1ink.data.site.base.ApiFactory
import kr.b1ink.data.site.clien.ClienApiImpl
import kr.b1ink.data.site.damoang.DamoangApiImpl
import kr.b1ink.data.site.base.BaseApi
import kr.b1ink.data.site.meeco.MeecoApiImpl
import kr.b1ink.data.site.navercafe.NaverCafeApiImpl
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class ApiModule {
    @DamoangApiQualifier
    @Binds
    abstract fun bindDamoangApi(impl: DamoangApiImpl): BaseApi

    @ClienApiQualifier
    @Binds
    abstract fun bindClienApi(impl: ClienApiImpl): BaseApi

    @NaverCafeApiQualifier
    @Binds
    abstract fun bindNaverCafeApi(impl: NaverCafeApiImpl): BaseApi

    @MeecoApiQualifier
    @Binds
    abstract fun bindMeecoApi(impl: MeecoApiImpl): BaseApi

    companion object {
        @Provides
        @Singleton
        fun provideApiFactory(
            @DamoangApiQualifier damoangApi: BaseApi,
            @ClienApiQualifier clienApi: BaseApi,
            @NaverCafeApiQualifier naverCafeApi: BaseApi,
            @MeecoApiQualifier meecoApi: BaseApi,
        ): ApiFactory = ApiFactory(
            clienApi = clienApi,
            damoangApi = damoangApi,
            naverCafeApi = naverCafeApi,
            meecoApi = meecoApi
        )
    }
}