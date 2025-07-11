package kr.b1ink.mocl.ui.components.appdrawer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import kr.b1ink.mocl.BuildConfig
import kr.b1ink.mocl.R
import kr.b1ink.mocl.ui.components.ListDivider
import kr.b1ink.mocl.ui.components.Text

@Composable
fun <T : Enum<T>> AppDrawerContent(
    drawerState: DrawerState,
    menuItems: List<AppDrawerItemInfo<T>>,
    defaultPick: T,
    onClick: (T) -> Unit
) {
    var currentPick by remember { mutableStateOf(defaultPick) }
    val coroutineScope = rememberCoroutineScope()

    val closeDrawer: () -> Unit = {
        coroutineScope.launch {
            drawerState.close()
        }
    }

    ModalDrawerSheet(
        modifier = Modifier
            .fillMaxWidth(0.7f),
        windowInsets = DrawerDefaults.windowInsets.only(WindowInsetsSides.Start),
    ) {
        Surface(
            color = colorScheme.background,
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Box(
                    modifier = Modifier
                        .background(colorScheme.primary)
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(170.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(R.mipmap.ic_launcher),
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                LazyColumn(
                    modifier = Modifier
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(menuItems) { item ->
                        Column {
                            AppDrawerItem(item = item, isChecked = currentPick == item.drawerOption) { navOption ->
                                if (currentPick == navOption) {
                                    closeDrawer()
                                    return@AppDrawerItem
                                }

                                currentPick = navOption
                                closeDrawer()
                                onClick(navOption)
                            }
                            ListDivider(start = 15.dp, end = 4.dp)
                        }
                    }
                }
                Text(
                    text = { "v${BuildConfig.VERSION_NAME}" },
                    fontSize = 16.sp,
                    modifier = Modifier
                        .wrapContentWidth(Alignment.CenterHorizontally)
                        .padding(vertical = 4.dp)
                        .height(30.dp)
                )
            }
        }
    }
}