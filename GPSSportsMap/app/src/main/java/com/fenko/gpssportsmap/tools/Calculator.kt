package com.fenko.gpssportsmap.tools

import com.fenko.gpssportsmap.objects.LocationPoint
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class Calculator {

    fun paceAtLocation(lastLocationPoint: LocationPoint, currentLocationPoint: LocationPoint): Float {
        val lastLocationTime = lastLocationPoint.time
        val currentLocationTime = currentLocationPoint.time

        val passedTime = currentLocationTime - lastLocationTime
        val passedDistance = lastLocationPoint.distanceTo(currentLocationPoint)

        return 50 / (3 * (passedDistance / TimeUnit.MILLISECONDS.toSeconds(passedTime)))
    }

    fun totalTime(startTime: Long, endTime: Long): String {
        val passedTime = endTime - startTime
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(passedTime),
                TimeUnit.MILLISECONDS.toMinutes(passedTime) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(passedTime)),
                TimeUnit.MILLISECONDS.toSeconds(passedTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(passedTime)))
    }

    fun compassDirection (bearing: Float): String {
        var direction = ""
        if (bearing >= 356f || bearing <= 4f) {
            direction = "N"
        } else if (bearing in 5f..40f) {
            direction = "NNE"
        } else if (bearing in 41f..49f) {
            direction = "NE"
        } else if (bearing in 50f..85f) {
            direction = "ENE"
        } else if (bearing in 86f..94f) {
            direction = "E"
        } else if (bearing in 95f..130f) {
            direction = "ESE"
        } else if (bearing in 131f..139f) {
            direction = "SE"
        } else if (bearing in 140f..175f) {
            direction = "SSE"
        } else if (bearing in 176f..184f) {
            direction = "S"
        } else if (bearing in 185f..220f) {
            direction = "SSW"
        } else if (bearing in 221f..229f) {
            direction = "SW"
        } else if (bearing in 230f..265f) {
            direction = "WSW"
        } else if (bearing in 266f..274f) {
            direction = "W"
        } else if (bearing in 275f..310f) {
            direction = "WNW"
        } else if (bearing in 311f..319f) {
            direction = "NW"
        } else if (bearing in 320f..355f) {
            direction = "NNW"
        }
        return direction
    }

    fun converterHMS (time: Long): String {
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(time),
                TimeUnit.MILLISECONDS.toMinutes(time) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time)),
                TimeUnit.MILLISECONDS.toSeconds(time) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time)))

    }

    fun converterTime(time: Long): String {
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        //df.timeZone = TimeZone.getTimeZone("UTC")
        return df.format(time)
    }
}