package kr.b1ink.htmlcompose

import androidx.compose.ui.unit.dp
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.nodes.Node
import com.fleeksoft.ksoup.nodes.TextNode

/**
 * HTML → List<HtmlBlock> 파서.
 *
 * flutter_widget_from_html의 BuildTree/WidgetFactory 아키텍처를 참고.
 * - 순수 함수 (no @Composable) → remember()로 캐싱 가능
 * - CSS 속성 상속 (InheritedProperties 개념)
 * - 블록/인라인 정확한 분리
 */
object HtmlParser {

    private val BLOCK_TAGS = setOf(
        "p", "div", "section", "article", "header", "footer", "nav", "main", "aside",
        "h1", "h2", "h3", "h4", "h5", "h6",
        "blockquote", "pre", "figure", "figcaption",
        "ul", "ol", "li",
        "hr", "br",
        "img", "video", "iframe",
        "table", "thead", "tbody", "tr", "td", "th",
        "details", "summary",
    )

    private val MEDIA_TAGS = setOf("img", "video", "iframe")

    /**
     * HTML 문자열을 파싱하여 블록 리스트로 변환.
     * @Composable이 아니므로 remember()로 캐싱 가능.
     */
    fun parse(html: String): List<HtmlBlock> {
        if (html.isBlank()) return emptyList()
        val document = Ksoup.parse(html)
        val blocks = mutableListOf<HtmlBlock>()
        processNodes(document.body().childNodes(), blocks, InheritedStyle())
        return blocks.trimSpacers()
    }

    // ─── 스타일 상속 (flutter_widget_from_html의 InheritedProperties 개념) ───

    private data class InheritedStyle(
        val bold: Boolean = false,
        val italic: Boolean = false,
        val underline: Boolean = false,
        val lineThrough: Boolean = false,
        val fontSize: Float? = null,
        val color: HtmlColor? = null,
        val textAlign: TextAlign = TextAlign.Start,
    ) {
        fun applyTag(tag: String): InheritedStyle = when (tag) {
            "b", "strong" -> copy(bold = true)
            "i", "em" -> copy(italic = true)
            "u" -> copy(underline = true)
            "s", "del", "strike" -> copy(lineThrough = true)
            else -> this
        }

        fun applyInlineStyle(styleAttr: String): InheritedStyle {
            if (styleAttr.isBlank()) return this
            var result = this
            parseCssProperties(styleAttr).forEach { (prop, rawValue) ->
                val value = rawValue.lowercase()
                when (prop) {
                    "color" -> HtmlColor.parse(rawValue)?.let { result = result.copy(color = it) }
                    "font-weight" -> {
                        if (value in setOf("bold", "700", "800", "900"))
                            result = result.copy(bold = true)
                    }
                    "font-style" -> {
                        if (value == "italic") result = result.copy(italic = true)
                    }
                    "text-decoration" -> {
                        if ("underline" in value) result = result.copy(underline = true)
                        if ("line-through" in value) result = result.copy(lineThrough = true)
                    }
                    "font-size" -> parseFontSize(value)?.let { result = result.copy(fontSize = it) }
                    "text-align" -> {
                        result = result.copy(textAlign = when (value) {
                            "center" -> TextAlign.Center
                            "right", "end" -> TextAlign.End
                            else -> TextAlign.Start
                        })
                    }
                }
            }
            return result
        }

        fun toInlineStyle(): InlineStyle = InlineStyle(
            bold = bold,
            italic = italic,
            underline = underline,
            lineThrough = lineThrough,
            fontSize = fontSize,
            color = color,
        )
    }

    // ─── 블록 처리 ───

    private fun processNodes(
        nodes: List<Node>,
        blocks: MutableList<HtmlBlock>,
        inherited: InheritedStyle,
    ) {
        val inlineBuffer = mutableListOf<Node>()

        fun flushInline() {
            if (inlineBuffer.isEmpty()) return
            val segments = buildInlineSegments(inlineBuffer, inherited)
            if (segments.hasContent()) {
                blocks.add(HtmlBlock.RichText(
                    segments = segments,
                    textAlign = inherited.textAlign,
                ))
            }
            inlineBuffer.clear()
        }

        for (node in nodes) {
            when {
                node is TextNode -> {
                    val text = node.text()
                    if (text.isNotBlank()) {
                        inlineBuffer.add(node)
                    }
                }
                node is Element -> {
                    val tag = node.tagName().lowercase()
                    if (isDisplayNone(node)) continue

                    if (tag in BLOCK_TAGS) {
                        flushInline()
                        processBlockElement(node, blocks, inherited)
                    } else {
                        inlineBuffer.add(node)
                    }
                }
            }
        }
        flushInline()
    }

