package com.fenko.gpssportsmap

class Kalman {
    private var minAccuracy: Float = 1f
    var qMperS: Float? = null
    var timestampMills: Long = 0
    var lat: Double = 0.0
    var lng: Double = 0.0
    var variance: Float = 0f

    constructor(qMperS: Float) {
        this.qMperS = qMperS
    }

    fun getTimestamp() : Long {
        return this.timestampMills
    }
    @JvmName("getLat1")
    fun getLat() : Double {
        return this.lat
    }
    @JvmName("getLng1")
    fun getLng() : Double {
        return this.lng
    }
    fun getAccuracy() : Float {
        return Math.sqrt(variance.toDouble()).toFloat()
    }

    fun setState(lat: Double, lng: Double, accuracy: Float, timestamp: Long) {
        this.lat = lat
        this.lng = lng
        this.variance = accuracy * accuracy
        this.timestampMills = timestamp
    }

    fun process (latMsmnt: Double, lngMsmt: Double, accuracy: Float, timestamp: Long) {
        var mAccuracy = accuracy
        if (mAccuracy < minAccuracy) {
            mAccuracy = minAccuracy
        }
        if (variance < 0f) {
            this.timestampMills = timestamp
            this.lat = latMsmnt
            this.lng = lngMsmt
            variance = mAccuracy * mAccuracy
        } else {
            val timeIncrementMills = timestamp - this.timestampMills!!
            if (timeIncrementMills > 0) {
                variance += timeIncrementMills * qMperS!! * qMperS!! / 1000
                this.timestampMills = timestamp
            }
        }

        val k = variance / (variance + accuracy * accuracy)
        lat += k * (latMsmnt - lat)
        lng += k * (lngMsmt - lng)
        variance = (1 - k) * variance
    }

}