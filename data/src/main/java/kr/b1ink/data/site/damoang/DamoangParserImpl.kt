package kr.b1ink.data.site.damoang

import kr.b1ink.data.BuildConfig
import kr.b1ink.data.site.base.BaseParser
import kr.b1ink.data.site.dto.DetailDto
import kr.b1ink.data.site.dto.ListItemDto
import kr.b1ink.data.util.toTimeAgo
import kr.b1ink.domain.model.DetailComment
import kr.b1ink.domain.model.UserInfo
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

/**
 * 다모앙 파서 (2026.03~ SvelteKit 리뉴얼 대응)
 *
 * 리뉴얼 후 HTML 구조:
 * - 리스트: a.post-row 기반
 * - 상세: div[data-slot="card"] + div.prose 기반
 * - 댓글: li.comment-item 기반
 */
class DamoangParserImpl @Inject constructor() : BaseParser {

    companion object {
        // ─── List Selectors (SvelteKit) ───
        private const val SEL_LIST_ROW = "a.post-row"
        private const val SEL_LIST_TITLE = "span.post-title"
        private const val SEL_LIST_COMMENT_COUNT = ".comment-count"

        private const val CDN_BASE_URL = "https://cdn.damoang.net/"

        // ─── Detail Selectors (SvelteKit) ───
        private const val SEL_DETAIL_TITLE = "div[data-slot=card-title]"
        private const val SEL_DETAIL_BODY = "#economy-post-content div.prose"
        private const val SEL_DETAIL_BODY_FALLBACK = "#economy-post-content"

        // ─── Comment Selectors (SvelteKit) ───
        private const val SEL_COMMENT_ITEM = "#comments li.comment-item"
        private const val SEL_COMMENT_BODY = "div.comment-body"
    }

    // ═══════════════════════════════════════════
    //  리스트 파싱
    // ═══════════════════════════════════════════

    override suspend fun list(
        html: String,
        boardCd: String,
        lastId: Long,
    ): List<ListItemDto> {
        // 1차: SvelteKit hydration 스크립트에서 포스트 데이터 추출
        val fromScript = extractPostsFromScript(html, boardCd, lastId)
        if (fromScript.isNotEmpty()) {
            Timber.d("[DamoangParser] list: ${fromScript.size} posts from script data")
            return fromScript
        }

        // 2차: HTML DOM 파싱 (fallback)
        Timber.d("[DamoangParser] list: falling back to DOM parsing")
        val document = Jsoup.parse(html, BuildConfig.DAMOANG_API_URL)
        return document.select(SEL_LIST_ROW).mapNotNull { row ->
            parseListRow(row, boardCd, lastId)
        }
    }

    private fun parseListRow(row: Element, boardCd: String, lastId: Long): ListItemDto? {
        val href = row.attr("href").trim()
        if (href.isBlank()) return null

        // URL에서 ID 추출: /free/5993858?page=96 → 5993858
        val pathPart = href.substringBefore("?")
        val id = pathPart.substringAfterLast("/").toLongOrNull() ?: return null

        if (id < 0 || (lastId > 0 && id >= lastId)) {
            Timber.d("[DamoangParser] SKIP ===> lastId=$lastId, id=$id")
            return null
        }

        // board 추출: /free/5993858 → free
        val board = pathPart.removePrefix("/").substringBefore("/").ifEmpty { boardCd }

        // 제목
        val title = row.selectFirst(SEL_LIST_TITLE)?.text()?.trim() ?: return null

        // 댓글 수: [4] → 4
        val reply = row.selectFirst(SEL_LIST_COMMENT_COUNT)?.text()
            ?.trim()
            ?.removeSurrounding("[", "]")
            ?.trim() ?: ""

        // 모바일 메타 정보 추출
        val mobileMeta = row.selectFirst("div.mobile-meta")
        val metaSpans = mobileMeta?.children()?.map { it.text().trim() } ?: emptyList()

        // metaSpans 예: ["♡ 3", "03.20", "949", "6미리"]
        // 첫번째: 좋아요 (♡ 포함), 두번째: 날짜, 세번째: 조회수, 네번째: 닉네임
        val like = metaSpans.getOrNull(0)?.replace(Regex("[^0-9]"), "")?.trim() ?: ""
        val time = metaSpans.getOrNull(1) ?: ""
        val hit = metaSpans.getOrNull(2) ?: ""
        val nickName = metaSpans.getOrNull(3) ?: ""

        // 프로필 이미지
        val nickImage = row.selectFirst("img.rounded-full")?.attr("src")?.trim() ?: ""

        val info = buildInfoString(nickName, time, hit)

        return ListItemDto(
            id = id,
            title = title,
            reply = reply,
            userInfo = UserInfo(
                id = nickName,
                nickName = nickName,
                nickImage = nickImage,
            ),
            info = info,
            category = "",
            time = time,
            url = pathPart,
            board = board,
            hasImage = false,
            like = like,
            hit = hit,
        )
    }

