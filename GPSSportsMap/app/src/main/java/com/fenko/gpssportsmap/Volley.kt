package com.fenko.gpssportsmap

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import org.json.JSONObject
import java.time.LocalDateTime
import kotlin.math.floor

class Volley {

    var volleyUser: User? = null
    private var url = "https://sportmap.akaver.com/api/v1.0"

    fun login(context: Context) {
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
            volleyUser = User(eMail.text.toString(), password.text.toString(), "", "")

            val jsonBody = JSONObject()
            jsonBody.put("email", volleyUser!!.eMail)
            jsonBody.put("password", volleyUser!!.password)

            val httpRequest =
                object : JsonObjectRequest(Method.POST, loginPage, jsonBody, { response ->
                    volleyUser?.token = response.getString("token")
                    Toast.makeText(context, "Login Successful", Toast.LENGTH_LONG).show()
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
                volleyUser = User(eMail.text.toString(), password.text.toString(), lastName.text.toString(), firstName.text.toString())

                val jsonBody = JSONObject()
                jsonBody.put("email", volleyUser!!.eMail)
                jsonBody.put("password", volleyUser!!.password)
                jsonBody.put("lastName", volleyUser!!.lastName)
                jsonBody.put("firstName", volleyUser!!.firstName)

                val httpRequest =
                    object : JsonObjectRequest(Method.POST, registerPage, jsonBody, { response ->
                        volleyUser?.token = response.getString("token")
                        Toast.makeText(context, "Register Successful", Toast.LENGTH_LONG).show()
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
    fun postSession(context: Context, activity: Activity) {
        val sessionUrl = "$url/GpsSessions"
        val handler = HttpSingletonHandler.getInstance(context)
        val jsonBody = JSONObject()
        val time = LocalDateTime.now()
        jsonBody.put("name", time)
        jsonBody.put("description", time)
        jsonBody.put("recordedAt", time)
        jsonBody.put("paceMin", 60)
        jsonBody.put("paceMax", 1000)

        val httpRequest =
                object : JsonObjectRequest(Method.POST, sessionUrl, jsonBody, { response ->
                    activity.sessionId = response.getString("id")
                    volleyUser!!.userId = response.getString("appUserId")

                    println(response.getString("id"))
                    println(response.getString("appUserId"))
                    Toast.makeText(context, "Session id:${activity.sessionId}", Toast.LENGTH_LONG).show()
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

    fun getSession(context: Context, id: String, activity: Activity) {
        val sessionUrl = "$url/GpsSessions/$id"

        val handler = HttpSingletonHandler.getInstance(context)

        val httpRequest =
                object : StringRequest(Method.GET, sessionUrl, Response.Listener { response ->
                    val jsonObject = JSONObject(response)
                    activity.totalDistance = floor(jsonObject.getDouble("distance"))
                    activity.duration = jsonObject.getDouble("duration")
                }, Response.ErrorListener { response ->
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
    fun postLU(context: Context, activity: Activity){
        val locationsUrl = "$url/GpsLocations"
        val handler = HttpSingletonHandler.getInstance(context)
        val jsonBody = JSONObject()
        val time = LocalDateTime.now()

        jsonBody.put("recordedAt", time)
        jsonBody.put("latitude", activity.currentLocation!!.latitude)
        jsonBody.put("longitude", activity.currentLocation!!.longitude)
        jsonBody.put("accuracy", activity.currentLocation!!.accuracy)
        jsonBody.put("altitude", activity.currentLocation!!.altitude)
        jsonBody.put("verticalAccuracy", activity.currentLocation!!.verticalAccuracyMeters)
        jsonBody.put("AppUserId", volleyUser!!.userId)
        jsonBody.put("gpsSessionId", activity.sessionId)
        jsonBody.put("gpsLocationTypeId","00000000-0000-0000-0000-000000000001")

        val httpRequest =
                object : JsonObjectRequest(Method.POST, locationsUrl, jsonBody, { response ->
                    println(response.getString("id"))
                    //Toast.makeText(context, "CPoint ID:${response.getString("id")}", Toast.LENGTH_LONG).show()
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
    fun postCP(context: Context, activity: Activity){
        val locationsUrl = "$url/GpsLocations"
        val handler = HttpSingletonHandler.getInstance(context)
        val jsonBody = JSONObject()
        val time = LocalDateTime.now()

        jsonBody.put("recordedAt", time)
        jsonBody.put("latitude", activity.checkPointLocation!!.latitude)
        jsonBody.put("longitude", activity.checkPointLocation!!.longitude)
        jsonBody.put("accuracy", activity.checkPointLocation!!.accuracy)
        jsonBody.put("altitude", activity.checkPointLocation!!.altitude)
        jsonBody.put("verticalAccuracy", activity.checkPointLocation!!.verticalAccuracyMeters)
        jsonBody.put("AppUserId", volleyUser!!.userId)
        jsonBody.put("gpsSessionId", activity.sessionId)
        jsonBody.put("gpsLocationTypeId","00000000-0000-0000-0000-000000000003")

        val httpRequest =
                object : JsonObjectRequest(Method.POST, locationsUrl, jsonBody, { response ->
                    println(response.getString("id"))
                    Toast.makeText(context, "CPoint ID:${response.getString("id")}", Toast.LENGTH_LONG).show()
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
    fun postWP(context: Context, activity: Activity){
        val locationsUrl = "$url/GpsLocations"
        val handler = HttpSingletonHandler.getInstance(context)
        val jsonBody = JSONObject()
        val time = LocalDateTime.now()

        jsonBody.put("recordedAt", time)
        jsonBody.put("latitude", activity.wayPointLatLng!!.latitude)
        jsonBody.put("longitude", activity.wayPointLatLng!!.longitude)
        jsonBody.put("AppUserId", volleyUser!!.userId)
        jsonBody.put("gpsSessionId", activity.sessionId)
        jsonBody.put("gpsLocationTypeId","00000000-0000-0000-0000-000000000002")

        val httpRequest =
                object : JsonObjectRequest(Method.POST, locationsUrl, jsonBody, { response ->
                    println(response.getString("id"))
                    activity.wayPointId = response.getString("id")
                    Toast.makeText(context, "CPoint ID:${response.getString("id")}", Toast.LENGTH_LONG).show()
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

    fun deletePoint(id: String, context: Context) {
        val locationUrl = "$url/GpsLocations/$id"
        val handler = HttpSingletonHandler.getInstance(context)

        val httpRequest = object: StringRequest(Method.DELETE, locationUrl, {
            Toast.makeText(context, "Point: $id - DELETED", Toast.LENGTH_LONG).show()
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
}