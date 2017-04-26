package com.droidrise.snaptext;

import android.Manifest;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.droidrise.snaptext.clipboard.ClipboardService;

import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private static final int MY_PERMISSIONS_REQUEST_GET_TASKS = 2;

    public final static String PRES_NAME = "prefs_snaptext";
    public final static String PREFS_SERVICE = "service";
    public final static String PREFS_COPY_SOURCE = "copy_source";

    public static class PrefsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            SharedPreferences prefs = getActivity().getSharedPreferences(
                    SettingsActivity.PRES_NAME, Context.MODE_PRIVATE);

            SwitchPreference prefService = (SwitchPreference) getPreferenceManager().findPreference("prefService");
            prefService.setChecked(prefs.getBoolean(SettingsActivity.PREFS_SERVICE, true));
            prefService.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    boolean start = (boolean) o;
                    if (start) {
                        getActivity().startService(new Intent(getActivity(), ClipboardService.class));
                    } else {
                        SharedPreferences prefs = getActivity().getSharedPreferences(
                                SettingsActivity.PRES_NAME, Context.MODE_PRIVATE);
                        prefs.edit().putBoolean(SettingsActivity.PREFS_SERVICE, false).apply();
                        getActivity().stopService(new Intent(getActivity(), ClipboardService.class));
                    }

                    return true;
                }
            });

            boolean usageAccess = false;
            // Only need USAGE_ACCESS for API 21 and beyond
            if (Build.VERSION.SDK_INT >= 21) {
                usageAccess = checkUsageAccessGranted(getActivity());
            } else {
                usageAccess = ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.GET_TASKS)
                        == PackageManager.PERMISSION_GRANTED;
            }
            SwitchPreference prefAccess = (SwitchPreference) getPreferenceManager().findPreference("prefRecentTask");
            prefAccess.setChecked(usageAccess);
            prefAccess.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    boolean isAccessing = (Boolean) o;

                    if (isAccessing) {
                        // Try to enable recent task collection
                        if (Build.VERSION.SDK_INT >= 21) {
                            if (checkUsageAccessGranted(getActivity())) {
                                SharedPreferences prefs = getActivity().getSharedPreferences(
                                        SettingsActivity.PRES_NAME, Context.MODE_PRIVATE);
                                prefs.edit().putBoolean(SettingsActivity.PREFS_COPY_SOURCE, true).apply();

                                return true;
                            } else {
                                new AlertDialog.Builder(getActivity())
                                        .setMessage(getString(R.string.enable_usage_access_alert))
                                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                                                startActivity(intent);
                                            }
                                        })
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .show();

                                return false;
                            }
                        } else {
                            if (ContextCompat.checkSelfPermission(getActivity(),
                                    android.Manifest.permission.GET_TASKS)
                                    != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.GET_TASKS},
                                        MY_PERMISSIONS_REQUEST_GET_TASKS);

                                return false;
                            } else {
                                SharedPreferences prefs = getActivity().getSharedPreferences(
                                        SettingsActivity.PRES_NAME, Context.MODE_PRIVATE);
                                prefs.edit().putBoolean(SettingsActivity.PREFS_COPY_SOURCE, true).apply();

                                return true;
                            }
                        }
                    } else {
                        // Do not check copy source
                        SharedPreferences prefs = getActivity().getSharedPreferences(
                                SettingsActivity.PRES_NAME, Context.MODE_PRIVATE);
                        prefs.edit().putBoolean(SettingsActivity.PREFS_COPY_SOURCE, false).apply();

                        return true;
                    }
                }
            });

            Preference prefFeedback = getPreferenceManager().findPreference("pref_feedback");
            prefFeedback.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(Intent.ACTION_SENDTO);
                    i.setData(Uri.parse("mailto:"));
                    i.putExtra(Intent.EXTRA_EMAIL, new String[]{"droidrise@gmail.com"});
                    i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));
                    try {
                        startActivity(Intent.createChooser(i, getString(R.string.email_sending)));
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(getActivity(), getString(R.string.no_email_client), Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ArrayList<String> permissions = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (Build.VERSION.SDK_INT < 21) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.GET_TASKS)
                    != PackageManager.PERMISSION_GRANTED) {
                permissions.add(android.Manifest.permission.GET_TASKS);
            }
        }
        if (permissions.size() > 0) {
            String[] mStringArray = new String[permissions.size()];
            mStringArray = permissions.toArray(mStringArray);
            ActivityCompat.requestPermissions(this,
                    mStringArray,
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(R.id.content_main, new PrefsFragment()).commit();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    protected static boolean checkUsageAccessGranted(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(APP_OPS_SERVICE);
            int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    applicationInfo.uid, applicationInfo.packageName);
            return (mode == AppOpsManager.MODE_ALLOWED);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_GET_TASKS: {
                return;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
