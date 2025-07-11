package kr.b1ink.htmlcompose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import kr.b1ink.coil.ProgressAwareAsyncImage
import timber.log.Timber
import kotlin.math.roundToInt

@Composable
fun ImageRenderer(
    modifier: Modifier = Modifier,
    src: String,
    alt: String,
    originalWidth: Int,
    originalHeight: Int,
    screenWidth: Int
) {
    Timber.d("[ImageRenderer] src=$src, alt=$alt, width=$originalWidth, height=$originalHeight")

    val uriHandler = LocalUriHandler.current

    val displayWidth =
        remember(src) { if (originalWidth > 0 && originalWidth > screenWidth) screenWidth else originalWidth }
    val displayHeight = remember(src) {
        if (originalWidth > 0 && originalHeight > 0 && displayWidth > 0) {
            (originalHeight * (displayWidth.toFloat() / originalWidth)).roundToInt()
        } else {
            originalHeight
        }
    }

    val modifier = if (displayWidth > 0 && displayHeight > 0) {
        modifier
            .width(displayWidth.dp)
            .height(displayHeight.dp)
    } else if (displayWidth > 0) {
        modifier
            .width(displayWidth.dp)
            .wrapContentHeight()
    } else {
        modifier.fillMaxWidth()
    }

    if (src.isNotBlank()) {
        ProgressAwareAsyncImage(
            src,
            modifier = modifier
                .padding(bottom = 8.dp)
                .clickable {
                    uriHandler.openUri(src)
                },
            alignment = Alignment.CenterStart,
            contentScale = ContentScale.FillWidth,
        )
    }
}