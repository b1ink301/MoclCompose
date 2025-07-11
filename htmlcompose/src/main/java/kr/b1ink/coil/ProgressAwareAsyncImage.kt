package kr.b1ink.coil

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade

@Composable
fun ProgressAwareAsyncImage(
    imageUrl: String,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Crop,
    contentDescription: String? = null,
) {
    var progress by remember(imageUrl) { mutableFloatStateOf(0f) }
    val progressId = remember(imageUrl) {
        imageUrl.hashCode()
            .toString()
    }

    DisposableEffect(progressId) {
        ProgressManager.register(progressId) { newProgress ->
            progress = newProgress
        }
        onDispose {
            ProgressManager.unregister(progressId)
        }
    }

    SubcomposeAsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .initProgress(progressId)
            .diskCachePolicy(CachePolicy.ENABLED)
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        modifier = modifier,
        alignment = alignment,
        contentScale = contentScale,
        loading = {
            Box(
                modifier = Modifier
                    .border(
                        0.2.dp,
                        MaterialTheme.colorScheme.secondary,
                        RoundedCornerShape(8.dp)
                    )
                    .fillMaxWidth()
                    .padding(14.dp),
                contentAlignment = Alignment.TopCenter,
            ) {
                Column {
                    Text(
                        text = imageUrl,
                        maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    )
//                    Text(
//                        text = { "%.2f MB / %.2f MB".format(read, total) },
//                        textAlign = TextAlign.End,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        trackColor = MaterialTheme.colorScheme.primary,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }
        },
        success = {
            // 성공 시 painter를 그대로 사용
            SubcomposeAsyncImageContent()
        },
        error = {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Error loading image",
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    )
}