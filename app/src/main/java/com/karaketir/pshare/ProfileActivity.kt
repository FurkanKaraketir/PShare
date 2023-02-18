@file:Suppress("DEPRECATION")

package com.karaketir.pshare

import android.Manifest
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.karaketir.pshare.databinding.ActivityProfileBinding
import com.karaketir.pshare.services.glide
import com.karaketir.pshare.services.placeHolderYap

class ProfileActivity : AppCompatActivity(), GiphyDialogFragment.GifSelectionListener {

    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var profileImageURL: String
    private lateinit var secilenGorsel: ImageView
    private lateinit var documentName: String
    private var istenen: String = ""

    private lateinit var userNameChangeEditText: EditText
    private lateinit var userNameEditButton: ImageView
    private lateinit var progressCircularProfile: ProgressBar
    private lateinit var userNameView: TextView
    private lateinit var versionTextView: TextView
    private lateinit var kaydetBtn: Button
    private lateinit var takipEdilenTextView: TextView
    private lateinit var takipciTextView: TextView
    private lateinit var engellenenTextView: TextView
    private lateinit var spaceRef: StorageReference

    private var urlFinal: String = ""
    private lateinit var userName: String

    private var i: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        kaydetBtn = binding.kaydetBtn
        userNameEditButton = binding.userNameEditButton
        secilenGorsel = binding.profileImageAdd
        userNameChangeEditText = binding.userNameChangeEditText
        progressCircularProfile = binding.progressCircularProfile
        takipEdilenTextView = binding.takipEdilenText
        takipciTextView = binding.takipciText
        engellenenTextView = binding.blockedText

        userNameView = binding.userNameView
        versionTextView = binding.versionTextView
        val deleteAccountButton = binding.deleteAccountButton


        auth = Firebase.auth
        db = Firebase.firestore
        storage = Firebase.storage

        val settings = GPHSettings(GridType.waterfall, GPHTheme.Dark)
        settings.mediaTypeConfig = arrayOf(GPHContentType.gif)

        val storageRef = storage.reference
        val imagesRef = storageRef.child("userProfilePhotos")

        val fileName = "${auth.uid.toString()}.jpg"
        spaceRef = imagesRef.child(fileName)

        db.collection("Keys").document("GiphyKey").get().addOnSuccessListener {
            Giphy.configure(this, it.get("key").toString())
        }

        takipEdilenTextView.setOnClickListener {
            val intent = Intent(this, FollowingsActivity::class.java)
            intent.putExtra("option", "Takip Edilenler")
            startActivity(intent)
        }
        takipciTextView.setOnClickListener {
            val intent = Intent(this, FollowingsActivity::class.java)
            intent.putExtra("option", "Takipçiler")
            startActivity(intent)
        }
        engellenenTextView.setOnClickListener {
            val intent = Intent(this, BlocksActivity::class.java)
            startActivity(intent)
        }


