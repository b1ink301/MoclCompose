package kr.b1ink.data.site.damoang

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kr.b1ink.data.di.coroutine.IoDispatcher
import kr.b1ink.data.site.base.BaseApi
import kr.b1ink.data.site.dto.DetailDto
import kr.b1ink.data.site.dto.ListItemDto
import kr.b1ink.data.site.dto.MainItemDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DamoangApiImpl @Inject constructor(
    @param:IoDispatcher private val dispatcher: CoroutineDispatcher,
    private val api: DamoangApi,
    private val parser: DamoangParserImpl,
) : BaseApi {
    override suspend fun getMain(): List<MainItemDto> = withContext(dispatcher) {
        TODO("[getMain] Not yet implemented")
    }

    override suspend fun getList(
        board: String,
        page: Int,
        lastId: Long
    ): List<ListItemDto> = withContext(dispatcher) {
        val response = api.list(board = board, page = page)
        parser.list(html = response, boardCd = board, lastId = lastId)
    }

    override suspend fun getDetail(
        board: String,
        id: Long
    ): DetailDto = withContext(dispatcher) {
        val response = api.detail(board = board, id = id)
        parser.detail(html = response, board = board, id = id)
    }

    override fun hasNoPage(board: String): Boolean = false
}