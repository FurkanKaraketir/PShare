package com.karaketir.pshare.model

import com.google.firebase.Timestamp

class Reply(
    var reply: String,
    var replyID: String,
    var replyOwnerID: String,
    var replyToComment: String,
    var replyToPost: String,
    var replyToWho: String,
    var timestamp: Timestamp
)