package com.fenko.gpssportsmap

class User(val eMail: String, val password: String, val lastName: String, val firstName: String) {
    var token: String? = null
}