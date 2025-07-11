package kr.b1ink.data.site.dto

import kr.b1ink.data.base.DtoMapper
import kr.b1ink.domain.model.MainItem
import kr.b1ink.domain.model.SiteType

data class MainItemDto(
    val id: Long = 0,
    val title: String = "",
    val url: String = "",
    val info: String = "",
    val board: String = "",
    val type: Int = 0,
    val siteType: SiteType = SiteType.None,
) : DtoMapper<MainItemDto, MainItem> {
    override fun MainItemDto.mapping(): MainItem = toMainItem()
}

fun MainItemDto.toMainItem() =
    MainItem(
        id = id,
        title = title,
        url = url,
        info = info,
        board = board,
        siteType = siteType,
    )