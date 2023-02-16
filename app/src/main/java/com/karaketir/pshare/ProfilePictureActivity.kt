@file:Suppress("DEPRECATION")

package com.karaketir.pshare

import android.Manifest.permission.CAMERA
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.karaketir.pshare.databinding.ActivityProfilePictureBinding
import com.karaketir.pshare.services.glide
import com.karaketir.pshare.services.placeHolderYap

class ProfilePictureActivity : AppCompatActivity(), GiphyDialogFragment.GifSelectionListener {
    private lateinit var binding: ActivityProfilePictureBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var istenen: String = ""
    private var urlFinal = ""
    private lateinit var storage: FirebaseStorage
    private lateinit var secilenGorsel: ImageView
    private lateinit var spaceRef: StorageReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfilePictureBinding.inflate(layoutInflater)
        setContentView(binding.root)


        auth = Firebase.auth
        db = Firebase.firestore
        storage = Firebase.storage
        db.collection("Keys").document("GiphyKey").get().addOnSuccessListener {
            Giphy.configure(this, it.get("key").toString())
        }
        val settings = GPHSettings(GridType.waterfall, GPHTheme.Dark)
        settings.mediaTypeConfig = arrayOf(GPHContentType.gif)

        val saveButton = binding.saveProfilePhoto
        secilenGorsel = binding.addProfilePhoto

        val storageRef = storage.reference
        val imagesRef = storageRef.child("userProfilePhotos")

        val fileName = "${auth.uid.toString()}.jpg"
        spaceRef = imagesRef.child(fileName)

        saveButton.setOnClickListener {

            if (urlFinal != "") {

                db.collection("User").document(auth.uid.toString())
                    .update("profileImageURL", urlFinal).addOnSuccessListener {
                        Toast.makeText(this, "Başarılı", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        this.startActivity(intent)
                        finish()
                    }

            } else {
                Toast.makeText(this, "Profil Resmi Ekleyin", Toast.LENGTH_SHORT).show()
            }

        }

        secilenGorsel.setOnClickListener {


            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle("GIF ya da Resim")
            alertDialog.setMessage("Seçim Yapınız")
            alertDialog.setNegativeButton("GIF") { _, _ ->
                GiphyDialogFragment.newInstance(settings)
                    .show(supportFragmentManager, "giphy_dialog")
            }
            alertDialog.setPositiveButton("Resim") { _, _ ->
                if (ContextCompat.checkSelfPermission(
                        this, READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                        this, CAMERA
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    //İzin Verilmedi, iste
                    ActivityCompat.requestPermissions(
                        this, arrayOf(READ_EXTERNAL_STORAGE, CAMERA), 1
                    )


                } else {
                    ImagePicker.with(this@ProfilePictureActivity)
                        .crop(10f, 10f) //Crop square image, its same as crop(1f, 1f)
                        .start()
                }
            }
            alertDialog.show()


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
        secilenGorsel.glide(urlFinal, placeHolderYap(this))
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> {
                //Image Uri will not be null for RESULT_OK
                val uri: Uri = data?.data!!
                secilenGorsel.glide("", placeHolderYap(this))

                Toast.makeText(this, "Lütfen Bekleyiniz", Toast.LENGTH_SHORT).show()


                spaceRef.putFile(uri).addOnSuccessListener {

                    val yuklenenGorselReference =
                        FirebaseStorage.getInstance().reference.child("userProfilePhotos")
                            .child(auth.uid.toString() + ".jpg")

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
                ImagePicker.with(this@ProfilePictureActivity)
                    .crop(10f, 10f) //Crop square image, its same as crop(1f, 1f)
                    .start()
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}