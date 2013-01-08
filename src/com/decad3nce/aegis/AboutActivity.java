package com.decad3nce.aegis;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class AboutActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstances) {
        super.onCreate(savedInstances);
        setContentView(R.layout.about_layout);      
        String version = "UKNOWN";

        try {
            version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            //Shouldn't happen
        }
        
        getActionBar().setDisplayHomeAsUpEnabled(true);
        TextView tV;
        tV = (TextView)findViewById(R.id.currentversion);  
        tV.setText(version);
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

    public void onGithubClick(View v) {
        String url = "http://www.github.com/Decad3nce/Aegis";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }
    
    public void onShareClick(View v) {
        try
        { Intent i = new Intent(Intent.ACTION_SEND);  
          i.setType("text/plain");
          i.putExtra(Intent.EXTRA_SUBJECT, "aeGis");
          String mInfo = "\nCheck out aeGis!\n\n";
          mInfo = mInfo + "https://play.google.com/store/apps/details?id=com.decad3nce.aegis \n\n";
          i.putExtra(Intent.EXTRA_TEXT, mInfo);  
          startActivity(Intent.createChooser(i, "Choose App"));
        }
        catch(Exception e)
        { //e.toString();
        }   
    }
    
    public void onXDAClick(View v) {
        String url = "http://forum.xda-developers.com/showthread.php?t=2038762";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

}
