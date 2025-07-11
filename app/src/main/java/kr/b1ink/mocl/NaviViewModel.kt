package kr.b1ink.mocl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kr.b1ink.domain.model.SiteType
import kr.b1ink.mocl.di.SiteTypeManager
import javax.inject.Inject

@HiltViewModel
class NaviViewModel @Inject constructor(
    private val siteTypeManager: SiteTypeManager
) : ViewModel() {
    val siteState = siteTypeManager.siteTypeState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = siteTypeManager.currentSiteType
        )

    fun setSiteType(siteType: SiteType) = viewModelScope.launch {
        siteTypeManager.updateSiteType(siteType)
    }
}