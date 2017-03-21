package com.droidrise.snaptext;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import static android.support.v4.content.FileProvider.getUriForFile;

import uk.co.chrisjenx.calligraphy.TypefaceUtils;
import com.github.jorgecastilloprz.FABProgressCircle;

/**
 * Created by a22460 on 16/9/14.
 */

class TextSnaper {
    private final static String IMAGE_FOLDER = ".snaptext";
    private final static String WECHAT_PACKAGE_NAME = "com.tencent.mm";

    private Context mContext;
    private Typeface mFontTitle;
    private Typeface mFontContent;
    private WindowManager.LayoutParams mLayoutParams;
    private View topView;

    private String title;
    static private SharedPreferences prefs;
    private WindowManager windowManager;
    private int bubbleX, bubbleY;

    static private HashMap<Long, SnapData> snaplist = new HashMap<>();

    TextSnaper(Context context) {
        mContext = context;
        //mFontContent = TypefaceUtils.load(mContext.getResources().getAssets(), "fonts/fz_lvjiande_jian.otf");
        //mFontContent = TypefaceUtils.load(mContext.getResources().getAssets(), "fonts/fz_songkebenxiukai_jian.ttf");
        mFontContent = TypefaceUtils.load(mContext.getResources().getAssets(), "fonts/fz_suxinshiliukai_jian.ttf");
        mFontTitle = TypefaceUtils.load(mContext.getResources().getAssets(), "fonts/traditional.otf");

        prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        bubbleX = prefs.getInt("bubble_x", 0);
        bubbleY = prefs.getInt("bubble_y", 240);
    }