    private fun processBlockElement(
        element: Element,
        blocks: MutableList<HtmlBlock>,
        inherited: InheritedStyle,
    ) {
        val tag = element.tagName().lowercase()
        val style = inherited.applyInlineStyle(element.attr("style"))

        when (tag) {
            "hr" -> blocks.add(HtmlBlock.Divider)

            "br" -> blocks.add(HtmlBlock.Spacer(height = 8.dp))

            "img" -> parseImage(element)?.let { blocks.add(it) }

            "video" -> parseVideo(element)?.let { blocks.add(it) }

            "iframe" -> {
                val src = element.attr("src")
                if (src.isNotBlank()) blocks.add(HtmlBlock.Iframe(src = src))
            }

            "p" -> {
                processInlineOrMixedContent(element, blocks, style)
                blocks.add(HtmlBlock.Spacer(height = 10.dp))
            }

            "h1" -> processHeading(element, blocks, style, 1)
            "h2" -> processHeading(element, blocks, style, 2)
            "h3" -> processHeading(element, blocks, style, 3)
            "h4" -> processHeading(element, blocks, style, 4)
            "h5" -> processHeading(element, blocks, style, 5)
            "h6" -> processHeading(element, blocks, style, 6)

            "blockquote" -> processBlockquote(element, blocks, style)

            "pre" -> {
                val codeElement = element.children().firstOrNull { it.tagName() == "code" }
                val code = (codeElement ?: element).wholeText()
                val language = codeElement?.className()?.removePrefix("language-") ?: ""
                if (code.isNotBlank()) {
                    blocks.add(HtmlBlock.Code(code = code, language = language))
                }
            }

            "ul" -> processUnorderedList(element, blocks, style, depth = 0)
            "ol" -> processOrderedList(element, blocks, style, depth = 0)

            "table" -> processTable(element, blocks, style)

            "figure" -> processNodes(element.childNodes(), blocks, style)

            "figcaption" -> {
                val figStyle = style.copy(italic = true)
                val segments = buildInlineSegments(element.childNodes(), figStyle)
                if (segments.hasContent()) {
                    blocks.add(HtmlBlock.RichText(segments = segments))
                }
            }

            "details" -> {
                // summary + content
                val summary = element.children().firstOrNull { it.tagName() == "summary" }
                if (summary != null) {
                    val segments = buildInlineSegments(summary.childNodes(), style.copy(bold = true))
                    if (segments.hasContent()) {
                        blocks.add(HtmlBlock.RichText(segments = segments))
                    }
                }
                val otherNodes = element.childNodes().filter {
                    !(it is Element && it.tagName() == "summary")
                }
                processNodes(otherNodes, blocks, style)
            }

            "summary" -> { /* handled in details */ }

            // 컨테이너 태그
            "div", "section", "article", "header", "footer", "nav", "main", "aside" -> {
                processNodes(element.childNodes(), blocks, style)
            }

            "thead", "tbody", "tr", "td", "th" -> {
                processNodes(element.childNodes(), blocks, style)
            }

            "li" -> {
                val segments = buildInlineSegments(element.childNodes(), style)
                if (segments.hasContent()) {
                    blocks.add(HtmlBlock.ListItem(segments = segments, marker = "\u2022"))
                }
            }
        }
    }

    // ─── 특수 블록 처리 ───

    private fun processHeading(
        element: Element,
        blocks: MutableList<HtmlBlock>,
        inherited: InheritedStyle,
        level: Int,
    ) {
        val style = inherited.copy(bold = true)
        processInlineOrMixedContent(element, blocks, style, headingLevel = level)
        blocks.add(HtmlBlock.Spacer(height = 8.dp))
    }

    private fun processBlockquote(
        element: Element,
        blocks: MutableList<HtmlBlock>,
        inherited: InheritedStyle,
    ) {
        // 블록 안에 블록이 있을 수 있음
        val innerBlocks = mutableListOf<HtmlBlock>()
        processNodes(element.childNodes(), innerBlocks, inherited)

        for (block in innerBlocks) {
            when (block) {
                is HtmlBlock.RichText -> blocks.add(
                    block.copy(indent = 12.dp, isBlockquote = true)
                )
                is HtmlBlock.Spacer -> blocks.add(block)
                else -> blocks.add(block)
            }
        }
    }

