package screen

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.rememberNavigator
import moe.tlaster.precompose.navigation.transition.NavTransition
import screen.history.HistoryScreen
import screen.home.HomeScreen

@Composable
internal fun MainHostNav() {
    val navigator = rememberNavigator()
    NavHost(
        navigator = navigator,
        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
        navTransition = NavTransition(),
        initialRoute = "/home",
    ) {
        scene(
            route = "/home",
        ) {
            HomeScreen()
        }

        scene(
            route = "/history",
        ) {
            HistoryScreen()
        }
    }
}