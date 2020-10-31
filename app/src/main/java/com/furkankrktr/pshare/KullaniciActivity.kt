package com.furkankrktr.pshare

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*


class KullaniciActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = FirebaseAuth.getInstance()
        val girisYapMain = findViewById<Button>(R.id.mainGirisYap)
        val kayitOlMain = findViewById<Button>(R.id.mainKayitOl)

        girisYapMain.setOnClickListener {

            val email = emailText.text.toString()
            val sifre = passwordText.text.toString()

            when {
                email.isEmpty() -> {
                    emailLayout.error = "Bu Alanı Boş Bırakamazsın"
                }
                sifre.isEmpty() -> {
                    emailLayout.error = null
                    passwordLayout.error = "Bu Alanı Boş Bırakamazsın"
                }
                sifre.length < 6 -> {
                    emailLayout.error = null
                    passwordLayout.error = "Kullanıcı Şifresi En Az 6 Karakter İle Oluşturulmuştur"
                }
                else -> {
                    emailLayout.error = null
                    passwordLayout.error = null
                    Toast.makeText(this, "Giriş Yapılıyor...", Toast.LENGTH_SHORT).show()

                    auth.signInWithEmailAndPassword(email, sifre).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val intent = Intent(this, HaberlerActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }.addOnFailureListener { exception ->
                        Toast.makeText(this, exception.localizedMessage, Toast.LENGTH_LONG).show()
                    }


                }
            }

        }
        kayitOlMain.setOnClickListener {


            val email = emailText.text.toString()
            val sifre = passwordText.text.toString()

            when {
                email.isEmpty() -> {
                    emailLayout.error = "Bu Alanı Boş Bırakamazsın"
                }
                sifre.isEmpty() -> {
                    emailLayout.error = null
                    passwordLayout.error = "Bu Alanı Boş Bırakamazsın"
                }
                sifre.length < 6 -> {
                    emailLayout.error = null
                    passwordLayout.error = "Kullanıcı Şifresi En Az 6 Karakter İle Oluşturulmuştur"
                }
                else -> {
                    emailLayout.error = null
                    passwordLayout.error = null
                    Toast.makeText(this, "Kullanıcı Kaydediliyor...", Toast.LENGTH_SHORT).show()

                    auth.createUserWithEmailAndPassword(email, sifre)
                        .addOnCompleteListener { task ->


                            if (task.isSuccessful) {
                                //diğer aktivite
                                val intent = Intent(this, HaberlerActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        }.addOnFailureListener { exception ->
                            Toast.makeText(this, exception.localizedMessage, Toast.LENGTH_LONG)
                                .show()
                        }
                }


            }

        }
        val guncelKullanici = auth.currentUser
        if (guncelKullanici != null) {
            val intent = Intent(this, HaberlerActivity::class.java)
            startActivity(intent)
            finish()
        }


    }


}