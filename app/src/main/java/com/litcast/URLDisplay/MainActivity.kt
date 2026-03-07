package com.litcast.URLDisplay

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Display
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText

class MainActivity : Activity() {

    private lateinit var webView: WebView
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var prefs: android.content.SharedPreferences
    private var url: String? = null

    private val reloadRunnable = object : Runnable {
        override fun run() {
            webView.reload()
            handler.postDelayed(this, 600_000)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("url_prefs", Context.MODE_PRIVATE)
        url = prefs.getString("url", null)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        window.decorView.systemUiVisibility =
            (View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        setContentView(R.layout.activity_main)
        webView = findViewById(R.id.urlView)

        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.mediaPlaybackRequiresUserGesture = false
        settings.loadsImagesAutomatically = true
        settings.cacheMode = WebSettings.LOAD_DEFAULT

        webView.webViewClient = WebViewClient()

        if (url == null) {
            promptForUrl()
        } else {
            loadLink(url!!)
            mirrorToExternalDisplay()
        }

        handler.postDelayed(reloadRunnable, 600_000)
    }

    private fun loadLink(loadUrl: String) {
        webView.loadUrl(loadUrl)
    }

    private fun promptForUrl() {
        val input = EditText(this)
        input.hint = "Enter URL to display"

        AlertDialog.Builder(this)
            .setTitle("Set URL")
            .setView(input)
            .setCancelable(false)
            .setPositiveButton("Save") { _, _ ->

                var entered = input.text.toString()

                if (!entered.startsWith("http")) {
                    entered = "https://$entered"
                }

                prefs.edit().putString("url", entered).apply()

                url = entered
                loadLink(entered)

                mirrorToExternalDisplay()
            }
            .show()
    }

    private fun mirrorToExternalDisplay() {
        val dm = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val displays = dm.displays

        for (display: Display in displays) {
            if (display.displayId != Display.DEFAULT_DISPLAY) {
                val presentation = UrlPresentation(this, display)
                presentation.show()
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {

            KeyEvent.KEYCODE_VOLUME_UP -> {
                webView.evaluateJavascript(
                    "window.dispatchEvent(new KeyboardEvent('keydown',{key:'ArrowLeft'}));",
                    null
                )
                true
            }

            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                webView.evaluateJavascript(
                    "window.dispatchEvent(new KeyboardEvent('keydown',{key:'ArrowRight'}));",
                    null
                )
                true
            }

            else -> super.onKeyDown(keyCode, event)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) {
            window.decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }

    override fun onDestroy() {
        handler.removeCallbacks(reloadRunnable)
        super.onDestroy()
    }
}