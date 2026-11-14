package com.vortex.browser

import android.Manifest
import android.content.pm.PackageManager
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.annotation.TargetApi
import android.app.PictureInPictureParams
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.content.res.Configuration
import android.graphics.drawable.Icon
import android.util.Rational
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
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Xml
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.*
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DownloadItem(val name: String, val url: String, val path: String, val ms: Long)
class Rule(var host: String, var css: String, var js: String, var on: Boolean)
class Tool(val kind: Int, val title: String, val sub: String, val action: () -> Unit)
class HistItem(val t: String, val u: String, val ms: Long)

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

    override fun setPressed(pressed: Boolean) {
        super.setPressed(pressed)
        val s = if (pressed) 0.84f else 1f
        animate().scaleX(s).scaleY(s).setDuration(90).start()
    }

    override fun onDraw(c: Canvas) {
        val size = minOf(width, height) * ratio
        val a = if (isPressed) 110 else 255
        p.color = tint; p.alpha = a; t.color = tint; t.alpha = a
        c.save()
        c.translate(width / 2f - size / 2f, height / 2f - size / 2f)
        c.scale(size / 24f, size / 24f)
        p.strokeWidth = 2f
        path.reset()
        when (kind) {
            0 -> { path.moveTo(19f, 12f); path.lineTo(5f, 12f); path.moveTo(12f, 5f); path.lineTo(5f, 12f); path.lineTo(12f, 19f); c.drawPath(path, p) }
            2 -> { path.moveTo(3f, 11f); path.lineTo(12f, 3f); path.lineTo(21f, 11f); path.moveTo(5f, 9.5f); path.lineTo(5f, 21f); path.lineTo(19f, 21f); path.lineTo(19f, 9.5f); path.moveTo(10f, 21f); path.lineTo(10f, 15f); path.lineTo(14f, 15f); path.lineTo(14f, 21f); c.drawPath(path, p) }
            3 -> { c.drawRoundRect(4f, 4f, 20f, 20f, 5f, 5f, p); t.textSize = 10f; c.drawText(count.toString(), 12f, 15.5f, t) }
            4 -> { c.drawCircle(12f, 5f, 1.9f, t); c.drawCircle(12f, 12f, 1.9f, t); c.drawCircle(12f, 19f, 1.9f, t) }
            5 -> { c.drawRoundRect(9f, 2f, 15f, 14f, 3f, 3f, p); rect.set(5f, 5f, 19f, 19f); c.drawArc(rect, 0f, 180f, false, p); c.drawLine(12f, 19f, 12f, 22f, p); c.drawLine(8f, 22f, 16f, 22f, p) }
            6 -> { c.drawCircle(10.5f, 10.5f, 6.5f, p); c.drawLine(15.5f, 15.5f, 21f, 21f, p) }
            7 -> { c.drawLine(6f, 6f, 18f, 18f, p); c.drawLine(18f, 6f, 6f, 18f, p) }
            8 -> { path.moveTo(9f, 5f); path.lineTo(16f, 12f); path.lineTo(9f, 19f); c.drawPath(path, p) }
            9 -> { c.drawLine(12f, 5f, 12f, 19f, p); c.drawLine(5f, 12f, 19f, 12f, p) }
            10 -> { path.moveTo(12f, 3f); path.lineTo(20f, 6f); path.lineTo(20f, 12f); path.quadTo(20f, 18f, 12f, 21f); path.quadTo(4f, 18f, 4f, 12f); path.lineTo(4f, 6f); path.close(); c.drawPath(path, p); c.drawLine(9f, 9f, 15f, 15f, p) }
            11 -> { path.moveTo(5f, 11f); path.lineTo(8f, 4f); path.lineTo(16f, 4f); path.lineTo(19f, 11f); c.drawPath(path, p); c.drawLine(3f, 11f, 21f, 11f, p); c.drawCircle(8f, 16.5f, 2.6f, p); c.drawCircle(16f, 16.5f, 2.6f, p); c.drawLine(10.6f, 16f, 13.4f, 16f, p) }
            13 -> { path.moveTo(4f, 9f); path.lineTo(4f, 4f); path.lineTo(9f, 4f); path.moveTo(15f, 4f); path.lineTo(20f, 4f); path.lineTo(20f, 9f); path.moveTo(20f, 15f); path.lineTo(20f, 20f); path.lineTo(15f, 20f); path.moveTo(9f, 20f); path.lineTo(4f, 20f); path.lineTo(4f, 15f); c.drawPath(path, p) }
            14 -> { c.drawRoundRect(3f, 4f, 21f, 16f, 2f, 2f, p); c.drawLine(12f, 16f, 12f, 20f, p); c.drawLine(8f, 20f, 16f, 20f, p) }
            15 -> { c.drawLine(12f, 4f, 12f, 15f, p); path.moveTo(7f, 10f); path.lineTo(12f, 15f); path.lineTo(17f, 10f); c.drawPath(path, p); c.drawLine(5f, 19f, 19f, 19f, p) }
            16 -> { c.drawCircle(6f, 12f, 2.4f, p); c.drawCircle(18f, 6f, 2.4f, p); c.drawCircle(18f, 18f, 2.4f, p); c.drawLine(8.2f, 11f, 15.8f, 7f, p); c.drawLine(8.2f, 13f, 15.8f, 17f, p) }
            19 -> { path.moveTo(8f, 7f); path.lineTo(3f, 12f); path.lineTo(8f, 17f); path.moveTo(16f, 7f); path.lineTo(21f, 12f); path.lineTo(16f, 17f); c.drawPath(path, p); c.drawLine(14f, 5f, 10f, 19f, p) }
            21 -> { rect.set(4.5f, 4.5f, 19.5f, 19.5f); p.strokeWidth = 2f; c.drawArc(rect, 0f, 310f, false, p); path.moveTo(15f, 16.5f); path.lineTo(19.5f, 14.5f); path.lineTo(17.5f, 10f); c.drawPath(path, p) }
            22 -> { path.moveTo(17f, 17f); path.lineTo(7f, 7f); path.moveTo(7f, 15f); path.lineTo(7f, 7f); path.lineTo(15f, 7f); c.drawPath(path, p) }
            23 -> { c.drawCircle(12f, 12f, 9f, p); path.moveTo(12f, 7f); path.lineTo(12f, 12f); path.lineTo(15.5f, 14f); c.drawPath(path, p) }
            24 -> { c.drawLine(4f, 7f, 20f, 7f, p); path.moveTo(9f, 7f); path.lineTo(9f, 4f); path.lineTo(15f, 4f); path.lineTo(15f, 7f); path.moveTo(6f, 7f); path.lineTo(7f, 20f); path.lineTo(17f, 20f); path.lineTo(18f, 7f); path.moveTo(10f, 11f); path.lineTo(10f, 16f); path.moveTo(14f, 11f); path.lineTo(14f, 16f); c.drawPath(path, p) }
            25 -> { c.drawCircle(12f, 12f, 9f, p); t.textSize = 13f; c.drawText("?", 12f, 16.4f, t) }
            26 -> { c.drawRoundRect(5f, 3f, 19f, 21f, 3f, 3f, p); c.drawLine(12f, 8.5f, 12f, 15.5f, p); c.drawLine(8.5f, 12f, 15.5f, 12f, p) }
        }
        c.restore()
    }
}

class PullLayout(ctx: Context) : FrameLayout(ctx) {
    var canPull: () -> Boolean = { false }
    var onRefresh: () -> Unit = {}
    private val density = ctx.resources.displayMetrics.density
    private val ind = IconView(ctx, 21)
    private val slop = ViewConfiguration.get(ctx).scaledTouchSlop
    private val maxPull = 110f * density
    private val trigger = 70f * density
    private var startX = 0f
    private var startY = 0f
    private var atTop = false
    private var pulling = false
    private var drag = 0f
    private var refreshing = false
    private var spin: ObjectAnimator? = null

    init {
        val bg = GradientDrawable()
        bg.shape = GradientDrawable.OVAL
        bg.setColor(Color.parseColor("#2A2150"))
        bg.setStroke((1 * density).toInt(), Color.parseColor("#55FFFFFF"))
        ind.background = bg; ind.tint = Color.parseColor("#B9A8FF"); ind.isClickable = false; ind.alpha = 0f; ind.translationY = -60f * density
    }

    fun attach() {
        val l = FrameLayout.LayoutParams((40 * density).toInt(), (40 * density).toInt())
        l.gravity = Gravity.CENTER_HORIZONTAL
        addView(ind, l)
    }

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        if (refreshing) return false
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> { startX = e.x; startY = e.y; pulling = false }
            MotionEvent.ACTION_MOVE -> {
                val dy = e.y - startY
                val dx = e.x - startX
                if (!pulling && !canPull()) { startX = e.x; startY = e.y }
                else if (!pulling && dy > slop && dy > Math.abs(dx) * 1.5f) pulling = true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> pulling = false
        }
        return pulling
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (!pulling) return super.onTouchEvent(e)
        when (e.actionMasked) {
            MotionEvent.ACTION_MOVE -> {
                drag = ((e.y - startY) * 0.5f).coerceIn(0f, maxPull)
                ind.alpha = (drag / trigger).coerceIn(0f, 1f)
                ind.translationY = -44f * density + drag
                ind.rotation = drag * 4f
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val fire = e.actionMasked == MotionEvent.ACTION_UP && drag >= trigger
                pulling = false
                if (fire) startRefresh() else hide()
                drag = 0f
            }
        }
        return true
    }

    private fun startRefresh() {
        refreshing = true
        ind.animate().translationY(24f * density).alpha(1f).setDuration(150).start()
        val a = ObjectAnimator.ofFloat(ind, View.ROTATION, ind.rotation, ind.rotation + 360f)
        a.duration = 700; a.repeatCount = ValueAnimator.INFINITE; a.interpolator = LinearInterpolator(); a.start()
        spin = a
        onRefresh()
        postDelayed({ finish() }, 6000L)
    }

    fun finish() {
        if (!refreshing) return
        refreshing = false
        spin?.cancel()
        hide()
    }

    private fun hide() {
        ind.animate().translationY(-60f * density).alpha(0f).setDuration(220).start()
    }
}

class MainActivity : Activity() {

    private val tabs = mutableListOf<WebView>()
    private val incog = HashSet<WebView>()
    private val thumbs = HashMap<WebView, Bitmap>()
    private var cur = -1
    private lateinit var pull: PullLayout
    private lateinit var container: FrameLayout
    private lateinit var overlay: FrameLayout
    private lateinit var urlBar: TextView
    private lateinit var progress: ProgressBar
    private lateinit var pill: LinearLayout
    private lateinit var pillBg: GradientDrawable
    private lateinit var spacer: View
    private lateinit var homeBtn: IconView
    private lateinit var plusBtn: IconView
    private lateinit var tabsBtn: IconView
    private lateinit var prefs: SharedPreferences

