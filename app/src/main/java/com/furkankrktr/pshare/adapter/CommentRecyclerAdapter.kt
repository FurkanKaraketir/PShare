package com.furkankrktr.pshare.adapter

import android.app.AlertDialog
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.furkankrktr.pshare.R
import com.furkankrktr.pshare.model.Comment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.recycler_comment.view.*

class CommentRecyclerAdapter(private val commentList: ArrayList<Comment>) :
    RecyclerView.Adapter<CommentRecyclerAdapter.CommentHolder>() {

    lateinit var database: FirebaseFirestore
    lateinit var auth: FirebaseAuth
    lateinit var guncelKullanici: String
    private lateinit var recyclerCommentViewAdapter: CommentRecyclerAdapter

    class CommentHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.recycler_comment, parent, false)
        return CommentHolder(view)
    }

    override fun onBindViewHolder(holder: CommentHolder, position: Int) {

        database = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            guncelKullanici = auth.currentUser!!.email.toString()
        }

        holder.itemView.commentEmail.text = commentList[position].kullaniciEmail
        holder.itemView.comment.text = commentList[position].kullaniciComment

        holder.itemView.deleteYorumButton.setOnClickListener {
            if (commentList[position].kullaniciEmail == guncelKullanici || guncelKullanici == "furkankaraketir2005@gmail.com") {
                recyclerCommentViewAdapter = CommentRecyclerAdapter(commentList)

                val alert = AlertDialog.Builder(holder.itemView.context)

                alert.setTitle("Yorum Sil")
                alert.setMessage("Yorumu Silmek İstediğinize Emin misiniz?")
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

                        val yorumsRef = database.collection("Yorumlar")
                        val queryYorum =
                            yorumsRef.whereEqualTo("commentId", commentList[position].commentId)
                        queryYorum.get().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                for (document in task.result) {
                                    yorumsRef.document(document.id).delete()
                                    Toast.makeText(
                                        holder.itemView.context,
                                        "Yorum Silindi.Yorumunuz Şuan Görünüyor Olabilir. Ancak Yorumunuz Başarıyla Silinmiştir",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    recyclerCommentViewAdapter.notifyDataSetChanged()
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
                    "Yalnızca Kendi Yüklediğiniz Yorumları Silebilirsiniz",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    override fun getItemCount(): Int {
        return commentList.size
    }
}