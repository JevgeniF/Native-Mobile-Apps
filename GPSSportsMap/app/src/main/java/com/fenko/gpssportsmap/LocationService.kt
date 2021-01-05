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
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.fenko.gpssportsmap.tools.C
import com.fenko.gpssportsmap.tools.Calculator
import com.google.android.gms.location.*
import java.util.*
import java.util.concurrent.TimeUnit


class LocationService : Service() {
    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
    }


    // The desired intervals for location updates. Inexact. Updates may be more or less frequent.
    private val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 2000
    private val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2

    private val broadcastReceiver = InnerBroadcastReceiver()
    private val broadcastReceiverIntentFilter: IntentFilter = IntentFilter()

    private val mLocationRequest: LocationRequest = LocationRequest()
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var mLocationCallback: LocationCallback? = null

    // last received location
    private var currentLocation: Location? = null
    private var currentSpeed = 0f

    private var distanceTotal = 0f
    private var timeTotal = "00:00:00"
    private var averageSpeedTotal = 0f

    private var timeStart = 0L
    private var locationStart: Location? = null

    private var timeAtCP = 0L
    private var distanceCPDirect = 0f
    private var distanceCPPassed = 0f
    private var averageSpeedCP = 0f
    private var locationCP: Location? = null

    private var timeWPSet = 0L
    private var locationWPSet: Location? = null
    private var locationWP : Location? = null
    private var distanceToWPDirect = 0f
    private var distanceToWPPassed = 0f
    private var averageSpeedToWP = 0f

    //activity
    var gpsActivity: GPSActivity? = null

    //repo
    private lateinit var activityRepo: ActivityRepo


    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()

        activityRepo = ActivityRepo(this).open()

        broadcastReceiverIntentFilter.addAction(C.NOTIFICATION_ACTION_CP_SET)
        broadcastReceiverIntentFilter.addAction(C.NOTIFICATION_ACTION_WP_RESET)
        broadcastReceiverIntentFilter.addAction(C.MAIN_ACTION_CP)
        broadcastReceiverIntentFilter.addAction(C.MAIN_ACTION_WP)
        broadcastReceiverIntentFilter.addAction(C.LOCATION_UPDATE_ACTION)


        registerReceiver(broadcastReceiver, broadcastReceiverIntentFilter)


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                onNewLocation(locationResult.lastLocation)
            }
        }

        createLocationRequest()
        requestLocationUpdates()

    }

    private fun requestLocationUpdates() {
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
        Log.i(TAG, "New location: $location")

        val locationPoint = LocationPoint(location)
        if (gpsActivity!!.listOfLocations.isEmpty()) {
            location.speed = 0f
            gpsActivity!!.listOfLocations.add(locationPoint)
            activityRepo.addLocation(locationPoint)
        } else {
            currentSpeed = Calculator().paceAtLocation(gpsActivity!!.listOfLocations.last()!!, locationPoint)
            currentLocation!!.speed = currentSpeed
            locationPoint.speed = currentSpeed
            activityRepo.addLocation(locationPoint)
        }
        if (currentLocation == null){
            locationStart = location
            locationCP = location
        } else {
            // overall metrics
            val timeNow = Calendar.getInstance().timeInMillis
            distanceTotal += location.distanceTo(currentLocation)
            timeTotal = Calculator().totalTime(timeStart, timeNow)
            averageSpeedTotal = 50 / (3 * (distanceTotal/TimeUnit.MILLISECONDS.toSeconds(timeNow - timeStart)))

            //cp metrics
            if(locationCP != location) {
                distanceCPDirect = locationCP!!.distanceTo(currentLocation)
                distanceCPPassed += locationCP!!.distanceTo(currentLocation)
                averageSpeedCP = 50 / (3 * (distanceCPPassed / TimeUnit.MILLISECONDS.toSeconds(timeNow - timeAtCP)))
            }
            //wp metrics
            if (locationWP != null) {
                distanceToWPDirect = locationWPSet!!.distanceTo(locationWP)
                distanceToWPPassed += locationWPSet!!.distanceTo(currentLocation)
                averageSpeedToWP = 50 / (3 * (distanceToWPPassed / TimeUnit.MILLISECONDS.toSeconds(timeNow - timeWPSet)))
            }
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
        intent.putExtra(C.LOCATION_UPDATE_ACTION_CP_PACE, averageSpeedCP)
        intent.putExtra(C.LOCATION_UPDATE_ACTION_WP_DIRECT, distanceToWPDirect)
        intent.putExtra(C.LOCATION_UPDATE_ACTION_WP_PASSED, distanceToWPPassed)
        intent.putExtra(C.LOCATION_UPDATE_ACTION_WP_PACE, averageSpeedToWP)


        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

    }

    private fun createLocationRequest() {
        mLocationRequest.interval = UPDATE_INTERVAL_IN_MILLISECONDS
        mLocationRequest.fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.maxWaitTime = UPDATE_INTERVAL_IN_MILLISECONDS
    }


    private fun getLastLocation() {
        try {
            mFusedLocationClient.lastLocation
                .addOnCompleteListener { task -> if (task.isSuccessful) {
                    Log.w(TAG, "task successful")
                    if (task.result != null){
                        onNewLocation(task.result!!)
                    }
                } else {

                    Log.w(TAG, "Failed to get location." + task.exception)
                }}
        } catch (unlikely: SecurityException) {
            Log.e(TAG, "Lost location permission.$unlikely")
        }
    }


    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()

        //stop location updates
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)

        gpsActivity!!.duration = Calendar.getInstance().timeInMillis - gpsActivity!!.timeStart
        gpsActivity!!.speed = averageSpeedTotal
        gpsActivity!!.distance = distanceTotal
        activityRepo.update(gpsActivity!!)
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
        Log.d(TAG, "onStartCommand")

        // set counters and locations to 0/null
        currentLocation = null
        locationStart = null
        locationCP = null
        locationWPSet = null

        timeStart = Calendar.getInstance().timeInMillis
        distanceTotal = 0f
        timeTotal = "00:00:00"
        averageSpeedTotal = 0f
        timeAtCP = 0L
        distanceCPDirect = 0f
        distanceCPPassed = 0f
        averageSpeedCP = 0f
        timeWPSet = 0L
        distanceToWPDirect = 0f
        distanceToWPPassed = 0f
        averageSpeedToWP = 0f

        gpsActivity = GPSActivity()
        activityRepo.addActivity(gpsActivity!!)

        showNotification()

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "onBind")
        TODO("not implemented")
    }

    override fun onRebind(intent: Intent?) {
        Log.d(TAG, "onRebind")
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "onUnbind")
        return super.onUnbind(intent)

    }

    fun showNotification(){
        val intentCp = Intent(C.NOTIFICATION_ACTION_CP_SET)
        val intentWp = Intent(C.NOTIFICATION_ACTION_WP_RESET)

        val pendingIntentCp = PendingIntent.getBroadcast(this, 0, intentCp, 0)
        val pendingIntentWp = PendingIntent.getBroadcast(this, 0, intentWp, 0)

        val notifyview = RemoteViews(packageName, R.layout.notification)

        notifyview.setOnClickPendingIntent(R.id.imageButtonCP, pendingIntentCp)
        notifyview.setOnClickPendingIntent(R.id.imageButtonWP, pendingIntentWp)


        notifyview.setTextViewText(R.id.textTime, "%s".format(timeTotal))
        notifyview.setTextViewText(R.id.textDistance, "%.2f m".format(distanceTotal))
        notifyview.setTextViewText(R.id.textSpeed, "%.2f min/km".format(averageSpeedTotal))

        notifyview.setTextViewText(R.id.textDirectFmCP, "%.2f m".format(distanceCPDirect))
        notifyview.setTextViewText(R.id.textDistanceFmCP, "%.2f m".format(distanceCPPassed))
        notifyview.setTextViewText(R.id.textAvgSpeedCP, "%.2f min/km".format(averageSpeedCP))

        notifyview.setTextViewText(R.id.textDirectToWP, "%.2f m".format(distanceToWPDirect))
        notifyview.setTextViewText(R.id.textDistanceToWP, "%.2f m".format(distanceToWPPassed))
        notifyview.setTextViewText(R.id.TextAvgSpeedToWP, "%.2f min/km".format(averageSpeedToWP))

        // construct and show notification
        val builder = NotificationCompat.Builder(applicationContext, C.NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.baseline_gps_not_fixed_24)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        builder.setContent(notifyview)

        startForeground(C.NOTIFICATION_ID, builder.build())

    }


    private inner class InnerBroadcastReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, intent!!.action!!)
            when(intent.action){
                C.MAIN_ACTION_WP -> {
                    if (intent.getParcelableExtra<Location>(C.MAIN_ACTION_WP) != null) {
                        locationWPSet = currentLocation
                        locationWP = intent.getParcelableExtra(C.MAIN_ACTION_WP)
                        timeWPSet = Calendar.getInstance().timeInMillis
                        distanceToWPDirect = 0f
                        distanceToWPPassed = 0f
                        showNotification()
                    } else {
                        locationWP = null
                        distanceToWPDirect = 0f
                        distanceToWPPassed = 0f
                        averageSpeedToWP = 0f
                        showNotification()
                    }
                }
                C.NOTIFICATION_ACTION_CP_SET -> {
                    locationCP = currentLocation
                    val pointCP = LocationPoint(locationCP!!.latitude, locationCP!!.longitude, locationCP!!.accuracy, locationCP!!.altitude, locationCP!!.time)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        pointCP.verticalAccuracyMeters = locationCP!!.verticalAccuracyMeters
                    }
                    pointCP.speed = Calculator().paceAtLocation(gpsActivity!!.listOfLocations.last()!!, pointCP)
                    pointCP.typeId = "00000000-0000-0000-0000-000000000003"
                    gpsActivity!!.listOfLocations.add(pointCP)
                    activityRepo.addLocation(pointCP)
                    distanceCPDirect = 0f
                    distanceCPPassed = 0f
                    showNotification()
                    val setCPMarker = Intent(C.NOTIFICATION_ACTION_CP_SET)
                    setCPMarker.putExtra(C.NOTIFICATION_ACTION_CP_SET, locationCP)
                    LocalBroadcastManager.getInstance(this@LocationService).sendBroadcast(setCPMarker)
                }

                C.NOTIFICATION_ACTION_WP_RESET -> {
                    locationWP = null
                    distanceToWPDirect = 0f
                    distanceToWPPassed = 0f
                    averageSpeedToWP = 0f
                    showNotification()
                    val resetWP = Intent(C.NOTIFICATION_ACTION_WP_RESET)
                    LocalBroadcastManager.getInstance(this@LocationService).sendBroadcast(resetWP)
                }
            }
        }

    }

}