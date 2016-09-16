package com.droidrise.shareplusplus;

import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by a22460 on 16/9/14.
 */

public class TextDrawer {
    protected final static String DEFAULT_TITLE = "From Share++";

    protected static Context mContext;
    protected static WindowManager.LayoutParams mLayoutParams;
    protected static View topView;

    public static void showContent(Context context, String content) {
        mContext = context;
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);

        topView = (View) View.inflate(context, R.layout.view_image, null);
        TextView tx = (TextView) topView.findViewById(R.id.text_content);
        tx.setText(content);

        TextView tvTime = (TextView) topView.findViewById(R.id.text_timestamp);
        tvTime.setText(new SimpleDateFormat("yyyy-MM-dd hh:mm").format(new java.util.Date()));

        TextView tvSign = (TextView) topView.findViewById(R.id.text_signature);
        tvSign.setText("Sent via Share++");

        TextView tvDataSource = (TextView) topView.findViewById(R.id.text_data_source);
        String source = getForgroundActivity();
        if (source != null) {
            tvDataSource.setText("From " + source);
        }

        FloatingActionButton button = (FloatingActionButton) topView.findViewById(R.id.button_exit);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissTopView();
            }
        });

        button = (FloatingActionButton) topView.findViewById(R.id.button_wechat);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareSnap();
                dismissTopView();
            }
        });

        int w = WindowManager.LayoutParams.MATCH_PARENT;
        int h = WindowManager.LayoutParams.WRAP_CONTENT;
        int flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        int type = WindowManager.LayoutParams.TYPE_TOAST;

        mLayoutParams =
                new WindowManager.LayoutParams(w, h, type, flags, PixelFormat.TRANSPARENT);
        mLayoutParams.gravity = Gravity.TOP;
        wm.addView(topView, mLayoutParams);

//        topView.post(new Runnable() {
//            @Override
//            public void run() {
//                saveFrameLayout(topView);
//            }
//        });
    }

    protected static void dismissTopView() {
        if (topView != null) {
            WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            wm.removeView(topView);
            topView = null;
        }
    }

    protected static void shareSnap() {
        File file = saveFrameLayout(topView.findViewById(R.id.layout_snap));
        if (file != null) {
            Intent intent = new Intent();
            ComponentName comp = new ComponentName("com.tencent.mm",
                    "com.tencent.mm.ui.tools.ShareToTimeLineUI");
            intent.setComponent(comp);
            intent.setAction(Intent.ACTION_SEND_MULTIPLE);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setType("image/*");
            ArrayList<Uri> uris = new ArrayList<>();
            uris.add(Uri.fromFile(file));

            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            mContext.startActivity(intent);
        }
    }

    protected static File saveFrameLayout(View view) {
        int width = view.getWidth();
        int height = view.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File folder = new File(Environment.getExternalStorageDirectory(), "Share++");
            if (!folder.exists()) {
                folder.mkdirs();
            }
            File file = new File(folder.getAbsolutePath() + folder.separator + "snap" + ".jpg");

            try {
                FileOutputStream ostream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, ostream);
                ostream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return file;
        }

        return null;
    }

    protected static void updateFrameLayout(View view) {
        mLayoutParams.width = view.getWidth();
        mLayoutParams.height = view.getHeight();

        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        wm.updateViewLayout(view, mLayoutParams);
    }

    protected static String getForgroundActivity() {
        if (Build.VERSION.SDK_INT < 21)
            return getAppLable(getPreLollipop());
        else
            return getAppLable(getLollipop());
    }

    @SuppressWarnings("deprecation")
    protected static String getPreLollipop() {
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        ActivityManager.RunningTaskInfo currentTask = tasks.get(0);
        ComponentName currentActivity = currentTask.topActivity;
        return currentActivity.getPackageName();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected static String getLollipop() {
        UsageStatsManager usm = (UsageStatsManager) mContext.getSystemService(Context.USAGE_STATS_SERVICE);
        long time = System.currentTimeMillis();
        List<UsageStats> applist = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 10 * 1000, time);
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

    protected static String getAppLable(String packageName) {
        if (packageName == null) return null;

        PackageManager packageManager = mContext.getPackageManager();
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            if (applicationInfo != null) {
                return packageManager.getApplicationLabel(applicationInfo).toString();
            }
        } catch (PackageManager.NameNotFoundException e) {
        }
        return null;
    }
}
