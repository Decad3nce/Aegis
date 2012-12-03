package com.decad3nce.aegis;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;

import java.io.IOException;

import com.decad3nce.aegis.Fragments.SMSAlarmFragment;

public class AlarmDialogActivity extends Activity {
    // Handler for duration time-out
    private final Handler mHandler = new Handler();

    private MediaPlayer mMediaPlayer;
    private Vibrator mVibrator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog);

        findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        boolean vibrate = preferences.getBoolean(
                SMSAlarmFragment.PREFERENCES_ALARM_VIBRATE,
                Boolean.parseBoolean(getResources().getString(
                        R.string.config_default_alarm_vibrate)));
        int duration = Integer.parseInt(preferences.getString(
                SMSAlarmFragment.PREFERENCES_ALARM_DURATION, getResources()
                .getString(R.string.config_default_alarm_duration)));

        // Get AudioManager
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Set ring stream max volume (to allow calling phone after first SMS)
        int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_RING);
        am.setStreamVolume(AudioManager.STREAM_RING, maxVolume,
                AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

        // Set alarm stream max volume
        maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        am.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume,
                AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

        // Get alarm resource
        AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.alarm);

        // Play alarm
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
        mMediaPlayer.setLooping(true);
        try {
            mMediaPlayer.setDataSource(afd.getFileDescriptor(),
                    afd.getStartOffset(), afd.getLength());
            mMediaPlayer.prepare();
        } catch (IOException e) {
            finish();
            mMediaPlayer.release();
            return;
        }
        mMediaPlayer.start();

        if (vibrate) {
            mVibrator.vibrate(duration * 1000);
        }

        // Set duration time-out
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, duration * 1000);

    }

    @Override
    public void onPause() {
        super.onPause();

        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        if (mVibrator != null) {
            mVibrator.cancel();
        }

        mHandler.removeCallbacksAndMessages(null);
    }
}
