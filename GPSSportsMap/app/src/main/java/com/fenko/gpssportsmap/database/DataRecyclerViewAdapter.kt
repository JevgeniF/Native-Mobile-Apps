package com.fenko.gpssportsmap.database

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.fenko.gpssportsmap.ListActivityData
import com.fenko.gpssportsmap.R
import com.fenko.gpssportsmap.backend.Volley
import com.fenko.gpssportsmap.objects.GPSActivity
import com.fenko.gpssportsmap.tools.Helpers
import kotlinx.android.synthetic.main.row_view.view.*


class DataRecyclerViewAdapter(context: Context, private val activityRepo: ActivityRepo) :
        RecyclerView.Adapter<DataRecyclerViewAdapter.ViewHolder>() {
    /*
    Class adapts data from database to RecyclerView and generates RowViews for every database entry
    Used for User interaction with recorded into Database activities.
     */

    private lateinit var dataSet: List<GPSActivity> //set of all activities from database

    fun refreshData() {
        //function updates set of all activities
        dataSet = activityRepo.getAll()
    }

    init {
        refreshData()
    }

    private val inflater = LayoutInflater.from(context) // inflater for row views
    private val openActivity = Intent(context, ListActivityData::class.java) //intent used to open every activity data
    private var volley = Volley() // backend requests class


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        /*
        function for creation of row views "packed" into recyclerView.
        also used by me for setting of LongClickListener for each row view.
        By long click user can interact with every activity directly
         */
        val rowView = inflater.inflate(R.layout.row_view, parent, false)
        val holder = ViewHolder(rowView)
        val context = rowView.context

        holder.itemView.setOnLongClickListener {
            //listener for every row view
            volley.volleyUser = activityRepo.getUser() //we require token for interaction with backend at the same time
            //to get right activity, we need to know holder position, as we can get activity by position index in dataSet
            val position = holder.adapterPosition
            val builder = AlertDialog.Builder(context)
            val gpsActivity = dataSet[position]
            //alert dialog with options for user
            builder.setTitle(context.resources.getString(R.string.question))
                    .setMessage(context.resources.getString(R.string.recyclerQuestion))
                    .setNegativeButton(context.resources.getString(R.string.openButton)) { _: DialogInterface, _: Int ->
                        //idea was to use same activity for view and edit, in order to save some resources
                        openActivity.putExtra("id", gpsActivity.id.toString())
                        context.startActivity(openActivity)
                        //previous activity closed
                        (context as Activity).finish()
                    }
                    .setPositiveButton(context.resources.getText(R.string.deleteButton)) { _: DialogInterface, _: Int ->
                        //removes activity from database and server
                        removeItem(gpsActivity.id.toInt())
                        volley.deleteSession(context, gpsActivity)
                        Toast.makeText(
                                context,
                                context.resources.getString(R.string.activityDeleted),
                                Toast.LENGTH_SHORT
                        ).show()
                    }
            val dialog = builder.create()
            dialog.show()
            true
        }
        return holder
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //function fills row views by activity data
        val gpsActivity = dataSet[position]
        holder.itemView.textNameDataRow.text = gpsActivity.name
        holder.itemView.textRecordedAtDataRow.text = gpsActivity.recordedAt
        holder.itemView.textDistanceDataRow.text = "%.0f m".format(gpsActivity.distance)
        holder.itemView.textDurationDataRow.text = Helpers().converterHMS(gpsActivity.duration)
        holder.itemView.textPaceDataRow.text = "%.2f min/km".format(gpsActivity.speed)
    }

    override fun getItemCount(): Int {
        return dataSet.count()
    }

    private fun removeItem(position: Int) {
        //function removes item from database and dataSet
        activityRepo.delete(position)
        dataSet.drop(position)
        refreshData()
        notifyDataSetChanged()
    }
}