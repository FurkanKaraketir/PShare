package com.furkankrktr.pshare.service

import android.content.Context
import android.widget.ImageView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.furkankrktr.pshare.R

fun ImageView.glide(url: String?, placeholder: CircularProgressDrawable){
    val options = RequestOptions().placeholder(placeholder).error(R.drawable.ic_baseline_block_24)

    Glide.with(context).setDefaultRequestOptions(options).load(url).into(this)
}

fun placeHolderYap(context: Context) : CircularProgressDrawable{
    return CircularProgressDrawable(context).apply {
        strokeWidth = 10f
        centerRadius = 40f
        start()
    }
}