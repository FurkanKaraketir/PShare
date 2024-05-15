package com.karaketir.pshare.services

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.karaketir.pshare.model.Post

class FirestoreRepository(private val database: FirebaseFirestore) {

    fun getUserProfileUrl(uid: String, callback: UserProfileCallback) {
        database.collection("User").document(uid).get().addOnSuccessListener {
            val profileImageURL = it.get("profileImageURL") as String
            callback.onProfileUrlRetrieved(profileImageURL)
        }.addOnFailureListener {
            callback.onProfileUrlRetrieved("default_url")
        }
    }

    fun getFollowedUsers(uid: String, callback: (List<String>) -> Unit) {
        database.collection("Followings").whereEqualTo("main", uid)
            .addSnapshotListener { followList, error ->
                if (error != null) {
                    callback(emptyList())
                    return@addSnapshotListener
                }
                val idList = followList?.map { it.get("followsWho").toString() } ?: emptyList()
                callback(idList)
            }
    }

    fun getBlockedUsers(uid: String, callback: (List<String>, List<String>) -> Unit) {
        database.collection("Blocks").whereEqualTo("main", uid)
            .addSnapshotListener { blockList, _ ->
                val myBlockList = blockList?.map { it.get("blocksWho").toString() } ?: emptyList()

                database.collection("Blocks").whereEqualTo("blocksWho", uid)
                    .addSnapshotListener { blockMeList, _ ->
                        val blockedMe = blockMeList?.map { it.get("main").toString() } ?: emptyList()
                        callback(myBlockList, blockedMe)
                    }
            }
    }

    fun getInitialPosts(batchSize: Int, callback: (List<Post>, DocumentSnapshot?) -> Unit) {
        database.collection("Post")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(batchSize.toLong())
            .get()
            .addOnSuccessListener { snapshot ->
                val posts = snapshot.documents.map { document ->
                    Post(
                        document.getString("postID")!!,
                        document.getString("postDescription")!!,
                        document.getString("postImageURL")!!,
                        document.getString("postOwnerID")!!,
                        document.getTimestamp("timestamp")!!
                    )
                }
                val lastVisible = snapshot.documents[snapshot.size() - 1]
                callback(posts, lastVisible)
            }
    }

    fun getMorePosts(lastVisible: DocumentSnapshot, batchSize: Int, callback: (List<Post>, DocumentSnapshot?) -> Unit) {
        database.collection("Post")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .startAfter(lastVisible)
            .limit(batchSize.toLong())
            .get()
            .addOnSuccessListener { snapshot ->
                val posts = snapshot.documents.map { document ->
                    Post(
                        document.getString("postID")!!,
                        document.getString("postDescription")!!,
                        document.getString("postImageURL")!!,
                        document.getString("postOwnerID")!!,
                        document.getTimestamp("timestamp")!!
                    )
                }
                val newLastVisible = if (snapshot.size() > 0) snapshot.documents[snapshot.size() - 1] else null
                callback(posts, newLastVisible)
            }
    }

    interface UserProfileCallback {
        fun onProfileUrlRetrieved(url: String)
    }
}
