package kr.b1ink.mocl.ui.site.list

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kr.b1ink.mocl.ui.site.model.ListItemWrapper

data class ListUiState(
    val pagingData: Flow<PagingData<ListItemWrapper>>,
    val isRefreshing: Boolean = false
)
