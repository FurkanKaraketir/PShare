package com.furkankrktr.pshare

import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.furkankrktr.pshare.databinding.ActivityGorselBinding
import com.furkankrktr.pshare.model.ZoomClass


@Suppress("DEPRECATION")
class GorselActivity : AppCompatActivity() {

    private lateinit var resimTamEkran: ZoomClass
    private var open: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val binding = ActivityGorselBinding.inflate(layoutInflater)
        setContentView(binding.root)
        resimTamEkran = binding.resimTamEkran
        hideSystemUI()
        val resimUri = intent.getStringExtra("resim")
        Glide.with(this).load(resimUri).into(resimTamEkran)

        resimTamEkran.setOnClickListener {
            if (open) {
                hideSystemUI()
            } else {
                showSystemUI()
            }
        }

    }


    private fun hideSystemUI() {
        open = false
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE

                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }


    private fun showSystemUI() {
        open = true
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }


}