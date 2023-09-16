package com.ncs.nextbus


data class LocationState(

val item:List<RealtimeDB> = emptyList(),
val error:String = "",
val isLoading:Boolean=false

)