        userNameEditButton.setOnClickListener {
            userNameChangeEditText.visibility = View.VISIBLE
            i = 1
        }
        userNameChangeEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.toString() != "") {
                    kaydetBtn.visibility = View.VISIBLE
                } else {
                    kaydetBtn.visibility = View.GONE
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })

        kaydetBtn.setOnClickListener {

            progressCircularProfile.visibility = View.VISIBLE
            save()


        }

        verileriAl()

        deleteAccountButton.setOnClickListener {

            deleteAccount()

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
                    ImagePicker.with(this@ProfileActivity)
                        .crop(10f, 10f) //Crop square image, its same as crop(1f, 1f)
                        .start()
                }
            }
            alertDialog.show()


        }

    }

    private fun deleteAccount() {
        val deleteAlertDialog = AlertDialog.Builder(this)
        deleteAlertDialog.setTitle("Hesabı Sil")
        deleteAlertDialog.setMessage("Hesabı Silmek İstediğinize Emin misiniz?")
        deleteAlertDialog.setPositiveButton("Hesabı Sil") { _, _ ->

            db.collection("Likes").whereEqualTo("userID", auth.uid.toString())
                .addSnapshotListener { likes, _ ->
                    if (likes != null) {
                        for (like in likes) {
                            db.collection("Likes").document(like.id).delete()
                        }
                    }

                    db.collection("Followings").whereEqualTo("main", auth.uid.toString())
                        .addSnapshotListener { followings, _ ->
                            if (followings != null) {
                                for (following in followings) {
                                    db.collection("Followings").document(following.id).delete()
                                }
                            }

                            db.collection("Followings")
                                .whereEqualTo("followsWho", auth.uid.toString())
                                .addSnapshotListener { followings2, _ ->
                                    if (followings2 != null) {
                                        for (following1 in followings2) {
                                            db.collection("Followings").document(following1.id)
                                                .delete()
                                        }
                                    }

                                    db.collection("Post")
                                        .whereEqualTo("postOwnerID", auth.uid.toString())
                                        .addSnapshotListener { posts, _ ->
                                            if (posts != null) {
                                                for (post in posts) {
                                                    db.collection("Post").document(post.id).delete()
                                                }
                                            }

                                            db.collection("Comments").whereEqualTo(
                                                "commentToWho", auth.uid.toString()
                                            ).addSnapshotListener { comments, _ ->
                                                if (comments != null) {
                                                    for (comment in comments) {
                                                        db.collection("Comments")
                                                            .document(comment.id).delete()
                                                    }
                                                }


                                                db.collection("Comments").whereEqualTo(
                                                    "commentOwnerID", auth.uid.toString()
                                                ).addSnapshotListener { comments2, _ ->
                                                    if (comments2 != null) {
                                                        for (comment2 in comments2) {
                                                            db.collection("Comments")
                                                                .document(comment2.id).delete()
                                                        }
                                                    }

                                                    db.collection("Replies").whereEqualTo(
                                                        "replyOwnerID", auth.uid.toString()
                                                    ).addSnapshotListener { replies, _ ->
                                                        if (replies != null) {
                                                            for (reply in replies) {
                                                                db.collection("Replies")
                                                                    .document(reply.id).delete()
                                                            }
                                                        }

                                                        db.collection("Replies").whereEqualTo(
                                                            "replyToWho", auth.uid.toString()
                                                        ).addSnapshotListener { replies2, _ ->
                                                            if (replies2 != null) {
                                                                for (reply2 in replies2) {
                                                                    db.collection("Replies")
                                                                        .document(
                                                                            reply2.id
                                                                        ).delete()
                                                                }
                                                            }

                                                            db.collection("User")
                                                                .document(auth.uid.toString())
                                                                .delete().addOnSuccessListener {
                                                                    auth.currentUser!!.delete()
                                                                        .addOnCompleteListener { task ->
                                                                            if (task.isSuccessful) {
                                                                                val intent = Intent(
                                                                                    this,
                                                                                    LoginActivity::class.java
                                                                                )
                                                                                startActivity(
                                                                                    intent
                                                                                )
                                                                                finish()
                                                                            }
                                                                        }
                                                                }

                                                        }


                                                    }


                                                }


                                            }


                                        }

                                }

                        }


                }
        }

        deleteAlertDialog.setNegativeButton("İptal") { _, _ ->

        }
        deleteAlertDialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun verileriAl() {
        db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener {

            documentName = it.id
            profileImageURL = it.get("profileImageURL") as String
            val serverUserName = it.get("username") as String
            if (profileImageURL == "") {
                profileImageURL = "https://media.giphy.com/media/VBfFv9oOZAvvi/giphy.gif"
            }
            urlFinal = profileImageURL

            userNameView.text = serverUserName
            userName = serverUserName
            secilenGorsel.glide(urlFinal, placeHolderYap(this))

            db.collection("Followings").whereEqualTo("main", auth.uid.toString())
                .addSnapshotListener { value, _ ->
                    if (value != null && !value.isEmpty) {
                        val realTakip = value.size() - 1
                        takipEdilenTextView.text = "Takip Edilen: $realTakip"


                    } else {
                        takipEdilenTextView.text = "Takip Edilen: 0"
                    }
                }

            db.collection("Followings").whereEqualTo("followsWho", auth.uid.toString())
                .addSnapshotListener { value, _ ->
                    if (value != null && !value.isEmpty) {
                        val realTakip = value.size() - 1
                        takipciTextView.text = "Takipçiler: $realTakip"

                    } else {
                        takipciTextView.text = "Takipçiler: 0"
                    }
                }

            db.collection("Blocks").whereEqualTo("main", auth.uid.toString())
                .addSnapshotListener { value, _ ->
                    if (value != null && !value.isEmpty) {
                        val realTakip = value.size()
                        engellenenTextView.text = "Engellenenler: $realTakip"

                    } else {
                        engellenenTextView.text = "Engellenenler: 0"
                    }
                }


        }
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

        kaydetBtn.isClickable = false

        db.collection("User").document(auth.uid.toString()).update("username", userName)
            .addOnSuccessListener {
                progressCircularProfile.visibility = View.GONE
                Toast.makeText(this, "İşlem Başarılı", Toast.LENGTH_SHORT).show()

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
        db.collection("User").document(auth.uid.toString()).update("profileImageURL", urlFinal)
            .addOnSuccessListener {
                Toast.makeText(this, "Başarılı", Toast.LENGTH_SHORT).show()
            }
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
                        db.collection("User").document(auth.uid.toString())
                            .update("profileImageURL", urlFinal).addOnSuccessListener {
                                Toast.makeText(this, "Başarılı", Toast.LENGTH_SHORT).show()
                            }
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
                ImagePicker.with(this@ProfileActivity)
                    .crop(10f, 10f) //Crop square image, its same as crop(1f, 1f)
                    .start()
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}