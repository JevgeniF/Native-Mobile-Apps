package com.fenko.gpssportsmap

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fenko.gpssportsmap.backend.Volley
import com.fenko.gpssportsmap.database.ActivityRepo
import com.fenko.gpssportsmap.objects.GPSActivity
import com.fenko.gpssportsmap.tools.GPXParser
import com.fenko.gpssportsmap.tools.Helpers
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_data.*

class ListActivityData : AppCompatActivity(), OnMapReadyCallback {
    /*
    Activity Class. Shows chosen from list of activities gpsActivity data and track on map fragment.
    gpsActivity name and description can be edited in activity.
     */

    private lateinit var activityRepo: ActivityRepo             //activities repository
    var volley = Volley()                                       //volley for backend communications
    private var id: Long? = null                                //id of activity chosen from list

    lateinit var activity: GPSActivity                          //GPSActivity object
    private var camLocation: LatLng? = null                 //location for camera
    private var mapObjects = MapObjects()                       //class for drawing on map
    private var gpxParser = GPXParser()                         //class for sharing GPX file

    private var goodPace: Int = 0                               //pace range chosen by user for this activity from databse
    private var badPace: Int = 0


    private lateinit var mMap: GoogleMap                //map fragment

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        id = intent.getStringExtra("id")!!.toLong()

        activityRepo = ActivityRepo(this).open()
        //getting gps activity and user from database
        activity = activityRepo.get(id!!)
        volley.volleyUser = activityRepo.getUser()

        //filling this app activity and it's layout with data
        goodPace = activity.goodPace
        badPace = activity.badPace
        camLocation = LatLng(activity.listOfLocations.last()!!.latitude, activity.listOfLocations.last()!!.longitude)

        editTextActivityName.setText(activity.name)
        textRecordedAtData.text = activity.recordedAt
        textDurationData.text = Helpers().converterHMS(activity.duration)
        textPaceData.text = "%.2f min/km".format(activity.speed)
        textDistanceData.text = "%.2f m".format(activity.distance)
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

        mMap.uiSettings.isZoomControlsEnabled = mapObjects.isZoomControlsEnabled
        mMap.uiSettings.isZoomGesturesEnabled = mapObjects.isZoomGesturesEnabled

        //drawing activity on map
        val locations = activity.listOfLocations
        mapObjects.addStart(locations[0], mMap)

        for (i in 1 until locations.size) {
            mapObjects.uiUpdate(LatLng(locations[i - 1]!!.latitude, locations[i - 1]!!.longitude),
                    LatLng(locations[i]!!.latitude, locations[i]!!.longitude),
                    locations[i]!!.speed, goodPace, badPace, mMap)

            if (locations[i]!!.typeId == "00000000-0000-0000-0000-000000000003") {
                mapObjects.addCPMarker(locations[i]!!, mMap)
            }
        }
        mapObjects.addFinish(locations.last(), mMap)

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(camLocation, mapObjects.defaultZoom))
    }

    fun buttonPreviousActivitiesOnClick(view: View) {
        //function to get to list of activities on "Previous Activities" click.
        //if name or description changed, it will be saved in database and on server automatically
        if (activity.name != editTextActivityName.text.toString() || activity.description != editTextDescription.text.toString()) {

            activity.name = editTextActivityName.text.toString()
            activity.description = editTextDescription.text.toString()

            //saving process
            activityRepo.update(activity)
            volley.putSession(this, activity)
            Toast.makeText(this, "Activity updated", Toast.LENGTH_SHORT).show()
        }
        //starting new activity, closing this one... passing to list of gps activities.
        val viewActivitiesList = Intent(this, ListActivity::class.java)
        activityRepo.close()
        startActivity(viewActivitiesList)
        finish()
    }

    fun buttonExportOnClick(view: View) {
        //function to share GPX file on "Share GPX" click.
        //if name or description changed, it will be saved in database and on server automatically
        //just to be sure that every change won't be lost if user will switch off app after that
        if (activity.name != editTextActivityName.text.toString() || activity.description != editTextDescription.text.toString()) {

            activity.name = editTextActivityName.text.toString()
            activity.description = editTextDescription.text.toString()

            //saving process
            activityRepo.update(activity)
            volley.putSession(this, activity)
        }
        //file sharing
        gpxParser.exportFile(this, activity)
    }

    override fun onDestroy() {
        super.onDestroy()
        activityRepo.close()
    }

    override fun onStop() {
        activityRepo.close()
        super.onStop()
    }
}