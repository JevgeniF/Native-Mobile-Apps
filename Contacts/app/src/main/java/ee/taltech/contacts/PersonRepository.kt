package ee.taltech.contacts

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase

class PersonRepository(private val context: Context) {

    private lateinit var dbHelper: DBHelper
    private lateinit var db: SQLiteDatabase
    private var ownerId: Long? = null

    fun open(): PersonRepository {
        dbHelper = DBHelper(context)
        db = dbHelper.writableDatabase

        return this
    }

    fun close() {
        dbHelper.close()
    }

    fun add(person: Person) {
        val contentValuesP = ContentValues()
        contentValuesP.put(DBHelper.PERSON_NAME, person.name)
        contentValuesP.put(DBHelper.PERSON_LASTNAME, person.lastname)
        ownerId = db.insert(DBHelper.PERSONS_TABLE_NAME, null, contentValuesP)
        val contentValuesC = ContentValues()
        contentValuesC.put(DBHelper.CONTACT_OWNER_ID, ownerId)
        contentValuesC.put(DBHelper.CONTACT_TYPE_ONE, person.contact!!.typeOne)
        contentValuesC.put(DBHelper.CONTACT_CONTACT_ONE, person.contact!!.contactOne)
        contentValuesC.put(DBHelper.CONTACT_TYPE_TWO, person.contact!!.typeTwo)
        contentValuesC.put(DBHelper.CONTACT_CONTACT_TWO, person.contact!!.contactTwo)
        contentValuesC.put(DBHelper.CONTACT_TYPE_THREE, person.contact!!.typeThree)
        contentValuesC.put(DBHelper.CONTACT_CONTACT_THREE, person.contact!!.contactThree)
        db.insert(DBHelper.CONTACTS_TABLE_NAME, null, contentValuesC)
    }

    fun update(person: Person) {
        val contentValuesP = ContentValues()
        contentValuesP.put(DBHelper.PERSON_NAME, person.name)
        contentValuesP.put(DBHelper.PERSON_LASTNAME, person.lastname)
        db.update(
            DBHelper.PERSONS_TABLE_NAME,
            contentValuesP,
            DBHelper.PERSON_ID + " = ${person.id}",
            null
        )
        val contentValuesC = ContentValues()
        contentValuesC.put(DBHelper.CONTACT_TYPE_ONE, person.contact!!.typeOne)
        contentValuesC.put(DBHelper.CONTACT_CONTACT_ONE, person.contact!!.contactOne)
        contentValuesC.put(DBHelper.CONTACT_TYPE_TWO, person.contact!!.typeTwo)
        contentValuesC.put(DBHelper.CONTACT_CONTACT_TWO, person.contact!!.contactTwo)
        contentValuesC.put(DBHelper.CONTACT_TYPE_THREE, person.contact!!.typeThree)
        contentValuesC.put(DBHelper.CONTACT_CONTACT_THREE, person.contact!!.contactThree)
        db.update(
            DBHelper.CONTACTS_TABLE_NAME,
            contentValuesC,
            DBHelper.CONTACT_OWNER_ID + " = ${person.id}",
            null
        )
    }

    fun getAll(): List<Person> {
        val persons = ArrayList<Person>()
        val sqlQuery =
            "SELECT * FROM ${DBHelper.PERSONS_TABLE_NAME} as persons " +
                    "INNER JOIN ${DBHelper.CONTACTS_TABLE_NAME} as contacts " +
                    "ON persons.${DBHelper.PERSON_ID} = contacts.${DBHelper.CONTACT_OWNER_ID} " +
                    "ORDER BY persons.${DBHelper.PERSON_NAME};"
        val cursor = db.rawQuery(sqlQuery, null)

        while (cursor.moveToNext()) {
            persons.add(
                Person(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    Contact(
                        cursor.getInt(3),
                        cursor.getLong(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7),
                        cursor.getString(8),
                        cursor.getString(9),
                        cursor.getString(10)
                    )
                )
            )
        }
        cursor.close()
        if (persons.size > 0) {
            for (i in 0 until persons.size) {
                persons[i].listId = i + 1
            }
        }
        return persons
    }

    fun get(id: Int): Person {
        var person = Person()
        val sqlQuery =
            "SELECT * FROM PERSONS as persons INNER JOIN CONTACTS as contacts ON persons.id = contacts.id WHERE persons.id = $id;"
        val cursor = db.rawQuery(sqlQuery, null)

        while (cursor.moveToNext()) {
            person = Person(
                cursor.getInt(0),
                cursor.getString(1),
                cursor.getString(2),
                Contact(
                    cursor.getInt(3),
                    cursor.getLong(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getString(7),
                    cursor.getString(8),
                    cursor.getString(9),
                    cursor.getString(10)
                )
            )
        }
        cursor.close()
        return person
    }

    fun delete(id: Int) {
        db.delete(DBHelper.PERSONS_TABLE_NAME, DBHelper.PERSON_ID + " = " + id, null)
        db.delete(DBHelper.CONTACTS_TABLE_NAME, DBHelper.CONTACT_OWNER_ID + " = " + id, null)
    }

}