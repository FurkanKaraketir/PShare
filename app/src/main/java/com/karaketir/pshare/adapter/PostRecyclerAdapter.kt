package com.karaketir.pshare.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.karaketir.pshare.CommentsActivity
import com.karaketir.pshare.HashtagActivity
import com.karaketir.pshare.LikesActivity
import com.karaketir.pshare.R
import com.karaketir.pshare.UserFilteredPostsActivity
import com.karaketir.pshare.databinding.PostRowBinding
import com.karaketir.pshare.model.Post
import com.karaketir.pshare.services.FcmNotificationsSenderService
import com.karaketir.pshare.services.getRelativeTime
import com.karaketir.pshare.services.glide
import com.karaketir.pshare.services.glideCircle
import com.karaketir.pshare.services.openLink
import com.karaketir.pshare.services.placeHolderYap
import java.util.UUID

class PostRecyclerAdapter(
    private val postList: ArrayList<Post>
) : RecyclerView.Adapter<PostRecyclerAdapter.PostHolder>() {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    class PostHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = PostRowBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.post_row, parent, false)
        return PostHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: PostHolder, position: Int) {
        if (position !in 0 until postList.size) return

        val myItem = postList[position]
        val myBinding = holder.binding

        db.collection("User").document(myItem.postOwnerID).get().addOnSuccessListener {
            myBinding.userName.text = it.getString("username")
            val profileImageURL = it.getString("profileImageURL") ?: ""
            myBinding.profileImage.glideCircle(
                profileImageURL, placeHolderYap(holder.itemView.context)
            )
        }

        myBinding.postDate.text = getRelativeTime(myItem.timestamp)
        myBinding.postDescription.text = myItem.postDescription

        if (myItem.postImageURL.isNotEmpty()) {
            myBinding.postImage.visibility = View.VISIBLE
            myBinding.postImage.glide(myItem.postImageURL, placeHolderYap(holder.itemView.context))
        } else {
            myBinding.postImage.visibility = View.GONE
        }

        setupUserSpecificViews(holder, myItem)
        setupLikeButton(holder, myItem)
        setupCommentButton(holder, myItem)
        setupHashtagClick(myBinding.postDescription, myItem.postDescription)

        myBinding.moreOptionsPost.setOnClickListener {
            showPopupMenu(holder, myItem)
        }

        myBinding.postImage.setOnClickListener {
            openLink(myItem.postImageURL, holder.itemView.context)
        }
        myBinding.profileImage.setOnClickListener {
            openLink(myItem.postImageURL, holder.itemView.context)
        }
        myBinding.userName.setOnClickListener {
            val intent = Intent(holder.itemView.context, UserFilteredPostsActivity::class.java)
            intent.putExtra("postOwnerID", myItem.postOwnerID)
            holder.itemView.context.startActivity(intent)
        }
    }

    private fun setupUserSpecificViews(holder: PostHolder, myItem: Post) {
        val myBinding = holder.binding

        if (myItem.postOwnerID != auth.uid) {
            myBinding.deleteButton.visibility = View.GONE
            myBinding.moreOptionsPost.visibility = View.VISIBLE

            db.collection("Followings").whereEqualTo("main", auth.uid)
                .addSnapshotListener { followIDs, _ ->
                    followIDs?.let {
                        val followList = it.map { doc -> doc.getString("followsWho").orEmpty() }
                        if (myItem.postOwnerID in followList) {
                            myBinding.followButton.visibility = View.GONE
                            myBinding.unFollowButton.visibility = View.VISIBLE
                        } else {
                            myBinding.followButton.visibility = View.VISIBLE
                            myBinding.unFollowButton.visibility = View.GONE
                        }
                    }
                }
        } else {
            myBinding.deleteButton.visibility = View.VISIBLE
            myBinding.moreOptionsPost.visibility = View.GONE
            myBinding.followButton.visibility = View.GONE
            myBinding.unFollowButton.visibility = View.GONE
        }

        myBinding.followButton.setOnClickListener {
            showAlertDialog(holder, "Takip Et", "Takip Etmek İstedğinizden Emin misiniz?") {
                val documentID = UUID.randomUUID().toString()
                val data = hashMapOf("main" to auth.uid, "followsWho" to myItem.postOwnerID)
                db.collection("Followings").document(documentID).set(data)
            }
        }

        myBinding.unFollowButton.setOnClickListener {
            showAlertDialog(holder, "Takibi Bırak", "Takibi Bırakmak İstediğinize Emin misiniz?") {
                db.collection("Followings").whereEqualTo("main", auth.uid)
                    .whereEqualTo("followsWho", myItem.postOwnerID).get()
                    .addOnSuccessListener { value ->
                        value?.forEach { doc ->
                            db.collection("Followings").document(doc.id).delete()
                        }
                    }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupLikeButton(holder: PostHolder, myItem: Post) {
        val myBinding = holder.binding
        var likedID = ""

        db.collection("Likes").whereEqualTo("postID", myItem.postID)
            .whereEqualTo("userID", auth.uid).addSnapshotListener { value, _ ->
                value?.let {
                    if (it.isEmpty) {
                        myBinding.likedButton.visibility = View.GONE
                        myBinding.unlikedButton.visibility = View.VISIBLE
                    } else {
                        myBinding.likedButton.visibility = View.VISIBLE
                        myBinding.unlikedButton.visibility = View.GONE
                        likedID = it.documents.first().id
                    }
                }
            }

        db.collection("Likes").whereEqualTo("postID", myItem.postID)
            .addSnapshotListener { value, _ ->
                myBinding.likeCountText.text = "${value?.size() ?: 0} Beğeni"
            }

        myBinding.likeCountText.setOnClickListener {
            val intent = Intent(holder.itemView.context, LikesActivity::class.java)
            intent.putExtra("postID", myItem.postID)
            holder.itemView.context.startActivity(intent)
        }

        myBinding.likedButton.setOnClickListener {
            db.collection("Likes").document(likedID).delete()
        }
        myBinding.unlikedButton.setOnClickListener {
            val documentName = UUID.randomUUID().toString()
            val data = hashMapOf("userID" to auth.uid, "postID" to myItem.postID)
            db.collection("Likes").document(documentName).set(data).addOnSuccessListener {
                if (auth.uid != myItem.postOwnerID) {
                    val notificationsSender = FcmNotificationsSenderService(
                        "/topics/${myItem.postOwnerID}",
                        "Yeni Beğeni",
                        "Yeni Beğeniniz Var \n${myItem.postDescription}",
                        holder.itemView.context
                    )
                    notificationsSender.sendNotifications()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupCommentButton(holder: PostHolder, myItem: Post) {
        val myBinding = holder.binding

        db.collection("Comments").whereEqualTo("commentToPost", myItem.postID)
            .addSnapshotListener { value, _ ->
                myBinding.commentCountText.text = "${value?.size() ?: 0} Yorum"
            }

        myBinding.commentsButton.setOnClickListener {
            commentGit(holder, myItem)
        }
        myBinding.commentCountText.setOnClickListener {
            commentGit(holder, myItem)
        }

        myBinding.deleteButton.setOnClickListener {
            showAlertDialog(holder, "Postu Sil", "Postu Silmek İstediğinize Emin misiniz?") {
                deletePost(myItem)
            }
        }
    }

    private fun setupHashtagClick(textView: TextView, description: String) {
        val spannableString = SpannableString(description)
        val hashtags = findHashTags(description)

        hashtags.forEach { hashtag ->
            val start = description.indexOf(hashtag)
            val end = start + hashtag.length

            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    val intent = Intent(widget.context, HashtagActivity::class.java)
                    intent.putExtra("selectedHashtag", hashtag)
                    widget.context.startActivity(intent)
                }
            }

            spannableString.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        textView.text = spannableString
        textView.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun showPopupMenu(holder: PostHolder, myItem: Post) {
        val myBinding = holder.binding
        val popup = PopupMenu(holder.itemView.context, myBinding.moreOptionsPost)
        popup.inflate(R.menu.post_options_menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.blockButton -> {
                    showAlertDialog(holder, "Engelle", "Engellemek İstedğinizden Emin misiniz?") {
                        blockUser(myItem.postOwnerID, holder)
                    }
                    true
                }

                else -> false
            }
        }
        popup.show()
    }

    private fun showAlertDialog(
        holder: PostHolder, title: String, message: String, onPositive: () -> Unit
    ) {
        AlertDialog.Builder(holder.itemView.context).apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton(title) { _, _ -> onPositive() }
            setNegativeButton("İptal", null)
        }.show()
    }

    private fun blockUser(userId: String, holder: PostHolder) {
        val documentName = UUID.randomUUID().toString()
        val data = hashMapOf("main" to auth.uid, "blocksWho" to userId)

        db.collection("Blocks").document(documentName).set(data)

        db.collection("Followings").whereEqualTo("main", auth.uid)
            .whereEqualTo("followsWho", userId).get().addOnSuccessListener { value ->
                value?.forEach { doc ->
                    db.collection("Followings").document(doc.id).delete()
                }
                db.collection("Followings").whereEqualTo("followsWho", auth.uid)
                    .whereEqualTo("main", userId).get().addOnSuccessListener { value2 ->
                        value2?.forEach { doc ->
                            db.collection("Followings").document(doc.id).delete()
                        }
                    }
            }
        Toast.makeText(holder.itemView.context, "Engellendi", Toast.LENGTH_SHORT).show()
    }

    private fun deletePost(myItem: Post) {
        db.collection("Replies").whereEqualTo("replyToPost", myItem.postID).get()
            .addOnSuccessListener { value1 ->
                value1?.forEach { doc ->
                    db.collection("Replies").document(doc.id).delete()
                }
                db.collection("Comments").whereEqualTo("commentToPost", myItem.postID).get()
                    .addOnSuccessListener { value2 ->
                        value2?.forEach { doc ->
                            db.collection("Comments").document(doc.id).delete()
                        }
                        db.collection("Post").document(myItem.postID).delete()
                        db.collection("Likes").whereEqualTo("postID", myItem.postID).get()
                            .addOnSuccessListener { value3 ->
                                value3?.forEach { doc ->
                                    db.collection("Likes").document(doc.id).delete()
                                }
                            }
                    }
            }
    }

    private fun commentGit(holder: PostHolder, myItem: Post) {
        val intent = Intent(holder.itemView.context, CommentsActivity::class.java)
        intent.putExtra("postID", myItem.postID)
        holder.itemView.context.startActivity(intent)
    }

    private fun findHashTags(text: String): List<String> {
        val hashtags = mutableListOf<String>()
        var hashtag = ""

        for (char in text) {
            if (char == '#') {
                if (hashtag.isNotEmpty()) {
                    hashtags.add(hashtag)
                }
                hashtag = "#"
            } else if (hashtag.isNotEmpty() && (char.isWhitespace() || char == '\n')) {
                hashtags.add(hashtag)
                hashtag = ""
            } else if (hashtag.isNotEmpty()) {
                hashtag += char
            }
        }

        if (hashtag.isNotEmpty()) {
            hashtags.add(hashtag)
        }

        return hashtags
    }

    override fun getItemCount(): Int = postList.size
}