    private ImageButton.OnClickListener ImageButtonClickListener =
            new ImageButton.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v == null) return;

                    switch (v.getId()) {
                        case R.id.button_wechat:
                            // Wechat conversion accept internal Uri.
                            shareToFriend();
                            dismissTopView();
                            break;
                        case R.id.button_moments:
                            // TODO Wechat moment only accept external file, need check permission.
                            if (ContextCompat.checkSelfPermission(mContext,
                                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    != PackageManager.PERMISSION_GRANTED) {
                                dismissTopView();
                            } else {
                                shareToMoments();
                                dismissTopView();
                            }
                            break;
                        case R.id.button_share:
                            Uri uri = snapToCache((ScrollView) topView.findViewById(R.id.layout_snap));
                            //File file = snapToFile((ScrollView) topView.findViewById(R.id.layout_snap), "share.jpg");
                            if (uri != null) {
                                //Uri uri = Uri.fromFile(file);
                                Intent shareIntent = new Intent();
                                shareIntent.setAction(Intent.ACTION_SEND);
                                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                shareIntent.setDataAndType(uri, mContext.getContentResolver().getType(uri));
                                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                                Intent intent = Intent.createChooser(shareIntent, mContext.getString(R.string.share));
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                mContext.startActivity(intent);
                            } else {
                                Toast.makeText(mContext, mContext.getString(R.string.failed_share_snap),
                                        Toast.LENGTH_SHORT).show();
                            }
                            dismissTopView();
                            break;
                        case R.id.button_save:
                            // TODO Check storage permission
                            if (ContextCompat.checkSelfPermission(mContext,
                                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    != PackageManager.PERMISSION_GRANTED) {
                                dismissTopView();
                            } else {
                                String imgSaved = saveFile((ScrollView) topView.findViewById(R.id.layout_snap));
                                if (imgSaved != null) {
                                    Toast.makeText(mContext, mContext.getString(R.string.saved),
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(mContext, mContext.getString(R.string.failed_share_snap),
                                            Toast.LENGTH_SHORT).show();
                                }
                                dismissTopView();
                            }
                            break;
                        default:
                            Toast.makeText(mContext, "Un-handle button", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            };

    private static final int MY_PERMISSIONS_REQUEST_MOMENTS = 1;
    private static final int MY_PERMISSIONS_REQUEST_SAVE = 2;

    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SAVE: {
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
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    protected void showContent(final String content, final String source) {
        SnapData snapData = new SnapData(content, source);
        // TODO: find dup
        snaplist.put(snapData.getId(), snapData);
        //showTextView(snapData);
        showSnapBubble(snapData);
    }

    protected void showSnapBubble(final SnapData snapData) {
        windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        if (topView != null) {
            //dismissTopView();
        } else {
            topView = View.inflate(mContext, R.layout.view_snap_bubble, null);

            final View btnBubble = topView.findViewById(R.id.button_snap_bubble);
            btnBubble.setOnTouchListener(new View.OnTouchListener() {
                boolean moved = false;
                int lastY, paramY;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            btnBubble.setPressed(true);
                            moved = false;
                            lastY = (int) event.getRawY();
                            paramY = mLayoutParams.y;
                            break;
                        case MotionEvent.ACTION_MOVE:
                            moved = true;
                            int dy = (int) event.getRawY() - lastY;
                            mLayoutParams.y = paramY + dy;
                            windowManager.updateViewLayout(topView, mLayoutParams);
                            break;
                        case MotionEvent.ACTION_UP:
                            btnBubble.setPressed(false);
                            if (moved) {
                                bubbleY = paramY + (int) event.getRawY() - lastY;
                                prefs.edit().putInt("bubble_y", bubbleY).apply();
                            } else {
                                Intent intent = new Intent(mContext, SnapTextActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra(SnapData.ID, snapData.getId());
                                mContext.startActivity(intent);
                            }
                            break;
                    }
                    return true;
                }
            });

            DisplayMetrics metrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(metrics);
            int w = (int) (58.0f * metrics.scaledDensity); //WindowManager.LayoutParams.WRAP_CONTENT;
            int h = (int) (72.0f * metrics.scaledDensity); //WindowManager.LayoutParams.WRAP_CONTENT;
            // Do not get back key event
            int flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
            int type = WindowManager.LayoutParams.TYPE_TOAST;
            mLayoutParams =
                    new WindowManager.LayoutParams(w, h, type, flags, PixelFormat.TRANSPARENT);
            mLayoutParams.gravity = Gravity.RIGHT | Gravity.TOP;
            mLayoutParams.x = bubbleX;
            mLayoutParams.y = bubbleY;
            mLayoutParams.alpha = 0.9f;

            windowManager.addView(topView, mLayoutParams);
        }

        topView.post(new Runnable() {
            @Override
            public void run() {
                updateSnapBubble(topView, snapData);
            }
        });
    }

    protected void updateSnapBubble(View view, SnapData snapData) {
        final FABProgressCircle progressCircle = (FABProgressCircle)view.findViewById(R.id.fabProgressCircle);
        // TODO: need fix in the lib
        progressCircle.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                ImageView imgView = (ImageView) v.findViewById(R.id.completeFabIcon);
                if ((imgView != null) && (imgView.getScaleType() != ImageView.ScaleType.CENTER_INSIDE)) {
                    imgView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                }
            }
        });
        progressCircle.show();

        // TODO: generate snap
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressCircle.beginFinalAnimation();
            }
        }, 320);
    }

    protected void showSnapTextView(final String content, final String source) {
        if (topView != null) {
            dismissTopView();
        }
        topView = View.inflate(mContext, R.layout.view_image, null);

        ImageButton btnClose = (ImageButton) topView.findViewById(R.id.button_exit);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissTopView();
            }
        });

        TextView  contentView = (TextView) topView.findViewById(R.id.text_content);
        if (Locale.getDefault().getCountry().equals(new Locale("zh","TW").getCountry())
                || Locale.getDefault().getCountry().equals(new Locale("zh","HK").getCountry())) {
            // Set content to traditional for TaiWan & HongKong
            contentView.setTypeface(mFontTitle);
        } else {
            contentView.setTypeface(mFontContent);
        }
        contentView.setText(content);

        String subTitle = "";
        title = new SimpleDateFormat(mContext.getString(R.string.date_format)).format(new java.util.Date());
        if (Locale.getDefault().getLanguage().equals(new Locale("zh").getLanguage())) {
            title = CNDateUtility.getFullCNDate() + " 记";

            if (source != null) {
                subTitle = CNDateUtility.getFullCNDate();
            }
        }
        if (source != null) {
            title = mContext.getString(R.string.snap_from) + " " + source;
            subTitle = new SimpleDateFormat(mContext.getString(R.string.date_format_title)).format(new java.util.Date());
        }

        TextView secondTitle = (TextView) topView.findViewById(R.id.text_second_title);
        secondTitle.setTypeface(mFontTitle);

        OneKeyClearEditText editText = (OneKeyClearEditText) topView.findViewById(R.id.text_data_source);
        editText.setTypeface(mFontTitle);

        secondTitle.setText(subTitle);
        editText.setHint(title);
        editText.setText(title);
        editText.setOnFocusChangeListener(new android.view.View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                OneKeyClearEditText editText = (OneKeyClearEditText) topView.findViewById(R.id.text_data_source);
                if (hasFocus) {
                    title = editText.getText().toString();
                    if (source == null) {
                        TextView tv = (TextView) topView.findViewById(R.id.text_second_title);
                        if (Locale.getDefault().getLanguage().equals(new Locale("zh").getLanguage())) {
                            tv.setTypeface(mFontTitle);
                            tv.setText(CNDateUtility.getFullCNDate());
                        } else {
                            tv.setText(new SimpleDateFormat(mContext.getString(R.string.date_format_title)).format(new java.util.Date()));
                        }
                    }
                } else {
                    if (editText.getText().equals(editText.getHint())) {
                        TextView tv = (TextView) topView.findViewById(R.id.text_second_title);
                        tv.setText("");
                    }
                }
                editText.onFocusChange(v, hasFocus);
            }
        });
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId== EditorInfo.IME_ACTION_DONE){
                    if(v.getText().toString().isEmpty()) {
                        v.setText(v.getHint());
                    }
                    v.clearFocus();
                }
                return false;
            }
        });
        editText.setOnClearClickedListener(new OneKeyClearEditText.OnClearClickedListener() {
            @Override
            public void onClearClicked(TextView v) {
                v.setText(title);
                v.clearFocus();
            }
        });

        TextView tv = (TextView) topView.findViewById(R.id.text_timestamp);
        tv.setTypeface(mFontTitle);
        tv.setText(new SimpleDateFormat(mContext.getString(R.string.date_format)).format(new java.util.Date()));

        tv = (TextView) topView.findViewById(R.id.text_signature);
        tv.setTypeface(mFontTitle);
        tv.setText(mContext.getString(R.string.sent_via_sanptext));

        ImageButton button = (ImageButton) topView.findViewById(R.id.button_wechat);
        button.setOnClickListener(ImageButtonClickListener);
        if (!isPackageInstalled(WECHAT_PACKAGE_NAME)) {
            button.setVisibility(View.GONE);
        } else {
            button.setVisibility(View.VISIBLE);
        }

        button = (ImageButton) topView.findViewById(R.id.button_moments);
        button.setOnClickListener(ImageButtonClickListener);
        if (!isPackageInstalled(WECHAT_PACKAGE_NAME)) {
            button.setVisibility(View.GONE);
        } else {
            button.setVisibility(View.VISIBLE);
        }

        button = (ImageButton) topView.findViewById(R.id.button_share);
        button.setOnClickListener(ImageButtonClickListener);

        button = (ImageButton) topView.findViewById(R.id.button_save);
        button.setOnClickListener(ImageButtonClickListener);

        button = (ImageButton) topView.findViewById(R.id.button_close);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissTopView();
            }
        });

        int w = WindowManager.LayoutParams.WRAP_CONTENT;
        int h = WindowManager.LayoutParams.WRAP_CONTENT;
        int flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        int type = WindowManager.LayoutParams.TYPE_TOAST;
        mLayoutParams =
                new WindowManager.LayoutParams(w, h, type, flags, PixelFormat.TRANSPARENT);
        mLayoutParams.gravity = Gravity.TOP;

        windowManager.addView(topView, mLayoutParams);
        topView.post(new Runnable() {
            @Override
            public void run() {
                updateSnapTextView(topView);
            }
        });
    }

    private void updateSnapTextView(View view) {
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);

        int target = (int) (metrics.heightPixels * 1);
        if (view.getHeight() > target) {
            mLayoutParams.height = target;
        }
        mLayoutParams.width = metrics.widthPixels;

        windowManager.updateViewLayout(view, mLayoutParams);
    }

    private void dismissTopView() {
        if (topView != null) {
            WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            wm.removeView(topView);
            topView.setVisibility(View.GONE);
            topView = null;
        }
    }

    private void shareToMoments() {
        File file = snapToFile((ScrollView) topView.findViewById(R.id.layout_snap), "snap.jpg");
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
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.failed_share_snap),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void shareToFriend() {
        Uri uri = snapToCache((ScrollView) topView.findViewById(R.id.layout_snap));
        //File file = snapToFile((ScrollView) topView.findViewById(R.id.layout_snap), "snap.jpg");
        if (uri != null) {
            Intent intent = new Intent();
            ComponentName comp = new ComponentName("com.tencent.mm",
                    "com.tencent.mm.ui.tools.ShareImgUI");
            intent.setComponent(comp);
            intent.setAction(Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setType("image/*");

            mContext.grantUriPermission("com.tencent.mm", uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(Intent.EXTRA_STREAM, uri);

            mContext.startActivity(intent);
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.failed_share_snap),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private Uri snapToCache(ScrollView view) {
        try {
            File folder = new File(mContext.getCacheDir(), "cache");
            if (!folder.exists()) {
                if (!folder.mkdirs()) {
                    return null;
                }
            } else {
                for (File child : folder.listFiles()) {
                    child.delete();
                }
            }

            //
            File file = new File(folder, String.valueOf(System.currentTimeMillis()) + ".jpg");
            FileOutputStream ostream = new FileOutputStream(file);

            Bitmap bitmap = getBitmapFromView(view.getChildAt(0));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
            ostream.close();

            return getUriForFile(mContext, "com.droidrise.snaptext.fileprovider", file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private File snapToFile(ScrollView view, String fileName) {
        File folder = new File(Environment.getExternalStorageDirectory(), IMAGE_FOLDER);
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                return null;
            }
        }

        try {
            File file = new File(folder, fileName);
            FileOutputStream ostream = new FileOutputStream(file);

            Bitmap bitmap = getBitmapFromView(view.getChildAt(0));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
            ostream.close();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String saveFile(ScrollView view) {
        Bitmap bitmap = getBitmapFromView(view.getChildAt(0));

        String date = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss", Locale.getDefault()).format(new java.util.Date());
        return MediaStore.Images.Media.insertImage(
                mContext.getContentResolver(), bitmap,
                "SnapText_" + date + ".png", "SnapText");
    }

    private static final int MAX_HEIGHT = 2048;

    private Bitmap getBitmapFromView(View view) {
        int totalHeight = view.getHeight();
        int totalWidth = view.getWidth();
        int height = Math.min(MAX_HEIGHT, totalHeight);
        float percent = height / (float) totalHeight;

        Bitmap canvasBitmap = Bitmap.createBitmap((int) (totalWidth * percent), (int) (totalHeight * percent), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(canvasBitmap);

        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);

        canvas.save();
        canvas.scale(percent, percent);
        view.draw(canvas);
        canvas.restore();

        return canvasBitmap;
    }

    private boolean isPackageInstalled(String packagename) {
        PackageManager packageManager = mContext.getPackageManager();
        try {
            packageManager.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static SnapData getSnapData(long id) {
        return snaplist.get(id);
    }
}
