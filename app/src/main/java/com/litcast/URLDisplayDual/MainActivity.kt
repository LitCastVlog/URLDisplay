package com.litcast.URLDisplayDual

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.LinearLayout

class MainActivity : Activity() {

    private lateinit var mainView: WebView
    private lateinit var topBar: WebView
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var mainUrl: String
    private lateinit var topBarUrl: String
    private var topBarHeightDp: Int = 32

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Fullscreen immersive mode
        window.decorView.systemUiVisibility =
            (View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        checkFirstRun()
    }

    private fun checkFirstRun() {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val savedMainUrl = prefs.getString("mainUrl", null)
        val savedTopBarUrl = prefs.getString("topBarUrl", null)
        val savedTopBarHeight = prefs.getInt("topBarHeightDp", -1)

        if (savedMainUrl == null || savedTopBarUrl == null || savedTopBarHeight == -1) {
            promptUserSettings()
        } else {
            mainUrl = savedMainUrl
            topBarUrl = savedTopBarUrl
            topBarHeightDp = savedTopBarHeight
            setupViews()
        }
    }

    private fun promptUserSettings() {
        val mainInput = EditText(this)
        mainInput.hint = "Enter main URL"

        val topBarInput = EditText(this)
        topBarInput.hint = "Enter top bar URL"

        val heightInput = EditText(this)
        heightInput.hint = "Top bar height in dp"

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(30, 20, 30, 20)
        layout.addView(mainInput)
        layout.addView(topBarInput)
        layout.addView(heightInput)

        AlertDialog.Builder(this)
            .setTitle("Setup URLs")
            .setView(layout)
            .setCancelable(false)
            .setPositiveButton("Save") { _, _ ->
                mainUrl = mainInput.text.toString()
                topBarUrl = topBarInput.text.toString()
                topBarHeightDp = heightInput.text.toString().toIntOrNull() ?: 32

                val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                prefs.edit()
                    .putString("mainUrl", mainUrl)
                    .putString("topBarUrl", topBarUrl)
                    .putInt("topBarHeightDp", topBarHeightDp)
                    .apply()

                setupViews()
            }
            .show()
    }

    @Suppress("SetJavaScriptEnabled")
    private fun setupViews() {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setBackgroundColor(0xFF000000.toInt())

        // Top bar WebView (software layer)
        topBar = WebView(this)
        val topBarHeightPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            topBarHeightDp.toFloat(),
            resources.displayMetrics
        ).toInt()
        topBar.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            topBarHeightPx
        )
        val topBarSettings: WebSettings = topBar.settings
        topBarSettings.javaScriptEnabled = true
        topBarSettings.domStorageEnabled = true
        topBarSettings.loadsImagesAutomatically = true
        topBar.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        topBar.webViewClient = WebViewClient()
        val topBarHtml = """
            <html>
            <body style="margin:0;background:black;overflow:hidden;">
            <iframe src="$topBarUrl" style="width:100%;height:100%;border:0;"></iframe>
            </body>
            </html>
        """.trimIndent()
        topBar.loadData(topBarHtml, "text/html", "utf-8")

        // Main WebView (hardware accelerated)
        mainView = WebView(this)
        mainView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1f
        )
        val ws: WebSettings = mainView.settings
        ws.javaScriptEnabled = true
        ws.domStorageEnabled = true
        ws.loadsImagesAutomatically = true
        ws.mediaPlaybackRequiresUserGesture = false
        ws.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        ws.setSupportZoom(false)
        ws.builtInZoomControls = false
        ws.displayZoomControls = false
        mainView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        mainView.webViewClient = WebViewClient()
        mainView.loadUrl(mainUrl)

        layout.addView(topBar)
        layout.addView(mainView)
        setContentView(layout)

        mirrorToExternalDisplay()
    }

    private fun mirrorToExternalDisplay() {
        val dm = getSystemService(Context.DISPLAY_SERVICE) as android.hardware.display.DisplayManager
        val displays = dm.displays

        for (display in displays) {
            if (display.displayId != android.view.Display.DEFAULT_DISPLAY) {
                val presentation = URLPresentation(this, display)
                presentation.show()
            }
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
}
