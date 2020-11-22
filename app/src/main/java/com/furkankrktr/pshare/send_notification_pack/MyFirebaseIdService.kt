@file:Suppress("DEPRECATION")

package com.furkankrktr.pshare.send_notification_pack


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService


class MyFirebaseIdService : FirebaseMessagingService() {
    override fun onNewToken(s: String) {
        super.onNewToken(s)
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val refreshToken: String = FirebaseInstanceId.getInstance().token.toString()
        if (firebaseUser != null) {
            updateToken(refreshToken)
        }
    }

    private fun updateToken(refreshToken: String) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val token = Token(refreshToken)
        FirebaseDatabase.getInstance().getReference("Tokens")
            .child(firebaseUser!!.uid).setValue(token)
    }
}