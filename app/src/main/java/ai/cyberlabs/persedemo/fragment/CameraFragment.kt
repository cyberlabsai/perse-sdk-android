package ai.cyberlabs.persedemo.fragment

import ai.cyberlabs.perse.PerseCamera
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

    private lateinit var perseCamera: PerseCamera

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buildCameraView()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.camera_fragment, container, false)
    }

    override fun onPause() {
        super.onPause()
        perseCamera.destroy()
    }

    override fun onResume() {
        super.onResume()
        buildCameraView()
    }

    private fun buildCameraView() {
        perseCamera = camera_view
        perseCamera.startPerse(BuildConfig.API_KEY, buildEventListener())

        if (allPermissionsGranted()) {
            perseCamera.startPreview()
            perseCamera.startCaptureType("face")
            perseCamera.setSaveImageCaptured(true)
            perseCamera.setDetectionBox(true)
            perseCamera.setFaceContours(true)
            perseCamera.setSaveImageCaptured(true)
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
                perseCamera.startPreview()
                perseCamera.startCaptureType("frame")
                perseCamera.setSaveImageCaptured(true)
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

        override fun onImageCaptured(count: Int,
                                     total: Int,
                                     imagePath: String,
                                     detectResponse: DetectResponse) {
            setImageSharpness(detectResponse.imageMetrics.sharpness)
            setImageUnderexpose(detectResponse.imageMetrics.underexpose)
            setFaceSharpness(detectResponse.faces.first().faceMetrics.sharpness)
            setFaceUnderexpose(detectResponse.faces.first().faceMetrics.underexpose)

            detectResponse.let {
                if (it.faces.first().livenessScore > 0.8) {
                    perseCamera.setDetectionBoxColor(255,0,255,0)
                    perseCamera.setFaceContoursColor(255,0,255,0)
                    return@let
                }
                perseCamera.setDetectionBoxColor(255,255,0,0)
                perseCamera.setFaceContoursColor(255,255,0,0)
            }

            image_preview.visibility = View.VISIBLE
            val imageFile = File(imagePath)
            if (imageFile.exists()) {
                val imageBitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                image_preview.setImageBitmap(imageBitmap)
            }
        }

        override fun onFaceDetected(x: Int,
                                    y: Int,
                                    width: Int,
                                    height: Int,
                                    leftEyeOpen: Boolean,
                                    rightEyeOpen: Boolean,
                                    smiling: Boolean,
                                    headVerticalMovement: HeadMovement,
                                    headHorizontalMovement: HeadMovement,
                                    headTiltMovement: HeadMovement) {
            leftEyeOpen.let {
                if (it) {
                    left_eye_tv.text = getString(R.string.open_label)
                    left_eye_tv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_check,0,0,0)
                    return@let
                }
                left_eye_tv.text = getString(R.string.close_label)
                left_eye_tv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_off,0,0,0)
            }
            rightEyeOpen.let {
                if (it) {
                    right_eye_tv.text = getString(R.string.open_label)
                    right_eye_tv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_check,0,0,0)
                    return@let
                }
                right_eye_tv.text = getString(R.string.close_label)
                right_eye_tv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_off,0,0,0)
            }
            smiling.let {
                if (it) {
                    smiling_tv.text = getString(R.string.open_label)
                    smiling_tv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_check,0,0,0)
                    return@let
                }
                smiling_tv.text = getString(R.string.close_label)
                smiling_tv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_off,0,0,0)
            }
            setFaceHorizontalMovement(headHorizontalMovement)
            setFaceVerticalMovement(headVerticalMovement)
            setFaceTiltMovement(headTiltMovement)
        }

        override fun onFaceUndetected() {
            perseCamera.setDetectionBoxColor(0,0,0,0)
            perseCamera.setFaceContoursColor(0,0,0,0)
            left_eye_tv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_neutral,0,0,0)
            right_eye_tv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_neutral,0,0,0)
            smiling_tv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_neutral,0,0,0)
            image_sharpness_tv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_neutral,0,0,0)
            image_underexpose_tv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_neutral,0,0,0)
            face_sharpness_tv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_neutral,0,0,0)
            face_underexpose_tv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_neutral,0,0,0)
            left_eye_tv.text = getString(R.string.left_eye_open)
            right_eye_tv.text = getString(R.string.right_eye_open)
            smiling_tv.text = getString(R.string.smiling)
            image_sharpness_tv.text = getString(R.string.image_sharpness)
            image_underexpose_tv.text = getString(R.string.image_underexpose)
            face_sharpness_tv.text = getString(R.string.face_sharpness)
            face_underexpose_tv.text = getString(R.string.face_underexpose)
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

    private fun setImageSharpness(sharpness: Float) {
        image_sharpness_tv.text =
            getString(
                R.string.image_sharpness_probability,
                sharpness.times(100).toString().substring(0,4)
            )
        if (sharpness < 0.5) {
            image_sharpness_tv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_check,0,0,0)
            return
        }
        image_sharpness_tv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_off,0,0,0)
    }

    private fun setImageUnderexpose(underexpose: Float) {
        image_underexpose_tv.text =
            getString(
                R.string.image_underexpose_probability,
                underexpose.times(100).toString().substring(0,4)
            )
        if (underexpose < 0.5) {
            image_underexpose_tv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_check,0,0,0)
            return
        }
        image_underexpose_tv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_off,0,0,0)
    }

    private fun setFaceSharpness(sharpness: Float) {
        face_sharpness_tv.text =
            getString(
                R.string.face_sharpness_probability,
                sharpness.times(100).toString().substring(0,4)
            )
        if (sharpness < 0.5) {
            face_sharpness_tv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_check,0,0,0)
            return
        }
        face_sharpness_tv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_off,0,0,0)
    }

    private fun setFaceUnderexpose(underexpose: Float) {
        face_underexpose_tv.text =
            getString(
                R.string.face_underexpose_probability,
                underexpose.times(100).toString().substring(0,4)
            )
        if (underexpose < 0.5) {
            face_underexpose_tv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_check,0,0,0)
            return
        }
        face_underexpose_tv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_off,0,0,0)
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
        }
    }
}