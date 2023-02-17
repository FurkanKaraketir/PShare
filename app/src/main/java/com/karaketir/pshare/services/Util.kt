package com.karaketir.pshare.services

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.ImageView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.karaketir.pshare.R

fun ImageView.glide(url: String?, placeholder: CircularProgressDrawable) {
    val options = RequestOptions().placeholder(placeholder).error(R.drawable.blank)

    Glide.with(context.applicationContext).setDefaultRequestOptions(options).load(url).into(this)
}

fun openLink(link: String, context: Context) {
    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
    context.startActivity(browserIntent)
}


fun placeHolderYap(context: Context): CircularProgressDrawable {
    return CircularProgressDrawable(context).apply {
        strokeWidth = 10f
        centerRadius = 40f
        start()
    }
}