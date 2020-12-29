package com.fenko.gpssportsmap

import android.os.Parcel
import android.os.Parcelable
import java.util.*

class LocationPoint() : Parcelable{

    var id: Int = 0
    private var recordedAt: Long = Calendar.getInstance(TimeZone.getTimeZone("GMT")).timeInMillis
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    private var altitude: Double = 0.0
    var bearing: Float = 0F
    var speed: Double = 0.0
    private var accuracy: Float = 0F
    var vAccuracy: Float = 0F
    var typeId: String = ""
    private var description: String = ""

    constructor(latitude: Double, longitude: Double) : this() {
        this.latitude = latitude
        this.longitude = longitude
    }
    constructor(latitude: Double, longitude: Double, altitude: Double, bearing: Float, accuracy: Float, typeId: String) : this() {
        this.latitude = latitude
        this.longitude = longitude
        this.altitude = altitude
        this.bearing = bearing
        this.accuracy = accuracy
        this.typeId = typeId
    }

    constructor(parcel: Parcel) : this() {
        id = parcel.readInt()
        recordedAt = parcel.readLong()
        latitude = parcel.readDouble()
        longitude = parcel.readDouble()
        altitude = parcel.readDouble()
        bearing = parcel.readFloat()
        speed = parcel.readDouble()
        accuracy = parcel.readFloat()
        vAccuracy = parcel.readFloat()
        typeId = parcel.readString().toString()
        description = parcel.readString().toString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeLong(recordedAt)
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeDouble(altitude)
        parcel.writeFloat(bearing)
        parcel.writeDouble(speed)
        parcel.writeFloat(accuracy)
        parcel.writeFloat(vAccuracy)
        parcel.writeString(typeId)
        parcel.writeString(description)
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