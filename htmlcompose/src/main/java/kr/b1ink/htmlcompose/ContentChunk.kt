package kr.b1ink.htmlcompose

import androidx.compose.ui.text.AnnotatedString

// Helper sealed class to represent different types of content chunks
internal sealed class ContentChunk {
    data class Text(val annotatedString: AnnotatedString) : ContentChunk()
    data class Image(
        val src: String,
        val alt: String,
        val width: Int = -1,
        val height: Int = -1
    ) : ContentChunk()

    data class Video(
        val poster: String,
        val src: String,
        val alt: String,
        val width: Int = -1,
        val height: Int = -1
    ) :
        ContentChunk()

    data class Iframe(
        val src: String,
        val alt: String,
        val width: Int = -1,
        val height: Int = -1
    ) : ContentChunk()
}