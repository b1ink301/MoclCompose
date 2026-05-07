package kr.b1ink.htmlcompose

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * HTML 블록 - flutter_widget_from_html의 BuildTree 개념.
 *
 * 파싱 결과를 순수 데이터로 표현. @Composable 의존 없음.
 * 각 블록은 LazyColumn의 개별 item으로 렌더링되어 스크롤 안정성을 보장.
 */
sealed class HtmlBlock {

    /** 인라인 요소들의 트리 구조 (텍스트, 스타일, 링크 등) */
    data class RichText(
        val segments: List<InlineSegment>,
        val headingLevel: Int = 0, // 0=body, 1=h1, ..., 6=h6
        val indent: Dp = 0.dp,
        val isBlockquote: Boolean = false,
        val textAlign: TextAlign = TextAlign.Start,
    ) : HtmlBlock()

    data class Image(
        val src: String,
        val alt: String = "",
        val width: Int = -1,
        val height: Int = -1,
    ) : HtmlBlock()

    data class Video(
        val src: String,
        val poster: String = "",
        val width: Int = 640,
        val height: Int = 360,
    ) : HtmlBlock()

    data class Iframe(
        val src: String,
    ) : HtmlBlock()

    data class Code(
        val code: String,
        val language: String = "",
    ) : HtmlBlock()

    data object Divider : HtmlBlock()

    data class Spacer(
        val height: Dp = 14.dp,
    ) : HtmlBlock()

    data class ListItem(
        val segments: List<InlineSegment>,
        val marker: String,
        val indent: Dp = 16.dp,
        val depth: Int = 0,
    ) : HtmlBlock()
}

enum class TextAlign { Start, Center, End }

/**
 * 인라인 콘텐츠 트리 - flutter_widget_from_html의 BuildBit 개념.
 *
 * AnnotatedString을 파싱 시점에 만들지 않고, 렌더링 시점에 생성.
 * 이로써 파서에서 @Composable 의존성을 완전히 제거.
 */
sealed class InlineSegment {
    /** 순수 텍스트 */
    data class Text(val text: String) : InlineSegment()

    /** 스타일이 적용된 자식 세그먼트 */
    data class Styled(
        val children: List<InlineSegment>,
        val style: InlineStyle,
    ) : InlineSegment()

    /** 링크 */
    data class Link(
        val children: List<InlineSegment>,
        val href: String,
    ) : InlineSegment()

    /** 줄바꿈 */
    data object LineBreak : InlineSegment()

    /** 인라인 코드 */
    data class InlineCode(val text: String) : InlineSegment()

    /** 인라인 이미지 (alt 텍스트로 표시) */
    data class InlineImage(
        val src: String,
        val alt: String,
    ) : InlineSegment()
}

/**
 * CSS에서 파싱된 인라인 스타일.
 * Compose SpanStyle과 분리하여 파서의 @Composable 의존 제거.
 */
data class InlineStyle(
    val bold: Boolean = false,
    val italic: Boolean = false,
    val underline: Boolean = false,
    val lineThrough: Boolean = false,
    val fontSize: Float? = null, // sp 단위
    val color: HtmlColor? = null,
    val backgroundColor: HtmlColor? = null,
    val fontFamily: FontFamilyType? = null,
    val baselineShift: BaselineShiftType? = null,
) {
    companion object {
        val Bold = InlineStyle(bold = true)
        val Italic = InlineStyle(italic = true)
        val Underline = InlineStyle(underline = true)
        val LineThrough = InlineStyle(lineThrough = true)
        val Monospace = InlineStyle(fontFamily = FontFamilyType.Monospace)
    }
}

enum class FontFamilyType { Monospace }
enum class BaselineShiftType { Superscript, Subscript }

/**
 * HTML 색상 값. 순수 데이터 - Compose Color와 분리.
 */
data class HtmlColor(val r: Int, val g: Int, val b: Int, val a: Int = 255) {
    companion object {
        fun fromHex(hex: String): HtmlColor? {
            val clean = hex.removePrefix("#").lowercase()
            return try {
                when (clean.length) {
                    3 -> HtmlColor(
                        clean[0].toString().repeat(2).toInt(16),
                        clean[1].toString().repeat(2).toInt(16),
                        clean[2].toString().repeat(2).toInt(16),
                    )
                    6 -> HtmlColor(
                        clean.substring(0, 2).toInt(16),
                        clean.substring(2, 4).toInt(16),
                        clean.substring(4, 6).toInt(16),
                    )
                    8 -> HtmlColor(
                        clean.substring(0, 2).toInt(16),
                        clean.substring(2, 4).toInt(16),
                        clean.substring(4, 6).toInt(16),
                        clean.substring(6, 8).toInt(16),
                    )
                    else -> null
                }
            } catch (_: Exception) { null }
        }

        fun fromRgb(value: String): HtmlColor? {
            return try {
                val parts = value.substringAfter("(").substringBefore(")")
                    .split(",").map { it.trim() }
                if (parts.size < 3) return null
                val r = parts[0].toFloat().toInt().coerceIn(0, 255)
                val g = parts[1].toFloat().toInt().coerceIn(0, 255)
                val b = parts[2].toFloat().toInt().coerceIn(0, 255)
                // rgba()의 alpha는 0.0~1.0 범위
                val a = if (parts.size >= 4) {
                    val alpha = parts[3].toFloat()
                    if (alpha <= 1f) (alpha * 255).toInt() else alpha.toInt()
                } else 255
                HtmlColor(r, g, b, a.coerceIn(0, 255))
            } catch (_: Exception) { null }
        }

        fun fromName(name: String): HtmlColor? = when (name.lowercase()) {
            "red" -> HtmlColor(255, 0, 0)
            "blue" -> HtmlColor(0, 0, 255)
            "green" -> HtmlColor(0, 128, 0)
            "gray", "grey" -> HtmlColor(128, 128, 128)
            "white" -> HtmlColor(255, 255, 255)
            "black" -> HtmlColor(0, 0, 0)
            "yellow" -> HtmlColor(255, 255, 0)
            "orange" -> HtmlColor(255, 165, 0)
            "purple" -> HtmlColor(128, 0, 128)
            "pink" -> HtmlColor(255, 192, 203)
            "brown" -> HtmlColor(139, 69, 19)
            "cyan" -> HtmlColor(0, 255, 255)
            "magenta" -> HtmlColor(255, 0, 255)
            "silver" -> HtmlColor(192, 192, 192)
            "gold" -> HtmlColor(255, 215, 0)
            "navy" -> HtmlColor(0, 0, 128)
            "teal" -> HtmlColor(0, 128, 128)
            "olive" -> HtmlColor(128, 128, 0)
            "maroon" -> HtmlColor(128, 0, 0)
            else -> null
        }

        fun parse(value: String): HtmlColor? {
            val trimmed = value.trim()
            return when {
                trimmed.startsWith("#") -> fromHex(trimmed)
                trimmed.startsWith("rgb") -> fromRgb(trimmed)
                else -> fromName(trimmed)
            }
        }
    }
}
