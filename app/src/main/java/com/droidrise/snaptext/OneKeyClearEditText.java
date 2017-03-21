package com.droidrise.snaptext;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

/**
 * Created by Hai on 3/20/17.
 */
public class OneKeyClearEditText extends AppCompatEditText implements View.OnFocusChangeListener, TextWatcher {
    private Drawable iconClear;
    private boolean hasFocus;
    private int colorAccent;
    private String text;

    public OneKeyClearEditText(Context context) {
        this(context, null);
    }
    public OneKeyClearEditText(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.editTextStyle);
    }

    @SuppressLint("InlinedApi")
    public OneKeyClearEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.getTheme()
                .obtainStyledAttributes(new int[] {android.R.attr.colorAccent});
        colorAccent = array.getColor(0, 0xFF00FF);
        array.recycle();
        initClearDrawable(context);
    }

    @SuppressLint("NewApi")
    private void initClearDrawable(Context context) {
        iconClear = getCompoundDrawables()[2];
        if (iconClear == null) {
            iconClear = getResources().getDrawable(R.drawable.ic_highlight_off_18dp, context.getTheme());
        }
        DrawableCompat.setTint(iconClear, colorAccent);
        iconClear.setBounds(0, 0, (int) getTextSize(), (int) getTextSize());

        setOnFocusChangeListener(this);
        addTextChangedListener(this);
    }

    private void setClearIconVisible(boolean visible) {
        Drawable right = visible ? iconClear : null;
        setCompoundDrawables(getCompoundDrawables()[0], getCompoundDrawables()[1], right, getCompoundDrawables()[3]);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        InputMethodManager imm = (InputMethodManager)getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        this.hasFocus = hasFocus;
        if (hasFocus) {
            text = getText().toString();
            if (text != null && text.equals(getHint())) {
                getText().clear();
            }
            setClearIconVisible(true);
            imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
        } else {
            setClearIconVisible(false);
            imm.hideSoftInputFromWindow(getWindowToken(), 0);
            //clearFocus();
        }
    }

    @Override
    public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = false;
        if (iconClear != null && event.getAction() == MotionEvent.ACTION_UP) {

            int x = (int) event.getX();
            boolean isInnerWidth = (x > (getWidth() - getTotalPaddingRight()))
                    && (x < (getWidth() - getPaddingRight()));
            Rect rect = iconClear.getBounds();
            int height = rect.height();
            int y = (int) event.getY();
            int distance = (getHeight() - height) / 2;
            boolean isInnerHeight = (y > distance) && (y < (distance + height));
            if (isInnerHeight && isInnerWidth) {
                // Clear button clicked
                if (mOnClearClickedListener != null) {
                    mOnClearClickedListener.onClearClicked(this);
                }
                result = true;
            }
        }
        return result ? false : super.onTouchEvent(event);
    }

    public interface OnClearClickedListener {
        void onClearClicked(TextView v);
    }

    private OnClearClickedListener mOnClearClickedListener;
    public void setOnClearClickedListener(OnClearClickedListener l) {
        mOnClearClickedListener = l;
    }
}
