package kr.b1ink.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class MainItem(
    val id: Long = 0,
    val title: String = "",
    val url: String = "",
    val info: String = "",
    val board: String = "",
    val type: Int = 0,
    val siteType: SiteType = SiteType.None,
    val no: Int = 0,
    val isShow: Boolean = true,
    val isRecommend: Boolean = false,
    val hasRoom: Boolean = false,
)