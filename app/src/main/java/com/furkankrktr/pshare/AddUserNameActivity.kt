package com.furkankrktr.pshare

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.furkankrktr.pshare.databinding.ActivityAddUserNameBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class AddUserNameActivity : AppCompatActivity() {


    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var ekleButton: Button
    private lateinit var userName: String
    private lateinit var userNameAddText: EditText
    private lateinit var addUserNameTextView: TextView
    private val uuid = UUID.randomUUID()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAddUserNameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()

        ekleButton = binding.addUserNameBtn
        userNameAddText = binding.userNameAddText
        addUserNameTextView = binding.addUserNameTextView

        ekleButton.isClickable = true


        ekleButton.setOnClickListener {
            userName = userNameAddText.text.toString()

            ekle()
        }
        addUserNameTextView.setOnClickListener {
            userName = userNameAddText.text.toString()

            ekle()
        }
    }


    private fun ekle() {
        ekleButton.isClickable = false
        when {
            userName.isEmpty() -> {
                userNameAddText.error = null
                userNameAddText.error = "Bu Alanı Boş Bırakamazsın"
                ekleButton.isClickable = true
            }
            userName.length < 6 -> {
                userNameAddText.error = null
                userNameAddText.error = "Kullanıcı Adı 6 Karaketerden Küçük Olamaz"
                ekleButton.isClickable = true
            }
            else -> {
                ekleButton.isClickable = false
                userNameAddText.error = null
                val userHashMap = hashMapOf<String, Any>()

                userHashMap["username"] = userName
                userHashMap["useremail"] = auth.currentUser!!.email.toString()
                userHashMap["userId"] = uuid.toString()
                userHashMap["theme"] = "dark"
                userHashMap["takipEdilenEmailler"] =
                    arrayListOf(auth.currentUser!!.email.toString())
                userHashMap["profileImage"] = ""
                database.collection("Users").add(userHashMap)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            val intent = Intent(this, HaberlerActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }.addOnFailureListener { exception ->
                        Toast.makeText(
                            this,
                            exception.localizedMessage,
                            Toast.LENGTH_LONG
                        )
                            .show()

                        ekleButton.isClickable = true

                    }

            }
        }
    }
}