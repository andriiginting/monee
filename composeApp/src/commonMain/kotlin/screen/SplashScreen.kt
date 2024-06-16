package screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.delay
import moe.tlaster.precompose.navigation.Navigator
import moneyproject.composeapp.generated.resources.Res
import moneyproject.composeapp.generated.resources.splash_ic
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun SplashScreen(navigator: Navigator) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Image(
            painterResource(Res.drawable.splash_ic),
            null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )
        LaunchedEffect(this) {
            delay(3000)
            navigator.navigate(navigation.Navigator.HOME.route)
        }
    }
}