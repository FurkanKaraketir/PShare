package com.karaketir.pshare.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.karaketir.pshare.CommentsActivity
import com.karaketir.pshare.HashtagActivity
import com.karaketir.pshare.R
import com.karaketir.pshare.UserFilteredPostsActivity
import com.karaketir.pshare.databinding.PostRowBinding
import com.karaketir.pshare.model.Post
import com.karaketir.pshare.services.glide
import com.karaketir.pshare.services.openLink
import com.karaketir.pshare.services.placeHolderYap
import java.util.*

open class PostRecyclerAdapter(private val postList: ArrayList<Post>) :
    RecyclerView.Adapter<PostRecyclerAdapter.PostHolder>() {
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

            if (postList.isNotEmpty() && position >= 0 && position < postList.size) {
                val myItem = postList[position]
                var profileImageURL = ""
                db.collection("User").document(myItem.postOwnerID).get().addOnSuccessListener {
                    binding.userName.text = it.get("username").toString()
                    profileImageURL = it.get("profileImageURL").toString()
                    binding.profileImage.glide(
                        profileImageURL, placeHolderYap(holder.itemView.context)
                    )
                }

                binding.postDescription.text = myItem.postDescription

                if (myItem.postImageURL != "") {
                    binding.postImage.visibility = View.VISIBLE
                    binding.postImage.glide(
                        myItem.postImageURL, placeHolderYap(holder.itemView.context)
                    )
                } else {
                    binding.postImage.visibility = View.GONE
                }

                if (myItem.postOwnerID != auth.uid.toString()) {
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
                                binding.followButton.visibility = View.GONE
                                binding.unFollowButton.visibility = View.VISIBLE
                            } else {
                                binding.followButton.visibility = View.VISIBLE
                                binding.unFollowButton.visibility = View.GONE
                            }

                        }
                } else {
                    binding.deleteButton.visibility = View.VISIBLE
                    binding.followButton.visibility = View.GONE
                    binding.unFollowButton.visibility = View.GONE
                }

                var likedID = ""

                db.collection("Likes").whereEqualTo("postID", myItem.postID)
                    .whereEqualTo("userID", auth.uid.toString())
                    .addSnapshotListener { value, error ->

                        if (error != null) {
                            println(error.localizedMessage)
                        }
                        if (value != null && !value.isEmpty) {
                            binding.likedButton.visibility = View.VISIBLE
                            binding.unlikedButton.visibility = View.GONE
                            for (i in value) {
                                likedID = i.id
                            }
                        } else {
                            binding.likedButton.visibility = View.GONE
                            binding.unlikedButton.visibility = View.VISIBLE
                        }
                    }

                db.collection("Likes").whereEqualTo("postID", myItem.postID)
                    .addSnapshotListener { value, error ->
                        if (error != null) {
                            println(error.localizedMessage)
                        }
                        if (value != null && !value.isEmpty) {
                            binding.likeCountText.text = "${value.size()} Beğeni"
                        } else {
                            binding.likeCountText.text = "0 Beğeni"

                        }
                    }

                binding.likedButton.setOnClickListener {
                    db.collection("Likes").document(likedID).delete()
                }
                binding.unlikedButton.setOnClickListener {
                    val documentName = UUID.randomUUID().toString()
                    val data = hashMapOf("userID" to auth.uid.toString(), "postID" to myItem.postID)
                    db.collection("Likes").document(documentName).set(data)
                }

                binding.followButton.setOnClickListener {


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

                binding.unFollowButton.setOnClickListener {

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
                binding.commentsButton.setOnClickListener {
                    commentGit(holder, myItem)
                }
                binding.commentCountText.setOnClickListener {
                    commentGit(holder, myItem)
                }

                binding.postImage.setOnClickListener {
                    openLink(myItem.postImageURL, holder.itemView.context)
                }
                binding.profileImage.setOnClickListener {
                    openLink(profileImageURL, holder.itemView.context)
                }

                binding.postDescription.setOnClickListener {
                    if (binding.postDescription.text.contains("#")) {
                        val intent = Intent(holder.itemView.context, HashtagActivity::class.java)
                        intent.putExtra(
                            "selectedHashtag", findHashTag(binding.postDescription.text.toString())
                        )
                        holder.itemView.context.startActivity(intent)
                    }
                }

                binding.userName.setOnClickListener {

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
                            binding.commentCountText.text = "${value.size()} Yorum"
                        } else {
                            binding.commentCountText.text = "0 Yorum"
                        }
                    }

                binding.deleteButton.setOnClickListener {

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