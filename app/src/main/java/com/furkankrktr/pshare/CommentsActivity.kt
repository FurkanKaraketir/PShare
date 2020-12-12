@file:Suppress("DEPRECATION", "DEPRECATED_IDENTITY_EQUALS")


package com.furkankrktr.pshare

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.furkankrktr.pshare.adapter.CommentRecyclerAdapter
import com.furkankrktr.pshare.databinding.ActivityCommentsBinding
import com.furkankrktr.pshare.model.Comment
import com.furkankrktr.pshare.send_notification_pack.*
import com.furkankrktr.pshare.service.glide
import com.furkankrktr.pshare.service.placeHolderYap
import com.giphy.sdk.core.models.Media
import com.giphy.sdk.ui.GPHContentType
import com.giphy.sdk.ui.Giphy
import com.giphy.sdk.ui.views.GiphyDialogFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import com.theartofdev.edmodo.cropper.CropImage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class CommentsActivity : AppCompatActivity(), GiphyDialogFragment.GifSelectionListener {

    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var selectedPost: String
    private lateinit var selectedPostEmail: String
    private lateinit var selectedPostText: String
    private lateinit var selectedPostUID: String
    private lateinit var gifOrImageBtn: ImageView
    private lateinit var secilenImageView: ImageView
    private lateinit var recyclerCommentsView: RecyclerView
    private lateinit var sendButton: FloatingActionButton
    private lateinit var commentSendEditText: EditText
    private var secilenGorsel: Uri? = null
    private var gifOrImage: Boolean? = null
    private var istenen: String = ""
    private var a: String = ""

    //true Image       false GIF
    private lateinit var recyclerCommentViewAdapter: CommentRecyclerAdapter
    private var commentList = ArrayList<Comment>()
    private lateinit var apiService: APIService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityCommentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()

        selectedPost = intent.getStringExtra("selectedPost").toString()
        selectedPostEmail = intent.getStringExtra("selectedPostEmail").toString()
        selectedPostText = intent.getStringExtra("selectedPostText").toString()
        selectedPostUID = intent.getStringExtra("selectedPostUID").toString()

        gifOrImageBtn = binding.attachCommentButton
        secilenImageView = binding.secilenCommentResimView
        recyclerCommentsView = binding.recyclerCommentsView
        commentSendEditText = binding.commentSendEditText
        sendButton = binding.sendCButton

        secilenImageView.visibility = View.GONE
        Giphy.configure(this, "Qyq8K6rBLuR2bYRetJteXkb6k7ngKUG8")
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService::class.java)

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val layoutManager = LinearLayoutManager(this)
        recyclerCommentsView.layoutManager = layoutManager
        recyclerCommentViewAdapter = CommentRecyclerAdapter(commentList)
        recyclerCommentsView.adapter = recyclerCommentViewAdapter
        val alert = AlertDialog.Builder(this)
        alert.setTitle("Resim veya GIF")
        alert.setMessage("Resim veya GIF seçiniz")

        alert.setPositiveButton("RESİM") { _, _ ->
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                //İzin Verilmedi, iste
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA),
                    1
                )


            } else {
                //İzin Var
                CropImage.activity().start(this)


            }
        }
        alert.setNegativeButton("GIF") { _, _ ->
            GiphyDialogFragment.newInstance().show(supportFragmentManager, "giphy_dialog")
        }
        gifOrImageBtn.setOnClickListener {
            alert.show()
        }
        secilenImageView.setOnClickListener {
            alert.show()
        }


        sendButton.setOnClickListener {
            if (gifOrImage == true) {
                val commentText = commentSendEditText.text.toString()
                val uuid = UUID.randomUUID()
                val gorselIsim = "${uuid}.jpg"
                val reference = storage.reference
                val gorselReference = reference.child("images").child(gorselIsim)

                if (secilenGorsel != null && commentText.isNotEmpty()) {
                    sendButton.isClickable = false
                    gorselReference.putFile(secilenGorsel!!).addOnSuccessListener {

                        val yuklenenGorselReference =
                            FirebaseStorage.getInstance().reference.child("images")
                                .child(gorselIsim)

                        yuklenenGorselReference.downloadUrl.addOnSuccessListener { uri ->

                            val downloadUrl = uri.toString()


                            val guncelKullaniciEmail = auth.currentUser!!.email.toString()

                            val tarih = Timestamp.now()
                            //veritabanı işlemleri
                            val commentHashMap = hashMapOf<String, Any>()
                            commentHashMap["kullaniciemail"] = guncelKullaniciEmail
                            commentHashMap["kullanicicomment"] = commentText
                            commentHashMap["commentAttach"] = downloadUrl
                            commentHashMap["selectedPost"] = selectedPost
                            commentHashMap["commentId"] = uuid.toString()
                            commentHashMap["tarih"] = tarih


                            database.collection("Yorumlar").add(commentHashMap)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {

                                        if (guncelKullaniciEmail != selectedPostEmail) {
                                            try {
                                                FirebaseDatabase.getInstance().reference.child("Tokens")
                                                    .child(selectedPostUID.trim()).child("token")
                                                    .addListenerForSingleValueEvent(object :
                                                        ValueEventListener {
                                                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                            val usertoken: String =
                                                                dataSnapshot.getValue(String::class.java)
                                                                    .toString()
                                                            sendNotification(
                                                                usertoken,
                                                                commentText,
                                                            )
                                                        }

                                                        override fun onCancelled(databaseError: DatabaseError) {

                                                        }
                                                    })
                                            } catch (e: Exception) {
                                                println(e.localizedMessage)
                                            }
                                        }


                                    }
                                }.addOnFailureListener { exception ->
                                    Toast.makeText(
                                        this,
                                        exception.localizedMessage,
                                        Toast.LENGTH_LONG
                                    )
                                        .show()

                                    sendButton.isClickable = true

                                }


                        }.addOnFailureListener { exception ->
                            Toast.makeText(this, exception.localizedMessage, Toast.LENGTH_LONG)
                                .show()

                            sendButton.isClickable = true


                        }
                    }.addOnFailureListener { exception ->
                        Toast.makeText(this, exception.localizedMessage, Toast.LENGTH_LONG).show()

                        sendButton.isClickable = true

                    }


                } else {
                    sendButton.isClickable = true
                    commentSendEditText.error = "Boş Yorum Atamazsın"
                }
            } else {

                val commentText = commentSendEditText.text.toString()
                val uuid = UUID.randomUUID()

                if (commentText.isNotEmpty()) {

                    sendButton.isClickable = false
                    val guncelKullaniciEmail = auth.currentUser!!.email.toString()
                    val guncelKullaniciUID = auth.currentUser!!.uid
                    val tarih = Timestamp.now()

                    val commentHashMap = hashMapOf<String, Any>()
                    commentHashMap["kullaniciemail"] = guncelKullaniciEmail
                    commentHashMap["kullanicicomment"] = commentText
                    commentHashMap["commentAttach"] = a
                    commentHashMap["selectedPost"] = selectedPost
                    commentHashMap["commentId"] = uuid.toString()
                    commentHashMap["tarih"] = tarih
                    commentHashMap["userID"] = guncelKullaniciUID


                    database.collection("Yorumlar").add(commentHashMap)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                commentSendEditText.text = null
                                recyclerCommentViewAdapter.notifyDataSetChanged()
                                verileriAl()
                                if (guncelKullaniciEmail != selectedPostEmail) {
                                    try {
                                        FirebaseDatabase.getInstance().reference.child("Tokens")
                                            .child(selectedPostUID.trim()).child("token")
                                            .addListenerForSingleValueEvent(object :
                                                ValueEventListener {
                                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                    val usertoken: String =
                                                        dataSnapshot.getValue(String::class.java)
                                                            .toString()
                                                    sendNotification(
                                                        usertoken,
                                                        commentText,
                                                    )
                                                }

                                                override fun onCancelled(databaseError: DatabaseError) {

                                                }
                                            })
                                    } catch (e: Exception) {
                                        println(e.localizedMessage)
                                    }
                                }
                            }
                        }.addOnFailureListener { exception ->
                            Toast.makeText(this, exception.localizedMessage, Toast.LENGTH_LONG)
                                .show()
                        }

                } else {
                    commentSendEditText.error = "Boş Yorum Atamazsın"

                }
            }


        }

        updateToken()
        verileriAl()

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        //İzin Yeni Verildi
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CropImage.activity().start(this)
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)

            if (resultCode == RESULT_OK) {
                secilenGorsel = result.uri
                gifOrImage = true
                secilenImageView.visibility = View.VISIBLE
                secilenImageView.setImageURI(secilenGorsel)

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val e = result.error
                Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()

            }
        }


        super.onActivityResult(requestCode, resultCode, data)
    }


    fun verileriAl() {
        database.collection("Yorumlar").whereEqualTo("selectedPost", selectedPost)
            .orderBy("tarih", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    println(exception.localizedMessage)
                } else {
                    if (snapshot != null) {
                        if (!snapshot.isEmpty) {
                            val documents = snapshot.documents


                            commentList.clear()
                            for (document in documents) {

                                try {


                                    val kullaniciComment =
                                        document.get("kullanicicomment") as String
                                    val kullaniciEmail = document.get("kullaniciemail") as String
                                    val commentId = document.get("commentId") as String
                                    val commentAttach = document.get("commentAttach") as String
                                    val userID = document.get("userID") as String
                                    val indirilenComment =
                                        Comment(
                                            kullaniciEmail,
                                            kullaniciComment,
                                            commentId,
                                            commentAttach,
                                            userID
                                        )
                                    commentList.add(indirilenComment)
                                } catch (e: Exception) {
                                    try {
                                        val kullaniciComment =
                                            document.get("kullanicicomment") as String
                                        val kullaniciEmail =
                                            document.get("kullaniciemail") as String
                                        val commentId = document.get("commentId") as String
                                        val commentAttach = document.get("commentAttach") as String
                                        val userID = "M6OZguiPKVQs6Z2qfh9HCntoKQi2"
                                        val indirilenComment =
                                            Comment(
                                                kullaniciEmail,
                                                kullaniciComment,
                                                commentId,
                                                commentAttach,
                                                userID
                                            )
                                        commentList.add(indirilenComment)
                                    } catch (e: Exception) {
                                        val kullaniciComment =
                                            document.get("kullanicicomment") as String
                                        val kullaniciEmail =
                                            document.get("kullaniciemail") as String
                                        val commentId = document.get("commentId") as String
                                        val commentAttach = ""
                                        val userID = "M6OZguiPKVQs6Z2qfh9HCntoKQi2"
                                        val indirilenComment =
                                            Comment(
                                                kullaniciEmail,
                                                kullaniciComment,
                                                commentId,
                                                commentAttach,
                                                userID
                                            )
                                        commentList.add(indirilenComment)
                                    }

                                }


                            }

                            recyclerCommentViewAdapter.notifyDataSetChanged()

                        }


                    }
                }
            }
    }

    override fun didSearchTerm(term: String) {

    }

    override fun onDismissed(selectedContentType: GPHContentType) {

    }

    override fun onGifSelected(
        media: Media,
        searchTerm: String?,
        selectedContentType: GPHContentType
    ) {
        val url = media.embedUrl!!

        val hepsi: List<String>
        hepsi = url.split('/')

        istenen = hepsi[hepsi.size - 1]
        a = "https://media.giphy.com/media/$istenen/giphy.gif"
        secilenImageView.visibility = View.VISIBLE
        secilenImageView.glide(a, placeHolderYap(applicationContext))
        gifOrImage = false
    }

    private fun updateToken() {
        val refreshToken: String = FirebaseInstanceId.getInstance().token.toString()
        val token = Token(refreshToken)
        FirebaseDatabase.getInstance().getReference("Tokens")
            .child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(token)
    }

    private fun sendNotification(usertoken: String, message: String) {
        val data = Data(
            "Postunuza Yeni Yorum",
            message,
            selectedPost,
            selectedPostEmail,
            selectedPostText,
            "",
            "",
            selectedPostUID
        )
        val sender = NotificationSender(data, usertoken)
        apiService.sendNotifcation(sender)!!.enqueue(object : Callback<MyResponse?> {

            override fun onResponse(call: Call<MyResponse?>, response: Response<MyResponse?>) {
                if (response.code() === 200) {
                    if (response.body()!!.success !== 1) {
                        Toast.makeText(this@CommentsActivity, "Failed ", Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onFailure(call: Call<MyResponse?>, t: Throwable?) {

            }
        })
    }


}