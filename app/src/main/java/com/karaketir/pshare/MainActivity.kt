package com.karaketir.pshare

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.FirebaseMessaging
import com.karaketir.pshare.adapter.PostRecyclerAdapter
import com.karaketir.pshare.databinding.ActivityMainBinding
import com.karaketir.pshare.model.Post
import com.karaketir.pshare.services.openLink


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
        val updateLayout = binding.updateLayout
        val recoveryLayout = binding.recoveryLayout
        val updateButton = binding.updateButton

        recyclerView.adapter = recyclerViewAdapter
        postAddButton = binding.addPostButton

        updateButton.setOnClickListener {
            openLink(
                "https://play.google.com/store/apps/details?id=com.karaketir.pshare", this
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission not granted, request it
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        database.collection("Keys").document("Version").get().addOnSuccessListener {
            val myVersion = BuildConfig.VERSION_CODE
            val latestVersion = it.get("key").toString().toInt()
            if (myVersion < latestVersion) {
                postAddButton.visibility = View.GONE
                recyclerView.visibility = View.GONE
                updateLayout.visibility = View.VISIBLE

                database.collection("Keys").document("Recovery").get().addOnSuccessListener { it2 ->
                    val recovery = it2.get("key") as Boolean
                    if (recovery) {
                        postAddButton.visibility = View.GONE
                        recyclerView.visibility = View.GONE
                        recoveryLayout.visibility = View.VISIBLE

                    } else {
                        postAddButton.visibility = View.VISIBLE
                        recyclerView.visibility = View.VISIBLE
                        recoveryLayout.visibility = View.GONE

                    }
                }


            } else {
                postAddButton.visibility = View.VISIBLE
                recyclerView.visibility = View.VISIBLE
                updateLayout.visibility = View.GONE

                database.collection("Keys").document("Recovery").get().addOnSuccessListener { it2 ->
                    val recovery = it2.get("key") as Boolean
                    if (recovery) {
                        postAddButton.visibility = View.GONE
                        recyclerView.visibility = View.GONE
                        recoveryLayout.visibility = View.VISIBLE

                    } else {
                        postAddButton.visibility = View.VISIBLE
                        recyclerView.visibility = View.VISIBLE
                        recoveryLayout.visibility = View.GONE

                    }
                }

            }
        }




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

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _: Boolean ->

        }

    override fun onResume() {
        super.onResume()
        invalidateOptionsMenu()  // Forces menu to be redrawn
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.secenekler_menusu, menu)


        return super.onCreateOptionsMenu(menu)


    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        getUserProfileUrl(object : UserProfileCallback {
            @SuppressLint("InflateParams")
            override fun onProfileUrlRetrieved(url: String) {
                val item = menu.findItem(R.id.profil)
                if (item.actionView == null) {
                    val inflater =
                        getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val actionView = inflater.inflate(R.layout.menu_item_custom_layout, null)
                    item.actionView = actionView
                }
                val actionView = item.actionView
                val menuImage = actionView?.findViewById<ImageView>(R.id.menu_image)
                if (menuImage != null) {
                    Glide.with(this@MainActivity).load(url).apply(RequestOptions().circleCrop())
                        .placeholder(R.drawable.baseline_account_circle_24) // Make sure this is a valid drawable
                        .error(R.drawable.baseline_cancel_24) // Make sure this is a valid drawable
                        .into(menuImage)
                    menuImage.setOnClickListener {
                        val intent = Intent(this@MainActivity, ProfileActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
        })
        return true
    }


    private fun getUserProfileUrl(callback: UserProfileCallback) {
        database.collection("User").document(auth.uid.toString()).get().addOnSuccessListener {
            val profileImageURL = it.get("profileImageURL") as String
            callback.onProfileUrlRetrieved(profileImageURL)
        }.addOnFailureListener {
            // Handle the error or provide a default URL
            callback.onProfileUrlRetrieved("default_url")
        }
    }

    interface UserProfileCallback {
        fun onProfileUrlRetrieved(url: String)
    }

}