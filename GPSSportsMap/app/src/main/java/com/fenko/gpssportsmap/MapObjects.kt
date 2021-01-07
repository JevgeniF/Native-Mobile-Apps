package com.fenko.gpssportsmap

import android.content.Context
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
    /*
    Class for map objects drawing on map fragment.
     */

    //map settings
    var mapType = 1
    var isZoomControlsEnabled = true
    var isZoomGesturesEnabled = true
    var defaultZoom = 17f

    //polylines
    private var passedRouteOptions: PolylineOptions = PolylineOptions().width(10F)
    private var passedRouteOptionsDef: PolylineOptions = PolylineOptions().width(10F)

    //button settings
    var northUp = false
    var startButtonText = "Start"
    var mapViewButtonText = "North Up"

    fun addMarker(context: Context, position: LatLng?, locationServiceActive: Boolean, map: GoogleMap, view: View): Marker {
        //function adds marker to the map
        lateinit var newMarker: Marker
        if (locationServiceActive) {
            newMarker = map.addMarker(MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_add_location_black_24dp))
                    .title("Add as WP?").position(position!!))
            TSnackbar.make(view, context.getString(R.string.longPressForWP), TSnackbar.LENGTH_LONG).show()
        }
        return newMarker
    }

    fun addStart(location: Location?, map: GoogleMap) {
        //function adds start location marker to the map
        map.addMarker(MarkerOptions().icon(BitmapDescriptorFactory
                .fromResource(R.drawable.baseline_outlined_flag_black_24dp)).title("Start")
                .position(LatLng(location!!.latitude, location.longitude)))
    }

    fun addFinish(location: Location?, map: GoogleMap) {
        //function adds finish location marker to the map
        map.addMarker(MarkerOptions().icon(BitmapDescriptorFactory
                .fromResource(R.drawable.baseline_flag_black_24dp)).title("Finish")
                .position(LatLng(location!!.latitude, location.longitude)))
    }


    fun addWPfmLocation(location: Location?, map: GoogleMap): Marker {
        //function recreates waypoint location marker on the map from its previous location
        return map.addMarker(MarkerOptions().icon(BitmapDescriptorFactory
                .fromResource(R.drawable.baseline_location_on_black_24dp)).title("WP")
                .position(LatLng(location!!.latitude, location.longitude)))

    }

    fun addWPfmMarker(marker: Marker?, locationServiceActive: Boolean, map: GoogleMap, view: View): Marker {
        //function substitutes marker to WP marker on the map
        lateinit var markerWP: Marker
        if (locationServiceActive) {
            if (marker != null) {
                markerWP = map.addMarker(MarkerOptions().icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.baseline_location_on_black_24dp))
                        .title("WP").position(LatLng(marker.position.latitude, marker.position.longitude)))
                val locationWP = Location(LocationManager.GPS_PROVIDER)
                locationWP.latitude = markerWP.position.latitude
                locationWP.longitude = markerWP.position.longitude
                TSnackbar.make(view, "WayPoint created.", TSnackbar.LENGTH_LONG).show()
            }
        }
        return markerWP
    }

    fun addCPMarker(location: Location?, map: GoogleMap) {
        //function adds CP marker to the map
        map.addMarker(MarkerOptions().icon(BitmapDescriptorFactory
                .fromResource(R.drawable.baseline_where_to_vote_black_24dp)).title("CP")
                .position(LatLng(location!!.latitude, location.longitude)))
    }

    fun updateCameraBearing(map: GoogleMap?, bearing: Float) {
        //function updates map camera rotation
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
        //function writes polylines on the map
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