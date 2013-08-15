package com.decad3nce.aegis;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: Decad3nce
 * Date: 8/15/13
 * Time: 5:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class WipeBaseActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        File[] sdcards =  { new File(Environment
                .getExternalStorageDirectory().toString())};
        new WipeTask(this, sdcards, extras.getString("address")).execute();
    }
}
