package com.vortex.browser

fun buildHomeHtml(showGreeting: Boolean): String {
    val greetDiv = if (showGreeting) "<div id=\"clock\" class=\"a2\"></div>" else ""
    return """
<!DOCTYPE html><html><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>Vortex</title>
<style>
*{box-sizing:border-box;margin:0;padding:0;-webkit-tap-highlight-color:transparent}
html,body{overscroll-behavior:none}
body{font-family:sans-serif;color:#fff;background:#07070c;min-height:100vh;overflow-x:hidden}
.waves{position:fixed;left:0;top:0;width:100%;height:100%;z-index:0;pointer-events:none}
.wrap{position:relative;z-index:1;display:flex;flex-direction:column;align-items:center;padding:42px 16px 40px}
.glass{background:rgba(255,255,255,0.08);border:1px solid rgba(255,255,255,.15);box-shadow:0 8px 24px rgba(0,0,0,.35)}
@keyframes up{from{opacity:0;transform:translateY(16px)}to{opacity:1;transform:none}}
.a1{animation:up .6s .02s both}.a2{animation:up .6s .10s both}.a3{animation:up .6s .18s both}.a4{animation:up .6s .26s both}.a5{animation:up .6s .34s both}
.logo{font-size:30px;font-weight:500;letter-spacing:15px;padding-left:15px;color:#f1f1fa}
.logo span{color:#9a6bff}
.ul{width:60px;height:3px;border-radius:2px;background:linear-gradient(90deg,#7c5cff,#b18cff);margin-top:12px}
#clock{margin-top:16px;font-size:17px;color:#a9a9c4}
.sb{margin-top:26px;width:100%;max-width:520px;height:58px;border-radius:29px;display:flex;align-items:center;padding:0 10px 0 16px;gap:12px}
.sb .ph{flex:1;font-size:17px;color:rgba(255,255,255,.55)}
.sb svg{flex:none}
.sb .dv{width:1px;height:24px;background:rgba(255,255,255,.15)}
.sb .ic{display:flex;align-items:center;justify-content:center;width:36px;height:36px}
.grid{margin-top:30px;display:grid;grid-template-columns:repeat(4,1fr);gap:20px 6px;width:100%;max-width:520px}
.sc{display:flex;flex-direction:column;align-items:center;gap:9px;color:#dcdcec;text-decoration:none;font-size:13px;transition:transform .15s}
.sc:active{transform:scale(.92)}
.sc i{width:58px;height:58px;border-radius:50%;display:flex;align-items:center;justify-content:center}
.sc svg{width:28px;height:28px}
.card{margin-top:30px;width:100%;max-width:520px;border-radius:26px;padding:6px 16px 12px}
.ch{display:flex;align-items:center;gap:12px;padding:12px 0}
.ch b{flex:1;font-weight:500;font-size:19px}
.ib{display:flex;align-items:center;justify-content:center;width:34px;height:34px;color:#c9c9de;text-decoration:none}
@keyframes sp{to{transform:rotate(360deg)}}
.spin svg{animation:sp .9s linear infinite}
.it{display:block;padding:14px 2px;border-top:1px solid rgba(255,255,255,.08);color:#fff;text-decoration:none}
.it b{display:block;font-weight:500;font-size:15.5px;line-height:1.4}
.meta{margin-top:9px;font-size:12px;color:#9a9ab5;display:flex;align-items:center;gap:8px}
.meta span:nth-child(2){flex:1}
.av{width:20px;height:20px;border-radius:50%;background:rgba(154,134,255,.25);color:#cbbcff;display:flex;align-items:center;justify-content:center;font-size:11px}
.shr{display:flex;width:30px;height:24px;align-items:center;justify-content:flex-end;font-style:normal}
@keyframes sh{0%{opacity:.35}50%{opacity:.7}100%{opacity:.35}}
.sk{height:56px;border-radius:12px;background:rgba(255,255,255,.08);margin:10px 0;animation:sh 1.3s infinite}
.err{padding:22px 4px;color:#9a9ab5;font-size:14px;text-align:center}
</style></head><body>
<svg class="waves" viewBox="0 0 400 800" preserveAspectRatio="none">
<defs>
<linearGradient id="g1" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#7c5cff" stop-opacity="0"/><stop offset="1" stop-color="#7c5cff" stop-opacity=".38"/></linearGradient>
<linearGradient id="g2" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#9a6bff" stop-opacity="0"/><stop offset="1" stop-color="#9a6bff" stop-opacity=".24"/></linearGradient>
<linearGradient id="fl" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#b18cff"/><stop offset="1" stop-color="#6a3cff"/></linearGradient>
</defs>
<path d="M-20,130 C80,70 200,180 420,50 L420,-20 L-20,-20 Z" fill="url(#g1)"/>
<path d="M-20,200 C110,120 240,220 420,110 L420,-20 L-20,-20 Z" fill="url(#g2)"/>
<path d="M-20,690 C100,650 230,750 420,670 L420,820 L-20,820 Z" fill="url(#g1)"/>
<path d="M-20,745 C130,700 250,790 420,730 L420,820 L-20,820 Z" fill="url(#g2)"/>
</svg>
<div class="wrap">
<div class="logo a1">VORTE<span>X</span></div>
<div class="ul a1"></div>
$greetDiv
<div class="sb glass a3" onclick="location.href='vortex://search'">
<svg width="26" height="26" viewBox="0 0 48 48"><path fill="#EA4335" d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z"/><path fill="#4285F4" d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z"/><path fill="#FBBC05" d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z"/><path fill="#34A853" d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15 1.45-4.92 2.3-8.16 2.3-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z"/></svg>
<span class="ph">Search or enter URL</span>
<span class="ic" onclick="event.stopPropagation();location.href='vortex://voice'"><svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2" stroke-linecap="round"><rect x="9" y="2" width="6" height="12" rx="3"/><path d="M5 11a7 7 0 0 0 14 0M12 18v4M8 22h8"/></svg></span>
<span class="dv"></span>
<span class="ic" onclick="event.stopPropagation();location.href='https://lens.google.com/'"><svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 8V5a1 1 0 0 1 1-1h3M16 4h3a1 1 0 0 1 1 1v3M20 16v3a1 1 0 0 1-1 1h-3M8 20H5a1 1 0 0 1-1-1v-3"/><circle cx="12" cy="12" r="3.5"/></svg></span>
</div>
<div class="grid a4">
<a class="sc" href="https://m.youtube.com"><i class="glass"><svg viewBox="0 0 24 24"><rect x="1" y="4.5" width="22" height="15" rx="4.5" fill="#FF0000"/><path d="M10 8.8l5.2 3.2L10 15.2z" fill="#fff"/></svg></i>YouTube</a>
<a class="sc" href="https://www.google.com"><i class="glass"><svg viewBox="0 0 48 48"><path fill="#EA4335" d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z"/><path fill="#4285F4" d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z"/><path fill="#FBBC05" d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z"/><path fill="#34A853" d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15 1.45-4.92 2.3-8.16 2.3-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z"/></svg></i>Google</a>
<a class="sc" href="https://github.com"><i class="glass"><svg viewBox="0 0 16 16" fill="#fff"><path d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.013 8.013 0 0016 8c0-4.42-3.58-8-8-8z"/></svg></i>GitHub</a>
<a class="sc" href="https://m.facebook.com"><i class="glass"><svg viewBox="0 0 24 24"><circle cx="12" cy="12" r="12" fill="#1877F2"/><path d="M13.5 22v-8h2.7l.5-3.2h-3.2V8.7c0-.9.4-1.7 1.8-1.7h1.5V4.2S15.9 4 14.7 4c-2.5 0-4.2 1.5-4.2 4.3v2.5H7.8V14h2.7v8z" fill="#fff"/></svg></i>Facebook</a>
<a class="sc" href="https://en.wikipedia.org"><i class="glass"><svg viewBox="0 0 24 24"><text x="12" y="18.5" text-anchor="middle" font-family="serif" font-weight="bold" font-size="20" fill="#fff">W</text></svg></i>Wikipedia</a>
<a class="sc" href="https://x.com"><i class="glass"><svg viewBox="0 0 24 24" fill="#fff"><path d="M18.244 2.25h3.308l-7.227 8.26 8.502 11.24H16.17l-5.214-6.817L4.99 21.75H1.68l7.73-8.835L1.254 2.25H8.08l4.713 6.231zm-1.161 17.52h1.833L7.084 4.126H5.117z"/></svg></i>X</a>
<a class="sc" href="https://www.reddit.com"><i class="glass"><svg viewBox="0 0 24 24"><circle cx="12" cy="12" r="12" fill="#FF4500"/><circle cx="5.4" cy="11.6" r="1.7" fill="#fff"/><circle cx="18.6" cy="11.6" r="1.7" fill="#fff"/><ellipse cx="12" cy="14" rx="6.6" ry="4.6" fill="#fff"/><circle cx="9.6" cy="13.4" r="1.2" fill="#FF4500"/><circle cx="14.4" cy="13.4" r="1.2" fill="#FF4500"/><circle cx="17.6" cy="5.6" r="1.3" fill="#fff"/><path d="M12 9.5l.9-4.1 3.6.8" stroke="#fff" stroke-width="1" fill="none"/></svg></i>Reddit</a>
<a class="sc" href="https://mail.google.com"><i class="glass"><svg viewBox="0 0 24 24" fill="none" stroke-linecap="round" stroke-linejoin="round"><path d="M3.5 7l8.5 6.2L20.5 7" stroke="#EA4335" stroke-width="3"/><path d="M3.5 7v11" stroke="#4285F4" stroke-width="3"/><path d="M20.5 7v11" stroke="#34A853" stroke-width="3"/></svg></i>Gmail</a>
</div>
<div class="card glass a5">
<div class="ch">
<svg width="26" height="26" viewBox="0 0 24 24"><path d="M12 2c1 3-1 4.5-2.5 6.5C8 10.5 7 12 7 14.5A5 5 0 0 0 17 14.5c0-2-1-3.2-2-4.5 0 1.2-.6 2-1.4 2.4C14 9.5 13.5 5 12 2z" fill="url(#fl)"/></svg>
<b>Discover</b>
<a class="ib" id="rf" href="vortex://feed" onclick="this.classList.add('spin')"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 12a8 8 0 1 1-2.3-5.6"/><path d="M20 4v5h-5"/></svg></a>
<a class="ib" href="vortex://feedlang"><svg width="22" height="22" viewBox="0 0 24 24" fill="currentColor"><circle cx="12" cy="5" r="1.9"/><circle cx="12" cy="12" r="1.9"/><circle cx="12" cy="19" r="1.9"/></svg></a>
</div>
<div id="feed"><div class="sk"></div><div class="sk"></div><div class="sk"></div></div>
</div>
</div>
<script>
function greet(h){
  if(h>=5&&h<12) return 'Good morning';
  if(h>=12&&h<17) return 'Good afternoon';
  if(h>=17&&h<20) return 'Good evening';
  return 'Good night';
}
function tick(){
  var c=document.getElementById('clock');
  if(!c) return;
  var d=new Date();
  c.textContent=greet(d.getHours())+' | '+d.toLocaleTimeString('en-US',{hour:'2-digit',minute:'2-digit'});
}
tick(); setInterval(tick,20000);
function ago(ms){
  var m=Math.floor((Date.now()-ms)/60000);
  if(m<1) return 'now';
  if(m<60) return m+'m ago';
  var h=Math.floor(m/60);
  if(h<24) return h+'h ago';
  return Math.floor(h/24)+'d ago';
}
var SHARE='<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#9a9ab5" stroke-width="2" stroke-linecap="round"><circle cx="6" cy="12" r="2.4"/><circle cx="18" cy="6" r="2.4"/><circle cx="18" cy="18" r="2.4"/><path d="M8.2 11L15.8 7M8.2 13L15.8 17"/></svg>';
window.vxFeed=function(json){
  var items;
  try{ items=JSON.parse(json); }catch(e){ return; }
  var f=document.getElementById('feed');
  f.innerHTML='';
  var rf=document.getElementById('rf'); if(rf) rf.classList.remove('spin');
  items.forEach(function(x){
    var a=document.createElement('a'); a.className='it'; a.href=x.l;
    var b=document.createElement('b'); b.textContent=x.t; a.appendChild(b);
    var m=document.createElement('div'); m.className='meta';
    var av=document.createElement('span'); av.className='av'; av.textContent=(x.s||'?').charAt(0).toUpperCase(); m.appendChild(av);
    var sp=document.createElement('span'); sp.textContent=(x.s||'')+(x.m?' \u00b7 '+ago(x.m):''); m.appendChild(sp);
    var sh=document.createElement('i'); sh.className='shr'; sh.innerHTML=SHARE;
    sh.onclick=function(ev){ ev.preventDefault(); ev.stopPropagation(); location.href='vortex://share?u='+encodeURIComponent(x.l); };
    m.appendChild(sh); a.appendChild(m); f.appendChild(a);
  });
};
window.vxFeedErr=function(){
  var rf=document.getElementById('rf'); if(rf) rf.classList.remove('spin');
  var f=document.getElementById('feed');
  if(f.querySelector('.sk')) f.innerHTML='<div class="err">Could not load news. Pull down to retry.</div>';
};
</script></body></html>
"""
}
