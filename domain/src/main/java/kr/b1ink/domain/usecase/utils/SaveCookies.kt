package kr.b1ink.domain.usecase.utils

import kr.b1ink.domain.data.CookieRepository
import kr.b1ink.domain.model.SiteType
import javax.inject.Inject

class SaveCookies @Inject constructor(
    private val repository: CookieRepository
) {
    suspend operator fun invoke(siteType: SiteType) = repository.saveCookies(siteType)
}