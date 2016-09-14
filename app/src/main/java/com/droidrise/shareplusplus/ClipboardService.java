package com.droidrise.shareplusplus;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

/**
 * Created by a22460 on 16/9/14.
 */

public class ClipboardService extends Service {
    private ClipboardManager mClipboardManager;
    private ClipboardManager.OnPrimaryClipChangedListener mClipChangedListener
            = new ClipboardManager.OnPrimaryClipChangedListener() {
        public void onPrimaryClipChanged() {
            performClipboardCheck();
        }
    };

    @Override
    public void onCreate() {
        mClipboardManager = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        mClipboardManager.addPrimaryClipChangedListener(mClipChangedListener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mClipboardManager != null) {
            mClipboardManager.removePrimaryClipChangedListener(mClipChangedListener);
            mClipboardManager = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void performClipboardCheck() {
        if (mClipboardManager.hasPrimaryClip()) {
            ClipData data = mClipboardManager.getPrimaryClip();
            if (data != null) {
                ClipDescription description = data.getDescription();
                if (description != null) {
                    String type = description.getMimeType(0);
                    if (type != null) {
                        if (type.equalsIgnoreCase(ClipDescription.MIMETYPE_TEXT_PLAIN)
                                || type.equalsIgnoreCase(ClipDescription.MIMETYPE_TEXT_HTML)) {
                            ClipData.Item item = data.getItemAt(0);
                            String text = item.getText().toString();

                            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }
    }
}
