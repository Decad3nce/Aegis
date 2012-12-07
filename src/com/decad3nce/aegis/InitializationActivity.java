package com.decad3nce.aegis;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class InitializationActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.initialization_activity);

        WebView mWebView;
        mWebView = (WebView) findViewById(R.id.web_view);
        mWebView.loadUrl("file:///android_asset/initialization.html");
    }
}
