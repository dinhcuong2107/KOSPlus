package com.example.kosplus.func;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SmoothStackLayoutManager extends LinearLayoutManager {

    private static final float SCALE_MIN = 0.8f;   // Item xa sẽ nhỏ nhất 80%
    private static final float ALPHA_MIN = 0.5f;   // Item xa sẽ mờ nhất 50%

    public SmoothStackLayoutManager(Context context) {
        super(context, VERTICAL, false);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        super.onLayoutChildren(recycler, state);
        applyScaleAndAlpha();
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int scroll = super.scrollVerticallyBy(dy, recycler, state);
        applyScaleAndAlpha(); // Cập nhật hiệu ứng khi scroll
        return scroll;
    }

    private void applyScaleAndAlpha() {
        float mid = getHeight() / 2.0f; // tâm màn hình
        float d0 = 0.0f;
        float d1 = 0.9f * mid; // phạm vi ảnh hưởng
        float s0 = 1.0f;
        float s1 = SCALE_MIN;

        float a0 = 1.0f;
        float a1 = ALPHA_MIN;

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child == null) continue;

            float childMid = (getDecoratedTop(child) + getDecoratedBottom(child)) / 2.0f;
            float d = Math.min(d1, Math.abs(mid - childMid)); // khoảng cách từ center

            // scale: càng xa center càng nhỏ
            float scale = s0 + (s1 - s0) * (d - d0) / (d1 - d0);
            child.setScaleX(scale);
            child.setScaleY(scale);

            // alpha: càng xa center càng mờ
            float alpha = a0 + (a1 - a0) * (d - d0) / (d1 - d0);
            child.setAlpha(alpha);
        }
    }
}