    // ═══════════════════════════════════════════
    //  리스트: SvelteKit hydration 스크립트 파싱
    // ═══════════════════════════════════════════

    /**
     * SvelteKit hydration 스크립트에서 posts 배열 추출.
     *
     * 스크립트 내부 형태:
     * ```js
     * b.postsData={posts:[{id:123,title:"...",author:"...",views:64,...}, ...]}
     * ```
     */
    private fun extractPostsFromScript(
        html: String,
        boardCd: String,
        lastId: Long,
    ): List<ListItemDto> {
        try {
            val marker = "posts:[{"
            val markerIdx = html.indexOf(marker)
            if (markerIdx == -1) return emptyList()

            // posts:[ 이후부터 매칭되는 ] 까지 추출
            val arrayStart = markerIdx + "posts:[".length - 1 // [ 위치
            val arrayEnd = findMatchingBracket(html, arrayStart)
            if (arrayEnd == -1) return emptyList()

            val arrayContent = html.substring(arrayStart + 1, arrayEnd) // [ ] 사이

            // 개별 객체 {…} 분리
            val objects = splitJsObjects(arrayContent)
            Timber.d("[DamoangParser] extractPostsFromScript: ${objects.size} objects")

            return objects.mapNotNull { parsePostFromJs(it, boardCd, lastId) }
        } catch (e: Exception) {
            Timber.e(e, "[DamoangParser] extractPostsFromScript failed")
            return emptyList()
        }
    }

    private fun parsePostFromJs(jsObj: String, boardCd: String, lastId: Long): ListItemDto? {
        try {
            val id = jsObj.jsLong("id") ?: return null
            if (id < 0 || (lastId > 0 && id >= lastId)) return null

            val title = jsObj.jsString("title").takeIf { it.isNotBlank() } ?: return null
            val author = jsObj.jsString("author")
            val authorId = jsObj.jsString("author_id")
            val authorImage = jsObj.jsString("author_image")
            val createdAt = jsObj.jsString("created_at")
            val views = jsObj.jsInt("views")
            val likes = jsObj.jsInt("likes")
            val commentsCount = jsObj.jsInt("comments_count")
            val hasImage = jsObj.jsBool("has_image")

            val time = createdAt.formatDateTime()

            // 프로필 이미지: 절대 URL이 아니면 CDN 프리픽스 추가
            val nickImage = when {
                authorImage.isBlank() -> ""
                authorImage.startsWith("http") -> authorImage
                else -> "$CDN_BASE_URL$authorImage"
            }

            val info = buildInfoString(author, time, views.toString())
            val reply = if (commentsCount > 0) commentsCount.toString() else ""

            return ListItemDto(
                id = id,
                title = title,
                reply = reply,
                userInfo = UserInfo(
                    id = authorId.ifBlank { author },
                    nickName = author,
                    nickImage = nickImage,
                ),
                info = info,
                category = "",
                time = time,
                url = "/$boardCd/$id",
                board = boardCd,
                hasImage = hasImage,
                like = if (likes > 0) likes.toString() else "",
                hit = views.toString(),
            )
        } catch (e: Exception) {
            Timber.e(e, "[DamoangParser] parsePostFromJs failed")
            return null
        }
    }

    // ═══════════════════════════════════════════
    //  상세 파싱
    // ═══════════════════════════════════════════

    override suspend fun detail(
        html: String,
        board: String,
        id: Long,
    ): DetailDto {
        // 1차: SvelteKit hydration 스크립트에서 포스트 데이터 추출
        val postJs = extractPostJsObject(html)
        if (postJs != null) {
            Timber.d("[DamoangParser] detail: using script data")
            return parseDetailFromJs(postJs, html, board)
        }

        // 2차: 기존 방식 (DOM + JSON fallback)
        Timber.d("[DamoangParser] detail: falling back to DOM/JSON parsing")
        return parseDetailFromDom(html, board)
    }

