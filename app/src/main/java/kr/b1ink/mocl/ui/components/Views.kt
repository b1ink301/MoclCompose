@file:OptIn(ExperimentalMaterial3Api::class)

package kr.b1ink.mocl.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.LazyPagingItems
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kr.b1ink.mocl.R
import timber.log.Timber

@Composable
fun AppBarText(
    text: () -> String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.primary, scrolledContainerColor = MaterialTheme.colorScheme.primary
    ),
//    hasMoreMenu: Boolean = false,
    moreMenuList: List<String> = emptyList(),
    moreMenuClick: (Int) -> Unit = {},
//    moreMenuContent: @Composable ColumnScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    var moreMenuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text(
                text = text(),
                fontSize = 16.4.sp,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color.White,
                ),
                maxLines = Int.MAX_VALUE,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
//                    .fillMaxSize()
                    .padding(end = 2.dp, top = 4.dp, bottom = 4.dp)
                    .wrapContentHeight(align = Alignment.CenterVertically),
            )
        },
        colors = colors,
        actions = {
            actions.invoke(this)
            if (moreMenuList.isNotEmpty()) {
                IconButton(onClick = { moreMenuExpanded = !moreMenuExpanded }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "More options",
                        tint = Color.White
                    )
                }
                DropdownMenu(
                    expanded = moreMenuExpanded,
                    onDismissRequest = { moreMenuExpanded = false },
                    content = {
                        moreMenuList.forEachIndexed { index, it ->
                            DropdownMenuItem(
                                text = { Text(it) },
                                onClick = {
                                    moreMenuExpanded = false
                                    moreMenuClick.invoke(index)
                                },
                            )
                        }
                    },
                )
            }
        },
        modifier = modifier,
        navigationIcon = navigationIcon,
        windowInsets = windowInsets,
        scrollBehavior = scrollBehavior,
    )
}

@Composable
@Stable
fun ListDivider(start: Dp = 0.dp, end: Dp = 0.dp) {
    HorizontalDivider(
        modifier = Modifier.padding(
            start = start,
            end = end
        ), color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
fun CustomAppBar(
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        shadowElevation = 8.dp, modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = text,
                fontSize = 17.sp,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clickable { /* Handle text click if needed */ },
                overflow = TextOverflow.Ellipsis
            )
            // ... (Navigation icon, actions, etc.)
        }
    }
}


@Composable
fun LoadingRow(
    modifier: Modifier = Modifier,
) = Row(
    modifier = modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 16.dp),
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically,
) {
    CircularProgressIndicator(color = MaterialTheme.colorScheme.tertiary)
}

@Composable
fun ListRow(
    modifier: Modifier = Modifier,
    title: () -> String,
    info: () -> String,
    image: () -> String,
    reply: () -> String,
    textColor: Color?,
) {
    Column(
        modifier = modifier.testTag("ListScreenContents:Item"),
    ) {
        ListText(
            text = title,
            fontSize = 17.sp,
            color = textColor,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        )
        if (info().isNotEmpty()) {
            Spacer(modifier = Modifier.size(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
            ) {
                GifNickImage(url = image)
                Text(
                    text = info,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 2.dp)
                )
                TextWithRoundBorder(
                    text = reply, fontSize = 12.sp, modifier = Modifier
                        .wrapContentHeight()
                        .padding(end = 2.dp)
                )
            }
        }
    }
}

@Composable
fun Text(
    text: () -> String,
    modifier: Modifier = Modifier,
    fontColor: Color? = null,
    fontSize: TextUnit = 13.sp,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
) {
    if (text().isBlank() || text() == "0" || text() == "+0" || text() == "+") return

    Text(
        text = text(),
        fontSize = fontSize,
        textAlign = textAlign,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.bodyMedium.copy(
            color = fontColor ?: MaterialTheme.colorScheme.onTertiary
        ),
        maxLines = maxLines,
        modifier = modifier,
    )
}

