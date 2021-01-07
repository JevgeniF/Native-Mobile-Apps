package com.fenko.gpssportsmap.tools

import kotlin.math.sqrt

class Kalman(qMperS: Float) {
    /*
    Class created as filter for GPS/Fused location noise in order to make navigation smoother and
    location jumps smaller.
    Idea is to filter every location. By the time, with every new location, the filter parameters
    will change and location will be more accurate.
    Current observations show that location jumps distance decreased to 10 meters (in closed premises).
    Previously location jumped to another streets.
     */


    private var minAccuracy: Float = 1f
    private var qMperS: Float? = qMperS     //Q (meters per second) describes how quickly accuracy decays
    private var timestampMills: Long = 0
    private var lat: Double = 0.0
    private var lng: Double = 0.0
    private var variance: Float = -1f //matrix P

    fun getLat(): Double {
        return this.lat
    }

    fun getLng(): Double {
        return this.lng
    }

    fun getAccuracy(): Float {
        return sqrt(variance)
    }

    fun process(latMsmnt: Double, lngMsmt: Double, accuracy: Float, timestamp: Long) {
        //Kalman filter processing for latitude and longitude
        var mAccuracy = accuracy
        if (mAccuracy < minAccuracy) {
            mAccuracy = minAccuracy
        }
        if (variance < 0f) {
            //object is uninitialised, so initialise with current values
            this.timestampMills = timestamp
            this.lat = latMsmnt
            this.lng = lngMsmt
            variance = mAccuracy * mAccuracy
        } else {
            //apply Kalman filter method
            val timeIncrementMills = timestamp - this.timestampMills
            if (timeIncrementMills > 0) {
                //time moved, so possibility of noise (uncertainty in position) increases
                variance += timeIncrementMills * qMperS!! * qMperS!! / 1000
                this.timestampMills = timestamp
            }
        }
        //Kalman gain matrix K
        val k = variance / (variance + accuracy * accuracy)
        //apply K
        lat += k * (latMsmnt - lat)
        lng += k * (lngMsmt - lng)

        //new covariance matrix
        variance *= (1 - k)
    }

}