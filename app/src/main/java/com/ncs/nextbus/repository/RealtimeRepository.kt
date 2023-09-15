package com.ncs.nextbus.repository

import android.net.Uri
import com.ncs.tradezy.RealtimeDB
import com.ncs.tradezy.ResultState
import kotlinx.coroutines.flow.Flow


interface RealtimeRepository {


    fun getLocationData():Flow<ResultState<List<RealtimeDB>>>




}