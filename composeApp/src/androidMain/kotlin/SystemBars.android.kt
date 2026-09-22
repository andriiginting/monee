import android.app.Activity
import androidx.core.view.WindowCompat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView

@Composable
actual fun ConfigureSystemBars(
    backgroundColor: Color,
    darkTheme: Boolean,
) {
    val view = LocalView.current

    SideEffect {
        val activity = view.context as? Activity ?: return@SideEffect
        val window = activity.window

        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
    }
}
