package ru.skillbranch.mappoints_vesion2.data

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.skillbranch.mappoints_vesion2.data.network.NetworkService
import java.lang.Exception

class MapRepository {

    val api = NetworkService.getApi()
    val TAG = "Error"

    fun getAddressByLatLng(latLng: LatLng, result : (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val networkStreet =
                    api.getStreetByLatLng("${latLng.latitude},${latLng.longitude}").await()
                val formattedAddress = networkStreet.results.first().formatted_address
                result(formattedAddress)
            } catch (e : Exception) {
                Log.d(TAG, "getAddressByLatLng: ${e.message} \n and ${e.stackTrace}")
                result("${latLng.latitude},${latLng.longitude}")
            }
        }
    }

    fun getAddressByName(name : String, result: (LatLng) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val networkStreet = api.getSteetByName(name).await()
                val latitude = networkStreet.results.first().geometry.location.lat
                val longitude = networkStreet.results.first().geometry.location.lng
                val resultValue = LatLng(latitude, longitude)
                result(resultValue)
            } catch (e : Exception) {
                Log.d(TAG, "getAddressByLatLng: ${e.message} \n and ${e.stackTrace}")
                result(LatLng(0.0,0.0))
            }
        }
    }

    fun getPathByCoordinate(origin : String, destination : String, result: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val routeResponse = api.getRouteResponse(origin, destination).await()
                val path = routeResponse.routes.first().overview_polyline.points
                result(path)
            } catch (e : Exception) {
                Log.d(TAG, "getPathByCoordinate: ${e.message}")
                result("Path not found")
            }
        }
    }

}