package kr.b1ink.data.setting

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kr.b1ink.data.di.coroutine.IoDispatcher
import kr.b1ink.domain.data.SettingRepository
import kr.b1ink.domain.model.SiteType
import javax.inject.Inject

class SettingRepositoryImpl @Inject constructor(
    private val dataStore: SettingDataStore,
    @param:IoDispatcher private val dispatcher: CoroutineDispatcher,
) : SettingRepository {
    override val siteType: SiteType = dataStore.siteType

    override suspend fun setSiteType(type: SiteType) = withContext(dispatcher) {
        dataStore.siteType = type
    }
}