package com.karaketir.pshare

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.karaketir.pshare.adapter.PostRecyclerAdapter
import com.karaketir.pshare.databinding.ActivityUserFilteredPostsBinding
import com.karaketir.pshare.model.Post

class UserFilteredPostsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var recyclerViewAdapter: PostRecyclerAdapter
    private lateinit var selectedPostOwnerID: String
    private lateinit var userEmailFilterPostAddBtn: FloatingActionButton
    private lateinit var recyclerView: RecyclerView

    private var postList = ArrayList<Post>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityUserFilteredPostsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView = binding.recyclerViewUserFilter
        userEmailFilterPostAddBtn = binding.userEmailFilterPostAddBtn
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        selectedPostOwnerID = intent.getStringExtra("postOwnerID").toString()
        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()


        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerViewAdapter = PostRecyclerAdapter(postList)
        recyclerView.adapter = recyclerViewAdapter

        database.collection("User").document(selectedPostOwnerID).get().addOnSuccessListener {
            supportActionBar?.title = it.get("username").toString()

        }


        userEmailFilterPostAddBtn.setOnClickListener {
            val intent = Intent(this, AddPostActivity::class.java)
            startActivity(intent)
        }


        verileriAl()


    }

    @SuppressLint("NotifyDataSetChanged")
    private fun verileriAl() {
        database.collection("Post").whereEqualTo("postOwnerID", selectedPostOwnerID)
            .orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener { value, _ ->
                if (value != null) {
                    for (post in value) {
                        val newPost = Post(
                            post.id,
                            post.get("postDescription").toString(),
                            post.get("postImageURL").toString(),
                            post.get("postOwnerID").toString(),
                            post.get("timestamp") as Timestamp
                        )

                        postList.add(newPost)


                    }
                }
                recyclerViewAdapter.notifyDataSetChanged()
            }
    }
}