package com.fenko.gpssportsmap.objects

import android.location.Location
import android.location.LocationManager
import android.os.Build

class LocationPoint() : Location(LocationManager.GPS_PROVIDER) {
    /*
    Class used for tracking of gps activity as tracking points.
    The idea was to make same as Location.class with additional data required for backend or database
     */

    var id: Long = 0L           //database id
    var activityId: Long = 0L   // activity database id
    var typeId: String = "00000000-0000-0000-0000-000000000001" //backend typeID, default for usual update, changes for CP

    constructor(id: Long, activityId: Long, time: Long, latitude: Double, longitude: Double, accuracy: Float, altitude: Double, verticalAccuracy: Float = 0f, speed: Float, typeId: String) : this() {
        //used to read data from database
        this.id = id
        this.activityId = activityId
        this.time = time
        this.latitude = latitude
        this.longitude = longitude
        this.accuracy = accuracy
        this.altitude = altitude
        this.speed = speed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.verticalAccuracyMeters = verticalAccuracy
        }
        this.typeId = typeId
    }

    constructor(location: Location) : this() {
        //used to generate LocationPoint from as Location can not be casted
        this.accuracy = location.accuracy
        this.altitude = location.altitude
        this.latitude = location.latitude
        this.longitude = location.longitude
        this.provider = location.provider
        this.speed = location.speed
        this.time = location.time
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.verticalAccuracyMeters = location.verticalAccuracyMeters
        }


    }
}