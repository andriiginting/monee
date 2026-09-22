package component.header

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal enum class SyncStatus {
    Synced,
    Offline,
}

@Composable
internal fun TopHeaderView(
    householdInitials: String,
    isDarkTheme: Boolean,
    syncStatus: SyncStatus,
    onThemeToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        InitialProfileView(householdInitials)

        Spacer(modifier = Modifier.weight(1f))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onThemeToggle,
                modifier = Modifier.size(48.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) {
                            Icons.Outlined.LightMode
                        } else {
                            Icons.Outlined.DarkMode
                        },
                        contentDescription = if (isDarkTheme) {
                            "Use light theme"
                        } else {
                            "Use dark theme"
                        },
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            SyncStatusChip(syncStatus)
        }
    }
}

@Composable
private fun InitialProfileView(initialName: String) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initialName.take(2).uppercase(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun SyncStatusChip(status: SyncStatus) {
    val isSynced = status == SyncStatus.Synced
    val label = if (isSynced) "Synced" else "Offline"

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (isSynced) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        shape = RoundedCornerShape(50),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (isSynced) Icons.Outlined.Cloud else Icons.Outlined.CloudOff,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Preview
@Composable
private fun TopHeaderViewPreview() {
    TopHeaderView(
        householdInitials = "AG",
        isDarkTheme = true,
        syncStatus = SyncStatus.Synced,
        onThemeToggle = {},
    )
}
