package kr.b1ink.htmlcompose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.nodes.Node
import com.fleeksoft.ksoup.nodes.TextNode
import timber.log.Timber

@Composable
fun HtmlRenderer(
    html: String,
    linkColor: Color = MaterialTheme.colorScheme.tertiary,
    modifier: Modifier = Modifier
) {
    val document = remember(html) { Ksoup.parse(html) }
    Timber.d("[HtmlRenderer] html=${html}")
    val uriHandler = LocalUriHandler.current

    SelectionContainer {
        Column(
            modifier = modifier
                .padding(vertical = 6.dp)
        ) {
            // 텍스트 + 이미지 + 텍스트 형태일 때..
            if (html.startsWith("<strong>@<img") || html.startsWith("<strong data-nick-id=")) {
                val inlineContentMap = mutableMapOf<String, InlineTextContent>()
                val text = buildAnnotatedStringWithStyles(
                    nodes = document.childNodes,
                    linkColor = linkColor,
                    parentStyle = SpanStyle(),
                    inlineContentMap = inlineContentMap
                ) {
                    uriHandler.openUri(it)
                }

                if (text.isNotBlank()) {
                    val textStyle = getTextStyleForHeader("strong")
                    Text(
                        text = text,
                        style = textStyle,
                        inlineContent = inlineContentMap,
                    )
                }
            } else {
                document.body()
                    .childNodes()
                    .forEach { node ->
                        node.RenderNode(linkColor, listIndex = null)
                    }
            }
        }
    }
}