    private fun processUnorderedList(
        element: Element,
        blocks: MutableList<HtmlBlock>,
        inherited: InheritedStyle,
        depth: Int,
    ) {
        val bulletChars = listOf("\u2022", "\u25E6", "\u25AA") // ●, ◦, ▪
        val bullet = bulletChars[depth.coerceAtMost(bulletChars.lastIndex)]
        val indent = (16 + depth * 16).dp

        for (li in element.children().filter { it.tagName() == "li" }) {
            // li의 직접 인라인 컨텐츠
            val inlineNodes = li.childNodes().filter { node ->
                !(node is Element && node.tagName().lowercase() in setOf("ul", "ol"))
            }
            val segments = buildInlineSegments(inlineNodes, inherited)
            if (segments.hasContent()) {
                blocks.add(HtmlBlock.ListItem(
                    segments = segments,
                    marker = bullet,
                    indent = indent,
                    depth = depth,
                ))
            }
            // 중첩 리스트
            for (nested in li.children()) {
                when (nested.tagName().lowercase()) {
                    "ul" -> processUnorderedList(nested, blocks, inherited, depth + 1)
                    "ol" -> processOrderedList(nested, blocks, inherited, depth + 1)
                }
            }
        }
    }

    private fun processOrderedList(
        element: Element,
        blocks: MutableList<HtmlBlock>,
        inherited: InheritedStyle,
        depth: Int,
    ) {
        val indent = (16 + depth * 16).dp
        var counter = element.attr("start").toIntOrNull() ?: 1

        for (li in element.children().filter { it.tagName() == "li" }) {
            val inlineNodes = li.childNodes().filter { node ->
                !(node is Element && node.tagName().lowercase() in setOf("ul", "ol"))
            }
            val segments = buildInlineSegments(inlineNodes, inherited)
            if (segments.hasContent()) {
                blocks.add(HtmlBlock.ListItem(
                    segments = segments,
                    marker = "$counter.",
                    indent = indent,
                    depth = depth,
                ))
            }
            counter++
            for (nested in li.children()) {
                when (nested.tagName().lowercase()) {
                    "ul" -> processUnorderedList(nested, blocks, inherited, depth + 1)
                    "ol" -> processOrderedList(nested, blocks, inherited, depth + 1)
                }
            }
        }
    }

    private fun processTable(
        table: Element,
        blocks: MutableList<HtmlBlock>,
        inherited: InheritedStyle,
    ) {
        val rows = table.select("tr")
        for (row in rows) {
            val cells = row.children().filter { it.tagName() in setOf("td", "th") }
            val isHeader = cells.any { it.tagName() == "th" }

            val segments = mutableListOf<InlineSegment>()
            cells.forEachIndexed { index, cell ->
                if (index > 0) segments.add(InlineSegment.Text("  |  "))
                val cellInherited = if (isHeader) inherited.copy(bold = true) else inherited
                segments.addAll(buildInlineSegments(cell.childNodes(), cellInherited))
            }

            if (segments.hasContent()) {
                blocks.add(HtmlBlock.RichText(segments = segments))
            }
        }
    }

    // ─── 인라인 + 미디어 혼합 처리 ───

    private fun processInlineOrMixedContent(
        element: Element,
        blocks: MutableList<HtmlBlock>,
        inherited: InheritedStyle,
        headingLevel: Int = 0,
    ) {
        val children = element.childNodes()
        val hasMedia = children.any { it.isMediaNode() } ||
                children.any { it is Element && hasNestedMedia(it) }

        if (!hasMedia) {
            val segments = buildInlineSegments(children, inherited)
            if (segments.hasContent()) {
                blocks.add(HtmlBlock.RichText(
                    segments = segments,
                    headingLevel = headingLevel,
                    textAlign = inherited.textAlign,
                ))
            }
        } else {
            val inlineBuffer = mutableListOf<Node>()

            fun flushInline() {
                if (inlineBuffer.isEmpty()) return
                val segments = buildInlineSegments(inlineBuffer, inherited)
                if (segments.hasContent()) {
                    blocks.add(HtmlBlock.RichText(
                        segments = segments,
                        headingLevel = headingLevel,
                        textAlign = inherited.textAlign,
                    ))
                }
                inlineBuffer.clear()
            }

            for (child in children) {
                if (child is Element) {
                    val childTag = child.tagName().lowercase()
                    when {
                        childTag in MEDIA_TAGS -> {
                            flushInline()
                            processBlockElement(child, blocks, inherited)
                        }
                        hasNestedMedia(child) -> {
                            flushInline()
                            processNodes(child.childNodes(), blocks, inherited)
                        }
                        else -> inlineBuffer.add(child)
                    }
                } else {
                    inlineBuffer.add(child)
                }
            }
            flushInline()
        }
    }

