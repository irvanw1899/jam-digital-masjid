package com.masjid.jamdigital

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private val mainHandler = Handler(Looper.getMainLooper())

    // Reload preventif terjadwal (bukan watchdog buta tiap-sekian-menit,
    // supaya tidak mengganggu tampilan jam yang sedang berjalan).
    private val dailyReloadHour = 3
    private val dailyReloadMinute = 30
    private var lastReloadDateKey: String? = null

    companion object {
        // Ganti sesuai URL & parameter masjid kamu.
        const val HOME_URL =
            "https://irvanw1899.github.io/jam-digital-masjid/?masjid=basmatuiliman-001"
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Layar TV tidak pernah tidur selama app ini di depan.
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        hideSystemUI()

        webView = createWebView()
        setContentView(webView)
        webView.loadUrl(HOME_URL)

        startDailyReloadWatchdog()
    }

    private fun createWebView(): WebView {
        val wv = WebView(this)

        wv.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true // wajib: app pakai localStorage utk pengaturan & ID masjid
            databaseEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            mediaPlaybackRequiresUserGesture = false // supaya live streaming bisa autoplay
            @Suppress("DEPRECATION")
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        }

        wv.webChromeClient = WebChromeClient()

        wv.webViewClient = object : WebViewClient() {
            // INTI dari perbaikan "hang": kalau proses render WebView
            // benar-benar crash/freeze, Android memanggil callback ini.
            // Kita buang WebView lama & buat baru secara otomatis —
            // sesuatu yang tidak tersedia di browser/kiosk app biasa.
            override fun onRenderProcessGone(
                view: WebView?,
                detail: RenderProcessGoneDetail?
            ): Boolean {
                (view?.parent as? ViewGroup)?.removeView(view)
                view?.destroy()
                mainHandler.postDelayed({ recreate() }, 1000)
                return true
            }
        }

        return wv
    }

    private fun startDailyReloadWatchdog() {
        mainHandler.postDelayed(object : Runnable {
            override fun run() {
                val cal = Calendar.getInstance()
                val key = "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.DAY_OF_YEAR)}"
                val isTargetTime = cal.get(Calendar.HOUR_OF_DAY) == dailyReloadHour &&
                    cal.get(Calendar.MINUTE) == dailyReloadMinute
                if (isTargetTime && lastReloadDateKey != key && !isFinishing) {
                    lastReloadDateKey = key
                    webView.reload()
                }
                mainHandler.postDelayed(this, 30_000) // cek tiap 30 detik
            }
        }, 30_000)
    }

    private fun hideSystemUI() {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
            )
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    // Cegah tombol Back di remote keluar dari app (perilaku khas kiosk),
    // supaya petugas tidak bisa tidak sengaja menutup jamnya.
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) return true
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        mainHandler.removeCallbacksAndMessages(null)
        webView.destroy()
        super.onDestroy()
    }
}
