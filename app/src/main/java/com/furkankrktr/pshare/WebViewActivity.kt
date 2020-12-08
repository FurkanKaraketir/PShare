package com.furkankrktr.pshare

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.furkankrktr.pshare.databinding.ActivityWebViewBinding

class WebViewActivity : AppCompatActivity() {
    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)


        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        webView = binding.webView
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            webView.webViewClient = WebViewClient()
            webView.apply {
                loadUrl("https://furkankrktr.wordpress.com/")
                settings.javaScriptEnabled = true
                settings.safeBrowsingEnabled = true
            }
        } else {
            webView.loadUrl("https://furkankrktr.wordpress.com/")
        }

    }
}

