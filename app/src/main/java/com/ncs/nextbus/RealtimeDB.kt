package com.ncs.tradezy


import android.location.Location
import java.io.Serializable

data class RealtimeDB(
    val item: locationData? = null,
    val key: String? = ""
) : Serializable {

    data class locationData(
        val accuracy: Double,
        val altitude: Int,
        val bearing: Double,
        val bearingAccuracyDegrees: Int,
        val complete: Boolean,
        val elapsedRealtimeNanos: Long,
        val elapsedRealtimeUncertaintyNanos: Int,
        val fromMockProvider: Boolean,
        val latitude: Double,
        val longitude: Double,
        val mock: Boolean,
        val provider: String,
        val speed: Double,
        val speedAccuracyMetersPerSecond: Int,
        val time: Long,
        val verticalAccuracyMeters: Double
    ) : Serializable

    constructor() : this(null, "")
}