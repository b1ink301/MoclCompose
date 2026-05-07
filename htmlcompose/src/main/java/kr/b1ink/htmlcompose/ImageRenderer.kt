package kr.b1ink.htmlcompose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
) {
    if (src.isBlank()) return

    Timber.d("[ImageRenderer] src=$src, alt=$alt, width=$originalWidth, height=$originalHeight")

    val uriHandler = LocalUriHandler.current

    BoxWithConstraints(modifier = modifier) {
        val availableWidth = maxWidth.value.toInt()

        val displayWidth =
            remember(
                src,
                availableWidth
            ) { if (originalWidth > 0 && originalWidth > availableWidth) availableWidth else originalWidth }
        val displayHeight = remember(src, availableWidth, displayWidth) {
            if (originalWidth > 0 && originalHeight > 0 && displayWidth > 0) {
                (originalHeight * (displayWidth.toFloat() / originalWidth)).roundToInt()
            } else {
                originalHeight
            }
        }

        val imageModifier = if (displayWidth > 0 && displayHeight > 0) {
            Modifier
                .width(displayWidth.dp)
                .height(displayHeight.dp)
        } else if (displayWidth > 0) {
            Modifier
                .width(displayWidth.dp)
                .heightIn(min = 200.dp)
        } else {
            Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp)
        }

        ProgressAwareAsyncImage(
            src,
            modifier = imageModifier
                .padding(bottom = 8.dp)
                .clickable {
                    uriHandler.openUri(src)
                },
            alignment = Alignment.CenterStart,
            contentScale = ContentScale.FillWidth,
        )
    }
}