import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import component.BottomBarView
import component.header.SyncStatus
import component.header.TopHeaderView
import moe.tlaster.precompose.PreComposeApp
import moe.tlaster.precompose.navigation.rememberNavigator
import androidx.compose.ui.tooling.preview.Preview
import navigation.Navigator
import screen.MainHostNav
import style.MoneyAppTheme

@Composable
@Preview
fun App() {
    PreComposeApp {
        val systemDarkTheme = isSystemInDarkTheme()
        var isDarkTheme by remember { mutableStateOf(systemDarkTheme) }

        MoneyAppTheme(darkTheme = isDarkTheme) {
            val navigator = rememberNavigator()

            ConfigureSystemBars(
                backgroundColor = MaterialTheme.colorScheme.background,
                darkTheme = isDarkTheme,
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
            ) {
                if (isAppChromeVisible(navigator)) {
                    Scaffold(
                        containerColor = MaterialTheme.colorScheme.background,
                        modifier = Modifier,
                        topBar = {
                            TopHeaderView(
                                householdInitials = "AG",
                                isDarkTheme = isDarkTheme,
                                syncStatus = SyncStatus.Synced,
                                onThemeToggle = { isDarkTheme = !isDarkTheme },
                            )
                        },
                        bottomBar = {
                            BottomBarView(
                                navigator = navigator,
                                onScanClick = {
                                    navigator.navigate(Navigator.SCANNER.route)
                                },
                            )
                        }
                    ) { innerPadding ->
                        BoxWithConstraints(
                            modifier = Modifier.fillMaxSize().padding(innerPadding)
                        ) {
                            MainHostNav(navigator = navigator)
                        }
                    }
                } else {
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        MainHostNav(navigator = navigator)
                    }
                }
            }
        }
    }
}

@Composable
private fun isAppChromeVisible(navigator: moe.tlaster.precompose.navigation.Navigator): Boolean {
    val currentRoute = navigator.currentEntry
        .collectAsState(null)
        .value
        ?.route
        ?.route

    return currentRoute in Navigator.getTopLevelRoute().map { it.route }
}
