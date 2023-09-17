package com.ncs.nextbus.repository

import android.location.Location
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.ncs.nextbus.RealtimeDB
import com.ncs.tradezy.ResultState
import kotlinx.coroutines.channels.awaitClose


import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class RealtimeDBRepository @Inject constructor(
    private val db:DatabaseReference
): RealtimeRepository {
    private var storageReference=Firebase.storage

    private val currentUserID=FirebaseAuth.getInstance().currentUser?.uid
    override fun getLocationData(): Flow<ResultState<List<RealtimeDB>>> =callbackFlow{
        trySend(ResultState.Loading)

        val valueEvent=object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                val items=snapshot.children.map {
                    RealtimeDB(
                        it.getValue(RealtimeDB.locationData::class.java),
                        key = it.key
                    )
                }
                trySend(ResultState.Success(items))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(ResultState.Failure(error.toException()))
            }

        }
        db.child("bus_location").addValueEventListener(valueEvent)
        awaitClose{
            db.child("bus_location").removeEventListener(valueEvent)
            close()
        }
    }


}