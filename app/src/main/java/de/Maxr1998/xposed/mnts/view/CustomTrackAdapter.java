package de.Maxr1998.xposed.mnts.view;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;

import de.Maxr1998.trackselectorlib.TrackItem;
import de.Maxr1998.xposed.mnts.BuildConfig;

import static de.robv.android.xposed.XposedBridge.log;

public class CustomTrackAdapter extends RecyclerView.Adapter<CustomTrackAdapter.TrackViewHolder> {

    public static final String SEEK_COUNT_EXTRA = "new_queue_position";

    private final ContentResolver contentResolver;
    private final List<Bundle> mList;
    private final int mCurrentPosition;
    private final PendingIntent mReply;
    private final Runnable mCloseRunnable;

    private int layoutId;
    private Resources res;

    private HashMap<Uri, WeakReference<Bitmap>> bitmapCache = new HashMap<>();

    public CustomTrackAdapter(Context context, List<Bundle> list, int position, PendingIntent replyIntent, Runnable closeRunnable) {
        contentResolver = context.getContentResolver();
        mList = list;
        mCurrentPosition = position;
        mReply = replyIntent;
        mCloseRunnable = closeRunnable;
    }

    @Override
    public TrackViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (res == null || layoutId == 0) {
            try {
                res = parent.getContext().getPackageManager().getResourcesForApplication(BuildConfig.APPLICATION_ID);
            } catch (PackageManager.NameNotFoundException e) {
                res = parent.getContext().getResources();
            }
            layoutId = res.getIdentifier("track_item_view", "layout", BuildConfig.APPLICATION_ID);
        }
        return new TrackViewHolder(LayoutInflater.from(parent.getContext()).inflate(res.getLayout(layoutId), parent, false));
    }

    @Override
    public void onBindViewHolder(final TrackViewHolder holder, int position) {
        TrackItem item = new TrackItem(mList.get(position));
        Bitmap art = item.getArt();
        Uri uri = item.getArtUri();
        if (art != null) {
            holder.art.setImageBitmap(art);
        } else if (uri != null) {
            try {
                WeakReference<Bitmap> cachedReference = bitmapCache.get(uri);
                if (cachedReference == null || (art = cachedReference.get()) == null) {
                    art = MediaStore.Images.Media.getBitmap(contentResolver, uri);
                    bitmapCache.put(uri, new WeakReference<>(art));
                }
                holder.art.setImageBitmap(art);
            } catch (IOException e) {
                log(e);
                holder.art.setImageDrawable(null);
            }
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
                if (mCloseRunnable != null) {
                    mCloseRunnable.run();
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