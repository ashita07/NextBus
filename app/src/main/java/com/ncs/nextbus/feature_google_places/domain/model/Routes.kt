package com.ncs.nextbus.feature_google_places.domain.model

import com.ncs.nextbus.feature_google_places.domain.model.Legs
import com.ncs.nextbus.feature_google_places.domain.model.OverviewPolyline

data class Routes(
    val summary: String,
    val overview_polyline: OverviewPolyline,
    val legs: List<Legs>
)
