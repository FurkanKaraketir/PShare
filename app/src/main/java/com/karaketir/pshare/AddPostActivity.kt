@file:Suppress("DEPRECATION")

package com.karaketir.pshare

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewAnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.giphy.sdk.core.models.Media
import com.giphy.sdk.ui.GPHContentType
import com.giphy.sdk.ui.GPHSettings
import com.giphy.sdk.ui.Giphy
import com.giphy.sdk.ui.themes.GPHTheme
import com.giphy.sdk.ui.themes.GridType
import com.giphy.sdk.ui.views.GiphyDialogFragment
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.karaketir.pshare.databinding.ActivityAddPostBinding
import com.karaketir.pshare.services.glide
import com.karaketir.pshare.services.placeHolderYap
import java.util.*
import kotlin.math.hypot


class AddPostActivity : AppCompatActivity(), GiphyDialogFragment.GifSelectionListener {

    private var istenen: String = ""
    private lateinit var secilenGorsel: ImageView

    private lateinit var sendButton: FloatingActionButton
    private lateinit var imageSec: Button
    private lateinit var postPaylasTextView: TextView
    private lateinit var progressCircular: ProgressBar
    private lateinit var yorumText: EditText

    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var spaceRef: StorageReference
    private var urlFinal = ""
    private var fileName = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAddPostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        MobileAds.initialize(
            this
        ) { }

        val mAdView: AdView = binding.adView
        val adRequest: AdRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)


        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()

        sendButton = binding.paylasButton
        sendButton.visibility = View.INVISIBLE
        imageSec = binding.imageView
        postPaylasTextView = binding.postPaylasTextView
        progressCircular = binding.progressCircular
        yorumText = binding.yorumText

        database.collection("Keys").document("GiphyKey").get().addOnSuccessListener {
            Giphy.configure(this, it.get("key").toString())
        }
        val settings = GPHSettings(GridType.waterfall, GPHTheme.Dark)
        settings.mediaTypeConfig = arrayOf(GPHContentType.gif)

        secilenGorsel = binding.secilenPostResimView
        secilenGorsel.visibility = View.GONE

        val storageRef = storage.reference
        val imagesRef = storageRef.child("photos")
        val randomID = UUID.randomUUID().toString()

        fileName = "${randomID}.jpg"
        spaceRef = imagesRef.child(fileName)

        val alertDialog = AlertDialog.Builder(this@AddPostActivity)
        alertDialog.setTitle("GIF ya da Resim")
        alertDialog.setMessage("Seçim Yapınız")
        alertDialog.setNegativeButton("GIF") { _, _ ->
            GiphyDialogFragment.newInstance(settings).show(supportFragmentManager, "giphy_dialog")
        }
        alertDialog.setPositiveButton("Resim") { _, _ ->
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    this, Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                //İzin Verilmedi, iste
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA
                    ), 1
                )


            } else {
                ImagePicker.with(this@AddPostActivity)
                    .crop(10f, 10f) //Crop square image, its same as crop(1f, 1f)
                    .start()
            }
        }
        imageSec.setOnClickListener {
            alertDialog.show()
        }
        secilenGorsel.setOnClickListener {
            alertDialog.show()
        }
        sendButton.setOnClickListener {
            paylas()
        }
        postPaylasTextView.setOnClickListener {
            paylas()
        }

        yorumText.addTextChangedListener(object : TextWatcher {
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
                    sendButton.visibility = View.VISIBLE
                    anim.start()


                    // set the view to invisible without a circular reveal animation below Lollipop

                } else if (p0.toString() == "" && sendButton.visibility == View.VISIBLE) {
                    val cx = sendButton.width / 2
                    val cy = sendButton.height / 2

                    // get the initial radius for the clipping circle
                    val initialRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()

                    // create the animation (the final radius is zero)
                    val anim = ViewAnimationUtils.createCircularReveal(
                        sendButton, cx, cy, initialRadius, 0f
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

    }

    private fun paylas() {
        val spinner = progressCircular

        if (yorumText.text.isNotEmpty()) {
            yorumText.error = null
            sendButton.isClickable = false
            spinner.visibility = View.VISIBLE

            val cal = Calendar.getInstance()
            val documentID = UUID.randomUUID().toString()
            val data = hashMapOf(
                "postID" to documentID,
                "postOwnerID" to auth.uid.toString(),
                "postDescription" to yorumText.text.toString(),
                "postImageURL" to urlFinal,
                "timestamp" to cal.time
            )

            database.collection("Post").document(documentID).set(data).addOnSuccessListener {
                Toast.makeText(this, "İşlem Başarılı", Toast.LENGTH_SHORT).show()
                finish()
            }

        } else {
            yorumText.error = "Bu Alan Boş Bırakılamaz"
        }
    }

    override fun didSearchTerm(term: String) {

    }

    override fun onDismissed(selectedContentType: GPHContentType) {

    }

    override fun onGifSelected(
        media: Media, searchTerm: String?, selectedContentType: GPHContentType
    ) {
        val url = media.embedUrl!!
        val hepsi: List<String> = url.split('/')

        istenen = hepsi[hepsi.size - 1]
        urlFinal = "https://media.giphy.com/media/$istenen/giphy.gif"
        secilenGorsel.visibility = View.VISIBLE
        secilenGorsel.glide(urlFinal, placeHolderYap(this))
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> {
                //Image Uri will not be null for RESULT_OK
                val uri: Uri = data?.data!!
                secilenGorsel.visibility = View.VISIBLE
                secilenGorsel.glide("", placeHolderYap(this))

                Toast.makeText(this, "Lütfen Bekleyiniz", Toast.LENGTH_SHORT).show()


                spaceRef.putFile(uri).addOnSuccessListener {

                    val yuklenenGorselReference =
                        FirebaseStorage.getInstance().reference.child("photos").child(fileName)

                    yuklenenGorselReference.downloadUrl.addOnSuccessListener { downloadURL ->

                        urlFinal = downloadURL.toString()
                        secilenGorsel.setImageURI(uri)

                    }


                }

            }
            ImagePicker.RESULT_ERROR -> {
                Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        //İzin Yeni Verildi
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ImagePicker.with(this@AddPostActivity)
                    .crop(10f, 10f) //Crop square image, its same as crop(1f, 1f)
                    .start()
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}