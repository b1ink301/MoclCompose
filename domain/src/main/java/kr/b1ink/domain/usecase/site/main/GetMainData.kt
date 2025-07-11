package kr.b1ink.domain.usecase.site.main

import kr.b1ink.domain.data.SiteRepository
import kr.b1ink.domain.model.MainItem
import kr.b1ink.domain.model.SiteType
import kr.b1ink.domain.base.Result
import javax.inject.Inject

class GetMainData @Inject constructor(
    private val repository: SiteRepository
) {
    suspend operator fun invoke(siteType: SiteType): Result<List<MainItem>> = repository.getMainData(siteType)
}