    // ─── 인라인 세그먼트 빌더 (BuildBit 개념) ───

    private fun buildInlineSegments(
        nodes: List<Node>,
        inherited: InheritedStyle,
    ): List<InlineSegment> {
        val segments = mutableListOf<InlineSegment>()

        for (node in nodes) {
            when (node) {
                is TextNode -> {
                    val text = node.text()
                    if (text.isNotBlank() || (text.isNotEmpty() && text.contains(" "))) {
                        val style = inherited.toInlineStyle()
                        if (style == InlineStyle()) {
                            segments.add(InlineSegment.Text(text))
                        } else {
                            segments.add(InlineSegment.Styled(
                                children = listOf(InlineSegment.Text(text)),
                                style = style,
                            ))
                        }
                    }
                }

                is Element -> {
                    if (isDisplayNone(node)) continue

                    val tag = node.tagName().lowercase()
                    val childInherited = inherited.applyTag(tag)
                        .applyInlineStyle(node.attr("style"))

                    when (tag) {
                        "br" -> segments.add(InlineSegment.LineBreak)

                        "b", "strong" -> {
                            val children = buildInlineSegments(node.childNodes(), childInherited)
                            if (children.isNotEmpty()) {
                                segments.add(InlineSegment.Styled(children, InlineStyle.Bold))
                            }
                        }

                        "i", "em" -> {
                            val children = buildInlineSegments(node.childNodes(), childInherited)
                            if (children.isNotEmpty()) {
                                segments.add(InlineSegment.Styled(children, InlineStyle.Italic))
                            }
                        }

                        "u" -> {
                            val children = buildInlineSegments(node.childNodes(), childInherited)
                            if (children.isNotEmpty()) {
                                segments.add(InlineSegment.Styled(children, InlineStyle.Underline))
                            }
                        }

                        "s", "del", "strike" -> {
                            val children = buildInlineSegments(node.childNodes(), childInherited)
                            if (children.isNotEmpty()) {
                                segments.add(InlineSegment.Styled(children, InlineStyle.LineThrough))
                            }
                        }

                        "code" -> {
                            segments.add(InlineSegment.InlineCode(node.text()))
                        }

                        "a" -> {
                            val href = node.attr("href")
                            if (href.isNotBlank()) {
                                val children = buildInlineSegments(node.childNodes(), childInherited)
                                val linkChildren = children.ifEmpty {
                                    listOf(InlineSegment.Text(href))
                                }
                                segments.add(InlineSegment.Link(linkChildren, href))
                            } else {
                                segments.addAll(buildInlineSegments(node.childNodes(), childInherited))
                            }
                        }

                        "span" -> {
                            val spanStyle = parseInlineStyleAttr(node.attr("style"))
                            val children = buildInlineSegments(node.childNodes(), childInherited)
                            if (children.isNotEmpty()) {
                                if (spanStyle != null) {
                                    segments.add(InlineSegment.Styled(children, spanStyle))
                                } else {
                                    segments.addAll(children)
                                }
                            }
                        }

                        "sub" -> {
                            val children = buildInlineSegments(node.childNodes(), childInherited)
                            if (children.isNotEmpty()) {
                                segments.add(InlineSegment.Styled(
                                    children,
                                    InlineStyle(fontSize = 10f, baselineShift = BaselineShiftType.Subscript)
                                ))
                            }
                        }

                        "sup" -> {
                            val children = buildInlineSegments(node.childNodes(), childInherited)
                            if (children.isNotEmpty()) {
                                segments.add(InlineSegment.Styled(
                                    children,
                                    InlineStyle(fontSize = 10f, baselineShift = BaselineShiftType.Superscript)
                                ))
                            }
                        }

                        "img" -> {
                            val src = node.attr("src")
                            val alt = node.attr("alt")
                            if (src.isNotBlank()) {
                                segments.add(InlineSegment.InlineImage(src = src, alt = alt))
                            }
                        }

                        // 블록 요소가 인라인 버퍼에 들어온 경우 (잘못된 HTML)
                        "p", "div" -> {
                            segments.addAll(buildInlineSegments(node.childNodes(), childInherited))
                            segments.add(InlineSegment.LineBreak)
                        }

                        else -> {
                            segments.addAll(buildInlineSegments(node.childNodes(), childInherited))
                        }
                    }
                }
            }
        }
        return segments
    }

    // ─── 미디어/이미지/비디오 파싱 ───

