package com.fenko.gpssportsmap

import android.graphics.Color.*
import android.location.Location
import android.location.LocationManager
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import com.androidadvance.topsnackbar.TSnackbar
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*


class MapObjects() : Parcelable {

    var mapType = 1
    var isZoomControlsEnabled = true
    var isZoomGesturesEnabled = true
    var defaultZoom = 17f

    //polylines
    var passedRouteOptions: PolylineOptions = PolylineOptions().width(10F)
    var passedRouteOptionsDef: PolylineOptions = PolylineOptions().width(10F)

    //markers


    var northUp = false
    var startButtonText = "Start"
    var mapViewButtonText = "North Up"

    constructor(parcel: Parcel) : this() {
        mapType = parcel.readInt()
        isZoomControlsEnabled = parcel.readByte() != 0.toByte()
        isZoomGesturesEnabled = parcel.readByte() != 0.toByte()
        defaultZoom = parcel.readFloat()
        passedRouteOptions = parcel.readParcelable(PolylineOptions::class.java.classLoader)!!
        passedRouteOptionsDef = parcel.readParcelable(PolylineOptions::class.java.classLoader)!!
        northUp = parcel.readByte() != 0.toByte()
        startButtonText = parcel.readString().toString()
        mapViewButtonText = parcel.readString().toString()
    }

    fun addMarker(position: LatLng?, locationServiceActive: Boolean, map: GoogleMap, view: View): Marker {
        lateinit var newMarker: Marker
        if (locationServiceActive) {
            newMarker = map.addMarker(MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_add_location_black_24dp))
                    .title("Add as WP?").position(position!!))
            TSnackbar.make(view, "Long press on map to create WayPoint on Marker position. Click to change position of Marker.", TSnackbar.LENGTH_LONG).show()
        }
        return newMarker
    }

    fun addWP(marker: Marker?, locationServiceActive: Boolean, map: GoogleMap, view: View): Marker {
        lateinit var markerWP: Marker
        if (locationServiceActive) {
            if (marker != null) {
                markerWP = map.addMarker(MarkerOptions().icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.baseline_location_on_black_24dp))
                        .title("WP").
                        position(LatLng(marker.position.latitude, marker.position.longitude)))
                val locationWP = Location(LocationManager.GPS_PROVIDER)
                locationWP.latitude = markerWP.position.latitude
                locationWP.longitude = markerWP.position.longitude
                TSnackbar.make(view, "WayPoint created.", TSnackbar.LENGTH_LONG).show()
            }
        }
        return markerWP
    }

    fun addCPMarker(location: Location?, map: GoogleMap) {
        map.addMarker(MarkerOptions().icon(BitmapDescriptorFactory
                .fromResource(R.drawable.baseline_where_to_vote_black_24dp)).title("CP")
                .position(LatLng(location!!.latitude, location.longitude)))
    }

        fun updateCameraBearing(map: GoogleMap?, bearing : Float) {
        if (map == null) return
        val camPos = CameraPosition
                .builder(
                        map.cameraPosition
                )
                .bearing(bearing)
                .build()
        map.animateCamera(CameraUpdateFactory.newCameraPosition(camPos))
    }

    fun uiUpdate(position1: LatLng, position2: LatLng?, speed: Float, fasterPace: Int, slowerPace: Int, map: GoogleMap) {
        val segment = (slowerPace - fasterPace) / 3
        if (speed >= slowerPace) {
            map.addPolyline(PolylineOptions().width(10F).color(RED).add(position1, position2))
        }
        if (speed >= fasterPace + segment * 2) {
            map.addPolyline(PolylineOptions().width(10F).color(MAGENTA).add(position1, position2))
        }
        if (speed >= fasterPace + segment) {
            map.addPolyline(PolylineOptions().width(10F).color(WHITE).add(position1, position2))
        }
        if (speed >= fasterPace) {
            map.addPolyline(PolylineOptions().width(10F).color(YELLOW).add(position1, position2))
        }
        if (speed <= fasterPace) {
            map.addPolyline(PolylineOptions().width(10F).color(GREEN).add(position1, position2))
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(mapType)
        parcel.writeByte(if (isZoomControlsEnabled) 1 else 0)
        parcel.writeByte(if (isZoomGesturesEnabled) 1 else 0)
        parcel.writeFloat(defaultZoom)
        parcel.writeParcelable(passedRouteOptions, flags)
        parcel.writeParcelable(passedRouteOptionsDef, flags)
        parcel.writeByte(if (northUp) 1 else 0)
        parcel.writeString(startButtonText)
        parcel.writeString(mapViewButtonText)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MapObjects> {
        override fun createFromParcel(parcel: Parcel): MapObjects {
            return MapObjects(parcel)
        }

        override fun newArray(size: Int): Array<MapObjects?> {
            return arrayOfNulls(size)
        }
    }

}