package component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.SpaceBetween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.BalanceData

@Composable
internal fun WalletBalanceView(
    balances: BalanceData
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.primaryContainer,
            disabledContentColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.height(180.dp).fillMaxWidth(),
        ) {
            TotalBalanceView(balances)

            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
                colors = CardDefaults.cardColors(
                    containerColor = getInnerCardColor(),
                    disabledContainerColor = getInnerCardColor(),
                    contentColor = getInnerCardColor(),
                    disabledContentColor = getInnerCardColor()
                ),
                modifier = Modifier.fillMaxHeight().fillMaxWidth()
            ) {
                LazyRow(
                    modifier = Modifier.fillMaxHeight(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    item {
                        AddCardView()
                    }
                }
            }
        }
    }
}

@Composable
private fun TotalBalanceView(balances: BalanceData) {
    Row(
        modifier = Modifier.padding(horizontal = 8.dp)
            .fillMaxWidth(),
        horizontalArrangement = SpaceBetween,
    ) {
        Column {
            Text(
                "Total balance", color = Color.White, fontSize = 10.sp
            )

            Text(
                buildString {
                    append(if (balances.isVisibleToUser) balances.totalBalance else "***")
                    append(" ")
                    append(balances.currency.name)
                }, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp
            )
        }

        IconButton(
            onClick = {

            }
        ) {
            Icon(
                rememberVectorPainter(image = Icons.Filled.Favorite),
                contentDescription = Icons.Filled.Favorite.name,
                tint = Color.White
            )
        }
    }
}

@Composable
private fun getInnerCardColor(): Color =
    if (isSystemInDarkTheme()) MaterialTheme.colorScheme.surfaceContainer else Color.White