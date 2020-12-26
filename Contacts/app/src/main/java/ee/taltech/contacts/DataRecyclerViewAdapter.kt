package ee.taltech.contacts

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
import kotlinx.android.synthetic.main.row_view.view.*


class DataRecyclerViewAdapter(context: Context, private val repo: PersonRepository) :
    RecyclerView.Adapter<DataRecyclerViewAdapter.ViewHolder>() {

    private lateinit var dataSet: List<Person>

    fun refreshData() {
        dataSet = repo.getAll()
    }

    init {
        refreshData()
    }

    private val inflater = LayoutInflater.from(context)
    private val editContact = Intent(context, EditContactActivity::class.java)


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rowView = inflater.inflate(R.layout.row_view, parent, false)
        val holder = ViewHolder(rowView)
        val context = rowView.context
        holder.itemView.setOnLongClickListener {
            val position = holder.adapterPosition
            val builder = AlertDialog.Builder(context)
            val person = dataSet[position]
            builder.setTitle(context.resources.getString(R.string.question))
                .setMessage(context.resources.getString(R.string.questionBody))
                .setNegativeButton(context.resources.getString(R.string.editButton)) { _: DialogInterface, _: Int ->
                    editContact.putExtra("id", person.id.toString())
                    context.startActivity(editContact)
                    (context as Activity).finish()
                }
                .setPositiveButton(context.resources.getText(R.string.deleteButton)) { _: DialogInterface, _: Int ->
                    removeItem(person.id)
                    Toast.makeText(
                        context,
                        context.resources.getString(R.string.contactDeleted),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            val dialog = builder.create()
            dialog.show()
            true
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val person = dataSet[position]
        holder.itemView.textId.text = person.listId.toString()
        holder.itemView.textName.text = person.name
        holder.itemView.textLastName.text = person.lastname
        holder.itemView.textTypeOne.text = person.contact!!.typeOne
        holder.itemView.textContactOne.text = person.contact!!.contactOne
        if (person.contact!!.contactTwo != "") {
            holder.itemView.textTypeTwo.text = person.contact!!.typeTwo
            holder.itemView.textContactTwo.text = person.contact!!.contactTwo
        }
        if (person.contact!!.contactThree != "") {
            holder.itemView.textTypeThree.text = person.contact!!.typeThree
            holder.itemView.textContactThree.text = person.contact!!.contactThree
        }
    }

    override fun getItemCount(): Int {
        return dataSet.count()
    }

    private fun removeItem(position: Int) {
        repo.delete(position)
        dataSet.drop(position)
        refreshData()
        notifyDataSetChanged()
    }
}