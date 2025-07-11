@file:OptIn(ExperimentalMaterial3Api::class)

package kr.b1ink.mocl.ui.site.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import timber.log.Timber

@Composable
fun ListTopAppBar(
    title: String,
    smallTitle: String,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    refreshCallback: () -> Unit = {},
) {
    val density = LocalDensity.current
    var moreMenuExpanded by remember { mutableStateOf(false) }
    var topAppBarHeight by remember { mutableStateOf(150.dp) }
    var oldTopAppBarOffsetHeight = 0.dp

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors()
            .copy(
                containerColor = MaterialTheme.colorScheme.primary,
                scrolledContainerColor = MaterialTheme.colorScheme.primary,
            ),
        title = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(align = Alignment.CenterVertically)
                    .onGloballyPositioned {
                        if (oldTopAppBarOffsetHeight == topAppBarHeight) return@onGloballyPositioned
                        Timber.d("TopAppBar#2 height = ${it.size.height}")
                        val tmpHeight = with(density) { it.size.height.toDp() }
                        topAppBarHeight = if (tmpHeight > 64.dp) tmpHeight else 64.dp
                        oldTopAppBarOffsetHeight = topAppBarHeight
                    }) {
                Text(
                    text = smallTitle, fontSize = 10.4.sp,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = Color.White,
                    ),
                    maxLines = 1,
                    modifier = Modifier.padding(end = 2.dp, top = 2.dp),
                )
                Text(
                    text = title,
                    fontSize = 16.sp,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                    ),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(end = 2.dp, top = 2.dp, bottom = 10.dp)
                        .wrapContentHeight(align = Alignment.CenterVertically),
                )
            }
        },
        actions = {
            IconButton(onClick = { moreMenuExpanded = true }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert, contentDescription = "More options", tint = Color.White
                )
            }
            DropdownMenu(
                expanded = moreMenuExpanded,
                onDismissRequest = { moreMenuExpanded = false },
                content = {
                    DropdownMenuItem(
                        text = { Text("새로고침") },
                        onClick = {
                            moreMenuExpanded = false
                            refreshCallback()
                        }
                    )
                }
            )
        },
        expandedHeight = topAppBarHeight,
        scrollBehavior = scrollBehavior,
        modifier = modifier.background(MaterialTheme.colorScheme.primary)
    )
}