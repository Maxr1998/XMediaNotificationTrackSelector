package de.Maxr1998.xposed.mnts.view;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;

import java.util.List;

import de.Maxr1998.trackselectorlib.TrackItem;
import de.Maxr1998.xposed.mnts.BuildConfig;

import static de.robv.android.xposed.XposedBridge.log;

public class CustomTrackAdapter extends RecyclerView.Adapter<CustomTrackAdapter.TrackViewHolder> {

    public static final String SEEK_COUNT_EXTRA = "new_queue_position";

    public View.OnClickListener mCloseHandler;

    private List<Bundle> mList;
    private int mCurrentPosition = 0;
    private PendingIntent mReply;

    public CustomTrackAdapter(List<Bundle> list, int position, PendingIntent replyIntent) {
        mList = list;
        mCurrentPosition = position;
        mReply = replyIntent;
    }

    @Override
    public TrackViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Resources res;
        try {
            res = parent.getContext().getPackageManager().getResourcesForApplication(BuildConfig.APPLICATION_ID);
        } catch (PackageManager.NameNotFoundException e) {
            res = parent.getContext().getResources();
        }
        XmlPullParser parser = res.getLayout(res.getIdentifier("track_item_view", "layout", BuildConfig.APPLICATION_ID));
        return new TrackViewHolder(LayoutInflater.from(parent.getContext()).inflate(parser, parent, false));
    }

    @Override
    public void onBindViewHolder(final TrackViewHolder holder, int position) {
        TrackItem item = new TrackItem(mList.get(position));
        Bitmap art = item.getArt();
        if (art != null) {
            holder.art.setImageBitmap(art);
        } else {
            holder.art.setImageDrawable(null);
        }
        holder.title.setText(position == mCurrentPosition ? getBoldString(item.getTitle()) : item.getTitle());
        holder.artist.setText(item.getArtist());
        holder.duration.setText(item.getDuration());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra(SEEK_COUNT_EXTRA, holder.getAdapterPosition() - mCurrentPosition);
                try {
                    mReply.send(holder.itemView.getContext(), 0, intent);
                } catch (PendingIntent.CanceledException e) {
                    log(e);
                }
                if (mCloseHandler != null) {
                    mCloseHandler.onClick(v);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    private SpannableString getBoldString(String toBold) {
        SpannableString sp = new SpannableString(toBold);
        sp.setSpan(new StyleSpan(Typeface.BOLD), 0, sp.length(), 0);
        return sp;
    }

    public static class TrackViewHolder extends RecyclerView.ViewHolder {

        public ImageView art;
        public TextView title;
        public TextView artist;
        public TextView duration;

        public TrackViewHolder(View view) {
            super(view);
            art = (ImageView) itemView.findViewById(android.R.id.icon);
            title = (TextView) itemView.findViewById(android.R.id.title);
            artist = (TextView) itemView.findViewById(android.R.id.text1);
            duration = (TextView) itemView.findViewById(android.R.id.text2);
        }
    }
}