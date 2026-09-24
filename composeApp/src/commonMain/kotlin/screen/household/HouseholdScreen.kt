package screen.household

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import component.QrCodeView
import data.HouseholdInvite
import data.MoneeRepository
import kotlinx.coroutines.launch

@Composable
internal fun HouseholdScreen(repository: MoneeRepository) {
    val session by repository.session.collectAsState()
    var invite by remember { mutableStateOf<HouseholdInvite?>(null) }
    var showJoin by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val household = session?.household
    val user = session?.user

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Household", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier.size(56.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = user?.displayName?.take(1)?.uppercase() ?: "M",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Column {
                    Text(user?.displayName ?: "Monee user", fontWeight = FontWeight.Bold)
                    Text(
                        user?.email.orEmpty(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Group, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.size(8.dp))
                    Text(
                        "Members (${household?.memberIds?.size ?: 0}/2)",
                        fontWeight = FontWeight.Bold,
                    )
                }
                MemberRow(user?.displayName ?: "You", suffix = "(You)")
                Button(
                    onClick = {
                        error = null
                        scope.launch {
                            repository.createHouseholdInvite()
                                .onSuccess { invite = it }
                                .onFailure { error = it.message }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(),
                ) {
                    Icon(Icons.Outlined.QrCode2, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Invite member", fontWeight = FontWeight.SemiBold)
                }
                TextButton(
                    onClick = { showJoin = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Outlined.QrCode2, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("Join another household")
                }
            }
        }

        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }

    invite?.let {
        InviteDialog(invite = it, onDismiss = { invite = null })
    }
    if (showJoin) {
        JoinDialog(
            repository = repository,
            onDismiss = { showJoin = false },
            onJoined = { showJoin = false },
        )
    }
}

@Composable
private fun MemberRow(name: String, suffix: String) {
    Row(
        modifier = Modifier.fillMaxWidth().background(
            MaterialTheme.colorScheme.surfaceVariant,
            RoundedCornerShape(14.dp),
        ).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surface, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(name.take(1).uppercase(), fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.size(12.dp))
        Text("$name $suffix", fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun InviteDialog(invite: HouseholdInvite, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Invite member", fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Outlined.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Have your partner scan this QR code to join your shared ledger.",
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(20.dp))
                Box(
                    modifier = Modifier.size(180.dp).background(Color.White, RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    QrCodeView(
                        payload = "monee://join?code=${invite.code}",
                        modifier = Modifier.size(156.dp),
                    )
                }
                Spacer(Modifier.height(18.dp))
                Text("FALLBACK CODE", style = MaterialTheme.typography.labelSmall)
                Text(
                    invite.code,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "This invite expires in 24 hours and is limited to one redemption.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Icon(Icons.Outlined.Share, contentDescription = null)
                Spacer(Modifier.size(6.dp))
                Text("Done")
            }
        },
    )
}

@Composable
private fun JoinDialog(
    repository: MoneeRepository,
    onDismiss: () -> Unit,
    onJoined: () -> Unit,
) {
    var code by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = { if (!loading) onDismiss() },
        icon = { Icon(Icons.Outlined.Group, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text("Join household?", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Enter the invite code shared by the household owner.")
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.uppercase(); error = null },
                    label = { Text("Invite code") },
                    singleLine = true,
                )
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(
                enabled = !loading && code.isNotBlank(),
                onClick = {
                    loading = true
                    scope.launch {
                        repository.joinHousehold(code)
                            .onSuccess { onJoined() }
                            .onFailure { error = it.message }
                        loading = false
                    }
                },
            ) {
                Text(if (loading) "Joining..." else "Confirm & join")
            }
        },
        dismissButton = {
            TextButton(enabled = !loading, onClick = onDismiss) { Text("Cancel") }
        },
    )
}
