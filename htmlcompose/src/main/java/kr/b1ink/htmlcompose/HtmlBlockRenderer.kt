package kr.b1ink.htmlcompose

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import timber.log.Timber

/**
 * flutter_widget_from_html의 WidgetFactory 개념.
 * 각 블록 타입의 렌더링을 커스터마이징 가능.
 */
open class HtmlWidgetFactory {
    @Composable
    open fun BuildImage(block: HtmlBlock.Image) {
        ImageRenderer(
            src = block.src,
            alt = block.alt,
            originalWidth = block.width,
            originalHeight = block.height,
        )
    }

    @Composable
    open fun BuildVideo(block: HtmlBlock.Video) {
        VideoRenderer(
            url = block.src,
            width = block.width,
            height = block.height,
        )
    }

    @Composable
    open fun BuildIframe(block: HtmlBlock.Iframe) {
        val videoId = extractYoutubeVideoId(block.src)
        if (videoId != null) {
            YoutubeRenderer(
                width = -1,
                height = -1,
                videoId = { videoId },
            )
        } else {
            IframeRenderer(src = block.src)
        }
    }
}

private val youtubePatterns = listOf(
    Regex("""youtube\.com/embed/([a-zA-Z0-9_-]{11})"""),
    Regex("""youtube\.com/watch\?v=([a-zA-Z0-9_-]{11})"""),
    Regex("""youtu\.be/([a-zA-Z0-9_-]{11})"""),
    Regex("""youtube-nocookie\.com/embed/([a-zA-Z0-9_-]{11})"""),
)

private fun extractYoutubeVideoId(url: String): String? {
    return youtubePatterns.firstNotNullOfOrNull { it.find(url)?.groupValues?.get(1) }
}

/**
 * LazyColumn용 확장 함수.
 * flutter_widget_from_html의 ListView.builder 모드와 동일하게
 * 각 블록을 별도의 LazyColumn item으로 추가.
 *
 * - 이미지/비디오 로딩 시 다른 블록에 영향 없음 (스크롤 안정)
 * - 화면 밖 블록은 lazy하게 렌더링 (메모리 절약)
 * - 파서가 순수함수이므로 LazyListScope에서 직접 호출 가능
 *
 * ⚠️ LazyListScope는 @Composable이 아니므로 매 recomposition마다 parse가 실행됨.
 * 성능이 중요한 경우 [htmlContent(blocks, ...)] 오버로드를 사용하여
 * 호출자가 remember로 캐싱한 블록을 전달할 것.
 */
fun LazyListScope.htmlContent(
    html: String,
    keyPrefix: String = "html",
    factory: HtmlWidgetFactory = HtmlWidgetFactory(),
) {
    val blocks = HtmlParser.parse(html)
    htmlContent(blocks = blocks, keyPrefix = keyPrefix, factory = factory)
}

/**
 * 사전 파싱된 블록 리스트를 받는 오버로드.
 * 호출자가 remember { HtmlParser.parse(html) }로 캐싱하여 전달하면
 * recomposition마다 파싱을 반복하지 않음.
 *
 * contentType을 블록 타입별로 지정하여 LazyList가
 * 같은 타입의 아이템을 더 효율적으로 재사용 (RecyclerView의 viewType과 유사).
 */
fun LazyListScope.htmlContent(
    blocks: List<HtmlBlock>,
    keyPrefix: String = "html",
    factory: HtmlWidgetFactory = HtmlWidgetFactory(),
) {
    items(
        count = blocks.size,
        key = { index -> "${keyPrefix}_${index}" },
        contentType = { index -> blocks[index]::class },
    ) { index ->
        val block = blocks[index]
        val linkColor = MaterialTheme.colorScheme.tertiary
        val uriHandler = LocalUriHandler.current

        RenderBlock(
            block = block,
            linkColor = linkColor,
            factory = factory,
            onLinkClick = { url ->
                try { uriHandler.openUri(url) } catch (e: Exception) { Timber.e(e, "Failed to open URI: $url") }
            },
        )
    }
}

/**
 * Column용 HtmlRenderer.
 * 댓글 등 LazyColumn 외부에서 사용.
 */
