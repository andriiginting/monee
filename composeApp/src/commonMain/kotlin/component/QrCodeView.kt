package component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal expect fun QrCodeView(
    payload: String,
    modifier: Modifier = Modifier,
)
