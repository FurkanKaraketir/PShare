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
import com.furkankrktr.pshare.adapter.ReplyRecyclerAdapter
import com.furkankrktr.pshare.databinding.ActivityRepliesBinding
import com.furkankrktr.pshare.model.Reply
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

class RepliesActivity : AppCompatActivity(), GiphyDialogFragment.GifSelectionListener {


    private lateinit var storage: FirebaseStorage
    private lateinit var selectedComment: String
    private lateinit var selectedCommentEmail: String
    private lateinit var selectedCommentText: String
    private lateinit var replyAttachmentBtn: ImageView
    private lateinit var secilenReplyIamgeView: ImageView
    private lateinit var selectedCommentUID: String
    private lateinit var selectedCommentImage: String
    private lateinit var recyclerRepliesView: RecyclerView
    private lateinit var selectedCommentImageView: ImageView
    private lateinit var replySendButton: FloatingActionButton
    private lateinit var replyToEmailText: TextView
    private lateinit var replySendEditText: EditText
    private lateinit var profileImageReplyActivity: CircleImageView
    private lateinit var replyToCommentText: TextView

    private var secilenGorsel: Uri? = null
    private var gifOrImage: Boolean? = null
    private var istenen: String = ""
    private var a: String = ""
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var recyclerReplyViewAdapter: ReplyRecyclerAdapter
    private var replyList = ArrayList<Reply>()
    private lateinit var apiService: APIService
    private var yorumYapildi: Boolean = false

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityRepliesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerRepliesView = binding.recyclerRepliesView
        replySendButton = binding.replySendButton
        replyAttachmentBtn = binding.attachReplyButton
        secilenReplyIamgeView = binding.secilenReplyResimView
        selectedCommentImageView = binding.selectedCommentImageView
        replyToEmailText = binding.replyToEmailText
        replySendEditText = binding.replySendEditText
        profileImageReplyActivity = binding.profileImageReplyActivity
        replyToCommentText = binding.replyToCommentText

        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()

