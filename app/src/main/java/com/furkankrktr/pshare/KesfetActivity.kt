package com.furkankrktr.pshare

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.furkankrktr.pshare.adapter.HaberRecyclerAdapter
import com.furkankrktr.pshare.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_kesfet.*

class KesfetActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var recyclerKesfetViewAdapter: HaberRecyclerAdapter
    private lateinit var guncelKullaniciEmail: String
    private var postList = ArrayList<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kesfet)
        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()
        guncelKullaniciEmail = auth.currentUser!!.email.toString()
        supportActionBar?.title = "Keşfet"

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val layoutManager = LinearLayoutManager(this)
        recyclerKesfetView.layoutManager = layoutManager
        recyclerKesfetViewAdapter = HaberRecyclerAdapter(postList)
        recyclerKesfetView.adapter = recyclerKesfetViewAdapter

        verileriAl()
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

                            recyclerKesfetViewAdapter.notifyDataSetChanged()

                        }
                    }
                }
            }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
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
            R.id.themeChange -> {
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
            R.id.webSiteButton -> {
                val intent = Intent(this, WebViewActivity::class.java)
                startActivity(intent)
            }
            R.id.homeBtn -> {
                val intent = Intent(this, HaberlerActivity::class.java)
                startActivity(intent)
                finish()
            }

        }


        return super.onOptionsItemSelected(item)
    }


}