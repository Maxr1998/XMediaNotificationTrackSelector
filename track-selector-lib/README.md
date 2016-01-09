#Library

This library helps third-party music player devs to use the track selector of the mod.

##Installation
Modify your  `build.gradle` like this:

```Groovy

repositories {
    jCenter() // If not already present
}

dependencies {
    compile 'de.Maxr1998:track-selector-lib:1.0'
}
```

##Usage
```Java
public class MusicService extends Service {
  …

  /**
   * First, you need to create an ArrayList of the current playing queue in your playback service,
   * which can be passed to the notification
   */
  public ArrayList<Bundle> createListFromQueue() {
    ArrayList<Bundle> queue = new ArrayList<>(); // Create list

    // Cycle through your media cursor or other list storing your current queue
    …
      Create track item for each track
      TrackItem ti = new TrackItem();
      ti.setArt(<coverBitmap>)
          .setTitle(<title>)
          .setArtist(<artist>)
          .setDuration(<duration>);
      queue.add(ti.get());
    …
    return queue;
  }

  …

  /**
   * Then, when building your notification, supply the data to it.
   */
  public void updateNotification() {
    Notification n;
    … // create your Notitfication
    NotificationHelper.insertToNotification(n, createListFromQueue(), this /*Service which handles your commands*/,<currentPlayingPostition>)
  }
  …
  @Override
  public void onStartCommand(Intent intent, int a, int b) {
    // Check if intent comes from queue switcher
    if (NotificationHelper.checkIntent(intent)) {
        seekTo(NotificationHelper.getPosition(intent));
        return;
    }
  }
}
```