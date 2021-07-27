package ai.cyberlabs.perse

import ai.cyberlabs.perse.model.Detection
import ai.cyberlabs.perse.model.HeadMovement

interface PerseEventListener {

    fun onImageCaptured(
        count: Int,
        total: Int,
        imagePath: String,
        detection: Detection
    )

    fun onFaceDetected(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        leftEyeOpen: Boolean,
        rightEyeOpen: Boolean,
        smiling: Boolean,
        headVerticalMovement: HeadMovement,
        headHorizontalMovement: HeadMovement,
        headTiltMovement: HeadMovement
    )

    fun onFaceUndetected()

    fun onEndCapture()

    fun onError(error: String)

    fun onMessage(message: String)

    fun onPermissionDenied()

    fun onQRCodeScanned(content: String)
}