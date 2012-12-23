package com.decad3nce.aegis;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

public class AboutActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstances) {
        super.onCreate(savedInstances);
        setContentView(R.layout.about_layout);
    }

    public void onGithubClick(View v) {
        String url = "http://www.github.com/Decad3nce/Aegis";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    public void onEmailClick(View v) {
        final Intent emailIntent = new Intent(
                android.content.Intent.ACTION_SEND);

        emailIntent.setType("plain/text");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
                new String[] { "decad3nce@cyanogenmod.org" });
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                "aeGis Questions");
        startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }

}
