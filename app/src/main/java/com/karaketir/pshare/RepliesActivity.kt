package com.karaketir.pshare

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewAnimationUtils
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.karaketir.pshare.adapter.ReplyRecyclerAdapter
import com.karaketir.pshare.databinding.ActivityRepliesBinding
import com.karaketir.pshare.model.Reply
import com.karaketir.pshare.services.glide
import com.karaketir.pshare.services.placeHolderYap
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.hypot

class RepliesActivity : AppCompatActivity() {

    private lateinit var selectedCommentID: String
    private lateinit var recyclerRepliesView: RecyclerView
    private lateinit var replySendButton: FloatingActionButton
    private lateinit var replyToEmailText: TextView
    private lateinit var replySendEditText: EditText
    private lateinit var profileImageReplyActivity: CircleImageView
    private lateinit var replyToCommentText: TextView
    private lateinit var selectedCommentOwnerID: String
    private lateinit var selectedCommentPostID: String

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var recyclerReplyViewAdapter: ReplyRecyclerAdapter
    private var replyList = ArrayList<Reply>()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityRepliesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerRepliesView = binding.recyclerRepliesView
        replySendButton = binding.replySendButton
        replyToEmailText = binding.replyToEmailText
        replySendEditText = binding.replySendEditText
        profileImageReplyActivity = binding.profileImageReplyActivity
        replyToCommentText = binding.replyToCommentText

        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()

        val layoutManager = LinearLayoutManager(this)
        recyclerRepliesView.layoutManager = layoutManager
        recyclerReplyViewAdapter = ReplyRecyclerAdapter(replyList)
        recyclerRepliesView.adapter = recyclerReplyViewAdapter
        selectedCommentID = intent.getStringExtra("commentID").toString()

        database.collection("Comments").document(selectedCommentID).get()
            .addOnSuccessListener { comment ->
                database.collection("User").document(comment.get("commentOwnerID").toString()).get()
                    .addOnSuccessListener {
                        selectedCommentPostID = comment.get("commentToPost").toString()
                        selectedCommentOwnerID = comment.get("commentOwnerID").toString()
                        replyToEmailText.text = it.get("username").toString()
                        profileImageReplyActivity.glide(
                            it.get("profileImageURL").toString(), placeHolderYap(this)
                        )
                        replyToCommentText.text = comment.get("comment").toString()

                    }
            }

        database.collection("Replies").whereEqualTo("replyToComment", selectedCommentID)
            .orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener { value, _ ->
                replyList.clear()
                if (value != null) {
                    for (reply in value) {
                        val newReply = Reply(
                            reply.get("reply").toString(),
                            reply.id,
                            reply.get("replyOwnerID").toString(),
                            reply.get("replyToComment").toString(),
                            reply.get("replyToPost").toString(),
                            reply.get("replyToWho").toString(),
                            reply.get("timestamp") as Timestamp
                        )

                        replyList.add(newReply)

                    }
                }
                recyclerReplyViewAdapter.notifyDataSetChanged()
            }



        replySendEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            @SuppressLint("RestrictedApi")
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {


            }

            @SuppressLint("RestrictedApi")
            override fun afterTextChanged(p0: Editable?) {
                if (p0.toString() != "" && replySendButton.visibility == View.INVISIBLE) {

                    val cx = replySendButton.width / 2
                    val cy = replySendButton.height / 2

                    // get the final radius for the clipping circle
                    val finalRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()

                    // create the animator for this view (the start radius is zero)
                    val anim = ViewAnimationUtils.createCircularReveal(
                        replySendButton, cx, cy, 0f, finalRadius
                    )
                    // make the view visible and start the animation
                    replySendButton.visibility = View.VISIBLE
                    anim.start()


                    // set the view to invisible without a circular reveal animation below Lollipop

                } else if (p0.toString() == "" && replySendButton.visibility == View.VISIBLE) {
                    val cx = replySendButton.width / 2
                    val cy = replySendButton.height / 2

                    // get the initial radius for the clipping circle
                    val initialRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()

                    // create the animation (the final radius is zero)
                    val anim = ViewAnimationUtils.createCircularReveal(
                        replySendButton, cx, cy, initialRadius, 0f
                    )

                    // make the view invisible when the animation is done
                    anim.addListener(object : AnimatorListenerAdapter() {

                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            replySendButton.visibility = View.INVISIBLE
                        }
                    })
                    anim.start()
                }


            }


        })

        var documentID = UUID.randomUUID().toString()

        replySendButton.setOnClickListener {
            val cal = Calendar.getInstance()
            if (replySendEditText.text.isNotEmpty()) {
                val data = hashMapOf(
                    "reply" to replySendEditText.text.toString(),
                    "replyID" to documentID,
                    "replyOwnerID" to auth.uid.toString(),
                    "replyToComment" to selectedCommentID,
                    "replyToPost" to selectedCommentPostID,
                    "replyToWho" to selectedCommentOwnerID,
                    "timestamp" to cal.time
                )

                database.collection("Replies").document(documentID).set(data).addOnSuccessListener {
                    replySendEditText.text.clear()
                    documentID = UUID.randomUUID().toString()
                }

            } else {
                replySendEditText.error = "Bu Alan Boş Bırakılamaz"
            }
        }

    }
}