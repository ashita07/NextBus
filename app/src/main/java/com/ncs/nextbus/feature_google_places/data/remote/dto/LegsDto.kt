package com.ncs.nextbus.feature_google_places.data.remote.dto

import com.ncs.nextbus.feature_google_places.domain.model.Legs

data class LegsDto(
    val distance: DistanceDto,
    val duration: DurationDto
){
    fun toLegs(): Legs {
        return Legs(
            distance = distance.toDistance(),
            duration = duration.toDuration()
        )
    }
}
