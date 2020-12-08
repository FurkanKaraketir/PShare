package com.furkankrktr.pshare.adapter

import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.furkankrktr.pshare.GorselActivity
import com.furkankrktr.pshare.R
import com.furkankrktr.pshare.databinding.RecyclerReplyBinding
import com.furkankrktr.pshare.model.Reply
import com.furkankrktr.pshare.service.glide
import com.furkankrktr.pshare.service.glider
import com.furkankrktr.pshare.service.placeHolderYap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ReplyRecyclerAdapter(private val replyList: ArrayList<Reply>) :
    RecyclerView.Adapter<ReplyRecyclerAdapter.ReplyHolder>() {

    private lateinit var database: FirebaseFirestore
    lateinit var auth: FirebaseAuth
    private lateinit var guncelKullanici: String

    class ReplyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = RecyclerReplyBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReplyHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.recycler_reply, parent, false)
        return ReplyHolder(view)
    }

    override fun onBindViewHolder(holder: ReplyHolder, position: Int) {
        with(holder) {


            database = FirebaseFirestore.getInstance()
            auth = FirebaseAuth.getInstance()
            if (auth.currentUser != null) {
                guncelKullanici = auth.currentUser!!.email.toString()
            }
            if (replyList[position].replyAttachment == "") {
                binding.replyImage.visibility = View.GONE
            } else {
                binding.replyImage.visibility = View.VISIBLE
                binding.replyImage.glide(
                    replyList[position].replyAttachment,
                    placeHolderYap(holder.itemView.context)
                )
            }
            database.collection("Users")
                .whereEqualTo("useremail", replyList[position].kullaniciEmail)
                .addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        Toast.makeText(
                            holder.itemView.context,
                            exception.localizedMessage,
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        if (snapshot != null) {
                            if (!snapshot.isEmpty) {
                                val documents = snapshot.documents
                                for (document in documents) {
                                    binding.replyEmail.text =
                                        document.get("username") as String
                                    val profile = document.get("profileImage") as String
                                    binding.profileImageReply.glider(
                                        profile,
                                        placeHolderYap(holder.itemView.context)
                                    )
                                }
                            } else {
                                binding.replyEmail.text = replyList[position].kullaniciEmail
                            }
                        } else {
                            binding.replyEmail.text = replyList[position].kullaniciEmail
                        }
                    }
                }
            binding.replyText.text = replyList[position].kullaniciReply

            binding.replyImage.setOnClickListener {
                val intent = Intent(holder.itemView.context, GorselActivity::class.java)
                intent.putExtra("resim", replyList[position].replyAttachment)
                holder.itemView.context.startActivity(intent)
            }

            if (replyList[position].kullaniciEmail == guncelKullanici) {
                binding.deleteReplyButton.visibility = View.VISIBLE
            } else {
                binding.deleteReplyButton.visibility = View.GONE
            }
            binding.deleteReplyButton.setOnClickListener {

                val alert = AlertDialog.Builder(holder.itemView.context)

                alert.setTitle("Yanıtı Sil")
                alert.setMessage("Yanıtı Silmek İstediğinize Emin misiniz?")
                alert.setNegativeButton(
                    "İptal Et"
                ) { _, _ ->
                    Toast.makeText(
                        holder.itemView.context,
                        "İşlem iptal edildi",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                alert.setPositiveButton(
                    "SİL"
                ) { _, _ ->

                    val yorumsRef = database.collection("Yanıtlar")
                    val queryYorum =
                        yorumsRef.whereEqualTo("replyId", replyList[position].replyId)
                    queryYorum.get().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            for (document in task.result) {
                                yorumsRef.document(document.id).delete()
                                Toast.makeText(
                                    holder.itemView.context,
                                    "Yanıt Silindi",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            Toast.makeText(holder.itemView.context, "Hata", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }

                }.show()

            }
        }

    }

    override fun getItemCount(): Int {
        return replyList.size
    }


}