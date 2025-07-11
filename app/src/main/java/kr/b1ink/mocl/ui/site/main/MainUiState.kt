package kr.b1ink.mocl.ui.site.main

import kotlinx.serialization.Serializable
import kr.b1ink.domain.model.MainItem

@Serializable
sealed class MainUiState {
    data class Success(val list: List<MainItem>) : MainUiState()
    data class Error(val message: String? = null) : MainUiState()
    data object Loading : MainUiState()
    data class NotLogin(val message: String) : MainUiState()
    data object Empty : MainUiState()
}