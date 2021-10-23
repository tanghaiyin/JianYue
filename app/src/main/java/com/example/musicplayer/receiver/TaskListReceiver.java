package com.example.musicplayer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.musicplayer.activity.MainActivity;
import com.example.musicplayer.activity.PlayActive;
import com.example.musicplayer.service.MusicService;
import com.example.musicplayer.tool.WriteCof;

public class TaskListReceiver extends BroadcastReceiver {
    WriteCof writeCof;

    @Override
    public void onReceive(Context context, Intent intent) {
        writeCof = new WriteCof();
        String reason = intent.getStringExtra("reason");
        if (reason != null) {
            switch (reason) {
                case "recentapps":
                case "assist":
                case "home":
                    MusicService.SimpleBinder ms = MainActivity.mBinder;
                    if (ms == null) {
                        ms = PlayActive.mBinder;
                    }
                    writeCof.writeCof(ms, context);
                    break;
                default:
                    break;
            }
        }
    }
}
