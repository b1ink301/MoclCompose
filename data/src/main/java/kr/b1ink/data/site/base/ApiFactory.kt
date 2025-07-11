package kr.b1ink.data.site.base

import kr.b1ink.data.di.data.ClienApiQualifier
import kr.b1ink.data.di.data.DamoangApiQualifier
import kr.b1ink.data.di.data.MeecoApiQualifier
import kr.b1ink.data.di.data.NaverCafeApiQualifier
import kr.b1ink.domain.model.SiteType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiFactory @Inject constructor(
    @param:ClienApiQualifier private val clienApi: BaseApi,
    @param:DamoangApiQualifier private val damoangApi: BaseApi,
    @param:NaverCafeApiQualifier private val naverCafeApi: BaseApi,
    @param:MeecoApiQualifier private val meecoApi: BaseApi,
) {
    fun createApi(siteType: SiteType): BaseApi = when (siteType) {
        SiteType.Clien -> clienApi
        SiteType.Damoang -> damoangApi
        SiteType.NaverCafe -> naverCafeApi
        SiteType.Meeco -> meecoApi
        else -> throw IllegalArgumentException("Unknown SiteType")
    }
}