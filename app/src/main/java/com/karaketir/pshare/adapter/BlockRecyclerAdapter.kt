package com.karaketir.pshare.adapter

import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.karaketir.pshare.R
import com.karaketir.pshare.UserFilteredPostsActivity
import com.karaketir.pshare.databinding.BlockRowBinding
import com.karaketir.pshare.model.User
import com.karaketir.pshare.services.glideCircle
import com.karaketir.pshare.services.openLink
import com.karaketir.pshare.services.placeHolderYap
import java.util.UUID

class BlockRecyclerAdapter(private val userList: ArrayList<User>) :
    RecyclerView.Adapter<BlockRecyclerAdapter.UserHolder>() {
    class UserHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = BlockRowBinding.bind(itemView)
    }

    private lateinit var database: FirebaseFirestore
    lateinit var auth: FirebaseAuth

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.block_row, parent, false)
        return UserHolder(view)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: UserHolder, position: Int) {


        database = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        with(holder) {
            val myBinding = binding

            if (userList.isNotEmpty() && position <= userList.size) {
                val myItem = userList[position]

                myBinding.userNameBlockRowView.text = myItem.username
                myBinding.userProfileImageBlockRow.glideCircle(
                    myItem.userProfilePhotoURL, placeHolderYap(holder.itemView.context)
                )
                myBinding.userProfileImageBlockRow.setOnClickListener {
                    openLink(myItem.userProfilePhotoURL, holder.itemView.context)
                }

                myBinding.userNameBlockRowView.setOnClickListener {
                    val intent =
                        Intent(holder.itemView.context, UserFilteredPostsActivity::class.java)
                    intent.putExtra("postOwnerID", myItem.userID)
                    holder.itemView.context.startActivity(intent)
                }

                database.collection("Blocks").whereEqualTo("main", auth.uid.toString())
                    .whereEqualTo("blocksWho", myItem.userID).addSnapshotListener { value, _ ->
                        if (value != null && !value.isEmpty) {
                            myBinding.blockRowButton.visibility = View.GONE
                            myBinding.unBlockRowButton.visibility = View.VISIBLE
                        } else {
                            myBinding.blockRowButton.visibility = View.VISIBLE
                            myBinding.unBlockRowButton.visibility = View.GONE
                        }
                    }


                val documentName = UUID.randomUUID().toString()

                myBinding.blockRowButton.setOnClickListener {

                    val blockAlert = AlertDialog.Builder(holder.itemView.context)
                    blockAlert.setTitle("Engelle")
                    blockAlert.setMessage("Engellemek İstedğinizden Emin misiniz?")
                    blockAlert.setPositiveButton("Engelle") { _, _ ->
                        val data =
                            hashMapOf("main" to auth.uid.toString(), "blocksWho" to myItem.userID)
                        database.collection("Blocks").document(documentName).set(data)

                        database.collection("Followings").whereEqualTo("main", auth.uid.toString())
                            .whereEqualTo("followsWho", myItem.userID)
                            .addSnapshotListener { value, _ ->
                                if (value != null) {
                                    for (i in value) {
                                        database.collection("Followings").document(i.id).delete()
                                    }
                                }
                                database.collection("Followings")
                                    .whereEqualTo("followsWho", auth.uid.toString())
                                    .whereEqualTo("main", myItem.userID)
                                    .addSnapshotListener { value2, _ ->
                                        if (value2 != null) {
                                            for (j in value2) {
                                                database.collection("Followings").document(j.id)
                                                    .delete()
                                            }
                                        }
                                    }
                            }
                    }

                    blockAlert.setNegativeButton("İptal") { _, _ ->

                    }
                    blockAlert.show()


                }
                myBinding.unBlockRowButton.setOnClickListener {

                    val unBlockAlert = AlertDialog.Builder(holder.itemView.context)
                    unBlockAlert.setTitle("Engeli Kaldır")
                    unBlockAlert.setMessage("Engeli Kaldırmak İstediğinize Emin misiniz?")
                    unBlockAlert.setPositiveButton("Engeli Kaldır") { _, _ ->
                        database.collection("Blocks").whereEqualTo("main", auth.uid.toString())
                            .whereEqualTo("blocksWho", myItem.userID)
                            .addSnapshotListener { value, _ ->
                                if (value != null) {
                                    for (i in value) {
                                        database.collection("Blocks").document(i.id).delete()
                                    }
                                }
                            }

                    }

                    unBlockAlert.setNegativeButton("İptal") { _, _ ->

                    }
                    unBlockAlert.show()


                }


            }
        }


    }

}