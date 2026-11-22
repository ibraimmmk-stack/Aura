package com.aura.home

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.BatteryManager
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class MainActivity : Activity() {
    private lateinit var web: WebView

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        window.statusBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        web = WebView(this)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.setBackgroundColor(Color.TRANSPARENT)
        web.addJavascriptInterface(Bridge(), "Android")
        web.loadUrl("file:///android_asset/index.html")
        setContentView(web)
    }

    override fun onNewIntent(i: Intent?) {
        super.onNewIntent(i)
        web.evaluateJavascript("closeSheets()", null)
    }

    @Deprecated("Launcher: le retour ferme seulement les panneaux")
    override fun onBackPressed() {
        web.evaluateJavascript("closeSheets()", null)
    }

    private fun icon(d: Drawable): String {
        val s = 96
        val bm = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_8888)
        d.setBounds(0, 0, s, s)
        d.draw(Canvas(bm))
        val o = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.PNG, 100, o)
        return Base64.encodeToString(o.toByteArray(), Base64.NO_WRAP)
    }

    inner class Bridge {
        @JavascriptInterface
        fun getApps(): String {
            val i = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            val arr = JSONArray()
            for (r in packageManager.queryIntentActivities(i, 0)) {
                if (r.activityInfo.packageName == packageName) continue
                arr.put(
                    JSONObject()
                        .put("n", r.loadLabel(packageManager).toString())
                        .put("p", r.activityInfo.packageName)
                        .put("i", icon(r.loadIcon(packageManager)))
                )
            }
            return arr.toString()
        }

        @JavascriptInterface
        fun launch(p: String) {
            packageManager.getLaunchIntentForPackage(p)?.let { runOnUiThread { startActivity(it) } }
        }

        @JavascriptInterface
        fun getBattery(): Int =
            (getSystemService(BATTERY_SERVICE) as BatteryManager)
                .getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }
}
