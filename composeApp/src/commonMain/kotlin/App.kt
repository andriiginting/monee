import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import component.BottomBarView
import component.BottomNavigationItem
import moe.tlaster.precompose.PreComposeApp
import moe.tlaster.precompose.navigation.rememberNavigator
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
                bottomBar = {
                    BottomBarView(navigator, BottomNavigationItem.bottomNavigationItems())
                }
            ) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    MainHostNav()
                }
            }
        }
    }
}