    private val rules = mutableListOf<Rule>()
    private val webHist = mutableListOf<HistItem>()
    private val searchHist = mutableListOf<String>()
    private val downloadHist = mutableListOf<DownloadItem>()
    private val dnsCache = java.util.concurrent.ConcurrentHashMap<String, Boolean>()

    @Volatile private var suggestToken = 0
    @Volatile private var feedBusy = false
    private var forceFeed = false
    private var overlayGen = 0
    private var pendingPerm: PermissionRequest? = null
    private var pendingGeoOrigin: String? = null
    private var pendingGeoCb: GeolocationPermissions.Callback? = null
    private var fileCallback: ValueCallback<Array<Uri>>? = null

    private val playingTabs = HashSet<WebView>()
    private val videoTabs = HashSet<WebView>()
    private val favicons = HashMap<WebView, Pair<String?, Bitmap>>()
    private lateinit var chromeViews: List<View>
    private var pendingDl: (() -> Unit)? = null
    private var hideFs: (() -> Unit)? = null
    private var keepAlive = false
    private var inPip = false
    private var vidW = 16
    private var vidH = 9
    @Volatile private var dohDownUntil = 0L
    private val ui = android.os.Handler(android.os.Looper.getMainLooper())
    private val stopKeep = Runnable { if (playingTabs.isEmpty()) stopKeepAlive() }

    private var adblock = true
    private var desktop = false
    private var engineIdx = 0
    private var feedIdx = 0
    private val showGreeting = true

    private val feedbackUrl = "https://www.facebook.com/share/1CSSRcHBTv/"
    private val homeUrl = "https://vortex.local/"
    private val dohUrl = "https://dns.adguard-dns.com/dns-query"

    private val engines = listOf(
        Pair("Google", "https://www.google.com/search?q="),
        Pair("DuckDuckGo", "https://duckduckgo.com/?q="),
        Pair("Bing", "https://www.bing.com/search?q=")
    )

    private val feeds = listOf(
        Pair("Bangla", "https://news.google.com/rss?hl=bn&gl=BD&ceid=BD:bn"),
        Pair("English", "https://news.google.com/rss?hl=en-BD&gl=BD&ceid=BD:en")
    )

    private val cBg = Color.parseColor("#07070C")
    private val cGray = Color.parseColor("#9A9AB5")
    private val cAccent = Color.parseColor("#9A86FF")

    private val mp = ViewGroup.LayoutParams.MATCH_PARENT
    private val wc = ViewGroup.LayoutParams.WRAP_CONTENT

