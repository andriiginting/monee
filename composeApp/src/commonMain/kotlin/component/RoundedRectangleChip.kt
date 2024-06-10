package component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RoundedRectangleChip(
    dates: List<String>,
    defaultSelectedItemIndex: Int = 0,
    onSelectedChanged: (Int) -> Unit = {}
) {
    var selectedItemIndex by remember { mutableStateOf(defaultSelectedItemIndex) }

    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp)) {
        items(dates.size) { index ->
            FilterChip(
                modifier = Modifier.padding(end = 6.dp),
                selected = dates[selectedItemIndex] == dates[index],
                colors = SelectableChipColors(
                    selectedLabelColor = MaterialTheme.colorScheme.secondaryContainer,
                    containerColor = MaterialTheme.colorScheme.surface,
                    disabledLabelColor = MaterialTheme.colorScheme.surface,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.secondaryContainer,
                    labelColor = MaterialTheme.colorScheme.primaryContainer,
                    leadingIconColor = MaterialTheme.colorScheme.primaryContainer,
                    trailingIconColor = MaterialTheme.colorScheme.primaryContainer,
                    disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.primaryContainer,
                    disabledSelectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedTrailingIconColor = MaterialTheme.colorScheme.secondaryContainer,
                ),
                border = BorderStroke(1.dp, color = MaterialTheme.colorScheme.primaryContainer),
                onClick = {
                    selectedItemIndex = index
                    onSelectedChanged(index)
                },
                label = {
                    Text(
                        dates[index],
                        color = MaterialTheme.colorScheme.primaryContainer
                    )
                },
            )
        }
    }
}