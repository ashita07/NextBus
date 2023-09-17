package com.ncs.nextbus

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.compose.material.*
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.ncs.nextbus.feature_google_places.presentation.GooglePlacesInfoViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


private lateinit var locationClient: FusedLocationProviderClient

@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    private val locationPermissionCode = 1
    private lateinit var locationClient: FusedLocationProviderClient
    private var isLocationPermissionGranted by mutableStateOf(false)
    private lateinit var databaseReference: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databaseReference = Firebase.database.reference.child("bus_location")

        setContent {

            val googleViewmodel: GooglePlacesInfoViewModel = hiltViewModel()
            MainContent(googleViewmodel=googleViewmodel)
        }
    }

    @Composable
    private fun MainContent(googleViewmodel:GooglePlacesInfoViewModel) {
        if (!isLocationPermissionGranted) {
            requestLocationPermission()
        } else {
            MapContent(googleViewModel = googleViewmodel)
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            locationPermissionCode
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            isLocationPermissionGranted = true
        } else {
            // Handle permission denied case
        }
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    private fun MapContent(viewModel: MainActivityViewModel= hiltViewModel(),googleViewModel:GooglePlacesInfoViewModel) {
        locationClient = LocationServices.getFusedLocationProviderClient(this)
        val res=viewModel.res.value
        val cameraPositionState = rememberCameraPositionState()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        locationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val userLatLng = LatLng(location.latitude, location.longitude)
                val cameraPosition = CameraPosition.fromLatLngZoom(userLatLng, 17f)
                cameraPositionState.position = cameraPosition
            }
        }
        var isMapLoaded by remember { mutableStateOf(false) }
        val snackbarHostState = remember { SnackbarHostState() }
        LaunchedEffect(key1 = true){
            googleViewModel.evenFlow.collectLatest { event ->
                when(event){
                    is GooglePlacesInfoViewModel.UIEvent.ShowSnackBar ->{
                        snackbarHostState.showSnackbar(
                            message = event.message
                        )
                    }
                }
            }
        }
        Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) },
            content = {
                GoogleMapView(
                    modifier = Modifier.fillMaxSize(),
                    googlePlacesInfoViewModel = googleViewModel,
                    onMapLoaded = {
                        isMapLoaded = true
                    },
                    cameraPositionState = cameraPositionState
                )
                if(!isMapLoaded){
                    AnimatedVisibility(
                        modifier = Modifier
                            .fillMaxSize(),
                        visible = !isMapLoaded,
                        enter = EnterTransition.None,
                        exit = fadeOut()
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .background(Color.White)
                                .wrapContentSize()
                        )
                    }
                }
            })
//        NextBusTheme {
//            GoogleMapView(
//                modifier = Modifier.fillMaxSize(),
//                onMapLoaded = {
//                    isMapLoaded = true
//                },
//                googlePlacesInfoViewModel = glaces
//            )
//        }


//                GoogleMap(
//                    modifier = Modifier.fillMaxSize(),
//                    uiSettings = MapUiSettings(zoomControlsEnabled = false, mapToolbarEnabled = true),
//                    cameraPositionState = cameraPositionState,
//                    properties = MapProperties(isMyLocationEnabled = true)
//                ) {
//                    Marker(
//                        state=MarkerState(markerPosition),
//                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
//                    )
//
//                }

    }
}
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun GoogleMapView(modifier: Modifier,onMapLoaded: () -> Unit, googlePlacesInfoViewModel: GooglePlacesInfoViewModel,cameraPositionState: CameraPositionState,viewModel: MainActivityViewModel= hiltViewModel()) {
    var poi by remember {
        mutableStateOf("")
    }
    val res=viewModel.res.value
    if (res.item.isNotEmpty()) {
        val location = res.item[0].item
        var mapProperties by remember {
            mutableStateOf(MapProperties(mapType = MapType.NORMAL, isMyLocationEnabled = true, isBuildingEnabled = false))
        }
        var uiSettings by remember {
            mutableStateOf(
                MapUiSettings(compassEnabled = false, zoomControlsEnabled = false)
            )
        }
        val context= LocalContext.current
        val sheetState = rememberModalBottomSheetState()
        var showBottomSheet by remember { mutableStateOf(false) }

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)

        ) { contentPadding ->
            GoogleMap(
                modifier = modifier.padding(contentPadding),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = uiSettings,
                onMapLoaded = onMapLoaded,
                onPOIClick = {
                    Log.d("TAG", "POI clicked: ${it.name}")
                    poi = it.name
                }
            ) {

                LaunchedEffect(location?.latitude, location?.longitude) {
                    googlePlacesInfoViewModel.getDirection(
                        origin = "${location?.latitude}, ${location?.longitude}",
                        destination = "${location?.endinglat}, ${location?.endinglong}",
                        key = MapKey.KEY
                    )
                }
                val markerClick: (Marker) -> Boolean = {
                    false
                }
                MapMarker(
                    modifier = Modifier,
                    position = LatLng(location?.latitude!!, location.longitude!!),
                    title = "Bus",
                    context = context,
                    iconResourceId = R.drawable.bus,
                    onInfoWindowClick = {showBottomSheet=true}
                )

                MapMarker(
                    modifier = Modifier,
                    position = LatLng(location?.startinglat!!, location.startinglong!!),
                    title = "Starting Station",
                    context = context,
                    iconResourceId = R.drawable.start,
                    )
                MapMarker(
                    modifier = Modifier,
                    position = LatLng(location?.endinglat!!, location.endinglong!!),
                    title = "Destination Station",
                    context = context,
                    iconResourceId = R.drawable.finish,
                    )
                Polyline(points = googlePlacesInfoViewModel.polyLinesPoints.value, onClick = {
                    Log.d("TAG", "${it.points} was clicked")
                }, color = Color.Black)

            }
            if (showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        showBottomSheet = false
                    },
                    sheetState = sheetState
                ) {

                    Column(
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.4f)
                            .padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally){
                            Text(text = "Begin your journey \uD83C\uDFA2", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 25.sp)
                            Spacer(modifier = Modifier.height(30.dp))
                            Text(text = "Give a right place to your things \n or explore a wide range of products!", color = Color.Gray, fontWeight = FontWeight.Medium, fontSize = 15.sp, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(30.dp))

                            Spacer(modifier = Modifier.height(30.dp))
                            Text(text = "*by continuing you Accept our Terms and Privacy Policies", color = Color.LightGray, fontWeight = FontWeight.Medium, fontSize = 12.sp, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(30.dp))
                            Text(text = "With ❤️ by TradeZ", color = Color.LightGray, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, textAlign = TextAlign.Center)

                        }
                    }
                }
            }
        }
    }
    else{
       Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
           CircularProgressIndicator()
       }
    }
}

@SuppressLint("SuspiciousIndentation")
@Composable
fun MapMarker(
    modifier: Modifier,
    context: Context,
    position: LatLng,
    title: String,
    snippet: String?="",
    onInfoWindowClick: (Marker) -> Unit = {},
    @DrawableRes iconResourceId: Int
) {
    val icon = bitmapDescriptor(
        context, iconResourceId
    )

        Marker(
            position = position,
            title = title,
            snippet = snippet,
            icon = icon,
            onInfoWindowClick = onInfoWindowClick
        )

}
fun bitmapDescriptor(
    context: Context,
    vectorResId: Int
): BitmapDescriptor? {

    val drawable = ContextCompat.getDrawable(context, vectorResId) ?: return null
    drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
    val bm = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = android.graphics.Canvas(bm)
    drawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bm)
}