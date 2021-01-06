package com.fenko.gpssportsmap.tools

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import com.fenko.gpssportsmap.objects.GPSActivity
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException

class GPXParser {

    fun exportFile(context: Context, gpsActivity: GPSActivity) {
        var cachePath = File(context.cacheDir, "files")
        cachePath.mkdirs()
        println(cachePath)

        var gpxFile = File(cachePath, "activity.gpx")
        try {
            var gpxWriter = FileWriter(gpxFile)
            var out = BufferedWriter(gpxWriter)

            out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<gpx creator=\"GPS Sports Map\" version=\"0.1a\"\n" +
                    "xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/11.xsd\"\n" +
                    "xmlns=\"http://www.topografix.com/GPX/1/1\"\n" +
                    "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                    "<metadata>\n" +
                    "<text>GPS Sport Map</text>\n" +
                    "<time>${gpsActivity.recordedAt}</time>\n" +
                    "</metadata>\n" +
                    "<trk>\n" +
                    "<name>${gpsActivity.name}</name>\n")
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
            out.write("<desc>${gpsActivity.description}</desc>\n" +
                    "<trkseg>\n")
            for (i in gpsActivity.listOfLocations.indices) {
                out.write("<trkpt lat=\"${gpsActivity.listOfLocations[i]!!.latitude}\" lon=\"${gpsActivity.listOfLocations[i]!!.longitude}\">\n" +
                        "<ele>${gpsActivity.listOfLocations[i]!!.altitude}</ele>\n")

                var time = Helpers().converterTime(gpsActivity.listOfLocations[i]!!.time)

                out.write("<time>$time</time>\n" +
                        "<speed>${gpsActivity.listOfLocations[i]!!.speed}</speed>\n")
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

        var filePath = File(context.cacheDir, "files")
        var file = File(filePath, "activity.gpx")
        var contentUri = FileProvider.getUriForFile(context, "com.fenko.gpssportsmap.fileprovider", file)
        if (contentUri != null) {
            var shareIntent = Intent()
            shareIntent.setAction(Intent.ACTION_SEND)
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            shareIntent.setDataAndType(contentUri, "text/xml")
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
            startActivity(context, Intent.createChooser(shareIntent, "Choose an app"), null)
        }
    }


}