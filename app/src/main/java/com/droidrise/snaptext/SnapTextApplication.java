package com.droidrise.snaptext;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import com.droidrise.snaptext.clipboard.ClipboardService;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.TypefaceUtils;

import java.util.Locale;

/**
 * Created by Hai on 4/25/17.
 */
public class SnapTextApplication extends Application {
    private static Context mContext;

    private Typeface mFontTraditionZH;
    private Typeface mFontSimpleZH;

    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();

        SharedPreferences prefs = getSharedPreferences(
                SettingsActivity.PRES_NAME, Context.MODE_PRIVATE);
        if (prefs.getBoolean(SettingsActivity.PREFS_SERVICE, true)) {
            startService(new Intent(this, ClipboardService.class));
        }

        mFontSimpleZH = TypefaceUtils.load(getResources().getAssets(),
                "fonts/fz_suxinshiliukai_jian.ttf");
        mFontTraditionZH = TypefaceUtils.load(getResources().getAssets(),
                "fonts/traditional.otf");

        if (Locale.getDefault().getCountry().equals(new Locale("zh","TW").getCountry())
                || Locale.getDefault().getCountry().equals(new Locale("zh","HK").getCountry())) {
            // Set content to traditional for TaiWan & HongKong
            CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                    .setDefaultFontPath("fonts/traditional.otf")
                    .setFontAttrId(R.attr.fontPath)
                    .build());
        } else {
            CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                    .setDefaultFontPath("fonts/fz_suxinshiliukai_jian.otf")
                    .setFontAttrId(R.attr.fontPath)
                    .build());
        }


    }

    public Typeface getFontTitle() {
        if (Locale.getDefault().getCountry().equals(new Locale("zh","TW").getCountry())
                || Locale.getDefault().getCountry().equals(new Locale("zh","HK").getCountry())) {
            // Set content to traditional for TaiWan & HongKong
            return mFontTraditionZH;
        }

        return mFontSimpleZH;
    }

    public Typeface getFontContent() {
        return mFontTraditionZH;
    }
}
