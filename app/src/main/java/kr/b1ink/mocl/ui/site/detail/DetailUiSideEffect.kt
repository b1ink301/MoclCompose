package kr.b1ink.mocl.ui.site.detail

import kotlinx.serialization.Serializable

@Serializable
sealed interface DetailUiSideEffect {
    data object Refresh : DetailUiSideEffect
}