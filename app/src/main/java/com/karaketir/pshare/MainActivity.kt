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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.karaketir.pshare.adapter.PostRecyclerAdapter
import com.karaketir.pshare.databinding.ActivityMainBinding
import com.karaketir.pshare.model.Post
import com.karaketir.pshare.services.FirestoreRepository
import com.karaketir.pshare.services.openLink


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestoreRepository: FirestoreRepository
    private lateinit var postAddButton: FloatingActionButton
    private lateinit var recyclerViewAdapter: PostRecyclerAdapter
    private lateinit var recyclerView: RecyclerView
    private var idList = ArrayList<String>()
    private var postList = ArrayList<Post>()
    private var myBlockList = ArrayList<String>()
    private var blockedMe = ArrayList<String>()

    private var lastVisible: DocumentSnapshot? = null
    private val batchSize = 10

    override fun onStart() {
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
        firestoreRepository = FirestoreRepository(FirebaseFirestore.getInstance())

        recyclerView = binding.postRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(applicationContext)
        recyclerViewAdapter = PostRecyclerAdapter(postList)
        recyclerView.adapter = recyclerViewAdapter

        postAddButton = binding.addPostButton

        setupUpdateButton()
        checkPermissions()
        fetchData()

        postAddButton.setOnClickListener {
            val intent = Intent(this, AddPostActivity::class.java)
            startActivity(intent)
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1)) {
                    loadMorePosts()
                }
            }
        })
    }

    private fun setupUpdateButton() {
        binding.updateButton.setOnClickListener {
            openLink("https://play.google.com/store/apps/details?id=com.karaketir.pshare", this)
        }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun fetchData() {
        firestoreRepository.getFollowedUsers(auth.uid.toString()) { followedUsers ->
            idList.clear()
            idList.addAll(followedUsers)
            fetchBlockedUsers()
        }
    }

    private fun fetchBlockedUsers() {
        firestoreRepository.getBlockedUsers(auth.uid.toString()) { myBlocks, blocksMe ->
            myBlockList.clear()
            blockedMe.clear()
            myBlockList.addAll(myBlocks)
            blockedMe.addAll(blocksMe)
            fetchInitialPosts()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fetchInitialPosts() {
        firestoreRepository.getInitialPosts(batchSize) { posts, lastVisibleDoc ->
            postList.clear()
            postList.addAll(posts.filter { post ->
                post.postOwnerID in idList && post.postOwnerID !in myBlockList && post.postOwnerID !in blockedMe
            })
            lastVisible = lastVisibleDoc
            recyclerViewAdapter.notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadMorePosts() {
        lastVisible?.let {
            firestoreRepository.getMorePosts(it, batchSize) { posts, lastVisibleDoc ->
                val filteredPosts = posts.filter { post ->
                    post.postOwnerID in idList && post.postOwnerID !in myBlockList && post.postOwnerID !in blockedMe
                }
                postList.addAll(filteredPosts)
                lastVisible = lastVisibleDoc
                recyclerViewAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.cikis_yap -> {
                showSignOutDialog()
            }

            R.id.search -> {
                startActivity(Intent(this, ExploreActivity::class.java))
            }

            R.id.profil -> {
                startActivity(Intent(this, ProfileActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showSignOutDialog() {
        val signOutAlertDialog = AlertDialog.Builder(this@MainActivity)
        signOutAlertDialog.setTitle("Çıkış Yap")
        signOutAlertDialog.setMessage("Hesabınızdan Çıkış Yapmak İstediğinize Emin misiniz?")
        signOutAlertDialog.setPositiveButton("Çıkış") { _, _ ->
            signOut()
            finish()
        }
        signOutAlertDialog.setNegativeButton("İptal", null)
        signOutAlertDialog.show()
    }

    private fun signOut() {
        auth.signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _: Boolean -> }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.secenekler_menusu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        firestoreRepository.getUserProfileUrl(

            auth.uid.toString(), object : FirestoreRepository.UserProfileCallback {
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
                    menuImage?.let {
                        Glide.with(this@MainActivity).load(url).apply(RequestOptions().circleCrop())
                            .placeholder(R.drawable.baseline_account_circle_24)
                            .error(R.drawable.baseline_cancel_24).into(it)
                        it.setOnClickListener {
                            startActivity(Intent(this@MainActivity, ProfileActivity::class.java))
                        }
                    }
                }
            })
        return true
    }
}
