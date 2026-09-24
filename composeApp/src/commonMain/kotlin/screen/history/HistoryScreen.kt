package screen.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.MoneeRepository
import data.forHousehold
import moe.tlaster.precompose.navigation.Navigator
import screen.home.ExpenseRow

@Composable
internal fun HistoryScreen(navigator: Navigator, repository: MoneeRepository) {
    val session by repository.session.collectAsState()
    val expenses by repository.expenses.collectAsState()
    val householdExpenses = expenses
        .forHousehold(session?.household?.id)
        .sortedWith(compareByDescending { it.createdAtMillis })
    val totalMinor = householdExpenses.sumOf { it.amountMinor }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 4.dp)) {
                Text("History", style = MaterialTheme.typography.headlineMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                Text(
                    session?.household?.name ?: "Household",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Total household spending", color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(
                        "${householdExpenses.firstOrNull()?.currency?.symbol ?: "¥"}${totalMinor / 100}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Text(
                        "${householdExpenses.size} transaction${if (householdExpenses.size == 1) "" else "s"}",
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
        if (householdExpenses.isEmpty()) {
            item {
                Text(
                    "No household expenses yet.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 20.dp),
                )
            }
        } else {
            items(householdExpenses, key = { it }) { expense ->
                ExpenseRow(expense)
            }
        }
    }
}
