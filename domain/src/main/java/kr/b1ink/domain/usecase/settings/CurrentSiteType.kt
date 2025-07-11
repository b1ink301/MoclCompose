package kr.b1ink.domain.usecase.settings

import kr.b1ink.domain.data.SettingRepository
import javax.inject.Inject

class CurrentSiteType @Inject constructor(
    private val repository: SettingRepository
) {
    operator fun invoke() = repository.siteType
}