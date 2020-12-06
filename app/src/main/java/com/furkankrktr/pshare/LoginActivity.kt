package com.furkankrktr.pshare

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()

        val girisYapButton = findViewById<Button>(R.id.loginGirisYap)

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        girisYapButton.setOnClickListener {

            val email = emailLoginText.text.toString()
            val sifre = passwordLoginText.text.toString()

            when {
                email.isEmpty() -> {
                    emailLoginLayout.error = "Bu Alanı Boş Bırakamazsın"
                }
                sifre.isEmpty() -> {
                    emailLoginLayout.error = null
                    passwordLoginLayout.error = "Bu Alanı Boş Bırakamazsın"
                }
                sifre.length < 6 -> {
                    emailLoginLayout.error = null
                    passwordLoginLayout.error =
                        "Kullanıcı Şifresi En Az 6 Karakter İle Oluşturulmuştur"
                }
                else -> {
                    emailLoginLayout.error = null
                    passwordLoginLayout.error = null
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

    }
}