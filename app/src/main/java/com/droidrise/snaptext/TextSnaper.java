package com.droidrise.snaptext;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import static android.support.v4.content.FileProvider.getUriForFile;

/**
 * Created by a22460 on 16/9/14.
 */

class TextSnaper {
    private final static String IMAGE_FOLDER = ".snaptext";
    private final static String WECHAT_PACKAGE_NAME = "com.tencent.mm";

    private Context mContext;
    private WindowManager.LayoutParams mLayoutParams;
    private View topView;

    private CircleButton.OnCircleButtonClickListener circleButtonClickListener =
            new CircleButton.OnCircleButtonClickListener() {
                @Override
                public void onClick(int index) {
                    switch (index) {
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
                            //File file = snapToFile((ScrollView) topView.findViewById(R.id.layout_snap), "snap.jpg");
                            if (uri != null) {
                                //Uri uri = Uri.fromFile(file);
                                Intent shareIntent = new Intent();
                                shareIntent.setAction(Intent.ACTION_SEND);
                                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                shareIntent.setDataAndType(uri, mContext.getContentResolver().getType(uri));
                                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                                Intent intent = Intent.createChooser(shareIntent, mContext.getString(R.string.share));
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
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

    TextSnaper(Context context) {
        mContext = context;
    }

    void showContent(String content, String source) {
        if (topView != null) {
            // dismiss previously top view
            dismissTopView();
        }
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        topView = View.inflate(mContext, R.layout.view_image, null);

        FloatingActionButton btnClose = (FloatingActionButton) topView.findViewById(R.id.button_exit);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissTopView();
            }
        });

        TextView tx = (TextView) topView.findViewById(R.id.text_content);
        tx.setText(content);

        TextView tvTime = (TextView) topView.findViewById(R.id.text_timestamp);
        tvTime.setText(new SimpleDateFormat(mContext.getString(R.string.date_format)).format(new java.util.Date()));

        TextView tvSign = (TextView) topView.findViewById(R.id.text_signature);
        tvSign.setText(mContext.getString(R.string.sent_via_sanptext));

        TextView tvDataSource = (TextView) topView.findViewById(R.id.text_data_source);
        if (source != null) {
            tvDataSource.setText(mContext.getString(R.string.snap_from) + " " + source);
            tvDataSource.setVisibility(View.VISIBLE);
        } else {
            tvDataSource.setVisibility(View.GONE);
        }

        CircleButton circleButton = (CircleButton) topView.findViewById(R.id.button_wechat);
        circleButton.setOnCircleButtonClickListener(circleButtonClickListener, R.id.button_wechat);
        circleButton.setDrawable(ContextCompat.getDrawable(mContext, R.drawable.sample_flat_136));
        circleButton.setText(mContext.getString(R.string.wechat_friend));
        if (!isPackageInstalled(WECHAT_PACKAGE_NAME)) {
            circleButton.setVisibility(View.GONE);
        } else {
            circleButton.setVisibility(View.VISIBLE);
        }

        circleButton = (CircleButton) topView.findViewById(R.id.button_moments);
        circleButton.setOnCircleButtonClickListener(circleButtonClickListener, R.id.button_moments);
        circleButton.setDrawable(ContextCompat.getDrawable(mContext, R.drawable.sample_flat_137));
        circleButton.setText(mContext.getString(R.string.wechat_moment));
        if (!isPackageInstalled(WECHAT_PACKAGE_NAME)) {
            circleButton.setVisibility(View.GONE);
        } else {
            circleButton.setVisibility(View.VISIBLE);
        }

        circleButton = (CircleButton) topView.findViewById(R.id.button_share);
        circleButton.setOnCircleButtonClickListener(circleButtonClickListener, R.id.button_share);
        circleButton.setDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_share));
        int dimensionInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                24, mContext.getResources().getDisplayMetrics());
        circleButton.getImageView().getLayoutParams().height = dimensionInDp;
        circleButton.getImageView().getLayoutParams().width = dimensionInDp;
        circleButton.getImageView().requestLayout();
        circleButton.setText(mContext.getString(R.string.share));

        circleButton = (CircleButton) topView.findViewById(R.id.button_save);
        circleButton.setOnCircleButtonClickListener(circleButtonClickListener, R.id.button_save);
        circleButton.setDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_save));
        dimensionInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                24, mContext.getResources().getDisplayMetrics());
        circleButton.getImageView().getLayoutParams().height = dimensionInDp;
        circleButton.getImageView().getLayoutParams().width = dimensionInDp;
        circleButton.getImageView().requestLayout();
        circleButton.setText(mContext.getString(R.string.save));

        int w = WindowManager.LayoutParams.WRAP_CONTENT;
        int h = WindowManager.LayoutParams.WRAP_CONTENT;

        int flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        int type = WindowManager.LayoutParams.TYPE_TOAST;

        mLayoutParams =
                new WindowManager.LayoutParams(w, h, type, flags, PixelFormat.TRANSPARENT);
        mLayoutParams.gravity = Gravity.TOP;
        wm.addView(topView, mLayoutParams);

        topView.post(new Runnable() {
            @Override
            public void run() {
                updateFrameLayout(topView);
            }
        });
    }

    private void updateFrameLayout(View view) {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);

        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);

        int target = (int) (metrics.heightPixels * 0.8);
        if (view.getHeight() > target) {
            mLayoutParams.height = target;
        }
        mLayoutParams.width = view.getWidth();

        wm.updateViewLayout(view, mLayoutParams);
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
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
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
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
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
            }
            File file = new File(folder, "snap.jpg");
            FileOutputStream ostream = new FileOutputStream(file);

            int totalHeight = view.getChildAt(0).getHeight();
            int totalWidth = view.getChildAt(0).getWidth();
            Bitmap bitmap = getBitmapFromView(view, totalHeight, totalWidth);
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

            int totalHeight = view.getChildAt(0).getHeight();
            int totalWidth = view.getChildAt(0).getWidth();
            Bitmap bitmap = getBitmapFromView(view, totalHeight, totalWidth);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
            ostream.close();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String saveFile(ScrollView view) {
        int totalHeight = view.getChildAt(0).getHeight();
        int totalWidth = view.getChildAt(0).getWidth();
        Bitmap bitmap = getBitmapFromView(view, totalHeight, totalWidth);

        String date = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss", Locale.getDefault()).format(new java.util.Date());
        return MediaStore.Images.Media.insertImage(
                mContext.getContentResolver(), bitmap,
                "SnapText_" + date + ".png", "SnapText");

//        if (imgSaved != null) {
//            Cursor cursor = null;
//            try {
//                String[] proj = { MediaStore.Images.Media.DATA };
//                cursor = mContext.getContentResolver().query(Uri.parse(imgSaved),
//                        proj, null, null, null);
//                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//                cursor.moveToFirst();
//                imgSaved = cursor.getString(column_index);
//            } finally {
//                if (cursor != null) {
//                    cursor.close();
//                }
//            }
//       }
    }

    private static final int MAX_HEIGHT = 2048;

    private Bitmap getBitmapFromView(View view, int totalHeight, int totalWidth) {
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
}
