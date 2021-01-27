@file:Suppress("DEPRECATION", "UNCHECKED_CAST")

package com.furkankrktr.pshare

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewAnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.furkankrktr.pshare.adapter.HaberRecyclerAdapter
import com.furkankrktr.pshare.databinding.ActivityHaberlerBinding
import com.furkankrktr.pshare.model.Post
import com.furkankrktr.pshare.send_notification_pack.APIService
import com.furkankrktr.pshare.send_notification_pack.Client
import com.furkankrktr.pshare.send_notification_pack.Token
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import kotlin.math.hypot


class HaberlerActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var postAddButton: FloatingActionButton
    private lateinit var recyclerViewAdapter: HaberRecyclerAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var guncelKullaniciEmail: String
    private lateinit var theme: String
    private lateinit var takipArray: ArrayList<String>
    private var postList = ArrayList<Post>()
    private lateinit var apiService: APIService

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityHaberlerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()
        FirebaseInstanceId.getInstance().instanceId
        guncelKullaniciEmail = auth.currentUser!!.email.toString()


        FirebaseMessaging.getInstance().subscribeToTopic("users")
        postAddButton = binding.postAddButton
        postAddButton.setOnClickListener {
            val intent = Intent(this, FotografPaylasmaActivity::class.java)
            startActivity(intent)
        }

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService::class.java)

        recyclerView = binding.recyclerView
        val layoutManager = LinearLayoutManager(applicationContext)
        recyclerView.layoutManager = layoutManager
        recyclerViewAdapter = HaberRecyclerAdapter(postList)
        recyclerView.adapter = recyclerViewAdapter

        updateToken()
        verileriAl()

        if (postAddButton.visibility == View.INVISIBLE) {

            postAddButton.post {

                val cx = postAddButton.width / 2
                val cy = postAddButton.height / 2

                val finalRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()
                val anim = ViewAnimationUtils.createCircularReveal(
                    postAddButton,
                    cx,
                    cy,
                    0f,
                    finalRadius
                )
                postAddButton.visibility = View.VISIBLE
                anim.start()
            }
        }


    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.secenekler_menusu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.cikis_yap -> {

                auth.signOut()
                val intent = Intent(this, KullaniciActivity::class.java)
                startActivity(intent)
                finish()

            }
            R.id.temaDegistir -> {
                when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        database.collection("Users").whereEqualTo("useremail", guncelKullaniciEmail)
                            .addSnapshotListener { snapshot, exception ->
                                if (exception == null) {

                                    if (snapshot != null) {
                                        if (!snapshot.isEmpty) {
                                            val documents = snapshot.documents
                                            for (document in documents) {
                                                database.collection("Users")
                                                    .document(document.id)
                                                    .update("theme", "light")
                                            }
                                        }
                                    }
                                }
                            }
                    }
                    Configuration.UI_MODE_NIGHT_NO -> {
                        database.collection("Users").whereEqualTo("useremail", guncelKullaniciEmail)
                            .addSnapshotListener { snapshot, exception ->
                                if (exception == null) {

                                    if (snapshot != null) {
                                        if (!snapshot.isEmpty) {
                                            val documents = snapshot.documents
                                            for (document in documents) {
                                                database.collection("Users")
                                                    .document(document.id)
                                                    .update("theme", "dark")
                                            }
                                        }
                                    }
                                }
                            }
                    }
                }
            }
            R.id.WebSite -> {
                val intent = Intent(this, WebViewActivity::class.java)
                intent.putExtra("link", "https://furkankrktr.wordpress.com/")
                startActivity(intent)
            }
            R.id.search -> {
                val intent = Intent(this, KesfetActivity::class.java)
                startActivity(intent)
            }
            R.id.profil -> {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
            }

        }


        return super.onOptionsItemSelected(item)
    }


    private fun verileriAl() {
        database.collection("Users").whereEqualTo("useremail", guncelKullaniciEmail)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    theme = "dark"
                } else {
                    if (snapshot != null) {
                        if (!snapshot.isEmpty) {
                            val documents = snapshot.documents
                            for (document in documents) {
                                theme = document.get("theme") as String
                                takipArray =
                                    document.get("takipEdilenEmailler") as ArrayList<String>


                                database.collection("Post")
                                    .whereIn("kullaniciemail", takipArray)
                                    .orderBy("tarih", Query.Direction.DESCENDING)
                                    .addSnapshotListener { snapshot2, exception2 ->
                                        if (exception2 != null) {
                                            println(exception2)
                                        } else {
                                            if (snapshot2 != null) {
                                                if (!snapshot2.isEmpty) {
                                                    val documents2 = snapshot2.documents

                                                    postList.clear()

                                                    for (document2 in documents2) {
                                                        try {
                                                            val kullaniciEmail =
                                                                document2.get("kullaniciemail") as String
                                                            val kullaniciYorum =
                                                                document2.get("kullaniciyorum") as String
                                                            val gorselUrl =
                                                                document2.get("gorselurl") as String
                                                            val postId =
                                                                document2.get("postId") as String
                                                            val kullaniciUID =
                                                                document2.get("userID") as String
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
                                                                    document2.get("kullaniciemail") as String
                                                                val kullaniciYorum =
                                                                    document2.get("kullaniciyorum") as String
                                                                val gorselUrl =
                                                                    document2.get("gorselurl") as String
                                                                val postId =
                                                                    document2.get("postId") as String
                                                                val kullaniciUID =
                                                                    "M6OZguiPKVQs6Z2qfh9HCntoKQi2"
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
                                                                    document2.get("kullaniciemail") as String
                                                                val kullaniciYorum =
                                                                    document2.get("kullaniciyorum") as String
                                                                val gorselUrl = ""
                                                                val postId =
                                                                    document2.get("postId") as String
                                                                val kullaniciUID =
                                                                    "M6OZguiPKVQs6Z2qfh9HCntoKQi2"
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


                                if (theme == "dark") {
                                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                                } else if (theme == "light") {
                                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                                }
                            }
                        }
                    }
                }
            }
        database.collection("Users").whereEqualTo("useremail", auth.currentUser!!.email.toString())
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Toast.makeText(
                        this,
                        exception.localizedMessage,
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    if (snapshot != null) {
                        if (snapshot.isEmpty) {
                            val intent = Intent(this, AddUserNameActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        val intent = Intent(this, AddUserNameActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }


    }


    private fun updateToken() {
        val refreshToken: String = FirebaseInstanceId.getInstance().token.toString()
        val token = Token(refreshToken)
        FirebaseDatabase.getInstance().getReference("Tokens")
            .child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(token)
    }


}