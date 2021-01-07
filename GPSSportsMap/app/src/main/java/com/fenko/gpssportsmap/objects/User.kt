package com.fenko.gpssportsmap.objects

class User() {
    /*
    Object used to keep app user data for Volley
     */

    var id: Long = 0                //database id
    var backendId: String = ""      //backend server id
    var eMail: String = ""          //registered e-mail
    var lastName: String = ""       //registered last name
    var firstName: String = ""      //registered first name
    var token: String = ""          //token received on login/registration

    constructor(firstName: String, lastName: String, eMail: String, token: String) : this() {
        //constructor used on login/registration and to read data from database
        this.firstName = firstName
        this.lastName = lastName
        this.eMail = eMail
        this.token = token
    }
}