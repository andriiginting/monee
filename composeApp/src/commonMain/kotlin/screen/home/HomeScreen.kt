package screen.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import component.TransactionRowView
import component.formatMoney
import data.CalendarDay
import data.CurrencyType
import data.Expense
import data.MoneeRepository
import data.calendarDay
import data.forHousehold
import moe.tlaster.precompose.navigation.Navigator
import navigation.Navigator as AppRoute

/**
 * Placeholder household budget until budgets are stored per household.
 * Minor units, so this is ¥250,000.
 */
private const val MONTHLY_BUDGET_MINOR = 250_000L * 100

private const val RECENT_ACTIVITY_COUNT = 3

@Composable
internal fun HomeScreen(navigator: Navigator, repository: MoneeRepository) {
    val session by repository.session.collectAsState()
    val expenses by repository.expenses.collectAsState()
    val householdExpenses = expenses.forHousehold(session?.household?.id)
    var showValues by rememberSaveable { mutableStateOf(true) }

    val today = remember { CalendarDay.today() }
    val spentMinor = householdExpenses
        .filter { it.calendarDay.let { day -> day.year == today.year && day.month == today.month } }
        .sumOf { it.amountMinor }
    val currency = householdExpenses.firstOrNull()?.currency ?: CurrencyType.JPY
    val recentExpenses = householdExpenses
        .sortedWith(compareByDescending<Expense> { it.createdAtMillis }.thenByDescending { it.id })
        .take(RECENT_ACTIVITY_COUNT)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
    ) {
        item {
            SafeToSpendHero(
                budgetMinor = MONTHLY_BUDGET_MINOR,
                spentMinor = spentMinor,
                currency = currency,
                showValues = showValues,
                onToggleValues = { showValues = !showValues },
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 28.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                HomeActionButton(
                    icon = Icons.Outlined.Add,
                    label = "Add Manual",
                    onClick = { navigator.navigate(AppRoute.SCANNER.route) },
                    modifier = Modifier.weight(1f),
                )
                HomeActionButton(
                    icon = Icons.Outlined.Check,
                    label = "Reconcile",
                    onClick = { navigator.navigate(AppRoute.HISTORY.route) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Recent Activity",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { navigator.navigate(AppRoute.HISTORY.route) }) {
                    Icon(
                        Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                        contentDescription = "View all activity",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        if (recentExpenses.isEmpty()) {
            item { EmptyActivityView() }
        } else {
            items(recentExpenses, key = { it.id }) { expense ->
                TransactionRowView(expense, showValues = showValues)
            }
        }
    }
}

@Composable
private fun SafeToSpendHero(
    budgetMinor: Long,
    spentMinor: Long,
    currency: CurrencyType,
    showValues: Boolean,
    onToggleValues: () -> Unit,
) {
    val safeToSpend = budgetMinor - spentMinor
    fun money(minor: Long) = if (showValues) formatMoney(minor, currency) else "••••••"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            // Tapping the figure hides it, for checking the app in public.
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggleValues,
            )
            .padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "SAFE TO SPEND",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = money(safeToSpend),
            fontSize = 52.sp,
            lineHeight = 56.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-1).sp,
            textAlign = TextAlign.Center,
            color = if (safeToSpend < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.size(12.dp))
        Text(
            text = "${money(spentMinor)} spent of ${money(budgetMinor)}",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun HomeActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(28.dp)
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = shape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(26.dp))
            Spacer(Modifier.size(10.dp))
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun EmptyActivityView() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 16.dp),
    ) {
        Icon(Icons.Outlined.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.size(10.dp))
        Text("No expenses yet", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Text(
            "Scan a receipt to post the first household expense.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
