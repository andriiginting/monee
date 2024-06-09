package screen.home

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
internal fun HomeScreen(modifier: Modifier) {
    LazyColumn {
        item {
            Text(
                text = "Expenses and Bills",
                color = MaterialTheme.colorScheme.inverseSurface,
                modifier = modifier,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}