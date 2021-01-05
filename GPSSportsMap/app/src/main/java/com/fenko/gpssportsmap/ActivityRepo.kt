package com.fenko.gpssportsmap

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase

class ActivityRepo(private val context: Context) {

    private lateinit var dbHelper: DBHelper
    private lateinit var db: SQLiteDatabase
    private var activityId: Long? = null

    fun open(): ActivityRepo {
        dbHelper = DBHelper(context)
        db = dbHelper.writableDatabase

        return this
    }

    fun close() {
        dbHelper.close()
    }

    fun addActivity(gpsActivity: GPSActivity) {
        val cv = ContentValues()
        cv.put(DBHelper.ACTIVITY_BACKEND_ID, gpsActivity.backendId)
        cv.put(DBHelper.ACTIVITY_NAME, gpsActivity.name)
        cv.put(DBHelper.ACTIVITY_DESCRIPTION, gpsActivity.description)
        cv.put(DBHelper.ACTIVITY_RECORDED_AT, gpsActivity.recordedAt)
        cv.put(DBHelper.ACTIVITY_DURATION, gpsActivity.duration)
        cv.put(DBHelper.ACTIVITY_SPEED, gpsActivity.speed)
        cv.put(DBHelper.ACTIVITY_DISTANCE, gpsActivity.distance)
        cv.put(DBHelper.ACTIVITY_CLIMB, gpsActivity.climb)
        cv.put(DBHelper.ACTIVITY_DESCENT, gpsActivity.descent)
        cv.put(DBHelper.ACTIVITY_PACE_MIN, gpsActivity.paceMin)
        cv.put(DBHelper.ACTIVITY_PACE_MAX, gpsActivity.paceMax)
        cv.put(DBHelper.ACTIVITY_TYPE_ID, gpsActivity.gpsSessionTypeId)
        cv.put(DBHelper.ACTIVITY_USER_ID, gpsActivity.userId)
        activityId = db.insert(DBHelper.ACTIVITY_TABLE_NAME, null, cv)
        gpsActivity.id = activityId as Long
    }

    fun addLocation(locationPoint: LocationPoint) {
        val cv = ContentValues()
        cv.put(DBHelper.LOCATION_ACTIVITY_ID, activityId)
        cv.put(DBHelper.LOCATION_RECORDED_AT, locationPoint.time)
        cv.put(DBHelper.LOCATION_LATITUDE, locationPoint.latitude)
        cv.put(DBHelper.LOCATION_LONGITUDE, locationPoint.longitude)
        cv.put(DBHelper.LOCATION_ACCURACY, locationPoint.accuracy)
        cv.put(DBHelper.LOCATION_ALTITUDE, locationPoint.altitude)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            cv.put(DBHelper.LOCATION_VERTICAL_ACCURACY, locationPoint.verticalAccuracyMeters)
        }
        cv.put(DBHelper.LOCATION_SPEED, locationPoint.speed)
        cv.put(DBHelper.LOCATION_TYPE_ID, locationPoint.typeId)
        db.insert(DBHelper.LOCATION_TABLE_NAME, null, cv)
    }

    fun getAll(): List<GPSActivity> {
        val activities = ArrayList<GPSActivity>()
        val locationPoints = ArrayList<LocationPoint?>()

        val cursor = db.query(DBHelper.ACTIVITY_TABLE_NAME, null, null, null, null, null, null)

        while (cursor.moveToNext()) {
            activities.add(
                GPSActivity(
                    cursor.getLong(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getLong(5),
                    cursor.getFloat(6),
                    cursor.getFloat(7),
                    cursor.getDouble(8),
                    cursor.getDouble(9),
                    cursor.getInt(10),
                    cursor.getInt(11),
                    cursor.getString(12),
                    cursor.getString(13)

                )
            )
        }
        cursor.close()

        val cursorA =
            db.query(DBHelper.LOCATION_TABLE_NAME, null, null, null, null, null, null, null)

        while (cursorA.moveToNext()) {
            locationPoints.add(
                LocationPoint(
                        cursorA.getLong(0),
                        cursorA.getLong(1),
                        cursorA.getLong(2),
                        cursorA.getDouble(3),
                        cursorA.getDouble(4),
                        cursorA.getFloat(5),
                        cursorA.getDouble(6),
                        cursorA.getFloat(7),
                        cursorA.getFloat(8),
                        cursorA.getString(9)
                )
            )
        }
        cursorA.close()

        for (i in 0 until locationPoints.size){
            for(j in 0 until activities.size) {
                if (locationPoints[i]!!.activityId == activities[j].id) {
                    activities[j].listOfLocations.add(locationPoints[i])
                }
            }
        }

        if (activities.isNotEmpty()) {
            for(i in 0 until activities.size) {
                activities[i].listId = i + 1
            }
        }

        return activities
    }

    fun get(id: Long): GPSActivity {
        var activity = GPSActivity()
        val locationPoints = ArrayList<LocationPoint?>()

        val cursor = db.query(DBHelper.ACTIVITY_TABLE_NAME, null, DBHelper.ACTIVITY_ID + " = ?",
            arrayOf(id.toString()), null, null, null)

        while (cursor.moveToNext()) {
            activity = GPSActivity(
                    cursor.getLong(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getLong(5),
                    cursor.getFloat(6),
                    cursor.getFloat(7),
                    cursor.getDouble(8),
                    cursor.getDouble(9),
                    cursor.getInt(10),
                    cursor.getInt(11),
                    cursor.getString(12),
                    cursor.getString(13)
            )
        }
        cursor.close()

        val cursorA =
            db.query(DBHelper.LOCATION_TABLE_NAME, null, DBHelper.LOCATION_ACTIVITY_ID + " = ?", arrayOf(id.toString()), null, null, null)

        while (cursorA.moveToNext()) {
            locationPoints.add(
                LocationPoint(
                        cursorA.getLong(0),
                        cursorA.getLong(1),
                        cursorA.getLong(2),
                        cursorA.getDouble(3),
                        cursorA.getDouble(4),
                        cursorA.getFloat(5),
                        cursorA.getDouble(6),
                        cursorA.getFloat(7),
                        cursorA.getFloat(8),
                        cursorA.getString(9)
                )
            )
        }
        cursorA.close()

        activity.listOfLocations = locationPoints
        return activity
    }

    fun update(gpsActivity: GPSActivity) {
        val contentValuesP = ContentValues()
        contentValuesP.put(DBHelper.ACTIVITY_NAME, gpsActivity.name)
        contentValuesP.put(DBHelper.ACTIVITY_DESCRIPTION, gpsActivity.description)
        contentValuesP.put(DBHelper.ACTIVITY_DURATION, gpsActivity.duration)
        contentValuesP.put(DBHelper.ACTIVITY_SPEED, gpsActivity.speed)
        contentValuesP.put(DBHelper.ACTIVITY_SPEED, gpsActivity.distance)
        db.update(
            DBHelper.ACTIVITY_TABLE_NAME,
            contentValuesP,
            DBHelper.ACTIVITY_ID + " = ${gpsActivity.id}",
            null
        )
    }

    fun delete(id: Int) {
        db.delete(DBHelper.ACTIVITY_TABLE_NAME, DBHelper.ACTIVITY_ID + " = " + id, null)
        db.delete(DBHelper.LOCATION_TABLE_NAME, DBHelper.LOCATION_ACTIVITY_ID + " = " + id, null)
    }

}