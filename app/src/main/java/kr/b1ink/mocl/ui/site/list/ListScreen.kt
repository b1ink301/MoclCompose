@file:OptIn(ExperimentalMaterial3Api::class)

package kr.b1ink.mocl.ui.site.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import kr.b1ink.domain.model.ListItem
import kr.b1ink.domain.model.SiteType
import kr.b1ink.mocl.ui.components.ListDivider
import kr.b1ink.mocl.ui.components.ListRow
import kr.b1ink.mocl.ui.components.LoadingRow
import kr.b1ink.mocl.ui.site.model.ListItemWrapper
import kr.b1ink.mocl.util.singleClick
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun ListScreen(
    readId: Long,
    viewModel: ListViewModel = hiltViewModel(),
    onClicked: (ListItem, SiteType, String) -> Unit,
) {
    val listState = rememberLazyListState()
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(state = topAppBarState)

    val pagingState by viewModel.collectAsState()
    val lazyPagingItems = pagingState.pagingData.collectAsLazyPagingItems()

    LaunchedEffect(readId) {
        viewModel.markRead(readId)
    }

    viewModel.collectSideEffect { result ->
        when (result) {
            is ListUiSideEffect.ReadId -> {
                lazyPagingItems.itemSnapshotList.find { it?.listItem?.id == result.id }?.isRead?.value = true
            }

            ListUiSideEffect.Refresh -> lazyPagingItems.refresh()
        }
    }

    Scaffold(
//        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ListTopAppBar(
                title = viewModel.title,
                smallTitle = viewModel.smallTitle,
                scrollBehavior = scrollBehavior,
                refreshCallback = viewModel::refresh
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(top = innerPadding.calculateTopPadding()),
        ) {
            PullToRefreshBox(
                modifier = Modifier.fillMaxSize(),
                isRefreshing = remember { pagingState.isRefreshing },
                onRefresh = viewModel::refresh,
            ) {
                ListContents(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                    lazyPagingItems = lazyPagingItems,
                    contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding()),
                    listState = listState,
                    onClicked = {
                        onClicked.invoke(it, viewModel.siteType, viewModel.title)
                    },
                    onRetry = viewModel::refresh
                )
            }
        }
    }
}

@Composable
private fun ListContents(
    modifier: Modifier,
    lazyPagingItems: LazyPagingItems<ListItemWrapper>,
    listState: LazyListState,
    contentPadding: PaddingValues = PaddingValues(),
    onClicked: (ListItem) -> Unit,
    onRetry: () -> Unit
) = LazyColumn(
    contentPadding = contentPadding,
    verticalArrangement = Arrangement.Top,
    modifier = modifier,
    state = listState,
) {
    with(lazyPagingItems.loadState) {
        when {
            refresh is LoadState.Loading -> {
                loadingItem()
            }

            refresh is LoadState.Error -> errorItem(
                message = (refresh as? LoadState.Error)?.error?.message ?: "Unknown Error", onRetry = onRetry
            )

            append is LoadState.Error -> errorItem(
                message = (append as? LoadState.Error)?.error?.message ?: "Unknown Error", onRetry = onRetry
            )

            append is LoadState.Loading -> {
                buildItems(lazyPagingItems, onClicked)
                loadingItem()
            }

            else -> buildItems(lazyPagingItems, onClicked)
        }
    }
}

private fun LazyListScope.buildItems(
    lazyPagingItems: LazyPagingItems<ListItemWrapper>,
    onClicked: (ListItem) -> Unit,
) {
    items(
        count = lazyPagingItems.itemCount,
        key = lazyPagingItems.itemKey { it.listItem.id },
    ) { index ->
        val item = lazyPagingItems[index] ?: return@items
        ListRowContent(item, onClicked)
    }
}

@Composable
private fun ListRowContent(
    item: ListItemWrapper,
    onClicked: (ListItem) -> Unit,
) {
    val listItem = item.listItem
    val isRead by item.isRead
    val textColor = if (isRead) MaterialTheme.colorScheme.onTertiary else null

    Column(
        modifier = Modifier
            .clickable(onClick = singleClick {
                onClicked.invoke(listItem)
            })
            .padding(start = 16.dp, end = 8.dp)
    ) {
        ListRow(
            title = { listItem.title },
            nickImage = listItem.userInfo.nickImage,
            nickName = listItem.userInfo.nickName,
            time = listItem.time,
            hit = listItem.hit,
            reply = { listItem.reply },
            textColor = textColor,
            isRead = isRead,
            modifier = Modifier.padding(vertical = 14.dp)
        )
        ListDivider()
    }
}

private fun LazyListScope.loadingItem() = item {
    Column(modifier = Modifier.padding(start = 16.dp, end = 8.dp)) {
        LoadingRow()
        ListDivider()
    }
}

private fun LazyListScope.errorItem(
    message: String, onRetry: () -> Unit
) = item {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}
