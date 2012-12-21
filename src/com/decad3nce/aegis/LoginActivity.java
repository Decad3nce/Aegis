package com.decad3nce.aegis;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {
    private static boolean mPasswordSet;
    private String mCurrentPassword;
    EditText mPassword;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set View to login.xml
        setContentView(R.layout.login);

        final SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        mCurrentPassword = preferences.getString(
                RegisterActivity.PREFERENCES_CURRENT_PASSWORD,
                this.getResources().getString(
                        R.string.config_default_login_password));

        mPasswordSet = preferences.getBoolean(
                RegisterActivity.PREFERENCES_AEGIS_PASSWORD_SET,
                this.getResources().getBoolean(
                        R.bool.config_default_password_set));

        if (!mPasswordSet) {
            mPasswordSet = true;
            Intent registerIntent = new Intent(LoginActivity.this,
                    RegisterActivity.class);
            registerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            registerIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(registerIntent);
            finish();
        }

        mPassword = (EditText) findViewById(R.id.login_password);
        Button loginScreen = (Button) findViewById(R.id.btnLogin);

        // Listening to Login Screen button
        loginScreen.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                if (mPassword == null) {
                    Toast.makeText(getApplicationContext(),
                            "Please enter the password", Toast.LENGTH_LONG)
                            .show();
                }

                String mPasswordText = mPassword.getText().toString();

                if (mPasswordText.equals(mCurrentPassword)) {
                    Intent aeGisIntent = new Intent(LoginActivity.this,
                            AegisActivity.class);
                    aeGisIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    aeGisIntent
                            .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    startActivity(aeGisIntent);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Password is incorrect.", Toast.LENGTH_LONG).show();
                }

            }
        });
    }
}
