package com.droidrise.snaptext;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.droidrise.snaptext.model.ClipItem;
import com.droidrise.snaptext.utils.JustifyTextView;

import java.util.Locale;

/**
 * Created by Hai on 4/24/17.
 */
public class ClipsViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.text_content)
    JustifyTextView mTextView;

    public ClipsViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);

        mTextView.setTypeface(((SnapTextApplication)SnapTextApplication.getContext()).getFontContent());
    }

    public void onBindViewHolder(int position, ClipItem clipItem) {
        mTextView.setText(clipItem.getClip());
    }
}
