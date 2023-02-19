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
import com.karaketir.pshare.databinding.FollowRowBinding
import com.karaketir.pshare.model.User
import com.karaketir.pshare.services.glide
import com.karaketir.pshare.services.openLink
import com.karaketir.pshare.services.placeHolderYap
import java.util.UUID

class FollowRecyclerAdapter(private val userList: ArrayList<User>) :
    RecyclerView.Adapter<FollowRecyclerAdapter.UserHolder>() {
    class UserHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = FollowRowBinding.bind(itemView)
    }

    private lateinit var database: FirebaseFirestore
    lateinit var auth: FirebaseAuth
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.follow_row, parent, false)
        return UserHolder(view)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: UserHolder, position: Int) {

        database = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        with(holder) {

            if (userList.isNotEmpty() && position <= userList.size) {
                val myItem = userList[position]

                binding.userNameRowView.text = myItem.username
                binding.userProfileImageRow.glide(
                    myItem.userProfilePhotoURL, placeHolderYap(holder.itemView.context)
                )
                binding.userProfileImageRow.setOnClickListener {
                    openLink(myItem.userProfilePhotoURL, holder.itemView.context)
                }
                binding.userNameRowView.setOnClickListener {
                    val intent =
                        Intent(holder.itemView.context, UserFilteredPostsActivity::class.java)
                    intent.putExtra("postOwnerID", myItem.userID)
                    holder.itemView.context.startActivity(intent)
                }
                database.collection("Followings").whereEqualTo("main", auth.uid.toString())
                    .whereEqualTo("followsWho", myItem.userID).addSnapshotListener { value, _ ->
                        if (value != null && !value.isEmpty) {
                            binding.followRowButton.visibility = View.GONE
                            binding.unFollowRowButton.visibility = View.VISIBLE

                        } else {
                            binding.followRowButton.visibility = View.VISIBLE
                            binding.unFollowRowButton.visibility = View.GONE
                        }

                        if (myItem.userID == auth.uid.toString()) {
                            binding.moreOptionsFollow.visibility = View.GONE
                            binding.followRowButton.visibility = View.GONE
                            binding.unFollowRowButton.visibility = View.GONE
                        } else {
                            binding.moreOptionsFollow.visibility = View.VISIBLE
                        }

                    }

                binding.moreOptionsFollow.setOnClickListener {
                    val popup = PopupMenu(holder.itemView.context, binding.moreOptionsFollow)
                    popup.inflate(R.menu.post_options_menu)

                    popup.setOnMenuItemClickListener(object : MenuItem.OnMenuItemClickListener,
                        PopupMenu.OnMenuItemClickListener {
                        override fun onMenuItemClick(item: MenuItem): Boolean {
                            return when (item.itemId) {
                                R.id.blockButton -> {
                                    val documentID = UUID.randomUUID().toString()
                                    val data = hashMapOf(
                                        "main" to auth.uid.toString(), "blocksWho" to myItem.userID
                                    )

                                    val blockAlert = AlertDialog.Builder(holder.itemView.context)
                                    blockAlert.setTitle("Engelle")
                                    blockAlert.setMessage("Engellemek İstedğinizden Emin misiniz?")
                                    blockAlert.setPositiveButton("Engelle") { _, _ ->

                                        database.collection("Blocks").document(documentID).set(data)
                                            .addOnSuccessListener {
                                                database.collection("Followings")
                                                    .whereEqualTo("main", auth.uid.toString())
                                                    .whereEqualTo("followsWho", myItem.userID)
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
                                                            ).whereEqualTo("main", myItem.userID)
                                                            .addSnapshotListener { value2, _ ->
                                                                if (value2 != null) {
                                                                    for (j in value2) {
                                                                        database.collection("Followings")
                                                                            .document(j.id).delete()
                                                                    }
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

                val documentName = UUID.randomUUID().toString()

                binding.followRowButton.setOnClickListener {

                    val followAlert = AlertDialog.Builder(holder.itemView.context)
                    followAlert.setTitle("Takip Et")
                    followAlert.setMessage("Takip Etmek İstedğinizden Emin misiniz?")
                    followAlert.setPositiveButton("Takip Et") { _, _ ->
                        val data =
                            hashMapOf("main" to auth.uid.toString(), "followsWho" to myItem.userID)
                        database.collection("Followings").document(documentName).set(data)

                    }

                    followAlert.setNegativeButton("İptal") { _, _ ->

                    }
                    followAlert.show()


                }
                binding.unFollowRowButton.setOnClickListener {

                    val unFollowAlert = AlertDialog.Builder(holder.itemView.context)
                    unFollowAlert.setTitle("Takibi Bırak")
                    unFollowAlert.setMessage("Takibi Bırakmak İstediğinize Emin misiniz?")
                    unFollowAlert.setPositiveButton("Takibi Bırak") { _, _ ->
                        database.collection("Followings").whereEqualTo("main", auth.uid.toString())
                            .whereEqualTo("followsWho", myItem.userID)
                            .addSnapshotListener { value, _ ->
                                if (value != null) {
                                    for (i in value) {
                                        database.collection("Followings").document(i.id).delete()
                                    }
                                }
                            }

                    }

                    unFollowAlert.setNegativeButton("İptal") { _, _ ->

                    }
                    unFollowAlert.show()


                }


            }

        }
    }


}