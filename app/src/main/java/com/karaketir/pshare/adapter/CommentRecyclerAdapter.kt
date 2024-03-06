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
import com.karaketir.pshare.R
import com.karaketir.pshare.RepliesActivity
import com.karaketir.pshare.UserFilteredPostsActivity
import com.karaketir.pshare.databinding.CommentRowBinding
import com.karaketir.pshare.model.Comment
import com.karaketir.pshare.services.glide
import com.karaketir.pshare.services.openLink
import com.karaketir.pshare.services.placeHolderYap
import java.util.*
import kotlin.collections.ArrayList

class CommentRecyclerAdapter(private val commentList: ArrayList<Comment>) :
    RecyclerView.Adapter<CommentRecyclerAdapter.CommentHolder>() {

    private lateinit var database: FirebaseFirestore
    lateinit var auth: FirebaseAuth

    class CommentHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = CommentRowBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.comment_row, parent, false)
        return CommentHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CommentHolder, position: Int) {

        database = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        with(holder) {

            val myBinding = binding

            if (commentList.isNotEmpty() && position <= commentList.size) {

                val myItem = commentList[position]

                myBinding.comment.text = myItem.comment

                database.collection("User").document(myItem.commentOwnerID).get()
                    .addOnSuccessListener {
                        myBinding.commentUserName.text = it.get("username").toString()
                        myBinding.profileImageComment.glide(
                            it.get("profileImageURL").toString(),
                            placeHolderYap(holder.itemView.context)
                        )
                        myBinding.profileImageComment.setOnClickListener { _ ->
                            openLink(it.get("profileImageURL").toString(), holder.itemView.context)
                        }

                        myBinding.commentUserName.setOnClickListener {
                            val intent = Intent(
                                holder.itemView.context, UserFilteredPostsActivity::class.java
                            )
                            intent.putExtra("postOwnerID", myItem.commentOwnerID)
                            holder.itemView.context.startActivity(intent)
                        }

                    }

                database.collection("Replies").whereEqualTo("replyToComment", myItem.commentID)
                    .addSnapshotListener { value, _ ->
                        if (value != null && !value.isEmpty) {
                            myBinding.replyCount.text = "${value.size()} Yanıt"
                        } else {
                            myBinding.replyCount.text = "0 Yanıt"
                        }
                    }
                if (myItem.commentOwnerID == auth.uid.toString()) {
                    myBinding.deleteYorumButton.visibility = View.VISIBLE
                    myBinding.moreOptionsComment.visibility = View.GONE
                } else {
                    myBinding.deleteYorumButton.visibility = View.GONE
                    myBinding.moreOptionsComment.visibility = View.VISIBLE
                }

                myBinding.moreOptionsComment.setOnClickListener {

                    val popup = PopupMenu(holder.itemView.context, myBinding.moreOptionsComment)
                    popup.inflate(R.menu.post_options_menu)

                    popup.setOnMenuItemClickListener(object : MenuItem.OnMenuItemClickListener,
                        PopupMenu.OnMenuItemClickListener {
                        override fun onMenuItemClick(item: MenuItem): Boolean {
                            return when (item.itemId) {
                                R.id.blockButton -> {
                                    val documentName = UUID.randomUUID().toString()
                                    val data = hashMapOf(
                                        "main" to auth.uid.toString(),
                                        "blocksWho" to myItem.commentOwnerID
                                    )

                                    val blockAlert = AlertDialog.Builder(holder.itemView.context)
                                    blockAlert.setTitle("Engelle")
                                    blockAlert.setMessage("Engellemek İstedğinizden Emin misiniz?")
                                    blockAlert.setPositiveButton("Engelle") { _, _ ->

                                        database.collection("Blocks").document(documentName)
                                            .set(data)

                                        database.collection("Followings")
                                            .whereEqualTo("main", auth.uid.toString())
                                            .whereEqualTo("followsWho", myItem.commentOwnerID)
                                            .addSnapshotListener { value, _ ->
                                                if (value != null) {
                                                    for (i in value) {
                                                        database.collection("Followings")
                                                            .document(i.id).delete()
                                                    }
                                                }
                                                database.collection("Followings").whereEqualTo(
                                                    "followsWho", auth.uid.toString()
                                                ).whereEqualTo(
                                                    "main", myItem.commentOwnerID
                                                ).addSnapshotListener { value2, _ ->
                                                    if (value2 != null) {
                                                        for (j in value2) {
                                                            database.collection("Followings")
                                                                .document(j.id).delete()
                                                        }
                                                    }
                                                }

                                                Toast.makeText(
                                                    holder.itemView.context,
                                                    "Engellendi",
                                                    Toast.LENGTH_SHORT
                                                ).show()

                                            }

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

                myBinding.deleteYorumButton.setOnClickListener {

                    val deleteAlertDialog = AlertDialog.Builder(holder.itemView.context)
                    deleteAlertDialog.setTitle("Yorumu Sil")
                    deleteAlertDialog.setMessage("Yorumu Silmek İstediğinize Emin misiniz?")
                    deleteAlertDialog.setPositiveButton("Yorumu Sil") { _, _ ->

                        database.collection("Replies")
                            .whereEqualTo("replyToComment", myItem.commentID)
                            .addSnapshotListener { value, _ ->
                                if (value != null) {
                                    for (i in value) {
                                        database.collection("Replies").document(i.id).delete()
                                    }
                                }
                                database.collection("Comments").document(myItem.commentID).delete()
                            }


                    }
                    deleteAlertDialog.setNegativeButton("İptal") { _, _ ->

                    }
                    deleteAlertDialog.show()

                }

                myBinding.replyYorumButton.setOnClickListener {
                    replyGit(holder, myItem)
                }
                myBinding.replyCount.setOnClickListener {
                    replyGit(holder, myItem)
                }

            }

        }
    }

    private fun replyGit(holder: CommentHolder, myItem: Comment) {
        val intent = Intent(holder.itemView.context, RepliesActivity::class.java)
        intent.putExtra("commentID", myItem.commentID)
        holder.itemView.context.startActivity(intent)
    }

    override fun getItemCount(): Int {
        return commentList.size
    }
}
