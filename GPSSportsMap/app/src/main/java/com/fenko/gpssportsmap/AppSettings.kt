package com.fenko.gpssportsmap

import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.maps.model.PolylineOptions

class AppSettings() : Parcelable{

    //map ui
    var mapType = 1
    var isZoomControlsEnabled = true
    var isZoomGesturesEnabled = true
    var isCompassEnabled = false
    var defaultZoom = 17f

    //polylines
    var passedRouteOptions: PolylineOptions = PolylineOptions().width(7F).color(Color.RED)
    var passedRouteOptionsDef: PolylineOptions = PolylineOptions().width(7F).color(Color.RED)

    //markers


    var northUp = false

    constructor(parcel: Parcel) : this() {
        mapType = parcel.readInt()
        isZoomControlsEnabled = parcel.readByte() != 0.toByte()
        isZoomGesturesEnabled = parcel.readByte() != 0.toByte()
        isCompassEnabled = parcel.readByte() != 0.toByte()
        defaultZoom = parcel.readFloat()
        passedRouteOptions = parcel.readParcelable(PolylineOptions::class.java.classLoader)!!
        passedRouteOptionsDef = parcel.readParcelable(PolylineOptions::class.java.classLoader)!!
        northUp = parcel.readByte() != 0.toByte()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(mapType)
        parcel.writeByte(if (isZoomControlsEnabled) 1 else 0)
        parcel.writeByte(if (isZoomGesturesEnabled) 1 else 0)
        parcel.writeByte(if (isCompassEnabled) 1 else 0)
        parcel.writeFloat(defaultZoom)
        parcel.writeParcelable(passedRouteOptions, flags)
        parcel.writeParcelable(passedRouteOptionsDef, flags)
        parcel.writeByte(if (northUp) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AppSettings> {
        override fun createFromParcel(parcel: Parcel): AppSettings {
            return AppSettings(parcel)
        }

        override fun newArray(size: Int): Array<AppSettings?> {
            return arrayOfNulls(size)
        }
    }
}