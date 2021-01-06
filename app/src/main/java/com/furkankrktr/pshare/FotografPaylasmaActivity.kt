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
import com.furkankrktr.pshare.databinding.ActivityFotografPaylasmaBinding
import com.furkankrktr.pshare.service.glide
import com.furkankrktr.pshare.service.placeHolderYap
import com.giphy.sdk.core.models.Media
import com.giphy.sdk.ui.GPHContentType
import com.giphy.sdk.ui.Giphy
import com.giphy.sdk.ui.views.GiphyDialogFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.theartofdev.edmodo.cropper.CropImage
import java.util.*

open class FotografPaylasmaActivity : AppCompatActivity(),
    GiphyDialogFragment.GifSelectionListener {
    private var istenen: String = ""
    private var a: String = ""
    private var secilenGorsel: Uri? = null
    private var gifOrImage: Boolean? = null

    private lateinit var secilenPostImageView: ImageView
    private lateinit var paylasButton: FloatingActionButton
    private lateinit var imageSec: ImageView
    private lateinit var postPaylasTextView: TextView
    private lateinit var progressCircular: ProgressBar
    private lateinit var yorumText: EditText

    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityFotografPaylasmaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()

        paylasButton = binding.paylasButton
        imageSec = binding.imageView
        secilenPostImageView = binding.secilenPostResimView
        postPaylasTextView = binding.postPaylasTextView
        progressCircular = binding.progressCircular
        yorumText = binding.yorumText


        secilenPostImageView.visibility = View.GONE
        paylasButton.isClickable = true

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
        imageSec.setOnClickListener {
            alert.show()
        }
        secilenPostImageView.setOnClickListener {
            alert.show()
        }
        paylasButton.setOnClickListener {
            paylas()
        }
        postPaylasTextView.setOnClickListener {
            paylas()
        }
    }

    private fun paylas() {
        if (gifOrImage == true) {
            //depo işlemleri
            val spinner = progressCircular


            //UUID


            val uuid = UUID.randomUUID()
            val gorselIsim = "${uuid}.jpg"
            val postId = "$uuid"
            val reference = storage.reference
            val gorselReference = reference.child("images").child(gorselIsim)

            val kullaniciYorum = yorumText.text.toString()
            if (secilenGorsel != null && kullaniciYorum.isNotEmpty()) {
                paylasButton.isClickable = false
                spinner.visibility = View.VISIBLE

                yorumText.error = null

                Toast.makeText(this, "Paylaşılıyor, Lütfen Bekleyin...", Toast.LENGTH_LONG)
                    .show()

                gorselReference.putFile(secilenGorsel!!).addOnSuccessListener {

                    val yuklenenGorselReference =
                        FirebaseStorage.getInstance().reference.child("images")
                            .child(gorselIsim)

                    yuklenenGorselReference.downloadUrl.addOnSuccessListener { uri ->

                        val downloadUrl = uri.toString()


                        val guncelKullaniciEmail = auth.currentUser!!.email.toString()
                        val guncelKullaniciUID = auth.currentUser!!.uid
                        val tarih = Timestamp.now()
                        //veritabanı işlemleri
                        val postHashMap = hashMapOf<String, Any>()
                        postHashMap["postId"] = postId
                        postHashMap["gorselurl"] = downloadUrl
                        postHashMap["kullaniciemail"] = guncelKullaniciEmail
                        postHashMap["kullaniciyorum"] = kullaniciYorum
                        postHashMap["tarih"] = tarih
                        postHashMap["userID"] = guncelKullaniciUID

                        database.collection("Post").add(postHashMap)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(this, "Paylaşım Yapıldı", Toast.LENGTH_LONG)
                                        .show()
                                    spinner.visibility = View.INVISIBLE
                                    finish()
                                }
                            }.addOnFailureListener { exception ->
                                Toast.makeText(
                                    this,
                                    exception.localizedMessage,
                                    Toast.LENGTH_LONG
                                )
                                    .show()

                                paylasButton.isClickable = true

                                spinner.visibility = View.INVISIBLE

                            }


                    }.addOnFailureListener { exception ->
                        Toast.makeText(this, exception.localizedMessage, Toast.LENGTH_LONG)
                            .show()

                        paylasButton.isClickable = true

                        spinner.visibility = View.INVISIBLE


                    }
                }.addOnFailureListener { exception ->
                    Toast.makeText(this, exception.localizedMessage, Toast.LENGTH_LONG).show()

                    paylasButton.isClickable = true

                    spinner.visibility = View.INVISIBLE
                }
            } else if (secilenGorsel == null) {
                paylasButton.isClickable = true

                Toast.makeText(this, "Lütfen Bir Görsel Seçiniz", Toast.LENGTH_SHORT).show()
            } else if (kullaniciYorum.isEmpty()) {
                paylasButton.isClickable = true
                yorumText.error = "Bu Alanı Boş Bırakamazsınız"
            }


        } else {
            val spinner = progressCircular


            //UUID


            val uuid = UUID.randomUUID()
            val postId = "$uuid"

            val kullaniciYorum = yorumText.text.toString()
            if (kullaniciYorum.isNotEmpty()) {
                paylasButton.isClickable = false
                spinner.visibility = View.VISIBLE

                yorumText.error = null

                Toast.makeText(this, "Paylaşılıyor, Lütfen Bekleyin...", Toast.LENGTH_LONG)
                    .show()


                val downloadUrl = a


                val guncelKullaniciEmail = auth.currentUser!!.email.toString()

                val tarih = Timestamp.now()
                //veritabanı işlemleri
                val postHashMap = hashMapOf<String, Any>()
                postHashMap["postId"] = postId
                postHashMap["gorselurl"] = downloadUrl
                postHashMap["kullaniciemail"] = guncelKullaniciEmail
                postHashMap["kullaniciyorum"] = kullaniciYorum
                postHashMap["tarih"] = tarih

                database.collection("Post").add(postHashMap)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Paylaşım Yapıldı", Toast.LENGTH_LONG)
                                .show()
                            spinner.visibility = View.INVISIBLE
                            finish()
                        }
                    }.addOnFailureListener { exception ->
                        Toast.makeText(this, exception.localizedMessage, Toast.LENGTH_LONG)
                            .show()

                        paylasButton.isClickable = true

                        spinner.visibility = View.INVISIBLE

                    }


            } else if (kullaniciYorum.isEmpty()) {
                paylasButton.isClickable = true
                yorumText.error = "Bu Alanı Boş Bırakamazsınız"
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
                secilenPostImageView.visibility = View.VISIBLE
                secilenPostImageView.setImageURI(secilenGorsel)

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val e = result.error
                Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()

            }
        }


        super.onActivityResult(requestCode, resultCode, data)
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
        secilenPostImageView.visibility = View.VISIBLE
        secilenPostImageView.glide(a, placeHolderYap(applicationContext))
        gifOrImage = false
    }

}