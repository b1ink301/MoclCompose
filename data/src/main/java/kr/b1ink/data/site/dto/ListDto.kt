package kr.b1ink.data.site.dto

import kr.b1ink.data.base.DtoMapper
import kr.b1ink.domain.model.ListItem
import kr.b1ink.domain.model.UserInfo

data class ListItemDto(
    val id: Long = 0,
    val title: String,
    val reply: String,
    val info: String = "",
    val category: String,
    val time: String,
    val url: String,
    val board: String,
    val userInfo: UserInfo,
    val hasImage: Boolean = false,
    val type: Int = 0,
    val like: String,
    val hit: String,
    val isRead: Boolean = false
) : DtoMapper<ListItemDto, ListItem> {
    override fun ListItemDto.mapping(): ListItem = toListItem()
}

fun ListItemDto.toListItem() =
    ListItem(
        id = id,
        title = title,
        url = url,
        info = info,
        reply = reply,
        userInfo = userInfo,
        category = category,
        time = time,
        board = board,
        hasImage = hasImage,
        like = like,
        hit = hit,
        isRead = isRead,
    )
