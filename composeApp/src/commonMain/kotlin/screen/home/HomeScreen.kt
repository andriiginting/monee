package screen.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.MoneeRepository
import data.forHousehold
import moe.tlaster.precompose.navigation.Navigator

@Composable
internal fun HomeScreen(navigator: Navigator, repository: MoneeRepository) {
    val session by repository.session.collectAsState()
    val expenses by repository.expenses.collectAsState()
    LazyColumn(
        modifier = Modifier.padding(16.dp).fillMaxSize()
    ) {
        item {
            ExpenseBillsSectionView(
                household = session?.household?.name ?: "Household",
                expenses = expenses.forHousehold(session?.household?.id),
                onViewAll = { navigator.navigate(navigation.Navigator.HISTORY.route) },
            )
        }
    }
}
