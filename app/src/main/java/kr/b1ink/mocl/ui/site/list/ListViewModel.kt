package kr.b1ink.mocl.ui.site.list

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kr.b1ink.common.util.fromJson
import kr.b1ink.domain.model.MainItem
import kr.b1ink.domain.model.title
import kr.b1ink.domain.usecase.site.list.GetListDataPager
import kr.b1ink.mocl.EXTRAS_QUERY
import kr.b1ink.mocl.ui.site.model.ListItemWrapper
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getListPager: GetListDataPager,
) : ViewModel(), ContainerHost<Flow<PagingData<ListItemWrapper>>, ListUiSideEffect> {

    private val mainItem: MainItem = requireNotNull(
        savedStateHandle.get<String>(EXTRAS_QUERY)
            ?.fromJson<MainItem>()
    ) { "linkItem is null" }

    private var currentReadId = -1L

    val siteType = mainItem.siteType
    val title = mainItem.title
    val smallTitle = siteType.title()

    var isRefreshing by mutableStateOf(false)
        private set

    fun refresh() = intent {
        isRefreshing = true
        withContext(Dispatchers.IO) { delay(100) }
        isRefreshing = false
        withContext(Dispatchers.IO) { delay(100) }
        postSideEffect(ListUiSideEffect.Refresh)
    }

    fun markRead(readId: Long) = intent {
        if (readId >= 0L && readId != currentReadId) {
            currentReadId = readId
            postSideEffect(ListUiSideEffect.ReadId(readId))
        }
    }

    private val _pagingFlow = getListPager(siteType, mainItem.board)
        .flow
        .catch { e ->
            PagingData.empty<ListItemWrapper>()
        }
        .map { data ->
            data.map { ListItemWrapper(it) }
        }
        .cachedIn(viewModelScope)

    override val container = container<Flow<PagingData<ListItemWrapper>>, ListUiSideEffect>(_pagingFlow)
}