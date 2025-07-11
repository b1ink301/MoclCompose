package com.ireward.htmlcompose

import androidx.compose.ui.text.AnnotatedString

sealed class ContentItem {
    data class TextContent(val text: AnnotatedString) : ContentItem()
    data class ImageContent(val url: String) : ContentItem()
    data class VideoContent(val url: String) : ContentItem()
}