        val layoutManager = LinearLayoutManager(this)
        recyclerRepliesView.layoutManager = layoutManager
        recyclerReplyViewAdapter = ReplyRecyclerAdapter(replyList)
        recyclerRepliesView.adapter = recyclerReplyViewAdapter
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService::class.java)


        secilenReplyIamgeView.visibility = View.GONE

        selectedComment = intent.getStringExtra("selectedComment").toString()
        selectedCommentEmail = intent.getStringExtra("selectedCommentEmail").toString()
        selectedCommentText = intent.getStringExtra("selectedCommentText").toString()
        selectedCommentUID = intent.getStringExtra("selectedCommentUID").toString()
        selectedCommentImage = intent.getStringExtra("selectedCommentImage").toString()

        if (selectedCommentImage == "") {
            selectedCommentImageView.visibility = View.GONE
        } else {
            selectedCommentImageView.glide(
                selectedCommentImage,
                placeHolderYap(this)
            )
        }

        database.collection("Users").whereEqualTo("useremail", selectedCommentEmail)
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
                                replyToEmailText.text =
                                    document.get("username") as String
                                val profile = document.get("profileImage") as String
                                profileImageReplyActivity.glider(profile, placeHolderYap(this))
                            }
                        } else {
                            replyToEmailText.text =
                                selectedCommentEmail
                        }
                    } else {
                        replyToEmailText.text =
                            selectedCommentEmail
                    }
                }
            }

        replyToCommentText.text = selectedCommentText

        Giphy.configure(this, "Qyq8K6rBLuR2bYRetJteXkb6k7ngKUG8")
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
        replyAttachmentBtn.setOnClickListener {
            alert.show()
        }
        secilenReplyIamgeView.setOnClickListener {
            alert.show()
        }

        replySendButton.setOnClickListener {

            if (gifOrImage == true) {
                val replyText = replySendEditText.text.toString()
                val uuid = UUID.randomUUID()
                val gorselIsim = "${uuid}.jpg"
                val reference = storage.reference
                val gorselReference = reference.child("images").child(gorselIsim)

                if (secilenGorsel != null && replyText.isNotEmpty()) {
                    replySendButton.isClickable = false
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
                            commentHashMap["kullanicireply"] = replyText
                            commentHashMap["selectedComment"] = selectedComment
                            commentHashMap["replyAttachment"] = downloadUrl
                            commentHashMap["replyId"] = uuid.toString()
                            commentHashMap["tarih"] = tarih

                            database.collection("Yanıtlar").add(commentHashMap)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {

                                        replySendEditText.hint =
                                            "Tek seferde yalnızca 1 yanıt yapılabilir"
                                        replyAttachmentBtn.visibility = View.GONE
                                        yorumYapildi = true
                                        if (guncelKullaniciEmail != selectedCommentEmail) {
                                            FirebaseDatabase.getInstance().reference.child("Tokens")
                                                .child(selectedCommentUID.trim()).child("token")
                                                .addListenerForSingleValueEvent(object :
                                                    ValueEventListener {
                                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                        val usertoken: String =
                                                            dataSnapshot.getValue(String::class.java)
                                                                .toString()
                                                        sendNotification(
                                                            usertoken,
                                                            replyText,
                                                        )
                                                    }

                                                    override fun onCancelled(databaseError: DatabaseError) {

                                                    }
                                                })
                                        }


                                    }
                                }.addOnFailureListener { exception ->
                                    Toast.makeText(
                                        this,
                                        exception.localizedMessage,
                                        Toast.LENGTH_LONG
                                    )
                                        .show()

                                    replySendButton.isClickable = true

                                }


                        }.addOnFailureListener { exception ->
                            Toast.makeText(this, exception.localizedMessage, Toast.LENGTH_LONG)
                                .show()

                            replySendButton.isClickable = true


                        }
                    }.addOnFailureListener { exception ->
                        Toast.makeText(this, exception.localizedMessage, Toast.LENGTH_LONG).show()

                        replySendButton.isClickable = true

                    }


                } else {
                    replySendButton.isClickable = true
                    replySendEditText
                    Toast.makeText(this, "Boş Yanıt Yapamazsınız", Toast.LENGTH_SHORT).show()
                }


            } else {
                val replyText = replySendEditText.text.toString()
                val uuid = UUID.randomUUID()

                if (replyText.isNotEmpty()) {

                    replySendButton.isClickable = false
                    val guncelKullaniciEmail = auth.currentUser!!.email.toString()

                    val tarih = Timestamp.now()

                    val commentHashMap = hashMapOf<String, Any>()
                    commentHashMap["kullaniciemail"] = guncelKullaniciEmail
                    commentHashMap["kullanicireply"] = replyText
                    commentHashMap["selectedComment"] = selectedComment
                    commentHashMap["replyAttachment"] = a
                    commentHashMap["replyId"] = uuid.toString()
                    commentHashMap["tarih"] = tarih

                    database.collection("Yanıtlar").add(commentHashMap)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                yorumYapildi = true
                                replySendEditText.text = null
                                replySendEditText.hint =
                                    "Tek seferde yalnızca 1 yanıt yapılabilir"
                                replyAttachmentBtn.visibility = View.GONE
                                if (guncelKullaniciEmail != selectedCommentEmail) {
                                    FirebaseDatabase.getInstance().reference.child("Tokens")
                                        .child(selectedCommentUID.trim()).child("token")
                                        .addListenerForSingleValueEvent(object :
                                            ValueEventListener {
                                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                val usertoken: String =
                                                    dataSnapshot.getValue(String::class.java)
                                                        .toString()
                                                sendNotification(
                                                    usertoken,
                                                    replyText
                                                )
                                            }

                                            override fun onCancelled(databaseError: DatabaseError) {

                                            }
                                        })
                                }


                                recyclerReplyViewAdapter.notifyDataSetChanged()
                                verileriAl()

                            }
                        }.addOnFailureListener { exception ->
                            Toast.makeText(this, exception.localizedMessage, Toast.LENGTH_LONG)
                                .show()
                        }

                } else {
                    replySendEditText.error = "Boş Yanıt Yapamazsınız"
                    replySendButton.isClickable = true
                }
            }
        }
        verileriAl()
        updateToken()


        replySendEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            @SuppressLint("RestrictedApi")
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {


            }

            @SuppressLint("RestrictedApi")
            override fun afterTextChanged(p0: Editable?) {
                if (p0.toString() != "" && replySendButton.visibility == View.INVISIBLE) {

                    val cx = replySendButton.width / 2
                    val cy = replySendButton.height / 2

                    // get the final radius for the clipping circle
                    val finalRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()

                    // create the animator for this view (the start radius is zero)
                    val anim =
                        ViewAnimationUtils.createCircularReveal(
                            replySendButton,
                            cx,
                            cy,
                            0f,
                            finalRadius
                        )
                    // make the view visible and start the animation
                    if (!yorumYapildi) {
                        replySendButton.visibility = View.VISIBLE
                        anim.start()
                    }


                    // set the view to invisible without a circular reveal animation below Lollipop

                } else if (p0.toString() == "" && replySendButton.visibility == View.VISIBLE) {
                    val cx = replySendButton.width / 2
                    val cy = replySendButton.height / 2

                    // get the initial radius for the clipping circle
                    val initialRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()

                    // create the animation (the final radius is zero)
                    val anim = ViewAnimationUtils.createCircularReveal(
                        replySendButton,
                        cx,
                        cy,
                        initialRadius,
                        0f
                    )

                    // make the view invisible when the animation is done
                    anim.addListener(object : AnimatorListenerAdapter() {

                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            replySendButton.visibility = View.INVISIBLE
                        }
                    })
                    anim.start()
                }


            }


        })

    }


    private fun updateToken() {
        val refreshToken: String = FirebaseInstanceId.getInstance().token.toString()
        val token = Token(refreshToken)
        FirebaseDatabase.getInstance().getReference("Tokens")
            .child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(token)
    }

    private fun sendNotification(usertoken: String, message: String) {
        val data = Data(
            "Yorumunuza Yeni Yanıt",
            message,
            selectedComment,
            selectedCommentEmail,
            selectedCommentText,
            selectedCommentUID,
            selectedCommentImage,
            ""
        )
        val sender = NotificationSender(data, usertoken)
        apiService.sendNotifcation(sender)!!.enqueue(object : Callback<MyResponse?> {

            override fun onResponse(call: Call<MyResponse?>, response: Response<MyResponse?>) {
                if (response.code() === 200) {
                    if (response.body()!!.success !== 1) {
                        Toast.makeText(this@RepliesActivity, "Failed ", Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onFailure(call: Call<MyResponse?>, t: Throwable?) {

            }
        })
    }

    private fun verileriAl() {
        database.collection("Yanıtlar").whereEqualTo("selectedComment", selectedComment)
            .orderBy("tarih", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    println(exception.localizedMessage)
                } else {
                    if (snapshot != null) {
                        if (!snapshot.isEmpty) {
                            val documents = snapshot.documents


                            replyList.clear()
                            for (document in documents) {
                                try {
                                    val kullaniciReply = document.get("kullanicireply") as String
                                    val kullaniciEmail = document.get("kullaniciemail") as String
                                    val replyId = document.get("replyId") as String
                                    val replyAttachment = document.get("replyAttachment") as String
                                    val indirilenReply =
                                        Reply(
                                            kullaniciEmail,
                                            kullaniciReply,
                                            replyId,
                                            replyAttachment
                                        )
                                    replyList.add(indirilenReply)
                                } catch (e: Exception) {
                                    val kullaniciReply = document.get("kullanicireply") as String
                                    val kullaniciEmail = document.get("kullaniciemail") as String
                                    val replyId = document.get("replyId") as String
                                    val replyAttachment = ""
                                    val indirilenReply =
                                        Reply(
                                            kullaniciEmail,
                                            kullaniciReply,
                                            replyId,
                                            replyAttachment
                                        )
                                    replyList.add(indirilenReply)
                                }

                            }

                            recyclerReplyViewAdapter.notifyDataSetChanged()

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
        secilenReplyIamgeView.visibility = View.VISIBLE

        secilenReplyIamgeView.glide(a, placeHolderYap(applicationContext))
        gifOrImage = false


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
                secilenReplyIamgeView.visibility = View.VISIBLE
                gifOrImage = true
                secilenReplyIamgeView.setImageURI(secilenGorsel)

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val e = result.error
                Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()

            }
        }


        super.onActivityResult(requestCode, resultCode, data)
    }

}