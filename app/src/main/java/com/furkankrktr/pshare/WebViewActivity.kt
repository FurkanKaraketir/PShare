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

        val link = intent.getStringExtra("link")
        webView = binding.webView
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            webView.webViewClient = WebViewClient()
            webView.apply {
                if (link != null) {
                    loadUrl(link)
                }
                settings.javaScriptEnabled = true
                settings.safeBrowsingEnabled = true
            }
        } else {
            if (link != null) {
                webView.loadUrl(link)
            }
        }

    }
}

