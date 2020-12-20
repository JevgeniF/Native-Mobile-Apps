package com.fenko.gpssportsmap

import android.app.Dialog
import android.content.Context
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONObject

class Volley {

    var volleyUser: User? = null
    private var url = "https://sportmap.akaver.com"

    fun login(context: Context) {
        val registerPage = "$url/api/v1.0/account/register"
        val loginPage = "$url/api/v1.0/account/login"
        val handler = HttpSingletonHandler.getInstance(context)
        val dialog = Dialog(context)

        dialog.setContentView(R.layout.login_popup)
        dialog.show()
        val buttonLogin = dialog.findViewById(R.id.buttonLogin) as Button
        val buttonNewUser = dialog.findViewById(R.id.buttonNewUser) as Button
        buttonLogin.setOnClickListener {
            val eMail = dialog.findViewById(R.id.editTextTextEmailAddress) as EditText
            val password = dialog.findViewById(R.id.editTextTextPassword) as EditText
            volleyUser = User(eMail.text.toString(), password.text.toString(), "", "")

            val jsonBody = JSONObject()
            jsonBody.put("email", volleyUser!!.eMail)
            jsonBody.put("password", volleyUser!!.password)

            val httpRequest =
                object : JsonObjectRequest(Method.POST, loginPage, jsonBody, { response ->
                    volleyUser?.token = response.getString("token")
                    Toast.makeText(context, "Login Success", Toast.LENGTH_LONG).show()
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
                val eMail = dialog.findViewById(R.id.editTextTextEmailAddress) as EditText
                val password = dialog.findViewById(R.id.editTextTextPassword) as EditText
                val firstName = dialog.findViewById(R.id.editTextTextPersonName) as EditText
                val lastName = dialog.findViewById(R.id.editTextTextPersonName2) as EditText
                volleyUser = User(eMail.text.toString(), password.text.toString(), lastName.text.toString(), firstName.text.toString())

                val jsonBody = JSONObject()
                jsonBody.put("email", volleyUser!!.eMail)
                jsonBody.put("password", volleyUser!!.password)
                jsonBody.put("lastName", volleyUser!!.lastName)
                jsonBody.put("firstName", volleyUser!!.firstName)

                val httpRequest =
                    object : JsonObjectRequest(Method.POST, registerPage, jsonBody, { response ->
                        volleyUser?.token = response.getString("token")
                        Toast.makeText(context, "Register Success", Toast.LENGTH_LONG).show()
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
        }
    }

}