package screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import data.MoneeRepository
import moe.tlaster.precompose.navigation.Navigator
import moneyproject.composeapp.generated.resources.Res
import moneyproject.composeapp.generated.resources.splash_ic
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun SplashScreen(navigator: Navigator, repository: MoneeRepository) {
    val session by repository.session.collectAsState()
    val sessionRestored by repository.sessionRestored.collectAsState()

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Image(
            painterResource(Res.drawable.splash_ic),
            null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )
        LaunchedEffect(sessionRestored, session) {
            if (sessionRestored) {
                navigator.navigate(
                    if (session == null) {
                        navigation.Navigator.AUTH.route
                    } else {
                        navigation.Navigator.HOME.route
                    },
                )
            }
        }
    }
}
