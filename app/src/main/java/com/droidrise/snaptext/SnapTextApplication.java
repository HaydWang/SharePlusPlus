package com.droidrise.snaptext;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import com.droidrise.snaptext.clipboard.ClipboardService;
import com.droidrise.snaptext.model.ClipItem;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.TypefaceUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Hai on 4/25/17.
 */
public class SnapTextApplication extends Application {
    private static Context mContext;
    private static SnapTextApplication instance;

    private Typeface mFontTraditionZH;
    private Typeface mFontSimpleZH;

    public static List<ClipItem> mData = new ArrayList<>();
    private Realm realm;

    public static Context getContext() {
        return mContext;
    }

    public static SnapTextApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        mContext = getApplicationContext();

        // Initialize Realm
        Realm.init(this);

        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("clips.realm")
                .schemaVersion(1)
                .deleteRealmIfMigrationNeeded()
                .build();
        realm = Realm.getInstance(config);

        RealmResults<ClipItem> clipItems = realm.where(ClipItem.class).findAll();
        mData.addAll(clipItems);

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

    public void onDestroy() {
        if (realm != null) {
            realm.close();
            realm = null;
        }
    }

    public void addClip(final String text, final String source) {
        realm.beginTransaction();
        ClipItem clip = realm.createObject(ClipItem.class);
        clip.setClip(text);
        clip.setSource(source);
        clip.setDate(System.currentTimeMillis());
        realm.commitTransaction();

        mData.add(0, clip);
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("notify_item_inserted", 0);
        startActivity(intent);
    }

    public void deleteClip(final int position) {
        final ClipItem clipItem = mData.get(position);
        mData.remove(position);

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                clipItem.deleteFromRealm();
            }
        });
    }
}
