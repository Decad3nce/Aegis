package com.decad3nce.aegis;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.app.SherlockActivity;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

public class InitializationActivity extends SherlockActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.initialization_activity);
        
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        WebView mWebView;
        mWebView = (WebView) findViewById(R.id.web_view);
        mWebView.loadUrl("file:///android_asset/initialization.html");
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent parentActivityIntent = new Intent(this, AegisActivity.class);
                parentActivityIntent.addFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(parentActivityIntent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
