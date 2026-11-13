package com.abdullah.mybrowser

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.speech.RecognizerIntent
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

// Code-drawn icons: 0 back, 1 forward, 2 home, 3 tabs, 4 menu, 5 mic, 6 search
class IconView(ctx: Context, private val kind: Int) : View(ctx) {
    var tint: Int = Color.parseColor("#D0D0E0")
    var count: Int = 1
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val t = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private val rect = RectF()

    init {
        p.style = Paint.Style.STROKE
        p.strokeCap = Paint.Cap.ROUND
        p.strokeJoin = Paint.Join.ROUND
        t.style = Paint.Style.FILL
        t.textAlign = Paint.Align.CENTER
        t.typeface = Typeface.DEFAULT_BOLD
        isClickable = true
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        invalidate()
    }

    override fun onDraw(c: Canvas) {
        val size = minOf(width, height) * 0.5f
        val a = if (isPressed) 110 else 255
        p.color = tint
        p.alpha = a
        t.color = tint
        t.alpha = a
        c.save()
        c.translate(width / 2f - size / 2f, height / 2f - size / 2f)
        c.scale(size / 24f, size / 24f)
        p.strokeWidth = 2f
        path.reset()
        when (kind) {
            0 -> {
                path.moveTo(19f, 12f); path.lineTo(5f, 12f)
                path.moveTo(12f, 5f); path.lineTo(5f, 12f); path.lineTo(12f, 19f)
                c.drawPath(path, p)
            }
            1 -> {
                path.moveTo(5f, 12f); path.lineTo(19f, 12f)
                path.moveTo(12f, 5f); path.lineTo(19f, 12f); path.lineTo(12f, 19f)
                c.drawPath(path, p)
            }
            2 -> {
                path.moveTo(3f, 11f); path.lineTo(12f, 3f); path.lineTo(21f, 11f)
                path.moveTo(5f, 9.5f); path.lineTo(5f, 21f); path.lineTo(19f, 21f); path.lineTo(19f, 9.5f)
                path.moveTo(10f, 21f); path.lineTo(10f, 15f); path.lineTo(14f, 15f); path.lineTo(14f, 21f)
                c.drawPath(path, p)
            }
            3 -> {
                c.drawRoundRect(4f, 4f, 20f, 20f, 5f, 5f, p)
                t.textSize = 10f
                c.drawText(count.toString(), 12f, 15.5f, t)
            }
            4 -> {
                c.drawCircle(12f, 5f, 1.9f, t)
                c.drawCircle(12f, 12f, 1.9f, t)
                c.drawCircle(12f, 19f, 1.9f, t)
            }
            5 -> {
                c.drawRoundRect(9f, 2f, 15f, 14f, 3f, 3f, p)
                rect.set(5f, 5f, 19f, 19f)
                c.drawArc(rect, 0f, 180f, false, p)
                c.drawLine(12f, 19f, 12f, 22f, p)
                c.drawLine(8f, 22f, 16f, 22f, p)
            }
            6 -> {
                c.drawCircle(10.5f, 10.5f, 6.5f, p)
                c.drawLine(15.5f, 15.5f, 21f, 21f, p)
            }
            else -> {
            }
        }
        c.restore()
    }
}

class MainActivity : Activity() {

    private val tabs = mutableListOf<WebView>()
    private var cur = -1
    private lateinit var container: FrameLayout
    private lateinit var urlBar: EditText
    private lateinit var progress: ProgressBar
    private lateinit var backBtn: IconView
    private lateinit var fwdBtn: IconView
    private lateinit var tabsBtn: IconView
    private lateinit var prefs: SharedPreferences
    private val rules = mutableListOf<Rule>()

    private var adblock = true
    private var darkPage = false
    private var desktop = false

    // home page greeting + clock: false kore dile hide
    private val showGreeting = true

    private val homeUrl = "https://vortex.local/"

