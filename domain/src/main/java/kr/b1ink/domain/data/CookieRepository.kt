package kr.b1ink.domain.data

import kr.b1ink.domain.model.SiteType

interface CookieRepository {
    suspend fun saveCookies(siteType: SiteType)
    suspend fun clearCookies(siteType: SiteType)
}