package com.droidrise.shareplusplus;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by a22460 on 16/9/14.
 */

public class TextDrawer {
    protected static Context mContext;
    protected static WindowManager.LayoutParams mLayoutParams;

    public static void showContent(Context context, String content) {
        mContext = context;
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);

        final LinearLayout view = (LinearLayout) View.inflate(context, R.layout.view_image, null);
        TextView tx = (TextView) view.findViewById(R.id.text_content);
        tx.setText(content);

        int w = WindowManager.LayoutParams.MATCH_PARENT;
        int h = WindowManager.LayoutParams.WRAP_CONTENT;
        int flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        int type = WindowManager.LayoutParams.TYPE_TOAST;

        mLayoutParams =
                new WindowManager.LayoutParams(w, h, type, flags, PixelFormat.TRANSPARENT);
        mLayoutParams.gravity = Gravity.TOP;

        wm.addView(view, mLayoutParams);

        view.post(new Runnable() {
            @Override
            public void run() {
                saveFrameLayout(view);
            }
        });
    }

    protected static void updateFrameLayout(View view) {
        mLayoutParams.width = view.getWidth();
        mLayoutParams.height = view.getHeight();

        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        wm.updateViewLayout(view, mLayoutParams);
    }

    protected static void saveFrameLayout(View view) {
        int width = view.getWidth();
        int height = view.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        File folder, file;
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            folder = new File(android.os.Environment.getExternalStorageDirectory(), "Share++");
            if (!folder.exists()) {
                folder.mkdirs();
            }
            file = new File(folder.getAbsolutePath() + folder.separator + "snap" + ".jpg");

            try {
                FileOutputStream ostream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, ostream);
                ostream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
