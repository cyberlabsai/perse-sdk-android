/**
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * | Perse SDK Android.                                              |
 * | More About: https://www.getperse.com/                           |
 * | From CyberLabs.AI: https://cyberlabs.ai/                        |
 * | Haroldo Teruya & Victor Goulart @ Cyberlabs AI 2021             |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 */

package ai.cyberlabs.perse

import ai.cyberlabs.perse.model.HeadMovement
import ai.cyberlabs.perselite.PerseLite
import ai.cyberlabs.yoonit.camera.CameraView
import ai.cyberlabs.yoonit.camera.interfaces.CameraEventListener
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.util.Pair

/**
 * CameraView component integrated with YoonitCamera and PerseLite.
 */
class PerseCamera @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
): CameraView(context,attrs, defStyle,defStyleRes), CameraEventListener {

    var apiKey: String? = null
        set(value) {
            value?.let {
                this.perse = PerseLite(it, BuildConfig.BASE_URL)
                field = value
            }
        }

    var perseEventListener: PerseEventListener? = null
    private lateinit var perse: PerseLite

    init { this.configure() }

    private fun configure() {
        this.setCameraEventListener(this)
        this.startCaptureType("face")
        this.setSaveImageCaptured(true)
        this.setTimeBetweenImages(1000)
        this.detectionTopSize = 0.2f
        this.detectionRightSize = 0.2f
        this.detectionBottomSize = 0.2f
        this.detectionLeftSize = 0.2f
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
        this.perseEventListener?.let { perseEventListener ->
            if (apiKey == null) {
                Log.w(TAG, "No API Key set. To get a API Key: https://github.com/cyberlabsai/perse-sdk-android/wiki/2.-API-Key.")
                perseEventListener.onImageCaptured(
                    count,
                    total,
                    imagePath,
                    null
                )
                return@let
            }

            this.perse.face.detect(
                imagePath,
                { detectResponse ->
                    perseEventListener.onImageCaptured(
                        count,
                        total,
                        imagePath,
                        detectResponse
                    )
                },
                { error ->
                    perseEventListener.onError(error)
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
        this.perseEventListener?.let { perseEventListener ->
            var leftEye = false
            leftEyeOpenProbability?.let {
                leftEye = it > 0.8
            }

            var rightEye = false
            rightEyeOpenProbability?.let {
                rightEye = it > 0.8
            }

            var smiling = false
            smilingProbability?.let {
                smiling = it > 0.8
            }

            perseEventListener.onFaceDetected(
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

    companion object {
        const val TAG: String = "PerseCamera"
    }
}