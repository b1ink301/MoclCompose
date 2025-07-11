package kr.b1ink.data.site.navercafe

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kr.b1ink.data.di.coroutine.IoDispatcher
import kr.b1ink.data.site.base.BaseApi
import kr.b1ink.data.site.dto.DetailDto
import kr.b1ink.data.site.dto.ListItemDto
import kr.b1ink.data.site.dto.MainItemDto
import kr.b1ink.data.util.toTimeAgo
import kr.b1ink.domain.model.CafeFailure
import kr.b1ink.domain.model.LoginFailure
import kr.b1ink.domain.model.UserInfo
import timber.log.Timber
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NaverCafeApiImpl @Inject constructor(
    @param:IoDispatcher private val dispatcher: CoroutineDispatcher,
    private val api: NaverCafeApi,
) : BaseApi {
    override suspend fun getMain(): List<MainItemDto> = withContext(dispatcher) {
        val result = api.cafes().message

        if (result.isSuccess()) {
            result.getList()
                .map { it.toMainItemDto() }
        } else {
            Timber.d("[getMain] status=${result.status}, error=${result.error}")

            throw if (result.error.code == "0004") {
                LoginFailure(result.error.code, result.error.msg)
            } else {
                CafeFailure(result.error.code, result.error.msg)
            }
        }
    }

    override suspend fun getList(
        board: String,
        page: Int,
        lastId: Long
    ): List<ListItemDto> = withContext(dispatcher) {
        val result = api.list(board.toLong(), page = page).message

        if (result.isSuccess()) {
            result.getList()
                .map { it.toListItemDto() }
        } else {
            Timber.d("[getList] status=${result.status}, error=${result.error}")
            throw CafeFailure(result.error.code, result.error.msg)
        }
    }

    override suspend fun getDetail(
        board: String,
        id: Long
    ): DetailDto = withContext(dispatcher) {
        val detailResultDeferred = async { api.detail(board, articleId = id).result }
        val commentsResultDeferred = async { api.comments(board, articleId = id).result }

        val detailResult = detailResultDeferred.await()
        val commentsResult = commentsResultDeferred.await()

        if (detailResult.isSuccess() && commentsResult.isSuccess()) {
            val article = detailResult.article
            val comments = commentsResult.comments.items.map { it.toDetailComment() }
            val time = Date(article.writeDate).toTimeAgo()

            DetailDto(
                title = article.subject,
                viewCount = article.readCount.toString(),
                likeCount = "",
                bodyHtml = article.contentHtml,
                info = "${article.writer.nick}ㆍ${time}ㆍ${article.readCount} 읽음",
                time = article.writeDate.toString(),
                userInfo = UserInfo(
                    id = article.writer.memberKey,
                    nickName = article.writer.nick,
                    nickImage = article.writer.image?.url ?: ""
                ),
                comments = comments
            )
        } else {
            val errorMessages = mutableListOf<String>()
            val errorCodes = mutableListOf<String>()

            detailResult.message?.let { errorMessages.add("Detail: $it") }
            detailResult.errorCode?.let { errorCodes.add("Detail: $it") }
            commentsResult.message?.let { errorMessages.add("Comments: $it") }
            commentsResult.errorCode?.let { errorCodes.add("Comments: $it") }

            val combinedMessage = errorMessages.joinToString(separator = "\n")
            val combinedErrorCode = errorCodes.joinToString(separator = ", ")

            Timber.e("Error fetching details: $combinedMessage (Codes: $combinedErrorCode)")
            throw CafeFailure(combinedErrorCode, combinedMessage)
        }
    }

    override fun hasNoPage(board: String): Boolean = false
}