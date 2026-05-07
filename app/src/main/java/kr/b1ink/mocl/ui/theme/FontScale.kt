package kr.b1ink.mocl.ui.theme

import androidx.compose.runtime.compositionLocalOf

/**
 * 사용자가 설정한 폰트 배율(0.8 ~ 1.6).
 * 실제 sp 스케일링은 [androidx.compose.ui.platform.LocalDensity]를
 * `Density(density, fontScale = baseFontScale * userFontScale)` 로 override 하여 적용한다.
 */
val LocalFontScale = compositionLocalOf { 1.0f }
