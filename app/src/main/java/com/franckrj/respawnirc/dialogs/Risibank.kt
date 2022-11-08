package com.franckrj.respawnirc.dialogs

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient


@SuppressLint("JavascriptInterface")
class Risibank (var wv: WebView) {
    private var risibankWebview = wv
    private var selectedMedia = null
    private val dataHtml =  "file:///android_asset/RisibankWeb.html"

    init {
        println("First initializer block that prints ")


        val webSettings: WebSettings = risibankWebview.settings
        if (webSettings != null) {
            webSettings.javaScriptEnabled = true
        }
        risibankWebview.webViewClient = WebViewClient()
        risibankWebview.addJavascriptInterface(JavascriptInterface(), "RisibankWeb");
        risibankWebview.loadUrl(dataHtml)

    }


    private inner class JavascriptInterface
    {
        @android.webkit.JavascriptInterface
        fun inputWebview(text: String?)
        {
            if (text != null) {
                Log.d("WEBVIEW", text)
            };
        }
    }

    fun getClicked() {
        risibankWebview.visibility = View.VISIBLE;
    }

    fun getUnclicked() {
        risibankWebview.visibility = View.GONE;
    }
}