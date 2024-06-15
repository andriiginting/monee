package screen.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import component.TextWithEmojiCard

@Composable
internal fun ExpenseBillsSectionView() {
    Column {
        Text(
            text = "Expenses and Bills",
            color = MaterialTheme.colorScheme.inverseSurface,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        FilterViews() {

        }

        TextWithEmojiCard("Create Pocket", Modifier) {

        }
    }
}