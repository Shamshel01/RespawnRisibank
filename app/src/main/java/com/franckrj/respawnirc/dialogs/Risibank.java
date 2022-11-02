package com.franckrj.respawnirc.dialogs;



import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;

import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.base.AbsToolbarActivity;
import com.franckrj.respawnirc.utils.AccountManager;
import com.franckrj.respawnirc.utils.PrefsManager;
import com.franckrj.respawnirc.utils.Undeprecator;
import com.franckrj.respawnirc.utils.Utils;

public class Risibank extends AbsToolbarActivity {
    private WebView risibankWebView = null;
    private String currentUrl = "https://www.google.fr/";
    private String currentTitle = "";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        risibankWebView = findViewById(R.id.risibank_webview);
        risibankWebView.setWebViewClient(new WebViewClient());
       // risibankWebView.getSettings().setJavaScriptEnabled(true);
        setContentView(R.layout.dialog_insertstuff);

        risibankWebView.loadUrl(currentUrl);
    }
    public void Clicked() {
        risibankWebView.setVisibility(View.VISIBLE);
    }
    public void Unclicked() {
        risibankWebView.setVisibility(View.INVISIBLE);
    }
}