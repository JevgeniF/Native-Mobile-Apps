package ee.taltech.contacts

import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView

open class ContactActivity : AppCompatActivity() {
    protected lateinit var personRepo: PersonRepository
    protected lateinit var adapter: RecyclerView.Adapter<*>

    fun entryCheckOK(
        name: String,
        lastname: String,
        typeOne: String,
        contactOne: String,
        typeTwo: String,
        contactTwo: String,
        typeThree: String,
        contactThree: String
    ): Boolean {
        var flag = false
        if (isNamesOk(name, lastname)) {
            if (contactOne != "") {
                if (isPatternOk(typeOne, contactOne)) {
                    if (contactTwo != "" && isPatternOk(typeTwo, contactTwo)) {
                        if (contactThree != "" && isPatternOk(typeThree, contactThree)) {
                            flag = true
                        } else if (contactThree == "") {
                            flag = true
                        }
                    } else if (contactTwo == "") {
                        flag = true
                    }
                }
            } else {
                Toast.makeText(
                    this,
                    resources.getString(R.string.mandatoryContact),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        return flag

    }

    open fun isNamesOk(name: String, lastname: String): Boolean {
        var flag = false
        if (name.length > 2) {
            if (lastname.length > 2) {
                val contacts = personRepo.getAll()
                if (contacts.isNotEmpty()) {
                    for (i in contacts.indices) {
                        if (name != contacts[i].name || lastname != contacts[i].lastname) {
                            flag = true
                        } else {
                            flag = false
                            Toast.makeText(
                                this,
                                "$name $lastname ${resources.getString(R.string.alreadyInContacts)}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else if (contacts.isEmpty()) {
                    flag = true
                }
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

    private fun isPatternOk(type: String, contact: String): Boolean {
        return if (type == "phone" && android.util.Patterns.PHONE.matcher(contact).matches()) {
            true
        } else if (type == "e-mail" && android.util.Patterns.EMAIL_ADDRESS.matcher(contact)
                .matches()
        ) {
            true
        } else if (type == "skype" && contact.length > 2 && contact != "") {
            true
        } else {
            Toast.makeText(
                this,
                "$contact ${resources.getString(R.string.invalidContact)}!",
                Toast.LENGTH_SHORT
            ).show()
            false
        }
    }

    fun spinnerPosition(value: String): Int {
        var position = 0
        if (value == "phone") {
            position = 0
        }
        if (value == "e-mail") {
            position = 1
        }
        if (value == "skype") {
            position = 2
        }
        return position
    }

    fun swipeBack() {
        val backToList = Intent(this, MainActivity::class.java)
        startActivity(backToList)
    }
}