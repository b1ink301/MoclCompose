package kr.b1ink.mocl.ui.intro.composables

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kr.b1ink.mocl.ui.intro.IntroNavOption
import kr.b1ink.mocl.ui.previews.AllScreenPreview
import kr.b1ink.mocl.ui.theme.AppTheme

@Composable
fun WelcomeScreen(navController: NavController = rememberNavController()) = IntroCompose(
    navController = navController,
    text = "Welcome",
    showBackButton = false
) {
    navController.navigate(IntroNavOption.MotivationScreen.name)
}


@AllScreenPreview
@Composable
fun WelcomeScreenPreview() {
    AppTheme {
        WelcomeScreen()
    }
}

