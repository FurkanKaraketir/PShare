package com.furkankrktr.pshare

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.furkankrktr.pshare.databinding.ActivityKullaniciBinding

class KullaniciActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityKullaniciBinding.inflate(layoutInflater)

        auth = FirebaseAuth.getInstance()


        binding.mainGirisYap.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.mainKayitOl.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()

        }
        val guncelKullanici = auth.currentUser
        if (guncelKullanici != null) {
            val intent = Intent(this, HaberlerActivity::class.java)
            startActivity(intent)
            finish()
        }
        setContentView(binding.root)


    }


}