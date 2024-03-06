package com.karaketir.pshare.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.karaketir.pshare.*
import com.karaketir.pshare.databinding.PostRowBinding
import com.karaketir.pshare.model.Post
import com.karaketir.pshare.services.FcmNotificationsSenderService
import com.karaketir.pshare.services.glide
import com.karaketir.pshare.services.openLink
import com.karaketir.pshare.services.placeHolderYap
import java.util.*


open class PostRecyclerAdapter(
    private val postList: ArrayList<Post>
) : RecyclerView.Adapter<PostRecyclerAdapter.PostHolder>() {


    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth


    class PostHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = PostRowBinding.bind(itemView)
    }


    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): PostHolder {

        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.post_row, parent, false)
        return PostHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: PostHolder, position: Int) {
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val followList = ArrayList<String>()

        with(holder) {
            val myBinding = binding

            if (postList.isNotEmpty() && position >= 0 && position < postList.size) {
                val myItem = postList[position]
                var profileImageURL = ""
                db.collection("User").document(myItem.postOwnerID).get().addOnSuccessListener {
                    myBinding.userName.text = it.get("username").toString()
                    profileImageURL = it.get("profileImageURL").toString()
                    myBinding.profileImage.glide(
                        profileImageURL, placeHolderYap(holder.itemView.context)
                    )
                }

                myBinding.moreOptionsPost.setOnClickListener {

                    val popup = PopupMenu(holder.itemView.context, myBinding.moreOptionsPost)
                    popup.inflate(R.menu.post_options_menu)

                    popup.setOnMenuItemClickListener(object : MenuItem.OnMenuItemClickListener,
                        PopupMenu.OnMenuItemClickListener {
                        override fun onMenuItemClick(item: MenuItem): Boolean {
                            return when (item.itemId) {
                                R.id.blockButton -> {
                                    val documentName = UUID.randomUUID().toString()
                                    val data = hashMapOf(
                                        "main" to auth.uid.toString(),
                                        "blocksWho" to myItem.postOwnerID
                                    )

                                    val blockAlert = AlertDialog.Builder(holder.itemView.context)
                                    blockAlert.setTitle("Engelle")
                                    blockAlert.setMessage("Engellemek İstedğinizden Emin misiniz?")
                                    blockAlert.setPositiveButton("Engelle") { _, _ ->

                                        db.collection("Blocks").document(documentName).set(data)

                                        db.collection("Followings")
                                            .whereEqualTo("main", auth.uid.toString())
                                            .whereEqualTo("followsWho", myItem.postOwnerID)
                                            .addSnapshotListener { value, _ ->
                                                if (value != null) {
                                                    for (i in value) {
                                                        db.collection("Followings").document(i.id)
                                                            .delete()
                                                    }
                                                }
                                                db.collection("Followings")
                                                    .whereEqualTo("followsWho", auth.uid.toString())
                                                    .whereEqualTo("main", myItem.postOwnerID)
                                                    .addSnapshotListener { value2, _ ->
                                                        if (value2 != null) {
                                                            for (j in value2) {
                                                                db.collection("Followings")
                                                                    .document(j.id).delete()
                                                            }
                                                        }
                                                    }
                                            }

                                        Toast.makeText(
                                            holder.itemView.context,
                                            "Engellendi",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    }

                                    blockAlert.setNegativeButton("İptal") { _, _ ->

                                    }
                                    blockAlert.show()


                                    //handle menu1 click
                                    true
                                }

                                else -> {
                                    false
                                }
                            }
                        }
                    })
                    //displaying the popup
                    //displaying the popup
                    popup.show()
                }
                myBinding.postDescription.text = myItem.postDescription

                if (myItem.postImageURL != "") {
                    myBinding.postImage.visibility = View.VISIBLE
                    myBinding.postImage.glide(
                        myItem.postImageURL, placeHolderYap(holder.itemView.context)
                    )
                } else {
                    myBinding.postImage.visibility = View.GONE
                }

                if (myItem.postOwnerID != auth.uid.toString()) {

                    myBinding.deleteButton.visibility = View.GONE
                    myBinding.moreOptionsPost.visibility = View.VISIBLE

                    db.collection("Followings").whereEqualTo("main", auth.uid.toString())
                        .addSnapshotListener { followIDs, error ->

                            if (error != null) {
                                println(error.localizedMessage)
                            }
                            followList.clear()
                            if (followIDs != null) {
                                for (followID in followIDs) {
                                    followList.add(followID.get("followsWho").toString())
                                }
                            }
                            if (myItem.postOwnerID in followList) {
                                myBinding.followButton.visibility = View.GONE
                                myBinding.unFollowButton.visibility = View.VISIBLE
                            } else {
                                myBinding.followButton.visibility = View.VISIBLE
                                myBinding.unFollowButton.visibility = View.GONE
                            }

                        }
                } else {
                    myBinding.deleteButton.visibility = View.VISIBLE
                    myBinding.moreOptionsPost.visibility = View.GONE
                    myBinding.followButton.visibility = View.GONE
                    myBinding.unFollowButton.visibility = View.GONE
                }

                var likedID = ""

                db.collection("Likes").whereEqualTo("postID", myItem.postID)
                    .whereEqualTo("userID", auth.uid.toString())
                    .addSnapshotListener { value, error ->

                        if (error != null) {
                            println(error.localizedMessage)
                        }
                        if (value != null && !value.isEmpty) {
                            myBinding.likedButton.visibility = View.VISIBLE
                            myBinding.unlikedButton.visibility = View.GONE
                            for (i in value) {
                                likedID = i.id
                            }
                        } else {
                            myBinding.likedButton.visibility = View.GONE
                            myBinding.unlikedButton.visibility = View.VISIBLE
                        }
                    }

                db.collection("Likes").whereEqualTo("postID", myItem.postID)
                    .addSnapshotListener { value, error ->
                        if (error != null) {
                            println(error.localizedMessage)
                        }
                        if (value != null && !value.isEmpty) {
                            myBinding.likeCountText.text = "${value.size()} Beğeni"
                        } else {
                            myBinding.likeCountText.text = "0 Beğeni"

                        }
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
                    val data = hashMapOf("userID" to auth.uid.toString(), "postID" to myItem.postID)
                    db.collection("Likes").document(documentName).set(data).addOnSuccessListener {

                        if (auth.uid.toString() != myItem.postOwnerID) {
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

                myBinding.followButton.setOnClickListener {


                    val documentID = UUID.randomUUID().toString()
                    val data =
                        hashMapOf("main" to auth.uid.toString(), "followsWho" to myItem.postOwnerID)

                    val followAlert = AlertDialog.Builder(holder.itemView.context)
                    followAlert.setTitle("Takip Et")
                    followAlert.setMessage("Takip Etmek İstedğinizden Emin misiniz?")
                    followAlert.setPositiveButton("Takip Et") { _, _ ->

                        db.collection("Followings").document(documentID).set(data)
                            .addOnSuccessListener {

                            }
                    }
                    followAlert.setNegativeButton("İptal") { _, _ ->

                    }
                    followAlert.show()

                }

                myBinding.unFollowButton.setOnClickListener {

                    val unFollowAlert = AlertDialog.Builder(holder.itemView.context)
                    unFollowAlert.setTitle("Takibi Bırak")
                    unFollowAlert.setMessage("Takibi Bırakmak İstediğinize Emin misiniz?")
                    unFollowAlert.setPositiveButton("Takibi Bırak") { _, _ ->
                        db.collection("Followings").whereEqualTo("main", auth.uid.toString())
                            .whereEqualTo("followsWho", myItem.postOwnerID)
                            .addSnapshotListener { value, _ ->
                                if (value != null) {
                                    for (i in value) {
                                        db.collection("Followings").document(i.id).delete()
                                            .addOnSuccessListener {

                                            }
                                    }
                                }
                            }
                    }
                    unFollowAlert.setNegativeButton("İptal") { _, _ ->

                    }
                    unFollowAlert.show()


                }
                myBinding.commentsButton.setOnClickListener {
                    commentGit(holder, myItem)
                }
                myBinding.commentCountText.setOnClickListener {
                    commentGit(holder, myItem)
                }

                myBinding.postImage.setOnClickListener {
                    openLink(myItem.postImageURL, holder.itemView.context)
                }
                myBinding.profileImage.setOnClickListener {
                    openLink(profileImageURL, holder.itemView.context)
                }

                myBinding.postDescription.setOnClickListener {
                    if (myBinding.postDescription.text.contains("#")) {
                        val intent = Intent(holder.itemView.context, HashtagActivity::class.java)
                        intent.putExtra(
                            "selectedHashtag",
                            findHashTag(myBinding.postDescription.text.toString())
                        )
                        holder.itemView.context.startActivity(intent)
                    }
                }

                myBinding.userName.setOnClickListener {

                    val intent =
                        Intent(holder.itemView.context, UserFilteredPostsActivity::class.java)
                    intent.putExtra("postOwnerID", myItem.postOwnerID)
                    holder.itemView.context.startActivity(intent)

                }

                db.collection("Comments").whereEqualTo("commentToPost", myItem.postID)
                    .addSnapshotListener { value, error ->
                        if (error != null) {
                            println(error.localizedMessage)
                        }
                        if (value != null) {
                            myBinding.commentCountText.text = "${value.size()} Yorum"
                        } else {
                            myBinding.commentCountText.text = "0 Yorum"
                        }
                    }

                myBinding.deleteButton.setOnClickListener {

                    val deleteAlertDialog = AlertDialog.Builder(holder.itemView.context)
                    deleteAlertDialog.setTitle("Postu Sil")
                    deleteAlertDialog.setMessage("Postu Silmek İstediğinize Emin misiniz?")
                    deleteAlertDialog.setPositiveButton("Postu Sil") { _, _ ->

                        db.collection("Replies").whereEqualTo("replyToPost", myItem.postID)
                            .addSnapshotListener { value1, _ ->
                                if (value1 != null) {
                                    for (i in value1) {
                                        db.collection("Replies").document(i.id).delete()
                                    }
                                }
                                db.collection("Comments")
                                    .whereEqualTo("commentToPost", myItem.postID)
                                    .addSnapshotListener { value2, _ ->
                                        if (value2 != null) {
                                            for (a in value2) {
                                                db.collection("Comments").document(a.id).delete()
                                            }
                                        }
                                        db.collection("Post").document(myItem.postID).delete()
                                        db.collection("Likes").whereEqualTo("postID", myItem.postID)
                                            .addSnapshotListener { value3, _ ->

                                                if (value3 != null) {
                                                    for (q in value3) {
                                                        db.collection("Likes").document(q.id)
                                                            .delete()
                                                    }
                                                }

                                            }
                                    }
                            }


                    }
                    deleteAlertDialog.setNegativeButton("İptal") { _, _ ->

                    }
                    deleteAlertDialog.show()


                }


            }


        }


    }

    private fun findHashTag(str: String): String {
        var hestegBulundu = false
        var sonuc = ""

        for (i in str) {
            if (i == '#') {
                hestegBulundu = true
            }
            if (hestegBulundu && i == ' ') {
                return sonuc
            }
            if (hestegBulundu) {
                sonuc += i
            }
        }
        return sonuc
    }

    private fun commentGit(holder: PostHolder, myItem: Post) {
        val intent = Intent(holder.itemView.context, CommentsActivity::class.java)
        intent.putExtra("postID", myItem.postID)
        holder.itemView.context.startActivity(intent)
    }

    override fun getItemCount(): Int {
        return postList.size
    }
}