private fun buildAnnotatedStringWithStyles(
    nodes: List<Node>,
    linkColor: Color,
    parentStyle: SpanStyle,
    inlineContentMap: MutableMap<String, InlineTextContent>,
    openUrl: (url: String) -> Unit = {}
): AnnotatedString = buildAnnotatedString {
    nodes.forEach { node ->
        when (node) {
            is TextNode -> append(
                node.text()
                    .trim()
            )

            is Element -> {
                val childStyle = parentStyle.merge(getStyleForTag(node.tagName()))
                when (node.tagName()
                    .lowercase()) {
                    "br" -> append("\n")

                    "a" -> {
                        val href = node.attr("href")
                        val link = LinkAnnotation.Url(
                            href,
                            TextLinkStyles(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline))
                        ) {
                            val url = (it as LinkAnnotation.Url).url
                            openUrl(url)
                        }
                        withLink(link) {
                            append(href)
                        }
                    }

                    "img" -> {
                        val src = node.attr("src")
                        if (src.isNotBlank()) {
                            val height = 17.sp
                            val width = height * 3.5
                            val alt = node.attr("alt")

                            val inlineId = "inline_img_${src.hashCode()}"
                            appendInlineContent(inlineId, "[$alt]")

                            inlineContentMap[inlineId] = InlineTextContent(
                                Placeholder(
                                    width = width,
                                    height = height,
                                    placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
                                )
                            ) {
                                AsyncImage(
                                    model = src,
                                    contentScale = ContentScale.FillBounds,
                                    modifier = Modifier
                                        .height(height.value.dp),
                                    contentDescription = alt
                                )
                            }
                        }
                    }

                    else -> {
                        Timber.d("else html = ${node.html()}")
                        withStyle(style = childStyle) {
                            append(
                                buildAnnotatedStringWithStyles(
                                    nodes = node.childNodes(),
                                    linkColor = linkColor,
                                    parentStyle = childStyle,
                                    inlineContentMap = inlineContentMap,
                                    openUrl = openUrl
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Node.RenderNode(
    linkColor: Color,
    listIndex: Int?
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val uriHandler = LocalUriHandler.current

//    Timber.d("[RenderNode] = $this")

    when (this) {
        is Element -> {
            when (tagName()
                .lowercase()) {

                "h1", "h2", "h3", "h4", "h5", "h6", "strong", "span" -> {
                    val contentChunks = childNodes().groupNodes(linkColor) {
                        uriHandler.openUri(it)
                    }
                    if (contentChunks.isNotEmpty()) {
                        val textStyle = getTextStyleForHeader(tagName())

                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            contentChunks.forEach { chunk ->
                                Timber.d("[contentChunks] chunk=$chunk")
                                when (chunk) {
                                    is ContentChunk.Text ->
                                        Text(
                                            text = chunk.annotatedString,
                                            style = textStyle,
                                        )

                                    is ContentChunk.Image -> ImageRenderer(
                                        src = chunk.src,
                                        alt = chunk.alt,
                                        originalWidth = chunk.width,
                                        originalHeight = chunk.height,
                                        screenWidth = screenWidth
                                    )

                                    is ContentChunk.Iframe -> IframeRenderer(src = chunk.src)

                                    is ContentChunk.Video ->
                                        ImageRenderer(
                                            src = chunk.poster,
                                            alt = chunk.alt,
                                            originalWidth = chunk.width,
                                            originalHeight = chunk.height,
                                            screenWidth = screenWidth
                                        )
                                }
                            }
                        }
                    }
                }

                "hr" -> HorizontalDivider(
                    thickness = 4.dp,
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                "ul" -> Column(modifier = Modifier.padding(start = 16.dp)) {
                    children()
                        .filter { it.tagName() == "li" }
                        .forEach { li ->
                            li.RenderNode(linkColor = linkColor, listIndex = null)
                        }
                }

                "br" -> Spacer(modifier = Modifier.height(17.dp))

                "ol" -> Column(modifier = Modifier.padding(start = 16.dp)) {
                    var counter = 0
                    children()
                        .filter { it.tagName() == "li" }
                        .forEach { li ->
                            li.RenderNode(linkColor, listIndex = ++counter)
                        }
                }

                "li" -> {
                    val text = buildAnnotatedStringForNodes(childNodes(), linkColor)
                    if (text.isNotBlank()) {
                        val marker = if (listIndex != null) "$listIndex." else "\u2022"
                        val paragraphStyle = ParagraphStyle(textIndent = TextIndent(restLine = 12.sp))
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = paragraphStyle) {
                                    append("$marker\t")
                                    append(text)
                                }
                            },
                        )
                    }
                }

                "blockquote" -> {
                    val paragraphStyle = ParagraphStyle(textIndent = TextIndent(firstLine = 12.sp, restLine = 12.sp))
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = paragraphStyle) {
                                append(text())
                            }
                        },
                        style = LocalTextStyle.current.copy(fontStyle = FontStyle.Italic),
                    )
                }

                "img" -> {
                    val src = attr("src")
                    val alt = attr("alt")
                    val width = attr("data-img-width")
                        .toIntOrNull() ?: attr("width").toIntOrNull() ?: -1
                    val height = attr("data-img-height")
                        .toIntOrNull() ?: attr("height").toIntOrNull() ?: -1

                    if (src.isNotBlank()) {
                        ImageRenderer(
                            src = src,
                            alt = alt,
                            originalWidth = width,
                            originalHeight = height,
                            screenWidth = screenWidth
                        )
                    }
                }

                "video" -> {
                    var src = attr("src")
                    val width = attr("width").toIntOrNull() ?: 640
                    val height = attr("height").toIntOrNull() ?: 360

                    if (src.isEmpty()) {
                        src = first { it.tagName() == "source" }.attr("src")
                    }

                    if (src.isNotBlank()) {
                        VideoRenderer(
                            url = src,
                            width = width,
                            height = height,
                            screenWidth = screenWidth
                        )
                    }
                }

                "iframe" -> {
                    val src = attr("src")
                    val width = attr("width").toIntOrNull() ?: 640
                    val height = attr("height").toIntOrNull() ?: 360

                    Timber.d("[iframe] src=$src, width=$width, height=$height")

                    if (src.isNotBlank()) {
                        IframeRenderer(src = src)
                    }
                }

                else -> {
                    // 다른 태그들은 자식 노드만 재귀적으로 렌더링
                    childNodes()
                        .forEach { child ->
                            child.RenderNode(linkColor, listIndex = listIndex)
                        }

                    if (tagName().lowercase() == "p") {
                        val parent = parent() // jsoup의 Element 객체라고 가정
                        val isLastParagraph = if (parent != null) {
                            val siblings = parent.children()
                                .filter { it.tagName() == "p" }
                            siblings.lastOrNull() == this
                        } else {
                            // 부모가 없거나 다른 경우 (최상위 p 태그 등)에는 마지막으로 간주하지 않음
                            false
                        }
                        if (!isLastParagraph) {
                            Spacer(
                                modifier = Modifier
                                    .size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        is TextNode -> {
            // TextNode는 부모의 buildAnnotatedString에서 직접 처리되므로 여기서는 아무것도 하지 않음
            // 단, 최상위 레벨의 텍스트 노드는 렌더링될 수 있도록 처리
            val text = text().trim()
            if (text.isNotEmpty()) {
                // URL을 찾기 위한 정규식
                val urlRegex = remember { Regex("""https?://[^\s/$.?#].[^\s]*""") }

                val annotatedString = buildAnnotatedString {
                    var lastIndex = 0
                    urlRegex.findAll(text)
                        .forEach { matchResult ->
                            // URL 앞부분의 텍스트 추가
                            if (matchResult.range.first > lastIndex) {
                                append(text.substring(lastIndex, matchResult.range.first))
                            }

                            // URL 부분
                            val url = matchResult.value
                            val link = LinkAnnotation.Url(
                                url = url,
                                styles = TextLinkStyles(
                                    SpanStyle(
                                        color = linkColor,
                                        textDecoration = TextDecoration.Underline
                                    )
                                ), // 포커스, 호버 등 상태에 따른 스타일 지정 가능 [1]
                                linkInteractionListener = {
                                    try {
                                        uriHandler.openUri(url)
                                    } catch (_: Exception) {
                                    }
                                }
                            )
                            withLink(link) {
                                append(url) // 링크로 표시될 텍스트
                            }

                            lastIndex = matchResult.range.last + 1
                        }

                    // URL 뒷부분의 텍스트 추가
                    if (lastIndex < text.length) {
                        append(text.substring(lastIndex))
                    }
                }

                Text(
                    text = annotatedString,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

private fun List<Node>.groupNodes(
    linkColor: Color,
    openUrl: (url: String) -> Unit = {}
): List<ContentChunk> {
    val chunks = mutableListOf<ContentChunk>()
    val textNodes = mutableListOf<Node>()

    fun flushTextNodes() {
        if (textNodes.isNotEmpty()) {
            chunks.add(
                ContentChunk.Text(
                    annotatedString = buildAnnotatedStringForNodes(
                        nodes = textNodes,
                        linkColor = linkColor,
                        openUrl = openUrl
                    )
                )
            )
            textNodes.clear()
        }
    }

    forEach { node ->
//        Timber.d("[groupNodes] node = $node")
        if (node is Element && node.tagName()
                .lowercase() == "img"
        ) {
            flushTextNodes()

            val src = node.attr("src")
            val alt = node.attr("alt")
            val width = node.attr("data-img-width")
                .toIntOrNull() ?: node.attr("width")
                .toIntOrNull() ?: -1
            val height = node.attr("data-img-height")
                .toIntOrNull() ?: node.attr("height")
                .toIntOrNull() ?: -1

            if (src.isNotBlank()) {
                chunks.add(ContentChunk.Image(src, alt, width, height))
            }
        } else if (node is Element && node.tagName()
                .lowercase() == "video"
        ) {
            node.childNodes.firstOrNull()
                ?.run {
                    val src = attr("src")
                    val poster = node.attr("poster")
                    val width = node.attr("width")
                        .toIntOrNull() ?: 640
                    val height = node.attr("height")
                        .toIntOrNull() ?: 360
                    chunks.add(ContentChunk.Video(poster, src, "iframe", width, height))
                }
        } else if (node is Element && node.tagName()
                .lowercase() == "iframe"
        ) {
            val src = node.attr("src")
            val width = node.attr("width")
                .toIntOrNull() ?: 640
            val height = node.attr("height")
                .toIntOrNull() ?: 360

            chunks.add(ContentChunk.Iframe(src, "iframe", width, height))
        } else if (node is Element && node.tagName()
                .lowercase() == "a"
        ) {
            val href = node.attr("href")

            if (node.childrenSize() > 0) {
                node.childNodes().groupNodes(linkColor, openUrl)
                    .forEach {
                        chunks.add(it)
                    }
            } else {
                val text = node.ownText()
                chunks.add(ContentChunk.Text(
                    buildAnnotatedString {
                        val link = LinkAnnotation.Url(
                            href,
                            TextLinkStyles(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline))
                        ) {
                            val url = (it as LinkAnnotation.Url).url
                            openUrl(url)
                        }
                        withLink(link) {
                            append(text.trim().ifEmpty { href })
                        }
                    }
                ))
            }
        } else {
//            Timber.d("[groupNodes] node=$node")
            textNodes.add(node)
        }
    }
    flushTextNodes()
    return chunks
}

private fun buildAnnotatedStringForNodes(
    nodes: List<Node>,
    linkColor: Color,
    openUrl: (url: String) -> Unit = {}
): AnnotatedString =
    buildAnnotatedString {
        nodes.forEach { node ->
            buildNode(node, SpanStyle(), linkColor, openUrl)
        }
    }

private fun AnnotatedString.Builder.buildNode(
    node: Node,
    style: SpanStyle,
    linkColor: Color,
    openUrl: (url: String) -> Unit = {}
) {
//    Timber.d("[buildNode] node=$node")
    when (node) {
        is TextNode -> {
            val text = node.text()
            withStyle(style) {
                if (text.startsWith("https://") || text.startsWith("http://")) {
                    val link = LinkAnnotation.Url(
                        text,
                        TextLinkStyles(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline))
                    ) {
                        val url = (it as LinkAnnotation.Url).url
                        openUrl(url)
                    }
                    withLink(link) {
                        append(text)
                    }
                } else {
                    append(text)
                }
            }
        }

        is Element -> {
            val newStyle = style.merge(getStyleForTag(node.tagName()))
            when (node.tagName()
                .lowercase()) {
                "br" -> append("\n")

                "a" -> {
                    val href = node.attr("href")

                    val link = LinkAnnotation.Url(
                        href,
                        TextLinkStyles(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline))
                    ) {
                        val url = (it as LinkAnnotation.Url).url
                        openUrl(url)
                    }
                    withLink(link) {
                        append(node.text().trim().ifEmpty { href })
                    }
                }

                else -> { // b, i, u, strong, em, etc.
                    withStyle(newStyle) {
                        node.childNodes()
                            .forEach { child -> buildNode(child, newStyle, linkColor) }
                    }
                }
            }
        }
    }
}

private fun getStyleForTag(tag: String): SpanStyle = when (tag.lowercase()) {
    "b", "strong" -> SpanStyle(fontWeight = FontWeight.Bold)
    "i", "em" -> SpanStyle(fontStyle = FontStyle.Italic)
    "u" -> SpanStyle(textDecoration = TextDecoration.Underline)
    "a" -> SpanStyle(color = Color.Blue, textDecoration = TextDecoration.Underline)
    else -> SpanStyle()
}

@Composable
private fun getTextStyleForHeader(tag: String): TextStyle = when (tag.lowercase()) {
    "h1" -> MaterialTheme.typography.headlineLarge
    "h2" -> MaterialTheme.typography.headlineMedium
    "h3" -> MaterialTheme.typography.headlineSmall
    "h4" -> MaterialTheme.typography.titleLarge
    "h5" -> MaterialTheme.typography.titleMedium
    "h6" -> MaterialTheme.typography.titleSmall
    else -> LocalTextStyle.current
}