package ai.cyberlabs.perse.model

import ai.cyberlabs.perselite.model.DetectThresholdsResponse
import ai.cyberlabs.perselite.model.FaceResponse
import ai.cyberlabs.perselite.model.MetricsResponse

data class Detection(
    val totalFaces: Int,
    val faces: List<FaceResponse>,
    val imageMetrics: MetricsResponse,
    val timeTaken: Float,
    val thresholds: DetectThresholdsResponse
)