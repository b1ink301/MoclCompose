package kr.b1ink.mocl.ui.intro.composables

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kr.b1ink.domain.model.MainNavOption
import kr.b1ink.mocl.R
import kr.b1ink.mocl.ui.intro.IntroViewModel
import kr.b1ink.mocl.ui.previews.AllScreenPreview

@Composable
fun RecommendationScreen(
    navController: NavController,
    viewModel: IntroViewModel = hiltViewModel()
) = IntroCompose(
    navController = navController,
    text = "Recommendation",
    buttonText = R.string.start_app
) {
    viewModel.saveUserOnboarding()
    navController.navigate(MainNavOption.SettingsScreen.name) {
        popUpTo(MainNavOption.SettingsScreen.name)
    }
}

@AllScreenPreview
@Composable
fun RecommendationScreenPreview() {
    val navController = rememberNavController()
    RecommendationScreen(navController = navController)
}