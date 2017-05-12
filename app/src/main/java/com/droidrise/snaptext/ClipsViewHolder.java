package com.droidrise.snaptext;

import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.droidrise.snaptext.model.ClipItem;
import com.droidrise.snaptext.model.ClipsRecyclerViewAdapter;
import com.droidrise.snaptext.utils.JustifyTextView;


/**
 * Created by Hai on 4/24/17.
 */
public class ClipsViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.text_content)
    JustifyTextView mTextView;

    @BindView(R.id.button_delete)
    ImageButton btnDelete;

    public ClipsViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);

        mTextView.setTypeface(((SnapTextApplication)SnapTextApplication.getContext()).getFontContent());
    }

    public void onBindViewHolder(ClipsRecyclerViewAdapter adapter, int position, ClipItem clipItem) {
        mTextView.setText(clipItem.getClip());

        btnDelete.setTag(position);
        btnDelete.setOnClickListener(adapter);
    }
}
