package com.fenko.gpssportsmap

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline
import org.json.JSONObject

class Activity {

    //state
    var started = false
    var paused = false
    var completed = false
    var sessionId: String?  = null

    //polylines
    var passedRoute: Polyline? = null
    var path: Polyline? = null

    //locations
    var currentLocation: Location? = null
    var currentLatLng: LatLng? = null

    //markers
    var marker: Marker? = null
    var markerLatLng: LatLng? = null
    var wayPoint: Marker? = null
    var wayPointLatLng: LatLng? = null
    var wayPointId: String? = null
    var checkPoint: Marker? = null
    var checkPointLocation : Location? = null
    var start: Marker? = null
    var startLocation: Location? = null

    //data
    var totalDistance: Double = 0.0
    var duration: Double = 0.0
    var averageSpeed: Double = 0.0

    var locationUpdates: MutableList<JSONObject>? = null
    var checkPoints: MutableList<JSONObject>? = null
    var wayPointJson: JSONObject? = null
}