package com.ncs.nextbus

import com.ncs.tradezy.RealtimeDB


data class LocationState(

val item:List<RealtimeDB> = emptyList(),
val error:String = "",
val isLoading:Boolean=false

)
