package com.karaketir.pshare

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.FirebaseMessaging
import com.karaketir.pshare.adapter.PostRecyclerAdapter
import com.karaketir.pshare.databinding.ActivityMainBinding
import com.karaketir.pshare.model.Post

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var postAddButton: FloatingActionButton
    private lateinit var recyclerViewAdapter: PostRecyclerAdapter
    private lateinit var recyclerView: RecyclerView
    private var idList = ArrayList<String>()
    private var postList = ArrayList<Post>()
    private var myBlockList = ArrayList<String>()
    private var blockedMe = ArrayList<String>()

    public override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser
        if (currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            this.startActivity(intent)
            finish()
        } else {
            FirebaseMessaging.getInstance().subscribeToTopic("allUsers")
            FirebaseMessaging.getInstance().subscribeToTopic(auth.uid.toString())
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()

        recyclerView = binding.postRecyclerView
        val layoutManager = LinearLayoutManager(applicationContext)
        recyclerView.layoutManager = layoutManager
        recyclerViewAdapter = PostRecyclerAdapter(postList)




        recyclerView.adapter = recyclerViewAdapter
        postAddButton = binding.addPostButton

        database.collection("Followings").whereEqualTo("main", auth.uid.toString())
            .addSnapshotListener { followList, error ->
                if (followList != null) {
                    idList.clear()
                    for (id in followList) {
                        idList.add(id.get("followsWho").toString())

                    }


                }
                database.collection("Blocks").whereEqualTo("main", auth.uid.toString())
                    .addSnapshotListener { blockList, _ ->
                        if (blockList != null) {
                            myBlockList.clear()
                            for (id in blockList) {
                                myBlockList.add(id.get("blocksWho").toString())

                            }

                        }
                        database.collection("Blocks").whereEqualTo("blocksWho", auth.uid.toString())
                            .addSnapshotListener { blockMeList, _ ->
                                if (blockMeList != null) {
                                    blockedMe.clear()
                                    for (id2 in blockMeList) {
                                        blockedMe.add(id2.get("main").toString())

                                    }

                                }
                                getData()

                            }

                    }
                if (error != null) {
                    println(error.localizedMessage)
                }
            }

        postAddButton.setOnClickListener {
            val intent = Intent(this, AddPostActivity::class.java)
            startActivity(intent)
        }


    }


    @SuppressLint("NotifyDataSetChanged")
    private fun getData() {
        database.collection("Post").orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { posts, error2 ->
                postList.clear()
                if (error2 != null) {
                    println(error2.localizedMessage)
                }
                if (posts != null) {
                    for (post in posts) {

                        val newPost = Post(
                            post.get("postID").toString(),
                            post.get("postDescription").toString(),
                            post.get("postImageURL").toString(),
                            post.get("postOwnerID").toString(),
                            post.get("timestamp") as Timestamp
                        )

                        if (newPost.postOwnerID in idList) {
                            if (newPost.postOwnerID !in myBlockList && newPost.postOwnerID !in blockedMe) {
                                postList.add(newPost)

                            }

                        }


                    }
                    recyclerViewAdapter.notifyDataSetChanged()
                }


            }

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.secenekler_menusu, menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.cikis_yap -> {

                val signOutAlertDialog = AlertDialog.Builder(this@MainActivity)
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
            R.id.search -> {
                val intent = Intent(this, ExploreActivity::class.java)
                startActivity(intent)
            }
            R.id.profil -> {
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