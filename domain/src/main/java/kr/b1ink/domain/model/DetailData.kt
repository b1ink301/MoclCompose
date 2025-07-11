package kr.b1ink.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DetailData(
    val title: String,
    val info: String,
    val time: String,
    val viewCount: String,
    val likeCount: String,
    val bodyHtml: String,
    val csrf: String,
    val userInfo: UserInfo,
    val comments: List<DetailComment> = emptyList(),
)
