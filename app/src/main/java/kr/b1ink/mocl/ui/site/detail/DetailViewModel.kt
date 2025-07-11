package kr.b1ink.mocl.ui.site.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.b1ink.common.util.decodeBase64String
import kr.b1ink.common.util.fromJson
import kr.b1ink.domain.base.Result
import kr.b1ink.domain.model.ListItem
import kr.b1ink.domain.model.SiteType
import kr.b1ink.domain.model.title
import kr.b1ink.domain.usecase.site.MarkRead
import kr.b1ink.domain.usecase.site.detail.GetDetailData
import kr.b1ink.mocl.EXTRAS_QUERY
import kr.b1ink.mocl.EXTRAS_SITE_TYPE
import kr.b1ink.mocl.ui.site.detail.DetailUiState.Error
import kr.b1ink.mocl.ui.site.detail.DetailUiState.Success
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val _getDetailData: GetDetailData,
    private val _markRead: MarkRead,
) : ViewModel(), ContainerHost<DetailUiState, DetailUiSideEffect> {
    override val container: Container<DetailUiState, DetailUiSideEffect> = container(DetailUiState.Loading) {
        reqDetailData()
    }

    private val siteType: SiteType =
        savedStateHandle.get<SiteType>(EXTRAS_SITE_TYPE) ?: throw IllegalStateException("siteType is null")

    private val listItem = savedStateHandle.get<String>(EXTRAS_QUERY)
        ?.fromJson<ListItem>() ?: throw IllegalStateException("ListItem is null")

    val title = listItem.title.decodeBase64String()
    val smallTitle = "${siteType.title()} > ${listItem.boardTitle}"
    val id = listItem.id
    val uri = listItem.url.toUri()

    var isRefreshing by mutableStateOf(false)
        private set

    fun refresh() = viewModelScope.launch {
        isRefreshing = true
        withContext(Dispatchers.IO) { delay(50) }
        isRefreshing = false
        withContext(Dispatchers.IO) { delay(100) }
        reqDetailData()
    }

    private fun reqDetailData() = intent(registerIdling = false) {
        reduce { DetailUiState.Loading }

        when (val result = _getDetailData(siteType, listItem.id, listItem.board)) {
            is Result.Error -> reduce {
                Error(result.error)
            }

            Result.Loading -> Unit
            Result.NetworkError -> reduce {
                Error("NetworkError")
            }

            is Result.Success -> reduce {
                Success(result.data)
            }

            is Result.LoginError -> Error(result.error)
        }
    }

    suspend fun markRead() = withContext(Dispatchers.Default) {
        _markRead.invoke(siteType = siteType, id = listItem.id)
    }
}