package com.furkankrktr.pshare

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.furkankrktr.pshare.adapter.UserEmailFilterAdapter
import com.furkankrktr.pshare.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_haberler.*

class UserEmailFilterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var recyclerViewAdapter: UserEmailFilterAdapter
    private lateinit var selectedEmail: String

    var postList = ArrayList<Post>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_email_filter)

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        selectedEmail = intent.getStringExtra("selectedEmail").toString()


        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()
        verileriAl()
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerViewAdapter = UserEmailFilterAdapter(postList)
        recyclerView.adapter = recyclerViewAdapter


    }

    fun verileriAl() {

        database.collection("Post").whereEqualTo("kullaniciemail", selectedEmail)
            .orderBy("tarih", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    println(exception.localizedMessage)

                    Toast.makeText(this, exception.localizedMessage, Toast.LENGTH_LONG).show()
                } else {
                    if (snapshot != null) {
                        if (!snapshot.isEmpty) {
                            val documents = snapshot.documents

                            postList.clear()

                            for (document in documents) {
                                val kullaniciEmail = document.get("kullaniciemail") as String
                                val kullaniciYorum = document.get("kullaniciyorum") as String
                                val gorselUrl = document.get("gorselurl") as String
                                val postId = document.get("postId") as String
                                val indirilenPost =
                                    Post(kullaniciEmail, kullaniciYorum, gorselUrl, postId)
                                postList.add(indirilenPost)


                            }

                            recyclerViewAdapter.notifyDataSetChanged()

                        }
                    }
                }
            }
    }


}