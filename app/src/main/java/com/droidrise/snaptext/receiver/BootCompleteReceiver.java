package com.droidrise.snaptext.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import com.droidrise.snaptext.SettingsActivity;
import com.droidrise.snaptext.clipboard.ClipboardService;

/**
 * Created by a22460 on 16/9/20.
 */

public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
                SharedPreferences prefs = context.getSharedPreferences(
                        SettingsActivity.PRES_NAME, Context.MODE_PRIVATE);
                if (prefs.getBoolean(SettingsActivity.PREFS_SERVICE, true)) {
                    context.startService(new Intent(context, ClipboardService.class));
                }
            }
        }
    }
}
