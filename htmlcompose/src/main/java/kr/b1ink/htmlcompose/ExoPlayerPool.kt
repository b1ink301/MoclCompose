package kr.b1ink.htmlcompose

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import timber.log.Timber

/**
 * ExoPlayer 풀 매니저.
 * LazyList에서 스크롤 밖으로 나간 아이템의 ExoPlayer를 캐싱하여
 * 다시 보일 때 재생성 없이 재사용.
 *
 * 사용법:
 * 1. 화면(Screen) 레벨에서 rememberExoPlayerPool()로 생성
 * 2. ProvideExoPlayerPool로 하위 트리에 제공
 * 3. VideoRenderer가 자동으로 풀에서 가져옴
 */
class ExoPlayerPool(private val context: Context) {
    private val players = mutableMapOf<String, ExoPlayer>()

    fun acquire(url: String): ExoPlayer {
        return players.getOrPut(url) {
            Timber.d("[ExoPlayerPool] Creating new player for: $url")
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

    fun releaseAll() {
        Timber.d("[ExoPlayerPool] Releasing ${players.size} players")
        players.values.forEach { it.release() }
        players.clear()
    }

    fun pauseAll() {
        players.values.forEach { it.pause() }
    }
}

val LocalExoPlayerPool = staticCompositionLocalOf<ExoPlayerPool?> { null }

@Composable
fun rememberExoPlayerPool(): ExoPlayerPool {
    val context = androidx.compose.ui.platform.LocalContext.current
    val pool = remember { ExoPlayerPool(context.applicationContext) }

    DisposableEffect(Unit) {
        onDispose { pool.releaseAll() }
    }

    return pool
}
