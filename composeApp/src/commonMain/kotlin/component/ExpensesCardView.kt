package component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ExpenseCardView(data: ExpensesData) {
    Card(
        modifier = Modifier.width(150.dp)
            .height(150.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardColors(
            contentColor = getInnerCardColor(),
            containerColor = getInnerCardColor(),
            disabledContentColor = getInnerCardColor(),
            disabledContainerColor = getInnerCardColor()
        ),
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                "\uD83D\uDCB5",
                fontSize = 32.sp,
            )

            Text(
                data.name,
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.inverseSurface,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Text(
                "${data.amount}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.inverseSurface,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Text(
                data.description,
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.inverseSurface,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .basicMarquee(),
                maxLines = 2,
            )
        }
    }
}

internal data class ExpensesData(
    val amount: Int,
    val description: String,
    val name: String
)