package com.fenko.gpssportsmap.tools

class C {
    //strings for broadcasting between services and activities. request codes
    companion object {
        const val MAIN_ACTION_WP = "com.fenko.wp"
        const val MAIN_ACTION_CP = "com.fenko.cp"

        const val NOTIFICATION_CHANNEL = "default_channel"
        const val NOTIFICATION_ACTION_WP_RESET = "com.fenko.wp_reset"
        const val NOTIFICATION_ACTION_CP_SET = "com.fenko.cp_set"

        const val LOCATION_UPDATE_ACTION = "com.fenko.location_update"

        const val LOCATION_UPDATE_ACTION_TOTAL_DISTANCE = "com.fenko.location_update.totalDistance"
        const val LOCATION_UPDATE_ACTION_TOTAL_TIME = "com.fenko.location_update.totalTime"
        const val LOCATION_UPDATE_ACTION_TOTAL_PACE = "com.fenko.location_update.totalPace"

        const val LOCATION_UPDATE_ACTION_CP_DIRECT = "com.fenko.location_update.cpDirect"
        const val LOCATION_UPDATE_ACTION_CP_PASSED = "com.fenko.location_update.cpPassed"
        const val LOCATION_UPDATE_ACTION_CP_TIME = "com.fenko.location_update.cpTime"

        const val LOCATION_UPDATE_ACTION_WP_DIRECT = "com.fenko.location_update.wpDirect"
        const val LOCATION_UPDATE_ACTION_WP_PASSED = "com.fenko.location_update.wpPassed"
        const val LOCATION_UPDATE_ACTION_WP_TIME = "com.fenko.location_update.wpTime"

        const val NOTIFICATION_ID = 4321
        const val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    }

}