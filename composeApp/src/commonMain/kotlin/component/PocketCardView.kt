package component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.CurrencyType
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun PocketCardView(data: CardData) {
    Card(
        modifier = Modifier.width(200.dp)
            .height(100.dp),
        shape = MaterialTheme.shapes.medium,
        backgroundColor = getInnerCardColor(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                data.cardName,
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                textAlign = TextAlign.End,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Text(
                buildString {
                    append(data.amount)
                    append(" ${data.currencyType}")
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(top = 4.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
            )
        }
    }
}

internal data class CardData(
    val cardName: String,
    val amount: Int,
    val currencyType: CurrencyType,
)


@Preview
@Composable
private fun PreviewPocketView() {
    val data = CardData(
        "PayPay",
        200000,
        CurrencyType.JPY
    )

    PocketCardView(data)
}
