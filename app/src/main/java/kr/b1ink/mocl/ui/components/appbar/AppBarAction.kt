package kr.b1ink.mocl.ui.components.appbar

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class AppBarAction(
    @param:DrawableRes val icon: Int,
    @param:StringRes val description: Int,
    val onClick: () -> Unit
)
