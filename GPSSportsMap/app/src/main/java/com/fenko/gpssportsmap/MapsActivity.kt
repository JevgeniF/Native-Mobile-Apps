package com.fenko.gpssportsmap

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.androidadvance.topsnackbar.TSnackbar
import com.google.android.gms.common.api.internal.ConnectionCallbacks
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*

//TODO SAVING ACTIVITY
//TODO RESTORING ACTIVITY
//TODO NOTIFICATIONS
//TODO DATABASE???
//TODO CORRECT DATA
//TODO FUSED LOCATION SERVICE 2ND PRIORITY
//TODO COLORED SEGMENTS 2ND PRIORITY


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener, ConnectionCallbacks, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener {

    private var myPerMissionRequestLocation = 99

    private lateinit var mMap: GoogleMap
    private lateinit var locationManager: LocationManager
    private var provider: String? = ""

    private var volley = Volley()
    private var settings = Settings()
    private var activity = Activity()

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("ActivityStarted",activity.started)
        outState.putBoolean("ActivityPaused",activity.paused)
        outState.putBoolean("ActivityCompleted",activity.completed)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(savedInstanceState != null) {
            activity.started = savedInstanceState.getBoolean("ActivityStarted")
            activity.paused = savedInstanceState.getBoolean("ActivityPaused")
            activity.completed = savedInstanceState.getBoolean("ActivityCompleted")
        }

        supportActionBar?.hide()
        setContentView(R.layout.main_layout)

        //login dialog popup and login to backend
        if (volley.volleyUser== null)
        volley.login(this)

        //location permission request
        checkLocationPermissions()

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()
        provider = locationManager.getBestProvider(criteria, true)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            activity.currentLocation = locationManager.getLastKnownLocation(provider!!)!!
            activity.currentLatLng = LatLng(activity.currentLocation!!.latitude, activity.currentLocation!!.longitude)
        } else {
            return
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = settings.mapType
        mMap.uiSettings.isZoomControlsEnabled = settings.isZoomControlsEnabled
        mMap.uiSettings.isZoomGesturesEnabled = settings.isZoomGesturesEnabled
        mMap.uiSettings.isCompassEnabled = settings.isCompassEnabled
        mMap.setOnMapClickListener(this)
        mMap.setOnMapLongClickListener(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission
                        .ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = settings.isMyLocationEnabled
        }
        mMap.moveCamera(CameraUpdateFactory.zoomTo(settings.defaultZoom))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(activity.currentLatLng))
        updateCameraBearing(mMap, activity.currentLocation!!.bearing)
    }

    override fun onConnected(p0: Bundle?) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission
                        .ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(provider!!, 400, 1f,
                    this, Looper.getMainLooper())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onLocationChanged(location: Location) {
        activity.currentLocation = location
        activity.currentLatLng = LatLng(location.latitude, location.longitude)
        if (activity.passedRoute != null) {
            activity.passedRoute!!.remove()
        }
        if (activity.path != null) {
            activity.path!!.remove()
            settings.pathOptions = PolylineOptions().width(5F).color(Color.YELLOW)
        }
        if(activity.wayPoint != null) {
            activity.path = mMap.addPolyline(settings.pathOptions!!
                    .add(activity.currentLatLng, activity.wayPointLatLng))
        }
        if(activity.started && !activity.paused) {
            activity.passedRoute = mMap.addPolyline(settings.passedRouteOptions!!
                    .add(activity.currentLatLng))
            volley.postLU(this, activity)
            volley.getSession(this, activity.sessionId!!, activity)

            val totalDistance = findViewById<TextView>(R.id.textFromStartDistance)
            totalDistance.text = activity.totalDistance.toString()
            val duration = findViewById<TextView>(R.id.textFromStartTime)
            duration.text = activity.duration.toString()
            val averageSpeed = findViewById<TextView>(R.id.textFromStartSpeed)
            averageSpeed.text = activity.averageSpeed.toString()
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(activity.currentLatLng))
        if (settings.northUp) {
            updateCameraBearing(mMap, 0f)
        } else {
            updateCameraBearing(mMap, location.bearing)
        }
    }

    override fun onMapClick(p0: LatLng?) {
        if (activity.marker != null) {
            activity.marker!!.remove()
        }
        activity.marker = mMap.addMarker(MarkerOptions().icon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_CYAN)).title("Add as WP").position(p0!!))
        activity.markerLatLng = LatLng(activity.marker!!.position.latitude, activity.marker!!.position.longitude)
        TSnackbar.make(findViewById(R.id.main), "Long press on map to create WayPoint on Marker position. Click to change position of Marker.", TSnackbar.LENGTH_LONG).show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMapLongClick(p0: LatLng?) {
        if (activity.wayPoint != null) {
            activity.wayPoint!!.remove()
            activity.path!!.remove()
            settings.pathOptions = PolylineOptions().width(5F).color(Color.YELLOW)
        }
        if (activity.marker != null) {
            activity.wayPoint = mMap.addMarker(MarkerOptions().icon(BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)).title("WP").position(activity.markerLatLng as LatLng))
            activity.wayPointLatLng = activity.markerLatLng
            activity.marker!!.remove()
            TSnackbar.make(findViewById(R.id.main), "WayPoint created.", TSnackbar.LENGTH_LONG).show()
            activity.path = mMap.addPolyline(settings.pathOptions!!.add(activity.currentLatLng, activity.wayPointLatLng))
            if (activity.started) {
                if (activity.wayPointId != null) {
                    volley.deletePoint(activity.wayPointId!!, this)
                    volley.postWP(this, activity)
                } else {
                    volley.postWP(this, activity)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (provider != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(provider!!, 400, 1f, this, Looper.getMainLooper())
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (provider != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.removeUpdates(this)
            }
        }
    }

    fun updateCameraBearing(googleMap: GoogleMap?, bearing: Float) {
        if (googleMap == null) return
        val camPos = CameraPosition
                .builder(
                        googleMap.cameraPosition
                )
                .bearing(bearing)
                .build()
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos))
    }

    private fun checkLocationPermissions(): Boolean {
        return if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), myPerMissionRequestLocation)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), myPerMissionRequestLocation)
            }
            false
        } else {
            true
        }
    }

    override fun onConnectionSuspended(p0: Int) {
    }

    //BUTTON COMMANDS
    fun buttonMapViewOnClick(view: View) {
        UiButtons().mapViewButton(settings, activity, view, mMap)
    }

    fun buttonResetWPointOnClick(view: View) {
        UiButtons().resetWPointButton(settings, activity, this, volley)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun buttonAddCPointOnClick(view: View) {
        UiButtons().addCPointButton(this,activity, mMap, volley)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    fun buttonStartOnClick(view: View){
        UiButtons().startButton(activity, view, this, mMap, volley)
        //todo new activity clean start
    }
}
