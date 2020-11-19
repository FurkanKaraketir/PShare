package com.furkankrktr.pshare

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.furkankrktr.pshare.service.glide
import com.furkankrktr.pshare.service.placeHolderYap
import com.giphy.sdk.core.models.Media
import com.giphy.sdk.ui.GPHContentType
import com.giphy.sdk.ui.Giphy
import com.giphy.sdk.ui.views.GiphyDialogFragment
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_gif_share.*
import java.util.*

class GifShareActivity : AppCompatActivity(), GiphyDialogFragment.GifSelectionListener {
    private lateinit var imageGif: ImageView
    private var istenen: String = ""
    private var a: String = ""
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gif_share)

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        imageGif = findViewById<ImageView>(R.id.imageGifID)

        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()

        val paylasmaButton = findViewById<Button>(R.id.paylasGIFButton)
        paylasmaButton.isClickable = true
        paylasmaButton.setOnClickListener {


            //depo işlemleri
            val spinner = progress_circularGIF


            //UUID


            val uuid = UUID.randomUUID()
            val postId = "${uuid}"
            val kullaniciYorum = yorumGIFText.text.toString()
            if (istenen != "" && kullaniciYorum.isNotEmpty()) {
                paylasmaButton.isClickable = false
                spinner.visibility = View.VISIBLE

                yorumGIFLayout.error = null

                Toast.makeText(this, "Paylaşılıyor, Lütfen Bekleyin...", Toast.LENGTH_LONG).show()


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

                database.collection("Post").add(postHashMap).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Paylaşım Yapıldı", Toast.LENGTH_LONG).show()
                        spinner.visibility = View.INVISIBLE
                        finish()
                    }
                }.addOnFailureListener { exception ->
                    Toast.makeText(this, exception.localizedMessage, Toast.LENGTH_LONG)
                        .show()

                    paylasmaButton.isClickable = true

                    spinner.visibility = View.INVISIBLE

                }


            } else if (istenen == "") {
                paylasmaButton.isClickable = true

                Toast.makeText(this, "Lütfen Bir GIF Seçiniz", Toast.LENGTH_SHORT).show()
            } else if (kullaniciYorum.isEmpty()) {
                paylasmaButton.isClickable = true
                yorumGIFLayout.error = "Bu Alanı Boş Bırakamazsınız"
            }


        }


        imageGif.setOnClickListener {
            Giphy.configure(this, "Qyq8K6rBLuR2bYRetJteXkb6k7ngKUG8")

            GiphyDialogFragment.newInstance().show(supportFragmentManager, "giphy_dialog")
        }


        Giphy.configure(this, "Qyq8K6rBLuR2bYRetJteXkb6k7ngKUG8")

        GiphyDialogFragment.newInstance().show(supportFragmentManager, "giphy_dialog")


    }

    override fun didSearchTerm(term: String) {
        println(term)
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

        imageGif.glide(a, placeHolderYap(applicationContext))


    }
}