package screen

import androidx.compose.runtime.Composable
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.rememberNavigator
import moe.tlaster.precompose.navigation.transition.NavTransition
import navigation.Navigator
import screen.history.HistoryScreen
import screen.home.HomeScreen
import screen.personalizecard.PersonalizeCardScreen

@Composable
internal fun MainHostNav() {
    val navigator = rememberNavigator()
    NavHost(
        navigator = navigator,
        navTransition = NavTransition(),
        initialRoute = Navigator.HOME.route,
    ) {
        scene(
            route = Navigator.HOME.route,
        ) {
            HomeScreen(navigator)
        }

        scene(
            route = Navigator.HISTORY.route,
        ) {
            HistoryScreen(navigator)
        }

        scene(
            route = Navigator.SPLASH.route,
        ) {
            SplashScreen(navigator)
        }

        scene(
            route = Navigator.PERSONALIZE_CARD.route,
        ) {
            PersonalizeCardScreen(navigator)
        }
    }
}