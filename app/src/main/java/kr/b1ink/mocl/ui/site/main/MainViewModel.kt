package kr.b1ink.mocl.ui.site.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.b1ink.domain.base.Result
import kr.b1ink.domain.model.SiteType
import kr.b1ink.domain.model.title
import kr.b1ink.domain.usecase.site.main.GetMainData
import kr.b1ink.mocl.di.SiteTypeManager
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getMainData: GetMainData,
    private val siteTypeManager: SiteTypeManager,
) : ViewModel(), ContainerHost<MainUiState, MainUiSideEffect> {
    private val siteTypeState = siteTypeManager.siteTypeState

    override val container = container<MainUiState, MainUiSideEffect>(MainUiState.Loading) {
        viewModelScope.launch {
            siteTypeState.collectLatest { siteType ->
                getMainList(siteType)
                postSideEffect(MainUiSideEffect.ChangeSiteType(siteType))
            }
        }
    }

    val title = siteTypeState.map { it.title() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = siteType.title()
        )

    var isRefreshing by mutableStateOf(false)
        private set

    private var _showAddDialog = MutableStateFlow(false)
    val showAddDialog = _showAddDialog.asStateFlow()

    val siteType get() = siteTypeManager.currentSiteType

    fun isShowActionButtons(siteType: SiteType) =
        siteType == SiteType.Clien || siteType == SiteType.Damoang || siteType == SiteType.Meeco

    private fun getMainList(siteType: SiteType) = intent(registerIdling = false) {
        reduce { MainUiState.Loading }

        val result = getMainData(siteType)
        Timber.d("[getMainList] result: $result")
        when (result) {
            is Result.Error -> reduce {
                MainUiState.Error(result.error)
            }

            is Result.Success -> reduce {
                if (result.data.isEmpty())
                    MainUiState.Empty
                else
                    MainUiState.Success(result.data)
            }

            is Result.LoginError -> reduce {
                MainUiState.NotLogin(result.error)
            }

            else -> Unit
        }

        postSideEffect(MainUiSideEffect.Refresh)
    }

    fun refresh() {
        viewModelScope.launch {
            isRefreshing = true
            getMainList(siteType)
        }
    }

    fun showAddDialog() {
        _showAddDialog.tryEmit(true)
    }

    fun hideAddDialog() {
        _showAddDialog.tryEmit(false)
    }

    fun notLoading() = viewModelScope.launch {
        withContext(Dispatchers.IO) { delay(300) }
        isRefreshing = false
    }
}