package com.karaketir.pshare

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewAnimationUtils
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.karaketir.pshare.adapter.PostRecyclerAdapter
import com.karaketir.pshare.databinding.ActivityExploreBinding
import com.karaketir.pshare.model.Post
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.hypot

class ExploreActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var recyclerKesfetViewAdapter: PostRecyclerAdapter
    private lateinit var guncelKullaniciEmail: String
    private lateinit var kesfetPostAddBtn: FloatingActionButton
    private lateinit var recyclerKesfetView: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var filteredList: ArrayList<Post>
    private var postList = ArrayList<Post>()
    private var myBlockList = ArrayList<String>()
    private var blockedMe = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityExploreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()
        guncelKullaniciEmail = auth.currentUser!!.email.toString()

        supportActionBar?.title = "Keşfet"

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recyclerKesfetView = binding.recyclerExploreView
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
                        database.collection("User").document(item.postOwnerID).get()
                            .addOnSuccessListener { user ->
                                val name = user.get("username").toString()
                                if (name.lowercase(Locale.getDefault())
                                        .contains(p0.toString().lowercase(Locale.getDefault()))
                                ) {
                                    filteredList.add(item)
                                }
                                setupRecyclerView(filteredList)


                            }

                    }
                } else {
                    setupRecyclerView(postList)
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
        kesfetPostAddBtn.setOnClickListener {
            val intent = Intent(this, AddPostActivity::class.java)
            startActivity(intent)
        }

        if (kesfetPostAddBtn.visibility == View.INVISIBLE) {

            kesfetPostAddBtn.post {

                val cx = kesfetPostAddBtn.width / 2
                val cy = kesfetPostAddBtn.height / 2

                val finalRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()
                val anim = ViewAnimationUtils.createCircularReveal(
                    kesfetPostAddBtn, cx, cy, 0f, finalRadius
                )
                kesfetPostAddBtn.visibility = View.VISIBLE
                anim.start()
            }
        }


        database.collection("Blocks").whereEqualTo("main", auth.uid.toString())
            .addSnapshotListener { blockList, error ->
                if (blockList != null) {
                    myBlockList.clear()
                    for (id in blockList) {
                        myBlockList.add(id.get("blocksWho").toString())

                    }
                    database.collection("Blocks").whereEqualTo("blocksWho", auth.uid.toString())
                        .addSnapshotListener { blockMeList, _ ->
                            if (blockMeList != null) {
                                blockedMe.clear()
                                for (id2 in blockMeList) {
                                    blockedMe.add(id2.get("main").toString())

                                }

                            }
                            verileriAl()

                        }

                }
                if (error != null) {
                    println(error.localizedMessage)
                }
            }

    }

    private fun setupRecyclerView(list: ArrayList<Post>) {
        val layoutManager = LinearLayoutManager(this)
        recyclerKesfetView.layoutManager = layoutManager
        recyclerKesfetViewAdapter = PostRecyclerAdapter(list)
        recyclerKesfetView.adapter = recyclerKesfetViewAdapter
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun verileriAl() {

        postList.clear()
        database.collection("Post").orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { posts, error ->

                if (error != null) {
                    println(error.localizedMessage)
                }
                postList.clear()

                if (posts != null && !posts.isEmpty) {

                    for (post in posts) {

                        val newPost = Post(
                            post.get("postID").toString(),
                            post.get("postDescription").toString(),
                            post.get("postImageURL").toString(),
                            post.get("postOwnerID").toString(),
                            post.get("timestamp") as Timestamp
                        )

                        if (newPost.postOwnerID !in myBlockList && newPost.postOwnerID !in blockedMe) {
                            postList.add(newPost)

                        }

                    }


                }
                recyclerKesfetViewAdapter.notifyDataSetChanged()
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

                val signOutAlertDialog = AlertDialog.Builder(this@ExploreActivity)
                signOutAlertDialog.setTitle("Çıkış Yap")
                signOutAlertDialog.setMessage("Hesabınızdan Çıkış Yapmak İstediğinize Emin misiniz?")
                signOutAlertDialog.setPositiveButton("Çıkış") { _, _ ->
                    signOut()
                    finish()
                }
                signOutAlertDialog.setNegativeButton("İptal") { _, _ ->

                }
                signOutAlertDialog.show()
            }

            R.id.homeBtn -> {
                val intent = Intent(this, MainActivity::class.java)
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

    private fun signOut() {
        auth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        this.startActivity(intent)
        finish()
    }
}