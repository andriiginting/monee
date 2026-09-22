package data.ocr

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UIKit.UIImage
import platform.Vision.VNImageRequestHandler
import platform.Vision.VNRecognizeTextRequest
import platform.Vision.VNRecognizedText
import platform.Vision.VNRecognizedTextObservation
import platform.Vision.VNRequestTextRecognitionLevelAccurate
import kotlinx.cinterop.ExperimentalForeignApi
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class)
actual class ReceiptImage(
    internal val image: UIImage,
)

@OptIn(ExperimentalForeignApi::class)
actual suspend fun recognizeReceiptText(image: ReceiptImage): ReceiptOcrResult =
    suspendCancellableCoroutine { continuation ->
        val cgImage = image.image.CGImage
        if (cgImage == null) {
            continuation.resume(ReceiptOcrResult(text = ""))
            return@suspendCancellableCoroutine
        }

        val request = VNRecognizeTextRequest { request, _ ->
            val observations = request?.results
                ?.filterIsInstance<VNRecognizedTextObservation>()
            val text = observations
                ?.mapNotNull { observation ->
                    (observation.topCandidates(1uL).firstOrNull() as? VNRecognizedText)?.string
                }
                ?.joinToString(separator = "\n")
                .orEmpty()

            continuation.resume(ReceiptOcrResult(text = text))
        }
        request.recognitionLevel = VNRequestTextRecognitionLevelAccurate
        request.recognitionLanguages = listOf("ja-JP", "en-US")
        request.usesLanguageCorrection = true

        val handler = VNImageRequestHandler(
            cGImage = cgImage,
            options = emptyMap<Any?, Any?>(),
        )

        try {
            handler.performRequests(listOf(request), error = null)
        } catch (_: Throwable) {
            continuation.resume(ReceiptOcrResult(text = ""))
        }
    }
