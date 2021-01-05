package com.fenko.gpssportsmap

import java.text.SimpleDateFormat
import java.util.*

class GPSActivity() {

    var listId: Int = 0
    var id: Long = 0L
    var backendId: String = ""
    var name: String = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(
        Calendar.getInstance(TimeZone.getTimeZone("GMT")).time)
    var description: String = ""
    var recordedAt: String? = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(Calendar.getInstance(TimeZone.getTimeZone("GMT")).time)
    var timeStart: Long = Calendar.getInstance().timeInMillis
    var duration: Long = 0L
    var speed: Float = 0f
    var distance: Float = 0f
    var climb: Double = 0.0
    var descent: Double = 0.0
    var paceMin: Int = 360
    var paceMax: Int = 720
    var gpsSessionTypeId: String = "00000000-0000-0000-0000-000000000003"
    var userId: String = ""

    var listOfLocations: ArrayList<LocationPoint?> = arrayListOf()

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
                userId: String) : this() {
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
                }
}