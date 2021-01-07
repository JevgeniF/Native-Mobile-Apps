package com.fenko.gpssportsmap.objects

import com.fenko.gpssportsmap.tools.Helpers
import java.util.*

class GPSActivity() {
    /*
    Main object for tracking of user activity.
     */

    var id: Long = 0L           //database table row id
    var backendId: String = ""  //backend session id
    var name: String = Helpers().converterTime(Calendar.getInstance().timeInMillis) //by default name is activity creation time
    var description: String = "Running - easy" //default description, changes when activity type changed to another
    var recordedAt: String? = Helpers().converterTime(Calendar.getInstance().timeInMillis) //creation time, recorded automatically as String
    var timeStart: Long = Calendar.getInstance().timeInMillis // creation time, recorded automatically as Long in milliseconds
    var duration: Long = 0L     //activity duration
    var speed: Float = 0f       //activity average speed
    var distance: Float = 0f    //activity total distance
    var climb: Double = 0.0     // not tracked locally
    var descent: Double = 0.0   // not tracked locally
    var paceMin: Int = 360      // default Min pace for Running -easy, changes when activity type changed to another
    var paceMax: Int = 600      // default Max pace for Running -easy, changes when activity type changed to another
    var gpsSessionTypeId: String = "00000000-0000-0000-0000-000000000001" //// default backend typeID for Running -easy, changes when activity type changed to another
    var userId: String = ""     //user id from backend
    var badPace: Int = 7        // lower level of user pace target, used for polylines
    var goodPace: Int = 4       // upper level of user pace target, used for polylines

    var listOfLocations: ArrayList<LocationPoint?> = arrayListOf() //list of locationPoints of this activity

    constructor(id: Long,
                backend_id: String,
                name: String,
                description: String,
                recordedAt: String,
                duration: Long,
                speed: Float,
                distance: Float,
                climb: Double,
                descent: Double,
                paceMin: Int,
                paceMax: Int,
                gpsSessionTypeId: String,
                userId: String,
                badPace: Int,
                goodPace: Int) : this() {
        //used for read activity data from database
        this.id = id
        this.backendId = backend_id
        this.name = name
        this.description = description
        this.recordedAt = recordedAt
        this.duration = duration
        this.speed = speed
        this.distance = distance
        this.climb = climb
        this.descent = descent
        this.paceMin = paceMin
        this.paceMax = paceMax
        this.gpsSessionTypeId = gpsSessionTypeId
        this.userId = userId
        this.badPace = badPace
        this.goodPace = goodPace
    }

    constructor(activityType: String, description: String, paceMin: Int, paceMax: Int, badPace: Int, goodPace: Int) : this() {
        //used for start of new activity with minimum data
        this.gpsSessionTypeId = activityType
        this.description = description
        this.paceMin = paceMin
        this.paceMax = paceMax
        this.badPace = badPace
        this.goodPace = goodPace
    }
}