package com.furkankrktr.pshare.adapter

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.furkankrktr.pshare.*
import com.furkankrktr.pshare.model.Post
import com.furkankrktr.pshare.service.glide
import com.furkankrktr.pshare.service.placeHolderYap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.recycler_row.view.*

open class UserEmailFilterAdapter(private val postList: ArrayList<Post>) :
    RecyclerView.Adapter<UserEmailFilterAdapter.PostHolder>() {

    private lateinit var database: FirebaseFirestore
    lateinit var auth: FirebaseAuth
    private lateinit var guncelKullanici: String

    class PostHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {

        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.recycler_row, parent, false)
        return PostHolder(view)
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    override fun onBindViewHolder(holder: PostHolder, position: Int) {
        database = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            guncelKullanici = auth.currentUser!!.email.toString()
        }
        holder.itemView.recycler_row_kullanici_email.text = postList[position].kullaniciEmail
        holder.itemView.recycler_row_kullanici_yorum.text = postList[position].kullaniciYorum
        //Picasso.get().load(postList[position].gorselUrl).into(holder.itemView.recycler_row_imageview)
        holder.itemView.recycler_row_imageview.glide(
            postList[position].gorselUrl,
            placeHolderYap(holder.itemView.context)
        )
        holder.itemView.recycler_row_imageview.setOnClickListener {
            val intent = Intent(holder.itemView.context, GorselActivity::class.java)
            intent.putExtra("resim", postList[position].gorselUrl)
            holder.itemView.context.startActivity(intent)
        }
        holder.itemView.commentsButton.setOnClickListener {
            val intent = Intent(holder.itemView.context, CommentsActivity::class.java)
            intent.putExtra("selectedPost", postList[position].postId)
            holder.itemView.context.startActivity(intent)
        }
        if (holder.itemView.recycler_row_kullanici_yorum.text[0] == "#"[0]) {
            holder.itemView.recycler_row_kullanici_yorum.setTextColor(Color.parseColor("#00FFFF"))
        } else {
            holder.itemView.recycler_row_kullanici_yorum.setTextColor(Color.parseColor("#888888"))
        }


        if (holder.itemView.recycler_row_kullanici_email.text == guncelKullanici) {
            holder.itemView.recycler_row_kullanici_email.setTextColor(Color.parseColor("#00FFFF"))
        } else {
            holder.itemView.recycler_row_kullanici_email.setTextColor(Color.parseColor("#888888"))
        }




        holder.itemView.deleteButton.setOnClickListener {
            if (postList[position].kullaniciEmail == guncelKullanici || guncelKullanici == "furkankaraketir2005@gmail.com") {
                //dialog

                val alert = AlertDialog.Builder(holder.itemView.context)

                alert.setTitle("Post Sil")
                alert.setMessage("Postu Silmek İstediğinize Emin misiniz?")

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

                    })
                alert.show()

            } else {
                Toast.makeText(
                    holder.itemView.context,
                    "Yalnızca Kendi Yüklediğiniz Postları Silebilirsiniz",
                    Toast.LENGTH_SHORT
                ).show()
            }
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


}
