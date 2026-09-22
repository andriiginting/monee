package screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.Navigator as PrecomposeNavigator
import moe.tlaster.precompose.navigation.transition.NavTransition
import navigation.Navigator as AppRoute
import screen.history.HistoryScreen
import screen.home.HomeScreen
import screen.personalizecard.PersonalizeCardScreen
import screen.scanner.ScannerScreen

@Composable
internal fun MainHostNav(
    navigator: PrecomposeNavigator,
) {
    NavHost(
        navigator = navigator,
        navTransition = NavTransition(),
        initialRoute = AppRoute.SPLASH.route,
    ) {
        scene(route = AppRoute.HOME.route) {
            HomeScreen(navigator)
        }

        scene(route = AppRoute.HISTORY.route) {
            HistoryScreen(navigator)
        }

        scene(route = AppRoute.BUDGET.route) {
            PlaceholderScreen("Budget")
        }

        scene(route = AppRoute.INSIGHT.route) {
            PlaceholderScreen("Insight")
        }

        scene(route = AppRoute.SPLASH.route) {
            SplashScreen(navigator)
        }

        scene(route = AppRoute.PERSONALIZE_CARD.route) {
            PersonalizeCardScreen(navigator)
        }

        scene(route = AppRoute.SCANNER.route) {
            ScannerScreen(navigator = navigator)
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
        )
    }
}
