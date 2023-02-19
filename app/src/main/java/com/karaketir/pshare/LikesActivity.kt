package com.karaketir.pshare

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.karaketir.pshare.adapter.FollowRecyclerAdapter
import com.karaketir.pshare.databinding.ActivityLikesBinding
import com.karaketir.pshare.model.User
import com.karaketir.pshare.services.glide
import com.karaketir.pshare.services.openLink
import com.karaketir.pshare.services.placeHolderYap
import de.hdodenhof.circleimageview.CircleImageView

class LikesActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var selectedPostID: String
    private lateinit var selectedPostOwnerID: String
    private lateinit var selectedPostDescription: String
    private lateinit var selectedPostUserName: String
    private lateinit var selectedPostImageURL: String
    private lateinit var selectedPostImageView: ImageView
    private lateinit var profileImageView: CircleImageView
    private lateinit var recyclerCommentsView: RecyclerView
    private lateinit var commentToPostText: TextView
    private lateinit var commentToUserNameText: TextView
    private lateinit var likeCountTextLikeActivity: TextView
    private lateinit var recyclerCommentViewAdapter: FollowRecyclerAdapter
    private var userList = ArrayList<User>()
    private var myBlockList = ArrayList<String>()
    private var blockedMe = java.util.ArrayList<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLikesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()

        selectedPostID = intent.getStringExtra("postID").toString()

        selectedPostImageView = binding.selectedPostImageViewLike
        profileImageView = binding.profileImageLikeActivity
        recyclerCommentsView = binding.recyclerLikesView
        commentToPostText = binding.likeToPostText
        commentToUserNameText = binding.likeToUserNameText
        likeCountTextLikeActivity = binding.likeCountTextLikeActivity

        database.collection("Post").document(selectedPostID).get().addOnSuccessListener {
            selectedPostOwnerID = it.get("postOwnerID").toString()
            selectedPostDescription = it.get("postDescription").toString()
            database.collection("User").document(selectedPostOwnerID).get()
                .addOnSuccessListener { it2 ->
                    selectedPostUserName = it2.get("username").toString()
                    profileImageView.glide(
                        it2.get("profileImageURL").toString(), placeHolderYap(this)
                    )
                    selectedPostImageURL = it.get("postImageURL").toString()

                    if (selectedPostImageURL != "") {
                        selectedPostImageView.glide(selectedPostImageURL, placeHolderYap(this))
                    } else {
                        selectedPostImageView.visibility = View.GONE
                    }
                    commentToPostText.text = selectedPostDescription
                    commentToUserNameText.text = selectedPostUserName
                    selectedPostImageView.setOnClickListener {
                        openLink(selectedPostImageURL, this)
                    }
                    profileImageView.setOnClickListener {
                        openLink(it2.get("profileImageURL").toString(), this)
                    }
                    commentToUserNameText.setOnClickListener {
                        val newIntent = Intent(this, UserFilteredPostsActivity::class.java)
                        newIntent.putExtra("postOwnerID", selectedPostOwnerID)
                        startActivity(newIntent)
                    }

                }


        }


        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val layoutManager = LinearLayoutManager(this)

        recyclerCommentsView.layoutManager = layoutManager
        recyclerCommentViewAdapter = FollowRecyclerAdapter(userList)
        recyclerCommentsView.adapter = recyclerCommentViewAdapter


        database.collection("Blocks").whereEqualTo("main", auth.uid.toString())
            .addSnapshotListener { blockList, error ->
                if (blockList != null) {
                    myBlockList.clear()
                    for (id in blockList) {
                        myBlockList.add(id.get("blocksWho").toString())

                    }
                    database.collection("Blocks").whereEqualTo("blocksWho", auth.uid.toString())
                        .addSnapshotListener { blockMeList, _ ->
                            if (blockMeList != null) {
                                blockedMe.clear()
                                for (id2 in blockMeList) {
                                    blockedMe.add(id2.get("main").toString())

                                }

                            }
                            verileriAl()

                        }
                }
                if (error != null) {
                    println(error.localizedMessage)
                }
            }


    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    private fun verileriAl() {

        database.collection("Likes").whereEqualTo("postID", selectedPostID)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    println(error.localizedMessage)
                }
                userList.clear()
                if (value != null) {
                    for (userID in value) {

                        database.collection("User").document(userID.get("userID").toString()).get()
                            .addOnSuccessListener {
                                val newUser = User(
                                    it.id,
                                    it.get("username").toString(),
                                    it.get("email").toString(),
                                    it.get("profileImageURL").toString()

                                )

                                if (newUser.userID !in myBlockList && newUser.userID !in blockedMe) {
                                    userList.add(newUser)
                                }
                                likeCountTextLikeActivity.text = "${userList.size} Beğeni"
                                recyclerCommentViewAdapter.notifyDataSetChanged()


                            }


                    }


                }


            }
    }
}