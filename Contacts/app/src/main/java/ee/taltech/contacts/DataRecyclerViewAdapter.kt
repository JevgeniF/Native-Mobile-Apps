package ee.taltech.contacts

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.row_view.view.*


class DataRecyclerViewAdapter(context: Context, private val repo: PersonRepository) : RecyclerView.Adapter<DataRecyclerViewAdapter.ViewHolder>() {

    private lateinit var dataSet: List<Person>

    fun refreshData() {
        dataSet = repo.getAll()
    }

    init {
        refreshData()
    }
    private val inflater = LayoutInflater.from(context)

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnLongClickListener {
        override fun onLongClick(v: View?): Boolean {
            TODO("Not yet implemented")
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rowView = inflater.inflate(R.layout.row_view, parent, false)
        return ViewHolder(rowView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val person = dataSet[position]
        holder.itemView.viewId.text = person.id.toString()
        holder.itemView.viewName.text = person.name
        holder.itemView.ViewLastName.text = person.lastname
        holder.itemView.viewMobilePhone.text = person.mobilePhone
        holder.itemView.viewMobType.text = "Mobile"
        holder.itemView.viewEmail.text = person.eMail
        holder.itemView.viewMailType.text = "E-Mail"
        holder.itemView.viewSkype.text = person.skype
        holder.itemView.viewSkypetype.text = "Skype"

    }

    override fun getItemCount(): Int {
       return dataSet.count()
    }

    /*fun removeItem(position: Int) {
        dataSet.drop(position)
        notifyDataSetChanged()
    }
     */

}