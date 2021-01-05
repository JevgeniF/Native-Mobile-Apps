package com.fenko.gpssportsmap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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


    override fun onDestroy() {
        super.onDestroy()
        activityRepo.close()
    }
}