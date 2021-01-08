package com.fenko.gpssportsmap.tools

import android.location.Location
import com.fenko.gpssportsmap.objects.GPSActivity
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class Helpers {
    /*
    Class contains helping functions, converters, calculators
     */

    fun paceAtLocation(lastLocation: Location, currentLocation: Location): Float {
        //function calculates pace min/km between two locations. Returns pace min/km: Float
        val lastLocationTime = lastLocation.time
        val currentLocationTime = currentLocation.time

        val passedTime = currentLocationTime - lastLocationTime
        val passedDistance = lastLocation.distanceTo(currentLocation)

        return 50 / (3 * (passedDistance / TimeUnit.MILLISECONDS.toSeconds(passedTime)))
    }

    fun totalTime(startTime: Long, endTime: Long): String {
        //function calculates time between two events and converts it from milliseconds to normal HH.mm.ss format
        //returns HH.mm.ss time as String
        val passedTime = endTime - startTime
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(passedTime),
                TimeUnit.MILLISECONDS.toMinutes(passedTime) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(passedTime)),
                TimeUnit.MILLISECONDS.toSeconds(passedTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(passedTime)))
    }

    fun compassDirection(bearing: Float): String {
        //function for naming directions on bearing basis. Returns name of direction as String
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

    fun converterHMS(time: Long): String {
        //function converts time from milliseconds to HH.mm.ss. Returns HH.mm.ss time as string
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(time),
                TimeUnit.MILLISECONDS.toMinutes(time) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time)),
                TimeUnit.MILLISECONDS.toSeconds(time) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time)))

    }

    fun converterStoMS(time: Long): String {
        //function converts time from seconds to mm.ss. Returns mm.ss time as string
        return String.format("%02d:%02d",
                TimeUnit.SECONDS.toMinutes(time),
                time - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(time)))
    }

    fun converterTime(time: Long): String {
        //function converts time from milliseconds to special pattern used in GPX and on backend server.
        //returns time in pattern as String
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        return df.format(time)
    }

    fun createGPSActivity(activityType: String, targetPace: ArrayList<Int>): GPSActivity {
        //functions creates and returns GPSActivity object. Used on gpsActivity start, to remove
        //long "when" code and make code more compact
        var paceMin = 360
        var paceMax = 600
        var description = "Running - easy"
        when (activityType) {
            "00000000-0000-0000-0000-000000000001" -> {
                paceMin = 360
                paceMax = 600
                description = "Running - easy"
            }
            "00000000-0000-0000-0000-000000000002" -> {
                paceMin = 300
                paceMax = 420
                description = "Running"
            }
            "00000000-0000-0000-0000-000000000003" -> {
                paceMin = 360
                paceMax = 720
                description = "Orienteering - easy"
            }
            "00000000-0000-0000-0000-000000000004" -> {
                paceMin = 300
                paceMax = 540
                description = "Orienteering - competition"
            }
            "00000000-0000-0000-0000-000000000005" -> {
                paceMin = 180
                paceMax = 360
                description = "Bicycle - easy"
            }
            "00000000-0000-0000-0000-000000000006" -> {
                paceMin = 120
                paceMax = 300
                description = "Bicycle - competition"
            }
        }
        return GPSActivity(activityType, description, paceMin, paceMax, targetPace[0], targetPace[1])
    }
}