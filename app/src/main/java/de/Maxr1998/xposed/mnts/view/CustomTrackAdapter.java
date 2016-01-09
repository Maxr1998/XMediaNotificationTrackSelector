package de.Maxr1998.xposed.mnts.view;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import de.Maxr1998.trackselectorlib.TrackItem;

public class CustomTrackAdapter extends ArrayAdapter<Bundle> {

    private int mCurrentPosition = 0;
    private PendingIntent mReply;

    @SuppressWarnings("unused")
    public CustomTrackAdapter(Context context, int position, PendingIntent replyIntent) {
        super(context, 0);
        mCurrentPosition = position;
        mReply = replyIntent;
    }

    public CustomTrackAdapter(Context context, int position, List<Bundle> list, PendingIntent replyIntent) {
        super(context, 0, list);
        mCurrentPosition = position;
        mReply = replyIntent;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TrackItem item = new TrackItem(getItem(position));
        final float density = getContext().getResources().getDisplayMetrics().density;
        final int dp48 = (int) (density * 48);
        LinearLayout trackLayout = new LinearLayout(getContext());
        trackLayout.setOrientation(LinearLayout.HORIZONTAL);
        trackLayout.setGravity(Gravity.CENTER_VERTICAL);
        trackLayout.setMinimumHeight(dp48);
        ShapeDrawable mask = new ShapeDrawable(new RectShape());
        mask.getPaint().setColor(Color.WHITE);
        trackLayout.setBackground(new RippleDrawable(ColorStateList.valueOf(Color.parseColor("#1f000000")), null, mask));
        trackLayout.setPadding(dp48 / 4, (int) (density * 6), dp48, (int) (density * 6));
        LinearLayout.LayoutParams trackLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        trackLayout.setLayoutParams(trackLayoutParams);

        // Album art
        ImageView albumArt = new ImageView(getContext());
        Bitmap art = item.getArt();
        if (art != null) {
            albumArt.setImageBitmap(art);
        }
        albumArt.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(dp48, ViewGroup.LayoutParams.MATCH_PARENT);
        imageParams.rightMargin = (int) (density * 6);
        imageParams.setMarginEnd(imageParams.rightMargin);

        // Titles
        LinearLayout titlesLayout = new LinearLayout(getContext());
        titlesLayout.setOrientation(LinearLayout.VERTICAL);
        titlesLayout.setGravity(Gravity.CENTER | Gravity.START);
        LinearLayout.LayoutParams titlesLayoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
        titlesLayoutParams.leftMargin = dp48 / 4;
        titlesLayoutParams.setMarginStart(titlesLayoutParams.leftMargin);

        TextView titleText = new TextView(getContext());
        String title = item.getTitle();
        titleText.setText(position == mCurrentPosition ? getBoldString(title) : title);
        titleText.setTextColor(Color.parseColor("#ff212121"));
        titleText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        titleText.setEllipsize(TextUtils.TruncateAt.END);
        titleText.setSingleLine();
        titleText.setLineSpacing(0, titleText.getLineSpacingMultiplier());
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView subtitleText = new TextView(getContext());
        subtitleText.setText(item.getArtist());
        subtitleText.setTextColor(Color.parseColor("#ff616161"));
        subtitleText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        subtitleText.setEllipsize(TextUtils.TruncateAt.END);
        subtitleText.setSingleLine();
        titleText.setLineSpacing(0, titleText.getLineSpacingMultiplier());
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        titlesLayout.addView(titleText, titleParams);
        titlesLayout.addView(subtitleText, subtitleParams);

        // Duration
        TextView duration = new TextView(getContext());
        duration.setText(item.getDuration());
        duration.setTextColor(Color.parseColor("#ff616161"));
        duration.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        titleText.setEllipsize(TextUtils.TruncateAt.END);
        duration.setSingleLine();
        titleText.setLineSpacing(0, titleText.getLineSpacingMultiplier());
        duration.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams durationParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        durationParams.leftMargin = dp48 / 4;
        durationParams.setMarginStart(durationParams.leftMargin);

        // Add views
        trackLayout.addView(albumArt, imageParams);
        trackLayout.addView(titlesLayout, titlesLayoutParams);
        trackLayout.addView(duration, durationParams);
        return trackLayout;
    }

    public int getCurrentPosition() {
        return mCurrentPosition;
    }

    public PendingIntent reply() {
        return mReply;
    }

    private SpannableString getBoldString(String toBold) {
        SpannableString sp = new SpannableString(toBold);
        sp.setSpan(new StyleSpan(Typeface.BOLD), 0, sp.length(), 0);
        return sp;
    }
}