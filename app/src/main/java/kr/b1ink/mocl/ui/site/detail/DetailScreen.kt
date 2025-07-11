package kr.b1ink.mocl.ui.site.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import kr.b1ink.domain.model.DetailComment
import kr.b1ink.domain.model.DetailData
import kr.b1ink.domain.model.UserInfo
import kr.b1ink.htmlcompose.HtmlRenderer
import kr.b1ink.mocl.ui.components.GifNickImage
import kr.b1ink.mocl.ui.components.HeartWithNumber
import kr.b1ink.mocl.ui.components.ListDivider
import kr.b1ink.mocl.ui.components.LoadingRow
import kr.b1ink.mocl.ui.components.RefreshButton
import kr.b1ink.mocl.util.openCustomTabs
import org.orbitmvi.orbit.compose.collectAsState

const val HEADER_HEIGHT = 48

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: DetailViewModel = hiltViewModel(),
    onReadCallback: (Long) -> Unit,
) {
    val context = LocalContext.current
    var title by rememberSaveable { mutableStateOf(viewModel.title) }
    val smallTitle by rememberSaveable { mutableStateOf(viewModel.smallTitle) }
    val scope = rememberCoroutineScope()

    val uiState by viewModel.collectAsState()
    val listState = rememberLazyListState()
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(state = topAppBarState)

//    viewModel.collectSideEffect {
//    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            DetailTopAppBar(
                scrollBehavior = scrollBehavior,
                smallTitle = smallTitle,
                title = title,
                onRefresh = {
                    scope.launch {
                        listState.scrollToItem(0)
                        viewModel.refresh()
                    }
                },
                onOpenCustomTabs = {
                    viewModel.uri
                        .openCustomTabs(context)
                }
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(top = innerPadding.calculateTopPadding()),
        ) {
            PullToRefreshBox(
                modifier = Modifier
                    .fillMaxSize(),
                isRefreshing = viewModel.isRefreshing,
                onRefresh = viewModel::refresh,
            ) {
                DetailScreenContents(
                    uiState = uiState,
                    listState = listState,
                    contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding()),
                    onUpdateTitle = {
                        if (title != it) title = it
                    },
                    onRefresh = viewModel::refresh,
                    id = viewModel.id,
                ) { itemId ->
                    scope.launch {
                        viewModel.markRead()
                        onReadCallback.invoke(itemId)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailScreenContents(
    modifier: Modifier = Modifier,
    id: Long,
    contentPadding: PaddingValues = PaddingValues(),
    uiState: DetailUiState,
    listState: LazyListState = rememberLazyListState(),
    onUpdateTitle: (String) -> Unit,
    onRefresh: () -> Unit,
    onRead: (Long) -> Unit,
) = LazyColumn(
    contentPadding = contentPadding,
    modifier = modifier
        .padding(start = 16.dp, end = 8.dp),
    verticalArrangement = Arrangement.spacedBy(1.dp),
    state = listState,
) {
    when (uiState) {
        is DetailUiState.Error -> item {
            Text(
                uiState.message ?: "Unknown Error",
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )
            ListDivider()
        }

        DetailUiState.Loading -> item {
            LoadingRow()
            ListDivider()
        }

        is DetailUiState.Success -> {
            val item = uiState.data
            if (item.title.isNotBlank()) onUpdateTitle(item.title)

            header(id, item)
            body(item)
            comments(id, item)
            footer(onRefresh)

            onRead.invoke(id)
        }
    }
}

private fun LazyListScope.header(
    id: Long,
    data: DetailData,
) = stickyHeader(key = id) {
    HeaderText(
        info = data.info,
        nickImage = data.userInfo.nickImage,
        likeCount = data.likeCount,
    )
    ListDivider()
}

private fun LazyListScope.body(
    data: DetailData,
) = item {
    HtmlRenderer(data.bodyHtml)
    ListDivider()
}

private fun LazyListScope.footer(
    onRefresh: (() -> Unit)? = null,
) = item {
    RefreshButton(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRefresh?.invoke() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
    )
    ListDivider()
}

private fun LazyListScope.comments(
    id: Long,
    data: DetailData,
) {
    if (data.comments.isNotEmpty()) {
        stickyHeader(key = "${id}_comment") {
            CommentHeader(commentCount = data.comments.size)
        }
        items(
            items = data.comments,
            key = { it.id },
        ) {
            CommentText(item = it)
        }
    }
}

@Composable
private fun CommentHeader(commentCount: Int) = Column(
    modifier = Modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.background),
) {
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier
            .height(HEADER_HEIGHT.dp)
    ) {
        kr.b1ink.mocl.ui.components.Text(
            text = { "댓글 ($commentCount)" },
            fontColor = MaterialTheme.colorScheme.tertiary,
            fontSize = 15.4.sp,
        )
    }
    ListDivider()
}

@Composable
private fun CommentText(
    item: DetailComment,
) {
    val nickImage = item.userInfo.nickImage
    val info = item.info
    val bodyHtml = item.bodyHtml
    val isReply = item.isReply
    val likeCount = item.likeCount

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (isReply) 16.dp else 0.dp,
                top = 10.dp,
                end = 0.dp,
                bottom = 10.dp
            ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
        ) {
            GifNickImage(url = { nickImage })
            kr.b1ink.mocl.ui.components.Text(
                text = { info },
                fontSize = 15.sp,
                fontColor = MaterialTheme.colorScheme.onTertiary,
                modifier = Modifier.weight(1f)
            )
            HeartWithNumber(
                number = likeCount,
                textSize = 15.sp,
            )
        }

        if (bodyHtml.isNotBlank()) {
            HtmlRenderer(bodyHtml)
        }
    }
    ListDivider()
}

