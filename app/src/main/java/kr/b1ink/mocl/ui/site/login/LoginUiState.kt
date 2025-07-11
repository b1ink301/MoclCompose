package kr.b1ink.mocl.ui.site.login

import kr.b1ink.domain.model.MainItem

sealed interface LoginUiState {
    data class Success(val list: List<MainItem>) : LoginUiState
    data class Error(val message: String? = null) : LoginUiState
    data object Loading : LoginUiState
}