    /**
     * hydration 스크립트에서 `post:{...}` 객체 문자열 추출
     */
    private fun extractPostJsObject(html: String): String? {
        try {
            val marker = "post:{"
            val markerIdx = html.indexOf(marker)
            if (markerIdx == -1) return null

            val objStart = markerIdx + "post:".length // { 위치
            val objEnd = findMatchingBracket(html, objStart)
            if (objEnd == -1) return null

            return html.substring(objStart, objEnd + 1)
        } catch (e: Exception) {
            Timber.e(e, "[DamoangParser] extractPostJsObject failed")
            return null
        }
    }

    private fun parseDetailFromJs(postJs: String, html: String, board: String): DetailDto {
        val title = postJs.jsString("title")
        val bodyHtml = postJs.jsString("content")
        val nickName = postJs.jsString("author")
        val authorId = postJs.jsString("author_id")
        val authorImage = postJs.jsString("author_image")
        val viewCount = postJs.jsInt("views").toString()
        val likeCount = postJs.jsInt("likes").toString()
        val createdAt = postJs.jsString("created_at")
        val time = createdAt.formatDateTime()

        val nickImage = when {
            authorImage.isBlank() -> ""
            authorImage.startsWith("http") -> authorImage
            else -> "$CDN_BASE_URL$authorImage"
        }

        val info = buildInfoString(nickName, time, viewCount)

        // 댓글: hydration 스크립트에서 추출
        val comments = extractCommentsFromScript(html)

        return DetailDto(
            title = title,
            info = info,
            time = time,
            viewCount = viewCount,
            likeCount = likeCount,
            bodyHtml = bodyHtml,
            csrf = "",
            userInfo = UserInfo(
                id = authorId.ifBlank { nickName },
                nickName = nickName,
                nickImage = nickImage,
            ),
            comments = comments,
        )
    }

    /**
     * hydration 스크립트에서 `comments:{items:[...]}` 추출
     */
    private fun extractCommentsFromScript(html: String): List<DetailComment> {
        try {
            val marker = "comments:{items:[{"
            val markerIdx = html.indexOf(marker)
            if (markerIdx == -1) return emptyList()

            val arrayStart = markerIdx + "comments:{items:".length // [ 위치
            val arrayEnd = findMatchingBracket(html, arrayStart)
            if (arrayEnd == -1) return emptyList()

            val arrayContent = html.substring(arrayStart + 1, arrayEnd)
            val objects = splitJsObjects(arrayContent)

            return objects.mapNotNull { parseCommentFromJs(it) }
        } catch (e: Exception) {
            Timber.e(e, "[DamoangParser] extractCommentsFromScript failed")
            return emptyList()
        }
    }

    private fun parseCommentFromJs(jsObj: String): DetailComment? {
        try {
            val commentId = jsObj.jsLong("id") ?: return null
            val content = jsObj.jsString("content")
            val author = jsObj.jsString("author")
            val authorId = jsObj.jsString("author_id")
            val authorIp = jsObj.jsString("author_ip")
            val authorImage = jsObj.jsString("author_image")
            val depth = jsObj.jsInt("depth")
            val likes = jsObj.jsInt("likes")
            val createdAt = jsObj.jsString("created_at").formatDateTime()

            val nickImage = when {
                authorImage.isBlank() -> ""
                authorImage.startsWith("http") -> authorImage
                else -> "$CDN_BASE_URL$authorImage"
            }

            val commentInfo = buildInfoString(author, createdAt, "")

            return DetailComment(
                id = commentId,
                isReply = depth > 0,
                likeCount = if (likes > 0) likes.toString() else "",
                bodyHtml = content.ifBlank { "<p></p>" },
                time = createdAt,
                info = commentInfo,
                userInfo = UserInfo(
                    id = authorId,
                    nickName = author,
                    nickImage = nickImage,
                    ip = authorIp,
                ),
            )
        } catch (e: Exception) {
            Timber.e(e, "[DamoangParser] parseCommentFromJs failed")
            return null
        }
    }

