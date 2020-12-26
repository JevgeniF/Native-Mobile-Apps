package ee.taltech.contacts

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_edit_contact.*

class EditContactActivity : ContactActivity() {

    private var id: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_contact)

        id = intent.getStringExtra("id")!!.toInt()

        window.decorView.setOnTouchListener(object : OnSwipeListener(this@EditContactActivity) {
            override fun onSwipeLeft() {
                super.onSwipeLeft()
                swipeBack()
                this@EditContactActivity.overridePendingTransition(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
                finish()
            }

            override fun onSwipeRight() {
                super.onSwipeRight()
                swipeBack()
                this@EditContactActivity.overridePendingTransition(
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                finish()
            }
        })

        personRepo = PersonRepository(this).open()
        adapter = DataRecyclerViewAdapter(this, personRepo)

        val person = personRepo.get(id!!)

        findViewById<EditText>(R.id.editTextName).setText(person.name)
        findViewById<EditText>(R.id.editTextLastName).setText(person.lastname)
        findViewById<EditText>(R.id.editTextContactOne).setText(person.contact!!.contactOne)
        findViewById<EditText>(R.id.editTextContactTwo).setText(person.contact!!.contactTwo)
        findViewById<EditText>(R.id.editTextContactThree).setText(person.contact!!.contactThree)
        findViewById<Spinner>(R.id.spinnerTypeOne).setSelection(spinnerPosition(person.contact!!.typeOne))
        findViewById<Spinner>(R.id.spinnerTypeTwo).setSelection(spinnerPosition(person.contact!!.typeTwo))
        findViewById<Spinner>(R.id.spinnerTypeThree).setSelection(spinnerPosition(person.contact!!.typeThree))
    }

    fun editPersonOnClick(view: View) {
        if (entryCheckOK(
                editTextName.text.toString().trim(),
                editTextLastName.text.toString().trim(),
                spinnerTypeOne.selectedItem.toString(),
                editTextContactOne.text.toString().trim(),
                spinnerTypeTwo.selectedItem.toString(),
                editTextContactTwo.text.toString().trim(),
                spinnerTypeThree.selectedItem.toString(),
                editTextContactThree.text.toString().trim()
            )
        ) {
            personRepo.update(
                Person(
                    id!!,
                    editTextName.text.toString().trim(),
                    editTextLastName.text.toString().trim(),
                    Contact(
                        spinnerTypeOne.selectedItem.toString(),
                        editTextContactOne.text.toString().trim(),
                        spinnerTypeTwo.selectedItem.toString(),
                        editTextContactTwo.text.toString().trim(),
                        spinnerTypeThree.selectedItem.toString(),
                        editTextContactThree.text.toString().trim()
                    )
                )
            )
            setContentView(R.layout.activity_main)
            (adapter as DataRecyclerViewAdapter).refreshData()
            adapter.notifyDataSetChanged()

            Toast.makeText(this, resources.getString(R.string.contactSaved), Toast.LENGTH_SHORT)
                .show()
            val saveAndClose = Intent(this, MainActivity::class.java)
            startActivity(saveAndClose)
            finish()
        }
    }

    override fun isNamesOk(name: String, lastname: String): Boolean {
        var flag = false
        if (name.length > 2) {
            if (lastname.length > 2) {
                flag = true
            } else {
                Toast.makeText(
                    this,
                    "$lastname ${resources.getString(R.string.tooShortName)}",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        } else {
            Toast.makeText(
                this,
                "$name ${resources.getString(R.string.tooShortName)}",
                Toast.LENGTH_SHORT
            ).show()
        }
        return flag
    }

    override fun onDestroy() {
        super.onDestroy()
        personRepo.close()
    }
}
