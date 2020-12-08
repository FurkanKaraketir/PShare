package com.furkankrktr.pshare

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.furkankrktr.pshare.databinding.ActivityProfileBinding
import com.furkankrktr.pshare.service.glide
import com.furkankrktr.pshare.service.placeHolderYap
import com.giphy.sdk.core.models.Media
import com.giphy.sdk.ui.GPHContentType
import com.giphy.sdk.ui.Giphy
import com.giphy.sdk.ui.views.GiphyDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.theartofdev.edmodo.cropper.CropImage
import java.util.*

class ProfileActivity : AppCompatActivity(),
    GiphyDialogFragment.GifSelectionListener {

    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var guncelKullanici: String
    private lateinit var profileImageURL: String
    private lateinit var documentName: String

    private lateinit var userNameChangeEditText: EditText
    private lateinit var userNameEditButton: ImageView
    private lateinit var profileImageAdd: ImageView
    private lateinit var progressCircularProfile: ProgressBar
    private lateinit var userNameView: TextView
    private lateinit var kaydetBtn: Button

    private var istenen: String = ""
    private var a: String = ""
    private var secilenGorsel: Uri? = null
    private var gifOrImage: Boolean? = null
    private lateinit var userName: String

    private var i: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        kaydetBtn = binding.kaydetBtn
        userNameEditButton = binding.userNameEditButton
        profileImageAdd = binding.profileImageAdd
        userNameChangeEditText = binding.userNameChangeEditText
        progressCircularProfile = binding.progressCircularProfile
        userNameView = binding.userNameView


        userNameChangeEditText.visibility = View.GONE
        kaydetBtn.visibility = View.GONE
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()
        if (auth.currentUser != null) {
            guncelKullanici = auth.currentUser!!.email.toString()
        }
        supportActionBar?.title = "Profil"
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        userNameEditButton.setOnClickListener {
            userNameChangeEditText.visibility = View.VISIBLE
            kaydetBtn.visibility = View.VISIBLE
            i = 1
        }
        val alert = AlertDialog.Builder(this)
        alert.setTitle("Resim veya GIF")
        alert.setMessage("Resim veya GIF seçiniz")
        Giphy.configure(this, "Qyq8K6rBLuR2bYRetJteXkb6k7ngKUG8")

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
        profileImageAdd.setOnClickListener {
            kaydetBtn.visibility = View.VISIBLE
            alert.show()
        }

        kaydetBtn.setOnClickListener {
            save()
        }

        verileriAl()

    }

    private fun save() {


        if (i == 1) {
            userName = userNameChangeEditText.text.toString()

            if (userName.isNotEmpty()) {
                userNameChangeEditText.error = null
                send()
            } else {
                userNameChangeEditText.error = "Bu Alanı Boş Bırakamazsın"
            }

        } else {
            send()
        }


    }

    private fun send() {
        if (gifOrImage == true) {
            //depo işlemleri


            //UUID


            val uuid = UUID.randomUUID()
            val gorselIsim = "${uuid}.jpg"
            val reference = storage.reference
            val gorselReference = reference.child("images").child(gorselIsim)

            if (secilenGorsel != null) {
                kaydetBtn.isClickable = false


                progressCircularProfile.visibility = View.VISIBLE
                gorselReference.putFile(secilenGorsel!!).addOnSuccessListener {

                    val yuklenenGorselReference =
                        FirebaseStorage.getInstance().reference.child("images")
                            .child(gorselIsim)

                    yuklenenGorselReference.downloadUrl.addOnSuccessListener { uri ->

                        a = uri.toString()
                        database.collection("Users").document(documentName)
                            .update("profileImage", a).addOnSuccessListener {
                                database.collection("Users").document(documentName)
                                    .update("username", userName).addOnSuccessListener {
                                        progressCircularProfile.visibility = View.GONE
                                        finish()
                                    }
                            }
                    }.addOnFailureListener { exception ->
                        Toast.makeText(this, exception.localizedMessage, Toast.LENGTH_LONG)
                            .show()
                        progressCircularProfile.visibility = View.GONE

                        kaydetBtn.isClickable = true


                    }
                }.addOnFailureListener { exception ->
                    Toast.makeText(this, exception.localizedMessage, Toast.LENGTH_LONG).show()
                    progressCircularProfile.visibility = View.GONE

                    kaydetBtn.isClickable = true

                }
            } else if (secilenGorsel == null) {
                kaydetBtn.isClickable = true
                progressCircularProfile.visibility = View.GONE

                Toast.makeText(this, "Lütfen Bir Görsel Seçiniz", Toast.LENGTH_SHORT).show()
            }


        } else {
            progressCircularProfile.visibility = View.VISIBLE

            database.collection("Users").document(documentName).update("profileImage", a)
                .addOnSuccessListener {
                    database.collection("Users").document(documentName).update("username", userName)
                        .addOnSuccessListener {
                            progressCircularProfile.visibility = View.GONE
                            finish()
                        }
                }

        }
    }

    private fun verileriAl() {
        database.collection("Users").whereEqualTo("useremail", guncelKullanici)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Toast.makeText(this, exception.localizedMessage, Toast.LENGTH_SHORT).show()
                } else {
                    if (snapshot != null) {
                        if (!snapshot.isEmpty) {
                            val documents = snapshot.documents
                            for (document in documents) {
                                documentName = document.id
                                profileImageURL = document.get("profileImage") as String
                                val serverUserName = document.get("username") as String
                                if (profileImageURL == "") {
                                    profileImageURL =
                                        "https://media.giphy.com/media/VBfFv9oOZAvvi/giphy.gif"
                                }
                                a = profileImageURL

                                userNameView.text = serverUserName
                                userName = serverUserName
                                profileImageAdd.glide(profileImageURL, placeHolderYap(this))
                            }
                        }
                    }
                }
            }
    }

    override fun didSearchTerm(term: String) {

    }

    override fun onDismissed(selectedContentType: GPHContentType) {

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
                profileImageAdd.setImageURI(secilenGorsel)

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val e = result.error
                Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()

            }
        }


        super.onActivityResult(requestCode, resultCode, data)
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
        profileImageAdd.glide(a, placeHolderYap(applicationContext))
        gifOrImage = false
    }
}