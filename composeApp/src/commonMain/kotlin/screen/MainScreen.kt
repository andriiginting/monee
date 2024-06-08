package screen

import androidx.compose.material.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import component.BottomBarView

@Composable
fun MainScreen() {
    Scaffold(
        backgroundColor = MaterialTheme.colorScheme.surface,
        bottomBar = {
            BottomBarView {
                // update navigation later
            }
        }
    ) {

    }
}