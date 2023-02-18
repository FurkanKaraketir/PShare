package com.karaketir.pshare

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.karaketir.pshare.adapter.BlockRecyclerAdapter
import com.karaketir.pshare.databinding.ActivityBlocksBinding
import com.karaketir.pshare.model.User

class BlocksActivity : AppCompatActivity() {

    private var userList = ArrayList<User>()
    private var blockList = ArrayList<String>()
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var recyclerUserView: RecyclerView
    private lateinit var recyclerUserViewAdapter: BlockRecyclerAdapter


    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityBlocksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()
        recyclerUserView = binding.blockRecyclerView

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportActionBar?.title = "Engellenen Hesaplar"

        val layoutManager = LinearLayoutManager(this)

        recyclerUserView.layoutManager = layoutManager
        recyclerUserViewAdapter = BlockRecyclerAdapter(userList)
        recyclerUserView.adapter = recyclerUserViewAdapter

        database.collection("Blocks").whereEqualTo("main", auth.uid.toString())
            .addSnapshotListener { value, _ ->
                if (value != null) {
                    blockList.clear()
                    for (id in value) {
                        blockList.add(id.get("blocksWho").toString())
                    }
                }
                userList.clear()
                for (i in blockList) {
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