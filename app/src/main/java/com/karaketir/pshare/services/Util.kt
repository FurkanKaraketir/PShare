package com.karaketir.pshare.services

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.format.DateUtils
import android.view.MenuItem
import android.widget.ImageView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.Timestamp
import com.karaketir.pshare.PhotoViewerActivity
import com.karaketir.pshare.R

fun ImageView.glide(url: String?, placeholder: CircularProgressDrawable) {
    val options = RequestOptions().placeholder(placeholder).error(R.drawable.blank)

    Glide.with(context.applicationContext).setDefaultRequestOptions(options).load(url).into(this)
}

fun ImageView.glideCircle(url: String?, placeholder: CircularProgressDrawable) {
    val options = RequestOptions().placeholder(placeholder).error(R.drawable.blank).circleCrop()

    Glide.with(context.applicationContext).setDefaultRequestOptions(options).load(url).into(this)
}


fun openLink(link: String, context: Context) {

    if (link.contains("giphy")) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        context.startActivity(intent)
    } else {
        val intent = Intent(context, PhotoViewerActivity::class.java)
        intent.putExtra("photoLink", link)
        context.startActivity(intent)
    }
}


fun placeHolderYap(context: Context): CircularProgressDrawable {
    return CircularProgressDrawable(context).apply {
        strokeWidth = 10f
        centerRadius = 40f
        start()
    }
}

fun getRelativeTime(timeStamp: Timestamp): CharSequence {
    val myLong = timeStamp.seconds * 1000 + timeStamp.nanoseconds / 1000000
    val now = System.currentTimeMillis()
    return DateUtils.getRelativeTimeSpanString(
        myLong, now, DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE
    )
}