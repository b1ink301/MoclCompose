package kr.b1ink.mocl.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kr.b1ink.data.di.coroutine.IoDispatcher
import kr.b1ink.domain.usecase.settings.CurrentFontScale
import kr.b1ink.domain.usecase.settings.SetFontScale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FontScaleManager @Inject constructor(
    private val setFontScale: SetFontScale,
    currentFontScale: CurrentFontScale,
    @param:IoDispatcher private val dispatcher: CoroutineDispatcher,
) {
    private val _fontScaleState = MutableStateFlow(currentFontScale.invoke().coerceIn(MIN, MAX))
    val fontScaleState: StateFlow<Float> = _fontScaleState.asStateFlow()

    suspend fun updateFontScale(scale: Float) = withContext(dispatcher) {
        val clamped = scale.coerceIn(MIN, MAX)
        setFontScale(clamped)
        _fontScaleState.value = clamped
    }

    val currentFontScale get() = _fontScaleState.value

    companion object {
        const val MIN = 0.8f
        const val MAX = 1.6f
        const val DEFAULT = 1.0f
        const val STEP = 0.05f
    }
}
