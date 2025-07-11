package kr.b1ink.data.site.base

import kr.b1ink.data.site.dto.DetailDto
import kr.b1ink.data.site.dto.ListItemDto
import kr.b1ink.data.site.dto.MainItemDto

interface BaseApi {
    suspend fun getMain(): List<MainItemDto>

    suspend fun getList(board: String, page: Int, lastId: Long = -1): List<ListItemDto>

    suspend fun getDetail(board: String, id: Long): DetailDto

    fun hasNoPage(board: String): Boolean
}