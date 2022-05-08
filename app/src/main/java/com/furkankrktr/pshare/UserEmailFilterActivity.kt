package com.furkankrktr.pshare

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewAnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.furkankrktr.pshare.adapter.UserEmailFilterAdapter
import com.furkankrktr.pshare.databinding.ActivityUserEmailFilterBinding
import com.furkankrktr.pshare.model.Post
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlin.math.hypot

class UserEmailFilterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var recyclerViewAdapter: UserEmailFilterAdapter
    private lateinit var selectedEmail: String
    private lateinit var userEmailFilterPostAddBtn: FloatingActionButton
    private lateinit var recyclerView: RecyclerView

    private var postList = ArrayList<Post>()


    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityUserEmailFilterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView = binding.recyclerView
        userEmailFilterPostAddBtn = binding.userEmailFilterPostAddBtn
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        selectedEmail = intent.getStringExtra("selectedEmail").toString()
        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()


        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerViewAdapter = UserEmailFilterAdapter(postList)
        recyclerView.adapter = recyclerViewAdapter
        userEmailFilterPostAddBtn.setOnClickListener {
            val intent = Intent(this, FotografPaylasmaActivity::class.java)
            startActivity(intent)
        }
        verileriAl()

        if (userEmailFilterPostAddBtn.visibility == View.INVISIBLE) {

            userEmailFilterPostAddBtn.post {

                val cx = userEmailFilterPostAddBtn.width / 2
                val cy = userEmailFilterPostAddBtn.height / 2

                val finalRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()
                val anim = ViewAnimationUtils.createCircularReveal(
                    userEmailFilterPostAddBtn,
                    cx,
                    cy,
                    0f,
                    finalRadius
                )
                userEmailFilterPostAddBtn.visibility = View.VISIBLE
                anim.start()
            }
        }


    }

    @SuppressLint("NotifyDataSetChanged")
    private fun verileriAl() {
        database.collection("Users").whereEqualTo("useremail", selectedEmail)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Toast.makeText(
                        this,
                        exception.localizedMessage,
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    if (snapshot != null) {
                        if (!snapshot.isEmpty) {
                            val documents = snapshot.documents
                            for (document in documents) {
                                supportActionBar?.title = document.get("username") as String
                            }
                        } else {
                            supportActionBar?.title = selectedEmail
                        }
                    } else {
                        supportActionBar?.title = selectedEmail
                    }
                }
            }
        database.collection("Post").whereEqualTo("kullaniciemail", selectedEmail)
            .orderBy("tarih", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Toast.makeText(this, exception.localizedMessage, Toast.LENGTH_LONG).show()
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
                                    val indirilenPost =
                                        Post(
                                            kullaniciEmail,
                                            kullaniciYorum,
                                            gorselUrl,
                                            postId,
                                            kullaniciUID
                                        )
                                    postList.add(indirilenPost)
                                } catch (e: Exception) {
                                    try {

                                        val kullaniciEmail =
                                            document.get("kullaniciemail") as String
                                        val kullaniciYorum =
                                            document.get("kullaniciyorum") as String
                                        val gorselUrl = document.get("gorselurl") as String
                                        val postId = document.get("postId") as String
                                        val kullaniciUID = "M6OZguiPKVQs6Z2qfh9HCntoKQi2"
                                        val indirilenPost =
                                            Post(
                                                kullaniciEmail,
                                                kullaniciYorum,
                                                gorselUrl,
                                                postId,
                                                kullaniciUID
                                            )

                                        postList.add(indirilenPost)
                                    } catch (e: Exception) {
                                        val kullaniciEmail =
                                            document.get("kullaniciemail") as String
                                        val kullaniciYorum =
                                            document.get("kullaniciyorum") as String
                                        val gorselUrl = ""
                                        val postId = document.get("postId") as String
                                        val kullaniciUID = "M6OZguiPKVQs6Z2qfh9HCntoKQi2"
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

                            recyclerViewAdapter.notifyDataSetChanged()

                        }
                    }
                }
            }
    }


}