package component

import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun TextWithIconCard(
    title: String,
    icon: Icon,
    modifier: Modifier,
    action: () -> Unit
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
    ) {

    }
}

sealed class Icon {
    data class Emoji(val resource: Int)

    data class Dynamic(val url: String)
}