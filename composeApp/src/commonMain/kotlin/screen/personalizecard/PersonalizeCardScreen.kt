package screen.personalizecard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import component.MoneeButtonView
import component.TextFieldView
import component.getInnerCardColor
import data.CurrencyType
import moe.tlaster.precompose.navigation.Navigator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PersonalizeCardScreen(navigation: Navigator) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(colors = TopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                scrolledContainerColor = MaterialTheme.colorScheme.surface,
                navigationIconContentColor = MaterialTheme.colorScheme.inverseSurface,
                titleContentColor = MaterialTheme.colorScheme.inverseSurface,
                actionIconContentColor = MaterialTheme.colorScheme.inverseSurface
            ), title = {
                Text(
                    "Personalize Pocket",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.inverseSurface
                )
            }, navigationIcon = {
                IconButton(onClick = {
                    navigation.goBack()
                }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        Icons.AutoMirrored.Filled.ArrowBack.name,
                        tint = MaterialTheme.colorScheme.inverseSurface
                    )
                }
            })
        },
        modifier = Modifier.statusBarsPadding(),
    ) { innerPadding ->
        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        var pocketName by rememberSaveable { mutableStateOf("Card Name") }
                        var initialBalance by rememberSaveable { mutableStateOf("0") }
                        var currencyType by remember { mutableStateOf(CurrencyType.JPY) }

                        PersonalCard(
                            pocketName, initialBalance, currencyType
                        )

                        Spacer(Modifier.height(30.dp))

                        TextFieldView("Pocket Name", onValueChange = { label ->
                            pocketName = label
                        })

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CurrencyTypeOptionView(currencyType)

                            TextFieldView("Initial balance", onValueChange = { label ->
                                initialBalance = label
                            })
                        }
                    }
                }
                Column(
                    modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                ) {
                    MoneeButtonView("Create Card") {

                    }
                }

            }
        }
    }
}

@Composable
private fun PersonalCard(cardName: String, initialBalance: String, currencyType: CurrencyType) {
    ElevatedCard(
        modifier = Modifier.width(200.dp).height(110.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        colors = CardColors(
            contentColor = getInnerCardColor(),
            containerColor = getInnerCardColor(),
            disabledContentColor = getInnerCardColor(),
            disabledContainerColor = getInnerCardColor()
        ),
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.padding(8.dp)
        ) {

            Text(
                cardName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.inverseSurface,
                modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
                maxLines = 1,
                textAlign = TextAlign.End
            )

            Text(
                buildString {
                    append(initialBalance)
                    append(" ")
                    append(currencyType)
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                color = MaterialTheme.colorScheme.inverseSurface,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun CurrencyTypeOptionView(currencyType: CurrencyType) {
    Card(
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier
            .height(OutlinedTextFieldDefaults.MinHeight)
            .width(56.dp),
        colors = CardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.surface,
            disabledContentColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                "Currency",
                color = MaterialTheme.colorScheme.primaryContainer,
                fontSize = 10.sp,
            )

            Text(
                currencyType.name,
                color = MaterialTheme.colorScheme.inverseSurface,
                fontSize = 16.sp,
            )
        }
    }
}
