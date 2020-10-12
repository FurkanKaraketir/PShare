package com.furkankrktr.pshare

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_comments.*
import java.util.*
import kotlin.collections.ArrayList

class CommentsActivity : AppCompatActivity() {

    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var selectedPost: String
    private lateinit var recyclerCommentViewAdapter: CommentRecyclerAdapter
    var commentList = ArrayList<Comment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()

        selectedPost = intent.getStringExtra("selectedPost").toString()
        println("verileri al çağrıldı")
        verileriAl()
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val layoutManager = LinearLayoutManager(this)
        recyclerCommentsView.layoutManager = layoutManager
        recyclerCommentViewAdapter = CommentRecyclerAdapter(commentList)
        recyclerCommentsView.adapter = recyclerCommentViewAdapter
        println("lauot manager oluşturuldu")
    }


    fun commentSend(view: View) {
        val commentText = commentSendEditText.text.toString()
        val uuid = UUID.randomUUID()
        Toast.makeText(this, "Tek Seferde Yalnızca 1 Yorum Yapabilirsin", Toast.LENGTH_SHORT).show()
        if (commentText.isNotEmpty()) {
            sendButton.isClickable = false
            val guncelKullaniciEmail = auth.currentUser!!.email.toString()

            val tarih = Timestamp.now()

            val commentHashMap = hashMapOf<String, Any>()
            commentHashMap.put("kullaniciemail", guncelKullaniciEmail)
            commentHashMap.put("kullanicicomment", commentText)
            commentHashMap.put("selectedPost", selectedPost)
            commentHashMap.put("commentId", uuid.toString())
            commentHashMap.put("tarih", tarih)

            database.collection("Yorumlar").add(commentHashMap).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    commentSendEditText.text = null
                    Toast.makeText(this, "Yorum Yapıldı", Toast.LENGTH_LONG).show()
                    recyclerCommentViewAdapter.notifyDataSetChanged()
                    verileriAl()

                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this, exception.localizedMessage, Toast.LENGTH_LONG).show()
            }

        } else {
            sendButton.isClickable = true
            Toast.makeText(this, "Boş Yorum Yapamazsınız", Toast.LENGTH_SHORT).show()
        }
    }


    fun verileriAl() {
        database.collection("Yorumlar").whereEqualTo("selectedPost", selectedPost)
            .orderBy("tarih", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    println(exception.localizedMessage)
                } else {
                    if (snapshot != null) {
                        if (!snapshot.isEmpty) {
                            val documents = snapshot.documents


                            commentList.clear()
                            for (document in documents) {
                                val kullaniciComment = document.get("kullanicicomment") as String
                                val kullaniciEmail = document.get("kullaniciemail") as String
                                val selectedPost = document.get("selectedPost") as String
                                val commentId = document.get("commentId") as String
                                val indirilenComment =
                                    Comment(
                                        kullaniciEmail,
                                        kullaniciComment,
                                        selectedPost,
                                        commentId
                                    )
                                commentList.add(indirilenComment)
                            }

                            recyclerCommentViewAdapter.notifyDataSetChanged()

                        }


                    }
                }
            }
    }


}