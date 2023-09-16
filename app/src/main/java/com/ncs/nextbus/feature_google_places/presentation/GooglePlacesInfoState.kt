package com.ncs.nextbus.feature_google_places.presentation

import com.ncs.nextbus.feature_google_places.domain.model.GooglePlacesInfo

data class GooglePlacesInfoState(val direction: GooglePlacesInfo? = null, val isLoading: Boolean = false)