    private fun parseImage(element: Element): HtmlBlock.Image? {
        val src = element.attr("src")
        if (src.isBlank()) return null
        return HtmlBlock.Image(
            src = src,
            alt = element.attr("alt"),
            width = element.attr("data-img-width").toIntOrNull()
                ?: element.attr("width").toIntOrNull() ?: -1,
            height = element.attr("data-img-height").toIntOrNull()
                ?: element.attr("height").toIntOrNull() ?: -1,
        )
    }

    private fun parseVideo(element: Element): HtmlBlock.Video? {
        var src = element.attr("src")
        if (src.isEmpty()) {
            src = element.children().firstOrNull { it.tagName() == "source" }?.attr("src") ?: ""
        }
        if (src.isBlank()) return null
        return HtmlBlock.Video(
            src = src,
            poster = element.attr("poster"),
            width = element.attr("width").toIntOrNull() ?: 640,
            height = element.attr("height").toIntOrNull() ?: 360,
        )
    }

    // ─── CSS 파싱 유틸리티 ───

    private fun parseCssProperties(styleAttr: String): List<Pair<String, String>> {
        return styleAttr.split(";")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { pair ->
                val colonIndex = pair.indexOf(':')
                if (colonIndex > 0) {
                    pair.substring(0, colonIndex).trim().lowercase() to
                            pair.substring(colonIndex + 1).trim()
                } else null
            }
    }

    private fun parseInlineStyleAttr(styleAttr: String): InlineStyle? {
        if (styleAttr.isBlank()) return null
        var style = InlineStyle()
        var hasStyle = false

        parseCssProperties(styleAttr).forEach { (prop, rawValue) ->
            val value = rawValue.lowercase()
            when (prop) {
                "color" -> HtmlColor.parse(rawValue)?.let {
                    style = style.copy(color = it); hasStyle = true
                }
                "background-color", "background" -> HtmlColor.parse(rawValue)?.let {
                    style = style.copy(backgroundColor = it); hasStyle = true
                }
                "font-weight" -> {
                    if (value in setOf("bold", "700", "800", "900")) {
                        style = style.copy(bold = true); hasStyle = true
                    }
                }
                "font-style" -> {
                    if (value == "italic") {
                        style = style.copy(italic = true); hasStyle = true
                    }
                }
                "text-decoration" -> {
                    if ("underline" in value) {
                        style = style.copy(underline = true); hasStyle = true
                    }
                    if ("line-through" in value) {
                        style = style.copy(lineThrough = true); hasStyle = true
                    }
                }
                "font-size" -> parseFontSize(value)?.let {
                    style = style.copy(fontSize = it); hasStyle = true
                }
            }
        }
        return if (hasStyle) style else null
    }

    private fun parseFontSize(value: String): Float? {
        val trimmed = value.trim().lowercase()
        return when {
            trimmed.endsWith("px") -> trimmed.removeSuffix("px").toFloatOrNull()
            trimmed.endsWith("em") -> trimmed.removeSuffix("em").toFloatOrNull()?.let { it * 16 }
            trimmed.endsWith("rem") -> trimmed.removeSuffix("rem").toFloatOrNull()?.let { it * 16 }
            trimmed.endsWith("pt") -> trimmed.removeSuffix("pt").toFloatOrNull()?.let { it * 1.33f }
            else -> null
        }
    }

    // ─── 유틸리티 ───

    private fun isDisplayNone(element: Element): Boolean {
        val style = element.attr("style")
        if (style.isBlank()) return false
        return parseCssProperties(style).any { (prop, value) ->
            prop == "display" && value.lowercase() == "none"
        }
    }

    private fun hasNestedMedia(element: Element): Boolean {
        if (element.tagName().lowercase() in MEDIA_TAGS) return true
        return element.children().any { hasNestedMedia(it) }
    }

    private fun Node.isMediaNode(): Boolean =
        this is Element && tagName().lowercase() in MEDIA_TAGS

    private fun List<InlineSegment>.hasContent(): Boolean = any { segment ->
        when (segment) {
            is InlineSegment.Text -> segment.text.isNotBlank()
            is InlineSegment.Styled -> segment.children.hasContent()
            is InlineSegment.Link -> true
            is InlineSegment.LineBreak -> false
            is InlineSegment.InlineCode -> segment.text.isNotBlank()
            is InlineSegment.InlineImage -> true
        }
    }

    private fun List<HtmlBlock>.trimSpacers(): List<HtmlBlock> {
        return this
            .dropWhile { it is HtmlBlock.Spacer }
            .dropLastWhile { it is HtmlBlock.Spacer }
            .fold(mutableListOf()) { acc, block ->
                if (block !is HtmlBlock.Spacer || acc.lastOrNull() !is HtmlBlock.Spacer) {
                    acc.add(block)
                }
                acc
            }
    }
}
