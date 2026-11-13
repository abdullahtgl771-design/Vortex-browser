package com.abdullah.mybrowser

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.speech.RecognizerIntent
import android.text.TextUtils
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.webkit.*
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream

class Rule(var host: String, var css: String, var js: String, var on: Boolean)

class Tool(val kind: Int, val title: String, val sub: String, val action: () -> Unit)

// Code-drawn icons
// 0 back, 1 forward, 2 home, 3 tabs, 4 menu, 5 mic, 6 search, 7 close, 8 chevron, 9 plus,
// 10 shield, 11 incognito, 12 translate, 13 screenshot, 14 desktop, 15 download, 16 share,
// 17 globe, 18 contrast, 19 code, 20 list, 21 reload
class IconView(ctx: Context, private val kind: Int) : View(ctx) {
    var tint: Int = Color.parseColor("#D0D0E0")
    var count: Int = 1
    var ratio: Float = 0.54f
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
        val size = minOf(width, height) * ratio
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
            7 -> {
                c.drawLine(6f, 6f, 18f, 18f, p)
                c.drawLine(18f, 6f, 6f, 18f, p)
            }
            8 -> {
                path.moveTo(9f, 5f); path.lineTo(16f, 12f); path.lineTo(9f, 19f)
                c.drawPath(path, p)
            }
            9 -> {
                c.drawLine(12f, 5f, 12f, 19f, p)
                c.drawLine(5f, 12f, 19f, 12f, p)
            }
            10 -> {
                path.moveTo(12f, 3f); path.lineTo(20f, 6f); path.lineTo(20f, 12f)
                path.quadTo(20f, 18f, 12f, 21f)
                path.quadTo(4f, 18f, 4f, 12f)
                path.lineTo(4f, 6f); path.close()
                c.drawPath(path, p)
                c.drawLine(9f, 9f, 15f, 15f, p)
            }
            11 -> {
                path.moveTo(5f, 11f); path.lineTo(8f, 4f); path.lineTo(16f, 4f); path.lineTo(19f, 11f)
                c.drawPath(path, p)
                c.drawLine(3f, 11f, 21f, 11f, p)
                c.drawCircle(8f, 16.5f, 2.6f, p)
                c.drawCircle(16f, 16.5f, 2.6f, p)
                c.drawLine(10.6f, 16f, 13.4f, 16f, p)
            }
            12 -> {
                t.textSize = 12f
                c.drawText("\u6587A", 12f, 16f, t)
            }
            13 -> {
                path.moveTo(4f, 9f); path.lineTo(4f, 4f); path.lineTo(9f, 4f)
                path.moveTo(15f, 4f); path.lineTo(20f, 4f); path.lineTo(20f, 9f)
                path.moveTo(20f, 15f); path.lineTo(20f, 20f); path.lineTo(15f, 20f)
                path.moveTo(9f, 20f); path.lineTo(4f, 20f); path.lineTo(4f, 15f)
                c.drawPath(path, p)
            }
            14 -> {
                c.drawRoundRect(3f, 4f, 21f, 16f, 2f, 2f, p)
                c.drawLine(12f, 16f, 12f, 20f, p)
                c.drawLine(8f, 20f, 16f, 20f, p)
            }
            15 -> {
                c.drawLine(12f, 4f, 12f, 15f, p)
                path.moveTo(7f, 10f); path.lineTo(12f, 15f); path.lineTo(17f, 10f)
                c.drawPath(path, p)
                c.drawLine(5f, 19f, 19f, 19f, p)
            }
            16 -> {
                c.drawCircle(6f, 12f, 2.4f, p)
                c.drawCircle(18f, 6f, 2.4f, p)
                c.drawCircle(18f, 18f, 2.4f, p)
                c.drawLine(8.2f, 11f, 15.8f, 7f, p)
                c.drawLine(8.2f, 13f, 15.8f, 17f, p)
            }
            17 -> {
                c.drawCircle(12f, 12f, 9f, p)
                rect.set(8f, 3f, 16f, 21f)
                c.drawOval(rect, p)
                c.drawLine(3f, 12f, 21f, 12f, p)
            }
            18 -> {
                c.drawCircle(12f, 12f, 8f, p)
                rect.set(4f, 4f, 20f, 20f)
                c.drawArc(rect, 90f, 180f, true, t)
            }
            19 -> {
                path.moveTo(8f, 7f); path.lineTo(3f, 12f); path.lineTo(8f, 17f)
                path.moveTo(16f, 7f); path.lineTo(21f, 12f); path.lineTo(16f, 17f)
                c.drawPath(path, p)
                c.drawLine(14f, 5f, 10f, 19f, p)
            }
            20 -> {
                c.drawLine(8f, 6f, 20f, 6f, p)
                c.drawLine(8f, 12f, 20f, 12f, p)
                c.drawLine(8f, 18f, 20f, 18f, p)
                c.drawCircle(4f, 6f, 1.2f, t)
                c.drawCircle(4f, 12f, 1.2f, t)
                c.drawCircle(4f, 18f, 1.2f, t)
            }
            21 -> {
                rect.set(4.5f, 4.5f, 19.5f, 19.5f)
                c.drawArc(rect, -45f, 280f, false, p)
                path.moveTo(19.5f, 3.5f); path.lineTo(19.5f, 8.5f); path.lineTo(14.5f, 8.5f)
                c.drawPath(path, p)
            }
            else -> {
            }
        }
        c.restore()
    }
}

