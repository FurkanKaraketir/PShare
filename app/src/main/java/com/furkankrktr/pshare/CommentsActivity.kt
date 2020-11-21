package com.furkankrktr.pshare

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.furkankrktr.pshare.adapter.CommentRecyclerAdapter
import com.furkankrktr.pshare.model.Comment
import com.furkankrktr.pshare.service.glide
import com.furkankrktr.pshare.service.placeHolderYap
import com.giphy.sdk.core.models.Media
import com.giphy.sdk.ui.GPHContentType
import com.giphy.sdk.ui.Giphy
import com.giphy.sdk.ui.views.GiphyDialogFragment
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_comments.*
import java.util.*
import kotlin.collections.ArrayList

class CommentsActivity : AppCompatActivity(), GiphyDialogFragment.GifSelectionListener {

    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var selectedPost: String
    private lateinit var gifOrImageBtn: ImageView
    private lateinit var secilenImageView: ImageView
    private var secilenGorsel: Uri? = null
    private var gifOrImage: Boolean? = null
    private var istenen: String = ""
    private var a: String = ""

    //true Image       false GIF
    private lateinit var recyclerCommentViewAdapter: CommentRecyclerAdapter
    private var commentList = ArrayList<Comment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()
        val sendButton = findViewById<ImageView>(R.id.sendCButton)
        selectedPost = intent.getStringExtra("selectedPost").toString()

        gifOrImageBtn = findViewById(R.id.attachCommentButton)
        secilenImageView = findViewById(R.id.secilenCommentResimView)
        secilenImageView.visibility = View.GONE
        Giphy.configure(this, "Qyq8K6rBLuR2bYRetJteXkb6k7ngKUG8")
        verileriAl()
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val layoutManager = LinearLayoutManager(this)
        recyclerCommentsView.layoutManager = layoutManager
        recyclerCommentViewAdapter = CommentRecyclerAdapter(commentList)
        recyclerCommentsView.adapter = recyclerCommentViewAdapter
        val alert = AlertDialog.Builder(this)
        alert.setTitle("Resim veya GIF")
        alert.setMessage("Resim veya GIF seçiniz")

        alert.setPositiveButton("RESİM", DialogInterface.OnClickListener { _, _ ->
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
        })
        alert.setNegativeButton("GIF", DialogInterface.OnClickListener { _, _ ->
            GiphyDialogFragment.newInstance().show(supportFragmentManager, "giphy_dialog")
        })
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
                Toast.makeText(
                    this,
                    "Tek Seferde Yalnızca 1 Yorum Yapabilirsin",
                    Toast.LENGTH_SHORT
                )
                    .show()
                if (secilenGorsel != null && commentText.isNotEmpty()) {
                    sendButton.isClickable = false
                    gorselReference.putFile(secilenGorsel!!).addOnSuccessListener { _ ->

                        val yuklenenGorselReference =
                            FirebaseStorage.getInstance().reference.child("images")
                                .child(gorselIsim)

                        yuklenenGorselReference.downloadUrl.addOnSuccessListener { uri ->
                            Toast.makeText(
                                this,
                                "Paylaşılıyor, lütfen bekleyiniz...",
                                Toast.LENGTH_SHORT
                            ).show()
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
                                        Toast.makeText(this, "Yorum Yapıldı", Toast.LENGTH_LONG)
                                            .show()
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
                    Toast.makeText(this, "Boş Yorum Yapamazsınız", Toast.LENGTH_SHORT).show()
                }
            } else {

                val commentText = commentSendEditText.text.toString()
                val uuid = UUID.randomUUID()
                Toast.makeText(
                    this,
                    "Tek Seferde Yalnızca 1 Yorum Yapabilirsin",
                    Toast.LENGTH_SHORT
                )
                    .show()
                if (commentText.isNotEmpty()) {
                    Toast.makeText(
                        this,
                        "Paylaşılıyor, lütfen bekleyiniz...",
                        Toast.LENGTH_SHORT
                    ).show()
                    sendButton.isClickable = false
                    val guncelKullaniciEmail = auth.currentUser!!.email.toString()

                    val tarih = Timestamp.now()

                    val commentHashMap = hashMapOf<String, Any>()
                    commentHashMap["kullaniciemail"] = guncelKullaniciEmail
                    commentHashMap["kullanicicomment"] = commentText
                    commentHashMap["commentAttach"] = a
                    commentHashMap["selectedPost"] = selectedPost
                    commentHashMap["commentId"] = uuid.toString()
                    commentHashMap["tarih"] = tarih

                    database.collection("Yorumlar").add(commentHashMap)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                commentSendEditText.text = null
                                Toast.makeText(this, "Yorum Yapıldı", Toast.LENGTH_LONG).show()
                                recyclerCommentViewAdapter.notifyDataSetChanged()
                                verileriAl()

                            }
                        }.addOnFailureListener { exception ->
                            Toast.makeText(this, exception.localizedMessage, Toast.LENGTH_LONG)
                                .show()
                        }

                } else {
                    Toast.makeText(this, "Boş Yorum Atamazsın", Toast.LENGTH_LONG).show()

                }
            }


        }
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
                                    val indirilenComment =
                                        Comment(
                                            kullaniciEmail,
                                            kullaniciComment,
                                            commentId,
                                            commentAttach
                                        )
                                    commentList.add(indirilenComment)
                                } catch (e: Exception) {


                                    val kullaniciComment =
                                        document.get("kullanicicomment") as String
                                    val kullaniciEmail = document.get("kullaniciemail") as String
                                    val commentId = document.get("commentId") as String
                                    val commentAttach = ""
                                    val indirilenComment =
                                        Comment(
                                            kullaniciEmail,
                                            kullaniciComment,
                                            commentId,
                                            commentAttach
                                        )
                                    commentList.add(indirilenComment)
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


}