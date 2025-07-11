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
//    val user: String,
//    val nickname: String,
//    val image: String,
    val board: String,
    val boardTitle: String = "",
    val hasImage: Boolean = false,
    val userInfo: UserInfo,
    val like: String,
    val hit: String,
    val isRead: Boolean = false,
)