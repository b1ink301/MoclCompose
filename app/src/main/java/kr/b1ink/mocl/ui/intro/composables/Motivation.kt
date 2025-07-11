package kr.b1ink.mocl.ui.intro.composables

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kr.b1ink.mocl.ui.intro.IntroNavOption
import kr.b1ink.mocl.ui.previews.AllScreenPreview
import kr.b1ink.mocl.ui.theme.AppTheme


@Composable
fun MotivationScreen(navController: NavController) = IntroCompose(
    navController = navController,
    text = "Motivation"
) {
    navController.navigate(IntroNavOption.RecommendationScreen.name)
}

@AllScreenPreview
@Composable
fun MotivationPrivacyPreview() {
    val navController = rememberNavController()
    AppTheme {
        MotivationScreen(navController = navController)
    }
}

