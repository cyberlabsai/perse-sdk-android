package ai.cyberlabs.persedemo.fragment

import ai.cyberlabs.perse.PerseEventListener
import ai.cyberlabs.perse.model.HeadMovement
import ai.cyberlabs.persedemo.BuildConfig
import ai.cyberlabs.persedemo.MainActivity
import ai.cyberlabs.persedemo.R
import ai.cyberlabs.perselite.model.DetectResponse
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.camera_fragment.*
import java.io.File

class CameraFragment: Fragment() {

    private val activity by lazy { requireActivity() as MainActivity }

    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.camera_fragment, container, false)
    }

    override fun onResume() {
        super.onResume()
        buildCameraView()
    }

    override fun onPause() {
        super.onPause()
        perse_camera.destroy()
    }

    private fun buildCameraView() {
        perse_camera.apiKey = BuildConfig.API_KEY
        perse_camera.perseEventListener = buildEventListener()

        if (allPermissionsGranted()) {
            perse_camera.startPreview()
            perse_camera.setDetectionBox(true)
            perse_camera.setFaceContours(true)
            return
        }

        requestPermissions(
            REQUIRED_PERMISSIONS,
            PackageManager.PERMISSION_GRANTED
        )
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            activity, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == PackageManager.PERMISSION_GRANTED) {
            if (allPermissionsGranted()) {
                perse_camera.startPreview()
                perse_camera.setDetectionBox(true)
                perse_camera.setFaceContours(true)
            } else {
                Toast.makeText(
                    activity,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun buildEventListener(): PerseEventListener = object : PerseEventListener {
        override fun onEndCapture() {
            Log.d("OnEndCapture", "OnEndCapture")
        }

        override fun onError(error: String) {
            Log.d("OnError", error)
        }

        override fun onImageCaptured(
            count: Int,
            total: Int,
            imagePath: String,
            detectResponse: DetectResponse?
        ) {
            val imageFile = File(imagePath)
            if (imageFile.exists()) {
                val imageBitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                image_preview.setImageBitmap(imageBitmap)
            }

            detectResponse?.let { detectResponse ->
                image_preview.visibility = View.VISIBLE

                setImageSharpness(
                    detectResponse.imageMetrics.sharpness,
                    detectResponse.defaultThresholds.sharpness
                )
                setImageUnderexposure(
                    detectResponse.imageMetrics.underexposure,
                    detectResponse.defaultThresholds.underexposure
                )
                setFaceSharpness(
                    detectResponse.faces.first().faceMetrics.sharpness,
                    detectResponse.defaultThresholds.sharpness
                )
                setFaceUnderexposure(
                    detectResponse.faces.first().faceMetrics.underexposure,
                    detectResponse.defaultThresholds.underexposure
                )

                if (
                    detectResponse
                        .faces
                        .first()
                        .livenessScore > detectResponse.defaultThresholds.liveness
                ) {
                    perse_camera.setDetectionBoxColor(255, 0, 255, 0)
                    perse_camera.setFaceContoursColor(255, 0, 255, 0)
                    return@let
                }
                perse_camera.setDetectionBoxColor(255, 255, 0, 0)
                perse_camera.setFaceContoursColor(255, 255, 0, 0)
            }
        }

        override fun onFaceDetected(
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
        ) {
            leftEyeOpen.let {
                if (it) {
                    left_eye_tv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_check,0,0,0)
                    return@let
                }
                left_eye_tv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_off,0,0,0)
            }
            rightEyeOpen.let {
                if (it) {
                    right_eye_tv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_check,0,0,0)
                    return@let
                }
                right_eye_tv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_off,0,0,0)
            }
            smiling.let {
                if (it) {
                    smiling_tv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_check,0,0,0)
                    return@let
                }
                smiling_tv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_off,0,0,0)
            }
            setFaceHorizontalMovement(headHorizontalMovement)
            setFaceVerticalMovement(headVerticalMovement)
            setFaceTiltMovement(headTiltMovement)
        }

        override fun onFaceUndetected() {
            perse_camera.setDetectionBoxColor(0,0,0,0)
            perse_camera.setFaceContoursColor(0,0,0,0)
            left_eye_tv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_neutral,0,0,0)
            right_eye_tv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_neutral,0,0,0)
            smiling_tv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_neutral,0,0,0)
            image_sharpness_tv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_neutral,0,0,0)
            image_underexposure_tv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_neutral,0,0,0)
            face_sharpness_tv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_neutral,0,0,0)
            face_underexposure_tv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_neutral,0,0,0)
            left_eye_tv.text = getString(R.string.left_eye_open)
            right_eye_tv.text = getString(R.string.right_eye_open)
            smiling_tv.text = getString(R.string.smiling)
            image_sharpness_tv.text = getString(R.string.image_sharpness)
            image_underexposure_tv.text = getString(R.string.image_underexposure)
            face_sharpness_tv.text = getString(R.string.face_sharpness)
            face_underexposure_tv.text = getString(R.string.face_underexposure)
            horizontal_movement_tv.text = getString(R.string.horizontal_movement)
            vertical_movement_tv.text = getString(R.string.vertical_movement)
            tilt_movement_tv.text = getString(R.string.tilt_movement)
            image_preview.visibility = View.GONE
        }

        override fun onMessage(message: String) {
            Log.d("OnEndCapture", message)
        }

        override fun onPermissionDenied() {
            Log.d("OnPermissionDenied", "onPermissionDenied")
        }

        override fun onQRCodeScanned(content: String) {
            Log.d("OnQRCodeScanned", content)
        }
    }

    private fun setImageSharpness(sharpness: Float, threshold: Float) {
        image_sharpness_tv.text = getString(R.string.image_sharpness)
        if (sharpness < threshold) {
            image_sharpness_tv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_check,0,0,0)
            return
        }
        image_sharpness_tv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_off,0,0,0)
    }

    private fun setImageUnderexposure(underexposure: Float, threshold: Float) {
        image_underexposure_tv.text = getString(R.string.image_underexposure_probability)
        if (underexposure < threshold) {
            image_underexposure_tv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_check,0,0,0)
            return
        }
        image_underexposure_tv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_off,0,0,0)
    }

    private fun setFaceSharpness(sharpness: Float, threshold: Float) {
        face_sharpness_tv.text = getString(R.string.face_sharpness)
        if (sharpness < threshold) {
            face_sharpness_tv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_check,0,0,0)
            return
        }
        face_sharpness_tv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_off,0,0,0)
    }

    private fun setFaceUnderexposure(underexposure: Float, threshold: Float) {
        face_underexposure_tv.text = getString(R.string.face_underexposure_probability)
        if (underexposure < threshold) {
            face_underexposure_tv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_check,0,0,0)
            return
        }
        face_underexposure_tv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_off,0,0,0)
    }

    private fun setFaceHorizontalMovement(headMovementY: HeadMovement) {
        when(headMovementY) {
            HeadMovement.HORIZONTAL_SUPER_LEFT -> {
                horizontal_movement_tv.text =
                    getString(
                        R.string.horizontal_movement_probability,
                        "Super Left"
                    )
            }
            HeadMovement.HORIZONTAL_LEFT -> {
                horizontal_movement_tv.text =
                    getString(
                        R.string.horizontal_movement_probability,
                        "Left"
                    )
            }
            HeadMovement.HORIZONTAL_NORMAL -> {
                horizontal_movement_tv.text =
                    getString(
                        R.string.horizontal_movement_probability,
                        "Frontal"
                    )
            }
            HeadMovement.HORIZONTAL_RIGHT -> {
                horizontal_movement_tv.text =
                    getString(
                        R.string.horizontal_movement_probability,
                        "Right"
                    )
            }
            HeadMovement.HORIZONTAL_SUPER_RIGHT -> {
                horizontal_movement_tv.text =
                    getString(
                        R.string.horizontal_movement_probability,
                        "Super Right"
                    )
            }
            else -> HeadMovement.UNDEFINED
        }
    }

    private fun setFaceVerticalMovement(headMovementX: HeadMovement) {
        when(headMovementX) {
            HeadMovement.VERTICAL_SUPER_DOWN -> {
                vertical_movement_tv.text =
                    getString(
                        R.string.vertical_movement_probability,
                        "Super Down"
                    )
            }
            HeadMovement.VERTICAL_DOWN -> {
                vertical_movement_tv.text =
                    getString(
                        R.string.vertical_movement_probability,
                        "Down"
                    )
            }
            HeadMovement.VERTICAL_NORMAL -> {
                vertical_movement_tv.text =
                    getString(
                        R.string.vertical_movement_probability,
                        "Frontal"
                    )
            }
            HeadMovement.VERTICAL_UP -> {
                vertical_movement_tv.text =
                    getString(
                        R.string.vertical_movement_probability,
                        "Up"
                    )
            }
            HeadMovement.VERTICAL_SUPER_UP -> {
                vertical_movement_tv.text =
                    getString(
                        R.string.vertical_movement_probability,
                        "Super Up"
                    )
            }
            else -> HeadMovement.UNDEFINED
        }
    }

    private fun setFaceTiltMovement(headMovementZ: HeadMovement) {
        when(headMovementZ) {
            HeadMovement.TILT_SUPER_RIGHT -> {
                tilt_movement_tv.text =
                    getString(
                        R.string.tilt_movement_probability,
                        "Super Right"
                    )
            }
            HeadMovement.TILT_RIGHT -> {
                tilt_movement_tv.text =
                    getString(
                        R.string.tilt_movement_probability,
                        "Right"
                    )
            }
            HeadMovement.TILT_NORMAL -> {
                tilt_movement_tv.text =
                    getString(
                        R.string.tilt_movement_probability,
                        "Frontal"
                    )
            }
            HeadMovement.TILT_LEFT -> {
                tilt_movement_tv.text =
                    getString(
                        R.string.tilt_movement_probability,
                        "Left"
                    )
            }
            HeadMovement.TILT_SUPER_LEFT -> {
                tilt_movement_tv.text = getString(
                    R.string.tilt_movement_probability,
                    "Super Left"
                )
            }
            else -> HeadMovement.UNDEFINED
        }
    }
}