package com.decad3nce.aegis;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;

import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Locale;

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
        HashSet<String> mounts = getExternalMounts();
        mounts.add(Environment.getExternalStorageDirectory().toString());
        File[] sdcards = new File[mounts.size()];
        String[] mountsarray = mounts.toArray(new String[mounts.size()]);
        for(int i = 0; i < mountsarray.length; ++i) {
            sdcards[i] = new File(mountsarray[i]);
        }
        new WipeTask(this, sdcards, extras.getString("address")).execute();
    }

    public static HashSet<String> getExternalMounts() {
        final HashSet<String> out = new HashSet<String>();
        String reg = "(?i).*vold.*(vfat|ntfs|exfat|fat32|ext3|ext4).*rw.*";
        String s = "";
        try {
            final Process process = new ProcessBuilder().command("mount")
                    .redirectErrorStream(true).start();
            process.waitFor();
            final InputStream is = process.getInputStream();
            final byte[] buffer = new byte[1024];
            while (is.read(buffer) != -1) {
                s = s + new String(buffer);
            }
            is.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        final String[] lines = s.split("\n");
        for (String line : lines) {
            if (!line.toLowerCase(Locale.US).contains("asec")) {
                if (line.matches(reg)) {
                    String[] parts = line.split(" ");
                    for (String part : parts) {
                        if (part.startsWith("/"))
                            if (!part.toLowerCase(Locale.US).contains("vold"))
                                out.add(part);
                    }
                }
            }
        }
        return out;
    }
}
