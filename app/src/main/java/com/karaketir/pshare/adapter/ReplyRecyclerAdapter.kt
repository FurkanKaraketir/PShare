package com.karaketir.pshare.adapter

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
import com.karaketir.pshare.UserFilteredPostsActivity
import com.karaketir.pshare.databinding.ReplyRowBinding
import com.karaketir.pshare.model.Reply
import com.karaketir.pshare.services.getRelativeTime
import com.karaketir.pshare.services.glide
import com.karaketir.pshare.services.openLink
import com.karaketir.pshare.services.placeHolderYap
import java.util.*
import kotlin.collections.ArrayList

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

            val myBinding = binding

            if (replyList.isNotEmpty() && position <= replyList.size) {

                val myItem = replyList[position]

                database.collection("User").document(myItem.replyOwnerID).get()
                    .addOnSuccessListener {
                        myBinding.replyUserName.text = it.get("username").toString()
                        myBinding.profileImageReply.glide(
                            it.get("profileImageURL").toString(),
                            placeHolderYap(holder.itemView.context)
                        )
                        myBinding.profileImageReply.setOnClickListener { _ ->
                            openLink(it.get("profileImageURL").toString(), holder.itemView.context)
                        }

                        myBinding.replyText.text = myItem.reply
                        myBinding.replyUserName.setOnClickListener {
                            val intent = Intent(
                                holder.itemView.context, UserFilteredPostsActivity::class.java
                            )
                            intent.putExtra("postOwnerID", myItem.replyOwnerID)
                            holder.itemView.context.startActivity(intent)
                        }
                        if (myItem.replyOwnerID == auth.uid.toString()) {
                            myBinding.moreOptionsReply.visibility = View.GONE
                            myBinding.deleteReplyButton.visibility = View.VISIBLE
                        } else {
                            myBinding.moreOptionsReply.visibility = View.VISIBLE
                            myBinding.deleteReplyButton.visibility = View.GONE
                        }
                        myBinding.moreOptionsReply.setOnClickListener {

                            val popup =
                                PopupMenu(holder.itemView.context, myBinding.moreOptionsReply)
                            popup.inflate(R.menu.post_options_menu)

                            popup.setOnMenuItemClickListener(object :
                                MenuItem.OnMenuItemClickListener,
                                PopupMenu.OnMenuItemClickListener {
                                override fun onMenuItemClick(item: MenuItem): Boolean {
                                    return when (item.itemId) {
                                        R.id.blockButton -> {
                                            val documentName = UUID.randomUUID().toString()
                                            val data = hashMapOf(
                                                "main" to auth.uid.toString(),
                                                "blocksWho" to myItem.replyOwnerID
                                            )

                                            val blockAlert =
                                                AlertDialog.Builder(holder.itemView.context)
                                            blockAlert.setTitle("Engelle")
                                            blockAlert.setMessage("Engellemek İstedğinizden Emin misiniz?")
                                            blockAlert.setPositiveButton("Engelle") { _, _ ->

                                                database.collection("Blocks").document(documentName)
                                                    .set(data)

                                                database.collection("Followings")
                                                    .whereEqualTo("main", auth.uid.toString())
                                                    .whereEqualTo("followsWho", myItem.replyOwnerID)
                                                    .addSnapshotListener { value, _ ->
                                                        if (value != null) {
                                                            for (i in value) {
                                                                database.collection("Followings")
                                                                    .document(i.id).delete()
                                                            }
                                                        }
                                                        database.collection("Followings")
                                                            .whereEqualTo(
                                                                "followsWho", auth.uid.toString()
                                                            ).whereEqualTo(
                                                                "main", myItem.replyOwnerID
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
                            popup.show()
                        }

                        myBinding.replyDate.text = getRelativeTime(myItem.timestamp)


                        myBinding.deleteReplyButton.setOnClickListener {

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