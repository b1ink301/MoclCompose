package kr.b1ink.data.site.meeco

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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class MeecoParserImpl @Inject constructor() : BaseParser {
    companion object {
        // List Item Selectors
        private const val SELECTOR_LIST_ITEM_CONTAINER =
            "div.wrap > section[id=container] > div > section.ctt > section.neon_board > div[id=list_swipe_area] > div.list_ctt > div.list_document > div.list_d > ul > li"
        private const val SELECTOR_LIST_CATEGORY_TEXT = "span.hot_text, span.notice_text" // 공지, 핫글 확인용
        private const val SELECTOR_LIST_LINK_A = "a.list_link"
        private const val SELECTOR_LIST_INFO_DIV = "div.list_info"
        private const val SELECTOR_LIST_INFO_CHILD_DIVS = "div" // INFO_DIV 하위의 정보 div들
        private const val SELECTOR_LIST_INFO_NICKNAME_CHECK_SPAN = "span" // 첫 번째 info div의 span 존재 여부로 닉네임 유무 판단
        private const val SELECTOR_LIST_INFO_NICKNAME_DIV_FIRST =
            "div:first-child" // 닉네임 (infoElements.size == 4 && isNickName)
        private const val SELECTOR_LIST_INFO_TIME_DIV_FIRST_NO_NICK =
            "div:first-child" // 시간 (infoElements.size == 4 && !isNickName)
        private const val SELECTOR_LIST_INFO_TIME_DIV_SECOND_WITH_NICK =
            "div:nth-child(2)" // 시간 (infoElements.size == 4 && isNickName) or (infoElements.size == 5)
        private const val SELECTOR_LIST_INFO_HIT_DIV_SECOND_NO_NICK =
            "div:nth-child(2)" // 조회수 (infoElements.size == 4 && !isNickName)
        private const val SELECTOR_LIST_INFO_HIT_DIV_THIRD_WITH_NICK =
            "div:nth-child(3)" // 조회수 (infoElements.size == 4 && isNickName) or (infoElements.size == 5)
        private const val SELECTOR_LIST_VOTE_DIV = "div.list_vote"
        private const val SELECTOR_LIST_REPLY_A = "a.list_cmt"

        // Detail View Selectors
        private const val SELECTOR_DETAIL_ARTICLE_CONTAINER = "article.atc"
        private const val SELECTOR_DETAIL_HEADER_INFO_DIV = "header.atc_hd > div.atc_info"
        private const val SELECTOR_DETAIL_TITLE_A = "header.atc_hd > h1 > a"
        private const val SELECTOR_DETAIL_TIME_LI_FIRST = "ul > li:first-of-type" // HEADER_INFO_DIV 하위
        private const val SELECTOR_DETAIL_VIEW_COUNT_LI_SECOND = "ul > li:nth-of-type(2)" // HEADER_INFO_DIV 하위
        private const val SELECTOR_DETAIL_NICKNAME_SPAN = "span.nickname" // HEADER_INFO_DIV 하위
        private const val SELECTOR_DETAIL_NICKNAME_SPAN_LOGIN = "span.nickname > a.bt_member_info" // HEADER_INFO_DIV 하위
        private const val SELECTOR_DETAIL_NICK_IMAGE_IMG = "span.pf > img.pf_img" // HEADER_INFO_DIV 하위
        private const val SELECTOR_DETAIL_BODY_DIV = "div.atc_body"
        private const val SELECTOR_DETAIL_BODY_CLEANUP_TAGS = "input, button"

        // Comment Selectors
        private const val SELECTOR_COMMENT_ARTICLE_CONTAINER = "div.cmt > div.cmt_list_parent > div.cmt_list > article"
        private const val SELECTOR_COMMENT_PROFILE_SPAN =
            "div > span.pf > img.pf_img"
        private const val SELECTOR_COMMENT_TIME_SPAN = "span.date"
        private const val SELECTOR_COMMENT_LIKE_COUNT_B = "div.cmt_vote > span.cmt_vote_up > b.num"
        private const val SELECTOR_COMMENT_BODY_DIV = "div.xe_content"
        private const val SELECTOR_COMMENT_BODY_CLEANUP_TAGS = "input, span.name, button"
    }

    override suspend fun list(
        html: String,
        boardCd: String,
        lastId: Long,
    ) =
        Jsoup
            .parseBodyFragment(html, BuildConfig.MEECO_API_URL)
            .select(SELECTOR_LIST_ITEM_CONTAINER)
            .mapNotNull { element ->
                element.toListItemDto(boardCd, lastId)
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
        val category = this.textOrEmpty(SELECTOR_LIST_CATEGORY_TEXT)
        if (category == "공지" || category == "핫글") {
            return null
        }

        val linkElement = this.selectFirst(SELECTOR_LIST_LINK_A) ?: return null
        val url = linkElement.absUrl("href")
        val uri = url.toUri()
        val idString = uri.lastPathSegment ?: return null
        val id = idString.toLongOrNull() ?: return null

        if (id < 0 || (lastId > 0 && id >= lastId)) {
            Timber.d("[MeecoParser] SKIP ===> lastId=$lastId, id=$id")
            return null
        }

        val board = url.extractBoardFromUrl(boardCd)
        val title = linkElement.attr("title")
            .trim()
        val infoElement = this.selectFirst(SELECTOR_LIST_INFO_DIV) ?: return null
        val infoElements = infoElement.select(SELECTOR_LIST_INFO_CHILD_DIVS)

        var time = ""
        var hit = ""
        var nickName = ""

        // list_info 내부 div 개수에 따른 파싱 (기존 로직 유지, 복잡도 있음)
        when (infoElements.size) {
            4 -> {
                val isNickNamePresent = infoElements.firstOrNull()
                    ?.select(SELECTOR_LIST_INFO_NICKNAME_CHECK_SPAN)
                    ?.isNotEmpty() ?: false
                if (isNickNamePresent) {
                    nickName = infoElement.textOrEmpty(SELECTOR_LIST_INFO_NICKNAME_DIV_FIRST)
                    time = infoElement.textOrEmpty(SELECTOR_LIST_INFO_TIME_DIV_SECOND_WITH_NICK)
                    hit = infoElement.textOrEmpty(SELECTOR_LIST_INFO_HIT_DIV_THIRD_WITH_NICK)
                } else {
                    time = infoElement.textOrEmpty(SELECTOR_LIST_INFO_TIME_DIV_FIRST_NO_NICK)
                    hit = infoElement.textOrEmpty(SELECTOR_LIST_INFO_HIT_DIV_SECOND_NO_NICK)
                }
            }

            5 -> { // 닉네임, 시간, 조회수, (아마도 추천수, 댓글수 중 하나)
                nickName = infoElement.textOrEmpty(SELECTOR_LIST_INFO_NICKNAME_DIV_FIRST) // 첫번째가 닉네임으로 가정
                time = infoElement.textOrEmpty(SELECTOR_LIST_INFO_TIME_DIV_SECOND_WITH_NICK)
                hit = infoElement.textOrEmpty(SELECTOR_LIST_INFO_HIT_DIV_THIRD_WITH_NICK)
            }
        }
        val like = infoElement.textOrEmpty(SELECTOR_LIST_VOTE_DIV)
        val reply = this.textOrEmpty(SELECTOR_LIST_REPLY_A)
        val nickImage = "" // Meeco 목록에서는 프로필 이미지를 가져오지 않는 것으로 보임
        val userId = nickName // ID를 닉네임으로 사용 (고유성 보장 안될 수 있음)
        val hasImage = false // 이미지 유무 판단 로직은 원본에 없었음. 필요시 추가

        val itemInfo = buildItemInfoString(nickName, time.toTimeAgo(), hit)

        return ListItemDto(
            id = id,
            title = title,
            reply = reply,
            userInfo = UserInfo(
                id = userId,
                nickName = nickName,
                nickImage = nickImage,
            ),
            info = itemInfo,
            category = category,
            time = time, // 원본 시간
            url = url,
            board = board,
            hasImage = hasImage,
            like = like,
            hit = hit,
        )
    }

    private fun parseDateTime(dateTimeString: String): Date {
        val calendar = Calendar.getInstance()
        val now = calendar.time

        return when {
            dateTimeString.contains(' ') -> {
                // "년.월.일 시:분" 또는 "월.일 시:분" 형식
                val parts = dateTimeString.split(' ')
                val dateParts = parts[0].split('.')
                val timeParts = parts[1].split(':')

                val year: Int
                val month: Int
                val day: Int

                when (dateParts.size) {
                    3, 4 -> { // "년.월.일"
                        year = dateParts[0].toInt()
                        month = dateParts[1].toInt() - 1 // Calendar의 월은 0부터 시작
                        day = dateParts[2].toInt()
                    }

                    2 -> { // "월.일"
                        year = calendar.get(Calendar.YEAR)
                        month = dateParts[0].toInt() - 1 // Calendar의 월은 0부터 시작
                        day = dateParts[1].toInt()
                    }

                    else -> throw Exception("Error parsing date part: $dateTimeString")
                }

                val hour = timeParts[0].toInt()
                val minute = timeParts[1].toInt()

                calendar.set(year, month, day, hour, minute, 0)
                calendar.time
            }

            dateTimeString.contains(':') -> {
                // "시:분" 형식
                val timeParts = dateTimeString.split(':')
                calendar.time = now // 현재 날짜로 설정
                calendar.set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                calendar.set(Calendar.MINUTE, timeParts[1].toInt())
                calendar.set(Calendar.SECOND, 0)
                calendar.time
            }

            dateTimeString == "어제" -> {
                calendar.time = now
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                calendar.time
            }

            else -> {
                // 다른 간단한 날짜 형식 (예: "yyyy-MM-dd", "MM/dd/yyyy")을 시도해볼 수 있습니다.
                // 여기서는 몇 가지 일반적인 형식을 시도합니다.
                // 필요에 따라 더 많은 형식을 추가하거나 라이브러리(예: Joda-Time 또는 ThreeTenABP) 사용을 고려하세요.
                val supportedFormats = listOf(
                    SimpleDateFormat("yy.MM.dd.", Locale.getDefault()),
                    SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()),
                    SimpleDateFormat("MM.dd", Locale.getDefault()),
                    // 필요한 다른 형식 추가
                )
                for (format in supportedFormats) {
                    try {
                        format.isLenient = false // 엄격한 파싱
                        return format.parse(dateTimeString)!!
                    } catch (_: Exception) {
                    }
                }
                throw Exception("Error parsing date string: $dateTimeString")
            }
        }
    }

    private fun String.toTimeAgo(): String = try {
        if (endsWith(" 전")) {
            this
        } else {
            parseDateTime(this).toTimeAgo()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        this
    }

    fun String.toDetailDtoInternal(originalBoard: String, originalId: Long): DetailDto {
        val document = Jsoup.parseBodyFragment(this, BuildConfig.MEECO_API_URL)
        val container = document.selectFirst(SELECTOR_DETAIL_ARTICLE_CONTAINER)
            ?: throw IllegalStateException("Detail article container not found for $originalBoard/$originalId")

        val headerInfoDiv = container.selectFirst(SELECTOR_DETAIL_HEADER_INFO_DIV)
            ?: throw IllegalStateException("Detail header info not found")

        val csrf = "" // CSRF 토큰 추출 로직은 원본에 없었음
        val title = container.textOrEmpty(SELECTOR_DETAIL_TITLE_A)
        val time = headerInfoDiv.textOrEmpty(SELECTOR_DETAIL_TIME_LI_FIRST)

        val bodyElement = container.selectFirst(SELECTOR_DETAIL_BODY_DIV)
        bodyElement?.select(SELECTOR_DETAIL_BODY_CLEANUP_TAGS)
            ?.remove()
        val bodyHtml = (bodyElement?.html() ?: "").replace("//img.", "https://img.")

        val viewCount = headerInfoDiv.textOrEmpty(SELECTOR_DETAIL_VIEW_COUNT_LI_SECOND)
//        val nickName = headerInfoDiv.textOrEmpty(SELECTOR_DETAIL_NICKNAME_SPAN)
        val nickNameElement = headerInfoDiv.selectFirst(SELECTOR_DETAIL_NICKNAME_SPAN)
        val nickNameLoginElement = headerInfoDiv.selectFirst(SELECTOR_DETAIL_NICKNAME_SPAN_LOGIN) ?: nickNameElement
        val nickName = nickNameLoginElement?.text()
            ?.trim() ?: ""
        val nickImage = headerInfoDiv.absUrlOrEmpty(SELECTOR_DETAIL_NICK_IMAGE_IMG, "src")
        val likeCount = "" // 상세 페이지에서 좋아요 수 추출 로직 원본에 없음

        val authorIp = "" // 상세 페이지에서 IP 추출 로직 원본에 없음
        val userId = nickName // ID를 닉네임으로 사용

        var commentIndex = 0L
        val comments = container.select(SELECTOR_COMMENT_ARTICLE_CONTAINER)
            .mapNotNull { commentElement ->
                parseCommentElement(commentElement, commentIndex++)
            }

        val detailInfo = buildItemInfoString(nickName, time.toTimeAgo(), viewCount)

        return DetailDto(
            viewCount = viewCount,
            likeCount = likeCount,
            csrf = csrf,
            title = title,
            time = time, // 원본 시간
            info = detailInfo,
            userInfo = UserInfo(
                id = userId,
                nickName = nickName,
                nickImage = nickImage,
                ip = authorIp,
            ),
            bodyHtml = bodyHtml,
            comments = comments,
        )
    }

    private fun buildItemInfoString(nickName: String, time: String, count: String): String = buildString {
        if (nickName.isNotBlank()) {
            append(nickName)
        }
        if (time.isNotBlank()) {
            if (isNotEmpty()) append("ㆍ")
            append(time)
        }
        if (count.isNotBlank()) {
            if (isNotEmpty()) append("ㆍ")
            append("$count 읽음")
        }
    }

    private fun parseCommentElement(element: Element, index: Long): DetailComment? {
        val headerElement = element.selectFirst("header.cmt_hd") ?: return null // 댓글 헤더 없으면 파싱 불가
        val isReply = headerElement.attr("class")
            .contains("replay")

        // 프로필 span에서 이미지와 닉네임 추출
        val nickImage = headerElement.attrOrEmpty(SELECTOR_COMMENT_PROFILE_SPAN, "abs:src")
        val nickName = headerElement.firstOrNull {
            it.tagName()
                .startsWith("spanclass")
        }
            ?.ownText() ?: ""

        val userId = nickName // ID를 닉네임으로 사용

        // val ip = "" // 댓글 IP 추출 로직 원본에 없음
        val time = element.textOrEmpty(SELECTOR_COMMENT_TIME_SPAN)
        val likeCount = element.textOrEmpty(SELECTOR_COMMENT_LIKE_COUNT_B)

        val bodyElement = element.selectFirst(SELECTOR_COMMENT_BODY_DIV)
        bodyElement?.select(SELECTOR_COMMENT_BODY_CLEANUP_TAGS)
            ?.remove()
        // 이미지 경로 수정: "//img." -> "https://img."
        val bodyHtml = (bodyElement?.html() ?: "").replace("//img.", "https://img.")

        val info = buildItemInfoString(nickName, time.toTimeAgo(), "") // 댓글은 조회수 없음, 상세 보기 플래그 사용

        return DetailComment(
            id = index,
            isReply = isReply,
            bodyHtml = bodyHtml,
            likeCount = likeCount,
            time = time, // 원본 시간
            info = info,
            userInfo = UserInfo(
                id = userId,
                nickName = nickName,
                nickImage = nickImage,
                nameMemo = "", // nameMemo 추출 로직 필요시 추가
                ip = "" // 댓글 IP 추출 로직 필요시 추가
            ),
        )
    }
}