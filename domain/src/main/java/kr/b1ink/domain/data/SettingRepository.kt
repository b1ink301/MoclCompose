package kr.b1ink.domain.data

import kr.b1ink.domain.model.SiteType

interface SettingRepository {
    suspend fun setSiteType(type: SiteType)
    val siteType: SiteType
}