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
import com.karaketir.pshare.R
import com.karaketir.pshare.RepliesActivity
import com.karaketir.pshare.databinding.CommentRowBinding
import com.karaketir.pshare.model.Comment
import com.karaketir.pshare.services.glide
import com.karaketir.pshare.services.placeHolderYap

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

            if (commentList.isNotEmpty() && position <= commentList.size) {

                val myItem = commentList[position]

                binding.comment.text = myItem.comment

                database.collection("User").document(myItem.commentOwnerID).get()
                    .addOnSuccessListener {
                        binding.commentUserName.text = it.get("username").toString()
                        binding.profileImageComment.glide(
                            it.get("profileImageURL").toString(),
                            placeHolderYap(holder.itemView.context)
                        )
                    }

                database.collection("Replies").whereEqualTo("replyToComment", myItem.commentID)
                    .addSnapshotListener { value, _ ->
                        if (value != null && !value.isEmpty) {
                            binding.replyCount.text = "${value.size()} Yanıt"
                        } else {
                            binding.replyCount.text = "0 Yanıt"
                        }
                    }
                if (myItem.commentOwnerID == auth.uid.toString()) {
                    binding.deleteYorumButton.visibility = View.VISIBLE
                } else {
                    binding.deleteYorumButton.visibility = View.GONE
                }

                binding.deleteYorumButton.setOnClickListener {

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

                binding.replyYorumButton.setOnClickListener {
                    replyGit(holder, myItem)
                }
                binding.replyCount.setOnClickListener {
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
