@file:OptIn(ExperimentalMaterial3Api::class)

package kr.b1ink.mocl.ui.site.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kr.b1ink.domain.model.MainItem
import kr.b1ink.domain.model.SiteType
import kr.b1ink.mocl.ui.components.AppBarText
import kr.b1ink.mocl.ui.components.ListDivider
import kr.b1ink.mocl.ui.components.ListText
import kr.b1ink.mocl.ui.components.LoadingRow
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun MainScreen(
    isReloadData: Boolean,
    openDrawer: () -> Unit,
    viewModel: MainViewModel = hiltViewModel(),
    onLogin: (SiteType) -> Unit,
    onClicked: (MainItem) -> Unit,
) {
    val showAddDialog by viewModel.showAddDialog.collectAsState()
    val uiState by viewModel.collectAsState()
    val title by viewModel.title.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var showActionButtons by rememberSaveable { mutableStateOf(true) }
    val listState: LazyListState = rememberLazyListState()

    LaunchedEffect(isReloadData) {
        if (isReloadData) {
            viewModel.refresh()
        }
    }

    viewModel.collectSideEffect {
        when (it) {
            is MainUiSideEffect.ChangeSiteType -> {
                showActionButtons = viewModel.isShowActionButtons(it.type)
                scrollBehavior.state.heightOffset = 0f
                listState.scrollToItem(0)
            }

            MainUiSideEffect.Refresh -> viewModel.notLoading()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
//        contentWindowInsets = WindowInsets.statusBars.only(WindowInsetsSides.Top),
        topBar = {
            MainAppBar(
                title = title,
                onDrawer = openDrawer,
                onLogin = {
                    onLogin.invoke(viewModel.siteType)
                },
                onAddDialog = viewModel::showAddDialog,
                scrollBehavior = scrollBehavior,
                showActionButtons = showActionButtons,
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(top = innerPadding.calculateTopPadding()),
        ) {
            MainContents(
                uiState = uiState,
                bottomPadding = innerPadding.calculateBottomPadding(),
                listState = listState,
                isRefreshing = viewModel.isRefreshing,
                onClicked = onClicked,
                onLogin = {
                    onLogin.invoke(viewModel.siteType)
                },
                onRefresh = viewModel::refresh
            )
        }
    }

    if (showAddDialog) {
        AddDialog { isRefresh ->
            if (isRefresh) {
                viewModel.refresh()
            }
            viewModel.hideAddDialog()
        }
    }
}

@Composable
private fun MainContents(
    modifier: Modifier = Modifier,
    uiState: MainUiState,
    bottomPadding: Dp,
    listState: LazyListState,
    isRefreshing: Boolean = false,
    onClicked: (MainItem) -> Unit,
    onLogin: () -> Unit,
    onRefresh: () -> Unit,
) {
    PullToRefreshBox(
        modifier = modifier
            .fillMaxSize(),
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
    ) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(bottom = bottomPadding),
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
        ) {
            when (uiState) {
                is MainUiState.Error -> buildError(uiState.message)

                MainUiState.Loading -> buildLoading()

                is MainUiState.Success -> buildSuccess(
                    list = uiState.list,
                    onCallback = onClicked
                )

                MainUiState.Empty -> buildEmptyView()

                is MainUiState.NotLogin -> {
                    buildError(uiState.message)
                    onLogin.invoke()
                }
            }
        }
    }
}

private fun LazyListScope.buildEmptyView() = item {
    Column(modifier = Modifier.padding(start = 16.dp, end = 8.dp)) {
        Text(
            text = "항목이 없습니다",
            modifier = Modifier.padding(vertical = 16.dp)
        )
        ListDivider()
    }
}

private fun LazyListScope.buildError(
    message: String?,
) = item {
    Column(modifier = Modifier.padding(start = 16.dp, end = 8.dp)) {
        Text(
            text = message ?: "Unknown Error",
            modifier = Modifier.padding(vertical = 16.dp)
        )
        ListDivider()
    }
}

private fun LazyListScope.buildLoading() = item {
    Column(modifier = Modifier.padding(start = 16.dp, end = 8.dp)) {
        LoadingRow()
        ListDivider()
    }
}

private fun LazyListScope.buildSuccess(
    list: List<MainItem>,
    onCallback: (MainItem) -> Unit,
) = itemsIndexed(
    items = list,
    key = { _, item -> item.id }
) { index, item ->
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .clickable {
                onCallback.invoke(item)
            }
            .padding(start = 16.dp, end = 8.dp)
    ) {
        ListText(
            text = { item.title },
            fontSize = 17.sp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("MainScreenContents:Item$index"),
            maxLines = 1,
        )
    }
    ListDivider(start = 16.dp, end = 8.dp)
}

@Composable
private fun MainAppBar(
    title: String,
    onDrawer: () -> Unit,
    onLogin: () -> Unit,
    onAddDialog: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    showActionButtons: Boolean = true,
) = AppBarText(
    text = { title },
    navigationIcon = {
        IconButton(onClick = onDrawer) {
            Icon(
                imageVector = Icons.Filled.Menu,
                contentDescription = "Menu",
                tint = Color.White
            )
        }
    },
    actions = {
        if (showActionButtons) {
            IconButton(onClick = onAddDialog) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add",
                    tint = Color.White
                )
            }
        }
    },
    moreMenuList = listOf("로그인"),
    moreMenuClick = {
        when (it) {
            0 -> onLogin()
            else -> Unit
        }
    },
    scrollBehavior = scrollBehavior,
)
