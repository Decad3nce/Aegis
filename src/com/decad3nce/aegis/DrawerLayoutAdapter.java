package com.decad3nce.aegis;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.decad3nce.aegis.Fragments.*;

import java.util.ArrayList;

/**
 * Because good looking adapters matter more than most of everything else you write
 */
public class DrawerLayoutAdapter extends ArrayAdapter<String> {

    Context context;
    int layoutResourceId;
    ArrayList<String> data = null;
    Typeface tf;

    private final Integer[] images = { R.drawable.ic_device_alarms, R.drawable.ic_device_lock, R.drawable.ic_device_wipe, R.drawable.ic_device_data,
            R.drawable.ic_device_locate, R.drawable.ic_action_settings, R.drawable.ic_sim_list, R.drawable.ic_action_about};

    public DrawerLayoutAdapter(Context context, int layoutResourceId, ArrayList<String> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
        this.tf = Typeface.createFromAsset(context.getAssets(), "Roboto-Light.ttf");
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        View mView = v;
        if(mView == null) {
            LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mView = vi.inflate(layoutResourceId, null);
        }
        if(data.get(position) != null)
        {
            TextView text = (TextView) mView.findViewById(R.id.text1);
            ImageView imageView = (ImageView) mView.findViewById(R.id.image1);
            Drawable drawable;
            text.setTypeface(tf);
            text.setText(data.get(position));
            drawable = context.getResources().getDrawable(images[position]);
            enableItem(mView, data.get(position));
            drawable.mutate().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
            imageView.setImageDrawable(drawable);
        }
        return mView;
    }

    private void enableItem(View mView, String what) {
        TextView enable = (TextView) mView.findViewById(R.id.enable);
        enable.setTypeface(tf);
        if(what.startsWith(getResourceString(R.string.alarm_section))) {
            toggleItem(SMSAlarmFragment.PREFERENCES_ALARM_ENABLED, R.bool.config_default_alarm_enabled, enable);
        } else if(what.startsWith(getResourceString(R.string.lock_section))) {
            toggleItem(SMSLockFragment.PREFERENCES_LOCK_ENABLED, R.bool.config_default_lock_enabled, enable);
        } else if(what.startsWith(getResourceString(R.string.wipe_section))) {
            toggleItem(SMSWipeFragment.PREFERENCES_WIPE_ENABLED, R.bool.config_default_wipe_enabled, enable);
        } else if(what.startsWith(getResourceString(R.string.locate_section))) {
            toggleItem(SMSLocateFragment.PREFERENCES_LOCATE_ENABLED, R.bool.config_default_locate_enabled, enable);
        } else if(what.startsWith(getResourceString(R.string.data_section))) {
            toggleItem(SMSDataFragment.PREFERENCES_DATA_ENABLED, R.bool.config_default_data_enabled, enable);
        } else {
            toggleItem(null, 0, enable);
        }
    }

    private boolean getEnabledBoolean(String what, int booleanid) {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return preferences.getBoolean(what, context.getResources().getBoolean(booleanid));
    }

    private void toggleItem(String what, int booleanid, TextView enable) {
        if(what == null) {
            enable.setVisibility(View.INVISIBLE);
            return;
        }

        if(getEnabledBoolean(what, booleanid)) {
            toggleON(enable);
        } else {
            toggleOFF(enable);
        }
    }

    private void toggleON(TextView enable) {
        enable.setVisibility(View.VISIBLE);
        enable.setText(getResourceString(R.string.indicator_on));
        enable.setTextColor(Color.parseColor("#669900"));
    }

    private void toggleOFF(TextView enable){
        enable.setVisibility(View.VISIBLE);
        enable.setText(getResourceString(R.string.indicator_off));
        enable.setTextColor(Color.parseColor("#CC0000"));
    }

    private String getResourceString(int id) {
        String resourceString = context.getResources().getString(id);
        return resourceString;
    }

    public void update() {
        this.notifyDataSetChanged();
    }

}
