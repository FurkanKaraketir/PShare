package com.karaketir.pshare.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.karaketir.pshare.R
import com.karaketir.pshare.databinding.ReplyRowBinding
import com.karaketir.pshare.model.Reply
import com.karaketir.pshare.services.glide
import com.karaketir.pshare.services.placeHolderYap

class ReplyRecyclerAdapter(private val replyList: ArrayList<Reply>) :
    RecyclerView.Adapter<ReplyRecyclerAdapter.ReplyHolder>() {

    private lateinit var database: FirebaseFirestore
    lateinit var auth: FirebaseAuth

    class ReplyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ReplyRowBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReplyHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.reply_row, parent, false)
        return ReplyHolder(view)
    }

    override fun onBindViewHolder(holder: ReplyHolder, position: Int) {

        database = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        with(holder) {

            if (replyList.isNotEmpty() && position <= replyList.size) {

                val myItem = replyList[position]

                database.collection("User").document(myItem.replyOwnerID).get()
                    .addOnSuccessListener {
                        binding.replyUserName.text = it.get("username").toString()
                        binding.profileImageReply.glide(
                            it.get("profileImageURL").toString(),
                            placeHolderYap(holder.itemView.context)
                        )

                        binding.replyText.text = myItem.reply

                        if (myItem.replyOwnerID == auth.uid.toString()) {
                            binding.deleteReplyButton.visibility = View.VISIBLE
                        } else {
                            binding.deleteReplyButton.visibility = View.GONE
                        }

                        binding.deleteReplyButton.setOnClickListener {

                            val deleteAlertDialog = AlertDialog.Builder(holder.itemView.context)
                            deleteAlertDialog.setTitle("Yanıtı Sil")
                            deleteAlertDialog.setMessage("Yanıtı Silmek İstediğinize Emin misiniz?")
                            deleteAlertDialog.setPositiveButton("Yanıtı Sil") { _, _ ->
                                database.collection("Replies").document(myItem.replyID).delete()
                            }

                            deleteAlertDialog.setNegativeButton("İptal") { _, _ ->

                            }
                            deleteAlertDialog.show()
                        }


                    }


            }


        }

    }

    override fun getItemCount(): Int {
        return replyList.size
    }


}