package com.fenko.gpssportsmap

import android.graphics.Color
import com.google.android.gms.maps.model.PolylineOptions

class Settings {

    //map ui
    var mapType = 2
    var isZoomControlsEnabled = true
    var isZoomGesturesEnabled = true
    var isCompassEnabled = false
    var isMyLocationEnabled = true
    var defaultZoom = 17f

    //polylines
    var pathOptions: PolylineOptions? = PolylineOptions().width(5F).color(Color.YELLOW)
    var passedRouteOptions: PolylineOptions? = PolylineOptions().width(7F).color(Color.RED)

    //markers


    var northUp = false
}