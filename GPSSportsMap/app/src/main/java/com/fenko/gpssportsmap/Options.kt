package com.fenko.gpssportsmap

import android.app.Dialog
import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.TextView

class Options {



    fun askOptions(context: Context, mapActivity: MapActivity) {

        val dialog = Dialog(context)

        dialog.setContentView(R.layout.options)
        dialog.show()

        var loginButton = dialog.findViewById<Button>(R.id.buttonOptionsLogIn)

        loginButton.setOnClickListener {
            //mapActivity.volley.login(context)
        }

        //if (mapActivity.volley.volleyUser != null && mapActivity.volley.volleyUser!!.token != "") {
          //  dialog.findViewById<TextView>(R.id.textOptionsLoggedIn).text = "Logged in as:"
            //dialog.findViewById<TextView>(R.id.textOptionsLoggedAs).text = mapActivity.volley.volleyUser!!.eMail
           // loginButton.visibility = View.GONE
        //}

    }
}