package kr.b1ink.domain.usecase.utils

import kr.b1ink.domain.data.CookieRepository
import kr.b1ink.domain.data.SiteRepository
import kr.b1ink.domain.model.SiteType
import javax.inject.Inject

class ClearCookies @Inject constructor(
    private val repository: CookieRepository
) {
    suspend operator fun invoke(
        siteType: SiteType,
    ) = repository.clearCookies(siteType)
}