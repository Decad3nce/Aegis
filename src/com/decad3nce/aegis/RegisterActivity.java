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

public class RegisterActivity extends Activity {

    public static final String PREFERENCES_AEGIS_PASSWORD_SET = "password_set";
    public static final String PREFERENCES_CURRENT_PASSWORD = "current_password";

    EditText mPassword;
    EditText mPasswordConfirm;
    private String mCurrentPassword;
    private static boolean mPasswordSet;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set View to register.xml
        setContentView(R.layout.register);

        final SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        mCurrentPassword = preferences.getString(
                PREFERENCES_CURRENT_PASSWORD,
                this.getResources().getString(
                        R.string.config_default_login_password));

        mPassword = (EditText) findViewById(R.id.reg_password);
        mPasswordConfirm = (EditText) findViewById(R.id.reg_password_confirm);

        Button registerScreen = (Button) findViewById(R.id.btnRegister);

        // Listening to Login Screen link
        registerScreen.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                if (mPassword == null || mPasswordConfirm == null) {
                    Toast.makeText(getApplicationContext(),
                            "Please enter a password and confirm it",
                            Toast.LENGTH_LONG).show();
                }

                String mPasswordText = mPassword.getText().toString();
                String mPasswordConfirmText = mPasswordConfirm.getText()
                        .toString();

                if (mPasswordConfirmText.equals(mPasswordText)) {
                    mCurrentPassword = mPasswordText;
                    mPasswordSet = true;
                    
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("current_password", mCurrentPassword);
                    editor.putBoolean("password_set", mPasswordSet);
                    editor.commit();
                    
                    Toast.makeText(getApplicationContext(),
                            "Passwords is set as " + mPasswordText,
                            Toast.LENGTH_LONG).show();
                    
                    Intent aeGisIntent = new Intent(RegisterActivity.this,
                            AegisActivity.class);
                    aeGisIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    aeGisIntent
                            .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    startActivity(aeGisIntent);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Passwords do not match", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
