package com.droidrise.snaptext;

import android.graphics.Canvas;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import com.droidrise.snaptext.model.ClipsRecyclerViewAdapter;

/**
 * Created by Hai on 4/24/17.
 */
public class RecyclerItemTouchHelperCallback extends ItemTouchHelper.Callback {
    private ClipsRecyclerViewAdapter mAdapter;

    public interface ItemTouchHelperAdapter {
        void onItemMove(int fromPosition,int toPosition);
        void onItemDissmiss(int position);
        void onSelectedChanged(RecyclerView.ViewHolder viewHolder);
        void clearView(RecyclerView.ViewHolder viewHolder);
    }

    public RecyclerItemTouchHelperCallback(RecyclerView.Adapter adapter){
        this.mAdapter = (ClipsRecyclerViewAdapter)adapter;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN
                | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;

        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        }

        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        final int fromPosition = viewHolder.getAdapterPosition();
        final int toPosition = target.getAdapterPosition();
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                mAdapter.onItemMove(i, i+1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                mAdapter.onItemMove(i, i-1);
            }
        }

        mAdapter.notifyItemMoved(fromPosition,toPosition);
        return true;
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            mAdapter.onSelectedChanged(viewHolder);
        }
        super.onSelectedChanged(viewHolder, actionState);
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        mAdapter.clearView(viewHolder);
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        mAdapter.onItemDissmiss(viewHolder.getAdapterPosition());
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView,
                            RecyclerView.ViewHolder viewHolder, float dX, float dY,
                            int actionState, boolean isCurrentlyActive) {

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            float width = (float) viewHolder.itemView.getWidth();
            float alpha = 0.9f - Math.abs(dX) / width;
            viewHolder.itemView.setAlpha(alpha);
            viewHolder.itemView.setTranslationX(dX);
        } else {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY,
                    actionState, isCurrentlyActive);
        }
    }
}