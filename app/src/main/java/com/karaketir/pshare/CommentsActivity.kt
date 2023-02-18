package com.karaketir.pshare

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewAnimationUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.karaketir.pshare.adapter.CommentRecyclerAdapter
import com.karaketir.pshare.databinding.ActivityCommentsBinding
import com.karaketir.pshare.model.Comment
import com.karaketir.pshare.services.FcmNotificationsSenderService
import com.karaketir.pshare.services.glide
import com.karaketir.pshare.services.openLink
import com.karaketir.pshare.services.placeHolderYap
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*
import kotlin.math.hypot

class CommentsActivity : AppCompatActivity() {

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
    private lateinit var sendButton: FloatingActionButton
    private lateinit var commentSendEditText: EditText
    private lateinit var commentToPostText: TextView
    private lateinit var commentToUserNameText: TextView
    private lateinit var recyclerCommentViewAdapter: CommentRecyclerAdapter
    private var commentList = ArrayList<Comment>()
    private var myBlockList = ArrayList<String>()
    private var blockedMe = ArrayList<String>()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityCommentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        selectedPostImageView = binding.selectedPostImageView
        profileImageView = binding.profileImageCommentActivity
        recyclerCommentsView = binding.recyclerCommentsView
        sendButton = binding.sendCButton
        commentSendEditText = binding.commentSendEditText
        commentToPostText = binding.commentToPostText
        commentToUserNameText = binding.commentToEmailText

        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()

        selectedPostID = intent.getStringExtra("postID").toString()

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
                }


        }


        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val layoutManager = LinearLayoutManager(this)

        recyclerCommentsView.layoutManager = layoutManager
        recyclerCommentViewAdapter = CommentRecyclerAdapter(commentList)
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



        commentSendEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            @SuppressLint("RestrictedApi")
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {


            }

            @SuppressLint("RestrictedApi")
            override fun afterTextChanged(p0: Editable?) {
                if (p0.toString() != "" && sendButton.visibility == View.INVISIBLE) {

                    val cx = sendButton.width / 2
                    val cy = sendButton.height / 2

                    // get the final radius for the clipping circle
                    val finalRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()

                    // create the animator for this view (the start radius is zero)
                    val anim =
                        ViewAnimationUtils.createCircularReveal(sendButton, cx, cy, 0f, finalRadius)
                    // make the view visible and start the animation
                    sendButton.visibility = View.VISIBLE
                    anim.start()


                    // set the view to invisible without a circular reveal animation below Lollipop

                } else if (p0.toString() == "" && sendButton.visibility == View.VISIBLE) {
                    val cx = sendButton.width / 2
                    val cy = sendButton.height / 2

                    // get the initial radius for the clipping circle
                    val initialRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()

                    // create the animation (the final radius is zero)
                    val anim = ViewAnimationUtils.createCircularReveal(
                        sendButton, cx, cy, initialRadius, 0f
                    )

                    // make the view invisible when the animation is done
                    anim.addListener(object : AnimatorListenerAdapter() {

                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            sendButton.visibility = View.INVISIBLE
                        }
                    })
                    anim.start()
                }


            }


        })
        var documentID = UUID.randomUUID().toString()


        sendButton.setOnClickListener {

            val cal = Calendar.getInstance()
            if (commentSendEditText.text.isNotEmpty()) {
                val data = hashMapOf(
                    "comment" to commentSendEditText.text.toString(),
                    "commentID" to documentID,
                    "commentOwnerID" to auth.uid.toString(),
                    "commentToPost" to selectedPostID,
                    "commentToWho" to selectedPostOwnerID,
                    "timestamp" to cal.time
                )

                database.collection("Comments").document(documentID).set(data)
                    .addOnSuccessListener {
                        documentID = UUID.randomUUID().toString()

                        if (auth.uid.toString() != selectedPostOwnerID) {
                            val notificationsSender = FcmNotificationsSenderService(
                                "/topics/$selectedPostOwnerID",
                                "Yeni Yorum",
                                "Yeni Yorumunuz Var \n${commentSendEditText.text}",
                                this
                            )
                            notificationsSender.sendNotifications()
                        }


                        commentSendEditText.text.clear()
                    }

            } else {
                commentSendEditText.error = "Bu Alan Boş Bırakılamaz"
            }

        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun verileriAl() {
        database.collection("Comments").whereEqualTo("commentToPost", selectedPostID)
            .orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener { value, error ->
                if (error != null) {
                    println(error.localizedMessage)
                }
                commentList.clear()
                if (value != null) {
                    for (comment in value) {
                        val newComment = Comment(
                            comment.get("comment").toString(),
                            comment.id,
                            comment.get("commentOwnerID").toString(),
                            comment.get("commentToPost").toString(),
                            comment.get("commentToWho").toString(),
                            comment.get("timestamp") as Timestamp

                        )

                        if (newComment.commentOwnerID !in myBlockList && newComment.commentOwnerID !in blockedMe) {
                            commentList.add(newComment)
                        }

                    }
                }
                recyclerCommentViewAdapter.notifyDataSetChanged()
            }
    }
}