@Composable
fun HtmlRenderer(
    modifier: Modifier = Modifier,
    html: String,
    linkColor: Color = MaterialTheme.colorScheme.tertiary,
    factory: HtmlWidgetFactory = HtmlWidgetFactory(),
) {
    val blocks = remember(html) { HtmlParser.parse(html) }
    val uriHandler = LocalUriHandler.current

    SelectionContainer {
        Column(modifier = modifier.padding(vertical = 6.dp)) {
            for (block in blocks) {
                RenderBlock(
                    block = block,
                    linkColor = linkColor,
                    factory = factory,
                    onLinkClick = { url ->
                        try { uriHandler.openUri(url) } catch (e: Exception) { Timber.e(e, "Failed to open URI: $url") }
                    },
                )
            }
        }
    }
}

// ─── 블록 렌더링 ───

@Composable
private fun RenderBlock(
    block: HtmlBlock,
    linkColor: Color,
    factory: HtmlWidgetFactory,
    onLinkClick: (String) -> Unit,
) {
    when (block) {
        is HtmlBlock.RichText -> RenderRichText(block, linkColor, onLinkClick)
        is HtmlBlock.Image -> factory.BuildImage(block)
        is HtmlBlock.Video -> factory.BuildVideo(block)
        is HtmlBlock.Iframe -> factory.BuildIframe(block)
        is HtmlBlock.Code -> RenderCodeBlock(block)
        is HtmlBlock.Divider -> HorizontalDivider(
            thickness = 2.dp,
            modifier = Modifier.padding(vertical = 6.dp),
        )
        is HtmlBlock.Spacer -> Spacer(modifier = Modifier.height(block.height))
        is HtmlBlock.ListItem -> RenderListItem(block, linkColor, onLinkClick)
    }
}

@Composable
private fun RenderRichText(
    block: HtmlBlock.RichText,
    linkColor: Color,
    onLinkClick: (String) -> Unit,
) {
    val result = remember(block) {
        buildRichTextResult(block.segments, linkColor, onLinkClick)
    }

    if (result.text.isNotBlank()) {
        val modifier = when {
            block.isBlockquote -> {
                val borderColor = Color.Gray.copy(alpha = 0.4f)
                Modifier
                    .fillMaxWidth()
                    .padding(start = block.indent, top = 4.dp, bottom = 4.dp)
                    .drawBehind {
                        drawLine(
                            color = borderColor,
                            start = Offset(0f, 0f),
                            end = Offset(0f, size.height),
                            strokeWidth = 3.dp.toPx()
                        )
                    }
                    .padding(start = 12.dp)
            }
            block.indent > 0.dp -> Modifier.fillMaxWidth().padding(start = block.indent)
            else -> Modifier.fillMaxWidth()
        }

        val textStyle = resolveHeadingStyle(block.headingLevel)
        val textAlign = when (block.textAlign) {
            TextAlign.Center -> androidx.compose.ui.text.style.TextAlign.Center
            TextAlign.End -> androidx.compose.ui.text.style.TextAlign.End
            TextAlign.Start -> null
        }

        Text(
            text = result.text,
            style = textStyle,
            textAlign = textAlign,
            inlineContent = result.inlineContent,
            modifier = modifier,
        )
    }
}

@Composable
private fun resolveHeadingStyle(level: Int): TextStyle = when (level) {
    1 -> MaterialTheme.typography.headlineLarge
    2 -> MaterialTheme.typography.headlineMedium
    3 -> MaterialTheme.typography.headlineSmall
    4 -> MaterialTheme.typography.titleLarge
    5 -> MaterialTheme.typography.titleMedium
    6 -> MaterialTheme.typography.titleSmall
    else -> LocalTextStyle.current
}

@Composable
private fun RenderCodeBlock(block: HtmlBlock.Code) {
    val bgColor = MaterialTheme.colorScheme.surfaceVariant
    Text(
        text = block.code,
        style = LocalTextStyle.current.copy(
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(8.dp))
            .horizontalScroll(rememberScrollState())
            .padding(12.dp),
    )
}

