package kr.b1ink.data.site.meeco

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kr.b1ink.data.di.coroutine.IoDispatcher
import kr.b1ink.data.site.base.BaseApi
import kr.b1ink.data.site.dto.DetailDto
import kr.b1ink.data.site.dto.ListItemDto
import kr.b1ink.data.site.dto.MainItemDto
import javax.inject.Inject

class MeecoApiImpl @Inject constructor(
    @param:IoDispatcher private val dispatcher: CoroutineDispatcher,
    private val api: MeecoApi,
    private val parser: MeecoParserImpl,
) : BaseApi {
    override suspend fun getMain(): List<MainItemDto> {
        TODO("Not yet implemented")
    }

    override suspend fun getList(
        board: String,
        page: Int,
        lastId: Long
    ): List<ListItemDto> = withContext(dispatcher) {
        val htmlString = api.list(board, page)
        parser.list(htmlString, board, lastId)
    }

    override suspend fun getDetail(
        board: String,
        id: Long
    ): DetailDto = withContext(dispatcher) {
        val htmlString = api.detail(board, id)
        parser.detail(html = htmlString, board = board, id = id)
    }

    override fun hasNoPage(board: String): Boolean = false
}