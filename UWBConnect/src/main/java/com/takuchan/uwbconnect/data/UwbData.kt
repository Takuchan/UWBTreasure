package com.takuchan.uwbconnect.data

data class AnchorData(
    val id: Int,
    var nLos: Int? = null,
    var distance: Int? = null,
    var azimuth: Float? = null,
    var elevation: Float? = null
) {
    fun isCompleteForTrilateration(): Boolean = distance != null
}

data class TrilaterationResult(
    val anchor0: AnchorData,
    val anchor1: AnchorData,
    val anchor2: AnchorData,
    val timestamp: Long = System.currentTimeMillis()
)