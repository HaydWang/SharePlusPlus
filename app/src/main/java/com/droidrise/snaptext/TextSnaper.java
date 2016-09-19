package com.droidrise.snaptext;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
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

import static android.support.v4.content.FileProvider.getUriForFile;

/**
 * Created by a22460 on 16/9/14.
 */

public class TextSnaper {
    protected final static String IMAGE_FOLDER = ".snaptext";

    protected Context mContext;
    protected WindowManager.LayoutParams mLayoutParams;
    protected View topView;

    protected CircleButton.OnCircleButtonClickListener circleButtonClickListener =
            new CircleButton.OnCircleButtonClickListener() {
                @Override
                public void onClick(int index) {
                    switch (index) {
                        case R.id.button_wechat:
                            shareToFriend();
                            dismissTopView();
                            break;
                        case R.id.button_moments:
                            shareToMoments();
                            dismissTopView();
                            break;
                        case R.id.button_save:
                            String file = saveFile((ScrollView) topView.findViewById(R.id.layout_snap));
                            if (file != null) {
                                Toast.makeText(mContext, mContext.getString(R.string.saved),
                                        Toast.LENGTH_SHORT).show();
                            }
                            break;
                        default:
                            Toast.makeText(mContext, "Un-handle button", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            };

    public TextSnaper(Context context) {
        mContext = context;
    }

    public void showContent(String content, String source) {
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
        circleButton.setDrawable(mContext.getDrawable(R.drawable.sample_flat_136));
        circleButton.setText(mContext.getString(R.string.wechat_friend));

        circleButton = (CircleButton) topView.findViewById(R.id.button_moments);
        circleButton.setOnCircleButtonClickListener(circleButtonClickListener, R.id.button_moments);
        circleButton.setDrawable(mContext.getDrawable(R.drawable.sample_flat_137));
        circleButton.setText(mContext.getString(R.string.wechat_moment));

        circleButton = (CircleButton) topView.findViewById(R.id.button_share);
        circleButton.setOnCircleButtonClickListener(circleButtonClickListener, R.id.button_share);
        circleButton.setDrawable(mContext.getDrawable(android.R.drawable.ic_menu_share));
        int dimensionInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                24, mContext.getResources().getDisplayMetrics());
        circleButton.getImageView().getLayoutParams().height = dimensionInDp;
        circleButton.getImageView().getLayoutParams().width = dimensionInDp;
        circleButton.getImageView().requestLayout();
        circleButton.setText(mContext.getString(R.string.share));

        circleButton = (CircleButton) topView.findViewById(R.id.button_save);
        circleButton.setOnCircleButtonClickListener(circleButtonClickListener, R.id.button_save);
        circleButton.setDrawable(mContext.getDrawable(R.drawable.ic_save));
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

    protected void updateFrameLayout(View view) {
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

    protected void dismissTopView() {
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
        Uri contentUri = null;
        try {
            File folder = new File(mContext.getCacheDir(), "cache");
            if (!folder.exists()) {
                folder.mkdirs();
            }
            File file = new File(folder, "snap.jpg");
            FileOutputStream ostream = new FileOutputStream(file);

            int totalHeight = view.getChildAt(0).getHeight();
            int totalWidth = view.getChildAt(0).getWidth();
            Bitmap bitmap = getBitmapFromView(view, totalHeight, totalWidth);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
            ostream.close();

            contentUri = getUriForFile(mContext, "com.droidrise.snaptext.fileprovider", file);
        } catch (IOException e) {
            e.printStackTrace();
            contentUri = null;
        }

        return contentUri;
    }

    private File snapToFile(ScrollView view, String fileName) {
        File folder = new File(Environment.getExternalStorageDirectory(), IMAGE_FOLDER);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File file = new File(folder, fileName);

        try {
            FileOutputStream ostream = new FileOutputStream(file);

            int totalHeight = view.getChildAt(0).getHeight();
            int totalWidth = view.getChildAt(0).getWidth();
            Bitmap bitmap = getBitmapFromView(view, totalHeight, totalWidth);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
            ostream.close();
        } catch (IOException e) {
            e.printStackTrace();
            file = null;
        }
        return file;
    }

    private String saveFile(ScrollView view) {
        int totalHeight = view.getChildAt(0).getHeight();
        int totalWidth = view.getChildAt(0).getWidth();
        Bitmap bitmap = getBitmapFromView(view, totalHeight, totalWidth);

        String date = new SimpleDateFormat("yyyy-MM-dd-hh:mm:ss").format(new java.util.Date());
        String imgSaved = MediaStore.Images.Media.insertImage(
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
//        }

        return imgSaved;
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
}
