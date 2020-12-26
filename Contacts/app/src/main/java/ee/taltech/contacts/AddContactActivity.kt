package ee.taltech.contacts

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_add_contact.*


class AddContactActivity : ContactActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_contact)

        window.decorView.setOnTouchListener(object : OnSwipeListener(this@AddContactActivity) {
            override fun onSwipeLeft() {
                super.onSwipeLeft()
                swipeBack()
                this@AddContactActivity.overridePendingTransition(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
                finish()
            }

            override fun onSwipeRight() {
                super.onSwipeRight()
                swipeBack()
                this@AddContactActivity.overridePendingTransition(
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                finish()
            }
        })

        personRepo = PersonRepository(this).open()
        adapter = DataRecyclerViewAdapter(this, personRepo)
    }

    fun savePersonOnClick(view: View) {
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
            personRepo.add(
                Person(
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

    override fun onDestroy() {
        super.onDestroy()
        personRepo.close()
    }
}