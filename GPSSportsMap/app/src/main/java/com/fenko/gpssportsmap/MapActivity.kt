package com.fenko.gpssportsmap

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.SENSOR_DELAY_GAME
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
import com.fenko.gpssportsmap.tools.C
import com.fenko.gpssportsmap.tools.Calculator
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar


//TODO UI,
// toTEST: LINE COLORS,
// camera on saved activities broken
// ability to edit,
// locations filter,
// options,
// backend

class MapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener, SensorEventListener {
    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
    }

    //private var volley = Volley()

    private val broadcastReceiver = InnerBroadcastReceiver()
    private val broadcastReceiverIntentFilter: IntentFilter = IntentFilter()

    private var locationServiceActive = false
    private var compassOpen = false
    private lateinit var mMap: GoogleMap
    private var lastKnownLocation: Location? = null
    var currentLocation: Location? = null
    private var listOfLocations: ArrayList<Location> = arrayListOf()
    var listOfCP: ArrayList<Location> = arrayListOf()
    private var marker: Marker? = null
    var markerWP: Marker? = null
    var markerWPLocation: Location? = null

    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var magnetometer: Sensor
    private var lastAcellerometer = FloatArray(3)
    private var lastMagnetometer = FloatArray(3)
    private var lastAcellerometerSet = false
    private var lastMagnetometerSet = false
    private var bearing = 0f

    private var ui = UI()

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean("locationServiceActive", locationServiceActive)
        outState.putBoolean("compassOpen", compassOpen)
        outState.putParcelable("lastKnownLocation", lastKnownLocation)
        outState.putParcelable("currentLocation", currentLocation)
        outState.putParcelableArrayList("listOfLocations", listOfLocations)
        outState.putParcelableArrayList("listOfCP", listOfCP)
        if (markerWP != null) {
            outState.putParcelable("markerWPLocation", markerWPLocation)
        }
        outState.putParcelable("ui", ui)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        setContentView(R.layout.main_layout)
        findViewById<View>(R.id.compass).visibility = View.GONE

        createNotificationChannel()

        if(savedInstanceState != null){
            locationServiceActive = savedInstanceState.getBoolean("locationServiceActive")
            compassOpen = savedInstanceState.getBoolean("compassOpen")
            if (compassOpen) {
                findViewById<View>(R.id.compass).visibility = View.VISIBLE
            }
            if (savedInstanceState.getParcelable<Location>("markerWPLocation") != null) {
                markerWPLocation = savedInstanceState.getParcelable("markerWPLocation")!!
            }
            lastKnownLocation = savedInstanceState.getParcelable("lastKnownLocation")
            currentLocation = savedInstanceState.getParcelable("currentLocation")
            listOfLocations = savedInstanceState.getParcelableArrayList("listOfLocations")!!
            listOfCP = savedInstanceState.getParcelableArrayList("listOfCP")!!
            ui = savedInstanceState.getParcelable("ui")!!
            findViewById<Button>(R.id.buttonStart).text = ui.startButtonText
            findViewById<Button>(R.id.buttonMapView).text = ui.mapViewButtonText
        }

        /*
        login dialog popup and login to backend
        if (volley.volleyUser== null)
        volley.login(this)
         */
        if (!checkPermissions()) {
            requestPermissions()
        }

        broadcastReceiverIntentFilter.addAction(C.LOCATION_UPDATE_ACTION)
        broadcastReceiverIntentFilter.addAction(C.NOTIFICATION_ACTION_WP_RESET)
        broadcastReceiverIntentFilter.addAction(C.NOTIFICATION_ACTION_CP_SET)

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()
        val provider = locationManager.getBestProvider(criteria, true)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions()
        }
        lastKnownLocation = locationManager.getLastKnownLocation(provider!!)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

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
        mMap.mapType = ui.mapType
        mMap.uiSettings.isZoomControlsEnabled = ui.isZoomControlsEnabled
        mMap.uiSettings.isZoomGesturesEnabled = ui.isZoomGesturesEnabled

        mMap.setOnMapClickListener(this)
        mMap.setOnMapLongClickListener(this)

        var position = LatLng(59.5152974, 24.8241854)

        if (markerWPLocation != null) {
            markerWP = mMap.addMarker(MarkerOptions().icon(BitmapDescriptorFactory
                    .fromResource(R.drawable.baseline_location_on_black_24dp))
                    .title("WP").position(LatLng(markerWPLocation!!.latitude, markerWPLocation!!.longitude)))
        }


        if (lastKnownLocation != null) {
            position = LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude)
        }
        if(listOfLocations.isNotEmpty()) {
                    mMap.addMarker(MarkerOptions().icon(BitmapDescriptorFactory
                            .fromResource(R.drawable.baseline_outlined_flag_black_24dp)).title("Start")
                            .position(LatLng(listOfLocations[0].latitude, listOfLocations[0].longitude)))
                }
        for(i in 1 until listOfLocations.size) {
            ui.uiUpdate(LatLng(listOfLocations[i - 1].latitude, listOfLocations[i - 1].longitude), LatLng(listOfLocations[i].latitude, listOfLocations[i].longitude), listOfLocations[i].speed, ui.fasterPace, ui.slowerPace, mMap)
        }
        for(i in 0 until listOfCP.size) {
            ui.addCPMarker(listOfCP[i], mMap)
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(position))
        mMap.moveCamera(CameraUpdateFactory.zoomTo(ui.defaultZoom))
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

    @SuppressLint("SetTextI18n")
    override fun onSensorChanged(event: SensorEvent?) {
        if (event!!.sensor == accelerometer) {
            lowPass(event.values, lastAcellerometer)
            lastAcellerometerSet = true
        } else if (event.sensor == magnetometer) {
            lowPass(event.values, lastMagnetometer)
            lastMagnetometerSet = true
        }

        if (lastAcellerometerSet && lastMagnetometerSet) {
            val  r = FloatArray(9)
            if (SensorManager.getRotationMatrix(r, null, lastAcellerometer, lastMagnetometer)) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(r, orientation)

                bearing = (Math.toDegrees(orientation[0].toDouble()) + 360).toFloat() % 360
                findViewById<TextView>(R.id.textCompDirLeft).text = "${Calculator().compassDirection(bearing - 45)}   \u140A"
                findViewById<TextView>(R.id.textCompCurrDir).text = "%d\u00b0 ${Calculator().compassDirection(bearing)}".format(bearing.toInt())
                findViewById<TextView>(R.id.textCompDirRight).text = "\u1405   ${Calculator().compassDirection(bearing + 45)}"
            }
        }
    }

    private fun lowPass(input: FloatArray, output: FloatArray) {
        val alpha = 0.05f
        for (i in input.indices) {
            output[i] = output[i] + alpha * (input[i] - output[i])
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun onLocationChanged(location: Location) {
        listOfLocations.add(location)
        if (listOfLocations.size == 1) {
            mMap.addMarker(MarkerOptions().icon(BitmapDescriptorFactory
                    .fromResource(R.drawable.baseline_outlined_flag_black_24dp)).title("Start")
                    .position(LatLng(location.latitude, location.longitude)))
        }
        if (listOfLocations.size > 1) {
            ui.uiUpdate(LatLng(listOfLocations[listOfLocations.size - 2].latitude, listOfLocations[listOfLocations.size - 2].longitude), LatLng(location.latitude, location.longitude), location.speed, ui.slowerPace, ui.fasterPace, mMap)
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(location.latitude, location.longitude)))

        if (ui.northUp) {
            ui.updateCameraBearing(mMap, 0f)
        } else {
            ui.updateCameraBearing(mMap, bearing)
        }
    }

    fun onButtonCompassClick(view: View) {
        if(compassOpen) {
            findViewById<View>(R.id.compass).visibility = View.GONE
        } else {
            findViewById<View>(R.id.compass).visibility = View.VISIBLE
        }
        compassOpen = !compassOpen
    }

    override fun onMapClick(p0: LatLng?) {
        if (locationServiceActive) {
            if (marker != null) {
                marker!!.remove()
            }
            marker = ui.addMarker(p0, locationServiceActive, mMap, findViewById(R.id.layoutMain))
        }
    }

    override fun onMapLongClick(p0: LatLng?) {
        if (locationServiceActive) {
            if (markerWP != null) {
                markerWP!!.remove()
            }

            val intent = (Intent(C.MAIN_ACTION_WP))
            sendBroadcast(intent)

            markerWP = ui.addWP(marker, locationServiceActive, mMap, findViewById(R.id.layoutMain))

            markerWPLocation = Location(LocationManager.GPS_PROVIDER)
            markerWPLocation!!.latitude = markerWP!!.position.latitude
            markerWPLocation!!.longitude = markerWP!!.position.longitude

            intent.putExtra(C.MAIN_ACTION_WP, markerWPLocation)
            sendBroadcast(intent)

            marker!!.remove()
        }
    }

    fun buttonStartOnClick(view: View) {
        Log.d(TAG, "buttonStartStopOnClick. locationServiceActive: $locationServiceActive")
        // try to start/stop the background service

        if (locationServiceActive) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(resources.getString(R.string.question))
                    .setMessage(resources.getString(R.string.questionBody))
                    .setPositiveButton(resources.getText(R.string.yes)) { _: DialogInterface, _: Int ->
                        // stopping the service
                        stopService(Intent(this, LocationService::class.java))
                        TSnackbar.make(view, "Activity Completed", TSnackbar.LENGTH_LONG).show()
                        (view as Button).text = resources.getString(R.string.start)
                        ui.startButtonText = resources.getString(R.string.start)
                        val viewGPSActivity = Intent(this, DataActivity::class.java)
                        startActivity(viewGPSActivity)
                        finish()
                    }
                    .setNegativeButton(resources.getString(R.string.no)) { _: DialogInterface, _: Int ->
                        TSnackbar.make(view, "Activity Resumed", TSnackbar.LENGTH_LONG).show()
                    }
            val dialog = builder.create()
            dialog.show()

        } else {
            if (Build.VERSION.SDK_INT >= 26) {
                // starting the FOREGROUND service
                // service has to display non-dismissible notification within 5 secs
                startForegroundService(Intent(this, LocationService::class.java))
            } else {
                startService(Intent(this, LocationService::class.java))
            }
            (view as Button).text = resources.getString(R.string.stop)
            ui.startButtonText = resources.getString(R.string.stop)
        }
        locationServiceActive = !locationServiceActive
    }

    fun buttonMapViewOnClick(view: View) {

        ui.mapView(view, this, mMap, currentLocation)
    }

    fun buttonResetWPointOnClick(view: View) {
        if (locationServiceActive) {
            if (markerWP != null) {
                markerWP!!.remove()
                markerWPLocation = null
            }
            val intent = (Intent(C.MAIN_ACTION_WP))
            intent.putExtra(C.MAIN_ACTION_WP, 0)
            sendBroadcast(intent)
        }
    }

    fun buttonAddCPointOnClick(view: View) {
        if (locationServiceActive) {
            Log.d(TAG, "buttonCPOnClick")
            sendBroadcast(Intent(C.NOTIFICATION_ACTION_CP_SET))
            //ui.addCPMarker(currentLocation, mMap)
            //listOfCP.add(currentLocation!!)
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
                    findViewById(R.id.layoutActivityData),
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
                            findViewById(R.id.layoutActivityData),
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

        sensorManager.registerListener(this, accelerometer, SENSOR_DELAY_GAME)
        sensorManager.registerListener(this, magnetometer, SENSOR_DELAY_GAME)

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(broadcastReceiver, broadcastReceiverIntentFilter)
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()

        sensorManager.unregisterListener(this, accelerometer)
        sensorManager.unregisterListener(this, magnetometer)
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

    private inner class InnerBroadcastReceiver : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, intent!!.action!!)
            when (intent.action) {
                C.LOCATION_UPDATE_ACTION -> {
                    //TODO UPGRADE TO WORK WITH DATABASES
                    try {
                        currentLocation = intent.getParcelableExtra(C.LOCATION_UPDATE_ACTION)

                        findViewById<TextView>(R.id.textFromStartSpeed).text = "%.2f min/km".format(intent.getFloatExtra(C.LOCATION_UPDATE_ACTION_TOTAL_PACE, 0f))
                        findViewById<TextView>(R.id.textFromStartDistance).text = "%.2f m".format(intent.getFloatExtra(C.LOCATION_UPDATE_ACTION_TOTAL_DISTANCE, 0f))
                        findViewById<TextView>(R.id.textFromStartTime).text = "%s".format(intent.getStringExtra(C.LOCATION_UPDATE_ACTION_TOTAL_TIME))

                        findViewById<TextView>(R.id.textFromCPointSpeed).text = "%.2f min/km".format(intent.getFloatExtra(C.LOCATION_UPDATE_ACTION_CP_PACE, 0f))
                        println("%.2f min/km".format(intent.getFloatExtra(C.LOCATION_UPDATE_ACTION_CP_PACE, 0f)))
                        findViewById<TextView>(R.id.textFromCPointDistance).text = "%.2f m".format(intent.getFloatExtra(C.LOCATION_UPDATE_ACTION_CP_PASSED, 0f))
                        findViewById<TextView>(R.id.textCPointDirect).text = "%.2f m".format(intent.getFloatExtra(C.LOCATION_UPDATE_ACTION_CP_DIRECT, 0f))

                        findViewById<TextView>(R.id.textFromWPointSpeed).text = "%.2f min/km".format(intent.getFloatExtra(C.LOCATION_UPDATE_ACTION_WP_PACE, 0f))
                        findViewById<TextView>(R.id.textFromWPointDistance).text = "%.2f m".format(intent.getFloatExtra(C.LOCATION_UPDATE_ACTION_WP_PASSED, 0f))
                        findViewById<TextView>(R.id.textWPointDirect).text = "%.2f m".format(intent.getFloatExtra(C.LOCATION_UPDATE_ACTION_WP_DIRECT, 0f))

                        onLocationChanged(currentLocation!!)

                    } catch (e: NullPointerException) {
                    }
                }
                C.NOTIFICATION_ACTION_WP_RESET -> {
                    if (markerWP != null) {
                        markerWP!!.remove()
                        markerWPLocation = null
                        val response = (Intent(C.MAIN_ACTION_WP))
                        response.putExtra(C.MAIN_ACTION_WP, 0)
                        sendBroadcast(response)
                    }
                }
                C.NOTIFICATION_ACTION_CP_SET -> {
                    val cpLocation = intent.getParcelableExtra<Location>(C.NOTIFICATION_ACTION_CP_SET)
                    ui.addCPMarker(cpLocation, mMap)
                    listOfCP.add(cpLocation!!)

                }
            }
        }
    }
}
