package ee.taltech.contacts

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class DBHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_NAME = "contactsManager.db"
        const val DATABASE_VERSION = 1

        const val PERSONS_TABLE_NAME = "PERSONS"
        const val PERSON_ID = "id"
        const val PERSON_NAME = "firstname"
        const val PERSON_LASTNAME = "lastname"

        const val CONTACTS_TABLE_NAME = "CONTACTS"
        private const val CONTACT_ID = "id"
        const val CONTACT_OWNER_ID = "owner_id"
        const val CONTACT_TYPE_ONE = "type_one"
        const val CONTACT_CONTACT_ONE = "contact_one"
        const val CONTACT_TYPE_TWO = "type_two"
        const val CONTACT_CONTACT_TWO = "contact_two"
        const val CONTACT_TYPE_THREE = "type_three"
        const val CONTACT_CONTACT_THREE = "contact_three"

        const val SQL_PERSON_CREATE_TABLE = "create table $PERSONS_TABLE_NAME (" +
                "$PERSON_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$PERSON_NAME TEXT NOT NULL, " +
                "$PERSON_LASTNAME TEXT NOT NULL);"

        const val SQL_CONTACT_CREATE_TABLE = "create table $CONTACTS_TABLE_NAME (" +
                "$CONTACT_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$CONTACT_OWNER_ID INTEGER, " +
                "$CONTACT_TYPE_ONE TEXT, " +
                "$CONTACT_CONTACT_ONE TEXT, " +
                "$CONTACT_TYPE_TWO TEXT, " +
                "$CONTACT_CONTACT_TWO TEXT, " +
                "$CONTACT_TYPE_THREE TEXT, " +
                "$CONTACT_CONTACT_THREE TEXT);"

        const val SQL_DELETE_TABLES = "DROP TABLE IF EXISTS $PERSONS_TABLE_NAME;\n" +
                "DROP TABLE IF EXISTS $CONTACTS_TABLE_NAME;"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(SQL_PERSON_CREATE_TABLE)
        db?.execSQL(SQL_CONTACT_CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL((SQL_DELETE_TABLES))
        onCreate(db)
    }

}