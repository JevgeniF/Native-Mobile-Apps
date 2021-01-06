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
import com.fenko.gpssportsmap.objects.GPSActivity
import com.fenko.gpssportsmap.ListActivityData
import com.fenko.gpssportsmap.R
import com.fenko.gpssportsmap.Volley
import com.fenko.gpssportsmap.tools.Helpers
import kotlinx.android.synthetic.main.row_view.view.*


class DataRecyclerViewAdapter(context: Context, private val activityRepo: ActivityRepo) :
    RecyclerView.Adapter<DataRecyclerViewAdapter.ViewHolder>() {

    lateinit var dataSet: List<GPSActivity>

    fun refreshData() {
        dataSet = activityRepo.getAll()
    }

    init {
        refreshData()
    }

    private val inflater = LayoutInflater.from(context)
    private val openActivity = Intent(context, ListActivityData::class.java)
    var volley = Volley()


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rowView = inflater.inflate(R.layout.row_view, parent, false)
        val holder = ViewHolder(rowView)
        val context = rowView.context
        holder.itemView.setOnLongClickListener {
            volley.volleyUser = activityRepo.getUser()
            val position = holder.adapterPosition
            val builder = AlertDialog.Builder(context)
            val gpsActivity = dataSet[position]
            builder.setTitle(context.resources.getString(R.string.question))
                .setMessage("Do you want to?")
                .setNegativeButton(context.resources.getString(R.string.openButton)) { _: DialogInterface, _: Int ->
                    openActivity.putExtra("id", gpsActivity.id.toString())
                    println(gpsActivity.id)
                    context.startActivity(openActivity)
                    (context as Activity).finish()
                }
                .setPositiveButton(context.resources.getText(R.string.deleteButton)) { _: DialogInterface, _: Int ->

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
        val gpsActivity = dataSet[position]
        holder.itemView.textNameDataRow.text = gpsActivity.name
        holder.itemView.textRecordedAtDataRow.text = gpsActivity.recordedAt
        holder.itemView.textDurationDataRow.text = Helpers().converterHMS(gpsActivity.duration)
        holder.itemView.textPaceDataRow.text = "%.2f min/km".format(gpsActivity.speed)
    }

    override fun getItemCount(): Int {
        return dataSet.count()
    }

    private fun removeItem(position: Int) {
        activityRepo.delete(position)
        dataSet.drop(position)
        refreshData()
        notifyDataSetChanged()
    }
}