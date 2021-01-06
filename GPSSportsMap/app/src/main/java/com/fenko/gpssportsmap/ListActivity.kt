package com.fenko.gpssportsmap

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fenko.gpssportsmap.database.ActivityRepo
import com.fenko.gpssportsmap.database.DataRecyclerViewAdapter
import kotlinx.android.synthetic.main.activities_list.*

class ListActivity : AppCompatActivity() {
    private lateinit var activityRepo: ActivityRepo
    private lateinit var adapter: RecyclerView.Adapter<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activities_list)

        activityRepo = ActivityRepo(this).open()

        recyclerViewActivities.layoutManager = LinearLayoutManager(this)
        adapter = DataRecyclerViewAdapter(this, activityRepo)
        recyclerViewActivities.adapter = adapter
        (adapter as DataRecyclerViewAdapter).refreshData()
    }

    fun onButtonCloseClick(view: View) {
       // val viewMainScreen = Intent(this, MapActivity::class.java)
       // startActivity(viewMainScreen)
        activityRepo.close()
        finish()
    }


    override fun onDestroy() {
        super.onDestroy()
        activityRepo.close()
    }
}