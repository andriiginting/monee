package screen.home

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import component.WalletBalanceView
import data.getWalletBalanceData

@Composable
internal fun HomeScreen() {
    LazyColumn(
        modifier = Modifier.padding(16.dp)
    ) {
        item {
            WalletBalanceView(getWalletBalanceData())
            Text(
                text = "Expenses and Bills",
                color = MaterialTheme.colorScheme.inverseSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}