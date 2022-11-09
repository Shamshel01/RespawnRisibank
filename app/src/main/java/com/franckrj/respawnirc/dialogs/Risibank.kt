package com.franckrj.respawnirc.dialogs

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast


@SuppressLint("JavascriptInterface")
class Risibank (var wv: WebView) {
    private var risibankWebview = wv
    private var selectedMedia: String? = null
    private val dataHtml =  "file:///android_asset/RisibankWeb.html"
    //private val dataHtml =  "https://risibank.fr/"

    init {

        val webSettings: WebSettings = risibankWebview.settings
        if (webSettings != null) {
            webSettings.javaScriptEnabled = true
            webSettings.domStorageEnabled = true;
        }
        risibankWebview.webViewClient = WebViewClient()
        risibankWebview.addJavascriptInterface(JavascriptInterface(), "RisibankWeb");
        risibankWebview.loadUrl(dataHtml)
        Log.i("TEST", "ISSSSSSSSSSSSSOUUUU")

    }


    @JavascriptInterface
    fun SelectedSticker(selectedSticker :String) {
        selectedMedia = selectedSticker
        Log.i("TEST", selectedSticker)

    }

    fun getClicked() {
        risibankWebview.visibility = View.VISIBLE;
    }

    fun getUnclicked() {
        risibankWebview.visibility = View.GONE;
    }
}