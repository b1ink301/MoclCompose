package kr.b1ink.htmlcompose

import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
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
) {
    Timber.d("[VideoRenderer] url=$url, width=$width, height=$height")

    val pool = LocalExoPlayerPool.current
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 풀이 있으면 풀에서 가져오고, 없으면 로컬 생성
    val exoPlayer = if (pool != null) {
        remember(url) { pool.acquire(url) }
    } else {
        remember(url) {
            ExoPlayer.Builder(context)
                .build()
                .apply {
                    playWhenReady = false
                    repeatMode = Player.REPEAT_MODE_ONE
                    setMediaItem(MediaItem.fromUri(url))
                    prepare()
                }
        }
    }

    DisposableEffect(url, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> exoPlayer.pause()
                Lifecycle.Event.ON_RESUME -> { /* 사용자가 직접 재생 */ }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            // 풀이 있으면 pause만 (풀이 lifecycle 관리), 없으면 release
            if (pool != null) {
                exoPlayer.pause()
            } else {
                exoPlayer.release()
            }
        }
    }

    BoxWithConstraints {
        val availableWidth = maxWidth.value.toInt()

        val displayWidth =
            remember(url, availableWidth) { if (width > 0 && width > availableWidth) availableWidth else width }
        val displayHeight = remember(url, availableWidth, displayWidth) {
            if (width > 0 && height > 0 && displayWidth > 0) {
                (height * (displayWidth.toFloat() / width)).roundToInt()
            } else {
                height
            }
        }

        val videoModifier = if (displayWidth > 0 && displayHeight > 0) {
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

        AndroidView(
            factory = { PlayerView(it).apply { player = exoPlayer } },
            modifier = videoModifier.padding(bottom = 8.dp),
        )
    }
}
