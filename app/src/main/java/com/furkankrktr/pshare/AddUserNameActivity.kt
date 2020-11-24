package com.furkankrktr.pshare

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_add_user_name.*
import java.util.*

class AddUserNameActivity : AppCompatActivity() {


    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var ekleButton: Button
    private lateinit var userName: String
    private val uuid = UUID.randomUUID()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_user_name)

        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()

        ekleButton = findViewById(R.id.addUserNameBtn)
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
                userNameAddLayout.error = null
                userNameAddLayout.error = "Bu Alanı Boş Bırakamazsın"
                ekleButton.isClickable = true
            }
            userName.length < 6 -> {
                userNameAddLayout.error = null
                userNameAddLayout.error = "Kullanıcı Adı 6 Karaketerden Küçük Olamaz"
                ekleButton.isClickable = true
            }
            else -> {
                ekleButton.isClickable = false
                userNameAddLayout.error = null
                val userHashMap = hashMapOf<String, Any>()

                userHashMap["username"] = userName
                userHashMap["useremail"] = auth.currentUser!!.email.toString()
                userHashMap["userId"] = uuid.toString()
                userHashMap["theme"] = "dark"
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