class MainActivity : Activity() {

    private val tabs = mutableListOf<WebView>()
    private val incog = HashSet<WebView>()
    private val thumbs = HashMap<WebView, Bitmap>()
    private var cur = -1
    private lateinit var container: FrameLayout
    private lateinit var overlay: FrameLayout
    private lateinit var urlBar: EditText
    private lateinit var progress: ProgressBar
    private lateinit var pill: LinearLayout
    private lateinit var pillBg: GradientDrawable
    private lateinit var spacer: View
    private lateinit var homeBtn: IconView
    private lateinit var tabsBtn: IconView
    private lateinit var prefs: SharedPreferences
    private val rules = mutableListOf<Rule>()

    private var adblock = true
    private var darkPage = false
    private var desktop = false
    private var engineIdx = 0

    // home page greeting + clock: false kore dile hide
    private val showGreeting = true

    // Translator target language (bn = Bangla, en = English, hi = Hindi)
    private val translateTo = "bn"

    private val engines = listOf(
        Pair("Google", "https://www.google.com/search?q="),
        Pair("DuckDuckGo", "https://duckduckgo.com/?q="),
        Pair("Bing", "https://www.bing.com/search?q=")
    )

    private val homeUrl = "https://vortex.local/"

    private val cBg = Color.parseColor("#07070C")
    private val cBar = Color.parseColor("#0E0E14")
    private val cPill = Color.parseColor("#1A1A26")
    private val cLine = Color.parseColor("#1F1F2B")
    private val cCard = Color.parseColor("#12121C")
    private val cStroke = Color.parseColor("#26263A")
    private val cGray = Color.parseColor("#8A8AA0")
    private val cAccent = Color.parseColor("#9A86FF")

    private val mp = ViewGroup.LayoutParams.MATCH_PARENT
    private val wc = ViewGroup.LayoutParams.WRAP_CONTENT

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
        engineIdx = prefs.getInt("engine", 0).coerceIn(0, engines.size - 1)
        loadRules()
        window.statusBarColor = cBar
        window.navigationBarColor = cBg
        buildUi()
        newTab(intent?.data?.toString() ?: "home")
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (overlay.visibility == View.VISIBLE) {
            closeOverlay()
            return
        }
        val w = tabs[cur]
        if (w.canGoBack()) w.goBack()
        else if (tabs.size > 1) closeTabAt(cur)
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

    private fun lp(w: Int, h: Int, weight: Float = 0f) = LinearLayout.LayoutParams(w, h, weight)

    private fun cellLp(): LinearLayout.LayoutParams {
        val l = LinearLayout.LayoutParams(0, wc, 1f)
        l.setMargins(dp(6), dp(6), dp(6), dp(6))
        return l
    }

