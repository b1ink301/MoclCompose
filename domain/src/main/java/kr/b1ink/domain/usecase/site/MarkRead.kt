package kr.b1ink.domain.usecase.site

import kr.b1ink.domain.data.SiteRepository
import kr.b1ink.domain.model.SiteType
import javax.inject.Inject

class MarkRead @Inject constructor(
    private val repository: SiteRepository
) {
    suspend operator fun invoke(
        siteType: SiteType,
        id: Long,
    ) = repository.markRead(siteType, id)
}