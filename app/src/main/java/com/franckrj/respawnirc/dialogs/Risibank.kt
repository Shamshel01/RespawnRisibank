package com.franckrj.respawnirc.dialogs

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import com.franckrj.respawnirc.R


class Risibank (var wv: WebView) {
    private val risibankWebview = wv

    init {
        println("First initializer block that prints ")
        risibankWebview.loadUrl("http:\\www.google.com")
        risibankWebview.setWebViewClient(object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }
        })
       // risibankWebview.visibility = View.INVISIBLE;
    }


    fun getClicked() {
        risibankWebview.visibility = View.VISIBLE;
    }

    fun getUnclicked() {
        risibankWebview.visibility = View.GONE;
    }
}