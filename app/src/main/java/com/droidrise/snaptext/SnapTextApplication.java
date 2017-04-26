package com.droidrise.snaptext;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import com.droidrise.snaptext.clipboard.ClipboardService;

/**
 * Created by Hai on 4/25/17.
 */
public class SnapTextApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences prefs = getSharedPreferences(
                SettingsActivity.PRES_NAME, Context.MODE_PRIVATE);
        if (prefs.getBoolean(SettingsActivity.PREFS_SERVICE, true)) {
            startService(new Intent(this, ClipboardService.class));
        }
    }
}
