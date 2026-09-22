package data.ocr

data class ReceiptOcrResult(
    val text: String,
)

expect class ReceiptImage

expect suspend fun recognizeReceiptText(image: ReceiptImage): ReceiptOcrResult
