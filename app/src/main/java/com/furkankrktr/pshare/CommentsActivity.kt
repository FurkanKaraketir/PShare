@file:Suppress("DEPRECATION", "DEPRECATED_IDENTITY_EQUALS")


package com.furkankrktr.pshare

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewAnimationUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
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
import com.furkankrktr.pshare.service.glider
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
import de.hdodenhof.circleimageview.CircleImageView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.hypot

class CommentsActivity : AppCompatActivity(), GiphyDialogFragment.GifSelectionListener {

    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var selectedPost: String
    private lateinit var selectedPostEmail: String
    private lateinit var selectedPostText: String
    private lateinit var selectedPostUID: String
    private lateinit var selectedPostImage: String
    private lateinit var gifOrImageBtn: ImageView
    private lateinit var secilenImageView: ImageView
    private lateinit var profileImageCommentActivity: CircleImageView
    private lateinit var recyclerCommentsView: RecyclerView
    private lateinit var selectedPostImageView: ImageView
    private lateinit var sendButton: FloatingActionButton
    private lateinit var commentSendEditText: EditText
    private lateinit var commentToPostText: TextView
    private lateinit var profile: String
    private lateinit var commentToEmailText: TextView
    private var secilenGorsel: Uri? = null
    private var gifOrImage: Boolean? = null
    private var istenen: String = ""
    private var a: String = ""
    private var yorumYapildi: Boolean = false

    //true Image       false GIF
    private lateinit var recyclerCommentViewAdapter: CommentRecyclerAdapter
    private var commentList = ArrayList<Comment>()
    private lateinit var apiService: APIService

    @SuppressLint("RestrictedApi")
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
        selectedPostImage = intent.getStringExtra("selectedPostImage").toString()



        gifOrImageBtn = binding.attachCommentButton
        secilenImageView = binding.secilenCommentResimView
        profileImageCommentActivity = binding.profileImageCommentActivity
        selectedPostImageView = binding.selectedPostImageView
        commentToPostText = binding.commentToPostText
        recyclerCommentsView = binding.recyclerCommentsView
        commentSendEditText = binding.commentSendEditText
        commentToEmailText = binding.commentToEmailText
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

        if (selectedPostImage == "") {
            selectedPostImageView.visibility = View.GONE
        } else {
            selectedPostImageView.glide(
                selectedPostImage,
                placeHolderYap(this)
            )
        }

        database.collection("Users").whereEqualTo("useremail", selectedPostEmail)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Toast.makeText(
                        this,
                        exception.localizedMessage,
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    if (snapshot != null) {
                        if (!snapshot.isEmpty) {
                            val documents = snapshot.documents
                            for (document in documents) {
                                commentToEmailText.text =
                                    document.get("username") as String
                                profile = document.get("profileImage") as String
                                profileImageCommentActivity.glider(profile, placeHolderYap(this))
                                profileImageCommentActivity.setOnClickListener {
                                    val intent = Intent(this, GorselActivity::class.java)
                                    intent.putExtra("resim", profile)
                                    startActivity(intent)
                                }
                            }
                        } else {
                            commentToEmailText.text =
                                selectedPostEmail
                        }
                    } else {
                        commentToEmailText.text =
                            selectedPostEmail
                    }
                }
            }

        selectedPostImageView.setOnClickListener {
            val intent = Intent(this, GorselActivity::class.java)
            intent.putExtra("resim", selectedPostImage)
            startActivity(intent)
        }

        commentToPostText.text = selectedPostText

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

        commentSendEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            @SuppressLint("RestrictedApi")
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {


            }

            @SuppressLint("RestrictedApi")
            override fun afterTextChanged(p0: Editable?) {
                if (p0.toString() != "" && sendButton.visibility == View.INVISIBLE) {

                    val cx = sendButton.width / 2
                    val cy = sendButton.height / 2

                    // get the final radius for the clipping circle
                    val finalRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()

                    // create the animator for this view (the start radius is zero)
                    val anim =
                        ViewAnimationUtils.createCircularReveal(sendButton, cx, cy, 0f, finalRadius)
                    // make the view visible and start the animation
                    if (!yorumYapildi) {
                        sendButton.visibility = View.VISIBLE
                        anim.start()
                    }


                    // set the view to invisible without a circular reveal animation below Lollipop

                } else if (p0.toString() == "" && sendButton.visibility == View.VISIBLE) {
                    val cx = sendButton.width / 2
                    val cy = sendButton.height / 2

                    // get the initial radius for the clipping circle
                    val initialRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()

                    // create the animation (the final radius is zero)
                    val anim = ViewAnimationUtils.createCircularReveal(
                        sendButton,
                        cx,
                        cy,
                        initialRadius,
                        0f
                    )

                    // make the view invisible when the animation is done
                    anim.addListener(object : AnimatorListenerAdapter() {

                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            sendButton.visibility = View.INVISIBLE
                        }
                    })
                    anim.start()
                }


            }


        })
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
                                        commentSendEditText.text = null
                                        commentSendEditText.hint =
                                            "Tek seferde yalnızca 1 yorum yapılabilir"
                                        yorumYapildi = true
                                        gifOrImageBtn.visibility = View.GONE
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
                                                                selectedPostImage
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
                                commentSendEditText.hint =
                                    "Tek seferde yalnızca 1 yorum yapılabilir"
                                yorumYapildi = true
                                gifOrImageBtn.visibility = View.GONE
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
                                                        selectedPostImage
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

    private fun sendNotification(usertoken: String, message: String, selectedCommentImage: String) {
        val data = Data(
            "Postunuza Yeni Yorum",
            message,
            selectedPost,
            selectedPostEmail,
            selectedPostText,
            "",
            selectedCommentImage,
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