package kr.b1ink.mocl.ui.intro

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import kr.b1ink.domain.model.MainNavOption
import kr.b1ink.mocl.ui.intro.composables.MotivationScreen
import kr.b1ink.mocl.ui.intro.composables.RecommendationScreen
import kr.b1ink.mocl.ui.intro.composables.WelcomeScreen
import timber.log.Timber

fun NavGraphBuilder.introGraph(navController: NavController) {
    navigation(
        startDestination = IntroNavOption.WelcomeScreen.name,
        route = MainNavOption.AboutScreen.name
    ) {
        Timber.d("introGraph navigation = $route")

        composable(IntroNavOption.WelcomeScreen.name) {
            Timber.d("introGraph WelcomeScreen = $it")
            WelcomeScreen(navController)
        }
        composable(IntroNavOption.MotivationScreen.name) {
            Timber.d("introGraph MotivationScreen = $it")
            MotivationScreen(navController)
        }
        composable(IntroNavOption.RecommendationScreen.name) {
            Timber.d("introGraph RecommendationScreen = $it")
            RecommendationScreen(navController)
        }
    }
}

enum class IntroNavOption {
    WelcomeScreen,
    MotivationScreen,
    RecommendationScreen
}
