package component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(80.dp)
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Top,
        ) {
            BottomNavigationItem.items().take(2).forEach { item ->
                NavigationItem(
                    item = item,
                    selected = item.route == currentDestination,
                    navigator = navigator,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            BottomNavigationItem.items().drop(2).forEach { item ->
                NavigationItem(
                    item = item,
                    selected = item.route == currentDestination,
                    navigator = navigator,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-24).dp)
                .size(64.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .border(6.dp, MaterialTheme.colorScheme.background, CircleShape)
                .clickable(onClick = onScanClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.CameraAlt,
                contentDescription = "Scan receipt",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
private fun NavigationItem(
    item: BottomNavigationItem,
    selected: Boolean,
    navigator: Navigator,
    modifier: Modifier,
) {
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = modifier
            .height(64.dp)
            .clip(MaterialTheme.shapes.large)
            .clickable {
                if (!selected) {
                    navigator.navigate(item.route)
                }
            }
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = contentColor,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = item.label,
            color = contentColor,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
        )
    }
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
                AppRoute.HISTORY.route
            ),
            BottomNavigationItem("Budget", Icons.Outlined.PieChart, AppRoute.BUDGET.route),
            BottomNavigationItem("Insight", Icons.Outlined.Flag, AppRoute.INSIGHT.route),
        )
    }
}
