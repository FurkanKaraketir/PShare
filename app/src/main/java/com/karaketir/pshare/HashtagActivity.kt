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
import com.karaketir.pshare.databinding.ActivityHashtagBinding
import com.karaketir.pshare.model.Post

class HashtagActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var recyclerViewAdapter: PostRecyclerAdapter
    private lateinit var hashtagPostAddBtn: FloatingActionButton
    private lateinit var selectedHashtag: String
    private lateinit var recyclerView: RecyclerView

    private var postList = ArrayList<Post>()


    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityHashtagBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        selectedHashtag = intent.getStringExtra("selectedHashtag").toString()

        supportActionBar?.title = selectedHashtag

        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()
        recyclerView = binding.recyclerView
        hashtagPostAddBtn = binding.hashtagPostAddBtn
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerViewAdapter = PostRecyclerAdapter(postList)
        recyclerView.adapter = recyclerViewAdapter

        hashtagPostAddBtn.setOnClickListener {
            val intent = Intent(this, AddPostActivity::class.java)
            startActivity(intent)
        }

        database.collection("Post").orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { value, _ ->
                if (value != null) {
                    for (post in value) {
                        val newPost = Post(
                            post.id,
                            post.get("postDescription").toString(),
                            post.get("postImageURL").toString(),
                            post.get("postOwnerID").toString(),
                            post.get("timestamp") as Timestamp
                        )

                        if (newPost.postDescription.contains(selectedHashtag)) {
                            postList.add(newPost)
                        }
                    }
                }
                recyclerViewAdapter.notifyDataSetChanged()
            }

    }
}