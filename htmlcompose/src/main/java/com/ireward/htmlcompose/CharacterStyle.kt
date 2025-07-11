package com.ireward.htmlcompose

import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.UnderlineSpan
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextDecoration

fun UnderlineSpan.spanStyle(): SpanStyle =
    SpanStyle(textDecoration = TextDecoration.Underline)

fun ForegroundColorSpan.spanStyle(): SpanStyle =
    SpanStyle(color = Color(foregroundColor))

fun StrikethroughSpan.spanStyle(): SpanStyle =
    SpanStyle(textDecoration = TextDecoration.LineThrough)