    private fun toast(m: String) {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show()
    }

    private fun onOff(b: Boolean) = if (b) "ON" else "OFF"

    private fun startVoice() {
        val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Search with Vortex")
        try {
            startActivityForResult(i, 77)
        } catch (e: Exception) {
            toast("Voice search not available")
        }
    }

    // ---------- UI ----------

    private fun buildUi() {
        val frame = FrameLayout(this)
        val main = LinearLayout(this)
        main.orientation = LinearLayout.VERTICAL
        main.setBackgroundColor(cBg)

        // Chrome-style toolbar: home | pill | + | tabs | menu
        val toolbar = LinearLayout(this)
        toolbar.orientation = LinearLayout.HORIZONTAL
        toolbar.gravity = Gravity.CENTER_VERTICAL
        toolbar.setBackgroundColor(cBar)
        toolbar.setPadding(dp(4), dp(6), dp(4), dp(6))

        homeBtn = IconView(this, 2)
        homeBtn.setOnClickListener { showHome(tabs[cur]) }

        pill = LinearLayout(this)
        pill.orientation = LinearLayout.HORIZONTAL
        pill.gravity = Gravity.CENTER_VERTICAL
        pillBg = GradientDrawable()
        pillBg.setColor(cPill)
        pillBg.cornerRadius = dp(20).toFloat()
        pill.background = pillBg

        urlBar = EditText(this)
        urlBar.setSingleLine()
        urlBar.hint = "Search or enter URL"
        urlBar.textSize = 15f
        urlBar.setTextColor(Color.WHITE)
        urlBar.setHintTextColor(Color.parseColor("#6C6C84"))
        urlBar.setBackgroundColor(Color.TRANSPARENT)
        urlBar.setPadding(dp(16), 0, 0, 0)
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

        pill.addView(urlBar, lp(0, mp, 1f))
        pill.addView(mic, lp(dp(40), dp(40)))

        spacer = View(this)

        val plusBtn = IconView(this, 9)
        plusBtn.ratio = 0.6f
        plusBtn.setOnClickListener { newTab("home") }
        tabsBtn = IconView(this, 3)
        tabsBtn.setOnClickListener { showTabsPage() }
        val menuBtn = IconView(this, 4)
        menuBtn.setOnClickListener { showToolbox() }

        toolbar.addView(homeBtn, lp(dp(44), dp(44)))
        val pl = lp(0, dp(40), 1f)
        pl.setMargins(dp(4), 0, dp(4), 0)
        toolbar.addView(pill, pl)
        toolbar.addView(spacer, lp(0, 1, 1f))
        toolbar.addView(plusBtn, lp(dp(44), dp(44)))
        toolbar.addView(tabsBtn, lp(dp(44), dp(44)))
        toolbar.addView(menuBtn, lp(dp(44), dp(44)))

        val line = View(this)
        line.setBackgroundColor(cLine)

        progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
        progress.max = 100
        progress.visibility = View.GONE
        progress.progressTintList = ColorStateList.valueOf(cAccent)

        container = FrameLayout(this)

        main.addView(toolbar)
        main.addView(line, lp(mp, dp(1)))
        main.addView(progress, lp(mp, dp(2)))
        main.addView(container, lp(mp, 0, 1f))

        overlay = FrameLayout(this)
        overlay.visibility = View.GONE

        frame.addView(main, FrameLayout.LayoutParams(mp, mp))
        frame.addView(overlay, FrameLayout.LayoutParams(mp, mp))
        setContentView(frame)
    }

    private fun updateToolbar(u: String?) {
        val home = shown(u).isEmpty()
        pill.visibility = if (home) View.GONE else View.VISIBLE
        spacer.visibility = if (home) View.VISIBLE else View.GONE
        homeBtn.visibility = if (home) View.INVISIBLE else View.VISIBLE
        val w = tabs.getOrNull(cur)
        val inc = w != null && incog.contains(w)
        pillBg.setStroke(dp(1), if (inc) cAccent else Color.TRANSPARENT)
    }

