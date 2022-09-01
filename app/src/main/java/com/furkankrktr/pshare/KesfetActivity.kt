@file:Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.furkankrktr.pshare

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewAnimationUtils
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.furkankrktr.pshare.adapter.HaberRecyclerAdapter
import com.furkankrktr.pshare.databinding.ActivityKesfetBinding
import com.furkankrktr.pshare.model.Post
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.hypot

class KesfetActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var recyclerKesfetViewAdapter: HaberRecyclerAdapter
    private lateinit var guncelKullaniciEmail: String
    private lateinit var kesfetPostAddBtn: FloatingActionButton
    private lateinit var recyclerKesfetView: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var filteredList: ArrayList<Post>
    private var postList = ArrayList<Post>()

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityKesfetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()
        guncelKullaniciEmail = auth.currentUser!!.email.toString()

        supportActionBar?.title = "Keşfet"

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recyclerKesfetView = binding.recyclerKesfetView
        searchEditText = binding.searchEditText
        kesfetPostAddBtn = binding.kesfetPostAddBtn
        setupRecyclerView(postList)

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            @SuppressLint("DefaultLocale")
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                filteredList = ArrayList()
                if (p0.toString() != "") {
                    for (item in postList) {
                        if (item.kullaniciEmail.lowercase(Locale.getDefault())
                                .contains(p0.toString().lowercase(Locale.getDefault()))
                        ) {
                            filteredList.add(item)
                        }
                    }
                    setupRecyclerView(filteredList)
                } else {
                    setupRecyclerView(postList)
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
        kesfetPostAddBtn.setOnClickListener {
            val intent = Intent(this, FotografPaylasmaActivity::class.java)
            startActivity(intent)
        }

        if (kesfetPostAddBtn.visibility == View.INVISIBLE) {

            kesfetPostAddBtn.post {

                val cx = kesfetPostAddBtn.width / 2
                val cy = kesfetPostAddBtn.height / 2

                val finalRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()
                val anim = ViewAnimationUtils.createCircularReveal(
                    kesfetPostAddBtn,
                    cx,
                    cy,
                    0f,
                    finalRadius
                )
                kesfetPostAddBtn.visibility = View.VISIBLE
                anim.start()
            }
        }

        verileriAl()
    }

    private fun setupRecyclerView(list: ArrayList<Post>) {
        recyclerKesfetViewAdapter = HaberRecyclerAdapter(list)
        val layoutManager = LinearLayoutManager(this)
        recyclerKesfetView.layoutManager = layoutManager
        recyclerKesfetViewAdapter = HaberRecyclerAdapter(list)
        recyclerKesfetView.adapter = recyclerKesfetViewAdapter
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun verileriAl() {

        database.collection("Post").orderBy("tarih", Query.Direction.DESCENDING)
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

                            recyclerKesfetViewAdapter.notifyDataSetChanged()

                        }
                    }
                }
            }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.options_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.signOutBtn -> {

                auth.signOut()
                val intent = Intent(this, KullaniciActivity::class.java)
                startActivity(intent)
                finish()

            }
            R.id.webSiteButton -> {
                val intent = Intent(this, WebViewActivity::class.java)
                startActivity(intent)
            }
            R.id.homeBtn -> {
                val intent = Intent(this, HaberlerActivity::class.java)
                startActivity(intent)
                finish()
            }
            R.id.profile -> {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
            }

        }


        return super.onOptionsItemSelected(item)
    }


}