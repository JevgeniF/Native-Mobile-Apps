package com.fenko.gpssportsmap

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.fenko.gpssportsmap.database.ActivityRepo
import kotlinx.android.synthetic.main.options.*

class Options {

    var activity: String = "00000000-0000-0000-0000-000000000001"

    var locationUpdateRate: Long = 2000

    lateinit var activityRepo: ActivityRepo

    fun askOptions(context: Context, mapActivity: MapActivity) {

        activityRepo = ActivityRepo(context).open()
        var user = activityRepo.getUser()

        val dialog = Dialog(context)

        dialog.setContentView(R.layout.options)
        dialog.show()

        var loginButton = dialog.findViewById<Button>(R.id.buttonOptionsLogIn)

        if (user.token == "") {
            loginButton.setOnClickListener {
                mapActivity.volley.login(context)
                dialog.dismiss()
            }
        } else {
          dialog.findViewById<TextView>(R.id.textOptionsLoggedIn).text = "Logged in as:"
            dialog.findViewById<TextView>(R.id.textOptionsLoggedAs).text = "${user.firstName} ${user.lastName}"
            loginButton.text = "Previous Activities"
            loginButton.setOnClickListener {
                val viewActivitiesList = Intent(context, ListActivity::class.java)
                context.startActivity(viewActivitiesList)
                dialog.dismiss()
            }
        }

        var saveButton = dialog.findViewById<Button>(R.id.buttonOptionsSave)
        saveButton.setOnClickListener {
            var activityType = dialog.spinnerOptionsSelectActivity.selectedItemPosition

            when (activityType) {
                0 -> {
                    activity = "00000000-0000-0000-0000-000000000001"
                }
                1 -> {
                    activity = "00000000-0000-0000-0000-000000000002"
                }
                2 -> {
                    activity = "00000000-0000-0000-0000-000000000003"
                }
                3 -> {
                    activity = "00000000-0000-0000-0000-000000000004"
                }
                4 -> {
                    activity = "00000000-0000-0000-0000-000000000005"
                }
                5 -> {
                    activity = "00000000-0000-0000-0000-000000000006"
                }
            }
            if (dialog.editTextOptionsBadPace.text.toString() != "") {
                mapActivity.badPace = dialog.editTextOptionsBadPace.text.toString().toInt()
            }
            if (dialog.editTextOptionsGoodPace.text.toString() != "") {
                mapActivity.goodPace = dialog.editTextOptionsGoodPace.text.toString().toInt()
            }
            if (dialog.editTextLocationUpdateRate.text.toString() != "") {
                locationUpdateRate = dialog.editTextLocationUpdateRate.text.toString().toLong()
            }

            Toast.makeText(context, "Options Saved", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

    }
}