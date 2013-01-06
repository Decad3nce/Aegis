package com.decad3nce.aegis;

import com.decad3nce.aegis.Fragments.AdvancedSettingsFragment;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

public class AdvancedSettingsActivity extends PreferenceActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         getActionBar().setDisplayHomeAsUpEnabled(true);
        
         getFragmentManager().beginTransaction().replace(android.R.id.content,
         new AdvancedSettingsFragment()).commit();
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
