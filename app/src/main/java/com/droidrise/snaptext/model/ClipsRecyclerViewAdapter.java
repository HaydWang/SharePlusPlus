package com.droidrise.snaptext.model;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.droidrise.snaptext.ClipsViewHolder;
import com.droidrise.snaptext.R;
import com.droidrise.snaptext.RecyclerItemTouchHelperCallback;
import com.droidrise.snaptext.SnapTextApplication;
import com.droidrise.snaptext.clipboard.ClipboardService;

import java.util.Collections;
import java.util.List;

/**
 * Created by Hai on 4/24/17.
 */
public class ClipsRecyclerViewAdapter extends RecyclerView.Adapter<ClipsViewHolder>
        implements RecyclerItemTouchHelperCallback.ItemTouchHelperAdapter, View.OnClickListener {
    private final LayoutInflater mLayoutInflater;
    private final Context mContext;


    private OnRecyclerViewItemClickListener mOnItemClickListener = null;
    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view , int position);
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public static class SpaceItemDecoration extends RecyclerView.ItemDecoration {
        private int space;
        public SpaceItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            if(parent.getChildAdapterPosition(view) != 0)
                outRect.top = space;
        }

    }

    public ClipsRecyclerViewAdapter(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public ClipsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.clips_recyclerview_item, parent, false);
        view.setOnClickListener(this);
        return new ClipsViewHolder(view);
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(v,(int)v.getTag());
        }
    }

    @Override
    public void onBindViewHolder(ClipsViewHolder holder, int position) {
        holder.itemView.setTag(position);
        holder.onBindViewHolder(this, position, SnapTextApplication.mData.get(position));
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(SnapTextApplication.mData ,fromPosition,toPosition);
    }

    @Override
    public void onItemDissmiss(int position) {
        SnapTextApplication.getInstance().deleteClip(position);
        notifyItemRemoved(position);
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder) {
        viewHolder.itemView.setBackgroundColor(mContext.getResources().getColor(R.color.colorAccent));
    }

    @Override
    public void clearView(RecyclerView.ViewHolder viewHolder) {
        if (viewHolder.getAdapterPosition() < 0 ) return;

        viewHolder.itemView.setBackgroundColor(mContext.getResources().getColor(android.R.color.holo_orange_light));
    }

    @Override
    public int getItemCount() {
        return SnapTextApplication.mData.size();
    }
}
