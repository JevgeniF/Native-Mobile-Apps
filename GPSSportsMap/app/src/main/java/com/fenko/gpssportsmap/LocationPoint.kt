package com.fenko.gpssportsmap

import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Parcel
import android.os.Parcelable

class LocationPoint() : Location(LocationManager.GPS_PROVIDER), Parcelable{

    var id: Long = 0L
    var activityId: Long = 0L
    var typeId: String = "00000000-0000-0000-0000-000000000001"

    constructor(id: Long, activityId: Long, time: Long, latitude: Double, longitude: Double, accuracy: Float, altitude: Double, verticalAccuracy: Float = 0f, speed: Float, typeId: String) : this() {
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

    constructor(location: Location): this() {
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

    constructor(latitude: Double, longitude: Double, accuracy: Float, altitude: Double, time: Long) : this() {
        this.latitude = latitude
        this.longitude = longitude
        this.accuracy = accuracy
        this.altitude = altitude
        this.time = time
    }

    constructor(latitude: Double, longitude: Double) : this() {
        this.latitude = latitude
        this.longitude = longitude
    }

    constructor(parcel: Parcel) : this() {
        id = parcel.readLong()
        activityId = parcel.readLong()
        //backendId = parcel.readString().toString()
        time = parcel.readLong()
        speed = parcel.readFloat()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            verticalAccuracyMeters = parcel.readFloat()
        }
        altitude = parcel.readDouble()
        accuracy = parcel.readFloat()
        longitude = parcel.readDouble()
        latitude = parcel.readDouble()
        typeId = parcel.readString().toString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeLong(activityId)
        //parcel.writeString(backendId)
        parcel.writeLong(time)
        parcel.writeFloat(speed)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            parcel.writeFloat(verticalAccuracyMeters)
        }
        parcel.writeDouble(altitude)
        parcel.writeFloat(accuracy)
        parcel.writeDouble(longitude)
        parcel.writeDouble(latitude)
        parcel.writeString(typeId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LocationPoint> {
        override fun createFromParcel(parcel: Parcel): LocationPoint {
            return LocationPoint(parcel)
        }

        override fun newArray(size: Int): Array<LocationPoint?> {
            return arrayOfNulls(size)
        }
    }

}