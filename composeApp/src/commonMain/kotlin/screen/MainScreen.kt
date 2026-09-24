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
import data.MoneeRepository
import navigation.Navigator as AppRoute
import screen.auth.AuthScreen
import screen.history.HistoryScreen
import screen.home.HomeScreen
import screen.household.HouseholdScreen
import screen.personalizecard.PersonalizeCardScreen
import screen.scanner.ScannerScreen

@Composable
internal fun MainHostNav(
    navigator: PrecomposeNavigator,
    repository: MoneeRepository,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
) {
    NavHost(
        navigator = navigator,
        navTransition = NavTransition(),
        initialRoute = AppRoute.SPLASH.route,
    ) {
        scene(route = AppRoute.HOME.route) {
            HomeScreen(navigator, repository)
        }

        scene(route = AppRoute.HISTORY.route) {
            HistoryScreen(navigator, repository)
        }

        scene(route = AppRoute.BUDGET.route) {
            PlaceholderScreen("Budget")
        }

        scene(route = AppRoute.HOUSEHOLD.route) {
            HouseholdScreen(repository)
        }

        scene(route = AppRoute.SPLASH.route) {
            SplashScreen(navigator, repository)
        }

        scene(route = AppRoute.AUTH.route) {
            AuthScreen(
                navigator = navigator,
                repository = repository,
                isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle,
            )
        }

        scene(route = AppRoute.PERSONALIZE_CARD.route) {
            PersonalizeCardScreen(navigator)
        }

        scene(route = AppRoute.SCANNER.route) {
            ScannerScreen(navigator = navigator, repository = repository)
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
