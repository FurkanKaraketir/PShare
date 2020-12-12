package com.furkankrktr.pshare

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.furkankrktr.pshare.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore

    private lateinit var emailLoginText: EditText
    private lateinit var passwordLoginText: EditText
    private lateinit var girisYapButton: Button
    private lateinit var loginToRegisterButton: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        girisYapButton = binding.loginGirisYap
        emailLoginText = binding.emailLoginText
        passwordLoginText = binding.passwordLoginText
        loginToRegisterButton = binding.loginToRegisterButton

        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()


        girisYapButton.setOnClickListener {

            val email = emailLoginText.text.toString()
            val sifre = passwordLoginText.text.toString()

            when {
                email.isEmpty() -> {
                    emailLoginText.error = "Bu Alanı Boş Bırakamazsın"
                }
                sifre.isEmpty() -> {
                    emailLoginText.error = null
                    passwordLoginText.error = "Bu Alanı Boş Bırakamazsın"
                }
                sifre.length < 6 -> {
                    emailLoginText.error = null
                    passwordLoginText.error =
                        "Kullanıcı Şifresi En Az 6 Karakter İle Oluşturulmuştur"
                }
                else -> {
                    emailLoginText.error = null
                    passwordLoginText.error = null
                    Toast.makeText(this, "Giriş Yapılıyor...", Toast.LENGTH_SHORT).show()
                    database.collection("Users").whereEqualTo("username", email)
                        .addSnapshotListener { value, error ->
                            if (error == null) {
                                if (value != null) {
                                    if (!value.isEmpty) {
                                        val documents = value.documents
                                        for (document in documents) {
                                            val userName = document.get("useremail") as String
                                            auth.signInWithEmailAndPassword(userName, sifre)
                                                .addOnCompleteListener { task ->
                                                    if (task.isSuccessful) {
                                                        val intent = Intent(
                                                            this,
                                                            HaberlerActivity::class.java
                                                        )
                                                        startActivity(intent)
                                                        finish()
                                                    }
                                                }.addOnFailureListener { exception ->
                                                    Toast.makeText(
                                                        this,
                                                        exception.localizedMessage,
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                        }
                                    }
                                }
                            }
                        }


                }
            }

        }

        loginToRegisterButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}