    private val cBg = Color.parseColor("#07070C")
    private val cBar = Color.parseColor("#0E0E14")
    private val cPill = Color.parseColor("#1A1A26")
    private val cLine = Color.parseColor("#1F1F2B")
    private val cOn = Color.parseColor("#D0D0E0")
    private val cOff = Color.parseColor("#4A4A5E")
    private val cAccent = Color.parseColor("#9A86FF")

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
        window.statusBarColor = cBar
        window.navigationBarColor = cBar
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

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 77 && resultCode == RESULT_OK) {
            val list = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val text = list?.firstOrNull()
            if (!text.isNullOrBlank()) load(text)
        }
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun startVoice() {
        val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Search with Vortex")
        try {
            startActivityForResult(i, 77)
        } catch (e: Exception) {
            Toast.makeText(this, "Voice search not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun buildUi() {
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(cBg)

        // ----- top: slim search pill -----
        val top = LinearLayout(this)
        top.orientation = LinearLayout.HORIZONTAL
        top.setBackgroundColor(cBar)
        top.setPadding(dp(10), dp(8), dp(10), dp(8))

        val pill = LinearLayout(this)
        pill.orientation = LinearLayout.HORIZONTAL
        pill.gravity = Gravity.CENTER_VERTICAL
        val pillBg = GradientDrawable()
        pillBg.setColor(cPill)
        pillBg.cornerRadius = dp(14).toFloat()
        pill.background = pillBg

        val searchIcon = IconView(this, 6)
        searchIcon.tint = cOff
        searchIcon.isClickable = false

        urlBar = EditText(this)
        urlBar.setSingleLine()
        urlBar.hint = "Search or enter URL"
        urlBar.textSize = 15f
        urlBar.setTextColor(Color.WHITE)
        urlBar.setHintTextColor(Color.parseColor("#6C6C84"))
        urlBar.setBackgroundColor(Color.TRANSPARENT)
        urlBar.setSelectAllOnFocus(true)
        urlBar.imeOptions = EditorInfo.IME_ACTION_GO
        urlBar.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_GO || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                load(urlBar.text.toString())
                true
            } else false
        }

        val mic = IconView(this, 5)
        mic.tint = cAccent
        mic.setOnClickListener { startVoice() }

        pill.addView(searchIcon, LinearLayout.LayoutParams(dp(40), dp(44)))
        pill.addView(urlBar, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        pill.addView(mic, LinearLayout.LayoutParams(dp(44), dp(44)))
        top.addView(pill, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
        progress.max = 100
        progress.visibility = View.GONE

        container = FrameLayout(this)

        // ----- bottom nav -----
        val nav = LinearLayout(this)
        nav.orientation = LinearLayout.HORIZONTAL
        nav.setBackgroundColor(cBar)

        backBtn = IconView(this, 0)
        backBtn.setOnClickListener { if (tabs[cur].canGoBack()) tabs[cur].goBack() }
        fwdBtn = IconView(this, 1)
        fwdBtn.setOnClickListener { if (tabs[cur].canGoForward()) tabs[cur].goForward() }
        val homeBtn = IconView(this, 2)
        homeBtn.tint = cAccent
        homeBtn.setOnClickListener { showHome(tabs[cur]) }
        tabsBtn = IconView(this, 3)
        tabsBtn.setOnClickListener { showTabs() }
        val menuBtn = IconView(this, 4)
        menuBtn.setOnClickListener { v -> showMenu(v) }

        val navLp = { LinearLayout.LayoutParams(0, dp(52), 1f) }
        nav.addView(backBtn, navLp())
        nav.addView(fwdBtn, navLp())
        nav.addView(homeBtn, navLp())
        nav.addView(tabsBtn, navLp())
        nav.addView(menuBtn, navLp())

        val line = View(this)
        line.setBackgroundColor(cLine)

        root.addView(top)
        root.addView(progress, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(2)))
        root.addView(container, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
        root.addView(line, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(1)))
        root.addView(nav)
        setContentView(root)
    }

    private fun refreshNav() {
        val w = tabs.getOrNull(cur) ?: return
        backBtn.tint = if (w.canGoBack()) cOn else cOff
        fwdBtn.tint = if (w.canGoForward()) cOn else cOff
        backBtn.invalidate()
        fwdBtn.invalidate()
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

    private fun homeHtml(): String {
        val greet = if (showGreeting) "<div id=\"clock\"></div>" else ""
        return """
<!DOCTYPE html><html><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>Vortex</title>
<style>
*{box-sizing:border-box;margin:0;padding:0;-webkit-tap-highlight-color:transparent}
body{font-family:sans-serif;color:#fff;background:radial-gradient(ellipse at 50% -10%,rgba(124,92,255,.20),transparent 60%),linear-gradient(180deg,#0d0d16,#06060a);background-attachment:fixed;display:flex;flex-direction:column;align-items:center;padding:56px 18px 32px;min-height:100vh}
.logo{font-size:20px;font-weight:600;letter-spacing:8px;padding-left:8px;color:#ececf6}
.line{width:28px;height:3px;border-radius:2px;background:#7c5cff;margin-top:10px}
#clock{margin-top:14px;font-size:12px;opacity:.55}
form{width:100%;max-width:480px;margin:26px 0 30px}
.sb{display:flex;align-items:center;height:46px;border-radius:12px;background:rgba(255,255,255,.07);border:1px solid rgba(255,255,255,.10);padding:0 6px 0 14px}
.sb svg{flex:none}
input{flex:1;min-width:0;height:100%;background:transparent;border:0;outline:none;color:#fff;font-size:15px;padding:0 10px}
input::placeholder{color:rgba(255,255,255,.4)}
.mic{width:36px;height:36px;display:flex;align-items:center;justify-content:center;border-radius:10px}
.grid{display:grid;grid-template-columns:repeat(4,1fr);gap:18px 8px;width:100%;max-width:480px}
.grid a{display:flex;flex-direction:column;align-items:center;gap:8px;color:#cfcfe0;text-decoration:none;font-size:11.5px}
.grid i{width:52px;height:52px;border-radius:14px;display:flex;align-items:center;justify-content:center;font-style:normal;font-weight:700;font-size:19px;background:rgba(255,255,255,.07);border:1px solid rgba(255,255,255,.08)}
</style></head><body>
<div class="logo">VORTEX</div>
<div class="line"></div>
$greet
<form onsubmit="return go()">
  <div class="sb">
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#9a9ab5" stroke-width="2" stroke-linecap="round"><circle cx="10.5" cy="10.5" r="6.5"/><path d="M15.5 15.5L21 21"/></svg>
    <input id="q" placeholder="Search or enter URL" autocomplete="off">
    <a class="mic" href="vortex://voice"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#b9a8ff" stroke-width="2" stroke-linecap="round"><rect x="9" y="2" width="6" height="12" rx="3"/><path d="M5 11a7 7 0 0 0 14 0M12 18v4M8 22h8"/></svg></a>
  </div>
</form>
<div class="grid">
<a href="https://m.youtube.com"><i style="color:#ff5252">Y</i>YouTube</a>
<a href="https://www.google.com"><i style="color:#6ea8ff">G</i>Google</a>
<a href="https://github.com"><i style="color:#e6e6f0">GH</i>GitHub</a>
<a href="https://m.facebook.com"><i style="color:#5b9bff">f</i>Facebook</a>
<a href="https://en.wikipedia.org"><i style="color:#d0d0d8">W</i>Wikipedia</a>
<a href="https://x.com"><i style="color:#ffffff">X</i>X</a>
<a href="https://www.reddit.com"><i style="color:#ff7a45">R</i>Reddit</a>
<a href="https://mail.google.com"><i style="color:#ff6b6b">M</i>Gmail</a>
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
  var c=document.getElementById('clock');
  if(!c) return;
  var d=new Date(), h=d.getHours();
  var g=h<12?'Good morning':(h<18?'Good afternoon':'Good evening');
  c.textContent=g+'  |  '+d.toLocaleTimeString([],{hour:'2-digit',minute:'2-digit'});
}
tick(); setInterval(tick,20000);
</script></body></html>
"""
    }

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
        w.setBackgroundColor(cBg)
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
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                if (request.url.toString() == "vortex://voice") {
                    startVoice()
                    return true
                }
                return false
            }

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
                if (view === tabs.getOrNull(cur)) {
                    urlBar.setText(shown(url))
                    refreshNav()
                }
            }

            override fun onPageFinished(view: WebView, url: String) {
                inject(view, url)
                if (view === tabs.getOrNull(cur)) refreshNav()
            }

            override fun doUpdateVisitedHistory(view: WebView, url: String, isReload: Boolean) {
                if (view === tabs.getOrNull(cur)) refreshNav()
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
        tabsBtn.count = tabs.size
        tabsBtn.invalidate()
        refreshNav()
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
        pm.menu.add(0, 2, 1, "Reload")
        pm.menu.add(0, 3, 2, "Desktop site: " + (if (desktop) "ON" else "OFF"))
        pm.menu.add(0, 4, 3, "Dark page: " + (if (darkPage) "ON" else "OFF"))
        pm.menu.add(0, 5, 4, "Adblock: " + (if (adblock) "ON" else "OFF"))
        pm.menu.add(0, 6, 5, "Site injector (this site)")
        pm.menu.add(0, 7, 6, "All site rules")
        pm.menu.add(0, 8, 7, "Share URL")
        pm.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> newTab("home")
                2 -> tabs[cur].reload()
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
