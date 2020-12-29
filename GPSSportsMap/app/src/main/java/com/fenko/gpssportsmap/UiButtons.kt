package com.fenko.gpssportsmap

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Build
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

class UiButtons {

    /*var startButtonText = "Start"

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    fun startButton(activity: Activity, view: View, context: Context, googleMap: GoogleMap, volley: Volley){
        if (!activity.started && !activity.paused) {
            activity.completed = false
            activity.started = true

            activity.start = googleMap.addMarker(MarkerOptions().icon(BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_RED)).title("Start").position(activity.currentLatLng as LatLng))
            activity.startLocation = activity.currentLocation

            if(volley.volleyUser != null){
                volley.postSession(context, activity)
            }

            (view as Button).text = "Pause"
            startButtonText = "Pause"
            Toast.makeText(context, "Activity Started", Toast.LENGTH_SHORT).show()
            return
        } else if (activity.started && !activity.paused) {
            activity.paused = true
            activity.started = true
            (view as Button).text = "Finish"
            startButtonText = "Finish"
            Toast.makeText(context, "Activity Paused", Toast.LENGTH_SHORT).show()
            return
        } else if (activity.paused) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Finish Activity")
            builder.setMessage("Do you want to finish or to continue activity?")
            builder.setPositiveButton("Continue") { _: DialogInterface, _: Int ->
                activity.started = true
                activity.paused = false
                (view as Button).text = "Pause"
                startButtonText = "Pause"
            }
            builder.setNegativeButton("Finish") { _: DialogInterface, _: Int ->
                activity.started = false
                activity.paused = false
                activity.completed = true
                (view as Button).text = "Start"
                startButtonText = "Start"
            }
            val dialog = builder.create()
            dialog.show()
        }
    }

     */

}