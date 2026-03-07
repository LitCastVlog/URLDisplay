package com.litcast.URLDisplay

import android.app.Presentation
import android.content.Context
import android.os.Bundle
import android.view.Display
import android.widget.FrameLayout
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

class UrlPresentation(context: Context, display: Display) :
    Presentation(context, display) {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Container layout to control sizing
        val container = FrameLayout(context)
        container.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        webView = WebView(context)
        webView.setLayerType(WebView.LAYER_TYPE_HARDWARE, null)

        // Stretch WebView to fill the entire display
        val params = FrameLayout.LayoutParams(
            display.width,
            display.height
        )
        webView.layoutParams = params

        container.addView(webView)
        setContentView(container)

        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.mediaPlaybackRequiresUserGesture = false
        settings.loadsImagesAutomatically = true
        settings.cacheMode = WebSettings.LOAD_DEFAULT

        webView.webViewClient = WebViewClient()

        // Load URL from shared preferences
        val prefs = context.getSharedPreferences("url_prefs", Context.MODE_PRIVATE)
        val url = prefs.getString("url", null)
        if (url != null) {
            webView.loadUrl(url)
        }
    }

    fun loadUrl(url: String) {
        webView.loadUrl(url)
    }

    fun reload() {
        webView.reload()
    }
}