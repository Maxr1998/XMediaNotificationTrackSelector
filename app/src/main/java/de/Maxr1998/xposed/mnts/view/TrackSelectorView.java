package de.Maxr1998.xposed.mnts.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaDescription;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import de.Maxr1998.trackselectorlib.TrackItem;
import de.Maxr1998.xposed.mnts.BuildConfig;

import static de.Maxr1998.trackselectorlib.NotificationHelper.INTENT_VIEW_ID;
import static de.Maxr1998.xposed.mnts.TrackSelector.CURRENT_PLAYING_POSITION_EXTRA;
import static de.Maxr1998.xposed.mnts.TrackSelector.REPLY_INTENT_EXTRA;
import static de.Maxr1998.xposed.mnts.TrackSelector.TRACK_INFO_EXTRA;
import static de.robv.android.xposed.XposedHelpers.callMethod;

@SuppressLint("ViewConstructor")
public class TrackSelectorView extends RecyclerView {

    public final float density;
    private final ImageView queueButton;
    private final Drawable openQueueDrawable;
    private final Drawable closeDrawable;
    private MediaSession.Token mediaToken;
    private int originalHeight;
    private final Runnable mCloseRunnable = new Runnable() {
        @Override
        public void run() {
            // Close
            final Animator closeAnimation = ViewAnimationUtils.createCircularReveal(TrackSelectorView.this, (int) (queueButton.getX() + queueButton.getWidth() / 2), (int) (queueButton.getY() + queueButton.getHeight() / 2), density * 500, density * 24);
            closeAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    setVisibility(GONE);
                    queueButton.setImageDrawable(openQueueDrawable);
                }
            });
            closeAnimation.start();
            final Animation collapseAnimation = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    getRoot().getLayoutParams().height = (int) (originalHeight + (((density * 256) - originalHeight) * (1f - interpolatedTime)));
                    getRoot().requestLayout();
                }

                @Override
                public boolean willChangeBounds() {
                    return true;
                }
            };
            collapseAnimation.setDuration(200);
            collapseAnimation.setInterpolator(getContext(), android.R.interpolator.accelerate_decelerate);
            startAnimation(collapseAnimation);
        }
    };

    @SuppressWarnings("ResourceType")
    public TrackSelectorView(Context context, View button, @Nullable MediaSession.Token token) {
        super(context);
        setId(INTENT_VIEW_ID);
        queueButton = (ImageView) button;
        mediaToken = token;
        density = getResources().getDisplayMetrics().density;
        Drawable temp, temp2;
        try {
            Resources modRes = getContext().createPackageContext(BuildConfig.APPLICATION_ID, 0).getResources();
            temp = modRes.getDrawable(modRes.getIdentifier("ic_queue_music", "drawable", BuildConfig.APPLICATION_ID), null);
            temp2 = modRes.getDrawable(modRes.getIdentifier("ic_close", "drawable", BuildConfig.APPLICATION_ID), null);
        } catch (PackageManager.NameNotFoundException e) {
            temp = null;
            temp2 = null;
        }
        openQueueDrawable = temp;
        closeDrawable = temp2;
        // Setup
        setLayoutManager(new LinearLayoutManager(getContext()));
        setItemAnimator(new DefaultItemAnimator());
        setBackgroundColor(Color.WHITE);
        setClickable(true);
        setVisibility(GONE);
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
        queueButton.setImageDrawable(openQueueDrawable);
        setToken(token);
    }

    @SuppressWarnings("unused")
    public void resolveIntent(Intent intent) {
        if (intent.hasExtra(TRACK_INFO_EXTRA)) {
            int position = intent.getIntExtra(CURRENT_PLAYING_POSITION_EXTRA, 0);

            List<TrackItem> tracks = new ArrayList<>();
            List<Bundle> trackBundles = intent.getParcelableArrayListExtra(TRACK_INFO_EXTRA);
            for (int i = 0; i < trackBundles.size(); i++) {
                tracks.add(new TrackItem(trackBundles.get(i)));
            }
            enable(tracks, position, (PendingIntent) intent.getParcelableExtra(REPLY_INTENT_EXTRA), null);
        }
    }

    public void setToken(MediaSession.Token token) {
        mediaToken = token;
        if (mediaToken != null) {
            MediaController controller = new MediaController(getContext(), mediaToken);
            List<MediaSession.QueueItem> queue = controller.getQueue();
            if (queue != null) {
                Log.i("XMNTS", controller.getPackageName() + " has queue");
                List<TrackItem> tracks = new ArrayList<>();
                int position = 0;
                for (int i = 0; i < queue.size(); i++) {
                    MediaSession.QueueItem item = queue.get(i);
                    MediaDescription description = item.getDescription();
                    TrackItem track = new TrackItem()
                            .setTitle(String.valueOf(description.getTitle()))
                            .setArtist(String.valueOf(description.getSubtitle()))
                            .setDuration("")
                            .setArt(description.getIconUri());
                    track.id = item.getQueueId();
                    if (controller.getPlaybackState() != null && track.id == controller.getPlaybackState().getActiveQueueItemId()) {
                        position = i;
                    }
                    tracks.add(track);
                }
                enable(tracks, position, null, controller.getTransportControls());
            }
        }
    }

    public boolean hasToken() {
        return mediaToken != null;
    }

    private void enable(List<TrackItem> tracks, int position, PendingIntent reply, MediaController.TransportControls controls) {
        setAdapter(new CustomTrackAdapter(getContext(), tracks, position, reply, controls, mCloseRunnable));
        scrollToPosition(position > 0 ? position - 1 : position);
        bringToFront();
        queueButton.bringToFront();
        queueButton.setVisibility(VISIBLE);
    }

    public void reveal() {
        if (originalHeight == 0) {
            originalHeight = getRoot().getLayoutParams().height;
        }
        final Animation expandAnimation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                getRoot().getLayoutParams().height = (int) (originalHeight + (((density * 256) - originalHeight) * interpolatedTime));
                getRoot().requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        expandAnimation.setDuration(200);
        expandAnimation.setInterpolator(getContext(), android.R.interpolator.accelerate_decelerate);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                startAnimation(expandAnimation);
            }
        }, 100);
        setVisibility(VISIBLE);
        final Animator revealAnimation = ViewAnimationUtils.createCircularReveal(this, (int) (queueButton.getX() + queueButton.getWidth() / 2), (int) (queueButton.getY() + queueButton.getHeight() / 2), density * 24, density * 500);
        revealAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                queueButton.setImageDrawable(closeDrawable);
            }
        });
        revealAnimation.setDuration(400);
        revealAnimation.start();
    }

    public void close() {
        mCloseRunnable.run();
    }

    private ViewGroup getRoot() {
        return (ViewGroup) getParent();
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