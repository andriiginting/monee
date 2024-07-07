package component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
internal fun MoneeButtonView(
    title: String,
    action: () -> Unit,
) {
    Button(
        modifier = Modifier.fillMaxWidth()
            .height(55.dp),
        contentPadding = PaddingValues(16.dp),
        onClick = {
            action.invoke()
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.primaryContainer,
            disabledContentColor = MaterialTheme.colorScheme.primaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.primaryContainer
        ),
    ) {
        Text(
            title,
            textAlign = TextAlign.Center,
            color =  MaterialTheme.colorScheme.secondaryContainer,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}
