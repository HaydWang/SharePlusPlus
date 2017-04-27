package com.droidrise.snaptext;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by Hai on 4/26/17.
 */
public class BaseActivity extends AppCompatActivity {
    protected View containerView;


//    @Inject
//    UserPrefs userPrefs;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
        containerView = findViewById(R.id.layout_container);
    }

    protected void setContainerBgColorFromPrefs() {
//        if (containerView != null) {
//            containerView.setBackgroundResource(userPrefs.getBackgroundColor());
//        }
    }

    protected void setContainerBgColor(int colorRes) {
        if (containerView != null) {
            containerView.setBackgroundResource(colorRes);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setContainerBgColorFromPrefs();
    }
}
