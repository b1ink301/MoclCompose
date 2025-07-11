package kr.b1ink.mocl

import androidx.compose.material3.DrawerState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.fragula2.compose.swipeable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kr.b1ink.common.util.toJson
import kr.b1ink.domain.model.ListItem
import kr.b1ink.domain.model.MainItem
import kr.b1ink.domain.model.MainNavOption
import kr.b1ink.domain.model.NavScreen
import kr.b1ink.domain.model.SiteType
import kr.b1ink.mocl.ui.site.detail.DetailScreen
import kr.b1ink.mocl.ui.site.list.ListScreen
import kr.b1ink.mocl.ui.site.login.LoginScreen
import kr.b1ink.mocl.ui.site.main.MainScreen
import kr.b1ink.mocl.util.encodeBase64String

const val EXTRAS_QUERY = "query"
const val EXTRAS_SITE_TYPE = "site_type"
const val EXTRAS_READ_ID = "read_id"
const val EXTRAS_RELOAD_DATA = "reload_data"

fun NavGraphBuilder.mainGraph(
    navController: NavController,
    drawerState: DrawerState,
    scope: CoroutineScope,
) {
    val openDrawer: () -> Unit = {
        scope.launch {
            drawerState.open()
        }
    }

    navigation(
        startDestination = NavScreen.Main.name,
        route = MainNavOption.SiteScreen.name,
    ) {
        swipeable(
            route = NavScreen.Main.name,
        ) { backStackEntry ->
            val isReloadDataFlow = backStackEntry.savedStateHandle.getStateFlow(EXTRAS_RELOAD_DATA, false)
            val isReloadData by isReloadDataFlow.collectAsStateWithLifecycle()

            MainScreen(
                isReloadData = isReloadData,
                openDrawer = openDrawer,
                onLogin = { siteType ->
                    val route = "${NavScreen.Login.name}?$EXTRAS_SITE_TYPE=$siteType"
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                }
            ) {
                val query = it.toJson<MainItem>()
                val route = "${NavScreen.List.name}?$EXTRAS_QUERY=$query"

                navController.navigate(route = route) {
                    launchSingleTop = true
                }
            }
        }

        swipeable(
            route = "${NavScreen.Login.name}?$EXTRAS_SITE_TYPE={$EXTRAS_SITE_TYPE}",
            arguments = listOf(
                navArgument(EXTRAS_SITE_TYPE) {
                    nullable = false
                    type = NavType.EnumType(SiteType::class.java)
                },
            ),
        ) {
            LoginScreen { isReloadData ->
                navController.previousBackStackEntry?.savedStateHandle?.set(EXTRAS_RELOAD_DATA, isReloadData)
                navController.popBackStack()
            }
        }

        swipeable(
            route = "${NavScreen.List.name}?$EXTRAS_QUERY={$EXTRAS_QUERY}",
            arguments = listOf(
                navArgument(EXTRAS_QUERY) {
                    nullable = false
                    type = NavType.StringType
                },
            ),
        ) { backStackEntry ->
            val readIdFlow = backStackEntry.savedStateHandle.getStateFlow(EXTRAS_READ_ID, -1L)
            val readId by readIdFlow.collectAsStateWithLifecycle()

            ListScreen(readId) { item, siteType, title ->
                val query = item.copy(title = item.title.encodeBase64String(), boardTitle = title)
                    .toJson<ListItem>()
                val route = "${NavScreen.Detail.name}?$EXTRAS_QUERY=${query}&$EXTRAS_SITE_TYPE=${siteType}"

                navController.navigate(route) {
                    launchSingleTop = true
                }
            }
        }

        swipeable(
            route = "${NavScreen.Detail.name}?$EXTRAS_QUERY={$EXTRAS_QUERY}&$EXTRAS_SITE_TYPE={$EXTRAS_SITE_TYPE}",
            arguments = listOf(navArgument(EXTRAS_QUERY) {
                nullable = true
                defaultValue = null
                type = NavType.StringType
            }, navArgument(EXTRAS_SITE_TYPE) {
                nullable = false
                type = NavType.EnumType(SiteType::class.java)
            }),
        ) {
            DetailScreen { id ->
                navController.previousBackStackEntry?.savedStateHandle?.set(EXTRAS_READ_ID, id)
            }
        }
    }
}
