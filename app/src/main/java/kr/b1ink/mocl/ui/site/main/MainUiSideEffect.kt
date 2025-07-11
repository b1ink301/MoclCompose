package kr.b1ink.mocl.ui.site.main

import kotlinx.serialization.Serializable
import kr.b1ink.domain.model.SiteType

@Serializable
sealed class MainUiSideEffect {
    data object Refresh : MainUiSideEffect()
    data class ChangeSiteType(val type: SiteType) : MainUiSideEffect()
}