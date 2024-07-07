package component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import moneyproject.composeapp.generated.resources.Res
import moneyproject.composeapp.generated.resources.dismiss_ic
import org.jetbrains.compose.resources.painterResource

@Composable
fun TextFieldView(
    labelTitle: String,
    onValueChange: (String) -> Unit,
) {
    var billName by rememberSaveable { mutableStateOf("") }

    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = billName,
        onValueChange = {
            billName = it
            onValueChange(billName)
        },
        label = {
            Text(
                labelTitle,
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primaryContainer
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primaryContainer,
            disabledBorderColor = MaterialTheme.colorScheme.primaryContainer,
            unfocusedBorderColor = MaterialTheme.colorScheme.primaryContainer,
            disabledLabelColor = MaterialTheme.colorScheme.primaryContainer,
            focusedLabelColor = MaterialTheme.colorScheme.primaryContainer,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            errorContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedLabelColor = MaterialTheme.colorScheme.surfaceContainer,
            focusedTextColor = MaterialTheme.colorScheme.primaryContainer,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
        trailingIcon = {
            IconButton(onClick = {
                billName = ""
                onValueChange(billName)
            }) {
                Icon(
                    painter = painterResource(Res.drawable.dismiss_ic),
                    contentDescription = "Visibility Icon",
                    tint = MaterialTheme.colorScheme.primaryContainer
                )
            }
        },
    )
}