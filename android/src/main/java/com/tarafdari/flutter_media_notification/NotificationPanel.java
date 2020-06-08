package com.tarafdari.flutter_media_notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.graphics.Bitmap;
import android.widget.Toast;
import java.io.InputStream;
import java.net.URL;
import java.io.IOException;
import android.graphics.Color;
import java.net.HttpURLConnection;
import android.content.res.AssetFileDescriptor;
import java.io.File;

import androidx.core.app.NotificationCompat;
import androidx.media.session.MediaButtonReceiver;
import android.content.res.AssetManager;

public class NotificationPanel extends Service {
    public static int NOTIFICATION_ID = 1;
    public static final String CHANNEL_ID = "flutter_media_notification";
    public static final String MEDIA_SESSION_TAG = "flutter_media_notification";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean isPlaying = intent.getBooleanExtra("isPlaying", true);
        String title = intent.getStringExtra("title");
        String author = intent.getStringExtra("author");
        String imageUrl = intent.getStringExtra("imageUrl");
        boolean hasNext = intent.getBooleanExtra("hasNext", false);
        boolean hasPrev = intent.getBooleanExtra("hasPrev", false);

        createNotificationChannel();

        MediaSessionCompat mediaSession = new MediaSessionCompat(this, MEDIA_SESSION_TAG);

        int iconPlayPause = R.drawable.baseline_play_arrow_black_48;
        String titlePlayPause = "pause";
        if (isPlaying) {
            iconPlayPause = R.drawable.baseline_pause_black_48;
            titlePlayPause = "play";
        }

        Intent toggleIntent = new Intent(this, NotificationReturnSlot.class).setAction("toggle")
                .putExtra("title", title).putExtra("author", author).putExtra("play", !isPlaying)
                .putExtra("imageUrl", imageUrl).putExtra("hasNext", hasNext).putExtra("hasPrev", hasPrev);
        PendingIntent pendingToggleIntent = PendingIntent.getBroadcast(this, 0, toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        MediaButtonReceiver.handleIntent(mediaSession, toggleIntent);

        // TODO(ALI): add media mediaSession Buttons and handle them
        Intent nextIntent = new Intent(this, NotificationReturnSlot.class).setAction("next");
        PendingIntent pendingNextIntent = PendingIntent.getBroadcast(this, 0, nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        // MediaButtonReceiver.handleIntent(mediaSession, nextIntent);

        Intent prevIntent = new Intent(this, NotificationReturnSlot.class).setAction("prev");
        PendingIntent pendingPrevIntent = PendingIntent.getBroadcast(this, 0, prevIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // MediaButtonReceiver.handleIntent(mediaSession, prevIntent);

        Intent selectIntent = new Intent(this, NotificationReturnSlot.class).setAction("select");
        PendingIntent selectPendingIntent = PendingIntent.getBroadcast(this, 0, selectIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        // MediaButtonReceiver.handleIntent(mediaSession, selectIntent);

        // File imgFile = new
        // File("/storage/emulated/0/Android/data/hu.encosoft.radio/files/" + imageUrl);
        // Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0, 1, 2)
                        .setShowCancelButton(true).setMediaSession(mediaSession.getSessionToken()))
                .setSmallIcon(R.drawable.manna_icon_small)
                // .setSmallIcon(imgFile)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC).setVibrate(new long[] { 0L })
                .setPriority(NotificationCompat.PRIORITY_MIN).setContentTitle(title).setContentText(author)
                .setSubText(title).setContentIntent(selectPendingIntent)
                // .setLargeIcon(myBitmap)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.manna_icon_small))
                // .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), fd))
                .setColor(Color.parseColor("#1D1E42"));

        if (hasPrev) {
            builder.addAction(R.drawable.baseline_skip_previous_black_48, "prev", pendingPrevIntent);
        } else {
            builder.addAction(R.drawable.empty_action, "prev", pendingPrevIntent);
        }
        builder.addAction(iconPlayPause, titlePlayPause, pendingToggleIntent);
        if (hasNext) {
            builder.addAction(R.drawable.baseline_skip_next_black_48, "next", pendingNextIntent);
        } else {
            builder.addAction(R.drawable.empty_action, "next", pendingNextIntent);
        }

        Notification notification = builder.build();

        startForeground(NOTIFICATION_ID, notification);
        if (!isPlaying) {
            stopForeground(false);
        }
        return START_NOT_STICKY;

    }

    public static Bitmap getBitmapFromUrl(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(CHANNEL_ID, "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_LOW);
            serviceChannel.setDescription("flutter_media_notification");
            serviceChannel.setShowBadge(false);
            serviceChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            NotificationManager manager = getSystemService(NotificationManager.class);
            assert manager != null;
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        stopSelf();
        android.os.Process.killProcess(android.os.Process.myPid());
        stopForeground(true);
    }
}
