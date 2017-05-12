package com.droidrise.snaptext.clipboard;

import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.provider.SyncStateContract;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import com.droidrise.snaptext.MainActivity;
import com.droidrise.snaptext.SettingsActivity;
import com.droidrise.snaptext.SnapTextApplication;
import com.droidrise.snaptext.model.ClipItem;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by a22460 on 16/9/14.
 */

public class ClipboardService extends Service {


    private ClipboardManager mClipboardManager;
    private TextSnaper mTextSnaper;

    private ClipboardManager.OnPrimaryClipChangedListener mClipChangedListener
            = new ClipboardManager.OnPrimaryClipChangedListener() {
        public void onPrimaryClipChanged() {
            performClipboardCheck();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        mClipboardManager = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        mClipboardManager.addPrimaryClipChangedListener(mClipChangedListener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences prefs = this.getSharedPreferences(
                SettingsActivity.PRES_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(SettingsActivity.PREFS_SERVICE, true).apply();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mClipboardManager != null) {
            mClipboardManager.removePrimaryClipChangedListener(mClipChangedListener);
            mClipboardManager = null;
        }

        SnapTextApplication.getInstance().onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void performClipboardCheck() {
        if (mClipboardManager.hasPrimaryClip()) {
            ClipData data = mClipboardManager.getPrimaryClip();
            if (data == null) return;

            ClipDescription description = data.getDescription();
            if (description == null) return;

            ClipData.Item item = data.getItemAt(0);
            if (item == null) return;

            String source = getForgroundActivity();
            if (description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                // TODO: check dup
                CharSequence sequence = item.getText();
                if (sequence != null) {
                    String text = sequence.toString() + "\r\n";
                    addClip(text, source);
                }
            } else if (description.hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML)) {
                CharSequence sequence = item.coerceToText(this);
                if (sequence != null) {
                    String text = sequence.toString() + "\r\n";
                    addClip(text, source);
                }
            } else if (description.hasMimeType(ClipDescription.MIMETYPE_TEXT_INTENT)) {
                // TODO: handle intent
            } else if (description.hasMimeType(ClipDescription.MIMETYPE_TEXT_URILIST)) {
                // TODO: handle uri
            }
        }
    }

    // TODO need test API20
    protected String getForgroundActivity() {
        SharedPreferences prefs = getSharedPreferences(
                SettingsActivity.PRES_NAME, Context.MODE_PRIVATE);
        if (prefs.getBoolean(SettingsActivity.PREFS_COPY_SOURCE, true)) {
            if (Build.VERSION.SDK_INT < 21) {
                return getAppLable(getPreLollipop());
            } else {
                return getAppLable(getLollipop());
            }
        } else {
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    protected String getPreLollipop() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        ActivityManager.RunningTaskInfo currentTask = tasks.get(0);
        ComponentName currentActivity = currentTask.topActivity;
        return currentActivity.getPackageName();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected String getLollipop() {
        UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        long time = System.currentTimeMillis();
        List<UsageStats> applist = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 30 * 60 * 1000, time);
        if (applist != null && applist.size() > 0) {
            SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();
            for (UsageStats usageStats : applist) {
                mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
            }

            if (mySortedMap != null && !mySortedMap.isEmpty()) {
                return mySortedMap.get(mySortedMap.lastKey()).getPackageName();
            }
        }
        return null;
    }

    protected String getAppLable(String packageName) {
        if (packageName == null) return null;

        PackageManager packageManager = getPackageManager();
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            if (applicationInfo != null) {
                return packageManager.getApplicationLabel(applicationInfo).toString();
            }
        } catch (PackageManager.NameNotFoundException e) {
        }
        return null;
    }

    private void addClip(final String text, final String source) {
        SnapTextApplication.getInstance().addClip(text, source);
    }
}
