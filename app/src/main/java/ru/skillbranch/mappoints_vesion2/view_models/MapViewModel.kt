package ru.skillbranch.mappoints_vesion2.view_models

import android.content.Context
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.skillbranch.mappoints_vesion2.data.MapRepository
import ru.skillbranch.mappoints_vesion2.data.pojo.MapPoint


class MapViewModel(private val repository: MapRepository) : ViewModel() {

    init {
        Log.d("ViewModel", "Repository is $repository")
    }

    val markersState : MutableLiveData<MapState> = MutableLiveData<MapState>().apply {
        value = MapState()
    }

    val currentState : MapState
          get() = markersState.value!!

    fun updateLocation(location: Location) {
        markersState.value = currentState.copy(yourLocation = location, removed = false)
    }

    fun updatePoint(index : Int, place: Place) {
        val markers = currentState.markers
        repository.getAddressByName(place.address!!) {
            CoroutineScope(Dispatchers.Main).launch {
                val mapPoint = MapPoint(it, place.address!!)
                markers.set(index, mapPoint)
                markersState.value = currentState.copy(markers = markers, removed = true, path = null)
            }
        }
    }

    fun addMarker(coordinate : LatLng) {

        val list = currentState.markers

        if (list.size == 2) return

        repository.getAddressByLatLng(coordinate) {
            CoroutineScope(Dispatchers.Main).launch {
                list.add(MapPoint(coordinate, it))
                markersState.value = currentState.copy(markers = list, removed = false)
            }
        }


    }

    fun drawPath(from : MapPoint, to : MapPoint) {
         repository.getPathByCoordinate(
             "${from.coordinate.latitude},${from.coordinate.longitude}",
             "${to.coordinate.latitude},${to.coordinate.longitude}"
         ) {
             CoroutineScope(Dispatchers.Main).launch {
                 Log.d("Path", "drawPath: $it")
                 markersState.value = currentState.copy(path = it, removed = false)
             }
         }
    }

    fun clearMarkers() {
        markersState.value = currentState.copy(markers = mutableListOf(), removed = true, path = null)
    }

}

data class MapState(val yourLocation : Location? = null,
                    val markers : MutableList<MapPoint> = mutableListOf(),
                    val removed : Boolean = false,
                    val path : String? = null)