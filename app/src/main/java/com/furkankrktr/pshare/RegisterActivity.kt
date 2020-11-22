package com.furkankrktr.pshare

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_add_user_name.*
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()

        val kayitOlButton = findViewById<Button>(R.id.registerKayitOl)
        kayitOlButton.isClickable = true

        kayitOlButton.setOnClickListener {
            val userName = userNameRegisterText.text.toString()
            val email = emailRegisterText.text.toString()
            val sifre = passwordRegisterText.text.toString()
            val uuid = UUID.randomUUID()

            when {
                userName.isEmpty() -> {
                    userNameRegisterLayout.error = "Bu Alanı Boş Bırakamazsın"
                }
                email.isEmpty() -> {
                    emailRegisterLayout.error = "Bu Alanı Boş Bırakamazsın"
                    userNameRegisterLayout.error = null
                }
                sifre.isEmpty() -> {
                    emailRegisterLayout.error = null
                    userNameRegisterLayout.error = null
                    passwordRegisterLayout.error = "Bu Alanı Boş Bırakamazsın"
                }
                sifre.length < 6 -> {
                    userNameRegisterLayout.error = null
                    emailRegisterLayout.error = null
                    passwordRegisterLayout.error =
                        "Kullanıcı Şifresi En Az 6 Karakter İle Oluşturulmuştur"
                }
                userName.length < 6 -> {
                    userNameAddLayout.error = "Kullanıcı Adı 6 Karaketerden Küçük Olamaz"
                    emailRegisterLayout.error = null
                    passwordRegisterLayout.error = null
                }
                else -> {
                    emailRegisterLayout.error = null
                    passwordRegisterLayout.error = null
                    Toast.makeText(this, "Kullanıcı Kaydediliyor...", Toast.LENGTH_SHORT).show()
                    kayitOlButton.isClickable = false
                    auth.createUserWithEmailAndPassword(email, sifre)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                //diğer aktivite

                                val userHashMap = hashMapOf<String, Any>()

                                userHashMap["username"] = userName
                                userHashMap["useremail"] = email
                                userHashMap["userId"] = uuid.toString()
                                database.collection("Users").add(userHashMap)
                                    .addOnCompleteListener {
                                        if (task.isSuccessful) {
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

                                        kayitOlButton.isClickable = true

                                    }

                            }
                        }.addOnFailureListener { exception ->
                            Toast.makeText(this, exception.localizedMessage, Toast.LENGTH_LONG)
                                .show()
                            kayitOlButton.isClickable = true
                        }
                }


            }
        }

    }
}