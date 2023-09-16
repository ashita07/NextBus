package com.ncs.nextbus.repository

import com.ncs.nextbus.RealtimeDB
import com.ncs.tradezy.ResultState
import kotlinx.coroutines.flow.Flow


interface RealtimeRepository {


    fun getLocationData():Flow<ResultState<List<RealtimeDB>>>




}