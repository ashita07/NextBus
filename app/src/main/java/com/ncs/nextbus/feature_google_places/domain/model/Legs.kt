package com.ncs.nextbus.feature_google_places.domain.model

import com.ncs.nextbus.feature_google_places.domain.model.Distance
import com.ncs.nextbus.feature_google_places.domain.model.Duration

data class Legs(
    val distance: Distance,
    val duration: Duration
)
