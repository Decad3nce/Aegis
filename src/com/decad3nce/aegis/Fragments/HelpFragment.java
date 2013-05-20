package com.decad3nce.aegis.Fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import com.decad3nce.aegis.R;

public class HelpFragment extends Fragment {
    WebView mWebView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View mainView = inflater.inflate(R.layout.help_activity, container, false);

        mWebView = (WebView) mainView.findViewById(R.id.web_view);
        mWebView.loadUrl("file:///android_asset/help.html");
        return mainView;
    }

    @Override
    public void onStop() {
        super.onStop();
        mWebView.clearCache(true);
    }
}
