package kr.b1ink.data.site.damoang

import androidx.core.net.toUri
import kr.b1ink.data.BuildConfig
import kr.b1ink.data.site.base.BaseParser
import kr.b1ink.data.site.base.absUrlOrEmpty
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
import java.util.Calendar
import javax.inject.Inject

class DamoangParserImpl @Inject constructor() : BaseParser {
    companion object {
        // List Item Selectors
        private const val SELECTOR_LIST_ITEM_CONTAINER =
            "form[id=fboardlist] > section[id=bo_list] > ul.list-group > li.list-group-item > div.d-flex"
        private const val SELECTOR_LIST_CATEGORY_IMG = "div.wr-num > div.rcmd-box > span.orangered > img"
        private const val SELECTOR_LIST_INFO_ELEMENT = "div.flex-grow-1"
        private const val SELECTOR_LIST_LINK_A = "div.d-flex > div > a"
        private const val SELECTOR_LIST_PROFILE_LINK =
            "div.da-list-meta > div.d-flex > div.wr-name > span.sv_wrap > a.sv_member, div.da-list-meta > div.d-flex > div.wr-name" // 복합 선택자 주의
        private const val SELECTOR_LIST_NICKNAME_SPAN = "span.sv_name" // PROFILE_LINK 하위
        private const val SELECTOR_LIST_REPLY_COUNT_SPAN = "div.d-flex > div > a > span.count-plus" // INFO_ELEMENT 하위
        private const val SELECTOR_LIST_TIME_DIV = "div > div.d-flex > div.wr-date" // INFO_ELEMENT 하위
        private const val SELECTOR_LIST_NICK_IMAGE_IMG = "img.mb-photo" // PROFILE_LINK 하위
        private const val SELECTOR_LIST_HIT_DIV = "div > div.d-flex > div.wr-num.order-4" // INFO_ELEMENT 하위
        private const val SELECTOR_LIST_LIKE_DIV = "div > div.d-flex > div.wr-num.order-3" // INFO_ELEMENT 하위
        private const val SELECTOR_LIST_HAS_IMAGE_ICON = "div.d-flex > div > span.na-icon" // INFO_ELEMENT 하위
        private const val SELECTOR_VISUALLY_HIDDEN_SPANS = "span.visually-hidden, i.bi" // 제거 대상

        // Detail View Selectors
        private const val SELECTOR_DETAIL_ARTICLE_CONTAINER = "article[id=bo_v]"
        private const val SELECTOR_DETAIL_TITLE_H1 = "header > h1[id=bo_v_title]"
        private const val SELECTOR_DETAIL_TIME_PRIMARY_SPAN =
            "section[id=bo_v_info] > div.d-flex > div > span.orangered"
        private const val SELECTOR_DETAIL_TIME_SECONDARY_DIV_INDEX =
            1 // section[id=bo_v_info] > div.d-flex > div 의 두 번째 요소
        private const val SELECTOR_DETAIL_BODY_CONTENT_DIV = "section[id=bo_v_atc] > div[id=bo_v_con]"
        private const val SELECTOR_DETAIL_BODY_CLEANUP_TAGS = "input, button"
        private const val SELECTOR_DETAIL_HEADER_INFO_DIVS = "section[id=bo_v_info] > div.gap-1 > div.pe-2"
        private const val SELECTOR_DETAIL_HEADER_INFO_CLEANUP_TAGS = "i, span.visually-hidden"
        private const val SELECTOR_DETAIL_AUTHOR_IP_DIV = "section[id=bo_v_info] > div > div.me-auto"
        private const val SELECTOR_DETAIL_MEMBER_LINK_A =
            "section[id=bo_v_info] > div.d-flex > div.me-auto > div.d-flex > span.sv_wrap > a.sv_member"
        private const val SELECTOR_DETAIL_MEMBER_NICKNAME_SPAN = "span.sv_name" // MEMBER_LINK_A 하위
        private const val SELECTOR_DETAIL_MEMBER_NICK_IMAGE_IMG = "img.mb-photo" // MEMBER_LINK_A 하위

        // Comment Selectors
        private const val SELECTOR_COMMENT_ARTICLE_CONTAINER = "div[id=viewcomment] > section > article"
        private const val SELECTOR_COMMENT_NICK_LINK_A =
            "div.comment-list-wrap > header > div.d-flex > div.me-2 > span.d-inline-block > span.sv_wrap > a.sv_member"
        private const val SELECTOR_COMMENT_NICKNAME_SPAN = "span.sv_name" // NICK_LINK_A 하위
        private const val SELECTOR_COMMENT_IS_REPLY_ICON = "div.comment-list-wrap > header > div > div.me-2 > i.bi"
        private const val SELECTOR_COMMENT_IP_SPAN =
            "div.comment_info > div.comment_info_area > div.comment_ip > span.ip_address" // 사용되지 않음 (현재 코드 기준)
        private const val SELECTOR_COMMENT_TIME_SPAN = "div.ms-auto > span.orangered"
        private const val SELECTOR_COMMENT_NICK_IMAGE_IMG = "img.mb-photo" // NICK_LINK_A 하위
        private const val SELECTOR_COMMENT_LIKE_COUNT_SPAN = "div.comment-content > div > button > span"
        private const val SELECTOR_COMMENT_BODY_DIV = "div.comment-content > div.na-convert"
        private const val SELECTOR_COMMENT_BODY_CLEANUP_TAGS = "input, span.name, button"
    }

