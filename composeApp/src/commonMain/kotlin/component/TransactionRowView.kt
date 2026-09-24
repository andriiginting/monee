package component

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.LocalGroceryStore
import androidx.compose.material.icons.outlined.LocalPharmacy
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Train
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.CurrencyType
import data.Expense
import data.ocr.CATEGORY_UNKNOWN

/**
 * One ledger line: category tile, merchant, `account • time`, and the signed
 * amount. Expenses are money out, so they read as negative; a negative expense
 * (a refund) reads as money in and is tinted with the accent colour.
 */
@Composable
internal fun TransactionRowView(
    expense: Expense,
    modifier: Modifier = Modifier,
    showValues: Boolean = true,
) {
    val isIncome = expense.amountMinor < 0
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .shadow(1.dp, RoundedCornerShape(14.dp))
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (isIncome) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f).compositeOver(MaterialTheme.colorScheme.surface)
                    else MaterialTheme.colorScheme.surfaceVariant,
                )
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (isIncome) Icons.Outlined.Check else categoryIcon(expense.category),
                contentDescription = null,
                tint = if (isIncome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.size(16.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = expense.merchant,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = expense.subtitle,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.size(12.dp))
        Text(
            text = if (showValues) formatSignedAmount(-expense.amountMinor, expense.currency) else "••••••",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = if (isIncome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

/** `Cash Wallet • 09:15`, falling back to the category when no account was recorded. */
private val Expense.subtitle: String
    get() {
        val source = account?.takeIf { it.isNotBlank() }
            ?: category.takeIf { it.isNotBlank() && it != CATEGORY_UNKNOWN }
            ?: "Household expense"
        return listOfNotNull(source, time?.takeIf { it.isNotBlank() }).joinToString(" • ")
    }

private fun categoryIcon(category: String): ImageVector = when (category) {
    "Food & Dining" -> Icons.Outlined.LocalCafe
    "Groceries" -> Icons.Outlined.LocalGroceryStore
    "Transport" -> Icons.Outlined.Train
    "Health & Pharmacy" -> Icons.Outlined.LocalPharmacy
    "Shopping" -> Icons.Outlined.ShoppingBag
    "Utilities" -> Icons.Outlined.Bolt
    else -> Icons.Outlined.ReceiptLong
}

/** `+¥350,000` / `-¥600`. */
internal fun formatSignedAmount(amountMinor: Long, currency: CurrencyType): String {
    val sign = if (amountMinor > 0) "+" else if (amountMinor < 0) "-" else ""
    return sign + formatMoney(if (amountMinor < 0) -amountMinor else amountMinor, currency)
}

/**
 * `¥65,000`. Amounts are minor units; whole amounts drop the fraction, matching
 * how the scanner formats receipt totals.
 */
internal fun formatMoney(amountMinor: Long, currency: CurrencyType): String {
    val negative = amountMinor < 0
    val absolute = if (negative) -amountMinor else amountMinor
    val grouped = (absolute / 100).toString().reversed().chunked(3).joinToString(",").reversed()
    val minor = absolute % 100
    val body = if (minor == 0L) grouped else "$grouped.${minor.toString().padStart(2, '0')}"
    return (if (negative) "-" else "") + currency.symbol + body
}
