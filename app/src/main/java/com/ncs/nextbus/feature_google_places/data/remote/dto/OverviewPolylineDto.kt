package com.ncs.nextbus.feature_google_places.data.remote.dto

import com.ncs.nextbus.feature_google_places.domain.model.OverviewPolyline

data class OverviewPolylineDto(
    val points: String
){
    fun toOverviewPolyline(): OverviewPolyline {
        return OverviewPolyline(
            points = points
        )
    }
}
