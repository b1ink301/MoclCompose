package kr.b1ink.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserInfo(
    val id:String,
    val nickName:String = "",
    val nickImage:String = "",
    val nameMemo:String = "",
    val ip:String = "",
    val isBlock: Boolean = false,
    val isMe: Boolean = false,
    val isAuthor: Boolean = false,
)
