package screen.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import component.TransactionRowView
import data.Expense
import data.MoneeRepository
import data.calendarDay
import data.forHousehold
import moe.tlaster.precompose.navigation.Navigator

@Composable
internal fun HistoryScreen(navigator: Navigator, repository: MoneeRepository) {
    val session by repository.session.collectAsState()
    val expenses by repository.expenses.collectAsState()
    val days = expenses
        .forHousehold(session?.household?.id)
        .groupBy { it.calendarDay }
        .entries
        .sortedByDescending { it.key }
        .map { (day, dayExpenses) ->
            day.heading to dayExpenses.sortedWith(
                compareByDescending<Expense> { it.createdAtMillis }.thenByDescending { it.id },
            )
        }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
    ) {
        item {
            Text(
                text = "Activity",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 4.dp, bottom = 20.dp),
            )
        }
        if (days.isEmpty()) {
            item {
                Text(
                    "No household expenses yet.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 20.dp),
                )
            }
        } else {
            items(days, key = { it.first }) { (heading, dayExpenses) ->
                ActivityDaySection(heading, dayExpenses)
            }
        }
    }
}

@Composable
private fun ActivityDaySection(heading: String, expenses: List<Expense>) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 28.dp)) {
        Text(
            text = heading,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp, bottom = 12.dp),
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
            shadowElevation = 1.dp,
        ) {
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)) {
                expenses.forEach { expense -> TransactionRowView(expense) }
            }
        }
    }
}
