import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
expect fun ConfigureSystemBars(
    backgroundColor: Color,
    darkTheme: Boolean,
)
