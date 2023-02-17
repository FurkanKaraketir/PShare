package com.karaketir.pshare.model

import com.google.firebase.Timestamp

class Comment(
    var comment: String,
    var commentID: String,
    var commentOwnerID: String,
    var commentToPost: String,
    var commentToWho: String,
    var timestamp: Timestamp
)