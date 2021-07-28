package ai.cyberlabs.perse

import ai.cyberlabs.perse.model.HeadMovement
import ai.cyberlabs.perselite.model.DetectResponse

interface PerseEventListener {

    fun onImageCaptured(
        count: Int,
        total: Int,
        imagePath: String,
        detectResponse: DetectResponse
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