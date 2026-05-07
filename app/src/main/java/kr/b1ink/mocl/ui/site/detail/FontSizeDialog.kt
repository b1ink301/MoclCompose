package kr.b1ink.mocl.ui.site.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.CompositionLocalProvider
import kr.b1ink.mocl.di.FontScaleManager
import kotlin.math.roundToInt

@Composable
fun FontSizeDialog(
    initialScale: Float,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit,
) {
    var scale by rememberSaveable { mutableFloatStateOf(initialScale) }
    val baseDensity = LocalDensity.current
    // 다이얼로그는 MainCompose의 scaled LocalDensity 바깥에서 호출되므로
    // 미리보기 텍스트만 별도로 scale을 적용한다.
    val previewDensity = remember(baseDensity, scale) {
        Density(baseDensity.density, baseDensity.fontScale * scale)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("폰트 크기") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    IconButton(onClick = {
                        scale = (scale - FontScaleManager.STEP)
                            .coerceAtLeast(FontScaleManager.MIN)
                    }) {
                        Icon(Icons.Filled.Remove, contentDescription = "작게")
                    }
                    Text(
                        text = "${(scale * 100).roundToInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    IconButton(onClick = {
                        scale = (scale + FontScaleManager.STEP)
                            .coerceAtMost(FontScaleManager.MAX)
                    }) {
                        Icon(Icons.Filled.Add, contentDescription = "크게")
                    }
                }

                Slider(
                    value = scale,
                    onValueChange = { scale = it },
                    valueRange = FontScaleManager.MIN..FontScaleManager.MAX,
                )

                CompositionLocalProvider(LocalDensity provides previewDensity) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        Text(
                            text = "미리보기",
                            color = MaterialTheme.colorScheme.tertiary,
                            fontSize = 12.sp,
                        )
                        Text(
                            text = "제목 텍스트",
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        Text(
                            text = "닉네임 · 5분 전 · 123 읽음",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onTertiary,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        Text(
                            text = "본문 내용은 이렇게 표시됩니다.",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(scale) }) {
                Text("적용")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = {
                    scale = FontScaleManager.DEFAULT
                }) {
                    Text("기본값")
                }
                TextButton(onClick = onDismiss) {
                    Text("취소")
                }
            }
        },
    )
}