    /**
     * DOM + JSON fallback (기존 방식)
     */
    private fun parseDetailFromDom(html: String, board: String): DetailDto {
        val document = Jsoup.parse(html, BuildConfig.DAMOANG_API_URL)
        val svelteData = extractSvelteKitData(html)

        val title = svelteData?.optString("title")?.takeIf { it.isNotBlank() }
            ?: document.selectFirst(SEL_DETAIL_TITLE)?.text()?.trim() ?: ""

        var bodyHtml = svelteData?.optString("content") ?: ""
        if (bodyHtml.isBlank()) {
            val bodyElement = document.selectFirst(SEL_DETAIL_BODY)
                ?: document.selectFirst(SEL_DETAIL_BODY_FALLBACK)
            bodyElement?.select("input, button, script")?.remove()
            bodyHtml = bodyElement?.html() ?: ""
        }

        val nickName = svelteData?.optString("author") ?: ""
        val authorId = svelteData?.optString("author_id") ?: ""
        val nickImage = svelteData?.optString("author_profile_image") ?: ""
        val viewCount = svelteData?.optInt("views", 0)?.toString() ?: ""
        val likeCount = svelteData?.optInt("likes", 0)?.toString() ?: ""
        val time = svelteData?.optString("created_at")?.formatDateTime() ?: ""

        val resolvedNickName = nickName.ifBlank {
            document.selectFirst("div[data-slot=card-header]")
                ?.selectFirst("button[data-dropdown-menu-trigger]")?.text()?.trim() ?: ""
        }

        val info = buildInfoString(resolvedNickName, time, viewCount)

        val comments = parseCommentsFromJson(svelteData).ifEmpty {
            var commentIndex = 0L
            document.select(SEL_COMMENT_ITEM).mapNotNull { li ->
                parseComment(li, commentIndex++)
            }
        }

        return DetailDto(
            title = title,
            info = info,
            time = time,
            viewCount = viewCount,
            likeCount = likeCount,
            bodyHtml = bodyHtml,
            csrf = "",
            userInfo = UserInfo(
                id = authorId.ifBlank { resolvedNickName },
                nickName = resolvedNickName,
                nickImage = nickImage,
            ),
            comments = comments,
        )
    }

    // ═══════════════════════════════════════════
    //  SvelteKit 데이터 추출
    // ═══════════════════════════════════════════

