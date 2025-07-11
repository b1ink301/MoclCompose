package kr.b1ink.mocl.ui.site.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kr.b1ink.domain.base.Result
import kr.b1ink.domain.model.MainItem
import kr.b1ink.domain.model.SiteType
import kr.b1ink.domain.usecase.site.main.GetMainDataFromJson
import kr.b1ink.domain.usecase.site.main.SetMainData
import kr.b1ink.mocl.di.SiteTypeManager
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class AddDialogViewModel @Inject constructor(
    private val getMainDataFromJson: GetMainDataFromJson,
    private val setMainData: SetMainData,
    siteTypeManager: SiteTypeManager
) : ViewModel(), ContainerHost<AddDialogUiState, AddDialogUiSideEffect> {
    override val container = container<AddDialogUiState, AddDialogUiSideEffect>(AddDialogUiState.Loading) {
        viewModelScope.launch {
            _siteTypeState.collectLatest {
                getAllLinks(it)
            }
        }
    }

    private val _selectedItems = MutableStateFlow<Set<MainItem>>(emptySet())
    val selectedItems = _selectedItems.asStateFlow()

    private val _siteTypeState = siteTypeManager.siteTypeState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = siteTypeManager.siteTypeState.value
    )

    private fun getAllLinks(siteType: SiteType) = intent(registerIdling = false) {
        reduce { AddDialogUiState.Loading }

        val result = getMainDataFromJson(siteType)

        when (result) {
            is Result.Error -> reduce { AddDialogUiState.Error(result.error) }
            Result.Loading -> Unit
            Result.NetworkError -> reduce { AddDialogUiState.Error("NetworkError") }
            is Result.Success -> {
                val data = result.data
                _selectedItems.value = result.data.filter { it.hasRoom }
                    .toSet()
                reduce { AddDialogUiState.Success(data) }
            }

            is Result.LoginError -> Unit
        }
    }

    fun toggleSelection(item: MainItem, isSelected: Boolean) {
        _selectedItems.update { currentSelected ->
            if (isSelected) {
                currentSelected + item
            } else {
                currentSelected - item
            }
        }
    }

    fun addLinks() = intent {
        try {
            setMainData(_siteTypeState.value, _selectedItems.value.map {
                it.copy(hasRoom = true)
            })
            postSideEffect(AddDialogUiSideEffect.OkDialog)
        } catch (e: Exception) {
            reduce { AddDialogUiState.Error(e.message ?: "Failed to add links") }
        }
    }

    fun cancelDialog() = intent {
        postSideEffect(AddDialogUiSideEffect.CancelDialog)
    }
}