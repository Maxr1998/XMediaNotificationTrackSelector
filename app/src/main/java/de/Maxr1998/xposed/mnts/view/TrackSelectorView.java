package de.Maxr1998.xposed.mnts.view;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;

import static de.Maxr1998.xposed.mnts.TrackSelector.CURRENT_PLAYING_POSITION_EXTRA;
import static de.Maxr1998.xposed.mnts.TrackSelector.REPLY_INTENT_EXTRA;
import static de.Maxr1998.xposed.mnts.TrackSelector.TRACK_INFO_EXTRA;
import static de.robv.android.xposed.XposedHelpers.callMethod;

@SuppressLint("ViewConstructor")
public class TrackSelectorView extends RecyclerView {

    private final View mButton;
    private Runnable mCloseRunnable;

    public TrackSelectorView(Context context, View button) {
        super(context);
        mButton = button;
        // Setup
        setLayoutManager(new LinearLayoutManager(getContext()));
        setItemAnimator(new DefaultItemAnimator());
        setBackgroundColor(Color.WHITE);
        setClickable(true);
        setVisibility(View.GONE);
        addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                if (e.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    ViewParent mScrollLayout = getStackScrollLayout(rv);
                    if (mScrollLayout != null) {
                        mScrollLayout.requestDisallowInterceptTouchEvent(true);
                        callMethod(mScrollLayout, "removeLongPressCallback");
                    }
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
        });
    }

    public void setCloseRunnable(Runnable r) {
        mCloseRunnable = r;
    }

    @SuppressWarnings("unused")
    public void resolveIntent(Intent intent) {
        if (!intent.hasExtra(TRACK_INFO_EXTRA)) {
            return;
        }
        mButton.setVisibility(VISIBLE);
        int position = intent.getIntExtra(CURRENT_PLAYING_POSITION_EXTRA, 0);
        setAdapter(new CustomTrackAdapter(intent.<Bundle>getParcelableArrayListExtra(TRACK_INFO_EXTRA),
                position, (PendingIntent) intent.getParcelableExtra(REPLY_INTENT_EXTRA), mCloseRunnable));
        scrollToPosition(position > 0 ? position - 1 : position);
    }

    private ViewParent getStackScrollLayout(View initial) {
        ViewParent current = initial.getParent();
        for (int depth = 0; depth < 8; depth++) {
            if (current.getClass().getName().contains("NotificationStackScrollLayout")) {
                return current;
            }
            current = current.getParent();
        }
        return null;
    }
}
