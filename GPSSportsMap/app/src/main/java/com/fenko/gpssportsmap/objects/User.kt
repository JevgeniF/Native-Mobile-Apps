package com.fenko.gpssportsmap.objects

import android.os.Parcel
import android.os.Parcelable

class User() {

    var id: Long = 0
    var backendId: String = ""
    var eMail: String = ""
    var lastName: String = ""
    var firstName: String = ""
    var token: String = ""
    var userId: String = ""

    constructor(firstName: String, lastName: String, eMail: String, token: String) : this() {
        this.firstName = firstName
        this.lastName = lastName
        this.eMail = eMail
        this.token = token
        //this.password = password
    }
}