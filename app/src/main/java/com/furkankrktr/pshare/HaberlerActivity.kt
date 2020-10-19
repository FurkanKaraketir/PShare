package com.furkankrktr.pshare

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.furkankrktr.pshare.adapter.HaberRecyclerAdapter
import com.furkankrktr.pshare.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_haberler.*

class HaberlerActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var recyclerViewAdapter: HaberRecyclerAdapter
    var postList = ArrayList<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_haberler)
        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                println(task.exception)
            }

            val token = task.result.token
            println(token)

        }

        FirebaseMessaging.getInstance().subscribeToTopic("users")
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    println(task.exception)
                }
            }
        verileriAl()

        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerViewAdapter = HaberRecyclerAdapter(postList)
        recyclerView.adapter = recyclerViewAdapter


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.secenekler_menusu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.fotograf_paylas) {

            val intent = Intent(this, FotografPaylasmaActivity::class.java)
            startActivity(intent)

        } else if (item.itemId == R.id.cikis_yap) {

            auth.signOut()
            val intent = Intent(this, KullaniciActivity::class.java)
            startActivity(intent)
            finish()

        }


        return super.onOptionsItemSelected(item)
    }


    fun verileriAl() {

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
                                val kullaniciEmail = document.get("kullaniciemail") as String
                                val kullaniciYorum = document.get("kullaniciyorum") as String
                                val gorselUrl = document.get("gorselurl") as String
                                val postId = document.get("postId") as String
                                val indirilenPost =
                                    Post(kullaniciEmail, kullaniciYorum, gorselUrl, postId)
                                postList.add(indirilenPost)


                            }

                            recyclerViewAdapter.notifyDataSetChanged()

                        }
                    }
                }
            }
    }


}