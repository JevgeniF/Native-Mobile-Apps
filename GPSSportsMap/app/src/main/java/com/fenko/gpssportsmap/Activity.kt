package com.fenko.gpssportsmap

import android.os.Parcel
import android.os.Parcelable
import java.util.*

class Activity() : Parcelable {

    var id: Int = 0
    var name: String = ""
    private var recordedAt: Long = Calendar.getInstance(TimeZone.getTimeZone("GMT")).timeInMillis
    private var paceMin: Int = 360
    private var paceMax: Int = 720
    private var gpsSessionTypeId: String = "00000000-0000-0000-0000-000000000003"
    private var description: String = ""

    var wayPoint: LocationPoint? = null
    var listOfLocationPoints: ArrayList<LocationPoint> = arrayListOf()

    constructor(name: String) : this() {
        this.name = name
    }

    constructor(parcel: Parcel) : this() {
        id = parcel.readInt()
        recordedAt = parcel.readLong()
        name = parcel.readString().toString()
        paceMin = parcel.readInt()
        paceMax = parcel.readInt()
        gpsSessionTypeId = parcel.readString().toString()
        description = parcel.readString().toString()
        wayPoint = parcel.readParcelable(LocationPoint::class.java.classLoader)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeInt(paceMin)
        parcel.writeInt(paceMax)
        parcel.writeString(gpsSessionTypeId)
        parcel.writeString(description)
        parcel.writeParcelable(wayPoint, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Activity> {
        override fun createFromParcel(parcel: Parcel): Activity {
            return Activity(parcel)
        }

        override fun newArray(size: Int): Array<Activity?> {
            return arrayOfNulls(size)
        }
    }

}