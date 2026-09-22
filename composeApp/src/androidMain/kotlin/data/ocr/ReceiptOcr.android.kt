package data.ocr

import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual class ReceiptImage(
    internal val uri: Uri,
)

actual suspend fun recognizeReceiptText(image: ReceiptImage): ReceiptOcrResult =
    suspendCancellableCoroutine { continuation ->
        val inputImage = InputImage.fromFilePath(AndroidReceiptOcrContext.value, image.uri)
        TextRecognition
            // The Japanese model also recognises latin script, so this one
            // recogniser covers both the JP and EN receipts the parser handles.
            .getClient(JapaneseTextRecognizerOptions.Builder().build())
            .process(inputImage)
            .addOnSuccessListener { result ->
                continuation.resume(ReceiptOcrResult(text = result.text))
            }
            .addOnFailureListener {
                continuation.resume(ReceiptOcrResult(text = ""))
            }
    }
