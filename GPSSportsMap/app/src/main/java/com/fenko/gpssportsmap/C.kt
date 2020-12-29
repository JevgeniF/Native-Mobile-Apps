package com.fenko.gpssportsmap

class C {
    companion object {
        const val NOTIFICATION_CHANNEL = "default_channel"
        const val NOTIFICATION_ACTION_WP = "com.fenko.wp"
        const val NOTIFICATION_ACTION_CP = "com.fenko.cp"

        const val LOCATION_UPDATE_ACTION = "com.fenko.location_update"
        const val LOCATION_UPDATE_ACTION_LATITUDE = "com.fenko.location_update.latitude"
        const val LOCATION_UPDATE_ACTION_LONGITUDE = "com.fenko.location_update.longitude"
        val LOCATION_UPDATE_ACTION_TOTAL_DISTANCE = "com.fenko.location_update.totalDistance"
        val LOCATION_UPDATE_ACTION_TOTAL_TIME = "com.fenko.location_update.totalTime"
        val LOCATION_UPDATE_ACTION_TOTAL_PACE = "com.fenko.location_update.totalPace"

        const val NOTIFICATION_ID = 4321
        const val REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    }

}