package com.decad3nce.aegis.Fragments;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.app.Fragment;
import android.os.Bundle;
import android.webkit.WebView;
import android.view.View;
import com.decad3nce.aegis.R;

public class LicensesFragment extends Fragment {
    WebView mWebView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState) {

        View mainView = inflater.inflate(R.layout.licenses_activity, container, false);

        mWebView = (WebView) mainView.findViewById(R.id.web_view);
        mWebView.loadUrl("file:///android_asset/licenses.html");
        return mainView;
    }

    @Override
    public void onStop() {
        super.onStop();
        mWebView.clearCache(true);
    }
}
