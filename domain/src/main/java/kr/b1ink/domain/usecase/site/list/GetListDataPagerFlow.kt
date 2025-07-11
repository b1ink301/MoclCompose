package kr.b1ink.domain.usecase.site.list

import kr.b1ink.domain.data.SiteRepository
import kr.b1ink.domain.model.SiteType
import javax.inject.Inject

class GetListDataPagerFlow @Inject constructor(
    private val repository: SiteRepository
) {
    operator fun invoke(
        siteType: SiteType,
        query: String,
    ) = repository.getListPager(siteType, query).flow
}