package de.Maxr1998.trackselectorlib;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

/**
 * Helper class to create {@link Bundle}s storing Track data
 */
@SuppressWarnings("unused")
public class TrackItem {
    /**
     * For storing temporary ID data, not transferred to System-UI
     */
    public long id = 0;
    private Bundle trackItem;

    public TrackItem() {
        trackItem = new Bundle(4);
    }

    public TrackItem(Bundle b) {
        trackItem = b;
    }

    public Uri getArtUri() {
        return trackItem.getParcelable(Keys.ART_URI);
    }

    /**
     * Sets the cover art of the current track
     *
     * @param u Cover {@link Uri} from media store
     * @return itself.
     */
    public TrackItem setArt(Uri u) {
        trackItem.putParcelable(Keys.ART_URI, u);
        return this;
    }

    public Bitmap getArt() {
        return trackItem.getParcelable(Keys.ART);
    }

    /**
     * Sets the cover art of the current track
     *
     * @param b Cover {@link Bitmap}, preferably scaled to 48dp * 48dp size
     * @return itself.
     */
    public TrackItem setArt(Bitmap b) {
        trackItem.putParcelable(Keys.ART, b);
        return this;
    }

    public String getTitle() {
        return trackItem.getString(Keys.TITLE);
    }

    /**
     * Sets the title of the current track
     *
     * @param t title string
     * @return itself.
     */
    public TrackItem setTitle(String t) {
        trackItem.putString(Keys.TITLE, t);
        return this;
    }

    public String getArtist() {
        return trackItem.getString(Keys.ARTIST);
    }

    /**
     * Sets the artist name of the current track
     *
     * @param a artist name String
     * @return itself.
     */
    public TrackItem setArtist(String a) {
        trackItem.putString(Keys.ARTIST, a);
        return this;
    }

    public String getDuration() {
        return trackItem.getString(Keys.DURATION);
    }

    /**
     * Sets the duration of the current track
     *
     * @param l human readable duration String in Minutes:Seconds, e.g. 03:56
     * @return itself.
     */
    public TrackItem setDuration(String l) {
        trackItem.putString(Keys.DURATION, l);
        return this;
    }

    public Bundle get() {
        return trackItem;
    }

    /**
     * Only used by {@link TrackItem}
     */
    public static class Keys {
        public static String ART = "art";
        public static String ART_URI = "uri";
        public static String TITLE = "title";
        public static String ARTIST = "artist";
        public static String DURATION = "duration";
    }
}