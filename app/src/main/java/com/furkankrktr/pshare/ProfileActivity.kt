@file:Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "DEPRECATION")

package com.furkankrktr.pshare

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageInfo
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
import com.furkankrktr.pshare.databinding.ActivityProfileBinding
import com.furkankrktr.pshare.service.glider
import com.furkankrktr.pshare.service.placeHolderYap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.theartofdev.edmodo.cropper.CropImage
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*


class ProfileActivity : AppCompatActivity() {

    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var guncelKullanici: String
    private lateinit var profileImageURL: String
    private lateinit var documentName: String

    private lateinit var userNameChangeEditText: EditText
    private lateinit var userNameEditButton: ImageView
    private lateinit var profileImageAdd: CircleImageView
    private lateinit var progressCircularProfile: ProgressBar
    private lateinit var userNameView: TextView
    private lateinit var versionTextView: TextView
    private lateinit var kaydetBtn: Button
    private lateinit var takipEdilenTextView: TextView

    private var a: String = ""
    private var secilenGorsel: Uri? = null
    private var gifOrImage: Boolean? = null
    private lateinit var userName: String

    private var i: Int = 0

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        kaydetBtn = binding.kaydetBtn
        userNameEditButton = binding.userNameEditButton
        profileImageAdd = binding.profileImageAdd
        userNameChangeEditText = binding.userNameChangeEditText
        progressCircularProfile = binding.progressCircularProfile
        takipEdilenTextView = binding.takipEdilenText
        userNameView = binding.userNameView
        versionTextView = binding.versionTextView
        try {
            val pInfo: PackageInfo =
                this.packageManager.getPackageInfo(this.packageName, 0)
            val version = pInfo.versionName
            versionTextView.text = "v$version"
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
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

        takipEdilenTextView.setOnClickListener {
            val intent = Intent(this, TakipEdilenlerActivity::class.java)
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
        profileImageAdd.setOnClickListener {
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

    @SuppressLint("SetTextI18n")
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
                                val takipEdilen =
                                    document.get("takipEdilenEmailler") as ArrayList<*>
                                val realTakip = takipEdilen.size - 1
                                takipEdilenTextView.text = "Takip Edilen: $realTakip"

                                userNameView.text = serverUserName
                                userName = serverUserName
                                profileImageAdd.glider(profileImageURL, placeHolderYap(this))
                            }
                        }
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


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)

            if (resultCode == RESULT_OK) {
                secilenGorsel = result.uri
                gifOrImage = true
                profileImageAdd.setImageURI(secilenGorsel)
                kaydetBtn.visibility = View.VISIBLE

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val e = result.error
                Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()

            }
        }


        super.onActivityResult(requestCode, resultCode, data)
    }
}