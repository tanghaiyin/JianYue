package com.example.musicplayer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.KeyEvent;

import com.example.musicplayer.activity.MainActivity;
import com.example.musicplayer.activity.PlayActive;
import com.example.musicplayer.application.ClearContent;
import com.example.musicplayer.service.MusicService;

public class MediaReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        String intentAction = intent.getAction();
//        if (!Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
//            return;
//        }
        KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

//        if (event == null) {
//            return;
//        }
        int action = event.getAction();
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_MEDIA_PLAY:
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
            case KeyEvent.KEYCODE_HEADSETHOOK:
                if (action == KeyEvent.ACTION_DOWN) {
                    if (event.getRepeatCount() > 0) {
                        MusicService.mediaPlayer.seekTo(MusicService.mediaPlayer.getCurrentPosition() + (event.getRepeatCount()) * 10);
                        return;
                    }
                    ClearContent.count++;
                    Handler handler = new Handler();
                    Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            if (ClearContent.count == 1) {

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
                                    } else {
                                        MusicService.mediaPlayer.start();
                                        Intent intentBroadcastReceiver = new Intent();
                                        intentBroadcastReceiver.putExtra("sendMain", "noPlaying");
                                        intentBroadcastReceiver.setAction(MainActivity.MAIN_SERVICE_NEED);
                                        ClearContent.context.sendBroadcast(intentBroadcastReceiver);

                                        Intent intentBroadcastReceiverPl = new Intent();
                                        intentBroadcastReceiverPl.putExtra("sendMain", "noPlaying");
                                        intentBroadcastReceiverPl.setAction(PlayActive.ACTION_SERVICE_NEED);
                                        ClearContent.context.sendBroadcast(intentBroadcastReceiverPl);
                                    }
                                } else {
                                    sendMsg("noServiceToOpenMain");
                                }
                            }
                            if (ClearContent.count == 2) {
                                sendMsg("isServiceNext");
                            }
                            if (ClearContent.count == 3) {
                                sendMsg("isServicePre");
                            }
                            ClearContent.count = 0;
                        }
                    };
                    if (ClearContent.count == 1) {
                        handler.postDelayed(r, 1000);
                    }
                }
                break;
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                if (action == KeyEvent.ACTION_DOWN) {
                    if (event.getRepeatCount() > 0) {
                        MusicService.mediaPlayer.seekTo(MusicService.mediaPlayer.getCurrentPosition() + (event.getRepeatCount()) * 10);
                        return;
                    }
                    sendMsg("isServiceNext");
                }
                break;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                if (action == KeyEvent.ACTION_DOWN) {
                    if (event.getRepeatCount() > 0) {
                        MusicService.mediaPlayer.seekTo(MusicService.mediaPlayer.getCurrentPosition() - (event.getRepeatCount()) * 10);
                        return;
                    }
                    sendMsg("isServicePre");
                }
                break;
        }
    }

    private void sendMsg(String msg) {
        if (MainActivity.context != null) {
            Intent intentBroadcastReceiver = new Intent();
            intentBroadcastReceiver.putExtra("sendMain", msg);
            intentBroadcastReceiver.setAction(MainActivity.MAIN_SERVICE_NEED);
            ClearContent.context.sendBroadcast(intentBroadcastReceiver);

            Intent intentBroadcastReceiverPl = new Intent();
            intentBroadcastReceiverPl.putExtra("sendMain", msg);
            intentBroadcastReceiverPl.setAction(PlayActive.ACTION_SERVICE_NEED);
            ClearContent.context.sendBroadcast(intentBroadcastReceiverPl);
        } else if (PlayActive.context != null) {
            Intent intentBroadcastReceiver = new Intent();
            intentBroadcastReceiver.putExtra("sendMain", msg);
            intentBroadcastReceiver.setAction(MainActivity.MAIN_SERVICE_NEED);
            ClearContent.context.sendBroadcast(intentBroadcastReceiver);

            Intent intentBroadcastReceiverPl = new Intent();
            intentBroadcastReceiverPl.putExtra("sendMain", msg);
            intentBroadcastReceiverPl.setAction(PlayActive.ACTION_SERVICE_NEED);
            ClearContent.context.sendBroadcast(intentBroadcastReceiverPl);
        }
    }
}