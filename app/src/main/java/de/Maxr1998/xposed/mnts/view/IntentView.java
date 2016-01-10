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
        if (mButton != null) {
            mButton.setVisibility(VISIBLE);
        }
        if (mRecyclerView != null) {
            Bundle extras = intent.getExtras();
            CustomTrackAdapter ct = new CustomTrackAdapter(extras.<Bundle>getParcelableArrayList(TRACK_INFO_EXTRA),
                    extras.getInt(CURRENT_PLAYING_POSITION_EXTRA), (PendingIntent) extras.getParcelable(REPLY_INTENT_EXTRA));
            ct.mCloseHandler = (OnClickListener) mRecyclerView.getTag(42 << 24);
            mRecyclerView.setAdapter(ct);
        }
    }
}
