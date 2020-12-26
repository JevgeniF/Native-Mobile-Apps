package ee.taltech.contacts

class Contact {
    var id = 0
    private var ownerID = 0L
    var typeOne = ""
    var contactOne = ""
    var typeTwo = ""
    var contactTwo = ""
    var typeThree = ""
    var contactThree = ""

    constructor(
        typeOne: String,
        contactOne: String,
        typeTwo: String,
        contactTwo: String,
        typeThree: String,
        contactThree: String
    ) {
        this.typeOne = typeOne
        this.contactOne = contactOne
        this.typeTwo = typeTwo
        this.contactTwo = contactTwo
        this.typeThree = typeThree
        this.contactThree = contactThree
    }

    constructor(
        id: Int,
        ownerID: Long,
        typeOne: String,
        contactOne: String,
        typeTwo: String,
        contactTwo: String,
        typeThree: String,
        contactThree: String
    ) {
        this.id = id
        this.ownerID = ownerID
        this.typeOne = typeOne
        this.contactOne = contactOne
        this.typeTwo = typeTwo
        this.contactTwo = contactTwo
        this.typeThree = typeThree
        this.contactThree = contactThree
    }
}