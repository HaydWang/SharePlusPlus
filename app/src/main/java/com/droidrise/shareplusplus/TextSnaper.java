package com.droidrise.shareplusplus;

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
import android.support.design.widget.FloatingActionButton;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by a22460 on 16/9/14.
 */

public class TextSnaper {
    protected final static String DEFAULT_TITLE = "From Share++";
    protected final static String IMAGE_FOLDER = "TextSnapImage";

    protected static Context mContext;
    protected static WindowManager.LayoutParams mLayoutParams;
    protected static View topView;

    public static void showContent(Context context, String content, String source) {
        mContext = context;
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);

        topView = View.inflate(context, R.layout.view_image, null);
        TextView tx = (TextView) topView.findViewById(R.id.text_content);
        tx.setText(content);

        TextView tvTime = (TextView) topView.findViewById(R.id.text_timestamp);
        tvTime.setText(new SimpleDateFormat("yyyy-MM-dd hh:mm").format(new java.util.Date()));

        TextView tvSign = (TextView) topView.findViewById(R.id.text_signature);
        tvSign.setText("Sent via TextSnap");

        TextView tvDataSource = (TextView) topView.findViewById(R.id.text_data_source);
        if (source != null) {
            tvDataSource.setText("From " + source);
            tvDataSource.setVisibility(View.VISIBLE);
        } else {
            tvDataSource.setVisibility(View.GONE);
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
        File file = snapView((ScrollView) topView.findViewById(R.id.layout_snap));
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

    protected static File snapView(ScrollView view) {
        int totalHeight = view.getChildAt(0).getHeight();
        int totalWidth = view.getChildAt(0).getWidth();

        Bitmap bitmap = getBitmapFromView(view, totalHeight, totalWidth);

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File folder = new File(Environment.getExternalStorageDirectory(), IMAGE_FOLDER);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            String date = new SimpleDateFormat("yyyy-MM-dd-hh:mm:ss").format(new java.util.Date());
            File file = new File(folder.getAbsolutePath() + folder.separator + date + ".jpg");

            try {
                FileOutputStream ostream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, ostream);
                ostream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.fromFile(file)));

            return file;
        }

        return null;
    }

    protected static final int MAX_HEIGHT = 2048;

    protected static Bitmap getBitmapFromView(View view, int totalHeight, int totalWidth) {
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
