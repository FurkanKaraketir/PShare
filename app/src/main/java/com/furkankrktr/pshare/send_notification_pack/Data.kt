@file:Suppress("unused")

package com.furkankrktr.pshare.send_notification_pack

class Data(
    val Title: String,
    val Message: String,
    val IntentID: String,
    val IntentEmail: String,
    val IntentText: String,
    val selectedCommentUID: String,
    val selectedCommentImage: String,
    val selectedPostUID: String
) {
    constructor() : this("", "", "", "", "", "", "", "")
}