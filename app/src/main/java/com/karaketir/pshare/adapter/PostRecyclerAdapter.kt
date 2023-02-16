package com.karaketir.pshare.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.karaketir.pshare.R
import com.karaketir.pshare.databinding.PostRowBinding
import com.karaketir.pshare.model.Post
import com.karaketir.pshare.services.glide
import com.karaketir.pshare.services.placeHolderYap

open class PostRecyclerAdapter(private val postList: ArrayList<Post>) :
    RecyclerView.Adapter<PostRecyclerAdapter.PostHolder>() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth


    class PostHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = PostRowBinding.bind(itemView)
    }


    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): PostHolder {

        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.post_row, parent, false)
        return PostHolder(view)
    }

    override fun onBindViewHolder(holder: PostHolder, position: Int) {
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        with(holder) {

            if (postList.isNotEmpty() && position >= 0 && position < postList.size) {
                val myItem = postList[position]

                db.collection("User").document(myItem.postOwnerID).get().addOnSuccessListener {
                    binding.userName.text = it.get("username").toString()
                    binding.profileImage.glide(
                        it.get("profileImageURL").toString(),
                        placeHolderYap(holder.itemView.context)
                    )
                }

                binding.postDescription.text = myItem.postDescription

                if (myItem.postImageURL != "") {
                    binding.postImage.visibility = View.VISIBLE
                    binding.postImage.glide(
                        myItem.postImageURL, placeHolderYap(holder.itemView.context)
                    )
                } else {
                    binding.postImage.visibility = View.GONE
                }


            }


        }


    }

    override fun getItemCount(): Int {
        return postList.size
    }
}