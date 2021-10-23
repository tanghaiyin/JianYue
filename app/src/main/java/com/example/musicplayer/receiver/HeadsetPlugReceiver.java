package com.example.musicplayer.receiver;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.musicplayer.activity.MainActivity;
import com.example.musicplayer.activity.PlayActive;
import com.example.musicplayer.application.ClearContent;
import com.example.musicplayer.service.MusicService;

public class HeadsetPlugReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)) {
            Log.e("bluetooth", "BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED");
        }

        if (intent.hasExtra("state")) {
            if (intent.getIntExtra("state", 0) == 0) {
                if (MusicService.mediaPlayer != null) {
                    if (MusicService.mediaPlayer.isPlaying()) {
                        MusicService.mediaPlayer.pause();
                        Intent intentBroadcastReceiver = new Intent();
                        intentBroadcastReceiver.putExtra("sendMain", "Playing");
                        intentBroadcastReceiver.setAction(MainActivity.MAIN_SERVICE_NEED);
                        ClearContent.context.sendBroadcast(intentBroadcastReceiver);

                        Intent intentBroadcastReceiverPl = new Intent();
                        intentBroadcastReceiverPl.putExtra("sendMain", "Playing");
                        intentBroadcastReceiverPl.setAction(PlayActive.ACTION_SERVICE_NEED);
                        ClearContent.context.sendBroadcast(intentBroadcastReceiverPl);
                    }
                }
            } else if (intent.getIntExtra("state", 0) == 1) {
                Log.e("连接成功", "2233");
            }
        }
    }
}