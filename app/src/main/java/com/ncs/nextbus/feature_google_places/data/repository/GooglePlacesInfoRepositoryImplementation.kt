package com.ncs.nextbus.feature_google_places.data.repository

import com.ncs.nextbus.util.Resource
import com.ncs.nextbus.feature_google_places.data.remote.GooglePlacesApi
import com.ncs.nextbus.feature_google_places.domain.model.GooglePlacesInfo
import com.ncs.nextbus.feature_google_places.domain.repository.GooglePlacesInfoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException

class GooglePlacesInfoRepositoryImplementation(private val api: GooglePlacesApi):
    GooglePlacesInfoRepository {
    override fun getDirection(
        origin: String,
        destination: String,
        key: String
    ): Flow<Resource<GooglePlacesInfo>> = flow{
        emit(Resource.Loading())
        try {
            val directionData = api.getDirection(origin = origin, destination = destination, key=key)
            emit(Resource.Success(data = directionData))
        }catch (e: HttpException){
            emit(Resource.Error(message = "Oops something is not right: $e"))
        }catch (e: IOException){
            emit(Resource.Error(message = "No Internet Connection: $e"))
        }
    }

}