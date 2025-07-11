package kr.b1ink.domain.usecase.site.main

import kr.b1ink.domain.data.SiteRepository
import kr.b1ink.domain.model.MainItem
import kr.b1ink.domain.model.SiteType
import javax.inject.Inject

class SetMainData @Inject constructor(
    private val repository: SiteRepository
) {
    suspend operator fun invoke(
        siteType: SiteType,
        list: List<MainItem>,
    ) = repository.setMainData(siteType, list)
}