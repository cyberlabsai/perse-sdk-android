package ai.cyberlabs.perse

import ai.cyberlabs.perse.model.Detection
import ai.cyberlabs.perse.model.HeadMovement
import ai.cyberlabs.yoonit.camera.CameraView
import ai.cyberlabs.yoonit.camera.interfaces.CameraEventListener
import android.content.Context
import android.util.Pair

class PerseCamera(context: Context, apiKey: String): CameraView(context), CameraEventListener {

    var perseEventListener: PerseEventListener? = null

    val perse: Perse = Perse(apiKey)
    val url: String = "https://api.stg.getperse.com/v0/"

    init { this.configure() }

    override fun onImageCaptured(
        type: String,
        count: Int,
        total: Int,
        imagePath: String,
        inferences: ArrayList<Pair<String, FloatArray>>,
        darkness: Double,
        lightness: Double,
        sharpness: Double
    ) {
        this.perseEventListener?.let { eventListener ->
            this.perse.face.detect(
                imagePath,
                { detectResponse ->
                    eventListener.onImageCaptured(
                        count,
                        total,
                        imagePath,
                        Detection(
                                detectResponse.totalFaces,
                                detectResponse.faces,
                                detectResponse.imageMetrics,
                                detectResponse.timeTaken,
                                detectResponse.thresholds
                        )
                    )
                },
                {

                }
            )
        }
    }

    override fun onEndCapture() {
        this.perseEventListener?.onEndCapture()
    }

    override fun onError(error: String) {
        this.perseEventListener?.onError(error)
    }

    override fun onFaceDetected(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        leftEyeOpenProbability: Float?,
        rightEyeOpenProbability: Float?,
        smilingProbability: Float?,
        headEulerAngleX: Float,
        headEulerAngleY: Float,
        headEulerAngleZ: Float
    ) {
        this.perseEventListener?.let { eventListener ->
            var leftEye = false
            leftEyeOpenProbability?.let {
                if (it > 0.8) {
                    leftEye = true
                    return
                }
                leftEye = false
            }

            var rightEye = false
            rightEyeOpenProbability?.let {
                if (it > 0.8) {
                    rightEye = true
                    return
                }
                rightEye = false
            }

            var smiling = false
            smilingProbability?.let {
                if (it > 0.8) {
                    smiling = true
                    return
                }
                smiling = false
            }

            eventListener.onFaceDetected(
                x,
                y,
                width,
                height,
                leftEye,
                rightEye,
                smiling,
                getVertical(headEulerAngleX),
                getHorizontal(headEulerAngleY),
                getTilt(headEulerAngleZ)
            )
        }
    }

    override fun onFaceUndetected() {
        this.perseEventListener?.onFaceUndetected()
    }

    override fun onMessage(message: String) {
        this.perseEventListener?.onMessage(message)
    }

    override fun onPermissionDenied() {
        this.perseEventListener?.onPermissionDenied()
    }

    override fun onQRCodeScanned(content: String) {
        this.perseEventListener?.onQRCodeScanned(content)
    }

    private fun configure() {
        this.setCameraEventListener(this)
        this.setTimeBetweenImages(1000)
        this.startCaptureType("face")
        this.setSaveImageCaptured(true)
    }

    private fun getHorizontal(headEulerAngleY: Float): HeadMovement {
        return when {
            headEulerAngleY < -36 -> HeadMovement.HORIZONTAL_SUPER_LEFT
            headEulerAngleY > -36 && headEulerAngleY < -12 -> HeadMovement.HORIZONTAL_LEFT
            headEulerAngleY > -12 && headEulerAngleY < 12 -> HeadMovement.HORIZONTAL_NORMAL
            headEulerAngleY > 12 && headEulerAngleY < 36 -> HeadMovement.HORIZONTAL_RIGHT
            headEulerAngleY > 36 -> HeadMovement.HORIZONTAL_SUPER_RIGHT
            else -> HeadMovement.UNDEFINED
        }
    }

    private fun getVertical(headEulerAngleX: Float): HeadMovement {
        return when {
            headEulerAngleX < -36 -> HeadMovement.VERTICAL_SUPER_DOWN
            headEulerAngleX > -36 && headEulerAngleX < -12 -> HeadMovement.VERTICAL_DOWN
            headEulerAngleX > -12 && headEulerAngleX < 12 -> HeadMovement.VERTICAL_NORMAL
            headEulerAngleX > 12 && headEulerAngleX < 36 -> HeadMovement.VERTICAL_UP
            headEulerAngleX > 36 -> HeadMovement.VERTICAL_SUPER_UP
            else -> HeadMovement.UNDEFINED
        }
    }

    private fun getTilt(headEulerAngleZ: Float): HeadMovement {
        return when {
            headEulerAngleZ < -36 -> HeadMovement.TILT_SUPER_RIGHT
            headEulerAngleZ > -36 && headEulerAngleZ < -12 -> HeadMovement.TILT_RIGHT
            headEulerAngleZ > -12 && headEulerAngleZ < 12 -> HeadMovement.TILT_NORMAL
            headEulerAngleZ > 12 && headEulerAngleZ < 36 -> HeadMovement.TILT_LEFT
            headEulerAngleZ > 36 -> HeadMovement.TILT_SUPER_LEFT
            else -> HeadMovement.UNDEFINED
        }
    }
}