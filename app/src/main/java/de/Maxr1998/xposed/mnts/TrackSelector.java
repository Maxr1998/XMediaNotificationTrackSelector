package de.Maxr1998.xposed.mnts;

import android.app.Notification;
import android.content.Context;
import android.media.session.MediaSession;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.lang.reflect.Method;

import de.Maxr1998.xposed.mnts.view.TrackSelectorView;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.callbacks.XCallback;

import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

public class TrackSelector {

    public static final String PACKAGE_NAME = "com.android.systemui";

    public static final String CURRENT_PLAYING_POSITION_EXTRA = "current_queue_position";
    public static final String TRACK_INFO_EXTRA = "track_data";
    public static final String REPLY_INTENT_EXTRA = "reply";

    static void initUI(final XC_LoadPackage.LoadPackageParam lPParam) {
        try {
            // Edit notification view
            findAndHookMethod("android.widget.RemoteViews", lPParam.classLoader, "performApply", View.class, ViewGroup.class, findClass("android.widget.RemoteViews.OnClickHandler", lPParam.classLoader), new XC_MethodHook(XCallback.PRIORITY_HIGHEST) {
                @Override
                protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                    View appliedView = (View) param.args[0];
                    ViewGroup parentView = (ViewGroup) param.args[1];
                    Class notificationViewClass = findClass(PACKAGE_NAME + ".statusbar.NotificationContentView", lPParam.classLoader);
                    if (parentView != null && notificationViewClass.isInstance(parentView)) {
                        RelativeLayout notificationView;
                        if (appliedView instanceof RelativeLayout &&
                                (((notificationView = (RelativeLayout) appliedView).getTag() != null && notificationView.getTag().toString().matches("bigMedia(Narrow)?"))
                                        || (notificationView.getChildCount() == 4
                                        && (notificationView.getChildAt(0) instanceof FrameLayout || notificationView.getChildAt(0) instanceof ImageView)
                                        && notificationView.getChildAt(1) instanceof LinearLayout
                                        && notificationView.getChildAt(2) instanceof LinearLayout
                                        && notificationView.getChildAt(3) instanceof ImageView)
                                )) {
                            notificationView.setTag("XMNTS");
                            addToNotificationContent(notificationView, null);
                        }
                    }
                }
            });

            findAndHookMethod(PACKAGE_NAME + ".statusbar.ExpandableNotificationRow", lPParam.classLoader, "setStatusBarNotification", StatusBarNotification.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    final Bundle extras = ((StatusBarNotification) param.args[0]).getNotification().extras;
                    MediaSession.Token token = extras.getParcelable(Notification.EXTRA_MEDIA_SESSION);
                    if (token != null)
                        addToNotificationContent((View) callMethod(callMethod(param.thisObject, "getPrivateLayout"), "getExpandedChild"), token);
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

    private static void addToNotificationContent(View notification, MediaSession.Token token) {
        if (!(notification instanceof ViewGroup)) {
            return;
        }
        ViewGroup expandedView = (ViewGroup) notification;
        for (int i = 0; i < expandedView.getChildCount(); i++) {
            View child = expandedView.getChildAt(i);
            if (child instanceof TrackSelectorView) {
                if (token != null && false /* false for now */) {
                    ((TrackSelectorView) child).setToken(token);
                }
                return;
            }
        }
        final Context mContext = expandedView.getContext();
        final ImageView queueButton = new ImageView(mContext);
        final TrackSelectorView trackSelectorView = new TrackSelectorView(mContext, queueButton, token);
        final float density = trackSelectorView.density;

        queueButton.setVisibility(View.GONE);
        queueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (trackSelectorView.getVisibility() == View.GONE) {
                    trackSelectorView.reveal();
                } else {
                    trackSelectorView.close();
                }
            }
        });
        queueButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        // Prevent text overlapping queue button
        View titleView = expandedView.findViewById(android.R.id.title);
        if (titleView != null) {
            ViewGroup.MarginLayoutParams titleContainerParams = (ViewGroup.MarginLayoutParams) titleView.getLayoutParams();
            titleContainerParams.rightMargin = (int) (density * 48);
            titleContainerParams.setMarginEnd(titleContainerParams.rightMargin);
        }
        // Add views
        expandedView.addView(trackSelectorView, expandedView.getChildCount(), new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ViewGroup.MarginLayoutParams buttonParams;
        if (expandedView instanceof RelativeLayout) {
            buttonParams = new RelativeLayout.LayoutParams((int) (density * 48), (int) (density * 48));
            ((RelativeLayout.LayoutParams) buttonParams).addRule(RelativeLayout.ALIGN_PARENT_TOP);
            ((RelativeLayout.LayoutParams) buttonParams).addRule(RelativeLayout.ALIGN_PARENT_END);
        } else if (expandedView instanceof FrameLayout) {
            buttonParams = new FrameLayout.LayoutParams((int) (density * 48), (int) (density * 48));
            ((FrameLayout.LayoutParams) buttonParams).gravity = Gravity.TOP | Gravity.END;
        } else {
            buttonParams = new ViewGroup.MarginLayoutParams((int) (density * 48), (int) (density * 48));
        }
        expandedView.addView(queueButton, expandedView.getChildCount(), buttonParams);
    }
}