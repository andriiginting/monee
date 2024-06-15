import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import component.BottomBarView
import component.BottomNavigationItem
import moe.tlaster.precompose.PreComposeApp
import moe.tlaster.precompose.navigation.rememberNavigator
import navigation.Navigator
import org.jetbrains.compose.ui.tooling.preview.Preview
import screen.MainHostNav
import style.MoneyAppTheme

@Composable
@Preview
fun App() {
    PreComposeApp {
        MoneyAppTheme {
            val navigator = rememberNavigator()

            Scaffold(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.statusBarsPadding(),
                bottomBar = {
                    if (isTopLevelRoute(navigator)) {
                        BottomBarView(navigator, BottomNavigationItem.bottomNavigationItems())
                    }
                }
            ) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    MainHostNav()
                }
            }
        }
    }
}

@Composable
private fun isTopLevelRoute(navigator: moe.tlaster.precompose.navigation.Navigator): Boolean {
    return navigator.currentEntry
        .collectAsState(null).value?.route?.route in Navigator.getTopLevelRoute()
        .map { it.route }
}
