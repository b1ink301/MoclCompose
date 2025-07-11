package kr.b1ink.data.site.base

import kr.b1ink.data.site.dto.DetailDto
import kr.b1ink.data.site.dto.ListItemDto
import org.jsoup.nodes.Element

interface BaseParser {
    suspend fun list(html: String, boardCd: String, lastId: Long = -1): List<ListItemDto>
    suspend fun detail(html: String, board: String, id: Long): DetailDto
}

fun Element.textOrEmpty(selector: String): String =
    this.selectFirst(selector)
        ?.text()
        ?.trim() ?: ""

fun Element.attrOrEmpty(selector: String, attrName: String): String =
    this.selectFirst(selector)
        ?.attr(attrName)
        ?.trim() ?: ""

fun Element.absUrlOrEmpty(selector: String, attributeKey: String = "href"): String =
    this.selectFirst(selector)
        ?.absUrl(attributeKey)
        ?.trim() ?: ""

fun Element.textOrEmpty(selector: String, cleanUpSelectors: String? = null): String {
    val selected = this.selectFirst(selector) ?: return ""
    cleanUpSelectors?.let {
        selected.select(it)
            .remove()
    }
    return selected.text()
        .trim()
}

fun String.extractBoardFromUrl(defaultBoard: String): String {
    return try {
        val end = lastIndexOf("/")
        if (end == -1) return defaultBoard
        val start = lastIndexOf("/", end - 1)
        if (start == -1) return defaultBoard
        substring(start + 1, end)
    } catch (_: Exception) {
        defaultBoard
    }
}