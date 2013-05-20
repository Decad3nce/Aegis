package com.decad3nce.aegis.Fragments;

import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View.OnClickListener;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import com.decad3nce.aegis.R;

public class AboutFragment extends Fragment implements OnClickListener {
    private Button onClickEmail, onClickGithub, onClickShare, onClickXDA;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.about_layout, container, false);
        String version = "UKNOWN";

        try {
            version = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            //Shouldn't happen
        }
        TextView tV;
        tV = (TextView) mainView.findViewById(R.id.currentversion);
        onClickEmail = (Button) mainView.findViewById(R.id.emailclick);
        onClickGithub = (Button) mainView.findViewById(R.id.githubclick);
        onClickShare = (Button) mainView.findViewById(R.id.shareclick);
        onClickXDA = (Button) mainView.findViewById(R.id.xdaclick);
        onClickEmail.setOnClickListener(this);
        onClickGithub.setOnClickListener(this);
        onClickShare.setOnClickListener(this);
        onClickXDA.setOnClickListener(this);
        tV.setText(version);
        return mainView;
    }

    @Override
    public void onClick(View v) {
        Intent i;
        String url;
        switch (v.getId()) {
            case R.id.emailclick:
                final Intent emailIntent = new Intent(
                        android.content.Intent.ACTION_SEND);

                emailIntent.setType("plain/text");
                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
                        new String[] { "decad3nce@cyanogenmod.org" });
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                        "aeGis Questions");
                startActivity(Intent.createChooser(emailIntent, "Email"));
                break;
            case R.id.githubclick:
                url = "http://www.github.com/Decad3nce/Aegis";
                i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                break;
            case R.id.shareclick:
                i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, "aeGis");
                String mInfo = "\nCheck out aeGis!\n\n";
                mInfo = mInfo + "https://play.google.com/store/apps/details?id=com.decad3nce.aegis \n\n";
                i.putExtra(Intent.EXTRA_TEXT, mInfo);
                startActivity(Intent.createChooser(i, "Choose App"));
                break;
            case R.id.xdaclick:
                url = "http://forum.xda-developers.com/showthread.php?t=2038762";
                i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                break;
        }
    }

}
