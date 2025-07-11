package kr.b1ink.htmlcompose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import timber.log.Timber

@Composable
fun YoutubeView(
    width: Int,
    height: Int,
    modifier: Modifier = Modifier,
    videoId: () -> String,
) {
    val videoIdString = remember { videoId() }
    val uriHandler = LocalUriHandler.current
    val urlThumbnailString = "https://img.youtube.com/vi/$videoIdString/mqdefault.jpg"
    var isSuccess by remember { mutableStateOf(false) }
    val screenWidth = LocalConfiguration.current.screenWidthDp

    Timber.d("YoutubeView videoIdString=$videoIdString, width=$width, height=$height")

    Box(
        contentAlignment = Alignment.Center,
    ) {
        ImageRenderer(
            modifier = modifier.then(
                if (width > 0 && height > 0) {
                    Modifier.size(
                        with(LocalDensity.current) { width.dp },
                        with(LocalDensity.current) { height.dp },
                    )
                } else {
                    Modifier
                        .fillMaxWidth(0.9f)
                        .aspectRatio(0.6f)
                }
            ),
            src = urlThumbnailString,
            alt = "",
            originalWidth = -1,
            originalHeight = -1,
            screenWidth = screenWidth,
        )
//        {
//                uriHandler.openUri("https://www.youtube.com/watch?v=$videoIdString")
//        }

        if (isSuccess) {
//            Icon(
//                painter = painterResource(R.drawable.baseline_play_circle_outline_24),
//                tint = MaterialTheme.colorScheme.tertiary,
//                modifier = Modifier.size(140.dp),
//                contentDescription = null,
//            )
        }
    }
}