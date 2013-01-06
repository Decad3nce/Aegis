package com.decad3nce.aegis;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends Activity {
    private static final String TAG = "aeGis";

    public static final String PREFERENCES_AEGIS_PASSWORD_SET = "password_set";
    public static final String PREFERENCES_PASSWORD_WANTED = "password_wanted";
    public static final String PREFERENCES_CURRENT_PASSWORD = "current_password";

    EditText mPassword;
    EditText mPasswordConfirm;
    private String mCurrentPassword;
    private static boolean mPasswordSet;
    private static boolean mPasswordWanted;
    private static boolean mFromAegis = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set View to register.xml
        setContentView(R.layout.register);

        Intent intent = getIntent();

        if (intent.hasExtra("fromAegis")) {
            mFromAegis = true;
            getActionBar().setDisplayHomeAsUpEnabled(true);
        } else {
            getActionBar().setHomeButtonEnabled(false);
        }

        final SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);

        mCurrentPassword = preferences.getString(
                PREFERENCES_CURRENT_PASSWORD,
                this.getResources().getString(
                        R.string.config_default_login_password));

        mPasswordWanted = preferences.getBoolean(
                RegisterActivity.PREFERENCES_PASSWORD_WANTED,
                this.getResources().getBoolean(
                        R.bool.config_default_password_wanted));

        mPassword = (EditText) findViewById(R.id.reg_password);
        mPasswordConfirm = (EditText) findViewById(R.id.reg_password_confirm);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        Button registerScreen = (Button) findViewById(R.id.btnRegister);
        registerScreen.getBackground().setAlpha(255);
        CheckBox checkBox = (CheckBox) findViewById(R.id.disablePassword);
        checkBox.setChecked(!mPasswordWanted);

        registerScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                case R.id.btnRegister:
                    if (mPasswordWanted) {
                        String mPasswordText = mPassword.getText().toString();
                        String mPasswordConfirmText = mPasswordConfirm
                                .getText().toString();

                        if (mPasswordConfirmText.equals(mPasswordText)
                                && (!mPasswordText.isEmpty() || !mPasswordConfirmText.isEmpty())) {
                            mCurrentPassword = mPasswordText;
                            mPasswordSet = true;

                            Toast.makeText(
                                    getApplicationContext(),
                                    getResources()
                                            .getString(
                                                    R.string.register_password_toast_password_set),
                                    Toast.LENGTH_LONG).show();
                            startAegis();
                        } else if (mPasswordConfirmText.equals(mPasswordText)
                                && (mPasswordText.isEmpty() || mPasswordConfirmText.isEmpty())) {
                            Toast.makeText(
                                    getApplicationContext(),
                                    getResources()
                                            .getString(
                                                    R.string.register_password_toast_password_fail),
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(
                                    getApplicationContext(),
                                    getResources()
                                            .getString(
                                                    R.string.register_password_toast_password_match_fail),
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        startAegis();
                    }

                }

            }
        });
    }
    
    private void startAegis() {
        if (mFromAegis) {
            finish();
        } else {
            Intent aeGisIntent = new Intent(RegisterActivity.this,
                    AegisActivity.class);
            aeGisIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            aeGisIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(aeGisIntent);
            finish();
        }
    }

    public void onCheckboxClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();     
        switch (view.getId()) {
        case R.id.disablePassword:
            if (checked) {
                mPasswordWanted = false;
            } else {
                mPasswordWanted = true;
            }
            break;
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            if (mFromAegis) {
                Intent parentActivityIntent = new Intent(this,
                        AegisActivity.class);
                parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(parentActivityIntent);
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        saveSettings();
    }
    
    protected void saveSettings() {
        final SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("current_password", mCurrentPassword);
        editor.putBoolean("password_set", mPasswordSet);
        editor.putBoolean("password_wanted", mPasswordWanted);
        editor.commit();
    }

}
