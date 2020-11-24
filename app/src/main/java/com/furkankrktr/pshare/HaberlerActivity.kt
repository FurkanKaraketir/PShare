@file:Suppress("DEPRECATION")

package com.furkankrktr.pshare

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import com.furkankrktr.pshare.adapter.HaberRecyclerAdapter
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
import kotlinx.android.synthetic.main.activity_haberler.*


class HaberlerActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var postAddButton: FloatingActionButton
    private lateinit var recyclerViewAdapter: HaberRecyclerAdapter
    private var postList = ArrayList<Post>()
    private lateinit var apiService: APIService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_haberler)
        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()
        FirebaseInstanceId.getInstance().instanceId

        FirebaseMessaging.getInstance().subscribeToTopic("users")
        verileriAl()
        postAddButton = findViewById(R.id.postAddBtn)
        postAddButton.setOnClickListener {
            val intent = Intent(this, FotografPaylasmaActivity::class.java)
            startActivity(intent)
        }
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService::class.java)

        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerViewAdapter = HaberRecyclerAdapter(postList)
        recyclerView.adapter = recyclerViewAdapter

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

        updateToken()

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
                    Configuration.UI_MODE_NIGHT_YES ->{
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    }
                    Configuration.UI_MODE_NIGHT_NO ->{
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    }
                }
            }
            R.id.WebSite -> {
                val intent = Intent(this, WebViewActivity::class.java)
                startActivity(intent)
            }

        }


        return super.onOptionsItemSelected(item)
    }


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

                            recyclerViewAdapter.notifyDataSetChanged()

                        }
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