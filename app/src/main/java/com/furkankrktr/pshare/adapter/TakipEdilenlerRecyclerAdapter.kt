@file:Suppress("UNCHECKED_CAST", "RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.furkankrktr.pshare.adapter

import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.furkankrktr.pshare.GorselActivity
import com.furkankrktr.pshare.R
import com.furkankrktr.pshare.databinding.RecyclerUserBinding
import com.furkankrktr.pshare.model.User
import com.furkankrktr.pshare.service.glider
import com.furkankrktr.pshare.service.placeHolderYap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class TakipEdilenlerRecyclerAdapter(private val userList: ArrayList<User>) :
    RecyclerView.Adapter<TakipEdilenlerRecyclerAdapter.UserHolder>() {
    private lateinit var database: FirebaseFirestore
    lateinit var auth: FirebaseAuth
    private lateinit var guncelKullanici: String
    private lateinit var takipArray: ArrayList<String>
    private lateinit var documentName: String

    class UserHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = RecyclerUserBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.recycler_user, parent, false)
        return UserHolder(view)
    }

    override fun onBindViewHolder(holder: UserHolder, position: Int) {
        with(holder) {
            database = FirebaseFirestore.getInstance()
            auth = FirebaseAuth.getInstance()
            if (auth.currentUser != null) {
                guncelKullanici = auth.currentUser!!.email.toString()
            }

            database.collection("Users")
                .whereEqualTo("useremail", userList[position].kullaniciEmail)
                .addSnapshotListener { snapshot, exception ->

                    if (exception == null) {
                        if (snapshot != null) {
                            if (!snapshot.isEmpty) {
                                val documents = snapshot.documents
                                for (document in documents) {
                                    binding.userNameView.text =
                                        document.get("username") as String
                                    val profile = document.get("profileImage") as String
                                    binding.userProfileImage.glider(
                                        profile,
                                        placeHolderYap(holder.itemView.context)
                                    )
                                }
                            } else {
                                binding.userNameView.text =
                                    userList[position].kullaniciEmail
                            }
                        } else {
                            binding.userNameView.text =
                                userList[position].kullaniciEmail
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
            database.collection("Users").whereEqualTo("useremail", guncelKullanici)
                .addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        TransitionManager.beginDelayedTransition(binding.userContainer)

                        binding.followButton.visibility = View.VISIBLE
                        binding.unFollowButton.visibility = View.GONE
                    } else {
                        if (snapshot != null) {
                            if (!snapshot.isEmpty) {
                                val documents = snapshot.documents
                                for (document in documents) {
                                    takipArray =
                                        document.get("takipEdilenEmailler") as ArrayList<String>
                                }
                                if (userList.size > 0 && takipArray.contains(userList[position].kullaniciEmail)) {
                                    TransitionManager.beginDelayedTransition(binding.userContainer)

                                    binding.followButton.visibility = View.GONE
                                    binding.unFollowButton.visibility = View.VISIBLE
                                } else {
                                    TransitionManager.beginDelayedTransition(binding.userContainer)

                                    binding.followButton.visibility = View.VISIBLE
                                    binding.unFollowButton.visibility = View.GONE
                                }

                            } else {
                                TransitionManager.beginDelayedTransition(binding.userContainer)

                                binding.followButton.visibility = View.VISIBLE
                                binding.unFollowButton.visibility = View.GONE

                            }
                        } else {
                            TransitionManager.beginDelayedTransition(binding.userContainer)

                            binding.followButton.visibility = View.VISIBLE
                            binding.unFollowButton.visibility = View.GONE
                        }
                    }
                }
            binding.followButton.setOnClickListener {
                val a = userList[position].kullaniciEmail
                database.collection("Users").document(documentName)
                    .update(
                        "takipEdilenEmailler",
                        FieldValue.arrayUnion(a)
                    ).addOnSuccessListener {
                        TransitionManager.beginDelayedTransition(binding.userContainer)

                       // binding.followButton.visibility = View.GONE
                      //  binding.unFollowButton.visibility = View.VISIBLE

                    }

            }

            val unfollowAlert = AlertDialog.Builder(holder.itemView.context)

            unfollowAlert.setTitle("Takibi Bırak")
            unfollowAlert.setMessage("Takibi Bırakmak İstediğinize Emin misiniz?")
            unfollowAlert.setPositiveButton(
                "TAKİBİ BIRAK"
            ) { _, _ ->
                val a = userList[position].kullaniciEmail
                database.collection("Users").document(documentName)
                    .update(
                        "takipEdilenEmailler",
                        FieldValue.arrayRemove(a)
                    ).addOnSuccessListener {
                        TransitionManager.beginDelayedTransition(binding.userContainer)

                      //  binding.followButton.visibility = View.VISIBLE
                      //  binding.unFollowButton.visibility = View.GONE
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

            binding.userProfileImage.setOnClickListener {
                val intent = Intent(holder.itemView.context, GorselActivity::class.java)
                database.collection("Users")
                    .whereEqualTo("useremail", userList[position].kullaniciEmail)
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
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }


}
