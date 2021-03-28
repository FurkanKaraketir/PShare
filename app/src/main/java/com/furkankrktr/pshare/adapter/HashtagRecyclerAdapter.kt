@file:Suppress("UNCHECKED_CAST", "RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

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
import com.furkankrktr.pshare.databinding.RecyclerRowBinding
import com.furkankrktr.pshare.model.Post
import com.furkankrktr.pshare.service.glide
import com.furkankrktr.pshare.service.glider
import com.furkankrktr.pshare.service.placeHolderYap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

open class HashtagRecyclerAdapter(private val postList: ArrayList<Post>) :
    RecyclerView.Adapter<HashtagRecyclerAdapter.PostHolder>() {


    private lateinit var database: FirebaseFirestore
    lateinit var auth: FirebaseAuth
    private lateinit var guncelKullanici: String
    private lateinit var documentName: String

    private lateinit var takipArray: ArrayList<String>

    class PostHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = RecyclerRowBinding.bind(itemView)
    }

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
        with(holder) {


            database = FirebaseFirestore.getInstance()
            auth = FirebaseAuth.getInstance()
            if (auth.currentUser != null) {
                guncelKullanici = auth.currentUser!!.email.toString()
            }

            database.collection("Users")
                .whereEqualTo("useremail", postList[position].kullaniciEmail)
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
                                    binding.recyclerRowKullaniciEmail.text =
                                        document.get("username") as String
                                    val profile = document.get("profileImage") as String
                                    binding.profileImage.glider(
                                        profile,
                                        placeHolderYap(holder.itemView.context)
                                    )
                                }
                            } else {
                                binding.recyclerRowKullaniciEmail.text =
                                    postList[position].kullaniciEmail
                            }
                        } else {
                            binding.recyclerRowKullaniciEmail.text =
                                postList[position].kullaniciEmail
                        }
                    }
                }
            database.collection("Users").whereEqualTo("useremail", guncelKullanici)
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
                                    documentName = document.id
                                }
                            }
                        }
                    }
                }

            binding.followButton.setOnClickListener {
                database.collection("Users").document(documentName)
                    .update(
                        "takipEdilenEmailler",
                        FieldValue.arrayUnion(postList[position].kullaniciEmail)
                    ).addOnSuccessListener {
                        binding.followButton.visibility = View.GONE
                        binding.unFollowButton.visibility = View.VISIBLE
                    }
            }
            val unfollowAlert = AlertDialog.Builder(holder.itemView.context)

            unfollowAlert.setTitle("Takibi Bırak")
            unfollowAlert.setMessage("Takibi Bırakmak İstediğinize Emin misiniz?")
            unfollowAlert.setPositiveButton(
                "TAKİBİ BIRAK"
            ) { _, _ ->
                database.collection("Users").document(documentName)
                    .update(
                        "takipEdilenEmailler",
                        FieldValue.arrayRemove(postList[position].kullaniciEmail)
                    ).addOnSuccessListener {
                        binding.followButton.visibility = View.VISIBLE
                        binding.unFollowButton.visibility = View.GONE
                    }
            }
            unfollowAlert.setNegativeButton("İPTAL") { _, _ ->
                Toast.makeText(
                    holder.itemView.context,
                    "İşlem iptal edildi",
                    Toast.LENGTH_SHORT
                ).show()
            }

            binding.unFollowButton.setOnClickListener {
                unfollowAlert.show()
            }


            if (guncelKullanici == postList[position].kullaniciEmail) {
                binding.followButton.visibility = View.GONE
                binding.unFollowButton.visibility = View.GONE
                binding.deleteButton.visibility = View.VISIBLE
            } else {
                database.collection("Users").whereEqualTo("useremail", guncelKullanici)
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
                                        takipArray =
                                            document.get("takipEdilenEmailler") as ArrayList<String>
                                    }
                                    if (takipArray.contains(postList[position].kullaniciEmail)) {

                                        binding.followButton.visibility = View.GONE
                                        binding.unFollowButton.visibility = View.VISIBLE
                                    } else {
                                        binding.followButton.visibility = View.VISIBLE
                                        binding.unFollowButton.visibility = View.GONE
                                    }

                                } else {
                                    binding.followButton.visibility = View.VISIBLE
                                    binding.unFollowButton.visibility = View.GONE

                                }
                            } else {
                                binding.followButton.visibility = View.VISIBLE
                                binding.unFollowButton.visibility = View.GONE
                            }
                        }
                    }
            }
            binding.recyclerRowKullaniciYorum.text = postList[position].kullaniciYorum

            if (postList[position].gorselUrl == "") {
                binding.recyclerRowImageView.visibility = View.GONE
            } else {
                binding.recyclerRowImageView.visibility = View.VISIBLE
                binding.recyclerRowImageView.glide(
                    postList[position].gorselUrl,
                    placeHolderYap(holder.itemView.context)
                )
            }

            binding.recyclerRowImageView.setOnClickListener {
                val intent = Intent(holder.itemView.context, GorselActivity::class.java)
                intent.putExtra("resim", postList[position].gorselUrl)
                holder.itemView.context.startActivity(intent)
            }


            binding.commentsButton.setOnClickListener {
                commentGit(holder, position)
            }
            binding.commentCountText.setOnClickListener {
                commentGit(holder, position)
            }
            binding.profileImage.setOnClickListener {
                val intent = Intent(holder.itemView.context, GorselActivity::class.java)
                database.collection("Users")
                    .whereEqualTo("useremail", postList[position].kullaniciEmail)
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
                                        val profile = document.get("profileImage") as String
                                        intent.putExtra("resim", profile)
                                        holder.itemView.context.startActivity(intent)
                                    }
                                }
                            }
                        }
                    }

            }



            if (postList[position].kullaniciEmail == guncelKullanici) {
                binding.deleteButton.visibility = View.VISIBLE
            } else {
                binding.deleteButton.visibility = View.GONE
            }

            binding.deleteButton.setOnClickListener {
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
                                binding.commentCountText.text = "${documents.size} Yorum"

                            } else {
                                binding.commentCountText.text = "0 Yorum"
                            }
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
        intent.putExtra("selectedPostEmail", postList[position].kullaniciEmail)
        intent.putExtra("selectedPostUID", postList[position].kullaniciUID)
        intent.putExtra("selectedPostText", postList[position].kullaniciYorum)
        holder.itemView.context.startActivity(intent)
    }

}



