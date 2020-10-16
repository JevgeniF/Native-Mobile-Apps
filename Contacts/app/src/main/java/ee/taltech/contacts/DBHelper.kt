package ee.taltech.contacts

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_NAME = "contacts.db"
        const val DATABASE_VERSION = 4

        const val PERSONS_TABLE_NAME = "PERSONS"
        const val PERSON_ID = "_id"
        const val PERSON_NAME = "firstname"
        const val PERSON_LASTNAME = "lastname"

        //const val CONTACTS_TABLE_NAME = "CONTACTS"
        //const val CONTACT_ID = "_id"
        //const val CONTACT_OWNER_ID = "id"
        const val CONTACT_PHONE = "mobile_phone"
        const val CONTACT_MAIL = "e_mail"
        const val CONTACT_SKYPE = "skype"

        const val SQL_PERSON_CREATE_TABLE = "create table $PERSONS_TABLE_NAME (" +
                "$PERSON_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$PERSON_NAME TEXT NOT NULL, " +
                "$PERSON_LASTNAME TEXT NOT NULL, " +
                "$CONTACT_PHONE TEXT, " +
                "$CONTACT_MAIL TEXT, " +
                "$CONTACT_SKYPE TEXT);"

        /*const val SQL_CONTACT_CREATE_TABLE = "create table $CONTACTS_TABLE_NAME (" +
                "$CONTACT_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$CONTACT_OWNER_ID INTEGER, " +
                "FOREIGN KEY ($CONTACT_OWNER_ID) REFERENCES $PERSONS_TABLE_NAME($PERSON_ID), " +
                "$CONTACT_PHONE TEXT, " +
                "$CONTACT_MAIL TEXT, " +
                "$CONTACT_SKYPE TEXT);"

         */

        const val SQL_DELETE_TABLES = "DROP TABLE IF EXISTS $PERSONS_TABLE_NAME;"
        //+ "DROP TABLE IF EXISTS $CONTACTS_TABLE_NAME;"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(SQL_PERSON_CREATE_TABLE)
        //db?.execSQL(SQL_CONTACT_CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL((SQL_DELETE_TABLES))
        onCreate(db)
    }

}