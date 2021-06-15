package ru.skillbranch.mappoints_vesion2.ui

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import ru.skillbranch.mappoints_vesion2.R
import ru.skillbranch.mappoints_vesion2.ui.map.MapFragment
import ru.skillbranch.mappoints_vesion2.ui.points.PointsView
import java.util.*

class MainActivity : AppCompatActivity(), PointsView {

    private val AUTOCOMPLETE_REQUEST_CODE = 1
    private val MAP_FRAGMENT_TAG = "Map"
    private var lastPointIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Places.initialize(applicationContext, "AIzaSyAYoN4LJJXYmebjCrcHmkqjcYuCZw-gl74")
        val placesClient = Places.createClient(this)


        supportFragmentManager.beginTransaction().add(
            R.id.container,
            MapFragment(),
            MAP_FRAGMENT_TAG
        ).commit()
    }

    override fun markersPoints(index : Int) {

        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN,
            Arrays.asList(Place.Field.ADDRESS, Place.Field.NAME, Place.Field.ID))
           // .setTypeFilter(TypeFilter.ESTABLISHMENT)
            .setTypeFilter(TypeFilter.GEOCODE)
            .build(this)

        lastPointIndex = index

        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {

            if (resultCode == Activity.RESULT_OK) {
                val place = Autocomplete.getPlaceFromIntent(data!!)
                Log.d(
                    "RequestPlace",
                    "Place: ${place.latLng} address: ${place.address} Index: $lastPointIndex "
                )
                val mapFragment = supportFragmentManager.findFragmentByTag(MAP_FRAGMENT_TAG) as MapFragment
                mapFragment.updatePoint(lastPointIndex, place)
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                val status = Autocomplete.getStatusFromIntent(data!!)
                Log.d("RequestPlace", "Status: ${status.statusMessage!!}")
            }

        } else {
            Log.d("RequestPlace", "Place not found :( ")
        }
    }


}