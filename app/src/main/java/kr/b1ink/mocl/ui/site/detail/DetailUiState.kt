package kr.b1ink.mocl.ui.site.detail

import kotlinx.serialization.Serializable
import kr.b1ink.domain.model.DetailData

@Serializable
sealed interface DetailUiState {
    data class Success(val data: DetailData) : DetailUiState
    data class Error(val message: String? = null) : DetailUiState
    data object Loading : DetailUiState
}