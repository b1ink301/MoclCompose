package kr.b1ink.htmlcompose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import timber.log.Timber

@Composable
fun YoutubeRenderer(
    width: Int,
    height: Int,
    modifier: Modifier = Modifier,
    videoId: () -> String,
) {
    val videoIdString = remember { videoId() }
    val uriHandler = LocalUriHandler.current
    val thumbnailUrl = "https://img.youtube.com/vi/$videoIdString/mqdefault.jpg"
    val youtubeUrl = "https://www.youtube.com/watch?v=$videoIdString"

    Timber.d("YoutubeView videoIdString=$videoIdString, width=$width, height=$height")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .padding(bottom = 8.dp)
            .clickable {
                try {
                    uriHandler.openUri(youtubeUrl)
                } catch (e: Exception) {
                    Timber.e(e, "Failed to open YouTube: $youtubeUrl")
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = thumbnailUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black),
        )

        // 재생 버튼 오버레이
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = Color.White,
                modifier = Modifier.size(36.dp),
            )
        }
    }
}
