package com.furkankrktr.pshare

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_fotograf_paylasma.*
import java.util.*

open class FotografPaylasmaActivity : AppCompatActivity() {

    var secilenGorsel: Uri? = null
    var secilenBitmap: Bitmap? = null
    var player: MediaPlayer? = null

    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fotograf_paylasma)

        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()
        paylasButton.isClickable = true


    }


    fun paylas(view: View) {

        //depo işlemleri
        val spinner = progress_circular


        //UUID


        val uuid = UUID.randomUUID()
        val gorselIsim = "${uuid}.jpg"
        val postId = "${uuid}"
        val reference = storage.reference
        val gorselReference = reference.child("images").child(gorselIsim)

        val kullaniciYorum = yorumText.text.toString()
        if (secilenGorsel != null && kullaniciYorum.isNotEmpty()) {
            paylasButton.isClickable = false
            spinner.visibility = View.VISIBLE;
            if (player == null) {
                player = MediaPlayer.create(this, R.raw.sound)
            }
            player?.setOnCompletionListener {
                if (player != null) {
                    player!!.release()
                    player = null
                }
            }
            player?.start()

            yorumLayout.error = null

            Toast.makeText(this, "Paylaşılıyor, Lütfen Bekleyin...", Toast.LENGTH_LONG).show()

            gorselReference.putFile(secilenGorsel!!).addOnSuccessListener { taskSnapshot ->

                val yuklenenGorselReference =
                    FirebaseStorage.getInstance().reference.child("images").child(gorselIsim)

                yuklenenGorselReference.downloadUrl.addOnSuccessListener { uri ->

                    val downloadUrl = uri.toString()


                    val guncelKullaniciEmail = auth.currentUser!!.email.toString()

                    val tarih = Timestamp.now()
                    //veritabanı işlemleri
                    val postHashMap = hashMapOf<String, Any>()
                    postHashMap.put("postId", postId)
                    postHashMap.put("gorselurl", downloadUrl)
                    postHashMap.put("kullaniciemail", guncelKullaniciEmail)
                    postHashMap.put("kullaniciyorum", kullaniciYorum)
                    postHashMap.put("tarih", tarih)

                    database.collection("Post").add(postHashMap).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Paylaşım Yapıldı", Toast.LENGTH_LONG).show()
                            if (player != null) {
                                player!!.release()
                                player = null
                            }
                            spinner.visibility = View.INVISIBLE;
                            finish()
                        }
                    }.addOnFailureListener { exception ->
                        Toast.makeText(this, exception.localizedMessage, Toast.LENGTH_LONG).show()
                        if (player != null) {
                            player!!.release()
                            player = null
                        }
                        paylasButton.isClickable = true

                        spinner.visibility = View.INVISIBLE;

                    }


                }.addOnFailureListener { exception ->
                    Toast.makeText(this, exception.localizedMessage, Toast.LENGTH_LONG).show()
                    if (player != null) {
                        player!!.release()
                        player = null
                    }
                    paylasButton.isClickable = true

                    spinner.visibility = View.INVISIBLE;


                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this, exception.localizedMessage, Toast.LENGTH_LONG).show()
                if (player != null) {
                    player!!.release()
                    player = null
                }
                paylasButton.isClickable = true

                spinner.visibility = View.INVISIBLE;
            }
        } else if (secilenGorsel == null) {
            paylasButton.isClickable = true

            Toast.makeText(this, "Lütfen Bir Görsel Seçiniz", Toast.LENGTH_SHORT).show()
        } else if (kullaniciYorum.isEmpty()) {
            paylasButton.isClickable = true
            yorumLayout.error = "Bu Alanı Boş Bırakamazsınız"
        }

    }


    fun gorselSec(view: View) {
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


                if (Build.VERSION.SDK_INT >= 28) {
                    val sources = ImageDecoder.createSource(this.contentResolver, secilenGorsel!!)
                    secilenBitmap = ImageDecoder.decodeBitmap(sources)
                    imageView.setImageBitmap(secilenBitmap)


                } else {
                    secilenBitmap =
                        MediaStore.Images.Media.getBitmap(this.contentResolver, secilenGorsel)
                    imageView.setImageBitmap(secilenBitmap)
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val e = result.error
                Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()

            }
        }


        super.onActivityResult(requestCode, resultCode, data)
    }


    override fun onStop() {
        super.onStop()
        if (player != null) {
            player!!.release()
            player = null
        }
    }

}