package component

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

@Composable
internal actual fun QrCodeView(
    payload: String,
    modifier: Modifier,
) {
    val bitmap = remember(payload) {
        val size = 512
        val matrix = QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, size, size)
        Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).also { output ->
            for (y in 0 until size) {
                for (x in 0 until size) {
                    output.setPixel(
                        x,
                        y,
                        if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE,
                    )
                }
            }
        }
    }

    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "Invite QR code",
        modifier = modifier,
    )
}
