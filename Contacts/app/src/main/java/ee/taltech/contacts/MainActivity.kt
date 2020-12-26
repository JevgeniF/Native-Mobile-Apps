package ee.taltech.contacts

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var personRepo: PersonRepository
    private lateinit var adapter: RecyclerView.Adapter<*>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        personRepo = PersonRepository(this).open()

        recyclerViewPersons.layoutManager = LinearLayoutManager(this)
        adapter = DataRecyclerViewAdapter(this, personRepo)
        recyclerViewPersons.adapter = adapter
        (adapter as DataRecyclerViewAdapter).refreshData()
    }


    fun addPersonOnClick(view: View) {
        val addContact = Intent(this, AddContactActivity::class.java)
        startActivity(addContact)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        personRepo.close()
    }
}