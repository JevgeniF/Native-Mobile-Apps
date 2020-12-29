package com.fenko.gpssportsmap

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.androidadvance.topsnackbar.TSnackbar
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import java.lang.NullPointerException
import java.text.SimpleDateFormat
import java.util.*


//TODO DATABASE???
//TODO SAVING ACTIVITY
//TODO RESTORING ACTIVITY
//TODO COLORED SEGMENTS 2ND PRIORITY

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener {
    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
    }
    /*
    private var myPerMissionRequestLocation = 99
    private lateinit var locationManager: LocationManager
    private var provider: String? = ""
    private var volley = Volley()
    private var settings = Settings()
    private var activity = Activity()
     */

    private val broadcastReceiver = InnerBroadcastReceiver()
    private val broadcastReceiverIntentFilter: IntentFilter = IntentFilter()

    private var locationServiceActive = false

    private lateinit var mMap: GoogleMap

    private var settings = AppSettings()

    private var activity: Activity? = null
    var currentPoint: LocationPoint? = null
    private var startPoint: LocationPoint? = null
    private var passedRoute: Polyline? = null
    private var marker: Marker? = null
    private var markerWP: Marker? = null

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("locationServiceActive", locationServiceActive)
        outState.putString("startButtonText", findViewById<Button>(R.id.buttonStart).text.toString())
        outState.putParcelable("settings", settings)
        if(activity != null) {
            outState.putParcelable("activity", activity)
            outState.putParcelableArrayList("locationPoints", activity!!.listOfLocationPoints)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        setContentView(R.layout.main_layout)

        createNotificationChannel()

        /*
        login dialog popup and login to backend
        if (volley.volleyUser== null)
        volley.login(this)
         */
        if (!checkPermissions()) {
            requestPermissions()
        }

        broadcastReceiverIntentFilter.addAction(C.LOCATION_UPDATE_ACTION)

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()
        val provider = locationManager.getBestProvider(criteria, true)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions()
        }
        currentPoint = LocationPoint(locationManager.getLastKnownLocation(provider!!)!!.latitude,
                locationManager.getLastKnownLocation(provider)!!.longitude)

        if (savedInstanceState != null) {
            locationServiceActive = savedInstanceState.getBoolean("locationServiceActive")
            findViewById<Button>(R.id.buttonStart).text = savedInstanceState.getString("startButtonText")
            settings = savedInstanceState.getParcelable("settings")!!
            try {
                activity = savedInstanceState.getParcelable("activity")!!
                activity!!.listOfLocationPoints = savedInstanceState.getParcelableArrayList("locationPoints")!!
            } catch (e: NullPointerException){}
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
        mMap.moveCamera(CameraUpdateFactory.zoomTo(settings.defaultZoom))
        var position = LatLng(59.5152974, 24.8241854)
        if (currentPoint != null) {
            position = LatLng(currentPoint!!.latitude, currentPoint!!.longitude)
        }
        if ( activity!= null) {
            if (activity!!.wayPoint != null) {
                markerWP = mMap.addMarker(MarkerOptions().icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.baseline_location_on_black_24dp))
                        .title("WP").position(LatLng(activity!!.wayPoint!!.latitude, activity!!.wayPoint!!.longitude)))
            }
            if (activity!!.listOfLocationPoints.isNotEmpty()) {
                for (i in 0 until activity!!.listOfLocationPoints.size) {
                    if (activity!!.listOfLocationPoints[i].typeId == "00000000-0000-0000-0000-000000000003") {
                        mMap.addMarker(MarkerOptions().icon(BitmapDescriptorFactory
                                .fromResource(R.drawable.baseline_where_to_vote_black_24dp)).title("CP")
                                .position(LatLng(activity!!.listOfLocationPoints[i].latitude,
                                        activity!!.listOfLocationPoints[i].longitude)))
                    }
                }
            }
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLng(position))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                    C.NOTIFICATION_CHANNEL, "Default channel",
                    NotificationManager.IMPORTANCE_LOW
            )

            channel.description = "Default channel"

            val notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private inner class InnerBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, intent!!.action!!)
            when (intent.action) {
                C.LOCATION_UPDATE_ACTION -> {
                    if (intent.getParcelableExtra<Location>(C.LOCATION_UPDATE_ACTION) != null) {
                        val location = intent.getParcelableExtra<Location>(C.LOCATION_UPDATE_ACTION)
                        currentPoint = LocationPoint(location!!.latitude, location.longitude, location.altitude, location.bearing, location.accuracy, typeId = "00000000-0000-0000-0000-000000000001")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            currentPoint!!.vAccuracy = location.verticalAccuracyMeters
                        }
                        currentPoint!!.speed = intent.getFloatExtra(C.LOCATION_UPDATE_ACTION_TOTAL_PACE, 0f).toDouble()
                        findViewById<TextView>(R.id.textFromStartSpeed).text = "%.2f min/km".format(intent.getFloatExtra(C.LOCATION_UPDATE_ACTION_TOTAL_PACE, 0f))
                        findViewById<TextView>(R.id.textFromStartDistance).text = "%.2f m".format(intent.getFloatExtra(C.LOCATION_UPDATE_ACTION_TOTAL_DISTANCE, 0f))
                        findViewById<TextView>(R.id.textFromStartTime).text = "%s".format(intent.getStringExtra(C.LOCATION_UPDATE_ACTION_TOTAL_TIME))

                        onLocationChanged(currentPoint!!)
                    }
                }
            }
        }
    }

    fun onLocationChanged(locationPoint: LocationPoint) {
        if (activity != null) {
            activity!!.listOfLocationPoints.add(locationPoint)

            startPoint = activity!!.listOfLocationPoints[0]
            mMap.addMarker(MarkerOptions().icon(BitmapDescriptorFactory
                    .fromResource(R.drawable.baseline_outlined_flag_black_24dp))
                    .title("Start")
                    .position(LatLng(startPoint!!.latitude, startPoint!!.longitude)))
        }

        if (passedRoute != null) {
            passedRoute!!.remove()
        }

        passedRoute = mMap.addPolyline(settings.passedRouteOptions
                    .add(LatLng(locationPoint.latitude, locationPoint.longitude)))
            //volley.postLU(this, activity)
            //volley.getSession(this, activity.sessionId!!, activity)

            //val totalDistance = findViewById<TextView>(R.id.textFromStartDistance)
            //totalDistance.text = activity.totalDistance.toString()
            //val duration = findViewById<TextView>(R.id.textFromStartTime)
            //duration.text = activity.duration.toString()
            //val averageSpeed = findViewById<TextView>(R.id.textFromStartSpeed)
            //averageSpeed.text = activity.averageSpeed.toString()
            mMap.moveCamera(CameraUpdateFactory
                    .newLatLng(LatLng(locationPoint.latitude, locationPoint.longitude)))
            if (settings.northUp) {
                updateCameraBearing(mMap, 0f)
            } else {
                updateCameraBearing(mMap, locationPoint.bearing)
        }
    }

    override fun onMapClick(p0: LatLng?) {
        if (locationServiceActive) {
            if (marker != null) {
                marker!!.remove()
            }
            marker = mMap.addMarker(MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_add_location_black_24dp))
                    .title("Add as WP?").position(p0!!))
            TSnackbar.make(findViewById(R.id.main), "Long press on map to create WayPoint on Marker position. Click to change position of Marker.", TSnackbar.LENGTH_LONG).show()
        }
    }

    override fun onMapLongClick(p0: LatLng?) {
        if (locationServiceActive) {
            if (activity!!.wayPoint != null) {
                activity!!.wayPoint = null
                markerWP!!.remove()
            }
            if (marker != null) {
                markerWP = mMap.addMarker(MarkerOptions().icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.baseline_location_on_black_24dp))
                        .title("WP").
                        position(LatLng(marker!!.position.latitude, marker!!.position.longitude)))
                activity!!.wayPoint = LocationPoint(marker!!.position.latitude, marker!!.position.longitude)
                activity!!.wayPoint!!.typeId = "00000000-0000-0000-0000-000000000002"
                TSnackbar.make(findViewById(R.id.main), "WayPoint created.", TSnackbar.LENGTH_LONG).show()
                Log.d(TAG, "buttonWPOnClick")
                sendBroadcast(Intent(C.NOTIFICATION_ACTION_WP))
                //if (activity.started) {
                //    if (activity.wayPointId != null) {
                //        volley.deletePoint(activity.wayPointId!!, this)
                //        volley.postWP(this, activity)
                //    } else {
                //        volley.postWP(this, activity)
                //    }
            }
            marker!!.remove()
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun buttonStartOnClick(view: View) {
        Log.d(TAG, "buttonStartStopOnClick. locationServiceActive: $locationServiceActive")
        // try to start/stop the background service

        if (locationServiceActive) {
            // stopping the service
            stopService(Intent(this, LocationService::class.java))
            if (activity != null) {
                mMap.addMarker(MarkerOptions().icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.baseline_flag_black_24dp))
                        .title("Finish")
                        .position(LatLng(currentPoint!!.latitude, currentPoint!!.longitude)))
            }

            (view as Button).text = resources.getString(R.string.start)
        } else {
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            val time = Calendar.getInstance(TimeZone.getTimeZone("GMT")).time
            val name = formatter.format(time)
            activity = Activity(name)
            if (Build.VERSION.SDK_INT >= 26) {
                // starting the FOREGROUND service
                // service has to display non-dismissable notification within 5 secs
                startForegroundService(Intent(this, LocationService::class.java))
            } else {
                startService(Intent(this, LocationService::class.java))
            }
            (view as Button).text = resources.getString(R.string.stop)
        }
        locationServiceActive = !locationServiceActive
    }

    fun buttonMapViewOnClick(view: View) {
        if (!settings.northUp) {
            settings.northUp = true
            (view as Button).text = resources.getString(R.string.northUp)
            MapsActivity().updateCameraBearing(mMap, 0f)
        } else {
            settings.northUp = false
            (view as Button).text = resources.getString(R.string.headUp)
            MapsActivity().updateCameraBearing(mMap, currentPoint!!.bearing)
        }
    }

    fun buttonResetWPointOnClick(view: View) {
        activity!!.wayPoint = null
        markerWP!!.remove()
        //if (activity.started) {
        //    if (activity.wayPointId != null) {
        //        volley.deletePoint(activity.wayPointId!!, context)
        //    }
        //}
    }

    fun buttonAddCPointOnClick(view: View) {
        if (locationServiceActive) {
            Log.d(TAG, "buttonCPOnClick")
            sendBroadcast(Intent(C.NOTIFICATION_ACTION_CP))
            mMap.addMarker(MarkerOptions().icon(BitmapDescriptorFactory
                    .fromResource(R.drawable.baseline_where_to_vote_black_24dp)).title("CP")
                    .position(LatLng(activity!!.listOfLocationPoints.last().latitude, activity!!.listOfLocationPoints.last().longitude)))
            activity!!.listOfLocationPoints.last().typeId = "00000000-0000-0000-0000-000000000003"
            //volley.postCP(context, activity)
        }
    }

    private fun checkPermissions(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private fun requestPermissions() {
        val shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                )

        if (shouldProvideRationale) {
            Log.i(
                    TAG,
                    "Displaying permission rationale to provide additional context."
            )
            Snackbar.make(
                    findViewById(R.id.main),
                    "GPS required for this app functionality.",
                    Snackbar.LENGTH_INDEFINITE
            )
                    .setAction("OK") {
                        ActivityCompat.requestPermissions(
                                this,
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                C.REQUEST_PERMISSIONS_REQUEST_CODE
                        )
                    }
                    .show()
        } else {
            Log.i(TAG, "Requesting permission")
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    C.REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == C.REQUEST_PERMISSIONS_REQUEST_CODE) {
            when {
                grantResults.count() <= 0 -> { // If user interaction was interrupted, the permission request is cancelled and you
                    // receive empty arrays.
                    Log.i(TAG, "User interaction was cancelled.")
                    Toast.makeText(this, "User interaction was cancelled.", Toast.LENGTH_SHORT).show()
                }
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> {// Permission was granted.
                    Log.i(TAG, "Permission was granted")
                    Toast.makeText(this, "Permission was granted", Toast.LENGTH_SHORT).show()
                }
                else -> { // Permission denied.
                    Snackbar.make(
                            findViewById(R.id.main),
                            "You denied GPS! What can I do?",
                            Snackbar.LENGTH_INDEFINITE
                    )
                            .setAction("Settings") {
                                val intent = Intent()
                                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                val uri: Uri = Uri.fromParts(
                                        "package",
                                        BuildConfig.APPLICATION_ID, null
                                )
                                intent.data = uri
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                            }
                            .show()
                }
            }
        }

    }

    override fun onStart() {
        Log.d(TAG, "onStart")
        super.onStart()
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(broadcastReceiver, broadcastReceiverIntentFilter)
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()
    }

    override fun onStop() {
        Log.d(TAG, "onStop")
        super.onStop()

        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
    }

    override fun onRestart() {
        Log.d(TAG, "onRestart")
        super.onRestart()
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
}