    /**
     * SvelteKit의 <script> 태그에서 JSON 데이터를 추출.
     * 패턴: __sveltekit_xxx.resolve({...}) 또는 data-sveltekit-fetched
     */
    private fun extractSvelteKitData(html: String): JSONObject? {
        try {
            // 방법 1: data-sveltekit-fetched 스크립트에서 JSON 추출
            val fetchedPattern = Regex("""<script[^>]*data-sveltekit-fetched[^>]*>(.*?)</script>""", RegexOption.DOT_MATCHES_ALL)
            for (match in fetchedPattern.findAll(html)) {
                val json = match.groupValues[1].trim()
                if (json.isNotBlank()) {
                    try {
                        val obj = JSONObject(json)
                        val body = obj.optJSONObject("body") ?: obj
                        // post 데이터가 있는 경우
                        body.optJSONObject("post")?.let { return it }
                        if (body.has("content")) return body
                    } catch (_: Exception) {}
                }
            }

            // 방법 2: inline script에서 SvelteKit resolve 데이터 추출
            // __sveltekit_xxx = { ... } 또는 resolve({...}) 형태
            val resolvePattern = Regex("""resolve\(\s*(\{.+?\})\s*\)""", RegexOption.DOT_MATCHES_ALL)
            for (match in resolvePattern.findAll(html)) {
                val jsonStr = match.groupValues[1]
                try {
                    val obj = JSONObject(jsonStr)
                    // data 배열 안에 post 정보가 있을 수 있음
                    obj.optJSONArray("data")?.let { dataArr ->
                        for (i in 0 until dataArr.length()) {
                            val item = dataArr.optJSONObject(i)
                            item?.optJSONObject("post")?.let { return it }
                            if (item?.has("content") == true) return item
                        }
                    }
                } catch (_: Exception) {}
            }

            // 방법 3: JSON 내의 "content" 필드를 직접 추출 (최후의 수단)
            val contentPattern = Regex(""""content"\s*:\s*"((?:[^"\\]|\\.)*)"""")
            val contentMatch = contentPattern.find(html)
            if (contentMatch != null) {
                val content = contentMatch.groupValues[1]
                    .replace("\\u003C", "<")
                    .replace("\\u003E", ">")
                    .replace("\\u0026", "&")
                    .replace("\\\"", "\"")
                    .replace("\\/", "/")
                    .replace("\\n", "\n")
                if (content.contains("<") && content.length > 10) {
                    // title도 추출 시도
                    val titlePattern = Regex(""""title"\s*:\s*"((?:[^"\\]|\\.)*)"""")
                    val titleMatch = titlePattern.find(html)
                    val title = titleMatch?.groupValues?.get(1)
                        ?.replace("\\u003C", "<")
                        ?.replace("\\u003E", ">")
                        ?.replace("\\\"", "\"") ?: ""

                    val authorPattern = Regex(""""author"\s*:\s*"((?:[^"\\]|\\.)*)"""")
                    val author = authorPattern.find(html)?.groupValues?.get(1) ?: ""

                    val viewsPattern = Regex(""""views"\s*:\s*(\d+)""")
                    val views = viewsPattern.find(html)?.groupValues?.get(1) ?: "0"

                    val likesPattern = Regex(""""likes"\s*:\s*(\d+)""")
                    val likes = likesPattern.find(html)?.groupValues?.get(1) ?: "0"

                    val createdPattern = Regex(""""created_at"\s*:\s*"((?:[^"\\]|\\.)*)"""")
                    val createdAt = createdPattern.find(html)?.groupValues?.get(1) ?: ""

                    return JSONObject().apply {
                        put("content", content)
                        put("title", title)
                        put("author", author)
                        put("views", views.toIntOrNull() ?: 0)
                        put("likes", likes.toIntOrNull() ?: 0)
                        put("created_at", createdAt)
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "[DamoangParser] Failed to extract SvelteKit data")
        }
        return null
    }

    /**
     * SvelteKit JSON 데이터에서 댓글 추출
     */
    private fun parseCommentsFromJson(svelteData: JSONObject?): List<DetailComment> {
        svelteData ?: return emptyList()
        try {
            // comments가 같은 script에 있을 수도 있고 별도일 수도 있음
            val commentsObj = svelteData.optJSONObject("comments") ?: return emptyList()
            val items = commentsObj.optJSONArray("items") ?: return emptyList()

            return (0 until items.length()).mapNotNull { i ->
                val item = items.optJSONObject(i) ?: return@mapNotNull null
                val commentId = item.optLong("id", i.toLong())
                val content = item.optString("content")
                    .replace("\\u003C", "<")
                    .replace("\\u003E", ">")
                val author = item.optString("author")
                val authorId = item.optString("author_id")
                val authorIp = item.optString("author_ip")
                val depth = item.optInt("depth", 0)
                val likes = item.optInt("likes", 0)
                val createdAt = item.optString("created_at").formatDateTime()

                val commentInfo = buildInfoString(author, createdAt, "")

                DetailComment(
                    id = commentId,
                    isReply = depth > 0,
                    likeCount = if (likes > 0) likes.toString() else "",
                    bodyHtml = content.ifBlank { "<p></p>" },
                    time = createdAt,
                    info = commentInfo,
                    userInfo = UserInfo(
                        id = authorId,
                        nickName = author,
                        nickImage = "",
                        ip = authorIp,
                    ),
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "[DamoangParser] Failed to parse comments from JSON")
            return emptyList()
        }
    }

    /**
     * ISO 8601 날짜를 상대 시간 형식("3분 전", "2시간 전" 등)으로 변환
     * "2026-03-20T12:13:21+09:00" → "5분 전"
     */
    private fun String.formatDateTime(): String {
        if (isBlank()) return ""
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.KOREA)
            sdf.timeZone = TimeZone.getTimeZone("Asia/Seoul")
            val date = sdf.parse(substringBefore("+").substringBefore("Z"))
            date!!.toTimeAgo()
        } catch (_: Exception) {
            substringBefore("T").removePrefix("2026-").replace("-", ".")
        }
    }

    // ═══════════════════════════════════════════
    //  댓글 파싱
    // ═══════════════════════════════════════════

    private fun parseComment(li: Element, index: Long): DetailComment {
        // 댓글 ID: id="c_5993868" → 5993868
        val commentId = li.attr("id").removePrefix("c_").toLongOrNull() ?: index

        // 대댓글 여부: margin-left 값으로 판단
        val marginLeft = li.attr("style")
            .let { Regex("margin-left:\\s*([\\d.]+)rem").find(it)?.groupValues?.get(1)?.toFloatOrNull() ?: 0f }
        val isReply = marginLeft > 0f

        // 작성자 정보
        val nickName = li.selectFirst("button[data-dropdown-menu-trigger]")?.text()?.trim()
            ?: li.selectFirst("p.font-medium")?.text()?.trim() ?: ""
        val nickImage = li.selectFirst("img.rounded-full")?.attr("src")?.trim() ?: ""

        // 시간 + IP
        val timeIpElement = li.select("p.text-muted-foreground").lastOrNull()
        val timeIpText = timeIpElement?.text()?.trim() ?: ""
        val timeParts = timeIpText.split("·").map { it.trim() }
        val time = timeParts.getOrNull(0) ?: ""
        val ip = timeParts.getOrNull(1) ?: ""

        // 좋아요
        val likeCount = li.selectFirst("div.comment-good-group button span")?.text()?.trim()
            ?.replace(Regex("[^0-9]"), "") ?: ""

        // 본문
        val bodyElement = li.selectFirst(SEL_COMMENT_BODY)
        bodyElement?.select("input, button, script")?.remove()
        val bodyHtml = "<p>${bodyElement?.html() ?: ""}</p>"

        val commentInfo = buildInfoString(nickName, time, "")

        return DetailComment(
            id = commentId,
            isReply = isReply,
            likeCount = likeCount,
            bodyHtml = bodyHtml,
            time = time,
            info = commentInfo,
            userInfo = UserInfo(
                id = nickName,
                nickName = nickName,
                nickImage = nickImage,
                ip = ip,
            ),
        )
    }

    // ═══════════════════════════════════════════
    //  유틸리티
    // ═══════════════════════════════════════════

    /**
     * '[' 또는 '{' 에 대응하는 닫는 괄호 위치를 반환.
     * 문자열 내부의 괄호는 무시.
     */
    private fun findMatchingBracket(s: String, openIdx: Int): Int {
        val open = s[openIdx]
        val close = if (open == '[') ']' else '}'
        var depth = 0
        var inStr = false
        var esc = false
        for (i in openIdx until minOf(s.length, openIdx + 500_000)) {
            if (esc) { esc = false; continue }
            val c = s[i]
            if (c == '\\' && inStr) { esc = true; continue }
            if (c == '"') { inStr = !inStr; continue }
            if (inStr) continue
            if (c == open) depth++
            if (c == close) { depth--; if (depth == 0) return i }
        }
        return -1
    }

    /** JS 배열 내용을 개별 `{…}` 객체 문자열로 분리 */
    private fun splitJsObjects(arrayContent: String): List<String> {
        val objects = mutableListOf<String>()
        var depth = 0
        var objStart = -1
        var inStr = false
        var esc = false
        for (i in arrayContent.indices) {
            if (esc) { esc = false; continue }
            val c = arrayContent[i]
            if (c == '\\' && inStr) { esc = true; continue }
            if (c == '"') { inStr = !inStr; continue }
            if (inStr) continue
            when (c) {
                '{' -> { if (depth == 0) objStart = i; depth++ }
                '}' -> {
                    depth--
                    if (depth == 0 && objStart >= 0) {
                        objects.add(arrayContent.substring(objStart, i + 1))
                        objStart = -1
                    }
                }
            }
        }
        return objects
    }

    // ─── JS 객체 리터럴 필드 추출 ───

    /** `{...,field:"value",...}` 에서 문자열 값 추출 */
    private fun String.jsString(field: String): String {
        val pattern = Regex("""[\{,]${Regex.escape(field)}:"((?:[^"\\]|\\.)*)"""")
        return pattern.find(this)?.groupValues?.get(1)
            ?.replace("\\\"", "\"")
            ?.replace("\\/", "/")
            ?.replace("\\n", "\n")
            ?.replace("\\u003C", "<")
            ?.replace("\\u003E", ">")
            ?.replace("\\u0026", "&")
            ?: ""
    }

    /** `{...,field:123,...}` 에서 Long 값 추출 */
    private fun String.jsLong(field: String): Long? {
        val pattern = Regex("""[\{,]${Regex.escape(field)}:(\d+)""")
        return pattern.find(this)?.groupValues?.get(1)?.toLongOrNull()
    }

    /** `{...,field:123,...}` 에서 Int 값 추출 */
    private fun String.jsInt(field: String): Int {
        val pattern = Regex("""[\{,]${Regex.escape(field)}:(\d+)""")
        return pattern.find(this)?.groupValues?.get(1)?.toIntOrNull() ?: 0
    }

    /** `{...,field:true,...}` 에서 Boolean 값 추출 */
    private fun String.jsBool(field: String): Boolean {
        val pattern = Regex("""[\{,]${Regex.escape(field)}:(true|false)""")
        return pattern.find(this)?.groupValues?.get(1) == "true"
    }

    private fun buildInfoString(nickName: String, time: String, hit: String): String {
        return buildString {
            append(nickName)
            if (time.isNotBlank()) {
                if (isNotEmpty()) append("ㆍ")
                append(time)
            }
            if (hit.isNotBlank()) {
                if (isNotEmpty()) append("ㆍ")
                append("$hit 읽음")
            }
        }
    }
}
