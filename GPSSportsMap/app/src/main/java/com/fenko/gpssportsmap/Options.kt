package com.fenko.gpssportsmap

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.fenko.gpssportsmap.database.ActivityRepo
import kotlinx.android.synthetic.main.options.*

class Options {
    /*
    user options class
     */

    var activity: String = "00000000-0000-0000-0000-000000000001" //default

    var locationUpdateRate: Long = 2000                          //default

    private lateinit var activityRepo: ActivityRepo                      //activities repository

    @SuppressLint("SetTextI18n")
    fun askOptions(context: Context, mapActivity: MapActivity) {
        //function opens options dialog

        activityRepo = ActivityRepo(context).open()
        val user = activityRepo.getUser()

        val dialog = Dialog(context)

        dialog.setContentView(R.layout.options)
        dialog.show()

        val loginButton = dialog.findViewById<Button>(R.id.buttonOptionsLogIn)

        //if user still not logged in, options dialog shows notification that user not logged in and login button.
        //button opens login pop-up
        if (user.token == "") {
            loginButton.setOnClickListener {
                mapActivity.volley.login(context)
                dialog.dismiss()
            }
        } else {
            //if user logged in, options dialog shows notification "Logged in as: Name LastName abd previous activities button.
            dialog.findViewById<TextView>(R.id.textOptionsLoggedIn).text = context.getString(R.string.optionsLoggedInAs)
            dialog.findViewById<TextView>(R.id.textOptionsLoggedAs).text = "${user.firstName} ${user.lastName}"
            loginButton.text = context.getString(R.string.previousActivities)
            loginButton.setOnClickListener {
                //button opens list of activities
                val viewActivitiesList = Intent(context, ListActivity::class.java)
                context.startActivity(viewActivitiesList)
                dialog.dismiss()
            }
        }

        val saveButton = dialog.findViewById<Button>(R.id.buttonOptionsSave)
        saveButton.setOnClickListener {
            //save button saves chosen options and closes dialog

            //spinner with activity types
            when (dialog.spinnerOptionsSelectActivity.selectedItemPosition) {
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
            //pace range for polylines
            if (dialog.editTextOptionsBadPace.text.toString() != "") {
                mapActivity.badPace = dialog.editTextOptionsBadPace.text.toString().toInt()
            }
            if (dialog.editTextOptionsGoodPace.text.toString() != "") {
                mapActivity.goodPace = dialog.editTextOptionsGoodPace.text.toString().toInt()
            }
            //update rate
            if (dialog.editTextLocationUpdateRate.text.toString() != "") {
                locationUpdateRate = dialog.editTextLocationUpdateRate.text.toString().toLong()
            }

            Toast.makeText(context, "Options Saved", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

    }
}