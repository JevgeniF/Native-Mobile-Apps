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
import com.fenko.gpssportsmap.backend.Volley
import com.fenko.gpssportsmap.database.ActivityRepo
import com.fenko.gpssportsmap.objects.LocationPoint
import com.fenko.gpssportsmap.tools.C
import com.fenko.gpssportsmap.tools.Helpers
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

class MapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener, SensorEventListener {
    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
    }
    /*
     MapActivity.class = main UI activity.
     Shows all current gps activity actions on map
     Passes some data to service, as all calculations and location updates made there.
     */

    //broadcast
    private val broadcastReceiver = InnerBroadcastReceiver()
    private val broadcastReceiverIntentFilter: IntentFilter = IntentFilter()

    private var locationServiceActive = false   //indicator for locationServiceActivity
    private var compassOpen = false             //indicator for Compass Switch

    private lateinit var mMap: GoogleMap
    private var lastKnownLocation: Location? = null

    //params used for mapObject drawing on map
    private var startMarked = false
    var currentLocation: Location? = null
    private var listOfLocations: ArrayList<LocationPoint?> = arrayListOf()
    private var marker: Marker? = null
    var markerWP: Marker? = null
    var markerWPLocation: Location? = null

    //params for compass
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var magnetometer: Sensor
    private var lastAccelerometer = FloatArray(3)
    private var lastMagnetometer = FloatArray(3)
    private var lastAccelerometerSet = false
    private var lastMagnetometerSet = false
    private var bearing = 0f //current compass direction, used for camera rotations

    private var mapObjects = MapObjects() // class with map objects drawing functions and some map settings
    private var options = Options() // class for options pop-up
    var volley = Volley() // used in main activity for user log in

    //gps activities local repository
    private lateinit var activityRepo: ActivityRepo

    //pace range for polyline coloring
    var goodPace: Int = 4
    var badPace: Int = 7

    override fun onSaveInstanceState(outState: Bundle) {
        /*
        function used for save of some data required for proper activity work onResume
        */
        super.onSaveInstanceState(outState)
        outState.putBoolean("locationServiceActive", locationServiceActive)
        outState.putBoolean("compassOpen", compassOpen)
        outState.putBoolean("startMarked", startMarked)
        //outState.putParcelable("lastKnownLocation", lastKnownLocation)
        outState.putParcelable("currentLocation", currentLocation)
        if (markerWP != null) {
            outState.putParcelable("markerWPLocation", markerWPLocation)
        }
        outState.putParcelable("ui", mapObjects)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_layout)
        findViewById<View>(R.id.compass).visibility = View.GONE //compass layout hidden by default

        activityRepo = ActivityRepo(this).open()

        createNotificationChannel()

        if (savedInstanceState != null) {
            //restoration of saved params
            locationServiceActive = savedInstanceState.getBoolean("locationServiceActive")
            compassOpen = savedInstanceState.getBoolean("compassOpen")
            if (compassOpen) {
                findViewById<View>(R.id.compass).visibility = View.VISIBLE
            }
            startMarked = savedInstanceState.getBoolean("startMarked")
            if (savedInstanceState.getParcelable<Location>("markerWPLocation") != null) {
                markerWPLocation = savedInstanceState.getParcelable("markerWPLocation")!!
            }
            //lastKnownLocation = savedInstanceState.getParcelable("lastKnownLocation")
            currentLocation = savedInstanceState.getParcelable("currentLocation")
            mapObjects = savedInstanceState.getParcelable("ui")!!
            findViewById<Button>(R.id.buttonStart).text = mapObjects.startButtonText
            findViewById<Button>(R.id.buttonMapView).text = mapObjects.mapViewButtonText
        }

        //location permission request
        if (!checkPermissions()) {
            requestPermissions()
        }

        /*
        Log-in/registration pop-up. Checks if there is any user in database.
        If no registered user in Database (empty token), the form will pop-up on any recreation of Activity.
        If there is registered user, activity will be started without any pop-up. Idea is that the phone is
        for personal use and only one user will be logged in the app from this phone.
        Another variant(maybe will add in future) - is to make logout option, then the activity in
        database must have user ID, in order
        to hide other users items.
         */
        val user = activityRepo.getUser()
        if (user.token == "") {
            volley.login(this)
        }

        //intentFilter for this activity
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


        //sensors used for compass
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        /*
         As the location is not passing from service, while activity on Pause/Stop, activity
         updates list of points required for track from the database on Start/Resume
         */
        if (locationServiceActive) {
            val activity = activityRepo.getLast()
            listOfLocations = activity.listOfLocations
            badPace = activity.badPace
            goodPace = activity.goodPace
        }
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
        Log.d(TAG, "onMapReady")
        //function updates map on screen, when map is ready
        mMap = googleMap

        /*
        was planned to give to user option to switch from usual map type to hybrid, as it could
        help in orienteering. However, sometimes the map tiles can be updated in wrong time, due to
        big speed or bad connection. The option "mapType" left in code only.
         */
        mMap.mapType = mapObjects.mapType

        //usual google api zoom controls
        mMap.uiSettings.isZoomControlsEnabled = mapObjects.isZoomControlsEnabled
        mMap.uiSettings.isZoomGesturesEnabled = mapObjects.isZoomGesturesEnabled

        //listeners for Waypoint or marker placement on click
        mMap.setOnMapClickListener(this)
        mMap.setOnMapLongClickListener(this)

        var position = LatLng(59.5152974, 24.8241854) //default camera position

        if (!locationServiceActive) {
            if(lastKnownLocation != null) {
                position = LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude)
                println(position)
            }
        }

        //restoration of Waypoint marker, if was placed before onPause/onStop
        if (markerWPLocation != null) {
            markerWP = mapObjects.addWPfmLocation(markerWPLocation, mMap)
        }

        if (currentLocation != null) {
            position = LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
        }

        //restoration of Startpoint
        if (listOfLocations.isNotEmpty()) {
            mapObjects.addStart(listOfLocations[0], mMap)
        }

        //restoration of Polylines
        for (i in 1 until listOfLocations.size) {
            mapObjects.uiUpdate(LatLng(listOfLocations[i - 1]!!.latitude,
                listOfLocations[i - 1]!!.longitude),
                LatLng(listOfLocations[i]!!.latitude, listOfLocations[i]!!.longitude),
                listOfLocations[i]!!.speed, goodPace, badPace, mMap, this)

            //restoration of Checkpoints if were placed before
            if (listOfLocations[i]!!.typeId == "00000000-0000-0000-0000-000000000003") {
                mapObjects.addCPMarker(listOfLocations[i], mMap)
            }
        }

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, mapObjects.defaultZoom))
    }

    private fun createNotificationChannel() {
        //function creates notification channel for app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                    C.NOTIFICATION_CHANNEL, "Default Notification",
                    NotificationManager.IMPORTANCE_LOW
            )

            channel.description = "notification for GPS Sports Map with action buttons"

            val notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onSensorChanged(event: SensorEvent?) {
        /*
        function for compass. Checks changes in sensors data.
        Changes transferred to compass layout.
        textCompDirLeft: shows Compass Direction 90 degrees left from current direction
        textCompDirRight: shows Compass Direction 90 degrees right from current direction
        textCompCurrDir: shows Current Compass Direction and degree (bearing)
        @SuppressLint used as there are no text, but Android Studio counts this as small violation
         */
        if (event!!.sensor == accelerometer) {
            lowPass(event.values, lastAccelerometer)
            lastAccelerometerSet = true
        } else if (event.sensor == magnetometer) {
            lowPass(event.values, lastMagnetometer)
            lastMagnetometerSet = true
        }

        if (lastAccelerometerSet && lastMagnetometerSet) {
            val r = FloatArray(9)
            if (SensorManager.getRotationMatrix(r, null, lastAccelerometer, lastMagnetometer)) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(r, orientation)

                bearing = (Math.toDegrees(orientation[0].toDouble()) + 360).toFloat() % 360
                findViewById<TextView>(R.id.textCompDirLeft).text = "${Helpers().compassDirection(bearing - 90)}   \u140A"
                findViewById<TextView>(R.id.textCompCurrDir).text = "%d\u00b0 ${Helpers().compassDirection(bearing)}".format(bearing.toInt())
                findViewById<TextView>(R.id.textCompDirRight).text = "\u1405   ${Helpers().compassDirection(bearing + 90)}"
            }
        }
    }

    private fun lowPass(input: FloatArray, output: FloatArray) {
        //filter to smooth sensors values
        val alpha = 0.05f
        for (i in input.indices) {
            output[i] = output[i] + alpha * (input[i] - output[i])
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun onLocationChanged(location: Location) {
        //function updates UI(Map) in real time. Basically same way as on Start/Resume, but for realtime activity.
        if (listOfLocations.size >= 1) {
            //marking of start for the first time
            if (!startMarked) {
                mapObjects.addStart(listOfLocations[0]!!, mMap)
                startMarked = true
            }
            //marking of last track part by polyline
            mapObjects.uiUpdate(LatLng(listOfLocations.last()!!.latitude,
                listOfLocations.last()!!.longitude),
                LatLng(location.latitude, location.longitude),
                location.speed, goodPace, badPace, mMap, this)
        }
        listOfLocations.add(LocationPoint(location))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(location.latitude, location.longitude)))

        //change of camera rotation with north always up or in accordance with user rotation(compass bearing)
        if (mapObjects.northUp) {
            mapObjects.updateCameraBearing(mMap, 0f)
        } else {
            mapObjects.updateCameraBearing(mMap, bearing)
        }
    }

    fun onButtonOptionsClick(view: View) {
        //function starts options pop-up from Options class
        options.askOptions(this, this)
    }

    fun onButtonCompassClick(view: View) {
        //function shows compass layer on top of map layer
        if (compassOpen) {
            findViewById<View>(R.id.compass).visibility = View.GONE
        } else {
            findViewById<View>(R.id.compass).visibility = View.VISIBLE
        }
        compassOpen = !compassOpen
    }

    override fun onMapClick(p0: LatLng?) {
        /*
        function gives user possibility to place marker on map by clicking on it.
        if marker placed, second click will change marker position to a new place (where user clicked)
         */
        if (locationServiceActive) {
            if (marker != null) {
                marker!!.remove()
            }
            marker = mapObjects.addMarker(this, p0, locationServiceActive, mMap, findViewById(R.id.layoutMain))
        }
    }

    override fun onMapLongClick(p0: LatLng?) {
        /*
        functions adds possibility to convert placed marker into Waypoint by long click anywhere on map.
        it is possible to add waypoint and marker at the same time, but on second long click Waypoint
        will be moved to the position of the marker. Marker will be removed.

        As marker is required during activity only, it is not added to database or server, so in order
        to save and reproduce marker, I made a location for it.
         */
        if (locationServiceActive && marker != null) {
            if (markerWP != null) {
                markerWP!!.remove()
            }
            //as all calculations made by service, the function sends empty intent to service when Waypoint removed
            val intent = (Intent(C.MAIN_ACTION_WP))
            sendBroadcast(intent)
            markerWP = mapObjects.addWPfmMarker(marker, locationServiceActive, mMap, findViewById(R.id.layoutMain))

            //location made for recreation on Start/Resume
            markerWPLocation = Location(LocationManager.GPS_PROVIDER)
            markerWPLocation!!.latitude = markerWP!!.position.latitude
            markerWPLocation!!.longitude = markerWP!!.position.longitude

            //as all calculations made by service, the function sends intent with location to service when Waypoint added
            intent.putExtra(C.MAIN_ACTION_WP, markerWPLocation)
            sendBroadcast(intent)

            marker!!.remove()
        }
    }

    fun buttonStartOnClick(view: View) {
        /*
        function starts/stops background location service and record of gps activity
         */
        Log.d(TAG, "buttonStartStopOnClick. locationServiceActive: $locationServiceActive")

        if (locationServiceActive) {

            //alert message to confirm if user wants to finish activity
            val builder = AlertDialog.Builder(this)
            builder.setTitle(resources.getString(R.string.question))
                    .setMessage(resources.getString(R.string.questionBody))
                    .setPositiveButton(resources.getText(R.string.yes)) { _: DialogInterface, _: Int ->

                        // stopping the service in case of confirmation
                        stopService(Intent(this, LocationService::class.java))
                        TSnackbar.make(view, resources.getString(R.string.activityCompleted), TSnackbar.LENGTH_LONG).show()
                        //to make it possible to start activity again without app restart, all important ui data must be reset

                        mMap.clear()
                        listOfLocations = arrayListOf()
                        currentLocation = null
                        markerWPLocation = null
                        markerWP = null
                        marker = null
                        startMarked = false
                        (view as Button).text = resources.getString(R.string.start)
                        mapObjects.startButtonText = resources.getString(R.string.start)

                        //completion of MapActivity opens LastActivityData (results of activity)
                        val viewGPSActivity = Intent(this, LastActivityData::class.java)
                        startActivity(viewGPSActivity)
                    }
                    .setNegativeButton(resources.getString(R.string.no)) { _: DialogInterface, _: Int ->
                        TSnackbar.make(view, resources.getString(R.string.activityResumed), TSnackbar.LENGTH_LONG).show()
                    }
            val dialog = builder.create()
            dialog.show()

        } else {
            //starting service and gps activity. As all calculations and activity creation made in service,
            //function sends some user options to service
            val startLocationUpdate = Intent(this, LocationService::class.java)
            startLocationUpdate.putExtra("activityType", options.activity) //type of activity chosen by user
            startLocationUpdate.putExtra("updateRate", options.locationUpdateRate) //update rate chosen by user
            startLocationUpdate.putIntegerArrayListExtra("targetPace", arrayListOf(badPace, goodPace)) //pace range chosen by user
            if (Build.VERSION.SDK_INT >= 26) {

                // starting the FOREGROUND service
                // service has to display non-dismissible notification within 5 secs
                startForegroundService(Intent(startLocationUpdate))
            } else {
                startService(Intent(startLocationUpdate))
            }
            (view as Button).text = resources.getString(R.string.stop)
            mapObjects.startButtonText = resources.getString(R.string.stop)
        }
        locationServiceActive = !locationServiceActive
    }

    fun buttonMapViewOnClick(view: View) {
        //function to change map rotation by button "Map View" (always N or in accordance to compass)
        if (!mapObjects.northUp) {
            mapObjects.northUp = true
            (view as Button).text = resources.getString(R.string.northUp)
            mapObjects.mapViewButtonText = resources.getString(R.string.northUp)
            mapObjects.updateCameraBearing(mMap, 0f)
        } else {
            mapObjects.northUp = false
            (view as Button).text = resources.getString(R.string.headUp)
            mapObjects.mapViewButtonText = resources.getString(R.string.headUp)
            mapObjects.updateCameraBearing(mMap, bearing)
        }
    }

    fun buttonResetWPointOnClick(view: View) {
        //function for reset (clear) waypoint by button "Reset WPoint"
        if (locationServiceActive) {
            if (markerWP != null) {
                markerWP!!.remove()
                markerWPLocation = null
            }
            //function sends "zero" intent to service as order to reset calculations
            val intent = (Intent(C.MAIN_ACTION_WP))
            intent.putExtra(C.MAIN_ACTION_WP, 0)
            sendBroadcast(intent)
        }
    }

    fun buttonAddCPointOnClick(view: View) {
        //function to mark current location as CP. function sends order to service, if order received,
        //service sends location point back and ui marks CP on map.
        if (locationServiceActive) {
            Log.d(TAG, "buttonCPOnClick")
            sendBroadcast(Intent(C.NOTIFICATION_ACTION_CP_SET))
        }
    }

    private fun checkPermissions(): Boolean {
        //function to check app permissions
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private fun requestPermissions() {
        //function to request permissions
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
            TSnackbar.make(
                    findViewById(R.id.layoutMain),
                    resources.getString(R.string.providePermissions),
                    TSnackbar.LENGTH_INDEFINITE
            )
                    .setAction(resources.getString(R.string.ok)) {
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
        //function for check permission request result
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == C.REQUEST_PERMISSIONS_REQUEST_CODE) {
            when {
                grantResults.count() <= 0 -> {

                    // If user interaction was interrupted, the permission request is cancelled.
                    Log.i(TAG, "User interaction was cancelled.")
                    Toast.makeText(this, resources.getString(R.string.interactionCancelled), Toast.LENGTH_SHORT).show()
                }
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                    // Permission was granted.
                    Log.i(TAG, "Permission was granted")
                    Toast.makeText(this, resources.getString(R.string.permissionGranted), Toast.LENGTH_SHORT).show()
                }
                else -> {
                    // Permission denied.
                    TSnackbar.make(
                            findViewById(R.id.layoutMain),
                            resources.getString(R.string.applicationMalfunction),
                            TSnackbar.LENGTH_INDEFINITE
                    )
                            .setAction(resources.getString(R.string.appSettings)) {
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
        activityRepo.close()
    }

    override fun onRestart() {
        Log.d(TAG, "onRestart")
        super.onRestart()
    }

    private inner class InnerBroadcastReceiver : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context?, intent: Intent?) {
            /*
            function receives location updates and notification actions from service, as well as calculated data and
            passes it to activity and layout

            on receive of notification actions, responds on WP reset action after WP resetting.
             */
            Log.d(TAG, intent!!.action!!)
            when (intent.action) {
                C.LOCATION_UPDATE_ACTION -> {
                    try {
                        currentLocation = intent.getParcelableExtra(C.LOCATION_UPDATE_ACTION)

                        findViewById<TextView>(R.id.textFromStartSpeed).text = "%.2f min/km".format(intent.getFloatExtra(C.LOCATION_UPDATE_ACTION_TOTAL_PACE, 0f))
                        findViewById<TextView>(R.id.textFromStartDistance).text = "%.0f m".format(intent.getFloatExtra(C.LOCATION_UPDATE_ACTION_TOTAL_DISTANCE, 0f))
                        findViewById<TextView>(R.id.textFromStartTime).text = "%s".format(intent.getStringExtra(C.LOCATION_UPDATE_ACTION_TOTAL_TIME))

                        findViewById<TextView>(R.id.textFromCPointDirect).text = "%.0f m".format(intent.getFloatExtra(C.LOCATION_UPDATE_ACTION_CP_DIRECT, 0f))
                        findViewById<TextView>(R.id.textFromCPTime).text = "%s".format(intent.getStringExtra(C.LOCATION_UPDATE_ACTION_CP_TIME))
                        findViewById<TextView>(R.id.textCPointDist).text = "%.0f m".format(intent.getFloatExtra(C.LOCATION_UPDATE_ACTION_CP_PASSED, 0f))

                        findViewById<TextView>(R.id.textTillWPointDirect).text = "%.0f m".format(intent.getFloatExtra(C.LOCATION_UPDATE_ACTION_WP_DIRECT, 0f))
                        findViewById<TextView>(R.id.textTillWPointTime).text = "%s".format(intent.getStringExtra(C.LOCATION_UPDATE_ACTION_WP_TIME))
                        findViewById<TextView>(R.id.textTillWPointDist).text = "%.0f m".format(intent.getFloatExtra(C.LOCATION_UPDATE_ACTION_WP_PASSED, 0f))

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
                    mapObjects.addCPMarker(cpLocation, mMap)

                }
            }
        }
    }
}
