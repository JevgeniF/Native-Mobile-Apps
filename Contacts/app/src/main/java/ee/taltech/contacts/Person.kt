package ee.taltech.contacts

class Person {
    var id: Int = 0
    var name: String =""
    var lastname: String =""
    var mobilePhone = ""
    var eMail = ""
    var skype = ""

    constructor(name: String, lastname: String, mobilePhone: String, eMail: String, skype: String): this(0, name, lastname,  mobilePhone, eMail, skype)

    constructor(id: Int, name: String, lastname: String, mobilePhone: String, eMail: String, skype: String) {
        this.id = id
        this.name = name
        this.lastname = lastname
        this.mobilePhone = mobilePhone
        this.eMail = eMail
        this.skype = skype
    }

}