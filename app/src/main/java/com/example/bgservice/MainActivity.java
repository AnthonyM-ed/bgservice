package com.example.bgservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.os.Handler;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ImageButton btnPlay, btnPause, btnStop;
    private TextView tvFileName;
    private SeekBar seekBar;
    private Handler handler = new Handler();
    private Runnable updateProgressRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnPlay = findViewById(R.id.btnPlay);
        btnPause = findViewById(R.id.btnPause);
        btnStop = findViewById(R.id.btnStop);
        tvFileName = findViewById(R.id.tvFileName);
        seekBar = findViewById(R.id.seekBar);

        btnPlay.setOnClickListener(v -> {
            Intent playIntent = new Intent(this, MusicService.class);
            playIntent.setAction("ACTION_PLAY");
            startService(playIntent);
            tvFileName.setText("Playing: Never Gonna Give You Up");
            startUpdatingProgress();
        });

        btnPause.setOnClickListener(v -> {
            Intent pauseIntent = new Intent(this, MusicService.class);
            pauseIntent.setAction("ACTION_PAUSE");
            startService(pauseIntent);
            stopUpdatingProgress();
        });

        btnStop.setOnClickListener(v -> {
            Intent stopIntent = new Intent(this, MusicService.class);
            stopIntent.setAction("ACTION_STOP_FOREGROUND");
            startService(stopIntent);
            tvFileName.setText("Press play to start");
            stopUpdatingProgress();
            seekBar.setProgress(0);
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && MusicService.mediaPlayer != null) {
                    MusicService.mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void startUpdatingProgress() {
        seekBar.setProgress(0);
        updateProgressRunnable = new Runnable() {
            @Override
            public void run() {
                if (MusicService.mediaPlayer != null && MusicService.mediaPlayer.isPlaying()) {
                    seekBar.setMax(MusicService.mediaPlayer.getDuration());
                    seekBar.setProgress(MusicService.mediaPlayer.getCurrentPosition());
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(updateProgressRunnable);
    }

    private void stopUpdatingProgress() {
        if (updateProgressRunnable != null) {
            handler.removeCallbacks(updateProgressRunnable);
        }
    }

    private BroadcastReceiver musicStoppedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            tvFileName.setText("Press play to start");
            seekBar.setProgress(0);
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(musicStoppedReceiver,
                new IntentFilter("MUSIC_STOPPED"));

        Intent intent = new Intent(this, MusicService.class);
        intent.setAction("ACTION_SET_ACTIVITY_VISIBLE");
        startService(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(musicStoppedReceiver);

        Intent intent = new Intent(this, MusicService.class);
        intent.setAction("ACTION_SET_ACTIVITY_INVISIBLE");
        startService(intent);
    }
}
