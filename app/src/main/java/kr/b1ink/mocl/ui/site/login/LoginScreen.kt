package kr.b1ink.mocl.ui.site.login

import android.annotation.SuppressLint
import android.webkit.CookieManager
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.kevinnzou.web.LoadingState
import com.kevinnzou.web.WebView
import com.kevinnzou.web.rememberWebViewNavigator
import com.kevinnzou.web.rememberWebViewState
import kotlinx.coroutines.launch
import kr.b1ink.mocl.ui.components.AppBarText
import kr.b1ink.mocl.ui.components.ListDivider
import kr.b1ink.mocl.ui.components.ListText
import kr.b1ink.mocl.ui.components.LoadingRow
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onDismiss: (isReload: Boolean) -> Unit,
) {
    val navigator = rememberWebViewNavigator()
    val state = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val onRefresh: () -> Unit = {
        isRefreshing = true
        coroutineScope.launch {
            navigator.reload()
            isRefreshing = false
        }
    }

    Scaffold(
        modifier = Modifier.pullToRefresh(
            state = state, isRefreshing = isRefreshing, onRefresh = onRefresh
        ),
        topBar = {
            AppBarText(
                text = { "로그인" },
                actions = {
                    IconButton(onClick = {
                        onDismiss.invoke(false)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Done, contentDescription = "Done", tint = Color.White
                        )
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier.padding(padding)
        ) {
            val webviewState = rememberWebViewState(url = viewModel.url, additionalHttpHeaders = viewModel.headers)

            WebView(
                state = webviewState,
                navigator = navigator,
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorScheme.background),
                onCreated = {
                    with(it.settings) {
                        @SuppressLint("SetJavaScriptEnabled")
                        javaScriptEnabled = true
                        javaScriptCanOpenWindowsAutomatically = true
                        userAgentString =
                            "Mozilla/5.0 (Linux; Android 9; SM-G950N) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/88.0.4324.93 Mobile Safari/537.36"
                    }
                    val cookieManager = CookieManager.getInstance()
                    cookieManager.acceptCookie()
                    cookieManager.setAcceptThirdPartyCookies(it, true)
                },
            )

            when (webviewState.loadingState) {
                is LoadingState.Loading -> LoadingRow()

                is LoadingState.Finished -> {
                    if (isRefreshing) isRefreshing = false

                    Timber.d("LoadingState.Finished=${webviewState.lastLoadedUrl}")

                    if (viewModel.isLogin(webviewState.lastLoadedUrl)) {
                        onDismiss.invoke(true)
                    }
                    Timber.d("Finished = ${webviewState.lastLoadedUrl}")
                }

                LoadingState.Initializing -> Unit
            }

            if (isRefreshing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), progress = { state.distanceFraction })
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SelectLoginDialog(
    onDismiss: (String) -> Unit
) {
    val loginList = listOf(
        "네이버 로그인" to "https://damoang.net/bbs/login.php?provider=naver&amp;url=https://damoang.net",
        "카카오톡 로그인" to "https://damoang.net/bbs/login.php?provider=kakao&amp;url=https://damoang.net",
        "구글 로그인" to "https://damoang.net/bbs/login.php?provider=google&amp;url=https://damoang.net",
        "페이코 로그인" to "https://damoang.net/bbs/login.php?provider=payco&amp;url=https://damoang.net",
    )
    var shouldShowDialog by remember { mutableStateOf(true) }
    if (shouldShowDialog.not()) return

    Dialog(
        onDismissRequest = {
            onDismiss("about:blank")
            shouldShowDialog = false
        },
        properties = DialogProperties(
            dismissOnClickOutside = false,
            dismissOnBackPress = false,
        ),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(RoundedCornerShape(8.dp))
                .background(colorScheme.background)
        ) {
            stickyHeader {
                ListText(
                    text = { "로그인 선택" },
                    textAlign = TextAlign.Center,
                    color = colorScheme.background,
                    modifier = Modifier
                        .background(colorScheme.primary)
                        .fillMaxWidth()
                        .padding(vertical = 14.dp)
                )
            }
            items(
                items = loginList,
            ) { item ->
                Column {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                onDismiss(item.second)
                                shouldShowDialog = false
                            }, verticalAlignment = Alignment.CenterVertically
                    ) {
                        ListText(
                            text = { item.first }, modifier = Modifier.padding(
                                horizontal = 12.dp, vertical = 14.dp
                            )
                        )
                    }
                    ListDivider()
                }
            }
        }
    }
}