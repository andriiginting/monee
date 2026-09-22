package data.ocr

import androidx.compose.runtime.Composable

@Composable
expect fun rememberReceiptImagePicker(
    onImageSelected: (ReceiptImage) -> Unit,
    onCancelled: () -> Unit,
): () -> Unit
