import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import component.BottomBarView
import component.header.SyncStatus
import component.header.TopHeaderView
import moe.tlaster.precompose.PreComposeApp
import moe.tlaster.precompose.navigation.rememberNavigator
import navigation.Navigator
import androidx.compose.ui.tooling.preview.Preview
import screen.MainHostNav
import style.MoneyAppTheme

@Composable
@Preview
@OptIn(ExperimentalMaterial3Api::class)
fun App() {
    PreComposeApp {
        val systemDarkTheme = isSystemInDarkTheme()
        var isDarkTheme by remember { mutableStateOf(systemDarkTheme) }

        MoneyAppTheme(darkTheme = isDarkTheme) {
            val navigator = rememberNavigator()
            var showScanSheet by remember { mutableStateOf(false) }

            ConfigureSystemBars(
                backgroundColor = MaterialTheme.colorScheme.background,
                darkTheme = isDarkTheme,
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
            ) {
                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    modifier = Modifier,
                    topBar = {
                        if (isTopLevelRoute(navigator)) {
                            TopHeaderView(
                                householdInitials = "YK",
                                isDarkTheme = isDarkTheme,
                                syncStatus = SyncStatus.Synced,
                                onThemeToggle = { isDarkTheme = !isDarkTheme },
                            )
                        }
                    },
                    bottomBar = {
                        if (isTopLevelRoute(navigator)) {
                            BottomBarView(
                                navigator = navigator,
                                onScanClick = { showScanSheet = true },
                            )
                        }
                    }
                ) { innerPadding ->
                    BoxWithConstraints(
                        modifier = Modifier.fillMaxSize().padding(innerPadding)
                    ) {
                        MainHostNav(navigator)
                    }
                }

                if (showScanSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showScanSheet = false },
                    ) {
                        Text(
                            text = "Receipt scanner",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun isTopLevelRoute(navigator: moe.tlaster.precompose.navigation.Navigator): Boolean {
    val currentRoute = navigator.currentEntry
        .collectAsState(null)
        .value
        ?.route
        ?.route

    return currentRoute in Navigator.getTopLevelRoute().map { it.route }
}
