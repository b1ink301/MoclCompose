package kr.b1ink.data.site.clien

import kotlinx.collections.immutable.toImmutableList
import kr.b1ink.data.BuildConfig
import kr.b1ink.data.site.base.BaseParser
import kr.b1ink.data.site.base.attrOrEmpty
import kr.b1ink.data.site.base.extractBoardFromUrl
import kr.b1ink.data.site.base.textOrEmpty
import kr.b1ink.data.site.dto.DetailDto
import kr.b1ink.data.site.dto.ListItemDto
import kr.b1ink.data.util.toTimeAgo
import kr.b1ink.domain.model.DetailComment
import kr.b1ink.domain.model.UserInfo
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class ClienParserImpl @Inject constructor() : BaseParser {
    companion object {
        private const val NOTICE_CATEGORY = "공지"
        private const val DELETED_COMMENT_TEXT = "<span>삭제 되었습니다.</span>"
        private const val INVALID_COMMENT_SN = -1L

        // Jsoup Selectors
        private const val SELECTOR_LIST_ITEM = "a.list_item.symph-row"
        private const val SELECTOR_LIST_CATEGORY = "div.list_infomation > div.list_number > span.category"
        private const val SELECTOR_LIST_TITLE = "div.list_title > div.list_subject > span[data-role=list-title-text]"
        private const val SELECTOR_LIST_TIME = "div.list_infomation > div.list_number > div.list_time > span"
        private const val SELECTOR_LIST_NICK_IMAGE =
            "div.list_infomation > div.list_author > span.nickimg > img, div.list_infomation > div.list_author > span.list_nickname > span.nickimg > img"
        private const val SELECTOR_LIST_HIT = "div.list_infomation > div.list_number > div.list_hit > span"
        private const val SELECTOR_LIST_LIKE = "div.list_title > div.list_symph > span"
        private const val SELECTOR_LIST_NICKNAME =
            "div.list_infomation > div.list_author > span.nickname, div.list_infomation > div.list_author > span.list_nickname > span.nickname"
        private const val SELECTOR_LIST_HAS_IMAGE = "div.list_title > span.fa-picture-o"

        private const val SELECTOR_DETAIL_CONTAINER = "body > div.nav_container > div.content_view"
        private const val SELECTOR_DETAIL_CSRF = "body > nav.navigation > div.dropdown-menu > form > input[name=_csrf]"
        private const val SELECTOR_DETAIL_TITLE = "div.post_title > div.post_subject > span"
        private const val SELECTOR_DETAIL_TIME = "div.post_information > div.post_time > div.post_date"
        private const val SELECTOR_DETAIL_BODY_HTML = "div.post_view > div.post_content > article > div.post_article"
        private const val SELECTOR_DETAIL_VIEW_COUNT = "div.post_information > div.post_time > div.view_count"
        private const val SELECTOR_DETAIL_AUTHOR_IP = "div.post_information > div.author_ip"
        private const val SELECTOR_DETAIL_USER_ID_BUTTON =
            "div.post_view > div.post_contact > span.contact_note > div.post_memo > div.memo_box > button.button_input"
        private const val SELECTOR_DETAIL_NICKNAME =
            "div.post_view > div.post_contact > span.contact_name > span.nickname, div.post_view > div.post_contact > span.contact_name > span.list_nickname > span.nickname"
        private const val SELECTOR_DETAIL_NICK_IMAGE =
            "div.post_view > div.post_contact > span.contact_name > span.nickimg > img, div.post_view > div.post_contact > span.contact_name > span.list_nickname > span.nickimg > img"
        private const val SELECTOR_DETAIL_LIKE_COUNT = "div.post_button > div.symph_area > button.symph_count > strong"
        private const val SELECTOR_DETAIL_COMMENT_ROW = "div.post_comment > div.comment > div.comment_row"

        private const val SELECTOR_COMMENT_NICKNAME =
            "div.comment_info > div.post_contact > span.contact_name > span.nickname, div.comment_info > div.post_contact > span.contact_name > span.list_nickname > span.nickname"
        private const val SELECTOR_COMMENT_IP =
            "div.comment_info > div.comment_info_area > div.comment_ip > span.ip_address"
        private const val SELECTOR_COMMENT_TIME_AREA = "div.comment_info > div.comment_info_area > div.comment_time"
        private const val SELECTOR_COMMENT_TIME_TIMESTAMP = "span.timestamp" // To remove from time area
        private const val SELECTOR_COMMENT_NICK_IMAGE =
            "div.comment_info > div.post_contact > span.contact_name > span.nickimg > img, div.comment_info > div.post_contact > span.contact_name > span.list_nickname > span.nickimg > img"
        private const val SELECTOR_COMMENT_LIKE_COUNT = "div.comment_content_symph > button > strong"
        private const val SELECTOR_COMMENT_BODY_VIEW = "div.comment_content > div.comment_view"
        private const val SELECTOR_COMMENT_BODY_CLEANUP_TAGS =
            "input, span.name, button" // Tags to remove from comment body
        private const val SELECTOR_COMMENT_VIDEO_ATTACH_SOURCE = "div.comment-video > video > source"
        private const val SELECTOR_COMMENT_IMAGE_ATTACH_IMG = "div.comment-img > img"
    }

    override suspend fun list(
        html: String,
        boardCd: String,
        lastId: Long,
    ) =
        Jsoup
            .parseBodyFragment(html, BuildConfig.CLIEN_API_URL)
            .select(SELECTOR_LIST_ITEM)
            .mapNotNull { element ->
                element.toListItemDto(boardCd, lastId)
            }
            .toImmutableList()

    override suspend fun detail(
        html: String,
        board: String,
        id: Long
    ): DetailDto = html.toDetailDtoInternal(board, id)


    private fun buildInfoString(nickName: String, formattedTime: String, hit: String): String = buildString {
        append(nickName)
        if (formattedTime.isNotBlank()) {
            if (isNotEmpty()) append("ㆍ")
            append(formattedTime)
        }
        if (hit.isNotBlank()) {
            if (isNotEmpty()) append("ㆍ")
            append("$hit 읽음")
        }
    }

    private fun Element.toListItemDto(
        boardCd: String,
        lastId: Long,
    ): ListItemDto? {
        val category = selectFirst(SELECTOR_LIST_CATEGORY)?.text() ?: ""
        if (NOTICE_CATEGORY == category) {
            return null
        }
        val id = attr("data-board-sn").toLongOrNull() ?: return null
        if (id < 0 || lastId in 1..id) {
            Timber.Forest.d("[ClienParser] SKIP ===> lastId=$lastId, id=$id")
            return null
        }

        val userId = attr("data-author-id") // user id
        val url = absUrl("href") // link abs url
        val reply = attr("data-comment-count") // link abs url
        val board = url.extractBoardFromUrl(boardCd)
        val title = textOrEmpty(SELECTOR_LIST_TITLE)
        val time = textOrEmpty(SELECTOR_LIST_TIME)
        val nickImage = attrOrEmpty(SELECTOR_LIST_NICK_IMAGE, "src")
        val hit = textOrEmpty(SELECTOR_LIST_HIT)
        val like = textOrEmpty(SELECTOR_LIST_LIKE)
//    val memo = select("div.list_infomation > div.list_author > span.memo").text()
        val nickName = textOrEmpty(SELECTOR_LIST_NICKNAME)
        val hasImage = selectFirst(SELECTOR_LIST_HAS_IMAGE) != null
        val info = buildInfoString(nickName, time.toTimeAgo(), hit)

        return ListItemDto(
            id = id,
            title = title,
            reply = reply,
            userInfo = UserInfo(
                id = userId,
                nickName = nickName,
                nickImage = nickImage,
            ),
            info = info,
            category = category,
            time = time,
            url = url,
            board = board,
            hasImage = hasImage,
            like = like,
            hit = hit,
        )
    }

    private fun String.toTimeAgo(): String {
        try {
            val supportedFormats = listOf(
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
                SimpleDateFormat("yy-MM-dd", Locale.getDefault()),
                SimpleDateFormat("MM-dd", Locale.getDefault()),
            )
            for (format in supportedFormats) {
                try {
                    format.isLenient = false // 엄격한 파싱
                    return format.parse(this)!!
                        .toTimeAgo()
                } catch (_: Exception) {
                }
            }
            throw Exception("Error parsing date string: $this")
        } catch (e: Exception) {
            return if (contains(":")) {
                val calendar: Calendar = Calendar.getInstance()
                val timeParts = split(":")
                val hour = timeParts[0].toInt()
                val minute = timeParts[1].toInt()
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.time.toTimeAgo()
            } else {
                e.printStackTrace()
                this
            }
        }
    }

    private fun extractUserIdFromOnClick(onClickAttr: String): String {
        // 예시: "popup_note('/memo/xxxxxxxxxx/form'); return false;" 에서 xxxxxxxxxx 추출
        // 이 로직은 매우 취약하며, 실제 onclick 내용에 따라 강력한 정규식이나 다른 분석 방법이 필요합니다.
        // 여기서는 매우 단순화된 예시를 제공합니다.
        val regex = Regex("/memo/([^/]+)/form")
        return regex.find(onClickAttr)?.groupValues?.getOrNull(1) ?: ""
    }

    fun String.toDetailDtoInternal(
        originalBoard: String, originalId: Long
    ): DetailDto {
        val document = Jsoup.parseBodyFragment(this@toDetailDtoInternal, BuildConfig.CLIEN_API_URL)
        val container = document.selectFirst(SELECTOR_DETAIL_CONTAINER)
            ?: throw IllegalStateException("Detail article container not found for $originalBoard/$originalId")

//    val marketInfo = container.select("div.post_view > div.market_product")
        val csrf = document.attrOrEmpty(SELECTOR_DETAIL_CSRF, "value")
        val title = container.textOrEmpty(SELECTOR_DETAIL_TITLE)
        val time = container.textOrEmpty(SELECTOR_DETAIL_TIME)

        val bodyElement = container.selectFirst(SELECTOR_DETAIL_BODY_HTML)
        bodyElement?.select("input, button")
            ?.remove()

        val viewCount = container.textOrEmpty(SELECTOR_DETAIL_VIEW_COUNT)
        val authorIp = container.textOrEmpty(SELECTOR_DETAIL_AUTHOR_IP)
        val userIdOnClick = container.attrOrEmpty(SELECTOR_DETAIL_USER_ID_BUTTON, "onclick")
        val userId = extractUserIdFromOnClick(userIdOnClick)
        val nickName = container.textOrEmpty(SELECTOR_DETAIL_NICKNAME)
        val nickImage = container.attrOrEmpty(SELECTOR_DETAIL_NICK_IMAGE, "src")
        val likeCount = container.textOrEmpty(SELECTOR_DETAIL_LIKE_COUNT)
        val bodyHtml = bodyElement?.html() ?: ""

        val comments = container.select(SELECTOR_DETAIL_COMMENT_ROW)
            .mapNotNull { element -> parseCommentElement(element) }

        val tmpTime = time.split("수정일")
            .firstOrNull()
            ?.trim() ?: time

        val info = buildInfoString(nickName = nickName, formattedTime = tmpTime.toTimeAgo(), hit = viewCount)

        return DetailDto(
            viewCount = viewCount,
            likeCount = likeCount,
            csrf = csrf,
            title = title,
            time = time,
            info = info,
            userInfo = UserInfo(
                id = userId,
                nickName = nickName,
                nickImage = nickImage,
                ip = authorIp,
            ),
            bodyHtml = bodyHtml,
            comments = comments
        )
    }

    private fun parseCommentElement(element: Element): DetailComment? {
        if (element.html() == DELETED_COMMENT_TEXT) return null
        val sn = element.attr("data-comment-sn")
            .toLongOrNull() ?: return null
        val userId = element.attr("data-author-id")
        val isReply = element.hasClass("re")
        val nickName = element.textOrEmpty(SELECTOR_COMMENT_NICKNAME)
        val ip = element.textOrEmpty(SELECTOR_COMMENT_IP)
        val timeElement = element.selectFirst(SELECTOR_COMMENT_TIME_AREA)
        timeElement?.select(SELECTOR_COMMENT_TIME_TIMESTAMP)
            ?.remove()
        val time = timeElement?.ownText()
            ?.trim() ?: ""
        val nickImage = element.attrOrEmpty(SELECTOR_COMMENT_NICK_IMAGE, "src")
        val likeCount = element.textOrEmpty(SELECTOR_COMMENT_LIKE_COUNT)
        val bodyElement = element.selectFirst(SELECTOR_COMMENT_BODY_VIEW)
        bodyElement?.select(SELECTOR_COMMENT_BODY_CLEANUP_TAGS)
            ?.remove()
        val videoAttach = element.attrOrEmpty(SELECTOR_COMMENT_VIDEO_ATTACH_SOURCE, "src")
        val imageAttach = element.attrOrEmpty(SELECTOR_COMMENT_IMAGE_ATTACH_IMG, "src")

        var mediaHtml = ""
        if (imageAttach.isNotEmpty() && imageAttach.startsWith("http")) {
            mediaHtml = "<img src=\"$imageAttach\"><br>"
        }
        if (videoAttach.isNotEmpty() && videoAttach.startsWith("http")) {
            mediaHtml += "<video src=\"$videoAttach\"></video><br>"
        }

        val commentBodyHtml = mediaHtml + (bodyElement?.html() ?: "")
        val info = buildInfoString(nickName = nickName, formattedTime = time.toTimeAgo(), hit = "")

        return DetailComment(
            id = sn,
            isReply = isReply,
            bodyHtml = commentBodyHtml,
            likeCount = likeCount,
            time = time,
            info = info,
            userInfo = UserInfo(
                id = userId,
                nickName = nickName,
                nickImage = nickImage,
                nameMemo = "", // nameMemo는 어디서 가져오는지 확인 필요
                ip = ip
            ),
        )
    }
}