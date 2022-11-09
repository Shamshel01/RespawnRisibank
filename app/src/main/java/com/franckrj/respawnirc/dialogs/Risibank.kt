package com.franckrj.respawnirc.dialogs

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import kotlin.properties.Delegates


@SuppressLint("JavascriptInterface")
class  Risibank (var wv: WebView) {
    private var risibankWebview = wv
    var selectedMedia: String? = null
    private val dataHtml = "file:///android_asset/RisibankWeb.html"
    //private val dataHtml =  "https://risibank.fr/"

    init {
        val webSettings: WebSettings = risibankWebview.settings
        if (webSettings != null) {
            webSettings.javaScriptEnabled = true
            webSettings.domStorageEnabled = true;
        }
        risibankWebview.webViewClient = WebViewClient()
        risibankWebview.addJavascriptInterface(this, "Android");
        risibankWebview.loadUrl(dataHtml)
    }


    @JavascriptInterface
    fun setSticker(selectedSticker: String) {
        selectedMedia = selectedSticker
    }

    fun getClicked() {
        risibankWebview.visibility = View.VISIBLE;
    }

    fun getUnclicked() {
        risibankWebview.visibility = View.GONE;
    }
}