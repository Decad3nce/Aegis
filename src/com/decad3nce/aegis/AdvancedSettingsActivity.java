package com.decad3nce.aegis;

import com.decad3nce.aegis.Fragments.AdvancedSettingsFragment;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class AdvancedSettingsActivity extends PreferenceActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         getFragmentManager().beginTransaction().replace(android.R.id.content,
         new AdvancedSettingsFragment()).commit();
    }
}