    private val blocked = listOf(
        "doubleclick.net", "googlesyndication.com", "googleadservices.com",
        "adservice.google.com", "adnxs.com", "taboola.com", "outbrain.com",
        "popads.net", "propellerads.com", "adsterra.com", "exoclick.com"
    )
    private val desktopUa = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("vortex", MODE_PRIVATE)
        engineIdx = prefs.getInt("engine", 0).coerceIn(0, engines.size - 1)
        feedIdx = prefs.getInt("feed", 0).coerceIn(0, feeds.size - 1)
        loadRules()
        loadHistory()
        loadDownloads()
        window.statusBarColor = Color.parseColor("#120A2B")
        window.navigationBarColor = Color.parseColor("#0E0824")
        window.setBackgroundDrawable(bgGradient())
        buildUi()
        newTab(intent?.data?.toString() ?: "home")
        if (Build.VERSION.SDK_INT >= 33 && !prefs.getBoolean("notif_asked", false)) {
            prefs.edit().putBoolean("notif_asked", true).apply()
            if (!hasPerm("android.permission.POST_NOTIFICATIONS")) requestPermissions(arrayOf("android.permission.POST_NOTIFICATIONS"), 94)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        hideFs?.let { it(); return }
        if (overlay.visibility == View.VISIBLE) {
            closeOverlay()
            return
        }
        val w = tabs.getOrNull(cur)
        if (w != null && w.canGoBack()) w.goBack()
        else if (tabs.size > 1) closeTabAt(cur)
        else if (videoPlaying() && enterPip()) { }
        else finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 77 && resultCode == RESULT_OK) {
            val list = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val text = list?.firstOrNull()
            if (!text.isNullOrBlank()) submitQuery(text)
        }
        if (requestCode == 78) {
            val cb = fileCallback
            fileCallback = null
            cb?.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data))
        }
    }

    // ===================== lifecycle / PiP / background media =====================

    override fun onDestroy() {
        ui.removeCallbacks(stopKeep)
        stopKeepAlive()
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val d = intent?.data
        if (d != null) newTab(d.toString())
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // Android 12+ uses setAutoEnterEnabled (see pipParams); older versions enter here
        if (Build.VERSION.SDK_INT in 26..30 && videoPlaying()) enterPip()
    }

    @TargetApi(26)
    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        inPip = isInPictureInPictureMode
        if (inPip && overlay.visibility == View.VISIBLE) {
            overlayGen++; overlay.visibility = View.GONE; overlay.removeAllViews()
        }
        val vis = if (inPip) View.GONE else View.VISIBLE
        for (v in chromeViews) v.visibility = vis
        if (!inPip) progress.visibility = View.GONE
        if (hideFs == null) {
            tabs.getOrNull(cur)?.evaluateJavascript("window.__vxPip&&window.__vxPip(" + inPip + ")", null)
        }
    }

    private fun videoPlaying(): Boolean {
        val w = tabs.getOrNull(cur)
        return w != null && videoTabs.contains(w)
    }

    @TargetApi(26)
    private fun pipParams(): PictureInPictureParams {
        val b = PictureInPictureParams.Builder().setAspectRatio(Rational(vidW, vidH))
        if (Build.VERSION.SDK_INT >= 31) b.setAutoEnterEnabled(videoPlaying())
        return b.build()
    }

    private fun updatePipParams() {
        if (Build.VERSION.SDK_INT < 26) return
        try { setPictureInPictureParams(pipParams()) } catch (e: Exception) {}
    }

    @TargetApi(26)
    private fun enterPip(): Boolean {
        if (Build.VERSION.SDK_INT < 26 || inPip) return false
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) return false
        return try { enterPictureInPictureMode(pipParams()) } catch (e: Exception) { false }
    }

    inner class MediaBridge(private val w: WebView) {
        @JavascriptInterface
        fun state(any: Boolean, video: Boolean, vw: Int, vh: Int) {
            runOnUiThread { onMediaState(w, any, video, vw, vh) }
        }
    }

    private fun onMediaState(w: WebView, any: Boolean, video: Boolean, vw: Int, vh: Int) {
        if (!tabs.contains(w)) return
        if (any) playingTabs.add(w) else playingTabs.remove(w)
        if (video) videoTabs.add(w) else videoTabs.remove(w)
        if (video && vw > 0 && vh > 0) {
            val r = vw.toFloat() / vh
            if (r in 0.45f..2.3f) { vidW = vw; vidH = vh }
        }
        ui.removeCallbacks(stopKeep)
        if (playingTabs.isNotEmpty()) startKeepAlive() else ui.postDelayed(stopKeep, 45000L)
        updatePipParams()
    }

    private fun startKeepAlive() {
        if (keepAlive) return
        try {
            MediaKeepAliveService.onStopAction = { pauseAllMedia() }
            val i = Intent(this, MediaKeepAliveService::class.java)
            if (Build.VERSION.SDK_INT >= 26) startForegroundService(i) else startService(i)
            keepAlive = true
        } catch (e: Exception) {}
    }

    private fun stopKeepAlive() {
        if (!keepAlive) return
        keepAlive = false
        MediaKeepAliveService.onStopAction = null
        try { stopService(Intent(this, MediaKeepAliveService::class.java)) } catch (e: Exception) {}
    }

    private fun pauseAllMedia() {
        for (t in tabs) {
            t.evaluateJavascript("document.querySelectorAll('video,audio').forEach(function(m){try{m.pause()}catch(e){}})", null)
        }
    }

    private val mediaJs = """
(function(){
  if(window.__vxMedia) return; window.__vxMedia=1;
  try{
    var D=document;
    ['hidden','webkitHidden'].forEach(function(k){Object.defineProperty(D,k,{get:function(){return false},configurable:true});});
    ['visibilityState','webkitVisibilityState'].forEach(function(k){Object.defineProperty(D,k,{get:function(){return 'visible'},configurable:true});});
    var stop=function(e){e.stopImmediatePropagation();};
    D.addEventListener('visibilitychange',stop,true);
    D.addEventListener('webkitvisibilitychange',stop,true);
  }catch(e){}
  var last='';
  function rep(){
    try{
      var els=document.querySelectorAll('video,audio'),any=false,vid=false,w=0,h=0;
      for(var i=0;i<els.length;i++){
        var m=els[i];
        if(!m.paused&&!m.ended&&!m.muted&&m.volume>0&&m.readyState>2){
          any=true;
          if(m.tagName==='VIDEO'&&m.videoWidth>0){vid=true;w=m.videoWidth;h=m.videoHeight;}
        }
      }
      var k=any+','+vid+','+w+','+h;
      if(k!==last){last=k;VortexMedia.state(any,vid,w,h);}
    }catch(e){}
  }
  ['play','playing','pause','ended','emptied','volumechange','loadeddata'].forEach(function(n){document.addEventListener(n,rep,true);});
  setInterval(rep,2000);
  window.__vxPip=function(on){
    try{
      var old=document.getElementById('__vxpipcss'); if(old) old.remove();
      document.querySelectorAll('[data-vxp]').forEach(function(e){e.removeAttribute('data-vxp');});
      if(!on) return;
      var vs=document.querySelectorAll('video'),v=null;
      for(var i=0;i<vs.length;i++){ if(!vs[i].paused){v=vs[i];break;} }
      if(!v) return;
      var e=v; while(e&&e!==document.documentElement){ e.setAttribute('data-vxp','1'); e=e.parentElement; }
      v.setAttribute('data-vxp','v');
      var s=document.createElement('style'); s.id='__vxpipcss';
      s.textContent='body *:not([data-vxp]){visibility:hidden!important}[data-vxp]{transform:none!important;filter:none!important;contain:none!important;overflow:visible!important;clip-path:none!important}video[data-vxp="v"]{position:fixed!important;left:0!important;top:0!important;width:100vw!important;height:100vh!important;max-width:none!important;max-height:none!important;z-index:2147483647!important;background:#000!important;object-fit:contain!important;visibility:visible!important}';
      document.documentElement.appendChild(s);
    }catch(e){}
  };
})();
"""

    private fun injectMedia(w: WebView) {
        val u = w.url ?: return
        if (!(u.startsWith("http://") || u.startsWith("https://"))) return
        w.evaluateJavascript(mediaJs, null)
    }

    // ===================== downloads =====================

    private fun smartFileName(url: String, cd: String?, mime: String?): String {
        var name: String? = null
        if (!cd.isNullOrBlank()) {
            val star = Regex("filename\\*\\s*=\\s*[^']*'[^']*'([^;]+)", RegexOption.IGNORE_CASE).find(cd)
            val plain = Regex("filename\\s*=\\s*\"?([^\";]+)\"?", RegexOption.IGNORE_CASE).find(cd)
            name = star?.groupValues?.get(1)?.trim()?.let { Uri.decode(it.trim('"')) }
                ?: plain?.groupValues?.get(1)?.trim()
        }
        if (name.isNullOrBlank()) {
            val uri = Uri.parse(url)
            val seg = uri.lastPathSegment?.let { Uri.decode(it) }
            name = if (seg != null && seg.contains('.')) seg else null
            if (name == null) {
                for (k in listOf("filename", "file", "name", "fn", "title")) {
                    val v = uri.getQueryParameter(k)
                    if (!v.isNullOrBlank() && v.contains('.')) { name = v; break }
                }
            }
        }
        val raw = (if (name.isNullOrBlank()) null else name) ?: URLUtil.guessFileName(url, cd, mime)
        var n = raw.replace(Regex("[\\\\/:*?\"<>|]"), "_").trim()
        if (n.isEmpty()) n = "download"

        val dot = n.lastIndexOf('.')
        val ext = if (dot > 0 && dot < n.length - 1) n.substring(dot + 1).lowercase() else ""
        if (ext.isEmpty() || ext == "bin") {
            val base = if (ext == "bin") n.substring(0, dot) else n
            var newExt: String? = null
            val lu = url.lowercase()
            val m = mime?.substringBefore(';')?.trim()?.lowercase() ?: ""
            if (m.contains("android.package-archive")) newExt = "apk"
            else if (lu.contains(".apk")) newExt = "apk"
            else if (m.isNotEmpty() && m != "application/octet-stream" && m != "binary/octet-stream") {
                newExt = MimeTypeMap.getSingleton().getExtensionFromMimeType(m)
            }
            if (newExt != null) n = base + "." + newExt
        }
        return n
    }

    private fun mimeFor(name: String, mime: String?): String? {
        val lower = name.lowercase()
        if (lower.endsWith(".apk")) return "application/vnd.android.package-archive"
        val m = mime?.substringBefore(';')?.trim()
        if (!m.isNullOrEmpty() && m != "application/octet-stream" && m != "binary/octet-stream") return m
        val ext = lower.substringAfterLast('.', "")
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: m
    }

    private fun startDownload(dlUrl: String, ua: String?, cd: String?, mime: String?, referer: String?) {
        if (!(dlUrl.startsWith("http://") || dlUrl.startsWith("https://"))) {
            toast("This download type is not supported")
            return
        }
        if (Build.VERSION.SDK_INT <= 28 && !hasPerm(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            pendingDl = { startDownload(dlUrl, ua, cd, mime, referer) }
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 93)
            return
        }
        val name = smartFileName(dlUrl, cd, mime)
        try {
            val req = DownloadManager.Request(Uri.parse(dlUrl))
            val type = mimeFor(name, mime)
            if (type != null) req.setMimeType(type)
            if (!ua.isNullOrEmpty()) req.addRequestHeader("User-Agent", ua)
            val ck = CookieManager.getInstance().getCookie(dlUrl)
            if (!ck.isNullOrEmpty()) req.addRequestHeader("Cookie", ck)
            if (!referer.isNullOrEmpty() && referer.startsWith("http")) req.addRequestHeader("Referer", referer)
            req.setTitle(name)
            req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Vortex/" + name)
            val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(req)
            downloadHist.add(0, DownloadItem(name, dlUrl, "Downloads/Vortex/" + name, System.currentTimeMillis()))
            saveDownloads()
            toast("Downloading " + name + "...")
        } catch (e: Exception) {
            toast("Download failed, opening in another app")
            try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(dlUrl))) } catch (x: Exception) {}
        }
    }

    // ===================== install / shortcut =====================

    private fun shortcutBitmap(fav: Bitmap?, host: String): Bitmap {
        val size = 432
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(Color.parseColor("#2A2150"))
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        if (fav != null && fav.width > 0) {
            val s = 200f
            val dst = RectF((size - s) / 2f, (size - s) / 2f, (size + s) / 2f, (size + s) / 2f)
            p.isFilterBitmap = true
            c.drawBitmap(fav, null, dst, p)
        } else {
            p.color = Color.WHITE; p.textSize = 190f; p.textAlign = Paint.Align.CENTER; p.typeface = Typeface.DEFAULT_BOLD
            val letter = host.removePrefix("www.").take(1).uppercase()
            c.drawText(letter, size / 2f, size / 2f + 66f, p)
        }
        return bmp
    }

    private fun installShortcut() {
        val w = tabs.getOrNull(cur) ?: return
        val u = w.url
        if (u == null || !(u.startsWith("http://") || u.startsWith("https://"))) { toast("Open a site first"); return }
        val host = hostOf(u) ?: "site"
        val title = w.title
        val label = (if (!title.isNullOrBlank()) title else host.removePrefix("www.")).take(24)
        val fav = favicons[w]?.takeIf { it.first == host }?.second
        val icon = shortcutBitmap(fav, host)

        val launch = Intent(this, MainActivity::class.java)
        launch.action = Intent.ACTION_VIEW
        launch.data = Uri.parse(u)

        try {
            if (Build.VERSION.SDK_INT >= 26) {
                val sm = getSystemService(ShortcutManager::class.java)
                if (sm != null && sm.isRequestPinShortcutSupported) {
                    val info = ShortcutInfo.Builder(this, "vx_" + Math.abs(u.hashCode()))
                        .setShortLabel(label).setLongLabel(label)
                        .setIcon(Icon.createWithAdaptiveBitmap(icon))
                        .setIntent(launch).build()
                    sm.requestPinShortcut(info, null)
                } else {
                    toast("Your launcher does not support shortcuts")
                }
            } else {
                launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                val add = Intent("com.android.launcher.action.INSTALL_SHORTCUT")
                add.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launch)
                add.putExtra(Intent.EXTRA_SHORTCUT_NAME, label)
                add.putExtra(Intent.EXTRA_SHORTCUT_ICON, icon)
                sendBroadcast(add)
                toast("Shortcut added")
            }
        } catch (e: Exception) {
            toast("Could not create shortcut")
        }
    }

    private fun hasPerm(p: String) = checkSelfPermission(p) == PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 93) {
            val job = pendingDl
            pendingDl = null
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) job?.invoke() else toast("Storage permission denied")
        }
        if (requestCode == 91) {
            val req = pendingPerm
            pendingPerm = null
            if (req != null) {
                val allowed = ArrayList<String>()
                for (r in req.resources) {
                    if (r == PermissionRequest.RESOURCE_VIDEO_CAPTURE && hasPerm(Manifest.permission.CAMERA)) allowed.add(r)
                    if (r == PermissionRequest.RESOURCE_AUDIO_CAPTURE && hasPerm(Manifest.permission.RECORD_AUDIO)) allowed.add(r)
                }
                if (allowed.isEmpty()) req.deny() else req.grant(allowed.toTypedArray())
            }
        }
        if (requestCode == 92) {
            val cb = pendingGeoCb
            val origin = pendingGeoOrigin
            pendingGeoCb = null
            pendingGeoOrigin = null
            if (cb != null && origin != null) {
                val ok = hasPerm(Manifest.permission.ACCESS_FINE_LOCATION) || hasPerm(Manifest.permission.ACCESS_COARSE_LOCATION)
                cb.invoke(origin, ok, false)
            }
        }
    }

    private fun grantWeb(req: PermissionRequest, cam: Boolean, mic: Boolean) {
        val list = ArrayList<String>()
        if (cam) list.add(PermissionRequest.RESOURCE_VIDEO_CAPTURE)
        if (mic) list.add(PermissionRequest.RESOURCE_AUDIO_CAPTURE)
        req.grant(list.toTypedArray())
    }

    private fun handleWebPermission(req: PermissionRequest) {
        val needCam = req.resources.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE)
        val needMic = req.resources.contains(PermissionRequest.RESOURCE_AUDIO_CAPTURE)
        if (!needCam && !needMic) {
            req.deny()
            return
        }
        val what = if (needCam && needMic) "camera and microphone" else if (needCam) "camera" else "microphone"
        val host = req.origin.host ?: "This site"
        AlertDialog.Builder(this)
            .setTitle("Permission request")
            .setMessage(host + " wants to use your " + what + ".")
            .setPositiveButton("Allow") { _, _ ->
                val need = ArrayList<String>()
                if (needCam && !hasPerm(Manifest.permission.CAMERA)) need.add(Manifest.permission.CAMERA)
                if (needMic && !hasPerm(Manifest.permission.RECORD_AUDIO)) need.add(Manifest.permission.RECORD_AUDIO)
                if (need.isEmpty()) {
                    grantWeb(req, needCam, needMic)
                } else {
                    pendingPerm?.deny()
                    pendingPerm = req
                    requestPermissions(need.toTypedArray(), 91)
                }
            }
            .setNegativeButton("Block") { _, _ -> req.deny() }
            .setOnCancelListener { req.deny() }
            .show()
    }

    private fun handleGeo(origin: String, cb: GeolocationPermissions.Callback) {
        val host = hostOf(origin) ?: origin
        AlertDialog.Builder(this)
            .setTitle("Location request")
            .setMessage(host + " wants to know your location.")
            .setPositiveButton("Allow") { _, _ ->
                if (hasPerm(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    cb.invoke(origin, true, false)
                } else {
                    pendingGeoCb = cb
                    pendingGeoOrigin = origin
                    requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                        92
                    )
                }
            }
            .setNegativeButton("Block") { _, _ -> cb.invoke(origin, false, false) }
            .setOnCancelListener { cb.invoke(origin, false, false) }
            .show()
    }

    private fun loadDownloads() {
        try {
            val raw = prefs.getString("dl_hist", "[]") ?: "[]"
            val a = JSONArray(raw)
            for (i in 0 until a.length()) {
                val o = a.getJSONObject(i)
                downloadHist.add(DownloadItem(o.getString("n"), o.getString("u"), o.getString("p"), o.getLong("ms")))
            }
        } catch (e: Exception) {}
    }

    private fun saveDownloads() {
        val a = JSONArray()
        for (d in downloadHist) {
            val o = JSONObject()
            o.put("n", d.name); o.put("u", d.url); o.put("p", d.path); o.put("ms", d.ms)
            a.put(o)
        }
        prefs.edit().putString("dl_hist", a.toString()).apply()
    }

    private fun showDownloadsPage() {
        var currentFilter = 0
        val page = LinearLayout(this)
        page.orientation = LinearLayout.VERTICAL
        page.background = bgGradient()
        page.setPadding(dp(12), dp(20), dp(12), 0)

        val head = LinearLayout(this)
        head.orientation = LinearLayout.HORIZONTAL
        head.gravity = Gravity.CENTER_VERTICAL
        val title = TextView(this)
        title.text = "Downloads"; title.textSize = 21f; title.setTextColor(Color.WHITE); title.setPadding(dp(6), 0, 0, 0)

        val trash = IconView(this, 24); trash.tint = Color.parseColor("#C4C4D4")
        val close = IconView(this, 7); close.tint = Color.parseColor("#C4C4D4")
        close.setOnClickListener { closeOverlay() }

        head.addView(title, lp(0, wc, 1f))
        head.addView(trash, lp(dp(40), dp(40)))
        head.addView(close, lp(dp(40), dp(40)))

        val storageLayout = LinearLayout(this)
        storageLayout.orientation = LinearLayout.VERTICAL
        storageLayout.setPadding(dp(6), dp(10), dp(6), dp(10))
        val storageText = TextView(this)
        storageText.text = "Storage: Using local Vortex folder"
        storageText.textSize = 12.5f
        storageText.setTextColor(cGray)
        
        val barBg = FrameLayout(this)
        val bgDrawable = GradientDrawable()
        bgDrawable.cornerRadius = dp(4).toFloat()
        bgDrawable.setColor(Color.parseColor("#262638"))
        barBg.background = bgDrawable
        
        val fillBar = View(this)
        val fillDrawable = GradientDrawable()
        fillDrawable.cornerRadius = dp(4).toFloat()
        fillDrawable.setColor(cAccent)
        fillBar.background = fillDrawable
        barBg.addView(fillBar, FrameLayout.LayoutParams(dp(120), dp(6)))
        storageLayout.addView(storageText)
        storageLayout.addView(barBg, lp(mp, dp(6)).apply { topMargin = dp(6) })

        val chipsRow = LinearLayout(this)
        chipsRow.orientation = LinearLayout.HORIZONTAL
        chipsRow.setPadding(dp(2), dp(8), dp(2), dp(12))

        fun chip(name: String, selected: Boolean): TextView {
            val tv = TextView(this)
            tv.text = name; tv.textSize = 13.5f
            tv.setPadding(dp(16), dp(7), dp(16), dp(7))
            val g = GradientDrawable()
            g.cornerRadius = dp(18).toFloat()
            if (selected) {
                g.setColor(Color.parseColor("#38286E"))
                g.setStroke(dp(1), cAccent)
                tv.setTextColor(cAccent)
            } else {
                g.setColor(Color.parseColor("#1C1C28"))
                g.setStroke(dp(1), Color.parseColor("#38384A"))
                tv.setTextColor(Color.parseColor("#D0D0E0"))
            }
            tv.background = g
            return tv
        }

        val sv = ScrollView(this)
        val list = LinearLayout(this)
        list.orientation = LinearLayout.VERTICAL
        list.setPadding(0, dp(4), 0, dp(24))
        sv.addView(list)

        fun refreshList() {
            list.removeAllViews()
            chipsRow.removeAllViews()
            val filters = listOf("All", "Videos", "Audio", "Other")
            for ((idx, fName) in filters.withIndex()) {
                val c = chip(fName, currentFilter == idx)
                val clp = lp(wc, wc)
                clp.rightMargin = dp(8)
                c.setOnClickListener { currentFilter = idx; refreshList() }
                chipsRow.addView(c, clp)
            }

            val filtered = downloadHist.filter { item ->
                val name = item.name.lowercase()
                when (currentFilter) {
                    1 -> name.endsWith(".mp4") || name.endsWith(".mkv") || name.endsWith(".webm") || name.endsWith(".avi")
                    2 -> name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".aac") || name.endsWith(".m4a")
                    3 -> !name.endsWith(".mp4") && !name.endsWith(".mkv") && !name.endsWith(".mp3") && !name.endsWith(".wav") && !name.endsWith(".zip")
                    else -> true
                }
            }

            if (filtered.isEmpty()) {
                val tv = TextView(this)
                tv.text = "No downloads found"
                tv.setTextColor(cGray)
                tv.gravity = Gravity.CENTER
                tv.setPadding(0, dp(50), 0, 0)
                list.addView(tv)
                return
            }

            val fmt = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
            for (d in filtered) {
                val row = LinearLayout(this)
                row.orientation = LinearLayout.HORIZONTAL
                row.gravity = Gravity.CENTER_VERTICAL
                row.setPadding(dp(6), dp(10), dp(6), dp(10))

                val iconBox = LinearLayout(this); iconBox.gravity = Gravity.CENTER
                val ig = GradientDrawable()
                ig.shape = GradientDrawable.RECTANGLE
                ig.cornerRadius = dp(8).toFloat()
                ig.setColor(Color.parseColor("#1D1836"))
                iconBox.background = ig
                
                val fileIcon = IconView(this, 15); fileIcon.tint = cAccent; fileIcon.isClickable = false
                iconBox.addView(fileIcon, lp(dp(24), dp(24)))

                val details = LinearLayout(this); details.orientation = LinearLayout.VERTICAL
                val t1 = TextView(this); t1.text = d.name; t1.textSize = 15f; t1.setTextColor(Color.WHITE); t1.maxLines = 1; t1.ellipsize = TextUtils.TruncateAt.END

                val t2 = TextView(this); t2.text = fmt.format(Date(d.ms)) + "  ·  Vortex Download"
                t2.textSize = 12f; t2.setTextColor(cGray)
                details.addView(t1); details.addView(t2)

                val delBtn = IconView(this, 7); delBtn.tint = cGray; delBtn.ratio = 0.45f
                delBtn.setOnClickListener {
                    downloadHist.remove(d); saveDownloads(); toast("Deleted record"); refreshList()
                }

                val lIcon = lp(dp(40), dp(40)); lIcon.rightMargin = dp(12)
                row.addView(iconBox, lIcon); row.addView(details, lp(0, wc, 1f)); row.addView(delBtn, lp(dp(36), dp(36)))
                row.setOnClickListener { toast("Saved in " + d.path) }
                list.addView(row)
            }
        }

        trash.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Clear all downloads")
                .setMessage("Remove all download records from history?")
                .setPositiveButton("Clear") { _, _ ->
                    downloadHist.clear(); saveDownloads(); refreshList(); toast("Cleared")
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        refreshList()
        page.addView(head); page.addView(storageLayout); page.addView(chipsRow); page.addView(sv, lp(mp, 0, 1f))
        openOverlay(page)
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun lp(w: Int, h: Int, weight: Float = 0f) = LinearLayout.LayoutParams(w, h, weight)
    private fun cellLp(): LinearLayout.LayoutParams {
        val l = LinearLayout.LayoutParams(0, wc, 1f)
        l.setMargins(dp(6), dp(6), dp(6), dp(6))
        return l
    }
    private fun toast(m: String) = Toast.makeText(this, m, Toast.LENGTH_SHORT).show()
    private fun onOff(b: Boolean) = if (b) "ON" else "OFF"

    private fun bgGradient() = GradientDrawable(
        GradientDrawable.Orientation.TL_BR,
        intArrayOf(Color.parseColor("#150B33"), Color.parseColor("#07070C"), Color.parseColor("#0E0824"))
    )

    private fun glass(radius: Int, top: Int = 0x2E, bottom: Int = 0x12): GradientDrawable {
        val g = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(Color.argb(top, 255, 255, 255), Color.argb(bottom, 255, 255, 255))
        )
        g.cornerRadius = dp(radius).toFloat()
        g.setStroke(dp(1), Color.argb(0x40, 255, 255, 255))
        return g
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun pressAnim(v: View) {
        v.setOnTouchListener { view, e ->
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> view.animate().scaleX(0.96f).scaleY(0.96f).setDuration(90).start()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> view.animate().scaleX(1f).scaleY(1f).setDuration(160).start()
            }
            false
        }
    }

    private fun staggerIn(views: List<View>) {
        for ((i, v) in views.withIndex()) {
            v.alpha = 0f; v.translationY = dp(18).toFloat()
            v.animate().alpha(1f).translationY(0f).setStartDelay(50L + i * 30L).setDuration(260).setInterpolator(DecelerateInterpolator(1.6f)).start()
        }
    }

    private fun animateIn(v: View) {
        v.alpha = 0f; v.scaleX = 0.94f; v.scaleY = 0.94f
        v.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(280).setInterpolator(DecelerateInterpolator(1.5f)).start()
    }

    private fun showKeyboard(v: View) {
        v.postDelayed({
            v.requestFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT)
        }, 250L)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(overlay.windowToken, 0)
    }

    private fun startVoice() {
        val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Search with Vortex")
        try { startActivityForResult(i, 77) } catch (e: Exception) { toast("Voice search not available") }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun buildUi() {
        val frame = FrameLayout(this)
        val main = LinearLayout(this)
        main.orientation = LinearLayout.VERTICAL
        main.background = bgGradient()

        val toolbar = LinearLayout(this)
        toolbar.orientation = LinearLayout.HORIZONTAL
        toolbar.gravity = Gravity.CENTER_VERTICAL
        toolbar.setPadding(dp(6), dp(6), dp(6), dp(6))

        homeBtn = IconView(this, 2)
        homeBtn.setOnClickListener { showHome(tabs[cur]) }

        pill = LinearLayout(this)
        pill.orientation = LinearLayout.HORIZONTAL
        pill.gravity = Gravity.CENTER_VERTICAL
        pillBg = glass(22)
        pill.background = pillBg
        pill.setOnClickListener { openSearch(currentEditText()) }

        urlBar = TextView(this)
        urlBar.hint = "Search or enter URL"
        urlBar.textSize = 15f
        urlBar.setTextColor(Color.WHITE)
        urlBar.setHintTextColor(Color.parseColor("#7C7C96"))
        urlBar.setSingleLine()
        urlBar.ellipsize = TextUtils.TruncateAt.END
        urlBar.gravity = Gravity.CENTER_VERTICAL
        urlBar.setPadding(dp(16), 0, 0, 0)

        val mic = IconView(this, 5); mic.tint = cAccent
        mic.setOnClickListener { startVoice() }

        pill.addView(urlBar, lp(0, mp, 1f))
        pill.addView(mic, lp(dp(40), dp(40)))

        spacer = View(this)
        plusBtn = IconView(this, 9); plusBtn.ratio = 0.6f
        plusBtn.setOnClickListener { newTab("home") }
        tabsBtn = IconView(this, 3)
        tabsBtn.setOnClickListener { showTabsPage() }
        val menuBtn = IconView(this, 4)
        menuBtn.setOnClickListener { showToolbox() }

        toolbar.addView(homeBtn, lp(dp(44), dp(44)))
        val pl = lp(0, dp(40), 1f); pl.setMargins(dp(4), 0, dp(4), 0)
        toolbar.addView(pill, pl)
        toolbar.addView(spacer, lp(0, 1, 1f))
        toolbar.addView(plusBtn, lp(dp(44), dp(44)))
        toolbar.addView(tabsBtn, lp(dp(44), dp(44)))
        toolbar.addView(menuBtn, lp(dp(44), dp(44)))

        val line = View(this)
        line.setBackgroundColor(Color.argb(0x22, 255, 255, 255))

        progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
        progress.max = 100; progress.visibility = View.GONE
        progress.progressTintList = ColorStateList.valueOf(cAccent)

        container = FrameLayout(this)
        pull = PullLayout(this)
        pull.addView(container, FrameLayout.LayoutParams(mp, mp))
        pull.attach()
        pull.canPull = {
            val w = tabs.getOrNull(cur)
            w != null && !w.canScrollVertically(-1)
        }
        pull.onRefresh = {
            val w = tabs[cur]
            if (shown(w.url).isEmpty()) {
                forceFeed = true; showHome(w)
            } else {
                w.reload()
            }
        }

        chromeViews = listOf(toolbar, line, progress)
        main.addView(toolbar)
        main.addView(line, lp(mp, dp(1)))
        main.addView(progress, lp(mp, dp(2)))
        main.addView(pull, lp(mp, 0, 1f))

        overlay = FrameLayout(this)
        overlay.visibility = View.GONE

        frame.addView(main, FrameLayout.LayoutParams(mp, mp))
        frame.addView(overlay, FrameLayout.LayoutParams(mp, mp))
        setContentView(frame)
    }

    private fun updateToolbar(u: String?) {
        val home = shown(u).isEmpty()
        val wasHidden = pill.visibility != View.VISIBLE
        pill.visibility = if (home) View.GONE else View.VISIBLE
        spacer.visibility = if (home) View.VISIBLE else View.GONE
        plusBtn.visibility = if (home) View.GONE else View.VISIBLE
        if (!home && wasHidden) {
            pill.alpha = 0f
            pill.animate().alpha(1f).setDuration(220).start()
        }
        val w = tabs.getOrNull(cur)
        val inc = w != null && incog.contains(w)
        pillBg.setStroke(dp(1), if (inc) cAccent else Color.argb(0x40, 255, 255, 255))
    }

    private fun openOverlay(v: View) {
        overlayGen++
        overlay.removeAllViews()
        overlay.addView(v, FrameLayout.LayoutParams(mp, mp))
        overlay.visibility = View.VISIBLE
        v.alpha = 0f; v.translationY = dp(36).toFloat(); v.scaleX = 0.97f; v.scaleY = 0.97f
        v.animate().alpha(1f).translationY(0f).scaleX(1f).scaleY(1f).setDuration(260).setInterpolator(DecelerateInterpolator(1.6f)).start()
    }

    private fun closeOverlay() {
        hideKeyboard()
        if (overlay.visibility != View.VISIBLE) return
        val gen = ++overlayGen
        val child = overlay.getChildAt(0)
        if (child == null) { overlay.visibility = View.GONE; return }
        child.animate().alpha(0f).translationY(dp(24).toFloat()).setDuration(170).withEndAction {
            if (gen == overlayGen) { overlay.visibility = View.GONE; overlay.removeAllViews() }
        }.start()
    }

    private fun looksLikeUrl(t: String): Boolean =
        t.startsWith("http://") || t.startsWith("https://") || (!t.contains(" ") && t.contains("."))

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

    private fun display(u: String?): String =
        shown(u).removePrefix("https://").removePrefix("http://").removePrefix("www.")

    private fun currentEditText(): String {
        val u = tabs.getOrNull(cur)?.url
        return if (u == null || shown(u).isEmpty()) "" else u
    }

    private fun hostOf(url: String?): String? {
        if (url == null) return null
        return try { Uri.parse(url).host } catch (e: Exception) { null }
    }

    private fun submitQuery(q: String) {
        val t = q.trim()
        if (t.isEmpty()) return
        val w = tabs[cur]
        if (!looksLikeUrl(t) && !incog.contains(w)) addSearchHistory(t)
        w.loadUrl(normalize(t))
        w.requestFocus()
    }

    private fun shareText(s: String) {
        val i = Intent(Intent.ACTION_SEND)
        i.type = "text/plain"
        i.putExtra(Intent.EXTRA_TEXT, s)
        startActivity(Intent.createChooser(i, "Share"))
    }

    private fun handleVortex(u: String) {
        val uri = Uri.parse(u)
        when (uri.host) {
            "voice" -> startVoice()
            "search" -> openSearch("")
            "feed" -> loadFeed(true)
            "feedlang" -> cycleFeed()
            "share" -> {
                val target = uri.getQueryParameter("u")
                if (target != null) shareText(target)
            }
        }
    }

    private fun loadHistory() {
        try {
            val raw = prefs.getString("web_hist", "[]") ?: "[]"
            val a = JSONArray(raw)
            for (i in 0 until a.length()) {
                val o = a.getJSONObject(i)
                webHist.add(HistItem(o.getString("t"), o.getString("u"), o.getLong("ms")))
            }
            val raw2 = prefs.getString("search_hist", "[]") ?: "[]"
            val s = JSONArray(raw2)
            for (i in 0 until s.length()) searchHist.add(s.getString(i))
        } catch (e: Exception) {}
    }

    private fun saveWebHist() {
        val a = JSONArray()
        for (h in webHist) {
            val o = JSONObject(); o.put("t", h.t); o.put("u", h.u); o.put("ms", h.ms); a.put(o)
        }
        prefs.edit().putString("web_hist", a.toString()).apply()
    }

    private fun saveSearchHist() {
        val a = JSONArray()
        for (s in searchHist) a.put(s)
        prefs.edit().putString("search_hist", a.toString()).apply()
    }

    private fun addWebHistory(title: String, url: String) {
        if (webHist.isNotEmpty() && webHist[0].u == url) {
            webHist[0] = HistItem(title, url, System.currentTimeMillis())
        } else {
            webHist.add(0, HistItem(title, url, System.currentTimeMillis()))
            while (webHist.size > 300) webHist.removeAt(webHist.size - 1)
        }
        saveWebHist()
    }

    private fun addSearchHistory(q: String) {
        searchHist.removeAll { it.equals(q, true) }
        searchHist.add(0, q)
        while (searchHist.size > 100) searchHist.removeAt(searchHist.size - 1)
        saveSearchHist()
    }

    private fun showClearDialog(after: (() -> Unit)? = null) {
        val opts = arrayOf("Website history", "Search history", "Both")
        AlertDialog.Builder(this)
            .setTitle("Clear history")
            .setItems(opts) { _, i ->
                if (i == 0 || i == 2) { webHist.clear(); saveWebHist() }
                if (i == 1 || i == 2) { searchHist.clear(); saveSearchHist() }
                toast("History cleared"); after?.invoke()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun histRow(lead: View, title: String, sub: String, click: () -> Unit, onLong: () -> Unit): View {
        val row = LinearLayout(this); row.orientation = LinearLayout.HORIZONTAL; row.gravity = Gravity.CENTER_VERTICAL
        row.setPadding(dp(6), dp(8), dp(6), dp(8))
        val col = LinearLayout(this); col.orientation = LinearLayout.VERTICAL
        val t1 = TextView(this); t1.text = title; t1.textSize = 15f; t1.setTextColor(Color.WHITE); t1.maxLines = 1; t1.ellipsize = TextUtils.TruncateAt.END
        col.addView(t1)
        if (sub.isNotEmpty()) {
            val t2 = TextView(this); t2.text = sub; t2.textSize = 12f; t2.setTextColor(cGray); t2.maxLines = 1; t2.ellipsize = TextUtils.TruncateAt.END
            col.addView(t2)
        }
        val l0 = lp(dp(40), dp(40)); l0.rightMargin = dp(12)
        row.addView(lead, l0); row.addView(col, lp(0, wc, 1f))
        row.setOnClickListener { click() }
        row.setOnLongClickListener { onLong(); true }
        return row
    }

    private fun avatar(letter: String): TextView {
        val tv = TextView(this); tv.text = letter; tv.gravity = Gravity.CENTER; tv.textSize = 16f; tv.setTextColor(cAccent)
        val g = GradientDrawable(); g.shape = GradientDrawable.OVAL
        g.setColor(Color.argb(0x26, 255, 255, 255)); g.setStroke(dp(1), Color.argb(0x33, 255, 255, 255))
        tv.background = g
        return tv
    }

    private fun fillHistory(list: LinearLayout, searches: Boolean, onChange: () -> Unit) {
        list.removeAllViews()
        val empty = if (searches) searchHist.isEmpty() else webHist.isEmpty()
        if (empty) {
            val tv = TextView(this); tv.text = "No history yet"; tv.setTextColor(cGray); tv.gravity = Gravity.CENTER; tv.setPadding(0, dp(40), 0, 0)
            list.addView(tv); return
        }
        if (searches) {
            for (s in searchHist.toList()) {
                val ic = IconView(this, 23); ic.tint = cGray; ic.isClickable = false
                list.addView(histRow(ic, s, "", { closeOverlay(); submitQuery(s) }, { searchHist.remove(s); saveSearchHist(); toast("Removed"); onChange() }))
            }
        } else {
            val fmt = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
            for (h in webHist.take(200).toList()) {
                val host = hostOf(h.u) ?: h.u
                val letter = host.removePrefix("www.").take(1).uppercase()
                list.addView(histRow(avatar(letter), h.t, fmt.format(Date(h.ms)) + "  |  " + host, { closeOverlay(); tabs[cur].loadUrl(h.u) }, { webHist.remove(h); saveWebHist(); toast("Removed"); onChange() }))
            }
        }
    }

    private fun styleTab(tv: TextView, selected: Boolean) {
        if (selected) { tv.background = glass(18, 0x40, 0x1C); tv.setTextColor(cAccent) }
        else { tv.background = null; tv.setTextColor(cGray) }
    }

    private fun pillTab(text: String): TextView {
        val tv = TextView(this); tv.text = text; tv.textSize = 14f; tv.gravity = Gravity.CENTER; tv.setPadding(0, dp(10), 0, dp(10))
        return tv
    }

    private fun showHistory(startSearches: Boolean) {
        var mode = startSearches
        val page = LinearLayout(this); page.orientation = LinearLayout.VERTICAL; page.background = bgGradient()
        page.setPadding(dp(12), dp(24), dp(12), 0)

        val head = LinearLayout(this); head.orientation = LinearLayout.HORIZONTAL; head.gravity = Gravity.CENTER_VERTICAL
        val title = TextView(this); title.text = "History"; title.textSize = 22f; title.setTextColor(Color.WHITE); title.setPadding(dp(6), 0, 0, 0)
        val trash = IconView(this, 24); val close = IconView(this, 7)
        close.setOnClickListener { closeOverlay() }
        head.addView(title, lp(0, wc, 1f)); head.addView(trash, lp(dp(44), dp(44))); head.addView(close, lp(dp(44), dp(44)))

        val tabW = pillTab("Websites"); val tabS = pillTab("Searches")
        val tabsRow = LinearLayout(this); tabsRow.orientation = LinearLayout.HORIZONTAL; tabsRow.setPadding(0, dp(8), 0, dp(8))
        tabsRow.addView(tabW, cellLp()); tabsRow.addView(tabS, cellLp())

        val sv = ScrollView(this)
        val list = LinearLayout(this); list.orientation = LinearLayout.VERTICAL; list.setPadding(0, dp(4), 0, dp(24))
        sv.addView(list)

        fun refresh() {
            fillHistory(list, mode) { refresh() }
            styleTab(tabW, !mode); styleTab(tabS, mode)
        }
        tabW.setOnClickListener { mode = false; refresh() }
        tabS.setOnClickListener { mode = true; refresh() }
        trash.setOnClickListener { showClearDialog { refresh() } }
        refresh()

        page.addView(head); page.addView(tabsRow); page.addView(sv, lp(mp, 0, 1f))
        openOverlay(page)
    }

    private fun fetchSuggest(q: String, cb: (List<String>) -> Unit) {
        val t = q.trim(); val token = ++suggestToken
        if (t.isEmpty()) return
        Thread {
            try {
                Thread.sleep(160)
                if (token != suggestToken) return@Thread
                val con = URL("https://suggestqueries.google.com/complete/search?client=firefox&q=" + Uri.encode(t)).openConnection() as HttpURLConnection
                con.connectTimeout = 3000; con.readTimeout = 3000
                val txt = con.inputStream.use { String(it.readBytes(), Charsets.UTF_8) }
                val arr = JSONArray(txt).getJSONArray(1)
                val out = ArrayList<String>()
                for (i in 0 until arr.length()) out.add(arr.getString(i))
                if (token == suggestToken) runOnUiThread { cb(out) }
            } catch (e: Exception) {}
        }.start()
    }

    private fun suggestRow(list: LinearLayout, et: EditText, kind: Int, text: String): View {
        val row = LinearLayout(this); row.orientation = LinearLayout.HORIZONTAL; row.gravity = Gravity.CENTER_VERTICAL
        row.setPadding(dp(6), 0, dp(6), 0)
        val ic = IconView(this, kind); ic.tint = cGray; ic.ratio = 0.45f; ic.isClickable = false
        val tv = TextView(this); tv.text = text; tv.textSize = 16f; tv.setTextColor(Color.WHITE); tv.maxLines = 1; tv.ellipsize = TextUtils.TruncateAt.END
        val fill = IconView(this, 22); fill.tint = cGray; fill.ratio = 0.45f
        fill.setOnClickListener { et.setText(text); et.setSelection(text.length) }
        row.addView(ic, lp(dp(40), dp(48))); row.addView(tv, lp(0, wc, 1f)); row.addView(fill, lp(dp(44), dp(48)))
        row.setOnClickListener { closeOverlay(); submitQuery(text) }
        if (kind == 23) {
            row.setOnLongClickListener {
                searchHist.removeAll { it.equals(text, true) }
                saveSearchHist(); toast("Removed"); fillSuggestions(list, et, emptyList())
                true
            }
        }
        return row
    }

    private fun fillSuggestions(list: LinearLayout, et: EditText, live: List<String>) {
        list.removeAllViews()
        val t = et.text.toString().trim()
        val items = ArrayList<Pair<Int, String>>()
        if (t.isEmpty()) {
            for (s in searchHist.take(8)) items.add(Pair(23, s))
        } else {
            items.add(Pair(6, t))
            for (s in searchHist) {
                if (items.size < 4 && s.contains(t, true) && !s.equals(t, true)) items.add(Pair(23, s))
            }
            for (s in live) {
                if (items.size < 11 && items.none { it.second.equals(s, true) }) items.add(Pair(6, s))
            }
        }
        for (p in items) list.addView(suggestRow(list, et, p.first, p.second))
    }

    private fun openSearch(prefill: String) {
        val page = LinearLayout(this); page.orientation = LinearLayout.VERTICAL; page.background = bgGradient()
        page.setPadding(dp(10), dp(24), dp(10), 0)

        val row = LinearLayout(this); row.orientation = LinearLayout.HORIZONTAL; row.gravity = Gravity.CENTER_VERTICAL
        val back = IconView(this, 0)
        back.setOnClickListener { closeOverlay() }

        val box = LinearLayout(this); box.orientation = LinearLayout.HORIZONTAL; box.gravity = Gravity.CENTER_VERTICAL
        box.background = glass(24)

        val et = EditText(this)
        et.setSingleLine(); et.hint = "Search or enter URL"; et.textSize = 16f; et.setTextColor(Color.WHITE)
        et.setHintTextColor(Color.parseColor("#7C7C96")); et.setBackgroundColor(Color.TRANSPARENT)
        et.setPadding(dp(16), 0, 0, 0); et.imeOptions = EditorInfo.IME_ACTION_GO

        val clear = IconView(this, 7); clear.ratio = 0.45f; clear.tint = cGray
        clear.setOnClickListener { et.setText("") }
        val mic = IconView(this, 5); mic.tint = cAccent
        mic.setOnClickListener { closeOverlay(); startVoice() }

        box.addView(et, lp(0, dp(46), 1f)); box.addView(clear, lp(dp(36), dp(46))); box.addView(mic, lp(dp(40), dp(46)))
        row.addView(back, lp(dp(44), dp(44))); row.addView(box, lp(0, wc, 1f))

        val sv = ScrollView(this)
        val list = LinearLayout(this); list.orientation = LinearLayout.VERTICAL; list.setPadding(0, dp(10), 0, dp(24))
        sv.addView(list)

        page.addView(row); page.addView(sv, lp(mp, 0, 1f))

        et.setText(prefill)
        fillSuggestions(list, et, emptyList())
        et.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val q = s?.toString() ?: ""
                fillSuggestions(list, et, emptyList())
                fetchSuggest(q) { live -> if (et.text.toString() == q) fillSuggestions(list, et, live) }
            }
        })
        et.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_GO || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                val q = et.text.toString(); closeOverlay(); submitQuery(q); true
            } else false
        }
        openOverlay(page)
        et.selectAll(); showKeyboard(et)
    }

    private fun showHome(w: WebView) {
        w.loadDataWithBaseURL(homeUrl, homeHtml(), "text/html", "utf-8", homeUrl)
    }

    private fun homeHtml(): String = buildHomeHtml(showGreeting)

    private fun feedJs(js: String) {
        for (w in tabs) {
            if (shown(w.url).isEmpty()) w.evaluateJavascript(js, null)
        }
    }

    private fun pushFeed(json: String) {
        feedJs("window.vxFeed&&window.vxFeed(" + JSONObject.quote(json) + ")")
    }

    private fun loadFeed(force: Boolean) {
        val cached = prefs.getString("feed_json" + feedIdx, null)
        val time = prefs.getLong("feed_time" + feedIdx, 0L)
        if (cached != null) pushFeed(cached)
        if (!force && cached != null && System.currentTimeMillis() - time < 30 * 60 * 1000L) return
        if (feedBusy) return
        feedBusy = true
        val idx = feedIdx
        Thread {
            val json = fetchFeed(idx)
            runOnUiThread {
                feedBusy = false
                if (json != null) {
                    prefs.edit().putString("feed_json" + idx, json).putLong("feed_time" + idx, System.currentTimeMillis()).apply()
                    if (idx == feedIdx) pushFeed(json)
                } else {
                    feedJs("window.vxFeedErr&&window.vxFeedErr()")
                }
                pull.finish()
            }
        }.start()
    }

    private fun cycleFeed() {
        feedIdx = (feedIdx + 1) % feeds.size
        prefs.edit().putInt("feed", feedIdx).apply()
        toast("Discover: " + feeds[feedIdx].first)
        loadFeed(true)
    }

    private fun fetchFeed(idx: Int): String? {
        return try {
            val con = URL(feeds[idx].second).openConnection() as HttpURLConnection
            con.connectTimeout = 6000; con.readTimeout = 8000
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Mobile Safari/537.36")
            val data = con.inputStream.use { it.readBytes() }
            parseRss(data)
        } catch (e: Exception) { null }
    }

    private fun parseRss(data: ByteArray): String? {
        val parser = Xml.newPullParser()
        parser.setInput(ByteArrayInputStream(data), "UTF-8")
        val arr = JSONArray()
        val fmt = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH)
        var inItem = false; var title = ""; var link = ""; var date = ""; var src = ""
        var ev = parser.eventType
        while (ev != XmlPullParser.END_DOCUMENT && arr.length() < 14) {
            if (ev == XmlPullParser.START_TAG) {
                when (parser.name) {
                    "item" -> { inItem = true; title = ""; link = ""; date = ""; src = "" }
                    "title" -> { if (inItem) title = parser.nextText() }
                    "link" -> { if (inItem) link = parser.nextText() }
                    "pubDate" -> { if (inItem) date = parser.nextText() }
                    "source" -> { if (inItem) src = parser.nextText() }
                }
            } else if (ev == XmlPullParser.END_TAG && parser.name == "item") {
                inItem = false
                var t = title; var s = src
                val cut = t.lastIndexOf(" - ")
                if (cut > 0) {
                    if (s.isEmpty()) s = t.substring(cut + 3)
                    t = t.substring(0, cut)
                }
                var ms = 0L
                try { ms = fmt.parse(date)?.time ?: 0L } catch (e: Exception) {}
                if (t.isNotEmpty() && link.isNotEmpty()) {
                    val o = JSONObject()
                    o.put("t", t); o.put("s", s); o.put("l", link); o.put("m", ms)
                    arr.put(o)
                }
            }
            ev = parser.next()
        }
        return if (arr.length() > 0) arr.toString() else null
    }

    private fun skipName(d: ByteArray, start: Int): Int {
        var p = start
        while (p < d.size) {
            val len = d[p].toInt() and 0xFF
            if (len == 0) return p + 1
            if ((len and 0xC0) == 0xC0) return p + 2
            p += len + 1
        }
        return p
    }

    private fun parseBlocked(d: ByteArray): Boolean {
        if (d.size < 12) return false
        val rcode = d[3].toInt() and 0x0F
        if (rcode == 3 || rcode == 5) return true
        val qd = ((d[4].toInt() and 0xFF) shl 8) or (d[5].toInt() and 0xFF)
        val an = ((d[6].toInt() and 0xFF) shl 8) or (d[7].toInt() and 0xFF)
        var pos = 12
        for (q in 0 until qd) pos = skipName(d, pos) + 4
        for (a in 0 until an) {
            pos = skipName(d, pos)
            if (pos + 10 > d.size) return false
            val type = ((d[pos].toInt() and 0xFF) shl 8) or (d[pos + 1].toInt() and 0xFF)
            val rdlen = ((d[pos + 8].toInt() and 0xFF) shl 8) or (d[pos + 9].toInt() and 0xFF)
            pos += 10
            if (pos + rdlen > d.size) return false
            if ((type == 1 && rdlen == 4) || (type == 28 && rdlen == 16)) {
                var zero = true
                for (k in 0 until rdlen) { if (d[pos + k].toInt() != 0) zero = false }
                if (zero) return true
            }
            pos += rdlen
        }
        return false
    }

    private fun dohBlocked(host: String): Boolean {
        val cached = dnsCache[host]
        if (cached != null) return cached
        if (System.currentTimeMillis() < dohDownUntil) return false
        var ok = false; var result = false
        try {
            val out = ByteArrayOutputStream()
            out.write(byteArrayOf(0x12, 0x34, 0x01, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00))
            for (label in host.split(".")) {
                if (label.isEmpty()) continue
                val b = label.toByteArray()
                out.write(b.size); out.write(b)
            }
            out.write(0)
            out.write(byteArrayOf(0x00, 0x01, 0x00, 0x01))
            val con = URL(dohUrl).openConnection() as HttpURLConnection
            con.requestMethod = "POST"; con.connectTimeout = 1500; con.readTimeout = 1500; con.doOutput = true
            con.setRequestProperty("Content-Type", "application/dns-message")
            con.setRequestProperty("Accept", "application/dns-message")
            con.outputStream.use { it.write(out.toByteArray()) }
            if (con.responseCode == 200) {
                val data = con.inputStream.use { it.readBytes() }
                result = parseBlocked(data); ok = true
            }
            con.disconnect()
        } catch (e: Exception) {}
        if (ok) dnsCache[host] = result else dohDownUntil = System.currentTimeMillis() + 60000L
        return result
    }

    private fun youtubeRule(): Rule {
        val css = ".ytp-ad-overlay-container,.ytp-ad-image-overlay,#masthead-ad,ytd-ad-slot-renderer,ytd-promoted-sparkles-web-renderer,.ytd-display-ad-renderer{display:none!important}"
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
            rules.add(youtubeRule()); saveRules(); return
        }
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                rules.add(Rule(o.getString("host"), o.optString("css"), o.optString("js"), o.optBoolean("on", true)))
            }
        } catch (e: Exception) {}
    }

    private fun saveRules() {
        val arr = JSONArray()
        for (r in rules) {
            val o = JSONObject(); o.put("host", r.host); o.put("css", r.css); o.put("js", r.js); o.put("on", r.on)
            arr.put(o)
        }
        prefs.edit().putString("rules", arr.toString()).apply()
    }

    private fun matches(host: String, ruleHost: String): Boolean = host == ruleHost || host.endsWith(".$ruleHost")

    private fun cssJs(id: String, css: String): String =
        "(function(){var s=document.getElementById('$id');if(!s){s=document.createElement('style');s.id='$id';document.documentElement.appendChild(s);}s.textContent=" + JSONObject.quote(css) + ";})();"

    private fun inject(w: WebView, url: String?) {
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
        val col = LinearLayout(this); col.orientation = LinearLayout.VERTICAL; col.setPadding(pad, pad, pad, pad)

        val hostEt = EditText(this); hostEt.setSingleLine(); hostEt.hint = "domain (e.g. youtube.com)"; hostEt.setText(hostIn)
        val cssEt = EditText(this); cssEt.hint = "Custom CSS"; cssEt.minLines = 4; cssEt.gravity = Gravity.TOP; cssEt.setText(existing?.css ?: "")
        val jsEt = EditText(this); jsEt.hint = "Custom JS"; jsEt.minLines = 6; jsEt.gravity = Gravity.TOP; jsEt.setText(existing?.js ?: "")
        val onCb = CheckBox(this); onCb.text = "Enabled"; onCb.isChecked = existing?.on ?: true

        col.addView(hostEt); col.addView(cssEt); col.addView(jsEt); col.addView(onCb)
        val sv = ScrollView(this); sv.addView(col)

        AlertDialog.Builder(this)
            .setTitle("Site injector")
            .setView(sv)
            .setPositiveButton("Save") { _, _ ->
                val h = hostEt.text.toString().trim().lowercase()
                if (h.isNotEmpty()) {
                    rules.removeAll { it.host == hostIn || it.host == h }
                    rules.add(Rule(h, cssEt.text.toString(), jsEt.text.toString(), onCb.isChecked))
                    saveRules(); tabs[cur].reload()
                }
            }
            .setNegativeButton("Delete") { _, _ ->
                rules.removeAll { it.host == hostIn }
                saveRules(); tabs[cur].reload()
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    private fun editSiteRule() {
        val h = hostOf(tabs[cur].url)
        if (h == null || h == "vortex.local") { toast("Open a site first"); return }
        showRuleEditor(h.removePrefix("www.").removePrefix("m."))
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
            if (Build.VERSION.SDK_INT >= 29) cv.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Vortex")
            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv) ?: return
            contentResolver.openOutputStream(uri)?.use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
            toast("Saved: Pictures/Vortex")
        } catch (e: Exception) { toast("Screenshot save failed") }
    }

    private fun toggleDesktop() {
        desktop = !desktop
        tabs[cur].settings.userAgentString = if (desktop) desktopUa else WebSettings.getDefaultUserAgent(this)
        tabs[cur].reload()
    }

    private fun sharePage() {
        val u = tabs[cur].url
        if (u == null || shown(u).isEmpty()) { toast("Open a site first"); return }
        shareText(u)
    }

    private fun cycleEngine() {
        engineIdx = (engineIdx + 1) % engines.size
        prefs.edit().putInt("engine", engineIdx).apply()
        toast("Search engine: " + engines[engineIdx].first)
    }

    private fun showHelp() {
        AlertDialog.Builder(this)
            .setTitle("Help & Feedback")
            .setMessage("Facing a problem or have a suggestion? Message me directly on Facebook and I will help.")
            .setPositiveButton("Message me") { _, _ -> newTab(feedbackUrl) }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showToolbox() {
        val sv = ScrollView(this); sv.background = bgGradient()
        val col = LinearLayout(this); col.orientation = LinearLayout.VERTICAL
        col.setPadding(dp(14), dp(28), dp(14), dp(28))

        val head = LinearLayout(this); head.orientation = LinearLayout.HORIZONTAL; head.gravity = Gravity.CENTER_VERTICAL
        head.setPadding(dp(6), 0, dp(6), dp(18))

        val logoId = resources.getIdentifier("logo", "drawable", packageName)
        if (logoId != 0) {
            val iv = ImageView(this); iv.setImageResource(logoId)
            head.addView(iv, lp(dp(56), dp(56)))
        }

        val titles = LinearLayout(this); titles.orientation = LinearLayout.VERTICAL; titles.setPadding(dp(12), 0, 0, 0)
        val nameRow = LinearLayout(this); nameRow.orientation = LinearLayout.HORIZONTAL
        val n1 = TextView(this); n1.text = "Vortex"; n1.textSize = 21f; n1.setTextColor(cAccent)
        val n2 = TextView(this); n2.text = " Browser"; n2.textSize = 21f; n2.setTextColor(Color.WHITE)
        nameRow.addView(n1); nameRow.addView(n2)
        val subT = TextView(this); subT.text = "Toolbox"; subT.textSize = 13f; subT.setTextColor(cGray)
        titles.addView(nameRow); titles.addView(subT)

        val close = IconView(this, 7)
        close.setOnClickListener { closeOverlay() }

        head.addView(titles, lp(0, wc, 1f)); head.addView(close, lp(dp(44), dp(44)))
        col.addView(head)

        val tools = listOf(
            Tool(10, "Ad Blocker", "AdGuard DNS: " + onOff(adblock)) { adblock = !adblock; tabs[cur].reload() },
            Tool(11, "Incognito Mode", "Browse privately") { newTab("home", true) },
            Tool(13, "Screenshot", "Capture screen") { screenshot() },
            Tool(14, "Desktop Mode", "Desktop site: " + onOff(desktop)) { toggleDesktop() },
            Tool(15, "Download History", "Check your downloads") { showDownloadsPage() },
            Tool(16, "Share Page", "Share current page") { sharePage() },
            Tool(19, "Site Injector", "Custom CSS/JS for this site") { editSiteRule() },
            Tool(6, "Search Engine", engines[engineIdx].first) { cycleEngine() },
            Tool(23, "History", "Websites and searches") { showHistory(false) },
            Tool(24, "Clear History", "Delete browsing data") { showClearDialog() },
            Tool(26, "Install App", "Create home shortcut") { installShortcut() },
            Tool(25, "Help & Feedback", "Message the developer") { showHelp() }
        )

        val tileViews = ArrayList<View>()
        for (pair in tools.chunked(2)) {
            val row = LinearLayout(this); row.orientation = LinearLayout.HORIZONTAL
            for (tool in pair) {
                val tv = tile(tool)
                tileViews.add(tv)
                row.addView(tv, cellLp())
            }
            if (pair.size == 1) row.addView(View(this), cellLp())
            col.addView(row)
        }

        val foot = TextView(this)
        foot.text = "Vortex Tools"; foot.gravity = Gravity.CENTER; foot.textSize = 15f; foot.setTextColor(cAccent)
        foot.background = glass(28, 0x30, 0x14)
        val fl = lp(mp, dp(52)); fl.setMargins(dp(6), dp(18), dp(6), 0)
        col.addView(foot, fl)

        sv.addView(col)
        openOverlay(sv)
        staggerIn(tileViews)
    }

    private fun tile(tool: Tool): View {
        val card = LinearLayout(this); card.orientation = LinearLayout.VERTICAL
        card.setPadding(dp(16), dp(14), dp(12), dp(14)); card.background = glass(20)

        val top = LinearLayout(this); top.orientation = LinearLayout.HORIZONTAL; top.gravity = Gravity.CENTER_VERTICAL
        val ic = IconView(this, tool.kind); ic.tint = cAccent; ic.ratio = 0.6f; ic.isClickable = false
        val gap = View(this)
        val ch = IconView(this, 8); ch.tint = cAccent; ch.isClickable = false
        top.addView(ic, lp(dp(52), dp(52))); top.addView(gap, lp(0, 1, 1f)); top.addView(ch, lp(dp(28), dp(28)))

        val t1 = TextView(this); t1.text = tool.title; t1.textSize = 16f; t1.setTextColor(Color.WHITE)
        val t2 = TextView(this); t2.text = tool.sub; t2.textSize = 12f; t2.setTextColor(cGray)

        card.addView(top)
        val tl = lp(mp, wc); tl.topMargin = dp(8)
        card.addView(t1, tl); card.addView(t2)
        pressAnim(card)
        card.setOnClickListener { closeOverlay(); tool.action() }
        return card
    }

    private fun captureThumb(w: WebView) {
        if (w.width <= 0 || w.height <= 0) return
        try {
            val sc = 0.4f
            val bmp = Bitmap.createBitmap((w.width * sc).toInt(), (w.height * sc).toInt(), Bitmap.Config.ARGB_8888)
            val c = Canvas(bmp); c.scale(sc, sc); w.draw(c)
            thumbs[w] = bmp
        } catch (e: Exception) {}
    }

    private fun showTabsPage() {
        tabs.getOrNull(cur)?.let { captureThumb(it) }
        val page = LinearLayout(this); page.orientation = LinearLayout.VERTICAL; page.background = bgGradient()

        val head = LinearLayout(this); head.orientation = LinearLayout.HORIZONTAL; head.gravity = Gravity.CENTER_VERTICAL
        head.setPadding(dp(18), dp(24), dp(10), dp(10))
        val title = TextView(this); title.text = "Tabs (" + tabs.size + ")"; title.textSize = 20f; title.setTextColor(Color.WHITE)
        val add = IconView(this, 9); add.tint = cAccent; add.setOnClickListener { closeOverlay(); newTab("home") }
        val close = IconView(this, 7); close.setOnClickListener { closeOverlay() }
        head.addView(title, lp(0, wc, 1f)); head.addView(add, lp(dp(44), dp(44))); head.addView(close, lp(dp(44), dp(44)))

        val sv = ScrollView(this)
        val col = LinearLayout(this); col.orientation = LinearLayout.VERTICAL; col.setPadding(dp(10), dp(4), dp(10), dp(24))
        val cards = ArrayList<View>()
        for (pair in tabs.indices.toList().chunked(2)) {
            val row = LinearLayout(this); row.orientation = LinearLayout.HORIZONTAL
            for (i in pair) {
                val cv = tabCard(i); cards.add(cv); row.addView(cv, cellLp())
            }
            if (pair.size == 1) row.addView(View(this), cellLp())
            col.addView(row)
        }
        sv.addView(col)
        page.addView(head); page.addView(sv, lp(mp, 0, 1f))
        openOverlay(page)
        staggerIn(cards)
    }

    private fun tabCard(i: Int): View {
        val w = tabs[i]
        val card = LinearLayout(this); card.orientation = LinearLayout.VERTICAL; card.setPadding(dp(8), dp(8), dp(8), dp(8))
        val bg = glass(16)
        if (i == cur) bg.setStroke(dp(2), cAccent)
        card.background = bg

        val raw = w.title; val host = hostOf(w.url)
        var name = if (shown(w.url).isEmpty()) "Home" else if (!raw.isNullOrBlank()) raw else (host ?: "Tab")
        if (incog.contains(w)) name = "Incognito: " + name

        val head = LinearLayout(this); head.orientation = LinearLayout.HORIZONTAL; head.gravity = Gravity.CENTER_VERTICAL
        val tv = TextView(this); tv.text = name; tv.textSize = 12f; tv.setTextColor(Color.WHITE); tv.maxLines = 1; tv.ellipsize = TextUtils.TruncateAt.END; tv.setPadding(dp(4), 0, 0, 0)
        val x = IconView(this, 7); x.ratio = 0.5f; x.setOnClickListener { closeTabAt(i); showTabsPage() }
        head.addView(tv, lp(0, wc, 1f)); head.addView(x, lp(dp(32), dp(32)))

        val iv = ImageView(this); iv.scaleType = ImageView.ScaleType.CENTER_CROP; iv.setBackgroundColor(cBg)
        val th = thumbs[w]
        if (th != null) iv.setImageBitmap(th)

        card.addView(head); card.addView(iv, lp(mp, dp(150)))
        pressAnim(card)
        card.setOnClickListener { closeOverlay(); switchTo(i) }
        return card
    }

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
        w.overScrollMode = View.OVER_SCROLL_NEVER
        w.addJavascriptInterface(MediaBridge(w), "VortexMedia")
        if (desktop) s.userAgentString = desktopUa
        if (incognito) {
            incog.add(w)
            s.cacheMode = WebSettings.LOAD_NO_CACHE
            s.saveFormData = false
        }

        w.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val u = request.url.toString()
                if (u.startsWith("vortex://")) { handleVortex(u); return true }
                return false
            }

            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                if (adblock && !request.isForMainFrame) {
                    val sch = request.url.scheme
                    val host = request.url.host ?: ""
                    val isIp = host.all { it.isDigit() || it == '.' || it == ':' }
                    if ((sch == "http" || sch == "https") && host.isNotEmpty() && host != "vortex.local" && !isIp) {
                        if (blocked.any { host == it || host.endsWith(".$it") } || dohBlocked(host)) {
                            return WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream(ByteArray(0)))
                        }
                    }
                }
                return null
            }

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                injectMedia(view)
                if (view === tabs.getOrNull(cur)) {
                    urlBar.text = display(url)
                    updateToolbar(url)
                }
            }

            override fun onPageCommitVisible(view: WebView, url: String) {
                injectMedia(view)
            }

            override fun onPageFinished(view: WebView, url: String) {
                inject(view, url)
                injectMedia(view)
                if (url.startsWith(homeUrl)) {
                    val f = forceFeed; forceFeed = false; loadFeed(f)
                } else if (!incog.contains(view) && shown(url).isNotEmpty() && (url.startsWith("http://") || url.startsWith("https://"))) {
                    val ttl = view.title
                    addWebHistory(if (!ttl.isNullOrBlank()) ttl else (hostOf(url) ?: url), url)
                }
                if (view === tabs.getOrNull(cur)) {
                    updateToolbar(url)
                    if (!url.startsWith(homeUrl)) pull.finish()
                }
            }
        }

        w.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                if (view !== tabs.getOrNull(cur)) return
                progress.progress = newProgress
                progress.visibility = if (!inPip && newProgress in 1..99) View.VISIBLE else View.GONE
            }

            override fun onReceivedIcon(view: WebView, icon: Bitmap?) {
                if (icon != null) favicons[view] = Pair(hostOf(view.url), icon)
            }

            override fun onPermissionRequest(request: PermissionRequest) { runOnUiThread { handleWebPermission(request) } }
            override fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback) { handleGeo(origin, callback) }

            override fun onShowFileChooser(webView: WebView, filePathCallback: ValueCallback<Array<Uri>>, fileChooserParams: WebChromeClient.FileChooserParams): Boolean {
                fileCallback?.onReceiveValue(null)
                fileCallback = filePathCallback
                return try {
                    startActivityForResult(fileChooserParams.createIntent(), 78); true
                } catch (e: Exception) {
                    fileCallback = null; toast("File picker not available"); false
                }
            }

            private var customView: View? = null
            private var customViewCallback: CustomViewCallback? = null

            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                if (customView != null) { callback?.onCustomViewHidden(); return }
                customView = view
                customViewCallback = callback
                hideFs = { onHideCustomView() }
                requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    window.insetsController?.let {
                        it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                        it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    }
                } else {
                    @Suppress("DEPRECATION")
                    window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
                }

                val decor = window.decorView as FrameLayout
                decor.addView(customView, FrameLayout.LayoutParams(mp, mp))
            }

            override fun onHideCustomView() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    window.insetsController?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                } else {
                    @Suppress("DEPRECATION")
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                }

                val decor = window.decorView as FrameLayout
                if (customView != null) decor.removeView(customView)
                hideFs = null
                customView = null
                customViewCallback?.onCustomViewHidden()
                requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }

        w.setDownloadListener { dlUrl, userAgent, contentDisposition, mime, _ ->
            startDownload(dlUrl, userAgent, contentDisposition, mime, w.url)
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
        animateIn(container)
        urlBar.text = display(tabs[i].url)
        tabsBtn.count = tabs.size
        tabsBtn.invalidate()
        updateToolbar(tabs[i].url)
        updatePipParams()
    }

    private fun wipeIfIncognito(w: WebView) {
        playingTabs.remove(w); videoTabs.remove(w); favicons.remove(w)
        if (playingTabs.isEmpty()) ui.postDelayed(stopKeep, 45000L)
        if (incog.remove(w)) {
            w.clearCache(true); w.clearHistory(); w.clearFormData()
        }
    }

    private fun closeTabAt(i: Int) {
        if (tabs.size == 1) {
            val old = tabs[0]
            container.removeAllViews()
            tabs.clear()
            wipeIfIncognito(old); thumbs.remove(old); old.destroy()
            cur = -1; newTab("home")
            return
        }
        val wasCur = (i == cur)
        val w = tabs[i]
        if (wasCur) container.removeAllViews()
        tabs.removeAt(i)
        wipeIfIncognito(w); thumbs.remove(w); w.destroy()
        if (wasCur) {
            switchTo(minOf(i, tabs.size - 1))
        } else {
            if (i < cur) cur--
            tabsBtn.count = tabs.size
            tabsBtn.invalidate()
        }
    }
}
