package com.karaketir.pshare.model

import com.google.firebase.Timestamp

class Post(
    var postID: String,
    var postDescription: String,
    var postImageURL: String,
    var postOwnerID: String,
    var timestamp: Timestamp,
)