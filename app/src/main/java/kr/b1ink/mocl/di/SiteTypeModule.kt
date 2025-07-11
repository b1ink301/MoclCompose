package kr.b1ink.mocl.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kr.b1ink.data.di.coroutine.IoDispatcher
import kr.b1ink.domain.model.SiteType
import kr.b1ink.domain.usecase.settings.CurrentSiteType
import kr.b1ink.domain.usecase.settings.SetSiteType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SiteTypeManager @Inject constructor(
    private val setSiteType: SetSiteType,
    currentSiteType: CurrentSiteType,
    @param:IoDispatcher private val dispatcher: CoroutineDispatcher
) {
    private val _siteTypeState = MutableStateFlow(currentSiteType.invoke())
    val siteTypeState: StateFlow<SiteType> = _siteTypeState.asStateFlow()

    suspend fun updateSiteType(siteType: SiteType) = withContext(dispatcher) {
        setSiteType(siteType)
        _siteTypeState.value = siteType
    }

    val currentSiteType get() = _siteTypeState.value
}
