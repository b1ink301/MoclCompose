package kr.b1ink.mocl.ui.site.main

import kotlinx.serialization.Serializable
import kr.b1ink.domain.model.MainItem

@Serializable
sealed interface AddDialogUiState {
    data class Success(val data: List<MainItem>) : AddDialogUiState
    data class Error(val message: String? = null) : AddDialogUiState
    data object Loading : AddDialogUiState
    data object Empty : AddDialogUiState
}