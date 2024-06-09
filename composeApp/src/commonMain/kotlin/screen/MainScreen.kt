package screen

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
        navTransition = NavTransition(),
        initialRoute = "/home",
    ) {
        scene(
            route = "/home",
        ) {
            HomeScreen(Modifier)
        }

        scene(
            route = "/history",
        ) {
            HistoryScreen()
        }
    }
}