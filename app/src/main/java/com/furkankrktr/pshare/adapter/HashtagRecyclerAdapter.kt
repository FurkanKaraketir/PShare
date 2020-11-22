package com.furkankrktr.pshare.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.furkankrktr.pshare.CommentsActivity
import com.furkankrktr.pshare.GorselActivity
import com.furkankrktr.pshare.R
import com.furkankrktr.pshare.model.Post
import com.furkankrktr.pshare.service.glide
import com.furkankrktr.pshare.service.placeHolderYap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.recycler_row.view.*

open class HashtagRecyclerAdapter(private val postList: ArrayList<Post>) :
    RecyclerView.Adapter<HashtagRecyclerAdapter.PostHolder>() {


    private lateinit var database: FirebaseFirestore
    lateinit var auth: FirebaseAuth
    private lateinit var guncelKullanici: String

    class PostHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PostHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.recycler_row, parent, false)
        return PostHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: PostHolder, position: Int) {

        database = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            guncelKullanici = auth.currentUser!!.email.toString()
        }

        database.collection("Users").whereEqualTo("useremail", postList[position].kullaniciEmail)
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
                                holder.itemView.recycler_row_kullanici_email.text =
                                    document.get("username") as String
                            }
                        } else {
                            holder.itemView.recycler_row_kullanici_email.text =
                                postList[position].kullaniciEmail
                        }
                    } else {
                        holder.itemView.recycler_row_kullanici_email.text =
                            postList[position].kullaniciEmail
                    }
                }
            }
        holder.itemView.recycler_row_kullanici_yorum.text = postList[position].kullaniciYorum

        if (postList[position].gorselUrl == "") {
            holder.itemView.recycler_row_imageview.visibility = View.GONE
        } else {
            holder.itemView.recycler_row_imageview.visibility = View.VISIBLE
            holder.itemView.recycler_row_imageview.glide(
                postList[position].gorselUrl,
                placeHolderYap(holder.itemView.context)
            )
        }

        holder.itemView.recycler_row_imageview.setOnClickListener {
            val intent = Intent(holder.itemView.context, GorselActivity::class.java)
            intent.putExtra("resim", postList[position].gorselUrl)
            holder.itemView.context.startActivity(intent)
        }


        holder.itemView.commentsButton.setOnClickListener {
            commentGit(holder, position)
        }
        holder.itemView.commentCountText.setOnClickListener {
            commentGit(holder, position)
        }



        if (postList[position].kullaniciEmail == guncelKullanici) {
            holder.itemView.deleteButton.visibility = View.VISIBLE
        } else {
            holder.itemView.deleteButton.visibility = View.GONE
        }

        holder.itemView.deleteButton.setOnClickListener {
            //dialog

            val alert = AlertDialog.Builder(holder.itemView.context)

            alert.setTitle("Post Sil")
            alert.setMessage("Postu Silmek İstediğinize Emin misiniz?")

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
                val itemsRef = database.collection("Post")

                val query = itemsRef.whereEqualTo("postId", postList[position].postId)

                query.get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result) {
                            itemsRef.document(document.id).delete()
                            Toast.makeText(
                                holder.itemView.context,
                                "Post Silindi",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(holder.itemView.context, "Hata", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                val yorumsRef = database.collection("Yorumlar")
                val queryYorum =
                    yorumsRef.whereEqualTo("selectedPost", postList[position].postId)
                queryYorum.get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result) {
                            yorumsRef.document(document.id).delete()
                        }
                    }
                }

            }
            alert.show()


        }



        database.collection("Yorumlar").whereEqualTo("selectedPost", postList[position].postId)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    println(exception.localizedMessage)
                } else {
                    if (snapshot != null) {
                        if (!snapshot.isEmpty) {
                            val documents = snapshot.documents
                            holder.itemView.commentCountText.text = "${documents.size} Yorum"

                        } else {
                            holder.itemView.commentCountText.text = "0 Yorum"
                        }
                    }


                }
            }


    }

    override fun getItemCount(): Int {
        return postList.size
    }

    private fun commentGit(holder: PostHolder, position: Int) {
        val intent = Intent(holder.itemView.context, CommentsActivity::class.java)
        intent.putExtra("selectedPost", postList[position].postId)
        intent.putExtra("selectedPostEmail", postList[position].kullaniciUID)
        intent.putExtra("selectedPostText",postList[position].kullaniciYorum)
        holder.itemView.context.startActivity(intent)
    }

}



