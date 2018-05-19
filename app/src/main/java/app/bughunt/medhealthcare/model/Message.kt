package app.bughunt.medhealthcare.model

class Message {

    var avatar: String? = null
    var message: String? = null
    var senderId: String? = null
    var type: String? = null
    var userName: String? = null

    companion object {

        const val TYPE_TEXT: String = "text"

        const val TYPE_IMAGE: String = "image"
    }

    constructor() {
        // Default constructor required for calls to DataSnapshot.getValue()
    }

    constructor(avatar: String?,
                message: String?,
                senderId: String?,
                type: String?,
                userName: String?) {

        this.avatar = avatar
        this.message = message
        this.userName = userName
        this.senderId = senderId
        this.type = type


    }


}