package kr.b1ink.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ListItem(
    val id: Long = 0,
    val title: String,
    val category: String,
    val info: String,
    val reply: String,
    val time: String,
    val url: String,
    val board: String,
    val boardTitle: String = "",
    val like: String,
    val hit: String,
    val userInfo: UserInfo,
    val hasImage: Boolean = false,
    val isRead: Boolean = false,
)