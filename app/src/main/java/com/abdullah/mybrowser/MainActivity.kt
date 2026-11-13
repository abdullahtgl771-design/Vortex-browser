package com.abdullah.mybrowser

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.webkit.*
import android.widget.*
import java.io.ByteArrayInputStream

class MainActivity : Activity() {

    private val tabs = mutableListOf<WebView>()
    private var cur = -1
    private lateinit var container: FrameLayout
    private lateinit var urlBar: EditText
    private lateinit var progress: ProgressBar
    private lateinit var tabBtn: Button

    private var adblock = true
    private var darkPage = false
    private var desktop = false

    private val blocked = listOf(
        "doubleclick.net", "googlesyndication.com", "googleadservices.com",
        "adservice.google.com", "adnxs.com", "taboola.com", "outbrain.com",
        "popads.net", "propellerads.com", "adsterra.com", "exoclick.com"
    )
    private val desktopUa =
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
        newTab(intent?.data?.toString() ?: "https://duckduckgo.com")
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val w = tabs[cur]
        if (w.canGoBack()) w.goBack()
        else if (tabs.size > 1) closeTab()
        else finish()
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun btn(t: String, click: (View) -> Unit): Button {
        val b = Button(this)
        b.text = t
        b.minWidth = 0
        b.minimumWidth = dp(44)
        b.setPadding(dp(4), 0, dp(4), 0)
        b.setOnClickListener { v -> click(v) }
        return b
    }

    private fun buildUi() {
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL

        val bar = LinearLayout(this)
        bar.orientation = LinearLayout.HORIZONTAL
        bar.gravity = Gravity.CENTER_VERTICAL
        bar.setPadding(dp(4), dp(4), dp(4), dp(4))

        urlBar = EditText(this)
        urlBar.setSingleLine()
        urlBar.hint = "Search or URL"
        urlBar.imeOptions = EditorInfo.IME_ACTION_GO
        urlBar.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_GO || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                load(urlBar.text.toString())
                true
            } else false
        }

        tabBtn = btn("1") { showTabs() }

        bar.addView(btn("<") { if (tabs[cur].canGoBack()) tabs[cur].goBack() })
        bar.addView(btn(">") { if (tabs[cur].canGoForward()) tabs[cur].goForward() })
        bar.addView(urlBar, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        bar.addView(btn("R") { tabs[cur].reload() })
        bar.addView(tabBtn)
        bar.addView(btn("...") { v -> showMenu(v) })

        progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
        progress.max = 100
        progress.visibility = View.GONE

        container = FrameLayout(this)

        root.addView(bar)
        root.addView(progress, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(3)))
        root.addView(container, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
        setContentView(root)
    }

    private fun normalize(q: String): String {
        val t = q.trim()
        return when {
            t.startsWith("http://") || t.startsWith("https://") -> t
            !t.contains(" ") && t.contains(".") -> "https://$t"
            else -> "https://duckduckgo.com/?q=" + Uri.encode(t)
        }
    }

    private fun load(q: String) {
        tabs[cur].loadUrl(normalize(q))
        tabs[cur].requestFocus()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun newTab(url: String) {
        val w = WebView(this)
        val s = w.settings
        s.javaScriptEnabled = true
        s.domStorageEnabled = true
        s.builtInZoomControls = true
        s.displayZoomControls = false
        s.loadWithOverviewMode = true
        s.useWideViewPort = true
        s.mediaPlaybackRequiresUserGesture = false
        if (desktop) s.userAgentString = desktopUa

        w.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                if (adblock) {
                    val host = request.url.host ?: ""
                    if (blocked.any { host == it || host.endsWith(".$it") }) {
                        return WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream(ByteArray(0)))
                    }
                }
                return null
            }

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                if (view === tabs.getOrNull(cur)) urlBar.setText(url)
            }

            override fun onPageFinished(view: WebView, url: String) {
                inject(view)
            }
        }

        w.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                if (view !== tabs.getOrNull(cur)) return
                progress.progress = newProgress
                progress.visibility = if (newProgress in 1..99) View.VISIBLE else View.GONE
            }
        }

        w.setDownloadListener { dlUrl, userAgent, contentDisposition, mime, _ ->
            val req = DownloadManager.Request(Uri.parse(dlUrl))
            req.setMimeType(mime)
            req.addRequestHeader("User-Agent", userAgent)
            req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            req.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                URLUtil.guessFileName(dlUrl, contentDisposition, mime)
            )
            val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(req)
            Toast.makeText(this, "Downloading...", Toast.LENGTH_SHORT).show()
        }

        tabs.add(w)
        switchTo(tabs.size - 1)
        w.loadUrl(normalize(url))
    }

    private fun switchTo(i: Int) {
        container.removeAllViews()
        cur = i
        container.addView(
            tabs[i],
            FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        )
        urlBar.setText(tabs[i].url ?: "")
        tabBtn.text = tabs.size.toString()
    }

    private fun closeTab() {
        if (tabs.size == 1) return
        val w = tabs.removeAt(cur)
        w.destroy()
        switchTo(minOf(cur, tabs.size - 1))
    }

    // Per-page injector: ekhane nijer custom CSS/JS rule boshabi
    private fun inject(w: WebView) {
        val js = if (darkPage) {
            "document.documentElement.style.filter='invert(1) hue-rotate(180deg)';" +
            "document.querySelectorAll('img,video,canvas').forEach(function(e){e.style.filter='invert(1) hue-rotate(180deg)';});"
        } else {
            "document.documentElement.style.filter='';"
        }
        w.evaluateJavascript(js, null)
    }

    private fun showTabs() {
        val names = tabs.map { t ->
            val title = t.title
            if (!title.isNullOrBlank()) title else (t.url ?: "New tab")
        }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Tabs")
            .setItems(names) { _, i -> switchTo(i) }
            .setPositiveButton("+ New") { _, _ -> newTab("https://duckduckgo.com") }
            .setNegativeButton("Close current") { _, _ -> closeTab() }
            .show()
    }

    private fun showMenu(anchor: View) {
        val pm = PopupMenu(this, anchor)
        pm.menu.add(0, 1, 0, "New tab")
        pm.menu.add(0, 2, 1, if (desktop) "Mobile site" else "Desktop site")
        pm.menu.add(0, 3, 2, if (darkPage) "Dark page: OFF" else "Dark page: ON")
        pm.menu.add(0, 4, 3, if (adblock) "Adblock: OFF" else "Adblock: ON")
        pm.menu.add(0, 5, 4, "Share URL")
        pm.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> newTab("https://duckduckgo.com")
                2 -> {
                    desktop = !desktop
                    tabs[cur].settings.userAgentString =
                        if (desktop) desktopUa else WebSettings.getDefaultUserAgent(this)
                    tabs[cur].reload()
                }
                3 -> {
                    darkPage = !darkPage
                    inject(tabs[cur])
                }
                4 -> {
                    adblock = !adblock
                    tabs[cur].reload()
                }
                5 -> {
                    val i = Intent(Intent.ACTION_SEND)
                    i.type = "text/plain"
                    i.putExtra(Intent.EXTRA_TEXT, tabs[cur].url)
                    startActivity(Intent.createChooser(i, "Share"))
                }
            }
            true
        }
        pm.show()
    }
}
