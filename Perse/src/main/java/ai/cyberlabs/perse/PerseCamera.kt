/**
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * | PerseLite is lib for Android applications                       |
 * | Haroldo Teruya & Victor Goulart @ Cyberlabs AI 2021             |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 */

package ai.cyberlabs.perse

import ai.cyberlabs.perse.model.HeadMovement
import ai.cyberlabs.yoonit.camera.CameraView
import ai.cyberlabs.yoonit.camera.interfaces.CameraEventListener
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.util.Pair

/**
 * CameraView component integrated with YoonitCamera and PerseLite
 */
class PerseCamera @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
): CameraView(context,attrs, defStyle,defStyleRes), CameraEventListener {

    var eventListener: PerseEventListener? = null

    var perse: Perse? = null

    init { this.configure() }

    fun startPerse(apiKey: String, perseEventListener: PerseEventListener?) {
        this.perse = Perse(apiKey)
        this.eventListener = perseEventListener
    }

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
        this.eventListener?.let { eventListener ->
            this.perse?.let {
                it.face.detect(
                    imagePath,
                    { detectResponse ->
                        eventListener.onImageCaptured(
                            count,
                            total,
                            imagePath,
                            detectResponse
                        )
                    },
                    {
                        Log.d("debug_error", it)
                    }
                )
            }
        }
    }

    override fun onEndCapture() {
        this.eventListener?.onEndCapture()
    }

    override fun onError(error: String) {
        this.eventListener?.onError(error)
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
        this.eventListener?.let { eventListener ->
            var leftEye = false
            leftEyeOpenProbability?.let {
                if (it > 0.8) {
                    leftEye = true
                    return@let
                }
                leftEye = false
            }

            var rightEye = false
            rightEyeOpenProbability?.let {
                if (it > 0.8) {
                    rightEye = true
                    return@let
                }
                rightEye = false
            }

            var smiling = false
            smilingProbability?.let {
                if (it > 0.8) {
                    smiling = true
                    return@let
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
        this.eventListener?.onFaceUndetected()
    }

    override fun onMessage(message: String) {
        this.eventListener?.onMessage(message)
    }

    override fun onPermissionDenied() {
        this.eventListener?.onPermissionDenied()
    }

    override fun onQRCodeScanned(content: String) {
        this.eventListener?.onQRCodeScanned(content)
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