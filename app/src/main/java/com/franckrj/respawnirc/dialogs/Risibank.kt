package com.franckrj.respawnirc.dialogs

import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient


class Risibank (var wv: WebView) {
    private var risibankWebview = wv
    private var selectedMedia = null
    private val dataHtml =  "file:///android_asset/RisibankWeb.html"

    init {
        println("First initializer block that prints ")

        risibankWebview.webViewClient = WebViewClient()
        risibankWebview.isClickable = true
        risibankWebview.webChromeClient = WebChromeClient()
        val webSettings: WebSettings = risibankWebview.settings
        if (webSettings != null) {
            webSettings.javaScriptEnabled = true
        }
        risibankWebview.isClickable = true;
        risibankWebview.loadUrl(dataHtml)

        risibankWebview.isScrollbarFadingEnabled = true;


    }


    fun getClicked() {
        risibankWebview.visibility = View.VISIBLE;
    }

    fun getUnclicked() {
        risibankWebview.visibility = View.GONE;
    }
}