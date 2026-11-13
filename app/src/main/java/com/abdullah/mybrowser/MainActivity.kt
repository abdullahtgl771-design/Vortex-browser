package com.abdullah.mybrowser

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Intent
import android.content.SharedPreferences
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
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream

class Rule(var host: String, var css: String, var js: String, var on: Boolean)

class MainActivity : Activity() {

    private val tabs = mutableListOf<WebView>()
    private var cur = -1
    private lateinit var container: FrameLayout
    private lateinit var urlBar: EditText
    private lateinit var progress: ProgressBar
    private lateinit var tabBtn: Button
    private lateinit var prefs: SharedPreferences
    private val rules = mutableListOf<Rule>()

    private var adblock = true
    private var darkPage = false
    private var desktop = false

    private val homeUrl = "https://vortex.local/"

    private val blocked = listOf(
        "doubleclick.net", "googlesyndication.com", "googleadservices.com",
        "adservice.google.com", "adnxs.com", "taboola.com", "outbrain.com",
        "popads.net", "propellerads.com", "adsterra.com", "exoclick.com"
    )
    private val desktopUa =
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("vortex", MODE_PRIVATE)
        loadRules()
        buildUi()
        newTab(intent?.data?.toString() ?: "home")
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

    // ---------- URL helpers ----------

    private fun normalize(q: String): String {
        val t = q.trim()
        return when {
            t.startsWith("http://") || t.startsWith("https://") -> t
            !t.contains(" ") && t.contains(".") -> "https://$t"
            else -> "https://duckduckgo.com/?q=" + Uri.encode(t)
        }
    }

    private fun shown(u: String?): String {
        if (u == null || u.startsWith(homeUrl) || u == "about:blank") return ""
        return u
    }

    private fun hostOf(url: String?): String? {
        if (url == null) return null
        return try { Uri.parse(url).host } catch (e: Exception) { null }
    }

    private fun load(q: String) {
        tabs[cur].loadUrl(normalize(q))
        tabs[cur].requestFocus()
    }

    // ---------- Home page ----------

    private fun showHome(w: WebView) {
        w.loadDataWithBaseURL(homeUrl, homeHtml(), "text/html", "utf-8", homeUrl)
    }

    private fun homeHtml(): String = """
<!DOCTYPE html><html><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>Vortex</title>
<style>
*{box-sizing:border-box;margin:0;padding:0;-webkit-tap-highlight-color:transparent}
body{min-height:100vh;font-family:sans-serif;color:#fff;background:radial-gradient(circle at 20% 10%,#3b1d8f 0%,#0b0b1a 55%),#0b0b1a;display:flex;flex-direction:column;align-items:center;padding:48px 20px}
.logo{font-size:38px;font-weight:800;letter-spacing:4px;background:linear-gradient(90deg,#7c5cff,#00d4ff);-webkit-background-clip:text;-webkit-text-fill-color:transparent}
#clock{margin-top:6px;font-size:14px;opacity:.7}
form{width:100%;max-width:520px;margin:28px 0}
input{width:100%;padding:16px 20px;border-radius:30px;border:1px solid rgba(255,255,255,.18);background:rgba(255,255,255,.08);color:#fff;font-size:16px;outline:none}
.grid{display:grid;grid-template-columns:repeat(3,1fr);gap:14px;width:100%;max-width:520px}
.grid a{display:flex;flex-direction:column;align-items:center;gap:8px;padding:18px 6px;border-radius:18px;background:rgba(255,255,255,.07);border:1px solid rgba(255,255,255,.12);color:#fff;text-decoration:none;font-size:13px}
.grid i{width:42px;height:42px;border-radius:12px;display:flex;align-items:center;justify-content:center;font-style:normal;font-weight:700;font-size:18px}
</style></head><body>
<div class="logo">VORTEX</div>
<div id="clock"></div>
<form onsubmit="return go()"><input id="q" placeholder="Search or type URL" autocomplete="off"></form>
<div class="grid">
<a href="https://m.youtube.com"><i style="background:#ff0033">Y</i>YouTube</a>
<a href="https://www.google.com"><i style="background:#4285f4">G</i>Google</a>
<a href="https://github.com"><i style="background:#333">GH</i>GitHub</a>
<a href="https://m.facebook.com"><i style="background:#1877f2">f</i>Facebook</a>
<a href="https://en.wikipedia.org"><i style="background:#555">W</i>Wikipedia</a>
<a href="https://x.com"><i style="background:#000">X</i>X</a>
</div>
<script>
function go(){
  var v=document.getElementById('q').value.trim();
  if(!v) return false;
  if(v.indexOf(' ')<0 && v.indexOf('.')>0){
    location.href=(v.indexOf('http')==0)?v:('https://'+v);
  } else {
    location.href='https://duckduckgo.com/?q='+encodeURIComponent(v);
  }
  return false;
}
function tick(){
  var d=new Date(), h=d.getHours();
  var g=h<12?'Good morning':(h<18?'Good afternoon':'Good evening');
  document.getElementById('clock').textContent=g+'  |  '+d.toLocaleTimeString([],{hour:'2-digit',minute:'2-digit'});
}
tick(); setInterval(tick,20000);
</script></body></html>
"""

