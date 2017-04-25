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

/**
 * Created by Hai on 4/24/17.
 */
public class ClipsRecyclerViewAdapter extends RecyclerView.Adapter<ClipsViewHolder>
        implements RecyclerItemTouchHelperCallback.ItemTouchHelperAdapter {
    private final LayoutInflater mLayoutInflater;
    private final Context mContext;

    //protected List<> mData;

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
        return new ClipsViewHolder(mLayoutInflater.inflate(R.layout.clips_recyclerview_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ClipsViewHolder holder, int position) {
        holder.onBindViewHolder(position);
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        //Collections.swap(mData,fromPosition,toPosition);
    }

    @Override
    public void onItemDissmiss(int position) {
        //mData.remove(position);
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
        return 16;//mData.size();
    }
}
