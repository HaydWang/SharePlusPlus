package com.droidrise.snaptext;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import butterknife.BindView;

/**
 * Created by Hai on 4/26/17.
 */
public class EditActivity extends BaseActivity {
    @BindView(R.id.edit_title)
    EditText title;

    @BindView(R.id.edit_content)
    EditText content;

    @BindView(R.id.edit_save)
    ImageButton save;

    @BindView(R.id.edit_scroll_view)
    ScrollView scrollView;

    private boolean unchanged = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

//        if (clip != null) {
//            loadClip();
//        }

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveClip();
            }
        });

        SimpleTextWatcher textWatcher = new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);
                unchanged = false;
            }
        };

        title.addTextChangedListener(textWatcher);
        content.addTextChangedListener(textWatcher);

        scrollView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (content.requestFocus()) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(content, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });

        title.setHint("Title");
        content.setHint("Content");
    }

    private void saveClip() {
        if (!checkNotNull()) {
            return;
        }

        String titleString = (TextUtils.isEmpty(title.getText().toString()))
                ? title.getHint().toString() : title.getText().toString();
        String contentString = (TextUtils.isEmpty(content.getText().toString()))
                ? content.getHint().toString() : content.getText().toString();

        // TODO: save and add to list
    }

    private boolean checkNotNull() {
        return !TextUtils.isEmpty(title.getText()) || !TextUtils.isEmpty(content.getText());
    }

    @Override
    public void onBackPressed() {
        if (unchanged) {
            super.onBackPressed();
        } else {
            saveClip();
        }
    }

    public class SimpleTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }
}