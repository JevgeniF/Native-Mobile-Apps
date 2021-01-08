package com.fenko.gpssportsmap.backend

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.fenko.gpssportsmap.R
import com.fenko.gpssportsmap.database.ActivityRepo
import com.fenko.gpssportsmap.objects.GPSActivity
import com.fenko.gpssportsmap.objects.LocationPoint
import com.fenko.gpssportsmap.objects.User
import org.json.JSONObject


class Volley {
    /*
    Class for communication between app and backend server.
    Provides possibilities for user login/registration.

    Sending, deleting and amending of gps activities.
     */

    var volleyUser: User? = null // Stores data about user after log-on
    lateinit var activityRepo: ActivityRepo // Repo for local database

    private var url = "https://sportmap.akaver.com/api/v1.0"  //start of server url

    fun login(context: Context) {
        /*
            function for user/registration login used in MapsActivity. It creates dialog window
            for login and registration. In case of successful registration, function creates User
            object and saves it in database.
            Function doesn't save password. I decided that in such case the user will be more protected,
            however, I am not sure that it is safe to keep token.
         */
        activityRepo = ActivityRepo(context).open()

        val registerPage = "$url/account/register"
        val loginPage = "$url/account/login"

        val handler = HttpSingletonHandler.getInstance(context)

        val dialog = Dialog(context)

        dialog.setContentView(R.layout.login_popup)
        dialog.show()

        val buttonLogin = dialog.findViewById(R.id.buttonLogin) as Button
        val buttonNewUser = dialog.findViewById(R.id.buttonNewUser) as Button

        buttonLogin.setOnClickListener {
            // on press tries to login user with entered data

            val eMail = dialog.findViewById(R.id.textFieldEMail) as EditText
            val password = dialog.findViewById(R.id.textFieldPassword) as EditText

            //request body
            val jsonBody = JSONObject()
            jsonBody.put("email", eMail.text.toString())
            jsonBody.put("password", password.text.toString())

            //request with application/json string
            val httpRequest =
                    object : JsonObjectRequest(Method.POST, loginPage, jsonBody, { response ->
                        Toast.makeText(context, context.resources.getString(R.string.loggedIn), Toast.LENGTH_SHORT).show()
                        //if logged - user created and added to database
                        volleyUser = User(response.getString("firstName"), response.getString("lastName"), eMail.text.toString(), response.getString("token"))
                        activityRepo.addUser(volleyUser!!)
                        dialog.dismiss()
                    }, { response ->
                        Toast.makeText(context, response.toString(), Toast.LENGTH_LONG).show()
                    }) {
                        override fun getHeaders(): MutableMap<String, String> {
                            val headers = HashMap<String, String>()
                            headers["Content-Type"] = "application/json"
                            return headers
                        }
                    }
            handler.addToRequestQueue(httpRequest)
        }

        buttonNewUser.setOnClickListener {
            //in case of new user, changes dialog to registration form
            dialog.dismiss()

            dialog.setContentView(R.layout.registration_popup)
            dialog.show()

            val buttonRegister = dialog.findViewById(R.id.buttonRegister) as Button
            buttonRegister.setOnClickListener {
                //tries to register user with inserted data
                val eMail = dialog.findViewById(R.id.textFieldEMail) as EditText
                val password = dialog.findViewById(R.id.textFieldPassword) as EditText
                val firstName = dialog.findViewById(R.id.textFieldFirstName) as EditText
                val lastName = dialog.findViewById(R.id.textFieldLastName) as EditText

                //request body
                val jsonBody = JSONObject()
                jsonBody.put("email", eMail.text.toString())
                jsonBody.put("password", password.text.toString())
                jsonBody.put("lastName", lastName.text.toString())
                jsonBody.put("firstName", firstName.text.toString())

                val httpRequest =
                        object : JsonObjectRequest(Method.POST, registerPage, jsonBody, { response ->
                            Toast.makeText(context, context.resources.getString(R.string.registrationDone), Toast.LENGTH_SHORT).show()
                            //in case of success, creates user object and saves to Database
                            volleyUser = User(firstName.text.toString(), lastName.text.toString(), eMail.text.toString(), response.getString("token"))
                            activityRepo.addUser(volleyUser!!)
                            dialog.dismiss()
                        }, { response ->
                            Toast.makeText(context, response.toString(), Toast.LENGTH_LONG).show()
                        }) {
                            override fun getHeaders(): MutableMap<String, String> {
                                val headers = HashMap<String, String>()
                                headers["Content-Type"] = "application/json"
                                return headers
                            }
                        }
                handler.addToRequestQueue(httpRequest)
            }
            val buttonBack = dialog.findViewById(R.id.buttonBack) as Button
            buttonBack.setOnClickListener {
                //starts from log-in dialog again
                dialog.dismiss()
                login(context)
            }
        }
    }