@Composable
fun TextWithRoundBorder(
    modifier: Modifier = Modifier,
    text: () -> String?,
    fontColor: Color? = null,
    fontSize: TextUnit = 16.sp,
    borderColor: Color = MaterialTheme.colorScheme.secondary,
) {
    if (text().isNullOrBlank() || text() == "0") return

    Text(
        text = text().toString(),
        fontSize = fontSize,
        style = if (fontSize < 15.sp) MaterialTheme.typography.bodySmall.copy(
            color = fontColor ?: MaterialTheme.colorScheme.onTertiary,
        ) else MaterialTheme.typography.bodyMedium.copy(
            color = fontColor ?: MaterialTheme.colorScheme.onTertiary,
        ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .border(0.2.dp, borderColor, CircleShape)
            .padding(5.dp, 1.dp)
    )
}

@Composable
fun GifNickImage(
    url: () -> String?,
    height: Dp = 16.dp,
    modifier: Modifier = Modifier,
) {
    val model = url()
    if (model.isNullOrBlank()) return
    AsyncImage(
        model = model,
        modifier = modifier
            .height(height)
            .padding(end = 8.dp),
        contentScale = ContentScale.FillHeight,
        contentDescription = null,
        error = painterResource(R.drawable.baseline_error_24),
        onError = {}
    )
}

fun Modifier.circleLayout() = layout { measurable, constraints ->
    // Measure the composable
    val placeable = measurable.measure(constraints)

    //get the current max dimension to assign width=height
    val currentHeight = placeable.height
    val currentWidth = placeable.width
    val newDiameter = maxOf(currentHeight, currentWidth)

    //assign the dimension and the center position
    layout(newDiameter, newDiameter) {
        // Where the composable gets placed
        placeable.placeRelative((newDiameter - currentWidth) / 2, (newDiameter - currentHeight) / 2)
    }
}

@Composable
fun <T : Any> LazyPagingItems<T>.rememberLazyListState(): LazyListState {
    // After recreation, LazyPagingItems first return 0 items, then the cached items.
    // This behavior/issue is resetting the LazyListState scroll position.
    // Below is a workaround. More info: https://issuetracker.google.com/issues/177245496.
    return when (itemCount) {
        // Return a different LazyListState instance.
        0 -> remember(this) { LazyListState(0, 0) }
        // Return rememberLazyListState (normal case).
        else -> androidx.compose.foundation.lazy.rememberLazyListState()
    }
}


@Composable
fun RefreshActionButton(isLoading: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick, enabled = isLoading.not()) {
        Icon(
            imageVector = Icons.Filled.Refresh,
            contentDescription = "Refresh",
            tint = if (isLoading.not()) Color.White else Color.Gray
        )
    }
}

@Composable
fun RefreshButton(modifier: Modifier) {
    val refresh = stringResource(id = R.string.refresh)
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = { refresh },
            fontSize = 15.sp,
            fontColor = MaterialTheme.colorScheme.tertiary,
        )
    }
}

@Composable
fun DetailImageCoil(
    urlString: () -> String,
    modifier: Modifier = Modifier,
    onSuccess: (() -> Unit)? = null,
    onClicked: (() -> Unit)? = null,
) {
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(urlString())
            .crossfade(true)
            // Coil 3에서는 SizeResolver를 명시적으로 지정하거나,
            // Modifier에서 size를 제약하여 Coil이 추론하도록 할 수 있습니다.
            // 여기서는 Modifier.fillMaxWidth()가 있으므로 Coil이 너비를 알 수 있습니다.
            // 높이가 가변적이라면, 원본 사이즈를 사용하도록 설정할 수 있습니다.
            // .size(SizeResolver(coil3.size.Size.ORIGINAL)) // 필요에 따라 추가
            .build(),
        modifier = modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth(),
        contentDescription = null,
        // Coil 3에서는 SubcomposeAsyncImage의 content 콜백에서 painter를 직접 받지 않고,
        // loading, success, error 슬롯을 사용합니다.
        loading = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                contentAlignment = Alignment.Center, // 보통 로딩은 중앙 정렬을 많이 사용합니다.
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onTertiary
                )
            }
        },
        success = { painterState -> // AsyncImagePainter.State.Success 상태일 때 호출
            onSuccess?.invoke() // 성공 콜백 호출

            val painter = painterState.painter
            val intrinsicSize = painter.intrinsicSize

            // 화면 너비 및 이미지 원본 크기 계산 로직은 유지
            val configuration = LocalConfiguration.current
            // Coil 3의 intrinsicSize는 이미 Density를 고려한 픽셀 단위일 수 있으므로,
            // 직접적인 DPI 계산보다는 painter의 크기를 활용하는 것이 더 안정적일 수 있습니다.
            // 다만, Coil 2와 동일한 로직을 유지하기 위해 dpi 계산을 포함했습니다.
            // 필요에 따라 이 부분을 테스트하고 조정하세요.
            val density = LocalContext.current.resources.displayMetrics.density
            val imagePixelWidth = intrinsicSize.width
            val imagePixelHeight = intrinsicSize.height

            val imageDpWidth = (imagePixelWidth / density).dp
            val screenWidthDp = configuration.screenWidthDp.dp

            var contentScale: ContentScale = ContentScale.None
            val imageModifier: Modifier

            if (imageDpWidth < screenWidthDp) {
                val imageDpHeight = (imagePixelHeight / density).dp
                imageModifier = Modifier.size(imageDpWidth, imageDpHeight)
                // .background(Color.Cyan) // 디버깅용 배경색, 필요시 유지
            } else {
                imageModifier = Modifier.fillMaxWidth()
                contentScale = ContentScale.FillWidth
            }

            // SubcomposeAsyncImageContent 대신 Image 컴포저블을 직접 사용하거나,
            // painterState.painter를 사용하는 다른 Coil 3 API를 사용할 수 있습니다.
            // 여기서는 Image 컴포저블을 사용하여 명시적으로 painter를 렌더링합니다.
            Image(
                painter = painter,
                contentDescription = null, // 이미 위에서 null로 설정
                contentScale = contentScale,
                modifier = imageModifier
                    .clickable { onClicked?.invoke() },
                alignment = Alignment.TopStart // 기존 alignment 유지
            )
        },
        error = {
            // 에러 상태일 때 보여줄 UI (예: 아이콘 또는 메시지)
            // 기존 로직에서는 에러 시 로딩과 동일한 UI를 보여줬으므로, 필요시 Box와 CircularProgressIndicator를 여기에도 추가
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                contentAlignment = Alignment.Center,
            ) {
                // 예시: 에러 아이콘 표시
                // Icon(imageVector = Icons.Filled.Error, contentDescription = "Error loading image")
                CircularProgressIndicator( // 기존 로직처럼 로딩 인디케이터 표시
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        // alignment는 SubcomposeAsyncImage의 파라미터로 직접 전달 (Coil 2와 동일)
        alignment = Alignment.TopStart
    )
}

