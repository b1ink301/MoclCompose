package kr.b1ink.mocl.ui.components.appdrawer

import androidx.annotation.StringRes

data class AppDrawerItemInfo<T>(
    val drawerOption: T,
    @param:StringRes val title: Int,
)