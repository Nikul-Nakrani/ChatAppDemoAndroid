package com.example.simplechatappdemo

class Message {
    var message: String? = null
    var senderId: String? = null
    var imageUrl: String? = null
    var fileName: String? = null
    var fileExtension: String? = null

    constructor()

    constructor(
        message: String,
        senderId: String,
        imageUrl: String,
        fileName: String,
        fileExtension: String
    ) {
        this.message = message
        this.senderId = senderId
        this.imageUrl = imageUrl
        this.fileName = fileName
        this.fileExtension = fileExtension
    }
}