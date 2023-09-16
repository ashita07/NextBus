package com.ncs.nextbus.feature_google_places.domain.repository

import com.ncs.nextbus.util.Resource
import com.ncs.nextbus.feature_google_places.domain.model.GooglePlacesInfo
import kotlinx.coroutines.flow.Flow

interface GooglePlacesInfoRepository {
    fun getDirection(origin: String, destination: String, key: String): Flow<Resource<GooglePlacesInfo>>
}