package kr.b1ink.domain.model

import kotlinx.serialization.Serializable

@Serializable
class DetailComment(
    val id: Long = 0,
    val bodyHtml: String,
    val time: String,
    val info: String,
    val likeCount: String,
    val userInfo: UserInfo,
    val authorId: String = "",
    val isReply:Boolean = false,
)
