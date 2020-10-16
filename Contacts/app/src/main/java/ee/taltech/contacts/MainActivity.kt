package ee.taltech.contacts

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var  personRepo : PersonRepository
    private lateinit var adapter : RecyclerView.Adapter<*>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        personRepo = PersonRepository(this).open()

        recyclerViewPersons.layoutManager = LinearLayoutManager(this)
        adapter = DataRecyclerViewAdapter(this, personRepo)
        recyclerViewPersons.adapter = adapter

    }

    fun savePersonOnClick(view : View) {
        if(editTextName.text.toString() != "Name" && editTextLastName.text.toString() != "Last Name") {
        personRepo.add(Person(editTextName.text.toString(), editTextLastName.text.toString(), editTextMobilePhone.text.toString(), editTextEmail.text.toString(), editTextSkype.text.toString()))}
        (adapter as DataRecyclerViewAdapter).refreshData()
        adapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        personRepo.close()
    }


    }