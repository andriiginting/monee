package component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIImage
import platform.UIKit.UIImageView
import platform.UIKit.UIViewContentMode
import qrcodeinterop.moneeCreateQrImage

@OptIn(ExperimentalForeignApi::class)
@Composable
internal actual fun QrCodeView(
    payload: String,
    modifier: Modifier,
) {
    UIKitView(
        factory = {
            UIImageView().apply {
                contentMode = UIViewContentMode.UIViewContentModeScaleAspectFit
            }
        },
        modifier = modifier,
        update = { imageView ->
            imageView.image = payload.toQrImage()
        },
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun String.toQrImage(): UIImage? {
    return moneeCreateQrImage(this)?.let(UIImage.Companion::imageWithCGImage)
}
