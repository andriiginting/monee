package screen.scanner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.ocr.CATEGORY_UNKNOWN
import data.ocr.ReceiptDraft
import data.ocr.ReceiptLineItem
import data.ocr.ReceiptImage
import data.ocr.formatReceiptAmount
import data.ocr.parseReceiptText
import data.ocr.recognizeReceiptText
import data.ocr.rememberReceiptImagePicker
import kotlinx.coroutines.launch
import moe.tlaster.precompose.navigation.Navigator

private enum class ScannerStep {
    Processing,
    Review,
}

private val ScannerBackground = Color(0xFF101512)


@Composable
internal fun ScannerScreen(
    navigator: Navigator,
) {
    val scope = rememberCoroutineScope()
    var step by remember { mutableStateOf(ScannerStep.Processing) }
    var receiptDraft by remember { mutableStateOf<ReceiptDraft?>(null) }

    val processImage: (ReceiptImage) -> Unit = { image ->
        scope.launch {
            step = ScannerStep.Processing
            receiptDraft = parseReceiptText(recognizeReceiptText(image).text)
            step = ScannerStep.Review
        }
    }
    val launchCamera = rememberReceiptImagePicker(
        onImageSelected = processImage,
        onCancelled = navigator::goBack,
    )

    LaunchedEffect(Unit) {
        launchCamera()
    }

    when (step) {
        ScannerStep.Processing -> Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        )

        ScannerStep.Review -> ScannerReviewView(
            draft = receiptDraft ?: ReceiptDraft(
                totalMinor = null,
                date = null,
                time = null,
                location = null,
                lineItems = emptyList(),
            ),
            onClose = navigator::goBack,
            onRescan = {
                receiptDraft = null
                step = ScannerStep.Processing
                launchCamera()
            },
        )
    }
}

@Composable
private fun ScannerReviewView(
    draft: ReceiptDraft,
    onClose: () -> Unit,
    onRescan: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Box(modifier = Modifier) {
                ScannerHeader(
                    title = "Review Draft",
                    onClose = onClose,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    action = {
                        Button(
                            onClick = onClose,
                            shape = RoundedCornerShape(32.dp),
                        ) {
                            Text(
                                text = "Save",
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item {
                Text(
                    text = "EXTRACTED TOTAL",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.6.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                )
            }

            item {
                Text(
                    text = formatReceiptAmount(draft.totalMinor),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ReviewInfoCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.CalendarMonth,
                        label = "DATE",
                        value = draft.date ?: "Unknown",
                        extracted = draft.date != null,
                    )
                    ReviewInfoCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.Schedule,
                        label = "TIME",
                        value = draft.time ?: "Unknown",
                        extracted = draft.time != null,
                    )
                }
            }

            item {
                ReviewInfoCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    icon = Icons.Outlined.LocationOn,
                    label = "LOCATION",
                    value = draft.location ?: "Unknown",
                    extracted = draft.location != null,
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    ReviewSectionLabel(
                        icon = Icons.Outlined.ReceiptLong,
                        text = "ITEMIZED RECEIPT",
                    )
                    Text(
                        text = if (draft.lineItems.size == 1) "1 item" else "${draft.lineItems.size} items",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                    )
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(24.dp),
                        ),
                ) {
                    Column {
                        if (draft.lineItems.isEmpty()) {
                            Text(
                                text = "No line items detected.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(20.dp),
                            )
                        } else {
                            draft.lineItems.forEach { item ->
                                ReviewLineItemRow(item = item, category = draft.category)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(MaterialTheme.colorScheme.outlineVariant),
                                )
                            }
                            ItemsSumFooter(draft = draft)
                        }
                    }
                }
            }

            item {
                ReviewSelectorCard(
                    label = "ACCOUNT (SOURCE)",
                    value = draft.account,
                    modifier = Modifier.padding(top = 32.dp),
                )
            }

            item {
                ReviewSelectorCard(
                    label = "CATEGORY (DESTINATION)",
                    value = draft.category,
                    highlighted = true,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }

            item {
                TextButton(
                    onClick = onRescan,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                ) {
                    Text("Scan another receipt")
                }
            }
        }
    }
}

/**
 * A parsed item. The parser reports a name and an amount only, so the category
 * shown is the draft-level one the user still has to confirm; flagging that
 * explicitly is more honest than printing a guess that looks already decided.
 */
@Composable
private fun ReviewLineItemRow(
    item: ReceiptLineItem,
    category: String,
) {
    val uncategorised = category == CATEGORY_UNKNOWN
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (uncategorised) {
                    MaterialTheme.colorScheme.error.copy(alpha = 0.04f)
                } else {
                    Color.Transparent
                },
            )
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(6.dp))
            if (uncategorised) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.WarningAmber,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(14.dp),
                    )
                    CategoryChip(
                        text = "UNCATEGORISED",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 6.dp),
                    )
                }
            } else {
                CategoryChip(
                    text = category,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        Text(
            text = formatReceiptAmount(item.amountMinor),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}

@Composable
private fun CategoryChip(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = color,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

/**
 * Whether the parsed items account for the parsed total. A mismatch is the
 * clearest signal that the scan dropped a row or paired one against the wrong
 * price, so it is surfaced rather than hidden behind a silent success.
 */
@Composable
private fun ItemsSumFooter(draft: ReceiptDraft) {
    val sum = draft.lineItems.sumOf { it.amountMinor }
    val total = draft.totalMinor
    val matches = total != null && sum == total
    val tint = if (matches) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Items sum",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (matches) Icons.Outlined.Check else Icons.Outlined.WarningAmber,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = when {
                    total == null -> "No total found"
                    matches -> "Matches ${formatReceiptAmount(total)}"
                    else -> "${formatReceiptAmount(sum)} of ${formatReceiptAmount(total)}"
                },
                color = tint,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 6.dp),
            )
        }
    }
}

/**
 * One extracted metadata field. [extracted] marks a value the scan actually
 * found, so a placeholder is never dressed up as a confident reading.
 */
@Composable
private fun ReviewInfoCard(
    modifier: Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    extracted: Boolean,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(20.dp),
            )
            .padding(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = label,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 6.dp),
                )
            }
            if (extracted) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = "Read from the receipt",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(12.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = value,
                color = if (extracted) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier.weight(1f, fill = false),
            )
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = "Edit $label",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(12.dp),
            )
        }
    }
}

@Composable
private fun ReviewSectionLabel(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Composable
private fun ReviewSelectorCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                if (highlighted) {
                    MaterialTheme.colorScheme.surfaceVariant
                } else {
                    MaterialTheme.colorScheme.surface
                },
            )
            .border(
                width = if (highlighted) 2.dp else 1.dp,
                color = if (highlighted) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                },
                shape = RoundedCornerShape(24.dp),
            )
            .padding(20.dp),
    ) {
        Column {
            Text(
                text = label,
                color = if (highlighted) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = value,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowDown,
                    contentDescription = "Choose $label",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ScannerSurface(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScannerBackground),
    ) {
        content()
    }
}

@Composable
private fun ScannerHeader(
    title: String,
    onClose: () -> Unit,
    contentColor: Color = Color.White,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Close scanner",
                tint = contentColor,
            )
        }

        Text(
            text = title,
            color = contentColor,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )

        Box(
            modifier = if (action == null) {
                Modifier.size(48.dp)
            } else {
                Modifier
                    .width(120.dp)
                    .height(48.dp)
            },
            contentAlignment = Alignment.Center,
        ) {
            action?.invoke()
        }
    }
}
