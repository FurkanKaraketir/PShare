package com.furkankrktr.pshare

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.furkankrktr.pshare.adapter.HashtagRecyclerAdapter
import com.furkankrktr.pshare.databinding.ActivityHashtagBinding
import com.furkankrktr.pshare.model.Post
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HashtagActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var recyclerViewAdapter: HashtagRecyclerAdapter
    private lateinit var hashtagPostAddBtn: FloatingActionButton
    private lateinit var selectedHashtag: String
    private lateinit var recyclerView: RecyclerView

    private var postList = ArrayList<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityHashtagBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        selectedHashtag = intent.getStringExtra("selectedHashtag").toString()

        supportActionBar?.title = selectedHashtag

        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()
        recyclerView = binding.recyclerView
        hashtagPostAddBtn = binding.hashtagPostAddBtn
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerViewAdapter = HashtagRecyclerAdapter(postList)
        recyclerView.adapter = recyclerViewAdapter

        hashtagPostAddBtn.setOnClickListener {
            val intent = Intent(this, FotografPaylasmaActivity::class.java)
            startActivity(intent)
        }

        verileriAl()
    }

    private fun verileriAl() {
        database.collection("Post")
            .orderBy("tarih", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Toast.makeText(this, exception.localizedMessage, Toast.LENGTH_LONG).show()
                    println(exception.localizedMessage)
                } else {
                    if (snapshot != null) {
                        if (!snapshot.isEmpty) {
                            val documents = snapshot.documents

                            postList.clear()

                            for (document in documents) {
                                try {
                                    val kullaniciEmail = document.get("kullaniciemail") as String
                                    val kullaniciYorum = document.get("kullaniciyorum") as String
                                    val gorselUrl = document.get("gorselurl") as String
                                    val postId = document.get("postId") as String
                                    val kullaniciUID = document.get("userID") as String
                                    if (kullaniciYorum.contains(selectedHashtag)) {
                                        val indirilenPost =
                                            Post(
                                                kullaniciEmail,
                                                kullaniciYorum,
                                                gorselUrl,
                                                postId,
                                                kullaniciUID
                                            )
                                        postList.add(indirilenPost)
                                    }

                                } catch (e: Exception) {
                                    try {

                                        val kullaniciEmail =
                                            document.get("kullaniciemail") as String
                                        val kullaniciYorum =
                                            document.get("kullaniciyorum") as String
                                        val gorselUrl = document.get("gorselurl") as String
                                        val postId = document.get("postId") as String
                                        val kullaniciUID = "M6OZguiPKVQs6Z2qfh9HCntoKQi2"
                                        if (kullaniciYorum.contains(selectedHashtag)) {
                                            val indirilenPost =
                                                Post(
                                                    kullaniciEmail,
                                                    kullaniciYorum,
                                                    gorselUrl,
                                                    postId,
                                                    kullaniciUID
                                                )
                                            postList.add(indirilenPost)
                                        }
                                    } catch (e: Exception) {
                                        val kullaniciEmail =
                                            document.get("kullaniciemail") as String
                                        val kullaniciYorum =
                                            document.get("kullaniciyorum") as String
                                        val gorselUrl = ""
                                        val postId = document.get("postId") as String
                                        val kullaniciUID = "M6OZguiPKVQs6Z2qfh9HCntoKQi2"
                                        if (kullaniciYorum.contains(selectedHashtag)) {
                                            val indirilenPost =
                                                Post(
                                                    kullaniciEmail,
                                                    kullaniciYorum,
                                                    gorselUrl,
                                                    postId,
                                                    kullaniciUID
                                                )
                                            postList.add(indirilenPost)
                                        }
                                    }

                                }


                            }

                            recyclerViewAdapter.notifyDataSetChanged()

                        }
                    }
                }
            }
    }

}