@Composable
fun YoutubeView(
    width: Int,
    height: Int,
    modifier: Modifier = Modifier,
    videoId: () -> String,
) {
    val videoIdString = remember { videoId() }
    val uriHandler = LocalUriHandler.current
    val urlThumbnailString = "https://img.youtube.com/vi/$videoIdString/mqdefault.jpg"
    var isSuccess by remember { mutableStateOf(false) }

    Timber.d("YoutubeView videoIdString=$videoIdString, width=$width, height=$height")

    Box(
        contentAlignment = Alignment.Center,
    ) {
        DetailImageCoil(
            urlString = { urlThumbnailString },
            onSuccess = { isSuccess = true },
            modifier = modifier.then(
                if (width > 0 && height > 0) {
                    Modifier.size(
                        with(LocalDensity.current) { width.dp },
                        with(LocalDensity.current) { height.dp },
                    )
                } else {
                    Modifier
                        .fillMaxWidth(0.9f)
                        .aspectRatio(0.6f)
                }
            ),
        ) {
            if (isSuccess) {
                uriHandler.openUri("https://www.youtube.com/watch?v=$videoIdString")
            }
        }

        if (isSuccess) {
            Icon(
                painter = painterResource(R.drawable.baseline_play_circle_outline_24),
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(140.dp),
                contentDescription = null,
            )
        }
    }
}

@Composable
fun HeartWithNumber(
    modifier: Modifier = Modifier,
    heartColor: Color = MaterialTheme.colorScheme.onSecondary,
    textColor: Color = MaterialTheme.colorScheme.onTertiary,
    textSize: TextUnit = 14.sp,
    number: String?, // 하트 안에 표시할 숫자
) {
    if (number.isNullOrBlank() || number == "0") return

    Row(
        verticalAlignment = Alignment.CenterVertically, modifier = modifier.fillMaxHeight()
    ) {
        Icon(
            imageVector = Icons.Default.FavoriteBorder,
            tint = heartColor,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.size(3.dp))
        Text(
            text = { number },
            fontSize = textSize,
            fontColor = textColor,
        )
    }
}

@Composable
fun ListText(
    modifier: Modifier = Modifier,
    text: () -> String,
    color: Color? = null,
    fontSize: TextUnit = 16.sp,
    textAlign: TextAlign = TextAlign.Start,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    maxLines: Int = Int.MAX_VALUE,
) {
    Text(
        style = MaterialTheme.typography.bodyMedium.copy(
            color = color ?: MaterialTheme.colorScheme.onBackground
        ),
        text = text(),
        modifier = modifier,
        fontSize = fontSize,
        textAlign = textAlign,
        overflow = overflow,
        maxLines = maxLines,
    )
}

@Composable
fun <T> T.useDebounce(
    delayMillis: Long = 300L,
    // 1. couroutine scope
    coroutineScope: CoroutineScope = rememberCoroutineScope(), onChange: (T) -> Unit
): T {
    // 2. updating state
    val state by rememberUpdatedState(this)

    // 3. launching the side-effect handler
    DisposableEffect(state) {
        val job = coroutineScope.launch {
            delay(delayMillis)
            onChange(state)
        }
        onDispose {
            job.cancel()
        }
    }
    return state
}