    fun postSession(context: Context, activity: GPSActivity) {
        //function posts session(gps activity to backend server

        val sessionUrl = "$url/GpsSessions"
        val handler = HttpSingletonHandler.getInstance(context)

        //request body
        val jsonBody = JSONObject()
        jsonBody.put("name", activity.name)
        jsonBody.put("description", activity.description)
        jsonBody.put("recordedAt", activity.recordedAt)
        jsonBody.put("paceMin", activity.paceMin)
        jsonBody.put("paceMax", activity.paceMax)
        jsonBody.put("gpsSessionTypeId", activity.gpsSessionTypeId)

        val httpRequest =
                object : JsonObjectRequest(Method.POST, sessionUrl, jsonBody, { response ->
                    //in case of success, writes back to gpsActivity and User objects backend Id's
                    activity.backendId = response.getString("id")
                    activity.userId = response.getString("appUserId")
                    volleyUser!!.backendId = activity.userId

                    Toast.makeText(context, context.resources.getString(R.string.sessionStarted), Toast.LENGTH_SHORT).show()
                }, { response ->
                    Toast.makeText(context, response.toString(), Toast.LENGTH_SHORT).show()
                }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Authorization"] = "Bearer ${volleyUser!!.token}"
                        headers["Content-Type"] = "application/json"
                        return headers
                    }
                }
        handler.addToRequestQueue(httpRequest)
    }

    fun postLU(context: Context, activity: GPSActivity, locationPoint: LocationPoint) {
        //function posts location updates to session created on server
        val locationsUrl = "$url/GpsLocations"
        val handler = HttpSingletonHandler.getInstance(context)

        //request body
        val jsonBody = JSONObject()
        jsonBody.put("latitude", locationPoint.latitude)
        jsonBody.put("longitude", locationPoint.longitude)
        jsonBody.put("accuracy", locationPoint.accuracy)
        jsonBody.put("altitude", locationPoint.altitude)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            jsonBody.put("verticalAccuracy", locationPoint.verticalAccuracyMeters)
        }
        jsonBody.put("AppUserId", volleyUser!!.backendId) //user id for server
        jsonBody.put("gpsSessionId", activity.backendId) // activity id for server
        jsonBody.put("gpsLocationTypeId", "00000000-0000-0000-0000-000000000001") //usual location update id

        val httpRequest =
                object : JsonObjectRequest(Method.POST, locationsUrl, jsonBody, {
                }, { response ->
                    Toast.makeText(context, response.toString(), Toast.LENGTH_LONG).show()
                }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Authorization"] = "Bearer ${volleyUser!!.token}"
                        headers["Content-Type"] = "application/json"
                        return headers
                    }
                }
        handler.addToRequestQueue(httpRequest)
    }

    fun postCP(context: Context, activity: GPSActivity, locationPoint: LocationPoint) {
        //function posts location updates to session created on server
        val locationsUrl = "$url/GpsLocations"
        val handler = HttpSingletonHandler.getInstance(context)

        //request body
        val jsonBody = JSONObject()
        jsonBody.put("latitude", locationPoint.latitude)
        jsonBody.put("longitude", locationPoint.longitude)
        jsonBody.put("accuracy", locationPoint.accuracy)
        jsonBody.put("altitude", locationPoint.altitude)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            jsonBody.put("verticalAccuracy", locationPoint.verticalAccuracyMeters)
        }
        jsonBody.put("AppUserId", volleyUser!!.backendId) //user id for server
        jsonBody.put("gpsSessionId", activity.backendId) // activity id for server
        jsonBody.put("gpsLocationTypeId", "00000000-0000-0000-0000-000000000003") //checkpoint type id for server

        val httpRequest =
                object : JsonObjectRequest(Method.POST, locationsUrl, jsonBody, {
                }, { response ->
                    Toast.makeText(context, response.toString(), Toast.LENGTH_SHORT).show()
                }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Authorization"] = "Bearer ${volleyUser!!.token}"
                        headers["Content-Type"] = "application/json"
                        return headers
                    }
                }
        handler.addToRequestQueue(httpRequest)
    }

    fun putSession(context: Context, activity: GPSActivity) {
        //function updates session on server. It is allowed to change name and description by default in the app
        val updateSessionUrl = "$url/GpsSessions/${activity.backendId}"
        val handler = HttpSingletonHandler.getInstance(context)

        //request body
        val jsonBody = JSONObject()
        jsonBody.put("id", activity.backendId)
        jsonBody.put("name", activity.name)
        jsonBody.put("description", activity.description)
        jsonBody.put("gpsSessionTypeId", activity.gpsSessionTypeId)

        val httpRequest =
                object : JsonObjectRequest(Method.PUT, updateSessionUrl, jsonBody, {
                    Toast.makeText(context, context.resources.getString(R.string.sessionUpdated), Toast.LENGTH_SHORT).show()
                }, {}) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Authorization"] = "Bearer ${volleyUser!!.token}"
                        headers["Content-Type"] = "application/json"
                        return headers
                    }
                }
        handler.addToRequestQueue(httpRequest)
    }

    fun deleteSession(context: Context, activity: GPSActivity) {
        //function deletes session from server
        val deleteUrl = "$url/GpsSessions/${activity.backendId}"
        val handler = HttpSingletonHandler.getInstance(context)

        val httpRequest =
                object : StringRequest(Method.DELETE, deleteUrl, {
                    Toast.makeText(context, context.resources.getString(R.string.sessionDeleted), Toast.LENGTH_SHORT).show()
                }, {}) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Authorization"] = "Bearer ${volleyUser!!.token}"
                        return headers
                    }
                }
        handler.addToRequestQueue(httpRequest)
    }
}