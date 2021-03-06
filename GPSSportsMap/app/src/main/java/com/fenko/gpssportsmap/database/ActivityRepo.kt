package com.fenko.gpssportsmap.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.fenko.gpssportsmap.objects.GPSActivity
import com.fenko.gpssportsmap.objects.LocationPoint
import com.fenko.gpssportsmap.objects.User

class ActivityRepo(private val context: Context) {
    /*
    Class for communication between database and application.
    Used for write to / read from database
     */

    private lateinit var dbHelper: DBHelper
    private lateinit var db: SQLiteDatabase
    private var activityId: Long? = null //workaround to avoid PRAGMA. inserted into locations as activity ID

    fun open(): ActivityRepo {
        // function opens database for read/write
        dbHelper = DBHelper(context)
        db = dbHelper.writableDatabase

        return this
    }

    fun close() {
        //function closes database
        dbHelper.close()
    }

    fun addUser(user: User) {
        //function adds User object to USER table and returns id to User object
        val cv = ContentValues()
        cv.put(DBHelper.USER_BACKEND_ID, user.backendId)
        cv.put(DBHelper.USER_NAME, user.firstName)
        cv.put(DBHelper.USER_LASTNAME, user.lastName)
        cv.put(DBHelper.USER_EMAIL, user.eMail)
        cv.put(DBHelper.USER_TOKEN, user.token)
        user.id = db.insert(DBHelper.USER_TABLE_NAME, null, cv)
    }

    fun addActivity(gpsActivity: GPSActivity) {
        //function adds GPSActivity object to ACTIVITY table and returns id to GPSActivity object
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
        cv.put(DBHelper.ACTIVITY_BAD_PACE, gpsActivity.badPace)
        cv.put(DBHelper.ACTIVITY_GOOD_PACE, gpsActivity.goodPace)
        activityId = db.insert(DBHelper.ACTIVITY_TABLE_NAME, null, cv)
        gpsActivity.id = activityId as Long
    }

    fun addLocation(locationPoint: LocationPoint) {
        //function adds LocationPoint object to LOCATIONS table and returns id to LocationPoint object
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
        locationPoint.id = db.insert(DBHelper.LOCATION_TABLE_NAME, null, cv)
    }

    fun getUser(): User {
        //function returns User object from USER table
        val user = User()
        val cursor = db.query(DBHelper.USER_TABLE_NAME, null, null, null, null, null, null)

        while (cursor.moveToNext()) {
            user.id = cursor.getLong(0)
            user.backendId = cursor.getString(1)
            user.firstName = cursor.getString(2)
            user.lastName = cursor.getString(3)
            user.eMail = cursor.getString(4)
            user.token = cursor.getString(5)
        }
        cursor.close()
        return user
    }

    fun getAll(): List<GPSActivity> {
        //function returns list of GPSActivity objects  with List of correspondent Location Objects
        //from ACTIVITY and LOCATIONS tables
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
                            cursor.getString(13),
                            cursor.getInt(14),
                            cursor.getInt(15)
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

        for (i in 0 until locationPoints.size) {
            for (j in 0 until activities.size) {
                if (locationPoints[i]!!.activityId == activities[j].id) {
                    activities[j].listOfLocations.add(locationPoints[i])
                }
            }
        }

        return activities
    }

    fun get(id: Long): GPSActivity {
        //function returns GPSActivity object with requested ID and correspondent list of LocationPoint
        // objects from ACTIVITY and LOCATIONS tables
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
                    cursor.getString(13),
                    cursor.getInt(14),
                    cursor.getInt(15)
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

    fun getLast(): GPSActivity {
        //function returns last written GPSActivity object and correspondent list of LocationPoint
        // objects from ACTIVITY and LOCATIONS tables
        var activity = GPSActivity()
        val locationPoints = ArrayList<LocationPoint?>()

        val cursor = db.query(DBHelper.ACTIVITY_TABLE_NAME, null, DBHelper.ACTIVITY_ID +
                " = (SELECT MAX(" +
                DBHelper.ACTIVITY_ID +
                ") FROM " +
                DBHelper.ACTIVITY_TABLE_NAME +
                ")",
                null, null, null, null)

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
                    cursor.getString(13),
                    cursor.getInt(14),
                    cursor.getInt(15)
            )
        }
        cursor.close()

        val cursorA =
                db.query(DBHelper.LOCATION_TABLE_NAME, null, DBHelper.LOCATION_ACTIVITY_ID + " = ?", arrayOf(activity.id.toString()), null, null, null)

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
        //function updates GPSActivity object in ACTIVITY table
        val cv = ContentValues()
        cv.put(DBHelper.ACTIVITY_BACKEND_ID, gpsActivity.backendId)
        cv.put(DBHelper.ACTIVITY_NAME, gpsActivity.name)
        cv.put(DBHelper.ACTIVITY_DESCRIPTION, gpsActivity.description)
        cv.put(DBHelper.ACTIVITY_DURATION, gpsActivity.duration)
        cv.put(DBHelper.ACTIVITY_SPEED, gpsActivity.speed)
        cv.put(DBHelper.ACTIVITY_DISTANCE, gpsActivity.distance)
        cv.put(DBHelper.ACTIVITY_USER_ID, gpsActivity.userId)

        db.update(
                DBHelper.ACTIVITY_TABLE_NAME,
                cv,
                DBHelper.ACTIVITY_ID + " = ${gpsActivity.id}",
                null
        )
    }

    fun updateUser(user: User) {
        //function updates User object in USER table
        val cv = ContentValues()
        cv.put(DBHelper.USER_BACKEND_ID, user.backendId)
        db.update(DBHelper.USER_TABLE_NAME, cv, DBHelper.USER_ID + " = ${user.id}", null)
    }

    fun delete(id: Int) {
        //function removes GPSActivity object with requested ID and correspondent list of LocationPoint
        // objects from ACTIVITY and LOCATIONS tables
        db.delete(DBHelper.ACTIVITY_TABLE_NAME, DBHelper.ACTIVITY_ID + " = " + id, null)
        db.delete(DBHelper.LOCATION_TABLE_NAME, DBHelper.LOCATION_ACTIVITY_ID + " = " + id, null)
    }

}