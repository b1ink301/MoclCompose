package kr.b1ink.mocl.util

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import kotlinx.coroutines.flow.Flow
import java.io.Serializable
import java.util.Base64
import java.util.regex.Matcher
import java.util.regex.Pattern

fun String.isYoutubeUrl(): Boolean = try {
    val uri = toUri()
    val scheme = uri.scheme ?: return false
    val host = uri.host ?: return false
    scheme.startsWith("http") && (host == "youtu.be" || host.endsWith(".youtube.com"))
} catch (_: Exception) {
    false
}

fun String.getYoutubeVideoId(): String {
    var videoId = ""
    val regex =
        "http(?:s)?:\\/\\/(?:m.)?(?:www\\.)?youtu(?:\\.be\\/|(?:be-nocookie|be)\\.com\\/(?:watch|[\\w]+\\?(?:feature=[\\w]+.[\\w]+\\&)?v=|v\\/|e\\/|embed\\/|user\\/(?:[\\w#]+\\/)+))([^&#?\\n]+)"
    val pattern: Pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE)
    val matcher: Matcher = pattern.matcher(this)
    if (matcher.find()) {
        videoId = matcher.group(1) as String
    }
    return videoId
}

fun LazyListScope.gridItems(
    count: Int,
    nColumns: Int,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    itemContent: @Composable BoxScope.(Int) -> Unit,
) {
    gridItems(
        data = List(count) { it },
        nColumns = nColumns,
        horizontalArrangement = horizontalArrangement,
        itemContent = itemContent,
    )
}

fun <T> LazyListScope.gridItems(
    data: List<T>,
    nColumns: Int,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    key: ((item: T) -> Any)? = null,
    itemContent: @Composable BoxScope.(T) -> Unit,
) {
    val rows = if (data.isEmpty()) 0 else 1 + (data.count() - 1) / nColumns
    items(rows) { rowIndex ->
        Row(horizontalArrangement = horizontalArrangement) {
            for (columnIndex in 0 until nColumns) {
                val itemIndex = rowIndex * nColumns + columnIndex
                if (itemIndex < data.count()) {
                    val item = data[itemIndex]
                    androidx.compose.runtime.key(key?.invoke(item)) {
                        Box(
                            modifier = Modifier.weight(1f, fill = true),
                            propagateMinConstraints = true
                        ) {
                            itemContent.invoke(this, item)
                        }
                    }
                } else {
                    Spacer(Modifier.weight(1f, fill = true))
                }
            }
        }
    }
}

@Composable
fun Dp.dpToPx() = with(LocalDensity.current) { this@dpToPx.toPx() }

@Composable
fun Dp.dpRoundToPx() = with(LocalDensity.current) { this@dpRoundToPx.roundToPx() }

@Composable
fun Int.pxToDp() = with(LocalDensity.current) { this@pxToDp.toDp() }

fun <T : Any> LazyListScope.newItems(
    pagingItems: LazyPagingItems<T>,
    itemContent: @Composable () -> Unit,
) {

    val refresh = pagingItems.loadState.refresh
    val append = pagingItems.loadState.append

    if (refresh is LoadState.NotLoading && append is LoadState.NotLoading) {
        if (pagingItems.itemCount == 0) {
            item {
                itemContent()
            }
        }
    } else {
        item {
//            LoadStateView(path = FOLLOW_WIND_LIST, refresh = refresh, append = append) {
//                pagingItems.retry()
//            }
        }
    }

}

@Suppress("DEPRECATION")
inline fun <reified T : Serializable> Bundle.getSerializableCompat(key: String): T? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getSerializable(key, T::class.java)
    } else {
        getSerializable(key) as? T
    }

fun String.encodeBase64String(): String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    Base64.getEncoder()
        .encodeToString(toByteArray())
} else {
    android.util.Base64.encodeToString(toByteArray(), android.util.Base64.NO_WRAP)
}

fun String.decodeBase64String(): String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    String(
        Base64.getDecoder()
            .decode(this)
    )
} else {
    String(android.util.Base64.decode(this, android.util.Base64.NO_WRAP))
}

fun singleClick(onClick: () -> Unit): () -> Unit {
    var latest: Long = 0
    return {
        val now = System.currentTimeMillis()
        if (now - latest >= 300) {
            onClick()
            latest = now
        }
    }
}

@Composable
fun <T> Flow<T>.collectAsStateWithLifecycleRemember(
    initial: T,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
): State<T> {
    val lifecycleOwner = LocalLifecycleOwner.current
    val flowLifecycleAware = remember(this, lifecycleOwner) {
        flowWithLifecycle(lifecycleOwner.lifecycle, minActiveState)
    }
    return flowLifecycleAware.collectAsState(initial)
}

@Composable
fun Lifecycle.observeAsState(): State<Lifecycle.Event> {
    val state = remember { mutableStateOf(Lifecycle.Event.ON_ANY) }
    DisposableEffect(this) {
        val observer = LifecycleEventObserver { _, event ->
            state.value = event
        }
        this@observeAsState.addObserver(observer)

        onDispose {
            this@observeAsState.removeObserver(observer)
        }
    }
    return state
}

fun Uri.openCustomTabs(context: Context) {
    val customTabsIntent = CustomTabsIntent.Builder()
        .setShowTitle(false)
        .build()

    customTabsIntent.launchUrl(context, this)
}

@Composable
fun Modifier.paddingValues(
    start: () -> Dp = { 0.dp },
    top: () -> Dp = { 0.dp },
    end: () -> Dp = { 0.dp },
    bottom: () -> Dp = { 0.dp }
): Modifier {
    val padding = remember {
        object : PaddingValues {
            override fun calculateTopPadding(): Dp = top()

            override fun calculateLeftPadding(layoutDirection: LayoutDirection): Dp {
                return if (layoutDirection == LayoutDirection.Ltr) start() else end()
            }

            override fun calculateRightPadding(layoutDirection: LayoutDirection): Dp {
                return if (layoutDirection == LayoutDirection.Ltr) end() else start()
            }

            override fun calculateBottomPadding(): Dp = bottom()
        }
    }
    return padding(padding)
}