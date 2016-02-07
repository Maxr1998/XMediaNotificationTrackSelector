package de.Maxr1998.xposed.mnts.view;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;

import static de.Maxr1998.xposed.mnts.TrackSelector.CURRENT_PLAYING_POSITION_EXTRA;
import static de.Maxr1998.xposed.mnts.TrackSelector.REPLY_INTENT_EXTRA;
import static de.Maxr1998.xposed.mnts.TrackSelector.TRACK_INFO_EXTRA;

public class IntentView extends FrameLayout {

    private RecyclerView mRecyclerView;
    private View mButton;

    public IntentView(Context context) {
        super(context);
        setVisibility(GONE);
    }

    public void setShowButton(View button) {
        mButton = button;
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
    }

    @SuppressWarnings("unused")
    public void resolveIntent(Intent intent) {
        if (!intent.hasExtra(TRACK_INFO_EXTRA)) {
            return;
        }
        if (mButton != null) {
            mButton.setVisibility(VISIBLE);
        }
        if (mRecyclerView != null) {
            int position = intent.getIntExtra(CURRENT_PLAYING_POSITION_EXTRA, 0);
            CustomTrackAdapter ct = new CustomTrackAdapter(intent.<Bundle>getParcelableArrayListExtra(TRACK_INFO_EXTRA), position,
                    (PendingIntent) intent.getParcelableExtra(REPLY_INTENT_EXTRA));
            ct.mCloseHandler = (OnClickListener) mRecyclerView.getTag(42 << 24);
            mRecyclerView.setAdapter(ct);
            mRecyclerView.scrollToPosition(position > 0 ? position - 1 : position);
        }
    }
}
