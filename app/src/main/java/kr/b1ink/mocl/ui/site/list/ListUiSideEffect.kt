package kr.b1ink.mocl.ui.site.list

import kotlinx.serialization.Serializable

@Serializable
sealed class ListUiSideEffect {
    data class ReadId(val id: Long) : ListUiSideEffect()
    data object Refresh : ListUiSideEffect()
}