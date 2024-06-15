package screen.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import component.WalletBalanceView
import data.getWalletBalanceData
import moe.tlaster.precompose.navigation.Navigator

@Composable
internal fun HomeScreen(navigator: Navigator) {
    LazyColumn(
        modifier = Modifier.padding(16.dp).fillMaxSize()
    ) {
        item {
            WalletBalanceView(getWalletBalanceData(), navigator)
            ExpenseBillsSectionView()
        }
    }
}