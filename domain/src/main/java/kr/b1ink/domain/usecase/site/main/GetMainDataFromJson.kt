package kr.b1ink.domain.usecase.site.main

import kr.b1ink.domain.data.SiteRepository
import kr.b1ink.domain.model.SiteType
import javax.inject.Inject

class GetMainDataFromJson @Inject constructor(
    private val repository: SiteRepository
) {
    suspend operator fun invoke(siteType: SiteType) =
        repository.getMainDataFromJson(siteType)
}