@Composable
fun HeaderText(
    info: String,
    nickImage: String,
    likeCount: String,
) = Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
        .fillMaxWidth()
        .height(HEADER_HEIGHT.dp)
        .background(MaterialTheme.colorScheme.background)
) {
    GifNickImage(
        url = { nickImage },
        height = 17.dp
    )
    kr.b1ink.mocl.ui.components.Text(
        text = { info },
        fontSize = 15.4.sp,
        fontColor = MaterialTheme.colorScheme.onTertiary,
        modifier = Modifier.weight(1f)
    )
    HeartWithNumber(
        number = likeCount,
        textSize = 15.4.sp,
    )
}

@Preview
@Composable
fun CommentTextPreview(
    item: DetailComment = DetailComment(
        id = 0L,
        bodyHtml = """
             <p>(생략)</p>
             <p>
              <br>
             </p>
             <p>대통령실은 이 대통령이 타운홀 미팅을 통해 최근 경기 불황으로 어려움을 겪고 있는 소상공인 및 자영업자의 의견과 정부에 바라는 요구사항을 청취한 뒤 함께 악성 채무 해소 방안을 논의한다고 밝혔다. 또 과학기술계 종사자들과는 과학기술 발전 방향을 논의하고, 지역 주민들의 자유로운 건의를 받을 예정이다.</p>
             <p>
              <br>
             </p>
             <p>행사 참석은 당일 현장에서 지역주민 300여명을 선착순으로 받는다. 주민들의 행사장 입장은 1시부터다. 행사장 입구에는 '대통령에게 바란다' 서식을 배치하고, 모든 참석자가 대통령에게 원하는 의견을 제출할 수 있도록 했다.</p>
             <p>
              <br>
             </p>
             <p>(생략)</p>
             <p>
              <br>
             </p>
             <hr>
             <p>
              <br>
             </p>
             <p>대전 충청 분들 오픈런 가나요?</p>
        """.trimIndent(),
        time = "",
        likeCount = "1",
        info = "닉네임ㆍ어제ㆍ100 읽음",
        userInfo = UserInfo("1", "test")
    )
) = CommentText(item = item)