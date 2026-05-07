package com.ireward.htmlcompose

import android.graphics.Typeface
import android.text.style.*
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import java.io.File

private const val PATH_SYSTEM_FONTS_FILE = "/system/etc/fonts.xml"
private const val PATH_SYSTEM_FONTS_DIR = "/system/fonts/"

// 시스템 폰트 XML을 한 번만 읽고 캐싱 (메인 스레드 블로킹 방지)
private val systemFontsXml: String? by lazy {
    try {
        java.io.File(PATH_SYSTEM_FONTS_FILE).readText()
    } catch (_: Exception) {
        null
    }
}

fun RelativeSizeSpan.spanStyle(fontSize: TextUnit): SpanStyle =
    SpanStyle(fontSize = (fontSize.value * sizeChange).sp)

fun StyleSpan.spanStyle(): SpanStyle? = when (style) {
    Typeface.BOLD -> SpanStyle(fontWeight = FontWeight.Bold)
    Typeface.ITALIC -> SpanStyle(fontStyle = FontStyle.Italic)
    Typeface.BOLD_ITALIC -> SpanStyle(
        fontWeight = FontWeight.Bold,
        fontStyle = FontStyle.Italic,
    )
    else -> null
}

fun SubscriptSpan.spanStyle(): SpanStyle =
    SpanStyle(baselineShift = BaselineShift.Subscript)

fun SuperscriptSpan.spanStyle(): SpanStyle =
    SpanStyle(baselineShift = BaselineShift.Superscript)

fun TypefaceSpan.spanStyle(): SpanStyle? {
    val xmlContent = systemFontsXml ?: return null
    return if (xmlContent.contains("""<family name="$family""")) {
        val familyChunkXml = xmlContent.substringAfter("""<family name="$family""")
            .substringBefore("""</family>""")
        val fontName = familyChunkXml.substringAfter("""<font weight="400" style="normal">""")
            .substringBefore("</font>")
        try {
            SpanStyle(fontFamily = FontFamily(Typeface.createFromFile("$PATH_SYSTEM_FONTS_DIR$fontName")))
        } catch (_: Exception) {
            null
        }
    } else {
        null
    }
}