    private fun openOverlay(v: View) {
        overlay.removeAllViews()
        overlay.addView(v, FrameLayout.LayoutParams(mp, mp))
        overlay.visibility = View.VISIBLE
    }

    private fun closeOverlay() {
        overlay.visibility = View.GONE
        overlay.removeAllViews()
    }

    // ---------- URL helpers ----------

    private fun normalize(q: String): String {
        val t = q.trim()
        return when {
            t.startsWith("http://") || t.startsWith("https://") -> t
            !t.contains(" ") && t.contains(".") -> "https://$t"
            else -> engines[engineIdx].second + Uri.encode(t)
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
        val searchBase = engines[engineIdx].second
        return """
<!DOCTYPE html><html><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>Vortex</title>
<style>
*{box-sizing:border-box;margin:0;padding:0;-webkit-tap-highlight-color:transparent}
body{font-family:sans-serif;color:#fff;background:radial-gradient(ellipse at 50% -10%,rgba(124,92,255,.20),transparent 60%),linear-gradient(180deg,#0d0d16,#06060a);background-attachment:fixed;display:flex;flex-direction:column;align-items:center;padding:48px 18px 32px;min-height:100vh}
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
    location.href='$searchBase'+encodeURIComponent(v);
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
                if (h == null) toast("Open a site first")
                else showRuleEditor(h)
            }
            .setNegativeButton("Close", null)
            .show()
    }

    // ---------- Toolbox actions ----------

    private fun toggleAdblock() {
        adblock = !adblock
        tabs[cur].reload()
    }

    private fun startIncognito() {
        newTab("home", true)
    }

    private fun translatePage() {
        val u = tabs[cur].url
        if (u == null || shown(u).isEmpty()) {
            toast("Open a site first")
            return
        }
        tabs[cur].loadUrl("https://translate.google.com/translate?sl=auto&tl=" + translateTo + "&u=" + Uri.encode(u))
    }

    private fun screenshot() {
        val w = tabs[cur]
        if (w.width <= 0 || w.height <= 0) return
        try {
            val bmp = Bitmap.createBitmap(w.width, w.height, Bitmap.Config.ARGB_8888)
            w.draw(Canvas(bmp))
            val cv = ContentValues()
            cv.put(MediaStore.Images.Media.DISPLAY_NAME, "Vortex_" + System.currentTimeMillis() + ".png")
            cv.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= 29) {
                cv.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Vortex")
            }
            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv)
            if (uri == null) {
                toast("Screenshot save failed")
                return
            }
            val os = contentResolver.openOutputStream(uri)
            if (os != null) {
                bmp.compress(Bitmap.CompressFormat.PNG, 100, os)
                os.close()
            }
            toast("Saved: Pictures/Vortex")
        } catch (e: Exception) {
            toast("Screenshot save failed")
        }
    }

    private fun toggleDesktop() {
        desktop = !desktop
        tabs[cur].settings.userAgentString =
            if (desktop) desktopUa else WebSettings.getDefaultUserAgent(this)
        tabs[cur].reload()
    }

    private fun openDownloads() {
        try {
            startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS))
        } catch (e: Exception) {
            toast("Downloads app not found")
        }
    }

    private fun sharePage() {
        val u = tabs[cur].url
        if (u == null || shown(u).isEmpty()) {
            toast("Open a site first")
            return
        }
        val i = Intent(Intent.ACTION_SEND)
        i.type = "text/plain"
        i.putExtra(Intent.EXTRA_TEXT, u)
        startActivity(Intent.createChooser(i, "Share"))
    }

    private fun showDnsInfo() {
        AlertDialog.Builder(this)
            .setTitle("Private DNS")
            .setMessage(
                "Android e app theke DNS set kora jay na. Settings e giye Private DNS on kor, " +
                    "hostname dis: dns.adguard-dns.com (ad block DNS) ba one.one.one.one (Cloudflare)."
            )
            .setPositiveButton("Open settings") { _, _ ->
                try {
                    startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
                } catch (e: Exception) {
                    toast("Settings open hoy ni")
                }
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun toggleDark() {
        darkPage = !darkPage
        inject(tabs[cur], tabs[cur].url)
    }

    private fun editSiteRule() {
        val h = currentRuleHost()
        if (h == null) toast("Open a site first")
        else showRuleEditor(h)
    }

    private fun cycleEngine() {
        engineIdx = (engineIdx + 1) % engines.size
        prefs.edit().putInt("engine", engineIdx).apply()
        toast("Search engine: " + engines[engineIdx].first)
    }

    private fun goForward() {
        if (tabs[cur].canGoForward()) tabs[cur].goForward() else toast("No forward page")
    }

    private fun reloadPage() {
        tabs[cur].reload()
    }

    // ---------- Toolbox page ----------

    private fun showToolbox() {
        val sv = ScrollView(this)
        sv.setBackgroundColor(cBg)
        val col = LinearLayout(this)
        col.orientation = LinearLayout.VERTICAL
        col.setPadding(dp(14), dp(28), dp(14), dp(28))

        val head = LinearLayout(this)
        head.orientation = LinearLayout.HORIZONTAL
        head.gravity = Gravity.CENTER_VERTICAL
        head.setPadding(dp(6), 0, dp(6), dp(18))

        val logoId = resources.getIdentifier("logo", "drawable", packageName)
        if (logoId != 0) {
            val iv = ImageView(this)
            iv.setImageResource(logoId)
            head.addView(iv, lp(dp(56), dp(56)))
        }

        val titles = LinearLayout(this)
        titles.orientation = LinearLayout.VERTICAL
        titles.setPadding(dp(12), 0, 0, 0)
        val nameRow = LinearLayout(this)
        nameRow.orientation = LinearLayout.HORIZONTAL
        val n1 = TextView(this)
        n1.text = "Vortex"
        n1.textSize = 21f
        n1.setTextColor(cAccent)
        val n2 = TextView(this)
        n2.text = " Browser"
        n2.textSize = 21f
        n2.setTextColor(Color.WHITE)
        nameRow.addView(n1)
        nameRow.addView(n2)
        val subT = TextView(this)
        subT.text = "Toolbox"
        subT.textSize = 13f
        subT.setTextColor(cGray)
        titles.addView(nameRow)
        titles.addView(subT)

        val close = IconView(this, 7)
        close.setOnClickListener { closeOverlay() }

        head.addView(titles, lp(0, wc, 1f))
        head.addView(close, lp(dp(44), dp(44)))
        col.addView(head)

        val tools = listOf(
            Tool(10, "Ad Blocker", "Block ads: " + onOff(adblock)) { toggleAdblock() },
            Tool(11, "Incognito Mode", "Browse privately") { startIncognito() },
            Tool(12, "Translator", "Translate this page") { translatePage() },
            Tool(13, "Screenshot", "Capture screen") { screenshot() },
            Tool(14, "Desktop Mode", "Desktop site: " + onOff(desktop)) { toggleDesktop() },
            Tool(15, "Download History", "Check your downloads") { openDownloads() },
            Tool(16, "Share Page", "Share current page") { sharePage() },
            Tool(17, "Private DNS", "Use custom DNS") { showDnsInfo() },
            Tool(18, "Dark Page", "Force dark: " + onOff(darkPage)) { toggleDark() },
            Tool(19, "Site Injector", "Custom CSS/JS for this site") { editSiteRule() },
            Tool(20, "Site Rules", "Manage all rules") { showRulesList() },
            Tool(6, "Search Engine", engines[engineIdx].first) { cycleEngine() },
            Tool(1, "Forward", "Go forward") { goForward() },
            Tool(21, "Reload", "Reload page") { reloadPage() }
        )

        for (pair in tools.chunked(2)) {
            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL
            for (tool in pair) row.addView(tile(tool), cellLp())
            if (pair.size == 1) row.addView(View(this), cellLp())
            col.addView(row)
        }

        val foot = TextView(this)
        foot.text = "Vortex Tools"
        foot.gravity = Gravity.CENTER
        foot.textSize = 15f
        foot.setTextColor(cAccent)
        val fb = GradientDrawable()
        fb.setColor(Color.parseColor("#14102A"))
        fb.cornerRadius = dp(28).toFloat()
        fb.setStroke(dp(1), cStroke)
        foot.background = fb
        val fl = lp(mp, dp(52))
        fl.setMargins(dp(6), dp(18), dp(6), 0)
        col.addView(foot, fl)

        sv.addView(col)
        openOverlay(sv)
    }

    private fun tile(tool: Tool): View {
        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setPadding(dp(16), dp(14), dp(12), dp(14))
        val bg = GradientDrawable()
        bg.setColor(cCard)
        bg.cornerRadius = dp(20).toFloat()
        bg.setStroke(dp(1), cStroke)
        card.background = bg

        val top = LinearLayout(this)
        top.orientation = LinearLayout.HORIZONTAL
        top.gravity = Gravity.CENTER_VERTICAL
        val ic = IconView(this, tool.kind)
        ic.tint = cAccent
        ic.ratio = 0.6f
        ic.isClickable = false
        val gap = View(this)
        val ch = IconView(this, 8)
        ch.tint = cAccent
        ch.isClickable = false
        top.addView(ic, lp(dp(52), dp(52)))
        top.addView(gap, lp(0, 1, 1f))
        top.addView(ch, lp(dp(28), dp(28)))

        val t1 = TextView(this)
        t1.text = tool.title
        t1.textSize = 16f
        t1.setTextColor(Color.WHITE)
        val t2 = TextView(this)
        t2.text = tool.sub
        t2.textSize = 12f
        t2.setTextColor(cGray)

        card.addView(top)
        val tl = lp(mp, wc)
        tl.topMargin = dp(8)
        card.addView(t1, tl)
        card.addView(t2)
        card.setOnClickListener {
            closeOverlay()
            tool.action()
        }
        return card
    }

    // ---------- Tabs page ----------

    private fun captureThumb(w: WebView) {
        if (w.width <= 0 || w.height <= 0) return
        try {
            val sc = 0.4f
            val bmp = Bitmap.createBitmap((w.width * sc).toInt(), (w.height * sc).toInt(), Bitmap.Config.ARGB_8888)
            val c = Canvas(bmp)
            c.scale(sc, sc)
            w.draw(c)
            thumbs[w] = bmp
        } catch (e: Exception) {
        }
    }

    private fun showTabsPage() {
        tabs.getOrNull(cur)?.let { captureThumb(it) }
        val page = LinearLayout(this)
        page.orientation = LinearLayout.VERTICAL
        page.setBackgroundColor(cBg)

        val head = LinearLayout(this)
        head.orientation = LinearLayout.HORIZONTAL
        head.gravity = Gravity.CENTER_VERTICAL
        head.setPadding(dp(18), dp(24), dp(10), dp(10))
        val title = TextView(this)
        title.text = "Tabs (" + tabs.size + ")"
        title.textSize = 20f
        title.setTextColor(Color.WHITE)
        val add = IconView(this, 9)
        add.tint = cAccent
        add.setOnClickListener {
            closeOverlay()
            newTab("home")
        }
        val close = IconView(this, 7)
        close.setOnClickListener { closeOverlay() }
        head.addView(title, lp(0, wc, 1f))
        head.addView(add, lp(dp(44), dp(44)))
        head.addView(close, lp(dp(44), dp(44)))

        val sv = ScrollView(this)
        val col = LinearLayout(this)
        col.orientation = LinearLayout.VERTICAL
        col.setPadding(dp(10), dp(4), dp(10), dp(24))
        for (pair in tabs.indices.toList().chunked(2)) {
            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL
            for (i in pair) row.addView(tabCard(i), cellLp())
            if (pair.size == 1) row.addView(View(this), cellLp())
            col.addView(row)
        }
        sv.addView(col)

        page.addView(head)
        page.addView(sv, lp(mp, 0, 1f))
        openOverlay(page)
    }

    private fun tabCard(i: Int): View {
        val w = tabs[i]
        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setPadding(dp(8), dp(8), dp(8), dp(8))
        val bg = GradientDrawable()
        bg.setColor(cCard)
        bg.cornerRadius = dp(16).toFloat()
        bg.setStroke(dp(if (i == cur) 2 else 1), if (i == cur) cAccent else cStroke)
        card.background = bg

        val raw = w.title
        val host = hostOf(w.url)
        var name = if (shown(w.url).isEmpty()) "Home" else if (!raw.isNullOrBlank()) raw else (host ?: "Tab")
        if (incog.contains(w)) name = "Incognito: " + name

        val head = LinearLayout(this)
        head.orientation = LinearLayout.HORIZONTAL
        head.gravity = Gravity.CENTER_VERTICAL
        val tv = TextView(this)
        tv.text = name
        tv.textSize = 12f
        tv.setTextColor(Color.WHITE)
        tv.maxLines = 1
        tv.ellipsize = TextUtils.TruncateAt.END
        tv.setPadding(dp(4), 0, 0, 0)
        val x = IconView(this, 7)
        x.ratio = 0.5f
        x.setOnClickListener {
            closeTabAt(i)
            showTabsPage()
        }
        head.addView(tv, lp(0, wc, 1f))
        head.addView(x, lp(dp(32), dp(32)))

        val iv = ImageView(this)
        iv.scaleType = ImageView.ScaleType.CENTER_CROP
        iv.setBackgroundColor(cBg)
        val th = thumbs[w]
        if (th != null) iv.setImageBitmap(th)

        card.addView(head)
        card.addView(iv, lp(mp, dp(150)))
        card.setOnClickListener {
            closeOverlay()
            switchTo(i)
        }
        return card
    }

    // ---------- Tabs ----------

    @SuppressLint("SetJavaScriptEnabled")
    private fun newTab(url: String, incognito: Boolean = false) {
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
        if (incognito) {
            incog.add(w)
            s.cacheMode = WebSettings.LOAD_NO_CACHE
            s.saveFormData = false
        }

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
                    updateToolbar(url)
                }
            }

            override fun onPageFinished(view: WebView, url: String) {
                inject(view, url)
                if (view === tabs.getOrNull(cur)) updateToolbar(url)
            }

            override fun doUpdateVisitedHistory(view: WebView, url: String, isReload: Boolean) {
                if (view === tabs.getOrNull(cur)) updateToolbar(url)
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
            toast("Downloading...")
        }

        tabs.add(w)
        switchTo(tabs.size - 1)
        if (url == "home") showHome(w) else w.loadUrl(normalize(url))
    }

    private fun switchTo(i: Int) {
        tabs.getOrNull(cur)?.let { captureThumb(it) }
        container.removeAllViews()
        cur = i
        container.addView(tabs[i], FrameLayout.LayoutParams(mp, mp))
        urlBar.setText(shown(tabs[i].url))
        tabsBtn.count = tabs.size
        tabsBtn.invalidate()
        updateToolbar(tabs[i].url)
    }

    private fun wipeIfIncognito(w: WebView) {
        if (incog.remove(w)) {
            w.clearCache(true)
            w.clearHistory()
            w.clearFormData()
        }
    }

    private fun closeTabAt(i: Int) {
        if (tabs.size == 1) {
            val old = tabs[0]
            container.removeAllViews()
            tabs.clear()
            wipeIfIncognito(old)
            thumbs.remove(old)
            old.destroy()
            cur = -1
            newTab("home")
            return
        }
        val wasCur = (i == cur)
        val w = tabs[i]
        if (wasCur) container.removeAllViews()
        tabs.removeAt(i)
        wipeIfIncognito(w)
        thumbs.remove(w)
        w.destroy()
        if (wasCur) {
            switchTo(minOf(i, tabs.size - 1))
        } else {
            if (i < cur) cur--
            tabsBtn.count = tabs.size
            tabsBtn.invalidate()
        }
    }
}
