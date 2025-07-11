package kr.b1ink.data.site.clien

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
class ClienApiImpl @Inject constructor(
    @param:IoDispatcher private val dispatcher: CoroutineDispatcher,
    private val api: ClienApi,
    private val parser: ClienParserImpl,
) : BaseApi {
    override suspend fun getMain(): List<MainItemDto> = withContext(dispatcher) {
        TODO("[getMain ] Not yet implemented")
    }

    override suspend fun getList(
        board: String,
        page: Int,
        lastId: Long
    ): List<ListItemDto> = withContext(dispatcher) {
        val response = if (hasNoPage(board)) {
            api.recommend()
        } else {
            api.list(boardCd = board, po = page)
        }
        parser.list(html = response, boardCd = board, lastId = lastId)
    }

    override fun hasNoPage(board: String): Boolean = board == "recommend"

    override suspend fun getDetail(
        board: String,
        id: Long
    ): DetailDto = withContext(dispatcher) {
        val response = api.detail(boardCd = board, boardSn = id)
        parser.detail(html = response, board = board, id = id)
    }
}