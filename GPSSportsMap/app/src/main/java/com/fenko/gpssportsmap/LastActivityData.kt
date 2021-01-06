package com.fenko.gpssportsmap

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.fenko.gpssportsmap.database.ActivityRepo
import com.fenko.gpssportsmap.database.DataRecyclerViewAdapter
import com.fenko.gpssportsmap.objects.GPSActivity
import com.fenko.gpssportsmap.tools.Helpers
import com.fenko.gpssportsmap.tools.GPXParser

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_data.*

class LastActivityData : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var activityRepo: ActivityRepo
    private lateinit var adapter: RecyclerView.Adapter<*>
    var volley = Volley()

    lateinit var activity: GPSActivity
    var ui = MapObjects()
    var gpxParser = GPXParser()

    var goodPace: Int = 4
    var badPace: Int = 7


    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data)

        supportActionBar?.hide()
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        activityRepo = ActivityRepo(this).open()

        adapter = DataRecyclerViewAdapter(this, activityRepo)
        (adapter as DataRecyclerViewAdapter).refreshData()

        activity = (adapter as DataRecyclerViewAdapter).dataSet.last()

        volley.volleyUser = activityRepo.getUser()

        editTextActivityName.setText(activity.name)
        textRecordedAtData.text = activity.recordedAt
        textDurationData.text = Helpers().converterHMS(activity.duration)
        textPaceData.text = "%.2f min/km".format(activity.speed)
        textDistanceData.text = "%.2f m". format(activity.distance)
        editTextDescription.setText(activity.description)

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.mapType = ui.mapType
        mMap.uiSettings.isZoomControlsEnabled = ui.isZoomControlsEnabled
        mMap.uiSettings.isZoomGesturesEnabled = ui.isZoomGesturesEnabled

        val locations = activity.listOfLocations
        val camLocation = LatLng(locations.last()!!.latitude, locations.last()!!.longitude)
        println("${locations[0]!!.id}, ${locations[0]!!.activityId}, ${locations[0]!!.typeId}, ${locations[0]!!.latitude}, ${locations[0]!!.longitude}")
        mMap.addMarker(MarkerOptions().icon(BitmapDescriptorFactory
                .fromResource(R.drawable.baseline_outlined_flag_black_24dp)).title("Start")
                .position(LatLng(locations[0]!!.latitude, locations[0]!!.longitude)))

        for(i in 1 until activity.listOfLocations.size) {
            ui.uiUpdate(LatLng(activity.listOfLocations[i - 1]!!.latitude, activity.listOfLocations[i - 1]!!.longitude),
                    LatLng(activity.listOfLocations[i]!!.latitude, activity.listOfLocations[i]!!.longitude),
                    activity.listOfLocations[i]!!.speed, goodPace, badPace, mMap)

            if (activity.listOfLocations[i]!!.typeId == "00000000-0000-0000-0000-000000000003") {
                ui.addCPMarker(activity.listOfLocations[i]!!, mMap)
            }
        }

        mMap.addMarker(MarkerOptions().icon(BitmapDescriptorFactory
                .fromResource(R.drawable.baseline_flag_black_24dp)).title("Start")
                .position(LatLng(activity.listOfLocations.last()!!.latitude, activity.listOfLocations.last()!!.longitude)))

        mMap.animateCamera(CameraUpdateFactory.newLatLng(camLocation))
    }

    fun buttonPreviousActivitiesOnClick(view: View) {
        if (activity.name != editTextActivityName.text.toString() || activity.description != editTextDescription.text.toString()) {
            activity.name = editTextActivityName.text.toString()
            activity.description = editTextDescription.text.toString()

            activityRepo.update(activity)
            volley.putSession(this, activity)
            Toast.makeText(this, "Activity updated", Toast.LENGTH_SHORT).show()

        }
        val viewActivitiesList = Intent(this, ListActivity::class.java)
        startActivity(viewActivitiesList)
        activityRepo.close()
        finish()
    }

    fun buttonExportOnClick(view: View) {
        if (activity.name != editTextActivityName.text.toString() || activity.description != editTextDescription.text.toString()) {
            activity.name = editTextActivityName.text.toString()
            activity.description = editTextDescription.text.toString()

            activityRepo.update(activity)
            volley.putSession(this, activity)
        }
        gpxParser.exportFile(this, activity)
    }

    override fun onDestroy() {
        super.onDestroy()
        activityRepo.close()
    }
}