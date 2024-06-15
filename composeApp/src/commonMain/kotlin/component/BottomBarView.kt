package component

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import moe.tlaster.precompose.navigation.Navigator
import moneyproject.composeapp.generated.resources.Res
import moneyproject.composeapp.generated.resources.home_ic
import moneyproject.composeapp.generated.resources.transaction_ic
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun BottomBarView(
    navigator: Navigator,
    menus: List<BottomNavigationItem>,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        menus.forEachIndexed { _, bottomNavigationItem ->

            val currentDestination =
                navigator.currentEntry.collectAsState(null).value?.route?.route
            val isSelected = bottomNavigationItem.route == currentDestination

            NavigationBarItem(
                colors = NavigationBarItemColors(
                    selectedIconColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primaryContainer,
                    disabledIconColor = MaterialTheme.colorScheme.surfaceContainer,
                    disabledTextColor = MaterialTheme.colorScheme.surfaceContainer,
                    selectedIndicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedTextColor = MaterialTheme.colorScheme.primaryContainer
                ),
                selected = isSelected,
                icon = {
                    Icon(
                        painterResource(bottomNavigationItem.iconResourcePath),
                        contentDescription = bottomNavigationItem.label
                    )
                }, label = {
                    Text(bottomNavigationItem.label)
                }, onClick = {
                    if (bottomNavigationItem.route != currentDestination) {
                        navigator.navigate(route = bottomNavigationItem.route)
                    }
                })
        }
    }

}

data class BottomNavigationItem(
    val label: String, val iconResourcePath: DrawableResource, val route: String = MainScreen.Home.route
) {
    companion object {
        fun bottomNavigationItems(): List<BottomNavigationItem> {
            return listOf(
                BottomNavigationItem(
                    label = "Home",
                    iconResourcePath = Res.drawable.home_ic,
                    route = MainScreen.Home.route
                ),
                BottomNavigationItem(
                    label = "History",
                    iconResourcePath = Res.drawable.transaction_ic,
                    route = MainScreen.History.route
                ),
            )
        }
    }
}

sealed class MainScreen(val route: String) {
    data object Home : MainScreen("/home")
    data object History : MainScreen("/history")
}