package ee.taltech.contacts

class Contact {
    var id: Int = 0
    var ownerID = 0
    var mobilePhone = ""
    var eMail = ""
    var skype = ""

    constructor(ownerID: Int, mobilePhone: String, eMail: String, skype: String): this(0, ownerID, mobilePhone, eMail, skype)

    constructor(id: Int, ownerID: Int, mobilePhone: String, eMail: String, skype: String) {
        this.id = id
        this.ownerID = ownerID
        this.mobilePhone = mobilePhone
        this.eMail = eMail
        this.skype = skype
    }
}