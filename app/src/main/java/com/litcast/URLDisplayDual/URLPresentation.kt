package com.litcast.URLDisplayDual

import android.app.Presentation
import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.Display
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout

class URLPresentation(context: Context, display: Display) :
    Presentation(context, display) {

    private lateinit var mainView: WebView
    private lateinit var topBar: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val mainUrl = prefs.getString("mainUrl", null) ?: return
        val topBarUrl = prefs.getString("topBarUrl", null) ?: return
        val topBarHeightDp = prefs.getInt("topBarHeightDp", 32)

        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL
        layout.setBackgroundColor(0xFF000000.toInt())

        // Top bar WebView (software layer)
        topBar = WebView(context)
        val topBarHeightPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            topBarHeightDp.toFloat(),
            context.resources.displayMetrics
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
        mainView = WebView(context)
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
    }
}
