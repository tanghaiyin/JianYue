package com.example.musicplayer.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.example.musicplayer.activity.MainActivity;
import com.example.musicplayer.activity.PlayActive;
import com.example.musicplayer.application.ClearContent;
import com.example.musicplayer.tool.WriteCof;

import java.util.ArrayList;

public class MusicService extends Service {
    private static final String TAG = "MusicService";
    private SimpleBinder mBinder;
    public static MediaPlayer mediaPlayer = null;
    private ArrayList<String> musicListGet = null;
    private ArrayList<String> nameList = null;
    private Intent unKnowIntent;
    private int nowPlay;
    private String mod = null;
    public String nowMusicName;
    private int mathmusic = -1;
    private Context context;
    private WriteCof writeCof;

    @Override
    public void onCreate() {
        super.onCreate();
        Intent mainIntentBroadcastReceiver = new Intent();
        mainIntentBroadcastReceiver.putExtra("sendMain", "conSuccess");
        mainIntentBroadcastReceiver.setAction(MainActivity.MAIN_SERVICE_NEED);
        sendBroadcast(mainIntentBroadcastReceiver);
        mediaPlayer = new MediaPlayer();
        mBinder = new SimpleBinder();
        writeCof=new WriteCof();
        Log.d(TAG, "服务启动");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        unKnowIntent = intent;
        switch (unKnowIntent.getStringExtra("musicState")) {
            case "unStartService":
                if (intent.getIntExtra("id", 0) != -1) {
                    nowPlay = intent.getIntExtra("id", 0);
                }
                String nextOrPre = intent.getStringExtra("nextOrPre");
                if (musicListGet == null) {
                    musicListGet = intent.getStringArrayListExtra("musicUrlList");
                    nameList = intent.getStringArrayListExtra("musicNameList");
                }
                String unStartServiceMusicName = musicListGet.get(nowPlay).substring(musicListGet.get(nowPlay).lastIndexOf("/"));
                nowMusicName = unStartServiceMusicName.substring(1, unStartServiceMusicName.lastIndexOf("."));
                if (mod == null) {
                    mod = intent.getStringExtra("playMode");
                }
                switch (mod) {
                    case "随机播放":
                        if (!nextOrPre.equals("onPlay")) {
                            nowPlay = (int) (Math.random() * musicListGet.size());
                            playMusic(nowPlay);
                            updateMainUI(nowPlay, "nextMusic");
                            mediaPlayer.seekTo(intent.getIntExtra("inprenum", 0));
                        } else {
                            goPlay(intent);
                        }
                        break;
                    default:
                        if (nextOrPre.equals("pre")) {
                            if ((nowPlay - 1) < 0) {
                                playMusic(musicListGet.size() - 1);
                            } else {
                                playMusic(nowPlay - 1);
                            }
                            updateMainUI(nowPlay, "nextMusic");
                        } else if (nextOrPre.equals("next")) {
                            if ((nowPlay + 1) > musicListGet.size()) {
                                playMusic(0);
                            } else {
                                playMusic(nowPlay + 1);
                            }
                            updateMainUI(nowPlay, "nextMusic");
                        } else if (nextOrPre.equals("onPlay")) {
                            goPlay(intent);
                        } else if (nextOrPre.equals("maintoplay")) {
                            nowPlay = intent.getIntExtra("id", 0);
                            playMusic(nowPlay);
                        }
                }
                break;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void playMusic(int num) {
        if (num >= musicListGet.size()) {
            num = 0;
        } else if (num < 0) {
            num = musicListGet.size() - 1;
        }
        nowPlay = num;
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(musicListGet.get(num));
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
            if (num > musicListGet.size() || num < 0) {
                playMusic(0);
                nowPlay = 0;
                return;
            }
            playMusic(num + 1);
        }
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                switch (mod) {
                    case "单曲循环":
                        playMusic(nowPlay);
                        break;
                    case "随机播放":
                        mathmusic = (int) (Math.random() * musicListGet.size() - 1);
                        playMusic(mathmusic);
                        updateMainUI(mathmusic, "nextMusic");
                        break;
                    default:
                        if ((nowPlay + 1) >= musicListGet.size()) {
                            playMusic(0);
                        } else {
                            playMusic(nowPlay + 1);
                        }
                        updateMainUI(nowPlay, "nextMusic");
                }

            }
        });

        writeCof.writeCof(new SimpleBinder(), ClearContent.context);
    }

    public void updateMainUI(int nowmusic, String updateID) {
        Intent mainIntentBroadcastReceiver = new Intent();
        mainIntentBroadcastReceiver.putExtra("sendMain", updateID);
        mainIntentBroadcastReceiver.putExtra("position", nowmusic);
        mainIntentBroadcastReceiver.putExtra("isPlaying", mediaPlayer.isPlaying());
        mainIntentBroadcastReceiver.setAction(MainActivity.MAIN_SERVICE_NEED);
        sendBroadcast(mainIntentBroadcastReceiver);

        Intent mainIntentBroadcastReceiverpl = new Intent();
        mainIntentBroadcastReceiverpl.putExtra("sendMain", updateID);
        mainIntentBroadcastReceiverpl.putExtra("position", nowmusic);
        mainIntentBroadcastReceiverpl.putExtra("isPlaying", mediaPlayer.isPlaying());
        mainIntentBroadcastReceiverpl.putExtra("times", mediaPlayer.getDuration());
        mainIntentBroadcastReceiverpl.setAction(PlayActive.ACTION_SERVICE_NEED);
        sendBroadcast(mainIntentBroadcastReceiverpl);
    }

    public void goPlay(Intent intent) {
        if (intent != null) {
            playMusic(nowPlay);
            if (intent.getStringExtra("muchtime") == null) {
                mediaPlayer.seekTo(0);
            } else {
                mediaPlayer.seekTo(Integer.valueOf(intent.getStringExtra("muchtime")));
            }
        } else {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                Intent intentBroadcastReceiver = new Intent();
                intentBroadcastReceiver.putExtra("sendMain", "Playing");
                intentBroadcastReceiver.setAction(MainActivity.MAIN_SERVICE_NEED);
                sendBroadcast(intentBroadcastReceiver);

                Intent intentBroadcastReceiverPl = new Intent();
                intentBroadcastReceiverPl.putExtra("sendMain", "Playing");
                intentBroadcastReceiverPl.setAction(PlayActive.ACTION_SERVICE_NEED);
                sendBroadcast(intentBroadcastReceiverPl);
            } else {
                mediaPlayer.start();
                Intent intentBroadcastReceiver = new Intent();
                intentBroadcastReceiver.putExtra("sendMain", "noPlaying");
                intentBroadcastReceiver.setAction(MainActivity.MAIN_SERVICE_NEED);
                sendBroadcast(intentBroadcastReceiver);

                Intent intentBroadcastReceiverPl = new Intent();
                intentBroadcastReceiverPl.putExtra("sendMain", "noPlaying");
                intentBroadcastReceiverPl.setAction(PlayActive.ACTION_SERVICE_NEED);
                sendBroadcast(intentBroadcastReceiverPl);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (mBinder != null) {
            return mBinder;
        }
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    public class SimpleBinder extends Binder {
        public int getDuration() {//获取总进度条
            return mediaPlayer.getDuration();
        }

        public int getCurrentPosition() {//获取當前进度
            return mediaPlayer.getCurrentPosition();
        }

        public void setPlayMod(String models) {
            mod = models;
        }

        public String getPlayMod() {
            return mod;
        }

        public void setSeekTo(int progress) {
            mediaPlayer.seekTo(progress);
        }

        public String getName() {
            String subMusicName = musicListGet.get(nowPlay).substring(musicListGet.get(nowPlay).lastIndexOf("/"));
            String nowMusicName = subMusicName.substring(1, subMusicName.lastIndexOf("."));
            return nowMusicName;
        }

        public int getNow() {
            return nowPlay;
        }

        public ArrayList<String> getMusicList() {
            return musicListGet;
        }

        public ArrayList<String> getNameList() {
            return nameList;
        }

        public boolean getPlayingSta() {
            return mediaPlayer.isPlaying();
        }

        public void nextPlay(String nextOrPre,int playposition){
            switch (mod) {
                case "随机播放":
                    if (!nextOrPre.equals("onPlay")) {
                        nowPlay = (int) (Math.random() * musicListGet.size());
                        playMusic(nowPlay);
                        updateMainUI(nowPlay, "nextMusic");
                    } else {
                        goPlay(null);
                    }
                    break;
                default:
                    if (nextOrPre.equals("pre")) {
                        if ((nowPlay - 1) < 0) {
                            playMusic(musicListGet.size() - 1);
                        } else {
                            playMusic(nowPlay - 1);
                        }
                        updateMainUI(nowPlay, "nextMusic");
                    } else if (nextOrPre.equals("next")) {
                        if ((nowPlay + 1) > musicListGet.size()) {
                            playMusic(0);
                        } else {
                            playMusic(nowPlay + 1);
                        }
                        updateMainUI(nowPlay, "nextMusic");
                    } else if (nextOrPre.equals("onPlay")) {
                        goPlay(null);
                    } else if (nextOrPre.equals("maintoplay")) {
                        nowPlay = playposition;
                        playMusic(nowPlay);
                    }
            }
        }
    }
}
