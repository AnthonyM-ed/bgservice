package com.example.bgservice;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.core.app.NotificationCompat;

public class MusicService extends Service {
    public static MediaPlayer mediaPlayer;
    private boolean isPaused = false;
    private static final String CHANNEL_ID = "MusicServiceChannel";
    private boolean isActivityVisible = false;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = MediaPlayer.create(this, R.raw.rick_astley_never_gonna_give_you_up);
        mediaPlayer.setLooping(false);
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getAction() == null) {
            return START_NOT_STICKY;
        }

        switch (intent.getAction()) {
            case "ACTION_PLAY":
                startMusic();
                isActivityVisible = true;
                stopForeground(true);
                break;
            case "ACTION_PAUSE":
                pauseMusic();
                break;
            case "ACTION_STOP_FOREGROUND":
                stopMusic();
                break;
            case "ACTION_SET_ACTIVITY_VISIBLE":
                isActivityVisible = true;
                stopForeground(true);
                break;
            case "ACTION_SET_ACTIVITY_INVISIBLE":
                isActivityVisible = false;
                break;
        }

        // Actualiza la notificación solo si la actividad no está visible
        if (!isActivityVisible && mediaPlayer != null && mediaPlayer.isPlaying()) {
            startForeground(1, buildNotification("Playing Music"));
        }

        return START_STICKY;
    }

    private void startMusic() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.rick_astley_never_gonna_give_you_up);
        }

        if (isPaused) {
            mediaPlayer.start();
            isPaused = false;
        } else if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }

        if (!isActivityVisible) {
            startForeground(1, buildNotification("Playing music"));
        }
    }

    public void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPaused = true;
        }
    }

    private void stopMusic() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }

        Intent intent = new Intent("MUSIC_STOPPED");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        stopForeground(true); // Esto eliminará la notificación
        stopSelf();
    }

    private Notification buildNotification(String contentText) {
        PendingIntent stopPendingIntent = PendingIntent.getService(
                this,
                0,
                new Intent(this, MusicService.class).setAction("ACTION_STOP_FOREGROUND"),
                PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Music Player")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_music_note)
                .addAction(R.drawable.ic_stop_small, "STOP", stopPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Music Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        stopForeground(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
