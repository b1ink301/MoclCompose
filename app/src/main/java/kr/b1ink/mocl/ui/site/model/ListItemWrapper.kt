package kr.b1ink.mocl.ui.site.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.serialization.Serializable
import kr.b1ink.domain.model.ListItem

@Serializable
data class ListItemWrapper(
    val listItem: ListItem,
    val isRead: MutableState<Boolean> = mutableStateOf(listItem.isRead)
)
