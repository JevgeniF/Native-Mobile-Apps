@file:Suppress("PrivatePropertyName")

package com.fenko.gpssportsmap

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.fenko.gpssportsmap.backend.Volley
import com.fenko.gpssportsmap.database.ActivityRepo
import com.fenko.gpssportsmap.objects.GPSActivity
import com.fenko.gpssportsmap.objects.LocationPoint
import com.fenko.gpssportsmap.tools.C
import com.fenko.gpssportsmap.tools.Helpers
import com.fenko.gpssportsmap.tools.Kalman
import com.google.android.gms.location.*
import java.util.*
import java.util.concurrent.TimeUnit


class LocationService : Service() {
    /*
    Service class for locations update, starting and finishing of activity, providing of all calculations
    service uses gpsActivity object with it's location points for calculations as the same work with
    database is slower
     */
    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
    }


    // The desired intervals for location updates.
    private var UPDATE_INTERVAL_IN_MILLISECONDS: Long = 2000
    private val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2

    private val broadcastReceiver = InnerBroadcastReceiver()
    private val broadcastReceiverIntentFilter: IntentFilter = IntentFilter()

    private val mLocationRequest: LocationRequest = LocationRequest()
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var mLocationCallback: LocationCallback? = null

    // last received location
    private var currentLocation: Location? = null

    //overall activity data
    private var distanceTotal = 0f
    private var timeTotal = "00:00:00"
    private var averageSpeedTotal = 0f

    private var timeStart = 0L                      //required for time calculation

    //params for CP distance and time calculation
    private var locationCP: Location? = null
    private var distanceCPDirect = 0f
    private var distanceCPPassed = 0f
    private var timeFmCP = "00:00:00"

    private var timeAtCP = 0L

    //params for WP distance and time calculation
    private var locationWP: Location? = null
    private var distanceToWPDirect = 0f
    private var distanceToWPPassed = 0f
    private var estTimeToWP = "00:00:00"

    //gps activity
    var gpsActivity: GPSActivity? = null

    //repo
    private lateinit var activityRepo: ActivityRepo

    //backend
    var volley = Volley()

    // Kalman latlng filter
    private var kalman = Kalman(5f)

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()

        activityRepo = ActivityRepo(this).open()

        //intentFilter for this service
        broadcastReceiverIntentFilter.addAction(C.NOTIFICATION_ACTION_CP_SET)
        broadcastReceiverIntentFilter.addAction(C.NOTIFICATION_ACTION_WP_RESET)
        broadcastReceiverIntentFilter.addAction(C.MAIN_ACTION_CP)
        broadcastReceiverIntentFilter.addAction(C.MAIN_ACTION_WP)
        broadcastReceiverIntentFilter.addAction(C.LOCATION_UPDATE_ACTION)

        registerReceiver(broadcastReceiver, broadcastReceiverIntentFilter)

        //setting up fusedLocationClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mLocationCallback = object : LocationCallback() {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                onNewLocation(locationResult.lastLocation)
            }
        }

        createLocationRequest()
        requestLocationUpdates()

    }

    private fun requestLocationUpdates() {
        //function requests location updates
        Log.i(TAG, "Requesting location updates")

        try {
            mFusedLocationClient.requestLocationUpdates(
                    mLocationRequest,
                    mLocationCallback, Looper.myLooper()
            )
        } catch (unlikely: SecurityException) {
            Log.e(
                    TAG,
                    "Lost location permission. Could not request updates. $unlikely"
            )
        }
    }

    private fun onNewLocation(location: Location) {
        //function filters locations, creates location points from locations, adds location points to
        //gps activity object, repo, backend. calculates metrics.
        Log.i(TAG, "New location: $location")

        //implementation of Kalman filter in order to make gps tracking smoother
        kalman.process(location.latitude, location.longitude, location.accuracy, location.time)
        location.latitude = kalman.getLat()
        location.longitude = kalman.getLng()
        location.accuracy = kalman.getAccuracy()

        //creating locationPoint from location and setting speed to each location. Speed required for polylines.

        if (gpsActivity!!.listOfLocations.isEmpty()) {
            location.speed = 0f
        } else {
            val currentSpeed = Helpers().paceAtLocation(gpsActivity!!.listOfLocations.last()!!, location)
            location.speed = currentSpeed
        }
        val locationPoint = LocationPoint(location)
        //adding location point to gps activity / repo / backend
        gpsActivity!!.listOfLocations.add(locationPoint)
        activityRepo.addLocation(locationPoint)
        volley.postLU(this, gpsActivity!!, locationPoint)

        //calculating all metrics
        val timeNow = Calendar.getInstance().timeInMillis
        if(currentLocation != null) {
            // overall metrics
            distanceTotal += location.distanceTo(currentLocation)
            timeTotal = Helpers().totalTime(timeStart, timeNow)
            averageSpeedTotal = 50 / (3 * (distanceTotal / TimeUnit.MILLISECONDS.toSeconds(timeNow - timeStart)))
        }

        if(locationCP != null) {
            //cp metrics
            timeAtCP = locationCP!!.time
            distanceCPDirect = location.distanceTo(locationCP)
            distanceCPPassed += location.distanceTo(currentLocation)
            timeFmCP = Helpers().totalTime(timeAtCP, timeNow)
        }

        if(locationWP != null) {
            val currentPaceInSec = location.speed * 60
            distanceToWPDirect = location.distanceTo(locationWP)
            distanceToWPPassed += location.distanceTo(currentLocation)
            val estTimeInSec = distanceToWPDirect/1000 * currentPaceInSec
            estTimeToWP = Helpers().converterStoMS(estTimeInSec.toLong())
        }
        // save the location for calculations
        currentLocation = location

        showNotification()

        // broadcast data to UI
        val intent = Intent(C.LOCATION_UPDATE_ACTION)
        intent.putExtra(C.LOCATION_UPDATE_ACTION, currentLocation)
        intent.putExtra(C.LOCATION_UPDATE_ACTION_TOTAL_DISTANCE, distanceTotal)
        intent.putExtra(C.LOCATION_UPDATE_ACTION_TOTAL_TIME, timeTotal)
        intent.putExtra(C.LOCATION_UPDATE_ACTION_TOTAL_PACE, averageSpeedTotal)
        intent.putExtra(C.LOCATION_UPDATE_ACTION_CP_DIRECT, distanceCPDirect)
        intent.putExtra(C.LOCATION_UPDATE_ACTION_CP_PASSED, distanceCPPassed)
        intent.putExtra(C.LOCATION_UPDATE_ACTION_CP_TIME, timeFmCP)
        intent.putExtra(C.LOCATION_UPDATE_ACTION_WP_DIRECT, distanceToWPDirect)
        intent.putExtra(C.LOCATION_UPDATE_ACTION_WP_PASSED, distanceToWPPassed)
        intent.putExtra(C.LOCATION_UPDATE_ACTION_WP_TIME, estTimeToWP)


        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

    }

    private fun createLocationRequest() {
        //function creates location request
        mLocationRequest.interval = UPDATE_INTERVAL_IN_MILLISECONDS
        mLocationRequest.fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.maxWaitTime = UPDATE_INTERVAL_IN_MILLISECONDS
    }

    private fun getLastLocation() {
        try {
            mFusedLocationClient.lastLocation
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.w(TAG, "task successful")
                            if (task.result != null) {
                                onNewLocation(task.result!!)
                            }
                        } else {

                            Log.w(TAG, "Failed to get location." + task.exception)
                        }
                    }
        } catch (unlikely: SecurityException) {
            Log.e(TAG, "Lost location permission.$unlikely")
        }
    }


    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()

        //stops location updates
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)

        //writes final data to gps activity
        gpsActivity!!.duration = Calendar.getInstance().timeInMillis - gpsActivity!!.timeStart
        gpsActivity!!.speed = averageSpeedTotal
        gpsActivity!!.distance = distanceTotal
        //updates activity in database and backend session
        activityRepo.update(gpsActivity!!)
        activityRepo.updateUser(volley.volleyUser!!)
        // remove notifications
        NotificationManagerCompat.from(this).cancelAll()


        // don't forget to unregister brodcast receiver!!!!
        unregisterReceiver(broadcastReceiver)


        // broadcast stop to UI
        val intent = Intent(C.LOCATION_UPDATE_ACTION)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

        activityRepo.close()

    }

    override fun onLowMemory() {
        Log.d(TAG, "onLowMemory")
        super.onLowMemory()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //function starts location requests, creates gps activity, starts calculations
        Log.d(TAG, "onStartCommand")

        // set counters and locations to 0/null
        currentLocation = null
        locationCP = null
        locationWP = null

        timeStart = Calendar.getInstance().timeInMillis // time of start
        distanceTotal = 0f
        timeTotal = "00:00:00"
        averageSpeedTotal = 0f
        timeAtCP = 0L
        distanceCPDirect = 0f
        distanceCPPassed = 0f
        timeFmCP = "00:00:00"
        distanceToWPDirect = 0f
        distanceToWPPassed = 0f
        estTimeToWP = "00:00"

        //receiving settings from user options (selected in options pop-up in mapActivity
        val activityType = intent!!.getStringExtra("activityType")
        val targetPace = intent.getIntegerArrayListExtra("targetPace")
        UPDATE_INTERVAL_IN_MILLISECONDS = intent.getLongExtra("updateRate", 2000)

        //receiving user data from database for Volley
        volley.volleyUser = activityRepo.getUser()

        //creating gpsActivity with received settings
        gpsActivity = Helpers().createGPSActivity(activityType!!, targetPace!!)

        //writing activity to database and backend server
        activityRepo.addActivity(gpsActivity!!)
        if (volley.volleyUser!!.token != "") {
            volley.postSession(this, gpsActivity!!)
        }

        //starting constant sticky notification in drawer
        showNotification()

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "onBind")
        return null
    }

    override fun onRebind(intent: Intent?) {
        Log.d(TAG, "onRebind")
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "onUnbind")
        return super.onUnbind(intent)

    }

    fun showNotification() {
        //function creates and shows notification in drawer

        //notification buttons settings (pending intents)
        val intentCp = Intent(C.NOTIFICATION_ACTION_CP_SET)
        val intentWp = Intent(C.NOTIFICATION_ACTION_WP_RESET)

        val pendingIntentCp = PendingIntent.getBroadcast(this, 0, intentCp, 0)
        val pendingIntentWp = PendingIntent.getBroadcast(this, 0, intentWp, 0)

        //notification layout settings
        val notifyview = RemoteViews(packageName, R.layout.notification)

        notifyview.setOnClickPendingIntent(R.id.imageButtonCP, pendingIntentCp)
        notifyview.setOnClickPendingIntent(R.id.imageButtonWP, pendingIntentWp)

        //passing calculated data to notification
        notifyview.setTextViewText(R.id.textNotifTotalTime, "TT:%s".format(timeTotal))
        notifyview.setTextViewText(R.id.textNotifTotalDistance, "TD:%.0f m".format(distanceTotal))
        notifyview.setTextViewText(R.id.textNotifAvgSpeed, "AP:%.2f min/km".format(averageSpeedTotal))

        notifyview.setTextViewText(R.id.textNotifDirectDistFmCP, "DD:%.0f m".format(distanceCPDirect))
        notifyview.setTextViewText(R.id.textNotifDistFmCP, "PD:%.0f m".format(distanceCPPassed))
        notifyview.setTextViewText(R.id.textNotifTimeFmCP, "PT:%s".format(timeFmCP))

        notifyview.setTextViewText(R.id.textNotifDirectDistToWP, "DD:%.0f m".format(distanceToWPDirect))
        notifyview.setTextViewText(R.id.textNotifDistPassedWP, "PD:%.0f m".format(distanceToWPPassed))
        notifyview.setTextViewText(R.id.textNotifEstTimeToWP, "ET:%s".format(estTimeToWP))

        // construct and show notification
        val builder = NotificationCompat.Builder(applicationContext, C.NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.baseline_satellite_black_48dp)
                .setContentTitle("GPS Sports Map")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        builder.setContent(notifyview)

        startForeground(C.NOTIFICATION_ID, builder.build())

    }


    private inner class InnerBroadcastReceiver : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onReceive(context: Context?, intent: Intent?) {
            /*
            function receives waypoint responds from mapActivity, as well as intents from notification buttons

            on receive of actions from notifications, sends actions to main activity for ui update and receives back
            responds on WP resetting.
             */
            Log.d(TAG, intent!!.action!!)
            when (intent.action) {
                C.MAIN_ACTION_WP -> {
                    //on receipt changes settings for WP calculations
                    if (intent.getParcelableExtra<Location>(C.MAIN_ACTION_WP) != null) {
                        locationWP = intent.getParcelableExtra(C.MAIN_ACTION_WP)
                        distanceToWPDirect = 0f
                        distanceToWPPassed = 0f
                        showNotification()
                    } else {
                        locationWP = null
                        distanceToWPDirect = 0f
                        distanceToWPPassed = 0f
                        estTimeToWP = "00:00"
                        showNotification()
                    }
                }

                C.NOTIFICATION_ACTION_CP_SET -> {
                    //on receipt, creates CP and changes settings for calculation
                    locationCP = currentLocation
                    val pointCP = LocationPoint(locationCP!!)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        pointCP.verticalAccuracyMeters = locationCP!!.verticalAccuracyMeters
                    }
                    pointCP.typeId = "00000000-0000-0000-0000-000000000003"
                    gpsActivity!!.listOfLocations.add(pointCP)
                    activityRepo.addLocation(pointCP)
                    volley.postCP(this@LocationService, gpsActivity!!, pointCP)
                    distanceCPDirect = 0f
                    distanceCPPassed = 0f
                    showNotification()
                    //sending notice to mapActivity for CP marker creation
                    val setCPMarker = Intent(C.NOTIFICATION_ACTION_CP_SET)
                    setCPMarker.putExtra(C.NOTIFICATION_ACTION_CP_SET, locationCP)
                    LocalBroadcastManager.getInstance(this@LocationService).sendBroadcast(setCPMarker)
                }

                C.NOTIFICATION_ACTION_WP_RESET -> {
                    //resets WP calculations and sends notice to mapActivity for marker reset
                    locationWP = null
                    distanceToWPDirect = 0f
                    distanceToWPPassed = 0f
                    estTimeToWP = "00:00"
                    showNotification()
                    val resetWP = Intent(C.NOTIFICATION_ACTION_WP_RESET)
                    LocalBroadcastManager.getInstance(this@LocationService).sendBroadcast(resetWP)
                }
            }
        }

    }

}