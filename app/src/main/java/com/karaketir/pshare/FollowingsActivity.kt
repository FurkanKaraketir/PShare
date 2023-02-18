package com.karaketir.pshare

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.karaketir.pshare.adapter.FollowRecyclerAdapter
import com.karaketir.pshare.databinding.ActivityFollowingsBinding
import com.karaketir.pshare.model.User

class FollowingsActivity : AppCompatActivity() {

    private var userList = ArrayList<User>()
    private var takipEdilenlerList = ArrayList<String>()
    private var takipcilerList = ArrayList<String>()
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var recyclerUserView: RecyclerView
    private lateinit var recyclerUserViewAdapter: FollowRecyclerAdapter

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityFollowingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()
        recyclerUserView = binding.followRecyclerView

        val option = intent.getStringExtra("option")

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val layoutManager = LinearLayoutManager(this)

        supportActionBar?.title = option

        recyclerUserView.layoutManager = layoutManager
        recyclerUserViewAdapter = FollowRecyclerAdapter(userList)
        recyclerUserView.adapter = recyclerUserViewAdapter

        when (option) {
            "Takip Edilenler" -> {

                database.collection("Followings").whereEqualTo("main", auth.uid.toString())
                    .addSnapshotListener { value, _ ->
                        if (value != null) {
                            takipEdilenlerList.clear()
                            for (id in value) {

                                if (auth.uid.toString() != id.get("followsWho").toString()) {
                                    takipEdilenlerList.add(id.get("followsWho").toString())
                                }

                            }
                        }
                        userList.clear()
                        for (i in takipEdilenlerList) {
                            database.collection("User").document(i).get().addOnSuccessListener {

                                val newUser = User(
                                    i,
                                    it.get("username").toString(),
                                    it.get("email").toString(),
                                    it.get("profileImageURL").toString()
                                )
                                userList.add(newUser)
                                recyclerUserViewAdapter.notifyDataSetChanged()

                            }
                        }


                    }

            }

            "Takipçiler" -> {
                database.collection("Followings").whereEqualTo("followsWho", auth.uid.toString())
                    .addSnapshotListener { value, _ ->
                        if (value != null) {
                            takipcilerList.clear()
                            for (id in value) {

                                if (auth.uid.toString() != id.get("main").toString()) {
                                    takipcilerList.add(id.get("main").toString())
                                }

                            }
                        }
                        userList.clear()
                        for (i in takipcilerList) {
                            database.collection("User").document(i).get().addOnSuccessListener {

                                val newUser = User(
                                    i,
                                    it.get("username").toString(),
                                    it.get("email").toString(),
                                    it.get("profileImageURL").toString()
                                )
                                userList.add(newUser)
                                recyclerUserViewAdapter.notifyDataSetChanged()

                            }
                        }


                    }

            }
        }


    }
}