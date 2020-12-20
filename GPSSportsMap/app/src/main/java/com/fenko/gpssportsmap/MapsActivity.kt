package com.fenko.gpssportsmap

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.androidadvance.topsnackbar.TSnackbar
import com.google.android.gms.common.api.internal.ConnectionCallbacks
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import org.json.JSONObject


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener, ConnectionCallbacks, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener {

    private var myPerMissionRequestLocation = 99

    private lateinit var mMap: GoogleMap
    private var northUp: Boolean = false
    private var started: Boolean = false
    private var paused: Boolean = false
    private lateinit var locationManager: LocationManager
    private var provider: String? = ""
    private var lastLocation: Location? = null
    private var wayPoint: Marker? = null
    private var checkPoint: Marker? = null
    private var marker: Marker? = null
    private var passedRouteOptions = PolylineOptions().width(7F).color(Color.RED)
    private var passedRoute: Polyline? = null
    private var pathOptions = PolylineOptions().width(5F).color(Color.YELLOW)
    private var path: Polyline? = null

    var volley = Volley()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.main_layout)

        volley.login(this)

        checkLocationPermissions()

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()
        provider = locationManager.getBestProvider(criteria, true)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            lastLocation = locationManager.getLastKnownLocation(provider!!)!!
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
        mMap.mapType = 2
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isZoomGesturesEnabled = true
        mMap.uiSettings.isCompassEnabled = false
        mMap.setOnMapClickListener(this)
        mMap.setOnMapLongClickListener(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        }
        val currentLatLng = LatLng(lastLocation!!.latitude, lastLocation!!.longitude)
        mMap.moveCamera(CameraUpdateFactory.zoomTo(17f))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng))
        updateCameraBearing(mMap, lastLocation!!.bearing)
    }

    override fun onConnected(p0: Bundle?) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(provider!!, 400, 1f, this, Looper.getMainLooper())
        }
    }

    override fun onLocationChanged(location: Location) {
        lastLocation = location
        if (passedRoute != null) {
            passedRoute!!.remove()
        }
        if (path != null) {
            path!!.remove()
            pathOptions = PolylineOptions().width(5F).color(Color.YELLOW)
        }
        val currentLatLng = LatLng(lastLocation!!.latitude, lastLocation!!.longitude)
        if(wayPoint != null) {
            val pathLatLng = LatLng(wayPoint!!.position.latitude, wayPoint!!.position.longitude)
            path = mMap.addPolyline(pathOptions.add(currentLatLng, pathLatLng))
        }
        if(started) {
            passedRoute = mMap.addPolyline(passedRouteOptions.add(currentLatLng))
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng))
        if (northUp) {
            updateCameraBearing(mMap, 0f)
        } else {
            updateCameraBearing(mMap, location.bearing)
        }
    }

    override fun onMapClick(p0: LatLng?) {
        if (marker != null) {
            marker!!.remove()
        }
        marker = mMap.addMarker(MarkerOptions().position(p0!!).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)).title("Add as WP"))
        if(started) {
            TSnackbar.make(findViewById(R.id.main), "Long press on map to create WayPoint on Marker position. Click to change position of Marker.", TSnackbar.LENGTH_LONG).show()
        }
    }

    override fun onMapLongClick(p0: LatLng?) {
        if(started) {
            if (wayPoint!= null){
                wayPoint!!.remove()
                path!!.remove()
                pathOptions = PolylineOptions().width(5F).color(Color.YELLOW)
            }
            if (marker != null) {
                wayPoint = mMap.addMarker(MarkerOptions().position(marker!!.position).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)).title("WP"))
                marker!!.remove()
                TSnackbar.make(findViewById(R.id.main), "WayPoint created.", TSnackbar.LENGTH_LONG).show()
                val pathLatLng = LatLng(wayPoint!!.position.latitude, wayPoint!!.position.longitude)
                val currentLatLng = LatLng(lastLocation!!.latitude, lastLocation!!.longitude)
                path = mMap.addPolyline(pathOptions.add(currentLatLng, pathLatLng))
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

    private fun updateCameraBearing(googleMap: GoogleMap?, bearing: Float) {
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
    @SuppressLint("SetTextI18n")
    fun buttonMapViewOnClick(view: View) {
        if (!northUp) {
            northUp = true
            (view as Button).text = "North Up"
            updateCameraBearing(mMap, 0f)
        } else {
            northUp = false
            (view as Button).text = "Head Up"
            updateCameraBearing(mMap, lastLocation!!.bearing)
        }
    }

    fun buttonResetWPointOnClick(view: View) {
        if(wayPoint != null) {
            wayPoint!!.remove()
            path!!.remove()
            pathOptions = PolylineOptions().width(5F).color(Color.YELLOW)
        }
    }

    fun buttonAddCPointOnClick(view: View) {
        val cpLatLng = LatLng(lastLocation!!.latitude, lastLocation!!.longitude)
        checkPoint = mMap.addMarker(MarkerOptions().position(cpLatLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title("CP"))
    }

    @SuppressLint("SetTextI18n")
    fun buttonStartOnClick(view: View){
        if (!started && !paused) {
            started = true
            (view as Button).text = "Pause"
            TSnackbar.make(findViewById(R.id.main), "Activity started", TSnackbar.LENGTH_LONG).show()
            return
        } else if (started && !paused) {
            paused = true
            started = false
            (view as Button).text = "Finish"
            TSnackbar.make(findViewById(R.id.main), "Activity Paused", TSnackbar.LENGTH_LONG).show()
            return
        } else if (paused) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Finish Activity")
            builder.setMessage("Do you want to finish or to continue activity?")
            builder.setPositiveButton("Continue") { _: DialogInterface, _: Int ->
                started = true
                paused = false
                (view as Button).text = "Pause"
            }
            builder.setNegativeButton("Finish") { _: DialogInterface, _: Int ->
                started = false
                paused = false
                (view as Button).text = "Start"
            }
            val dialog = builder.create()
            dialog.show()
        }
    }
}
