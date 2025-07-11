package kr.b1ink.mocl.ui.site.main

import kotlinx.serialization.Serializable

@Serializable
sealed interface AddDialogUiSideEffect {
    data object OkDialog : AddDialogUiSideEffect
    data object CancelDialog : AddDialogUiSideEffect
}