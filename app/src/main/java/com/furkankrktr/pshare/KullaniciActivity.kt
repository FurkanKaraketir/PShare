package com.furkankrktr.pshare

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth


class KullaniciActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = FirebaseAuth.getInstance()
        val girisYapMain = findViewById<Button>(R.id.mainGirisYap)
        val kayitOlMain = findViewById<Button>(R.id.mainKayitOl)

        girisYapMain.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        kayitOlMain.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)

        }
        val guncelKullanici = auth.currentUser
        if (guncelKullanici != null) {
            val intent = Intent(this, HaberlerActivity::class.java)
            startActivity(intent)
            finish()
        }


    }


}