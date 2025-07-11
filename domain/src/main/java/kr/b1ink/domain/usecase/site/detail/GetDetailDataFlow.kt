package kr.b1ink.domain.usecase.site.detail

import kotlinx.coroutines.flow.flow
import kr.b1ink.domain.base.Result
import kr.b1ink.domain.data.SiteRepository
import kr.b1ink.domain.model.SiteType
import javax.inject.Inject

class GetDetailDataFlow @Inject constructor(
    private val repository: SiteRepository
) {
    operator fun invoke(
        siteType: SiteType,
        id: Long,
        board: String
    ) = flow {
        try {
            emit(Result.Loading)
            if (siteType != SiteType.None) {
                emit(repository.getDetail(siteType, board, id))
            }
        } catch (e:Exception) {
            emit(Result.Error(error = e.localizedMessage))
        }
    }
}