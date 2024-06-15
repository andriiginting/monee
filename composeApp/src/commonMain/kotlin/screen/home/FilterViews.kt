package screen.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import moneyproject.composeapp.generated.resources.Res
import moneyproject.composeapp.generated.resources.receipt_ic
import moneyproject.composeapp.generated.resources.saving_ic
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun FilterViews(defaultSelectedItemIndex: Int = 0, onSelectedChanged: (Int) -> Unit = {}) {
    var selectedItemIndex by remember { mutableStateOf(defaultSelectedItemIndex) }

    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp)) {
        items(availableFilter.size) { index ->

            val isSelected = availableFilter[selectedItemIndex] == availableFilter[index]

            FilterChip(
                modifier = Modifier.padding(end = 6.dp),
                selected = isSelected,
                shape = RoundedCornerShape(16.dp),
                colors = SelectableChipColors(
                    selectedLabelColor = MaterialTheme.colorScheme.primaryContainer,
                    containerColor = MaterialTheme.colorScheme.surface,
                    disabledLabelColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.primaryContainer,
                    labelColor = MaterialTheme.colorScheme.primaryContainer,
                    leadingIconColor = MaterialTheme.colorScheme.primaryContainer,
                    trailingIconColor = MaterialTheme.colorScheme.primaryContainer,
                    disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.secondaryContainer,
                    disabledSelectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedTrailingIconColor = MaterialTheme.colorScheme.secondaryContainer,
                ),
                border = if (isSelected) {
                    BorderStroke(1.dp, color = MaterialTheme.colorScheme.primaryContainer)
                } else {
                    null
                },
                leadingIcon = {
                    Icon(
                        painterResource(availableFilter[index].icon),
                        availableFilter[index].name,
                        tint = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        }
                    )
                },
                onClick = {
                    selectedItemIndex = index
                    onSelectedChanged(index)
                },
                label = {
                    Text(
                        availableFilter[index].name,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        }
                    )
                },
            )
        }
    }
}

private val availableFilter = listOf(MoneyFilterType.Bills, MoneyFilterType.Saving)

internal sealed class MoneyFilterType {
    abstract val icon: DrawableResource
    abstract val name: String
    abstract val id: String

    data object Bills : MoneyFilterType() {
        override val icon: DrawableResource
            get() = Res.drawable.receipt_ic
        override val name: String
            get() = "Bills"
        override val id: String
            get() = "bill"
    }

    data object Saving : MoneyFilterType() {
        override val icon: DrawableResource
            get() = Res.drawable.saving_ic
        override val name: String
            get() = "Saving"
        override val id: String
            get() = "saving"
    }
}