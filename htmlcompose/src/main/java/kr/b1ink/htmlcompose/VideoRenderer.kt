package kr.b1ink.htmlcompose

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import timber.log.Timber
import kotlin.math.roundToInt

@Composable
fun VideoRenderer(
    url: String,
    width: Int,
    height: Int,
    screenWidth: Int
) {
    Timber.d("[VideoRenderer] url=$url, width=$width, height=$height")
    val context = LocalContext.current
    val exoPlayer = remember(url) {
        ExoPlayer.Builder(context)
            .build()
            .apply {
                playWhenReady = false
                repeatMode = Player.REPEAT_MODE_ONE
                setMediaItem(MediaItem.fromUri(url))
                prepare()
            }
    }

    val displayWidth =
        remember(url) { if (width > 0 && width > screenWidth) screenWidth else width }
    val displayHeight = remember(url) {
        if (width > 0 && height > 0 && displayWidth > 0) {
            (height * (displayWidth.toFloat() / width)).roundToInt()
        } else {
            height
        }
    }

    val modifier = if (displayWidth > 0 && displayHeight > 0) {
        Modifier
            .width(displayWidth.dp)
            .height(displayHeight.dp)
    } else if (displayWidth > 0) {
        Modifier
            .width(displayWidth.dp)
            .wrapContentHeight()
    } else {
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    }

//    val ratio = width.toFloat() / height.toFloat()

    AndroidView(
        factory = { PlayerView(it).apply { player = exoPlayer } },
        modifier = modifier
            .padding(bottom = 8.dp)
    )
    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }
}