@Composable
private fun RenderListItem(
    block: HtmlBlock.ListItem,
    linkColor: Color,
    onLinkClick: (String) -> Unit,
) {
    val result = remember(block) {
        buildRichTextResult(block.segments, linkColor, onLinkClick)
    }

    if (result.text.isNotBlank()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = block.indent),
        ) {
            Text(
                text = block.marker,
                modifier = Modifier.padding(end = 8.dp),
            )
            Text(
                text = result.text,
                inlineContent = result.inlineContent,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

// ─── InlineSegment → AnnotatedString 변환 (렌더링 시점) ───

private data class InlineRichTextResult(
    val text: AnnotatedString,
    val inlineContent: Map<String, InlineTextContent>,
)

private fun buildRichTextResult(
    segments: List<InlineSegment>,
    linkColor: Color,
    onLinkClick: (String) -> Unit,
): InlineRichTextResult {
    val inlineContentMap = mutableMapOf<String, InlineTextContent>()
    val text = buildAnnotatedString {
        appendSegments(segments, linkColor, onLinkClick, inlineContentMap)
    }
    return InlineRichTextResult(text, inlineContentMap)
}

private fun AnnotatedString.Builder.appendSegments(
    segments: List<InlineSegment>,
    linkColor: Color,
    onLinkClick: (String) -> Unit,
    inlineContentMap: MutableMap<String, InlineTextContent>,
) {
    for (segment in segments) {
        when (segment) {
            is InlineSegment.Text -> append(segment.text)

            is InlineSegment.Styled -> {
                val spanStyle = segment.style.toSpanStyle()
                withStyle(spanStyle) {
                    appendSegments(segment.children, linkColor, onLinkClick, inlineContentMap)
                }
            }

            is InlineSegment.Link -> {
                val link = LinkAnnotation.Url(
                    segment.href,
                    TextLinkStyles(SpanStyle(
                        color = linkColor,
                        textDecoration = TextDecoration.Underline
                    ))
                ) {
                    val url = (it as LinkAnnotation.Url).url
                    onLinkClick(url)
                }
                withLink(link) {
                    appendSegments(segment.children, linkColor, onLinkClick, inlineContentMap)
                }
            }

            is InlineSegment.LineBreak -> append("\n")

            is InlineSegment.InlineCode -> {
                withStyle(SpanStyle(
                    fontFamily = FontFamily.Monospace,
                    background = Color(0x20808080),
                )) {
                    append(segment.text)
                }
            }

            is InlineSegment.InlineImage -> {
                val inlineId = "inline_img_${segment.src.hashCode()}"
                val imgHeight = 17.sp
                val imgWidth = imgHeight * 3.5

                appendInlineContent(inlineId, "[${segment.alt}]")

                inlineContentMap[inlineId] = InlineTextContent(
                    Placeholder(
                        width = imgWidth,
                        height = imgHeight,
                        placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
                    )
                ) {
                    AsyncImage(
                        model = segment.src,
                        contentDescription = segment.alt,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.height(imgHeight.value.dp),
                    )
                }
            }
        }
    }
}

// ─── InlineStyle → SpanStyle 변환 ───

private fun InlineStyle.toSpanStyle(): SpanStyle {
    var fontWeight: FontWeight? = null
    var fontStyle: FontStyle? = null
    var textDecoration: TextDecoration? = null
    var fontSize: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified
    var color: Color = Color.Unspecified
    var background: Color = Color.Unspecified
    var fontFamily: FontFamily? = null
    var baselineShift: BaselineShift? = null

    if (bold) fontWeight = FontWeight.Bold
    if (italic) fontStyle = FontStyle.Italic
    if (underline && lineThrough) {
        textDecoration = TextDecoration.Underline + TextDecoration.LineThrough
    } else if (underline) {
        textDecoration = TextDecoration.Underline
    } else if (lineThrough) {
        textDecoration = TextDecoration.LineThrough
    }
    this.fontSize?.let { fontSize = it.sp }
    this.color?.let { color = Color(it.r, it.g, it.b, it.a) }
    this.backgroundColor?.let { background = Color(it.r, it.g, it.b, it.a) }
    if (this.fontFamily == FontFamilyType.Monospace) fontFamily = FontFamily.Monospace
    when (this.baselineShift) {
        BaselineShiftType.Superscript -> baselineShift = BaselineShift.Superscript
        BaselineShiftType.Subscript -> baselineShift = BaselineShift.Subscript
        null -> {}
    }

    return SpanStyle(
        fontWeight = fontWeight,
        fontStyle = fontStyle,
        textDecoration = textDecoration,
        fontSize = fontSize,
        color = color,
        background = background,
        fontFamily = fontFamily,
        baselineShift = baselineShift,
    )
}
