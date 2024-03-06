package com.karaketir.pshare

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.karaketir.pshare.databinding.ActivityPhotoViewerBinding


class PhotoViewerActivity : AppCompatActivity() {
    private lateinit var photoLink: String
    private lateinit var binding: ActivityPhotoViewerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotoViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        photoLink = intent.getStringExtra("photoLink").toString()

        supportActionBar?.hide()

        Glide.with(this).load(photoLink).into(object : CustomTarget<Drawable?>() {
            @SuppressLint("SetTextI18n")
            override fun onResourceReady(
                resource: Drawable,
                transition: com.bumptech.glide.request.transition.Transition<in Drawable?>?
            ) {
                binding.imageGlide.setImageDrawable(resource)
            }

            override fun onLoadCleared(placeholder: Drawable?) = Unit

        })

    }
}