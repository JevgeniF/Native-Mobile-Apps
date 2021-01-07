package com.fenko.gpssportsmap.tools

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import com.fenko.gpssportsmap.R
import com.fenko.gpssportsmap.objects.GPSActivity
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException

class GPXParser {
    /*
    Class used for creation and sharing of GPX file for gps activity.
    Writes temporary file in cache directory and provides possibility to share via shareIntent option.
    Idea is to substitute one way sharing via e-mail and give more options to user.
     */

    fun exportFile(context: Context, gpsActivity: GPSActivity) {
        //function generates and writes GPX file into cache directory, when gives option to share it

        //creating cache directory
        val cachePath = File(context.cacheDir, "files")
        cachePath.mkdirs()

        //creating file. used the same name, to keep storage clean :)
        val gpxFile = File(cachePath, "activity.gpx")
        //recording data into file part by part
        try {
            val gpxWriter = FileWriter(gpxFile)
            val out = BufferedWriter(gpxWriter)

            //writing header
            out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<gpx creator=\"GPS Sports Map\" version=\"0.1a\"\n" +  //name of creator app
                    "xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/11.xsd\"\n" +
                    "xmlns=\"http://www.topografix.com/GPX/1/1\"\n" +
                    "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                    "<metadata>\n" +
                    "<text>GPS Sport Map</text>\n" +                    //name of app again
                    "<time>${gpsActivity.recordedAt}</time>\n" +        //time activity recorded at
                    "</metadata>\n" +
                    "<trk>\n" +
                    "<name>${gpsActivity.name}</name>\n")               //activity name

            /*
            as type of activity depends on session type id, we use "when" operator
            we can not use description inserted by default in activity, despite the fact that it is the
            same, because it could be already changed by user
             */
            when (gpsActivity.gpsSessionTypeId) {
                "00000000-0000-0000-0000-000000000001" -> {
                    out.write("<type>Running - easy</type>\n")
                }
                "00000000-0000-0000-0000-000000000002" -> {
                    out.write("<type>Running</type>\n")
                }
                "00000000-0000-0000-0000-000000000003" -> {
                    out.write("<type>Orienteering - easy</type>\n")
                }
                "00000000-0000-0000-0000-000000000004" -> {
                    out.write("<type>Orienteering - competition</type>\n")
                }
                "00000000-0000-0000-0000-000000000005" -> {
                    out.write("<type>Bicycle - easy</type>\n")
                }
                "00000000-0000-0000-0000-000000000006" -> {
                    out.write("<type>Bicycle - competition</type>\n")
                }

            }
            //description
            out.write("<desc>${gpsActivity.description}</desc>\n" +
                    "<trkseg>\n")
            //data for each location point in this activity
            for (i in gpsActivity.listOfLocations.indices) {
                out.write("<trkpt lat=\"${gpsActivity.listOfLocations[i]!!.latitude}\" lon=\"${gpsActivity.listOfLocations[i]!!.longitude}\">\n" +
                        "<ele>${gpsActivity.listOfLocations[i]!!.altitude}</ele>\n")

                val time = Helpers().converterTime(gpsActivity.listOfLocations[i]!!.time) //converted from milliseconds point creation time

                out.write("<time>$time</time>\n" +
                        "<speed>${gpsActivity.listOfLocations[i]!!.speed}</speed>\n")

                //type of location point based on typeId
                if (gpsActivity.listOfLocations[i]!!.typeId == "00000000-0000-0000-0000-000000000003") {
                    out.write("<desc>Checkpoint</desc>\n" +
                            "</trkpt>\n")
                } else {
                    out.write("<desc>Location update</desc>\n" +
                            "</trkpt>\n")
                }
            }
            out.write("</trkseg>\n" +
                    "</trk>\n" +
                    "</gpx>")
            out.close()
        } catch (e: IOException) {
            Log.e("Can't write file", e.message!!)
        }

        //creating uri for file and sharing it via shareIntent
        val filePath = File(context.cacheDir, "files")
        val file = File(filePath, "activity.gpx")
        val contentUri = FileProvider.getUriForFile(context, "com.fenko.gpssportsmap.fileprovider", file)
        if (contentUri != null) {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            shareIntent.setDataAndType(contentUri, "text/xml")
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
            startActivity(context, Intent.createChooser(shareIntent, context.getString(R.string.chooser)), null)
        }
    }


}