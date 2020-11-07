package com.furkankrktr.pshare

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.furkankrktr.pshare.adapter.ReplyRecyclerAdapter
import com.furkankrktr.pshare.model.Reply
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_replies.*
import java.util.*
import kotlin.collections.ArrayList

class RepliesActivity : AppCompatActivity() {
    private lateinit var selectedComment: String
    private lateinit var selectedCommentEmail: String
    private lateinit var selectedCommentText: String

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var recyclerReplyViewAdapter: ReplyRecyclerAdapter
    private var replyList = ArrayList<Reply>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_replies)

        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()

        val layoutManager = LinearLayoutManager(this)
        recyclerRepliesView.layoutManager = layoutManager
        recyclerReplyViewAdapter = ReplyRecyclerAdapter(replyList)
        recyclerRepliesView.adapter = recyclerReplyViewAdapter

        val replySendButton = findViewById<ImageButton>(R.id.replySendButton)

        selectedComment = intent.getStringExtra("selectedComment").toString()
        selectedCommentEmail = intent.getStringExtra("selectedCommentEmail").toString()
        selectedCommentText = intent.getStringExtra("selectedCommentText").toString()
        replyToCommentText.text = selectedCommentText
        replyToEmailText.text = selectedCommentEmail

        verileriAl()

        replySendButton.setOnClickListener {
            val replyText = replySendEditText.text.toString()
            val uuid = UUID.randomUUID()
            Toast.makeText(this, "Tek Seferde Yalnızca 1 Yorum Yapabilirsin", Toast.LENGTH_SHORT)
                .show()
            if (replyText.isNotEmpty()) {
                replySendButton.isClickable = false
                val guncelKullaniciEmail = auth.currentUser!!.email.toString()

                val tarih = Timestamp.now()

                val commentHashMap = hashMapOf<String, Any>()
                commentHashMap["kullaniciemail"] = guncelKullaniciEmail
                commentHashMap["kullanicireply"] = replyText
                commentHashMap["selectedComment"] = selectedComment
                commentHashMap["replyId"] = uuid.toString()
                commentHashMap["tarih"] = tarih

                database.collection("Yanıtlar").add(commentHashMap).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        replySendEditText.text = null
                        Toast.makeText(this, "Yanıt Yazıldı", Toast.LENGTH_LONG).show()
                        recyclerReplyViewAdapter.notifyDataSetChanged()
                        verileriAl()

                    }
                }.addOnFailureListener { exception ->
                    Toast.makeText(this, exception.localizedMessage, Toast.LENGTH_LONG).show()
                }

            } else {
                replySendButton.isClickable = true
                Toast.makeText(this, "Boş Yanıt Yapamazsınız", Toast.LENGTH_SHORT).show()
            }
        }


    }

    private fun verileriAl() {
        database.collection("Yanıtlar").whereEqualTo("selectedComment", selectedComment)
            .orderBy("tarih", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    println(exception.localizedMessage)
                } else {
                    if (snapshot != null) {
                        if (!snapshot.isEmpty) {
                            val documents = snapshot.documents


                            replyList.clear()
                            for (document in documents) {
                                val kullaniciReply = document.get("kullanicireply") as String
                                val kullaniciEmail = document.get("kullaniciemail") as String
                                val replyId = document.get("replyId") as String
                                val indirilenReply =
                                    Reply(
                                        kullaniciEmail,
                                        kullaniciReply,
                                        replyId
                                    )
                                replyList.add(indirilenReply)
                            }

                            recyclerReplyViewAdapter.notifyDataSetChanged()

                        }


                    }
                }
            }
    }

}