package ee.taltech.contacts

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase

class PersonRepository(private val context: Context) {

    private lateinit var dbHelper: DBHelper
    private lateinit var db: SQLiteDatabase

    fun open(): PersonRepository {
        dbHelper = DBHelper(context)
        db = dbHelper.writableDatabase
        db.execSQL("PRAGMA foreign_keys=ON")

        return this
    }

    fun close() {
        dbHelper.close()
    }

    fun add(person: Person){
        val contentValues = ContentValues()
        contentValues.put(DBHelper.PERSON_NAME, person.name)
        contentValues.put(DBHelper.PERSON_LASTNAME, person.lastname)
        contentValues.put(DBHelper.CONTACT_PHONE, person.mobilePhone)
        contentValues.put(DBHelper.CONTACT_MAIL, person.eMail)
        contentValues.put(DBHelper.CONTACT_SKYPE, person.skype)
        db.insert(DBHelper.PERSONS_TABLE_NAME, null, contentValues)
    }

   /* fun add(contact: Contact){
        val contentValues = ContentValues()
        contentValues.put(DBHelper.CONTACT_PHONE, contact.mobilePhone)
        contentValues.put(DBHelper.CONTACT_MAIL, contact.eMail)
        contentValues.put(DBHelper.CONTACT_SKYPE, contact.skype)
        db.insert(DBHelper.CONTACTS_TABLE_NAME, null, contentValues)
    }

    */

    fun getAll(): List<Person> {
        val persons = ArrayList<Person>()
        val columns = arrayOf(DBHelper.PERSON_ID, DBHelper.PERSON_NAME, DBHelper.PERSON_LASTNAME, DBHelper.CONTACT_PHONE, DBHelper.CONTACT_MAIL, DBHelper.CONTACT_SKYPE)
        val cursor = db.query(DBHelper.PERSONS_TABLE_NAME, columns, null, null, null, null, null)

        while(cursor.moveToNext()) {
            persons.add(
                    Person(
                            cursor.getInt(0),
                            cursor.getString(1),
                            cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5)
                    )
            )
        }
        cursor.close()
        return persons
    }

}