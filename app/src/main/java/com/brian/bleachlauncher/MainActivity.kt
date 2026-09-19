package com.brian.bleachlauncher

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.*
import android.webkit.*
import android.widget.*
import android.graphics.Color
import android.graphics.drawable.GradientDrawable

class MainActivity : Activity() {
    private lateinit var web: WebView
    private val gameUrl = "http://www.plaync100.net/web.php"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)

        val root = FrameLayout(this)
        web = WebView(this)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.databaseEnabled = true
        web.settings.mediaPlaybackRequiresUserGesture = false
        web.settings.allowFileAccess = true
        web.settings.allowContentAccess = true
        web.settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        web.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) { injectRuffle() }
        }
        web.webChromeClient = WebChromeClient()
        root.addView(web, FrameLayout.LayoutParams(-1,-1))
        addControls(root)
        setContentView(root)
        web.loadUrl(gameUrl)
    }

    private fun injectRuffle() {
        val js = """
            (function(){
              if(window.__bleachRuffleInjected)return; window.__bleachRuffleInjected=true;
              var s=document.createElement('script');
              s.src='https://unpkg.com/@ruffle-rs/ruffle';
              s.onload=function(){try{if(window.RufflePlayer)window.RufflePlayer.config={letterbox:'on',autoplay:'on',unmuteOverlay:'hidden',warnOnUnsupportedContent:false};}catch(e){}};
              document.head.appendChild(s);
            })();
        """.trimIndent()
        web.evaluateJavascript(js,null)
    }

    private fun addControls(root: FrameLayout) {
        val panel = LinearLayout(this); panel.orientation=LinearLayout.HORIZONTAL; panel.setPadding(8,8,8,8)
        panel.setBackgroundColor(0x66000000)
        val lp=FrameLayout.LayoutParams(-2,64); lp.gravity=Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        val keys=listOf("◀","▲","▼","▶","ENTER","SPACE","ESC")
        for(k in keys){
            val b=Button(this); b.text=k; b.textSize=10f; b.setTextColor(Color.WHITE)
            b.background=GradientDrawable().apply{setColor(0x99000000.toInt());cornerRadius=12f;setStroke(1,Color.WHITE)}
            b.setOnClickListener{ sendKey(k) }
            panel.addView(b,LinearLayout.LayoutParams(if(k.length>1)110 else 70,60).apply{setMargins(3,0,3,0)})
        }
        root.addView(panel,lp)
    }

    private fun sendKey(k:String){
        val key=when(k){"◀"->37;"▲"->38;"▶"->39;"▼"->40;"ENTER"->13;"SPACE"->32;"ESC"->27;else->0}
        web.evaluateJavascript("window.dispatchEvent(new KeyboardEvent('keydown',{keyCode:$key,which:$key,key:'$k',bubbles:true}));",null)
    }
    override fun onBackPressed(){ if(web.canGoBack()) web.goBack() else super.onBackPressed() }
}
