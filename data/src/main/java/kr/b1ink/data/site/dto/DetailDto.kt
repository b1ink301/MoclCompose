package kr.b1ink.data.site.dto

import kr.b1ink.data.base.DtoMapper
import kr.b1ink.domain.model.DetailComment
import kr.b1ink.domain.model.DetailData
import kr.b1ink.domain.model.UserInfo

data class DetailDto(
    val title: String,
    val info: String,
    val time: String,
    val viewCount: String,
    val likeCount: String,
    val bodyHtml: String,
    val csrf: String = "",
    val userInfo: UserInfo,
    val comments: List<DetailComment> = emptyList(),
) : DtoMapper<DetailDto, DetailData> {
    override fun DetailDto.mapping(): DetailData = toDetailData()
}

fun DetailDto.toDetailData() = DetailData(
    viewCount = viewCount,
    likeCount = likeCount,
    bodyHtml = bodyHtml,
    csrf = csrf,
    title = title,
    info = info,
    time = time,
    userInfo = userInfo,
    comments = comments
)