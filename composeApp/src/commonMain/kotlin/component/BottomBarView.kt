package component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import moe.tlaster.precompose.navigation.Navigator
import navigation.Navigator as AppRoute

@Composable
internal fun BottomBarView(
    navigator: Navigator,
    onScanClick: () -> Unit,
) {
    val currentDestination = navigator.currentEntry
        .collectAsState(null)
        .value
        ?.route
        ?.route

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp),
    ) {
        NavigationBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(80.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            windowInsets = WindowInsets(0, 0, 0, 0),
        ) {
            BottomNavigationItem.items().take(2).forEach { item ->
                NavigationItem(item, currentDestination, navigator)
            }

            Spacer(modifier = Modifier.weight(1f))

            BottomNavigationItem.items().drop(2).forEach { item ->
                NavigationItem(item, currentDestination, navigator)
            }
        }

        FloatingActionButton(
            onClick = onScanClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-24).dp)
                .size(64.dp)
                .shadow(8.dp, CircleShape),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape,
        ) {
            Icon(
                imageVector = Icons.Outlined.CameraAlt,
                contentDescription = "Scan receipt",
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
private fun RowScope.NavigationItem(
    item: BottomNavigationItem,
    currentDestination: String?,
    navigator: Navigator,
) {
    NavigationBarItem(
        modifier = Modifier.weight(1f),
        selected = item.route == currentDestination,
        onClick = {
            if (item.route != currentDestination) {
                navigator.navigate(item.route)
            }
        },
        icon = {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
            )
        },
        label = {
            Text(item.label)
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    )
}

private data class BottomNavigationItem(
    val label: String,
    val icon: ImageVector,
    val route: String,
) {
    companion object {
        fun items(): List<BottomNavigationItem> = listOf(
            BottomNavigationItem("Home", Icons.Outlined.Home, AppRoute.HOME.route),
            BottomNavigationItem(
                "Activity",
                Icons.AutoMirrored.Outlined.List,
                AppRoute.HISTORY.route,
            ),
            BottomNavigationItem("Budget", Icons.Outlined.PieChart, AppRoute.BUDGET.route),
            BottomNavigationItem("Profile", Icons.Outlined.Person, AppRoute.HOUSEHOLD.route),
        )
    }
}
