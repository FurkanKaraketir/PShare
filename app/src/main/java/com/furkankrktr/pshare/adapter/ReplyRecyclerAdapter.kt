package com.furkankrktr.pshare.adapter

import android.app.AlertDialog
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.furkankrktr.pshare.R
import com.furkankrktr.pshare.model.Reply
import com.furkankrktr.pshare.service.glide
import com.furkankrktr.pshare.service.placeHolderYap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.recycler_reply.view.*

class ReplyRecyclerAdapter(private val replyList: ArrayList<Reply>) :
    RecyclerView.Adapter<ReplyRecyclerAdapter.ReplyHolder>() {

    private lateinit var database: FirebaseFirestore
    lateinit var auth: FirebaseAuth
    private lateinit var guncelKullanici: String

    class ReplyHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReplyHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.recycler_reply, parent, false)
        return ReplyHolder(view)
    }

    override fun onBindViewHolder(holder: ReplyHolder, position: Int) {
        database = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            guncelKullanici = auth.currentUser!!.email.toString()
        }
        if (replyList[position].replyAttachment == "") {
            holder.itemView.replyImage.visibility = View.GONE
        } else {
            holder.itemView.replyImage.visibility = View.VISIBLE
            holder.itemView.replyImage.glide(
                replyList[position].replyAttachment,
                placeHolderYap(holder.itemView.context)
            )
        }
        holder.itemView.replyEmail.text = replyList[position].kullaniciEmail
        holder.itemView.replyText.text = replyList[position].kullaniciReply

        holder.itemView.deleteReplyButton.setOnClickListener {
            if (replyList[position].kullaniciEmail == guncelKullanici || guncelKullanici == "furkankaraketir2005@gmail.com") {

                val alert = AlertDialog.Builder(holder.itemView.context)

                alert.setTitle("Yanıtı Sil")
                alert.setMessage("Yanıtı Silmek İstediğinize Emin misiniz?")
                alert.setNegativeButton(
                    "Hayır",
                    DialogInterface.OnClickListener { _, _ ->
                        Toast.makeText(
                            holder.itemView.context,
                            "İşlem iptal edildi",
                            Toast.LENGTH_SHORT
                        ).show()
                    })

                alert.setPositiveButton(
                    "Evet",
                    DialogInterface.OnClickListener { _, _ ->

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

                    }).show()


            } else {
                Toast.makeText(
                    holder.itemView.context,
                    "Yalnızca Kendi Yazdığınız Yanıtları Silebilirsiniz",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


    }

    override fun getItemCount(): Int {
        return replyList.size
    }


}