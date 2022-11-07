package com.franckrj.respawnirc.dialogs

import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient


class Risibank (var wv: WebView) {
    private var risibankWebview = wv
    private var selectedMedia = null
    private val dataHtml =  "file:///android_asset/RisibankWeb.html"

    init {
        println("First initializer block that prints ")

        risibankWebview.setWebViewClient(object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }
        })
        val webSettings: WebSettings = risibankWebview.getSettings()
        if (webSettings != null) {
            webSettings.javaScriptEnabled = true
        }
        risibankWebview.loadUrl(dataHtml)
    }


    fun getClicked() {
        risibankWebview.visibility = View.VISIBLE;
    }

    fun getUnclicked() {
        risibankWebview.visibility = View.GONE;
    }
}