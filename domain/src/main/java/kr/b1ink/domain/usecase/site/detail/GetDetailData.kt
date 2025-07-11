package kr.b1ink.domain.usecase.site.detail

import kr.b1ink.domain.base.Result
import kr.b1ink.domain.data.SiteRepository
import kr.b1ink.domain.model.SiteType
import javax.inject.Inject

class GetDetailData @Inject constructor(
    private val repository: SiteRepository
) {
    suspend operator fun invoke(
        siteType: SiteType,
        id: Long,
        board: String
    ) = try {
        repository.getDetail(siteType, board, id)
    } catch (e: Exception) {
        Result.Error(error = e.localizedMessage)
    }
}