    // ---------- Site rules (injector) ----------

    private fun youtubeRule(): Rule {
        val css = ".ytp-ad-overlay-container,.ytp-ad-image-overlay,#masthead-ad,ytd-ad-slot-renderer," +
            "ytd-promoted-sparkles-web-renderer,.ytd-display-ad-renderer{display:none!important}"
        val js = """
(function(){
  if(window.__vxYT) return; window.__vxYT=1;
  setInterval(function(){
    var b=document.querySelector('.ytp-ad-skip-button,.ytp-skip-ad-button,.ytp-ad-skip-button-modern');
    if(b) b.click();
    var p=document.querySelector('.html5-video-player');
    var v=document.querySelector('video');
    if(!p||!v) return;
    var ad=p.classList.contains('ad-showing');
    if(ad && !window.__vxM){ window.__vxM=1; v.muted=true; }
    if(ad && isFinite(v.duration) && v.duration>0){ v.currentTime=v.duration; }
    if(!ad && window.__vxM){ window.__vxM=0; v.muted=false; }
  },400);
})();
"""
        return Rule("youtube.com", css, js, true)
    }

    private fun loadRules() {
        val raw = prefs.getString("rules", null)
        if (raw == null) {
            rules.add(youtubeRule())
            saveRules()
            return
        }
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                rules.add(Rule(o.getString("host"), o.optString("css"), o.optString("js"), o.optBoolean("on", true)))
            }
        } catch (e: Exception) {
        }
    }

    private fun saveRules() {
        val arr = JSONArray()
        for (r in rules) {
            val o = JSONObject()
            o.put("host", r.host)
            o.put("css", r.css)
            o.put("js", r.js)
            o.put("on", r.on)
            arr.put(o)
        }
        prefs.edit().putString("rules", arr.toString()).apply()
    }

    private fun matches(host: String, ruleHost: String): Boolean =
        host == ruleHost || host.endsWith("." + ruleHost)

    private fun cssJs(id: String, css: String): String =
        "(function(){var s=document.getElementById('" + id + "');" +
            "if(!s){s=document.createElement('style');s.id='" + id + "';document.documentElement.appendChild(s);}" +
            "s.textContent=" + JSONObject.quote(css) + ";})();"

    private fun inject(w: WebView, url: String?) {
        if (darkPage) {
            w.evaluateJavascript(
                "document.documentElement.style.filter='invert(1) hue-rotate(180deg)';" +
                    "document.querySelectorAll('img,video,canvas').forEach(function(e){e.style.filter='invert(1) hue-rotate(180deg)';});",
                null
            )
        } else {
            w.evaluateJavascript("document.documentElement.style.filter='';", null)
        }
        val host = hostOf(url) ?: return
        for ((i, r) in rules.withIndex()) {
            if (!r.on || !matches(host, r.host)) continue
            if (r.css.isNotBlank()) w.evaluateJavascript(cssJs("vxcss" + i, r.css), null)
            if (r.js.isNotBlank()) w.evaluateJavascript("try{" + r.js + "\n}catch(e){}", null)
        }
    }

    private fun showRuleEditor(hostIn: String) {
        val existing = rules.firstOrNull { it.host == hostIn }
        val pad = dp(16)
        val col = LinearLayout(this)
        col.orientation = LinearLayout.VERTICAL
        col.setPadding(pad, pad, pad, pad)

        val hostEt = EditText(this)
        hostEt.setSingleLine()
        hostEt.hint = "domain (e.g. youtube.com)"
        hostEt.setText(hostIn)

        val cssEt = EditText(this)
        cssEt.hint = "Custom CSS"
        cssEt.minLines = 4
        cssEt.gravity = Gravity.TOP
        cssEt.setText(existing?.css ?: "")

        val jsEt = EditText(this)
        jsEt.hint = "Custom JS"
        jsEt.minLines = 6
        jsEt.gravity = Gravity.TOP
        jsEt.setText(existing?.js ?: "")

        val onCb = CheckBox(this)
        onCb.text = "Enabled"
        onCb.isChecked = existing?.on ?: true

        col.addView(hostEt)
        col.addView(cssEt)
        col.addView(jsEt)
        col.addView(onCb)
        val sv = ScrollView(this)
        sv.addView(col)

        AlertDialog.Builder(this)
            .setTitle("Site injector")
            .setView(sv)
            .setPositiveButton("Save") { _, _ ->
                val h = hostEt.text.toString().trim().lowercase()
                if (h.isNotEmpty()) {
                    rules.removeAll { it.host == hostIn || it.host == h }
                    rules.add(Rule(h, cssEt.text.toString(), jsEt.text.toString(), onCb.isChecked))
                    saveRules()
                    tabs[cur].reload()
                }
            }
            .setNegativeButton("Delete") { _, _ ->
                rules.removeAll { it.host == hostIn }
                saveRules()
                tabs[cur].reload()
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    private fun currentRuleHost(): String? {
        val h = hostOf(tabs[cur].url) ?: return null
        if (h == "vortex.local") return null
        return h.removePrefix("www.").removePrefix("m.")
    }

    private fun showRulesList() {
        val names = rules.map { (if (it.on) "ON   " else "OFF  ") + it.host }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Site rules")
            .setItems(names) { _, i -> showRuleEditor(rules[i].host) }
            .setPositiveButton("+ Current site") { _, _ ->
                val h = currentRuleHost()
                if (h == null) Toast.makeText(this, "Open a site first", Toast.LENGTH_SHORT).show()
                else showRuleEditor(h)
            }
            .setNegativeButton("Close", null)
            .show()
    }

    // ---------- Tabs ----------

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
                if (view === tabs.getOrNull(cur)) urlBar.setText(shown(url))
            }

            override fun onPageFinished(view: WebView, url: String) {
                inject(view, url)
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
        if (url == "home") showHome(w) else w.loadUrl(normalize(url))
    }

    private fun switchTo(i: Int) {
        container.removeAllViews()
        cur = i
        container.addView(
            tabs[i],
            FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        )
        urlBar.setText(shown(tabs[i].url))
        tabBtn.text = tabs.size.toString()
    }

    private fun closeTab() {
        if (tabs.size == 1) return
        val w = tabs.removeAt(cur)
        w.destroy()
        switchTo(minOf(cur, tabs.size - 1))
    }

    private fun showTabs() {
        val names = tabs.map { t ->
            val title = t.title
            if (!title.isNullOrBlank()) title else (t.url ?: "New tab")
        }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Tabs")
            .setItems(names) { _, i -> switchTo(i) }
            .setPositiveButton("+ New") { _, _ -> newTab("home") }
            .setNegativeButton("Close current") { _, _ -> closeTab() }
            .show()
    }

    private fun showMenu(anchor: View) {
        val pm = PopupMenu(this, anchor)
        pm.menu.add(0, 1, 0, "New tab")
        pm.menu.add(0, 2, 1, "Home")
        pm.menu.add(0, 3, 2, "Desktop site: " + (if (desktop) "ON" else "OFF"))
        pm.menu.add(0, 4, 3, "Dark page: " + (if (darkPage) "ON" else "OFF"))
        pm.menu.add(0, 5, 4, "Adblock: " + (if (adblock) "ON" else "OFF"))
        pm.menu.add(0, 6, 5, "Site injector (this site)")
        pm.menu.add(0, 7, 6, "All site rules")
        pm.menu.add(0, 8, 7, "Share URL")
        pm.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> newTab("home")
                2 -> showHome(tabs[cur])
                3 -> {
                    desktop = !desktop
                    tabs[cur].settings.userAgentString =
                        if (desktop) desktopUa else WebSettings.getDefaultUserAgent(this)
                    tabs[cur].reload()
                }
                4 -> {
                    darkPage = !darkPage
                    inject(tabs[cur], tabs[cur].url)
                }
                5 -> {
                    adblock = !adblock
                    tabs[cur].reload()
                }
                6 -> {
                    val h = currentRuleHost()
                    if (h == null) Toast.makeText(this, "Open a site first", Toast.LENGTH_SHORT).show()
                    else showRuleEditor(h)
                }
                7 -> showRulesList()
                8 -> {
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
