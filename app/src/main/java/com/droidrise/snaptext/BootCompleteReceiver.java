package com.droidrise.snaptext;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

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
                        MainActivity.PRES_NAME, Context.MODE_PRIVATE);
                if (prefs.getBoolean(MainActivity.PREFS_SERVICE, true)) {
                    context.startService(new Intent(context, ClipboardService.class));
                }
            }
        }
    }
}
