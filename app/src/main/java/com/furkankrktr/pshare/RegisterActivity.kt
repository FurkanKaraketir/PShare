package com.furkankrktr.pshare

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.furkankrktr.pshare.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore

    private lateinit var userNameRegisterText: EditText
    private lateinit var emailRegisterText: EditText
    private lateinit var passwordRegisterText: EditText
    private lateinit var kayitOlButton: Button
    private lateinit var registerToLoginButton: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userNameRegisterText = binding.userNameRegisterText
        emailRegisterText = binding.emailRegisterText
        passwordRegisterText = binding.passwordRegisterText
        kayitOlButton = binding.registerKayitOl
        registerToLoginButton = binding.registerToLoginButton

        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()

        kayitOlButton.isClickable = true

        kayitOlButton.setOnClickListener {
            val userName = userNameRegisterText.text.toString()
            val email = emailRegisterText.text.toString()
            val sifre = passwordRegisterText.text.toString()
            val uuid = UUID.randomUUID()

            when {
                userName.isEmpty() -> {
                    userNameRegisterText.error = "Bu Alanı Boş Bırakamazsın"
                }
                email.isEmpty() -> {
                    emailRegisterText.error = "Bu Alanı Boş Bırakamazsın"
                    userNameRegisterText.error = null
                }
                sifre.isEmpty() -> {
                    emailRegisterText.error = null
                    userNameRegisterText.error = null
                    passwordRegisterText.error = "Bu Alanı Boş Bırakamazsın"
                }
                sifre.length < 6 -> {
                    userNameRegisterText.error = null
                    emailRegisterText.error = null
                    passwordRegisterText.error =
                        "Kullanıcı Şifresi En Az 6 Karakter İle Oluşturulmuştur"
                }
                userName.length < 6 -> {
                    userNameRegisterText.error = "Kullanıcı Adı 6 Karaketerden Küçük Olamaz"
                    emailRegisterText.error = null
                    passwordRegisterText.error = null
                }
                else -> {
                    emailRegisterText.error = null
                    passwordRegisterText.error = null
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
                                userHashMap["takipEdilenEmailler"] = arrayListOf(email)
                                userHashMap["profileImage"] =
                                    "https://media.giphy.com/media/VBfFv9oOZAvvi/giphy.gif"
                                userHashMap["theme"] = "dark"
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
        registerToLoginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}