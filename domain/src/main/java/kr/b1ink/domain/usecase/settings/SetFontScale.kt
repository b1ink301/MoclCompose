package kr.b1ink.domain.usecase.settings

import kr.b1ink.domain.data.SettingRepository
import javax.inject.Inject

class SetFontScale @Inject constructor(
    private val repository: SettingRepository
) {
    suspend operator fun invoke(scale: Float) {
        repository.setFontScale(scale)
    }
}