    override suspend fun list(
        html: String,
        boardCd: String,
        lastId: Long,
    ): List<ListItemDto> =
        Jsoup
            .parseBodyFragment(html, BuildConfig.DAMOANG_API_URL)
            .select(SELECTOR_LIST_ITEM_CONTAINER)
            .mapNotNull { element ->
                element.toListItemDto(boardCd, lastId)
            }

    private fun buildItemInfoString(nickName: String, time: String, hit: String): String {
        return buildString {
            append(nickName)
            if (time.isNotBlank()) {
                if (isNotEmpty()) append("ㆍ")
                append(time) // Use helper
            }
            if (hit.isNotBlank()) {
                if (isNotEmpty()) append("ㆍ")
                append("$hit 읽음")
            }
        }
    }

    override suspend fun detail(
        html: String,
        board: String,
        id: Long
    ): DetailDto = html.toDetailDtoInternal(board, id)

    private fun Element.toListItemDto(
        boardCd: String,
        lastId: Long,
    ): ListItemDto? {
        val category = attrOrEmpty(SELECTOR_LIST_CATEGORY_IMG, "alt")
        if (category in listOf("공지", "홍보", "추천")) return null

        val infoElement = this.selectFirst(SELECTOR_LIST_INFO_ELEMENT) ?: return null
        val linkElement = infoElement.selectFirst(SELECTOR_LIST_LINK_A) ?: return null
        val url = linkElement.attr("href")
            .trim()
        if (url.startsWith("/promotion")) return null

        val uri = url.toUri()
        val id = uri.lastPathSegment?.toLongOrNull() ?: return null

        if (id < 0 || (lastId > 0 && id >= lastId)) {
            Timber.d("[DamoangParser] SKIP ===> lastId=$lastId, id=$id")
            return null
        }

        val metaElement = this.selectFirst(SELECTOR_LIST_PROFILE_LINK) ?: return null
        val profileUrl = metaElement.attr("href")
            .trim()
        val nickName = metaElement.textOrEmpty(SELECTOR_LIST_NICKNAME_SPAN)
        val userId = profileUrl.toUri()
            .getQueryParameter("mb_id") ?: ""
        val nickImage = metaElement.attrOrEmpty(SELECTOR_LIST_NICK_IMAGE_IMG, "src")

        val reply = infoElement.textOrEmpty(SELECTOR_LIST_REPLY_COUNT_SPAN)
        val board = url.extractBoardFromUrl(boardCd)
        val title = linkElement.text()
        val time = infoElement.textOrEmpty(SELECTOR_LIST_TIME_DIV, SELECTOR_VISUALLY_HIDDEN_SPANS)
        val hit = infoElement.textOrEmpty(SELECTOR_LIST_HIT_DIV, SELECTOR_VISUALLY_HIDDEN_SPANS)
        val like = infoElement.textOrEmpty(SELECTOR_LIST_LIKE_DIV, SELECTOR_VISUALLY_HIDDEN_SPANS)
        val hasImage = infoElement.select(SELECTOR_LIST_HAS_IMAGE_ICON)
            .isNotEmpty()

        val info = buildItemInfoString(nickName, time.toTimeAgo(), hit)

        return ListItemDto(
            id = id,
            title = title,
            reply = reply,
            userInfo = UserInfo(
                id = userId,
                nickName = nickName,
                nickImage = nickImage
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

    private fun String.toDetailDtoInternal(originalBoard: String, originalId: Long): DetailDto {
        val document = Jsoup.parseBodyFragment(this, BuildConfig.DAMOANG_API_URL)
        val container = document.selectFirst(SELECTOR_DETAIL_ARTICLE_CONTAINER)
            ?: throw IllegalStateException("Detail article container not found for $originalBoard/$originalId")

        val title = container.textOrEmpty(SELECTOR_DETAIL_TITLE_H1)
        val time =
            container.select(SELECTOR_DETAIL_TIME_PRIMARY_SPAN.substringBeforeLast(" > div") + " > div") // 부모의 자식 div들
                .getOrNull(SELECTOR_DETAIL_TIME_SECONDARY_DIV_INDEX)
                .also {
                    it?.select(SELECTOR_VISUALLY_HIDDEN_SPANS)
                        ?.remove()
                }
                ?.text()
                ?.trim() ?: ""

        val bodyHtmlElement = container.selectFirst(SELECTOR_DETAIL_BODY_CONTENT_DIV)
        bodyHtmlElement?.select(SELECTOR_DETAIL_BODY_CLEANUP_TAGS)
            ?.remove()

        val bodyHtml = bodyHtmlElement?.html() ?: ""
        val headerElements = container.select(SELECTOR_DETAIL_HEADER_INFO_DIVS)
        headerElements.select(SELECTOR_DETAIL_HEADER_INFO_CLEANUP_TAGS)
            .remove() // 일괄 제거

        var viewCount = ""
        var likeCount = ""
        // headerElements 파싱 로직은 복잡하여 원본 유지, 필요시 개선
        // 이 부분은 웹사이트 구조에 매우 의존적이라 일반화하기 어려움
        when (headerElements.size) {
            2 -> {
                viewCount = headerElements.elementAtOrNull(0)
                    ?.text()
                    ?.trim() ?: ""
                likeCount = headerElements.elementAtOrNull(1)
                    ?.text()
                    ?.trim() ?: ""
            }

            3 -> {
                viewCount = headerElements.elementAtOrNull(0)
                    ?.text()
                    ?.trim()
                    ?.toIntOrNull()
                    ?.toString()
                    ?: headerElements.elementAtOrNull(1)
                        ?.text()
                        ?.trim() ?: ""
                likeCount = headerElements.elementAtOrNull(2)
                    ?.text()
                    ?.trim() ?: ""
            }

            4 -> {
                viewCount = headerElements.elementAtOrNull(1)
                    ?.text()
                    ?.trim() ?: ""
                likeCount = headerElements.elementAtOrNull(3)
                    ?.text()
                    ?.trim() ?: ""
            }
        }

        val authorIp = container.attrOrEmpty(SELECTOR_DETAIL_AUTHOR_IP_DIV, "data-bs-title")
        val memberElement = container.selectFirst(SELECTOR_DETAIL_MEMBER_LINK_A) // nullable
        val nickName = memberElement?.textOrEmpty(SELECTOR_DETAIL_MEMBER_NICKNAME_SPAN) ?: ""
        val nickImage = memberElement?.absUrlOrEmpty(SELECTOR_DETAIL_MEMBER_NICK_IMAGE_IMG, "src") ?: ""
        val userId = nickName // 원본 코드에서 memberElement 에서 userId를 가져오는 부분이 없었음. 필요시 추가

        val info = buildItemInfoString(nickName, time.toTimeAgo(), viewCount)

        var commentIndex = 0L
        val comments = container.select(SELECTOR_COMMENT_ARTICLE_CONTAINER)
            .mapNotNull { commentElement ->
                parseCommentElement(commentElement, commentIndex++)
            }

        return DetailDto(
            viewCount = viewCount,
            likeCount = likeCount,
            csrf = "",
            title = title,
            info = info,
            time = time,
            bodyHtml = bodyHtml,
            userInfo = UserInfo(
                id = userId,
                nickName = nickName,
                nickImage = nickImage,
                ip = authorIp,
            ),
            comments = comments
        )
    }

    private fun parseCommentElement(element: Element, index: Long): DetailComment? {
        val nickLinkElement = element.selectFirst(SELECTOR_COMMENT_NICK_LINK_A) ?: return null // 댓글 작성자 정보 없으면 skip
        val url = nickLinkElement.attr("href")
        val userId = url.toUri()
            .getQueryParameter("mb_id") ?: "-1"
        val nickName = nickLinkElement.textOrEmpty(SELECTOR_COMMENT_NICKNAME_SPAN)
        val nickImage = nickLinkElement.attrOrEmpty(SELECTOR_COMMENT_NICK_IMAGE_IMG, "src")
        val isReply = element.select(SELECTOR_COMMENT_IS_REPLY_ICON)
            .isNotEmpty()
        val time = element.textOrEmpty(SELECTOR_COMMENT_TIME_SPAN)
        val likeCount = element.textOrEmpty(SELECTOR_COMMENT_LIKE_COUNT_SPAN)
        val bodyElement = element.selectFirst(SELECTOR_COMMENT_BODY_DIV)
        bodyElement?.select(SELECTOR_COMMENT_BODY_CLEANUP_TAGS)
            ?.remove()
        val bodyHtml = "<p>${bodyElement?.html() ?: ""}</p>" // 원본처럼 <p> 태그로 감싸기
        val commentInfo = buildItemInfoString(nickName, time.toTimeAgo(), "") // 댓글은 조회수 없음

        return DetailComment(
            id = index, // 원본처럼 순차적 index 사용
            isReply = isReply,
            likeCount = likeCount,
            bodyHtml = bodyHtml,
            time = time, // 원본 시간
            info = commentInfo,
            userInfo = UserInfo(
                id = userId,
                nickName = nickName,
                nickImage = nickImage,
                nameMemo = "", // nameMemo 추출 로직 필요시 추가
                ip = "" // 댓글 작성자 IP 추출 로직 필요시 추가 (원본에는 없었음)
            ),
        )
    }

    private fun String.toTimeAgo(): String {
        try {
            val calendar: Calendar = Calendar.getInstance()
            val now = calendar.clone() as Calendar

            when {
                this.contains(" ") -> { // "년.월.일 시:분", "월.일 시:분", "어제 시:분"
                    val parts = this.split(" ")
                    val datePart = parts[0]
                    val timeParts = parts[1].split(":")
                    val hour = timeParts[0].toInt()
                    val minute = timeParts[1].toInt()

                    if (datePart == "어제") {
                        calendar.add(Calendar.DAY_OF_MONTH, -1)
                    } else {
                        val dateParts = datePart.split(".")
                        when (dateParts.size) {
                            3 -> calendar.set(
                                dateParts[0].toInt(),
                                dateParts[1].toInt() - 1,
                                dateParts[2].toInt()
                            ) // 년.월.일
                            2 -> calendar.set(
                                now.get(Calendar.YEAR),
                                dateParts[0].toInt() - 1,
                                dateParts[1].toInt()
                            ) // 월.일
                            else -> return this // 알 수 없는 날짜 형식
                        }
                    }
                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                    calendar.set(Calendar.MINUTE, minute)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                }

                this.contains(":") -> { // "시:분" (오늘)
                    val timeParts = this.split(":")
                    calendar.set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                    calendar.set(Calendar.MINUTE, timeParts[1].toInt())
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                }

                this == "어제" -> {
                    calendar.add(Calendar.DAY_OF_MONTH, -1)
                    // 시간은 현재 시간으로 유지하거나, 특정 시간으로 설정 (예: 00:00)
                    calendar.set(Calendar.HOUR_OF_DAY, 0) // 자정으로 설정 예시
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                }

                else -> return this // 알 수 없는 형식
            }
            return calendar.time.toTimeAgo()
        } catch (e: Exception) {
            e.printStackTrace()
            return this
        }
    }
}