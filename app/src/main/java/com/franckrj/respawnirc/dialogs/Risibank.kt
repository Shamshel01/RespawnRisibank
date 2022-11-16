package com.franckrj.respawnirc.dialogs

import android.annotation.SuppressLint
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient



@SuppressLint("JavascriptInterface")
class  Risibank () {
    private var selectedMedia: String? = null

    init {

    }

    fun getSelectedMedia(): String? {
        return selectedMedia
    }
    fun SetSelectedMedia(str : String){
         selectedMedia = str
    }

    fun getClicked() {

    }

    fun getUnclicked() {

    }
}