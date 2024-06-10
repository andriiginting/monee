package screen.history

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import component.RoundedRectangleChip
import data.getAllHistoriesDate

@Composable
internal fun HistoryScreen() {
    Column {
        Text(
            modifier = Modifier.padding(top = 16.dp, start = 16.dp),
            text = "Expenses History",
            color = MaterialTheme.colorScheme.inverseSurface,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        RoundedRectangleChip(
            dates = getAllHistoriesDate()
        ) { index ->

        }
    }
}