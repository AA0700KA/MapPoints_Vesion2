package ru.skillbranch.mappoints_vesion2.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.model.Place
import com.google.android.material.snackbar.Snackbar
import com.google.maps.android.PolyUtil
import kotlinx.android.synthetic.main.map_layout.*
import ru.skillbranch.mappoints_vesion2.App
import ru.skillbranch.mappoints_vesion2.R
import ru.skillbranch.mappoints_vesion2.extensions.isNetworkAviable
import ru.skillbranch.mappoints_vesion2.ui.MainActivity
import ru.skillbranch.mappoints_vesion2.ui.points.PointsView
import ru.skillbranch.mappoints_vesion2.view_models.MapState
import ru.skillbranch.mappoints_vesion2.view_models.MapViewModel
import ru.skillbranch.mappoints_vesion2.view_models.ViewModelFactory


class MapFragment : Fragment(), OnMapReadyCallback, LocationListener {

    private lateinit var googleMap: GoogleMap
    private lateinit var viewModel : MapViewModel
    private lateinit var locationManager: LocationManager
    private lateinit var pointsView : PointsView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager

        requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, ::initLocationManager)
        requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION, ::initLocationManager)
    }


    @SuppressLint("MissingPermission")
    private fun initLocationManager() : Unit {
        if (requireContext().isNetworkAviable()) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000 * 10, 50f, this)
            Log.d("Location", "onResume: location network")
        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 10, 50f, this)
            Log.d("Location", "onResume: location gps")
        } else {
            Toast.makeText(requireActivity(), "ON Network or GPS to get your location",Toast.LENGTH_SHORT).show();
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this, ViewModelFactory(App.application())).get(MapViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.map_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let {
            val activity = it as MainActivity
            pointsView = activity
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?

        Log.d("Callback", "onViewCreated: $mapFragment")
        mapFragment?.getMapAsync(this)

        viewModel.markersState.observe(viewLifecycleOwner, Observer {
            Log.d("State", "onViewCreated: $it")
            if (::googleMap.isInitialized) {
                renderMarkers(it)
                renderLocation(it)
                renderPath(it)
            }
        })

        raunded_button.setOnClickListener {
            viewModel.clearMarkers()
        }


    }

    override fun onResume() {
        super.onResume()
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        initLocationManager()
    }

    private fun renderMarkers(mapState: MapState) {
        val points = mapState.markers

        if (points.size == 2) {
            raunded_button.visibility = View.VISIBLE
            app_panel.visibility = View.VISIBLE
            app_panel.initCoordinates(points.first().coordinate, points.last().coordinate, mapState.yourLocation?.latitude!!)
        } else {
            raunded_button.visibility = View.GONE
            app_panel.visibility = View.GONE
        }

        if (mapState.removed) {
            googleMap.clear()
        }

        for (point in points) {
           val marker = googleMap.addMarker(
                MarkerOptions()
                    .position(point.coordinate)
                    .title(point.address)
            )

            Log.d("MarkersClick", "renderMarkers: $marker")
        }

        if (points.size == 2 && mapState.path.isNullOrEmpty()) {
            viewModel.drawPath(points.first(), points.last())
        }

    }

    private fun renderLocation(mapState: MapState) {
        var currentLocation : LatLng? = null

        with(mapState) {


            if (markers.size == 0) {
                yourLocation?.let {
                    currentLocation = LatLng(it.latitude, it.longitude)
                }
            } else {
                val lastElem = markers.last()
                currentLocation = LatLng(lastElem.coordinate.latitude, lastElem.coordinate.longitude)
            }

        }

        currentLocation?.let {
            val cameraPosition = CameraPosition.Builder()
                .target(it)
                .zoom(10f)
                .build()
            val update = CameraUpdateFactory.newCameraPosition(cameraPosition)
            googleMap.animateCamera(update)
        }


    }

    private fun renderPath(mapState: MapState) {
        if (mapState.markers.size == 2 && !mapState.path.isNullOrEmpty()) {
            val points = PolyUtil.decode(mapState.path)
            val lineOptions: PolylineOptions =
                PolylineOptions().width(8f).color(R.color.light_green)
            val latLngBuilder: LatLngBounds.Builder = LatLngBounds.Builder()

            for (point in points) {
                lineOptions.add(point)
                latLngBuilder.include(point)
            }

//            if (line != null) {
//                line.remove()
//            }

            val line = googleMap.addPolyline(lineOptions)
            val size = resources.displayMetrics.widthPixels
            val latLngBounds: LatLngBounds = latLngBuilder.build()
            val track: CameraUpdate =
                CameraUpdateFactory.newLatLngBounds(latLngBounds, size, size, 25)
            googleMap.moveCamera(track)
        }
    }

    fun updatePoint(index : Int, place : Place) {
         viewModel.updatePoint(index, place)
    }

    override fun onMapReady(googleMap : GoogleMap) {
        Log.d("Callback", "onMapReady: ")
        this.googleMap = googleMap

        this.googleMap.setOnMapClickListener {
             viewModel.addMarker(it)
        }

        this.googleMap.setOnInfoWindowClickListener {

            val markers = viewModel.currentState.markers
            val index = markers.indices.filter { index -> markers[index].address.equals(it.title) }.first()
            Log.d("MarkerClick", "Marker click ${it.title} and index $index")
            pointsView.markersPoints(index)
        }


    }

    override fun onLocationChanged(location: Location) {
        Log.d("Location", "onLocationChanged: $location")
        viewModel.updateLocation(location)
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        Toast.makeText(requireActivity(), "Network: $status", Toast.LENGTH_SHORT).show()
    }


    override fun onProviderEnabled(provider: String) {
       if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        val location = locationManager.getLastKnownLocation(provider)
       Log.d("Location", "onProviderEnabled: $location")
    }

    override fun onProviderDisabled(provider: String) {

    }

    private fun requestPermission(permission : String, doSamthing : () -> Unit) {
        val requestPermissionLauncher =
                registerForActivityResult(ActivityResultContracts.RequestPermission()
                ) { isGranted: Boolean ->
                    if (isGranted) {
                        Log.d("Permission", "Granted")
                    } else {
                        Log.d("Permission", "Danied!")
                    }
                }
        when {
            ContextCompat.checkSelfPermission(
                    requireContext(),
                    permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                doSamthing()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), permission) -> {
            // In an educational UI, explain to the user why your app requires this
            // permission for a specific feature to behave as expected. In this UI,
            // include a "cancel" or "no thanks" button that allows the user to
            // continue using your app without granting the permission.
            Snackbar.make(layout, "Required permission", Snackbar.LENGTH_INDEFINITE).setAction("Ok") {
                requestPermissionLauncher.launch(permission)
            }.show()
        }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(permission)
            }
        }
    }

}