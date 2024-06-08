package component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import style.surfaceContainerDark
import style.surfaceContainerLight

@Composable
fun BottomBarView(
    onSelectedItem: (BottomNavigationItem) -> Unit
) {
    var navigationSelectedItem by remember { mutableStateOf(0) }
    val menus = BottomNavigationItem.bottomNavigationItems()

    NavigationBar(
        containerColor = if (isSystemInDarkTheme()) surfaceContainerDark else surfaceContainerLight
    ) {
        menus.forEachIndexed { index, bottomNavigationItem ->
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
                selected = navigationSelectedItem == index,
                icon = {
                    Icon(
                        bottomNavigationItem.icon,
                        contentDescription = bottomNavigationItem.label
                    )
                },
                label = {
                    Text(bottomNavigationItem.label)
                },
                onClick = {
                    navigationSelectedItem = index
                    onSelectedItem.invoke(menus[navigationSelectedItem])
                }
            )
        }
    }

}

data class BottomNavigationItem(
    val label: String,
    val icon: ImageVector = Icons.Filled.Home,
    val route: String = ""
) {
    companion object {
        fun bottomNavigationItems(): List<BottomNavigationItem> {
            return listOf(
                BottomNavigationItem(
                    label = "Home",
                    icon = Icons.Filled.Home,
                    route = MainScreen.Home.route
                ),
                BottomNavigationItem(
                    label = "History",
                    icon = Icons.Filled.Favorite,
                    route = MainScreen.History.route
                ),
            )
        }
    }
}

sealed class MainScreen(val route: String) {
    data object Home : MainScreen("home")
    data object History : MainScreen("history")
}