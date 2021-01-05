package com.fenko.gpssportsmap

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class DBHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_NAME = "test3ActivitiesManager.db"
        const val DATABASE_VERSION = 1

        const val ACTIVITY_TABLE_NAME = "ACTIVITIES"
        const val ACTIVITY_ID = "_id"
        const val ACTIVITY_BACKEND_ID = "id"
        const val ACTIVITY_NAME = "name"
        const val ACTIVITY_DESCRIPTION = "description"
        const val ACTIVITY_RECORDED_AT = "recordedAt"
        const val ACTIVITY_DURATION = "duration"
        const val ACTIVITY_SPEED = "speed"
        const val ACTIVITY_DISTANCE = "distance"
        const val ACTIVITY_CLIMB = "climb"
        const val ACTIVITY_DESCENT = "descent"
        const val ACTIVITY_PACE_MIN = "paceMin"
        const val ACTIVITY_PACE_MAX = "paceMax"
        const val ACTIVITY_TYPE_ID = "gpsSessionTypeId"
        const val ACTIVITY_USER_ID = "appUserId"

        const val LOCATION_TABLE_NAME = "LOCATIONS"
        const val LOCATION_ID = "_id"
        const val LOCATION_ACTIVITY_ID = "activityId"
        const val LOCATION_RECORDED_AT = "recordedAt"
        const val LOCATION_LATITUDE = "latitude"
        const val LOCATION_LONGITUDE = "longitude"
        const val LOCATION_ACCURACY = "accuracy"
        const val LOCATION_ALTITUDE = "altitude"
        const val LOCATION_VERTICAL_ACCURACY = "verticalAccuracy"
        const val LOCATION_SPEED = "speed"
        const val LOCATION_TYPE_ID = "gpsLocationTypeId"

        const val SQL_ACTIVITY_CREATE_TABLE = "create table $ACTIVITY_TABLE_NAME (" +
                "$ACTIVITY_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$ACTIVITY_BACKEND_ID TEXT, " +
                "$ACTIVITY_NAME TEXT NOT NULL, " +
                "$ACTIVITY_DESCRIPTION TEXT, " +
                "$ACTIVITY_RECORDED_AT TEXT NOT NULL, " +
                "$ACTIVITY_DURATION REAL, " +
                "$ACTIVITY_SPEED REAL, " +
                "$ACTIVITY_DISTANCE REAL, " +
                "$ACTIVITY_CLIMB REAL, " +
                "$ACTIVITY_DESCENT REAL, " +
                "$ACTIVITY_PACE_MIN REAL NOT NULL, " +
                "$ACTIVITY_PACE_MAX REAL NOT NULL, " +
                "$ACTIVITY_TYPE_ID TEXT NOT NULL, " +
                "$ACTIVITY_USER_ID TEXT NOT NULL);"

        const val SQL_LOCATION_CREATE_TABLE = "create table $LOCATION_TABLE_NAME (" +
                "$LOCATION_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$LOCATION_ACTIVITY_ID INTEGER, " +
                "$LOCATION_RECORDED_AT REAL NOT NULL, " +
                "$LOCATION_LATITUDE REAL NOT NULL, " +
                "$LOCATION_LONGITUDE REAL NOT NULL, " +
                "$LOCATION_ACCURACY REAL NOT NULL, " +
                "$LOCATION_ALTITUDE REAL NOT NULL, " +
                "$LOCATION_VERTICAL_ACCURACY REAL NOT NULL, " +
                "$LOCATION_SPEED REAL NOT NULL, " +
                "$LOCATION_TYPE_ID TEXT NOT NULL);"

        const val SQL_DELETE_TABLES = "DROP TABLE IF EXISTS $ACTIVITY_TABLE_NAME;\n" +
                "DROP TABLE IF EXISTS $LOCATION_TABLE_NAME;"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(SQL_ACTIVITY_CREATE_TABLE)
        db?.execSQL(SQL_LOCATION_CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL((SQL_DELETE_TABLES))
        onCreate(db)
    }

}