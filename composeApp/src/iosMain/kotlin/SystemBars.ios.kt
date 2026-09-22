import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
actual fun ConfigureSystemBars(
    backgroundColor: Color,
    darkTheme: Boolean,
) {
    // iOS status bar appearance is controlled by the host UIViewController.
}
