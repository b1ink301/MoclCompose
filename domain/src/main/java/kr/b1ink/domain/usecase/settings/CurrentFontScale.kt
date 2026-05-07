package kr.b1ink.domain.usecase.settings

import kr.b1ink.domain.data.SettingRepository
import javax.inject.Inject

class CurrentFontScale @Inject constructor(
    private val repository: SettingRepository
) {
    operator fun invoke() = repository.fontScale
}
