package ee.taltech.contacts

class Person {
    var id: Int = 0
    var listId: Int? = null
    var name: String = ""
    var lastname: String = ""
    var contact: Contact? = null

    constructor(name: String, lastname: String, contact: Contact) {
        this.name = name
        this.lastname = lastname
        this.contact = contact
    }

    constructor(id: Int, name: String, lastname: String, contact: Contact) {
        this.id = id
        this.name = name
        this.lastname = lastname
        this.contact = contact
    }

    constructor()
}