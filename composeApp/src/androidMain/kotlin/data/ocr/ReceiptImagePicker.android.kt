package data.ocr

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

@Composable
actual fun rememberReceiptImagePicker(
    onImageSelected: (ReceiptImage) -> Unit,
    onCancelled: () -> Unit,
): () -> Unit {
    val currentOnImageSelected by rememberUpdatedState(onImageSelected)
    val pendingCameraUri = remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { captured ->
        if (captured) {
            pendingCameraUri.value?.let { currentOnImageSelected(ReceiptImage(it)) }
        } else {
            onCancelled()
        }
        pendingCameraUri.value = null
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) launchFullResolutionCamera(cameraLauncher, pendingCameraUri)
        else onCancelled()
    }

    return {
        val hasPermission = ContextCompat.checkSelfPermission(
            AndroidReceiptOcrContext.value,
            Manifest.permission.CAMERA,
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            launchFullResolutionCamera(cameraLauncher, pendingCameraUri)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
}

private fun launchFullResolutionCamera(
    launcher: androidx.activity.result.ActivityResultLauncher<Uri>,
    pendingCameraUri: androidx.compose.runtime.MutableState<Uri?>,
) {
    val file = File(
        AndroidReceiptOcrContext.value.cacheDir,
        "receipt-${System.currentTimeMillis()}.jpg",
    )
    val uri = FileProvider.getUriForFile(
        AndroidReceiptOcrContext.value,
        "${AndroidReceiptOcrContext.value.packageName}.fileprovider",
        file,
    )
    pendingCameraUri.value = uri
    launcher.launch(uri)
}
