package kr.b1ink.mocl

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.fragula2.compose.FragulaNavHost
import com.fragula2.compose.rememberFragulaNavController
import kr.b1ink.domain.model.MainNavOption
import kr.b1ink.domain.model.NavScreen
import kr.b1ink.domain.model.SiteType
import kr.b1ink.mocl.ui.components.appdrawer.AppDrawerContent
import kr.b1ink.mocl.ui.components.appdrawer.AppDrawerItemInfo
import kr.b1ink.mocl.ui.theme.AppTheme

@Composable
fun MainCompose(
    navController: NavHostController = rememberFragulaNavController(),
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    viewModel: NaviViewModel = hiltViewModel(),
) {
    val siteState by viewModel.siteState.collectAsState()
    val scope = rememberCoroutineScope()
    var drawerEnabled by remember { mutableStateOf(true) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(navBackStackEntry) {
        drawerEnabled = navBackStackEntry?.destination?.route == NavScreen.Main.name
    }

    AppTheme {
        Surface {
            ModalNavigationDrawer(
                drawerState = drawerState,
                gesturesEnabled = drawerEnabled,
                drawerContent = {
                    AppDrawerContent<SiteType>(
                        drawerState = drawerState,
                        menuItems = DrawerParams.drawerButtons,
                        defaultPick = siteState,
                        onClick = viewModel::setSiteType
                    )
                },
                modifier = Modifier
                    .consumeWindowInsets(WindowInsets.statusBars.only(WindowInsetsSides.Bottom))
            ) {
                FragulaNavHost(
                    navController = navController,
                    startDestination = MainNavOption.SiteScreen.name,
                    scrimAmount = 0.7f,
                    animDurationMs = 400,
                    elevationAmount = 10.dp
                ) {
                    mainGraph(
                        navController = navController,
                        drawerState = drawerState,
                        scope = scope,
                    )
                }
            }
        }
    }
}

object DrawerParams {
    val drawerButtons = arrayListOf(
        AppDrawerItemInfo(
            SiteType.Clien,
            R.string.drawer_clien,
        ),
        AppDrawerItemInfo(
            SiteType.Damoang,
            R.string.drawer_damoang,
        ),
        AppDrawerItemInfo(
            SiteType.NaverCafe,
            R.string.drawer_naver_cafe,
        ),
        AppDrawerItemInfo(
            SiteType.Meeco,
            R.string.drawer_meeco,
        ),
    )
}

@Preview
@Composable
fun MainActivityPreview() {
    MainCompose()
}