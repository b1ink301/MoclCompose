package kr.b1ink.domain.usecase.settings

import kr.b1ink.domain.data.SettingRepository
import kr.b1ink.domain.model.SiteType
import javax.inject.Inject

class SetSiteType @Inject constructor(
    private val repository: SettingRepository
){
    suspend operator fun invoke(siteType: SiteType) {
        repository.setSiteType(siteType)
    }
}