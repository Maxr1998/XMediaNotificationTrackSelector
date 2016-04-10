package de.Maxr1998.xposed.mnts;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.lang.reflect.Method;

import de.Maxr1998.xposed.mnts.view.TrackSelectorView;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.callbacks.XCallback;

import static android.widget.RelativeLayout.TRUE;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

public class TrackSelector {

    public static final int INTENT_VIEW_ID = 0x7f0f0200;
    public static final String CURRENT_PLAYING_POSITION_EXTRA = "current_queue_position";
    public static final String TRACK_INFO_EXTRA = "track_data";
    public static final String REPLY_INTENT_EXTRA = "reply";

    public static void initUI(final XC_LoadPackage.LoadPackageParam lPParam) {
        try {
            // Edit notification view
            findAndHookMethod("android.widget.RemoteViews", lPParam.classLoader, "performApply", View.class, ViewGroup.class, findClass("android.widget.RemoteViews.OnClickHandler", lPParam.classLoader), new XC_MethodHook(XCallback.PRIORITY_HIGHEST) {
                @Override
                protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                    View notificationView = (View) param.args[0];
                    if (notificationView instanceof RelativeLayout) {
                        final RelativeLayout root = (RelativeLayout) notificationView;
                        if ((root.getChildCount() == 4
                                && (root.getChildAt(0) instanceof FrameLayout || root.getChildAt(0) instanceof ImageView)
                                && root.getChildAt(1) instanceof LinearLayout
                                && root.getChildAt(2) instanceof LinearLayout
                                && root.getChildAt(3) instanceof ImageView)
                                || (root.getTag() != null && root.getTag().toString().matches("bigMedia(Narrow)?"))) {
                            root.setTag("xgpmMedia");
                            // final vars
                            final Context mContext = root.getContext();
                            final Resources res = root.getResources();
                            final float density = res.getDisplayMetrics().density;
                            final ViewGroup.LayoutParams rootParams = root.getLayoutParams();
                            // Views
                            final ImageView queueButton = new ImageView(mContext);
                            final TrackSelectorView trackSelectorView = new TrackSelectorView(mContext, queueButton);
                            // Close callback
                            final Runnable closeRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    // Close
                                    final Animator revealCloseAnimation = ViewAnimationUtils.createCircularReveal(trackSelectorView, (int) (queueButton.getX() + queueButton.getWidth() / 2), (int) (queueButton.getY() + queueButton.getHeight() / 2), density * 500, density * 24);
                                    revealCloseAnimation.addListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            trackSelectorView.setVisibility(View.GONE);
                                            try {
                                                Resources modRes = mContext.createPackageContext(BuildConfig.APPLICATION_ID, 0).getResources();
                                                queueButton.setImageDrawable(modRes.getDrawable(modRes.getIdentifier("ic_queue_music", "drawable", BuildConfig.APPLICATION_ID), null));
                                            } catch (PackageManager.NameNotFoundException e) {
                                                log(e);
                                            }
                                        }
                                    });
                                    revealCloseAnimation.start();
                                    final Animation collapseAnimation = new Animation() {
                                        @Override
                                        protected void applyTransformation(float interpolatedTime, Transformation t) {
                                            rootParams.height = (int) (density * 128 * (2f - interpolatedTime));
                                            root.requestLayout();
                                        }

                                        @Override
                                        public boolean willChangeBounds() {
                                            return true;
                                        }
                                    };
                                    collapseAnimation.setDuration(200);
                                    collapseAnimation.setInterpolator(mContext, android.R.interpolator.accelerate_decelerate);
                                    root.startAnimation(collapseAnimation);
                                }
                            };
                            trackSelectorView.setCloseRunnable(closeRunnable);
                            //noinspection ResourceType
                            trackSelectorView.setId(INTENT_VIEW_ID);
                            // Queue button
                            queueButton.setVisibility(View.GONE);
                            queueButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (trackSelectorView.getVisibility() == View.VISIBLE) {
                                        closeRunnable.run();
                                        return;
                                    }
                                    final Animation expandAnimation = new Animation() {
                                        @Override
                                        protected void applyTransformation(float interpolatedTime, Transformation t) {
                                            rootParams.height = (int) (density * 128 * (1f + interpolatedTime));
                                            root.requestLayout();
                                        }

                                        @Override
                                        public boolean willChangeBounds() {
                                            return true;
                                        }
                                    };
                                    expandAnimation.setDuration(200);
                                    expandAnimation.setInterpolator(mContext, android.R.interpolator.accelerate_decelerate);
                                    root.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            root.startAnimation(expandAnimation);
                                        }
                                    }, 100);
                                    trackSelectorView.setVisibility(View.VISIBLE);
                                    final Animator revealAnimation = ViewAnimationUtils.createCircularReveal(trackSelectorView, (int) (v.getX() + v.getWidth() / 2), (int) (v.getY() + v.getHeight() / 2), density * 24, density * 500);
                                    revealAnimation.addListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            try {
                                                Resources modRes = mContext.createPackageContext(BuildConfig.APPLICATION_ID, 0).getResources();
                                                queueButton.setImageDrawable(modRes.getDrawable(modRes.getIdentifier("ic_close", "drawable", BuildConfig.APPLICATION_ID), null));
                                            } catch (PackageManager.NameNotFoundException e) {
                                                log(e);
                                            }

                                        }
                                    });
                                    revealAnimation.setDuration(400);
                                    revealAnimation.start();
                                }
                            });
                            try {
                                Resources modRes = mContext.createPackageContext(BuildConfig.APPLICATION_ID, 0).getResources();
                                queueButton.setImageDrawable(modRes.getDrawable(modRes.getIdentifier("ic_queue_music", "drawable", BuildConfig.APPLICATION_ID), null));
                            } catch (PackageManager.NameNotFoundException e) {
                                log(e);
                            }
                            queueButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                            RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams((int) (density * 48), (int) (density * 48));
                            buttonParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, TRUE);
                            buttonParams.addRule(RelativeLayout.ALIGN_PARENT_END, TRUE);
                            // Prevent text overlapping queue button
                            ViewGroup.MarginLayoutParams titleContainerParams = (ViewGroup.MarginLayoutParams) root.getChildAt(1).getLayoutParams();
                            titleContainerParams.rightMargin = (int) (density * 48);
                            titleContainerParams.setMarginEnd(titleContainerParams.rightMargin);
                            // Add views
                            root.addView(trackSelectorView, root.getChildCount(), new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                            root.addView(queueButton, root.getChildCount(), buttonParams);
                        }
                    }
                }
            });
            // Allow resolveIntent to be called as RemoteView
            findAndHookMethod("java.lang.reflect.Method", lPParam.classLoader, "isAnnotationPresent", Class.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (((Method) param.thisObject).getName().equals("resolveIntent") && ((Class) param.args[0]).getName().equals("android.view.RemotableViewMethod")) {
                        param.setResult(true);
                    }
                }
            });
        } catch (Throwable t) {
            log(t);
        }
    }
}
