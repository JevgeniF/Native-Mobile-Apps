package com.fenko.gpssportsmap

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi

import com.android.volley.VolleyError
import com.android.volley.VolleyLog
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.fenko.gpssportsmap.database.ActivityRepo
import com.fenko.gpssportsmap.objects.GPSActivity
import com.fenko.gpssportsmap.objects.LocationPoint
import com.fenko.gpssportsmap.objects.User
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException


class Volley {


    var volleyUser: User? = null
    lateinit var activityRepo: ActivityRepo

    private var url = "https://sportmap.akaver.com/api/v1.0"

    fun login(context: Context) {
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
            val eMail = dialog.findViewById(R.id.textFieldeMail) as EditText
            val password = dialog.findViewById(R.id.textFieldPassword) as EditText

            val jsonBody = JSONObject()
            jsonBody.put("email", eMail.text.toString())
            jsonBody.put("password", password.text.toString())

            val httpRequest =
                object : JsonObjectRequest(Method.POST, loginPage, jsonBody, { response ->
                    Toast.makeText(context, "Login Successful", Toast.LENGTH_LONG).show()
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
            dialog.dismiss()
            dialog.setContentView(R.layout.registration_popup)
            dialog.show()
            val buttonRegister = dialog.findViewById(R.id.buttonRegister) as Button
            buttonRegister.setOnClickListener {
                val eMail = dialog.findViewById(R.id.textFieldeMail) as EditText
                val password = dialog.findViewById(R.id.textFieldPassword) as EditText
                val firstName = dialog.findViewById(R.id.textFieldFirstName) as EditText
                val lastName = dialog.findViewById(R.id.textFieldLastName) as EditText

                val jsonBody = JSONObject()
                jsonBody.put("email", eMail.text.toString())
                jsonBody.put("password", password.text.toString())
                jsonBody.put("lastName", lastName.text.toString())
                jsonBody.put("firstName", firstName.text.toString())

                val httpRequest =
                    object : JsonObjectRequest(Method.POST, registerPage, jsonBody, { response ->
                        Toast.makeText(context, "Register Successful", Toast.LENGTH_LONG).show()
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
            buttonBack.setOnClickListener{
                dialog.dismiss()
                login(context)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun postSession(context: Context, activity: GPSActivity) {
        val sessionUrl = "$url/GpsSessions"
        val handler = HttpSingletonHandler.getInstance(context)
        val jsonBody = JSONObject()
        jsonBody.put("name", activity.name)
        jsonBody.put("description", activity.description)
        jsonBody.put("recordedAt", activity.recordedAt)
        jsonBody.put("paceMin", activity.paceMin)
        jsonBody.put("paceMax", activity.paceMax)
        jsonBody.put("gpsSessionTypeId", activity.gpsSessionTypeId)

        val httpRequest =
                object : JsonObjectRequest(Method.POST, sessionUrl, jsonBody, { response ->
                    activity.backendId = response.getString("id")
                    activity.userId = response.getString("appUserId")
                    volleyUser!!.backendId = activity.userId

                    Toast.makeText(context, "Session id:${activity.backendId}", Toast.LENGTH_LONG).show()
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun postLU(context: Context, activity: GPSActivity, locationPoint: LocationPoint){
        val locationsUrl = "$url/GpsLocations"
        val handler = HttpSingletonHandler.getInstance(context)
        val jsonBody = JSONObject()

        jsonBody.put("latitude", locationPoint.latitude)
        jsonBody.put("longitude", locationPoint.longitude)
        jsonBody.put("accuracy", locationPoint.accuracy)
        jsonBody.put("altitude", locationPoint.altitude)
        jsonBody.put("verticalAccuracy", locationPoint.verticalAccuracyMeters)
        jsonBody.put("AppUserId", volleyUser!!.backendId)
        jsonBody.put("gpsSessionId", activity.backendId)
        jsonBody.put("gpsLocationTypeId", "00000000-0000-0000-0000-000000000001")

        val httpRequest =
                object : JsonObjectRequest(Method.POST, locationsUrl, jsonBody, { response ->
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun postCP(context: Context, activity: GPSActivity, locationPoint: LocationPoint){
        val locationsUrl = "$url/GpsLocations"
        val handler = HttpSingletonHandler.getInstance(context)
        val jsonBody = JSONObject()

        //jsonBody.put("recordedAt", time)
        jsonBody.put("latitude", locationPoint.latitude)
        jsonBody.put("longitude", locationPoint.longitude)
        jsonBody.put("accuracy", locationPoint.accuracy)
        jsonBody.put("altitude", locationPoint.altitude)
        jsonBody.put("verticalAccuracy", locationPoint.verticalAccuracyMeters)
        jsonBody.put("AppUserId", volleyUser!!.backendId)
        jsonBody.put("gpsSessionId", activity.backendId)
        jsonBody.put("gpsLocationTypeId", "00000000-0000-0000-0000-000000000003")

        val httpRequest =
                object : JsonObjectRequest(Method.POST, locationsUrl, jsonBody, { response ->
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

    fun putSession(context: Context, activity: GPSActivity) {
        val updateSessionUrl = "$url/GpsSessions/${activity.backendId}"
        val handler = HttpSingletonHandler.getInstance(context)
        println("${activity.backendId}")
        val jsonBody = JSONObject()
        jsonBody.put("id", activity.backendId)
        jsonBody.put("name", activity.name)
        jsonBody.put("description", activity.description)
        jsonBody.put("gpsSessionTypeId", activity.gpsSessionTypeId)

        val httpRequest =
            object : JsonObjectRequest(Method.PUT, updateSessionUrl, jsonBody, { response ->
                Toast.makeText(context, "Session Updated", Toast.LENGTH_LONG).show()
            }, { response ->
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

    fun deleteSession(context: Context, activity: GPSActivity) {
        val deleteUrl = "$url/GpsSessions/${activity.backendId}"
        val handler = HttpSingletonHandler.getInstance(context)

        val httpRequest =
                object : StringRequest(Method.DELETE, deleteUrl, { response ->
                    Toast.makeText(context, "Session Deleted", Toast.LENGTH_LONG).show()
                }, { response ->
                }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Authorization"] = "Bearer ${volleyUser!!.token}"
                        return headers
                    }
                }
        handler.addToRequestQueue(httpRequest)
    }
}