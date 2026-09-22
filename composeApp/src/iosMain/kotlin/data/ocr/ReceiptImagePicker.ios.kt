package data.ocr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.darwin.NSObject

@Composable
actual fun rememberReceiptImagePicker(
    onImageSelected: (ReceiptImage) -> Unit,
    onCancelled: () -> Unit,
): () -> Unit {
    val currentOnImageSelected = rememberUpdatedState(onImageSelected)
    val currentOnCancelled = rememberUpdatedState(onCancelled)
    val pickerDelegate = remember {
        ReceiptImagePickerDelegate(
            onImageSelected = { image ->
                currentOnImageSelected.value(ReceiptImage(image))
            },
            onCancelled = {
                currentOnCancelled.value()
            },
        )
    }

    return {
        val picker = UIImagePickerController()
        picker.sourceType =
            UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
        picker.delegate = pickerDelegate

        topViewController()
            ?.presentViewController(picker, animated = true, completion = null)
    }
}

private fun topViewController(): UIViewController? {
    val root = UIApplication.sharedApplication.keyWindow?.rootViewController
        ?: UIApplication.sharedApplication.windows
            .filterIsInstance<UIWindow>()
            .firstOrNull { it.isKeyWindow() }
            ?.rootViewController

    return root?.topViewController()
}

private fun UIViewController.topViewController(): UIViewController {
    return presentedViewController?.topViewController() ?: this
}

private class ReceiptImagePickerDelegate(
    private val onImageSelected: (UIImage) -> Unit,
    private val onCancelled: () -> Unit,
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>,
    ) {
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        if (image != null) {
            onImageSelected(image)
        }
        picker.dismissViewControllerAnimated(flag = true, completion = null)
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        onCancelled()
        picker.dismissViewControllerAnimated(flag = true, completion = null)
    }
}
