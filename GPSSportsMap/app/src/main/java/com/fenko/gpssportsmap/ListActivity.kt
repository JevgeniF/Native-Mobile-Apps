package com.fenko.gpssportsmap

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fenko.gpssportsmap.database.ActivityRepo
import com.fenko.gpssportsmap.database.DataRecyclerViewAdapter
import kotlinx.android.synthetic.main.activities_list.*

class ListActivity : AppCompatActivity() {
    /*
    Class Activity. Shows list of all gps activities by means of recyclerview
     */

    private lateinit var activityRepo: ActivityRepo         //local activities repo
    private lateinit var adapter: RecyclerView.Adapter<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activities_list)

        activityRepo = ActivityRepo(this).open()

        //generating view in layout
        recyclerViewActivities.layoutManager = LinearLayoutManager(this)
        adapter = DataRecyclerViewAdapter(this, activityRepo)
        recyclerViewActivities.adapter = adapter
        (adapter as DataRecyclerViewAdapter).refreshData()
    }

    fun onButtonCloseClick(view: View) {
        activityRepo.close()
        finish()
    }


    override fun onDestroy() {
        super.onDestroy()
        activityRepo.close()
    }

    override fun onStop() {
        super.onStop()
        activityRepo.close()
    }
}