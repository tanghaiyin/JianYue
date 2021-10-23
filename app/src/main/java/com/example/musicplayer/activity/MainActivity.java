package com.example.musicplayer.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayer.R;
import com.example.musicplayer.adapter.FileAdapter;
import com.example.musicplayer.adapter.MusicAdapter;
import com.example.musicplayer.application.ClearContent;
import com.example.musicplayer.dao.MusicData;
import com.example.musicplayer.receiver.BluetoothMonitorReceiver;
import com.example.musicplayer.receiver.HeadsetPlugReceiver;
import com.example.musicplayer.receiver.NotificationClickReceiver;
import com.example.musicplayer.service.MusicService;
import com.example.musicplayer.tool.CirImageView;
import com.example.musicplayer.tool.WriteCof;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private RecyclerView music_list;
    private CirImageView bottomCircle;
    private Button open_folder, image5, image3, image6, image7, btn_playing, btn_left, btn_right;
    private TextView tvbom, tvtop, exitpage;
    private MusicAdapter musicAdapter;
    private FileAdapter filegv_adapter;
    public static List<MusicData> musicListSet;
    public static List<String> musicPathList, musicNameList;
    private String filePath;
    private String nowMusic;
    private int position;
    private final int RECODEBACKFILL = 0x22f, RECODEGETMUSI = 0x33f, RECODEPRE = 0x55f;
    private Intent musicServiceIntent;
    private Dialog folderDialog = null;
    private GridView filegv;
    private StringBuilder filePathBuild;
    private WriteCof writeCof;
    public static MusicService.SimpleBinder mBinder = null;
    private MainActivity.mConnection2 musicCon = null;
    private List<File> loses;
    private List<Map<String, Object>> fileMap;
    private MusicReceiver musicReceiver;
    public static final String MAIN_SERVICE_NEED = "action.MainService";
    private MainActivity.ServiceNeedBroadcastReceiver2 broadcastReceiver2 = null;
    private NotificationManager notificationMgr;
    private Notification notify;
    private Notification.Builder builder;
    public static final String PLAY = "play";
    public static final String PREV = "prev";
    public static final String NEXT = "next";
    public static final String CLOSE = "close";
    private static RemoteViews remoteViews;
    public static Context context = null;
    private BluetoothMonitorReceiver bleListenerReceiver = null;
    private String bluetoothStatus;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ClearContent.getInstance().addActivity(this);
        context = this;
        ClearContent.setCurrentActivity(this);
        musicServiceIntent = new Intent(MainActivity.this, MusicService.class);
        writeCof = new WriteCof();
        initView();
        clickListener();
        applyPermission(RECODEPRE);
//        if (!isFirstLoad()) {
        if (applyPermission(RECODEBACKFILL)) {
            initDate();
        }
//        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(MAIN_SERVICE_NEED);
        broadcastReceiver2 = new ServiceNeedBroadcastReceiver2();
        registerReceiver(broadcastReceiver2, filter);
        initRemoteViews();
        initNotification();
        registerMusicReceiver();
        registerReceiver(mReceiver, makeFilter());
        registerHeadsetPlugReceiver();
        this.bleListenerReceiver = new BluetoothMonitorReceiver();
        IntentFilter intentFilter = new IntentFilter();
        // 监视蓝牙关闭和打开的状态
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        // 监视蓝牙设备与APP连接的状态
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        registerReceiver(this.bleListenerReceiver, intentFilter);
    }

    private IntentFilter makeFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        return filter;
    }

    private void checkNotifySetting() {
        final Intent localIntent = new Intent();
        AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this);
        ad.setMessage("是否前往开启通知栏权限，后台播放通知栏操作更方便")
                .setPositiveButton("前往开启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        localIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        localIntent.addCategory(Intent.CATEGORY_DEFAULT);
                        localIntent.setData(Uri.parse("package:" + MainActivity.this.getPackageName()));
                        MainActivity.this.startActivity(localIntent);
                    }
                })
                .setNegativeButton("不再提示", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();
    }

    private void unService(String nextOrPre) {
        if (!writeCof.isServiceWork(MainActivity.this, "com.example.musicplayer.service.MusicService")) {
            List<String> cofList = writeCof.readFileLine(MainActivity.this.getFilesDir() + "/config.txt");
            Log.e("随机", cofList + "");
            if (cofList != null && cofList.size() > 0) {
                String muchtime = writeCof.readFileLine(MainActivity.this.getFilesDir() + "/config.txt").get(0);//获取上次播放的音乐进度
                String playtime = writeCof.readFileLine(MainActivity.this.getFilesDir() + "/config.txt").get(2);//获取上次播放的音乐总时长
                String playMod = writeCof.readFileLine(MainActivity.this.getFilesDir() + "/config.txt").get(8);//播放模式
                String musicName = writeCof.readFileLine(MainActivity.this.getFilesDir() + "/config.txt").get(4);//音乐名称
                if (!nextOrPre.equals("maintoplay")) {
                    position = Integer.valueOf(writeCof.readFileLine(MainActivity.this.getFilesDir() + "/config.txt").get(6));
                }

                if (nextOrPre.equals("openPlayPage")) {
                    Intent playIntent = new Intent(ClearContent.context, PlayActive.class);
                    playIntent.putStringArrayListExtra("musicUrlList", (ArrayList<String>) musicPathList);
                    playIntent.putStringArrayListExtra("musicNameList", (ArrayList<String>) musicNameList);
                    playIntent.putExtra("playtime", playtime);
                    playIntent.putExtra("muchtime", muchtime);
                    playIntent.putExtra("musicName", musicName);
                    playIntent.putExtra("playMode", playMod);
                    playIntent.putExtra("id", position);
                    playIntent.putExtra("zhuanji", musicListSet.get(position).getMusicImg());
                    playIntent.putExtra("nextOrPre", nextOrPre);
                    playIntent.putExtra("musicState", "unStartService");
                    playIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ClearContent.context.startActivity(playIntent);
                } else {
                    musicServiceIntent.putStringArrayListExtra("musicUrlList", (ArrayList<String>) musicPathList);
                    musicServiceIntent.putStringArrayListExtra("musicNameList", (ArrayList<String>) musicNameList);
                    musicServiceIntent.putExtra("playtime", playtime);
                    musicServiceIntent.putExtra("muchtime", muchtime);
                    musicServiceIntent.putExtra("playMode", playMod);
                    musicServiceIntent.putExtra("id", position);
                    musicServiceIntent.putExtra("nextOrPre", nextOrPre);
                    musicServiceIntent.putExtra("musicState", "unStartService");
                    startService(musicServiceIntent);
                    musicCon = new MainActivity.mConnection2();//建立新连接对象
                    bindService(musicServiceIntent, musicCon, BIND_AUTO_CREATE);//建立和service连接
                }
                if (!nextOrPre.equals("openPlayPage")) {
                    btn_playing.setBackgroundResource(R.drawable.ic_play_stop);
                    remoteViews.setImageViewResource(R.id.nofbtnplay, R.drawable.ic_play_stop);
                    notificationMgr.notify(R.string.app_name, notify);
//                    updateNotificationShow(-1,null);
                }
            } else {
                if (nextOrPre.equals("openPlayPage")) {
                    Intent playIntent = new Intent(ClearContent.context, PlayActive.class);
                    playIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ClearContent.context.startActivity(playIntent);
                    return;
                } else {
                    switch (nextOrPre) {
                        case "maintoplay":
                            musicServiceIntent.putExtra("id", position);
                            break;
                        case "next":
                            musicServiceIntent.putExtra("id", -1);
                            break;
                        case "pre":
                            musicServiceIntent.putExtra("id", 1);
                            break;
                        case "onPlay":
                            musicServiceIntent.putExtra("id", 0);
                            break;
                    }
                    musicServiceIntent.putStringArrayListExtra("musicUrlList", (ArrayList<String>) musicPathList);
                    musicServiceIntent.putStringArrayListExtra("musicNameList", (ArrayList<String>) musicNameList);
                    musicServiceIntent.putExtra("playMode", "顺序播放");
                    musicServiceIntent.putExtra("nextOrPre", nextOrPre);
                    musicServiceIntent.putExtra("musicState", "unStartService");
                    startService(musicServiceIntent);
                    musicCon = new MainActivity.mConnection2();//建立新连接对象
                    bindService(musicServiceIntent, musicCon, BIND_AUTO_CREATE);//建立和service连接
                }


            }
        } else if (writeCof.isServiceWork(MainActivity.this, "com.example.musicplayer.service.MusicService")) {
            if (nextOrPre.equals("openPlayPage")) {
                Intent playIntent = new Intent(ClearContent.context, PlayActive.class);
                playIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ClearContent.context.startActivity(playIntent);
            } else {
//                musicServiceIntent.putExtra("nextOrPre", nextOrPre);
//                if (!nextOrPre.equals("maintoplay")) {
//                    musicServiceIntent.putExtra("id", -1);
//                } else {
//                    musicServiceIntent.putExtra("id", position);
//                }
//                musicServiceIntent.putExtra("musicState", "unStartService");
//                goba(musicServiceIntent);
                if (musicCon == null) {
                    musicCon = new MainActivity.mConnection2();
                    bindService(musicServiceIntent, musicCon, BIND_AUTO_CREATE);
                }
                if (mBinder == null) {
                    if (PlayActive.mBinder != null) {
                        if (nextOrPre.equals("maintoplay")) {
                            PlayActive.mBinder.nextPlay(nextOrPre, position);
                        } else {
                            PlayActive.mBinder.nextPlay(nextOrPre, -1);
                        }
                    }
                } else {
                    if (nextOrPre.equals("maintoplay")) {
                        mBinder.nextPlay(nextOrPre, position);
                    } else {
                        mBinder.nextPlay(nextOrPre, -1);
                    }
                }
            }
        }
    }

    private void clickListener() {
        btn_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unService("pre");
            }
        });
        btn_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unService("next");
            }
        });
        btn_playing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unService("onPlay");
            }
        });
        exitpage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeNotification();
            }
        });

        bottomCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unService("openPlayPage");
            }
        });

        open_folder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (applyPermission(RECODEGETMUSI)) {
                    String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                    selectFile(path);
                }
            }
        });


    }

    public boolean applyPermission(int requestCode) {
        boolean isPermission;
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        int code = checkCallingOrSelfPermission(permissions[0]);
        if (code == PackageManager.PERMISSION_GRANTED) {
            isPermission = true;
        } else {
            isPermission = false;
            ActivityCompat.requestPermissions(MainActivity.this, permissions, requestCode);
        }
        return isPermission;
    }

    private class ServiceNeedBroadcastReceiver2 extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent2) {
            switch (intent2.getStringExtra("sendMain")) {
                case "nextMusic":
                    boolean isPlaying = intent2.getBooleanExtra("isPlaying", false);
                    if (!isPlaying) {
                        btn_playing.setBackgroundResource(R.drawable.ic_play_start);
                        if (notificationMgr != null) {
                            remoteViews.setImageViewResource(R.id.nofbtnplay, R.drawable.ic_play_start);
                            notificationMgr.notify(R.string.app_name, notify);
                        }
                    } else {
                        btn_playing.setBackgroundResource(R.drawable.ic_play_stop);
                        if (notificationMgr != null) {
                            remoteViews.setImageViewResource(R.id.nofbtnplay, R.drawable.ic_play_stop);
                            notificationMgr.notify(R.string.app_name, notify);
                        }
                    }
                    musicAdapter.changeSelected(intent2.getIntExtra("position", 0));
                    position = intent2.getIntExtra("position", 0);
                    LinearLayoutManager manager = new LinearLayoutManager(MainActivity.this);
                    int firstItem = manager.findFirstVisibleItemPosition();
                    int lastItem = manager.findLastVisibleItemPosition();
                    if (position <= firstItem) {
                        music_list.scrollToPosition(position);
                    } else if (position <= lastItem) {
                        int top = music_list.getChildAt(position - firstItem).getTop();
                        music_list.scrollBy(0, top);
                    } else {
                        music_list.scrollToPosition(position);
                    }
                    String nowMusic = musicListSet.get(intent2.getIntExtra("position", 0)).getMusicName();
                    String oldMusicName, newMusicName, naet, defaultName = nowMusic.substring(0, nowMusic.indexOf("."));
                    try {
                        oldMusicName = nowMusic.substring(0, nowMusic.indexOf("-"));
                        naet = nowMusic.substring(nowMusic.indexOf("-"));
                        newMusicName = naet.substring(1, naet.indexOf("."));
                    } catch (StringIndexOutOfBoundsException s) {
                        oldMusicName = defaultName;
                        newMusicName = "unknow";
                        Log.e("MainException", s.toString());
                    }
                    tvbom.setText(oldMusicName);
                    tvtop.setText(newMusicName.trim());
                    if (notificationMgr != null) {
                        //歌曲名
                        remoteViews.setTextViewText(R.id.musicTitleTextView2, oldMusicName);
                        //歌手名
                        remoteViews.setTextViewText(R.id.musicTitleTextView, newMusicName.trim());
                        //发送通知
                        notificationMgr.notify(R.string.app_name, notify);
                    }
                    Bitmap bitmap = writeCof.getAlbumPicture(musicListSet.get(position).getPath());
                    if (bitmap != null) {
                        bottomCircle.setImageBitmap(bitmap);
                    } else {
                        bottomCircle.setImageResource(R.drawable.bg_gradientramp_default);
                    }
                    break;
                case "Playing":
                    btn_playing.setBackgroundResource(R.drawable.ic_play_start);
                    if (notificationMgr != null) {
                        if (notificationMgr != null) {
                            remoteViews.setImageViewResource(R.id.nofbtnplay, R.drawable.ic_play_start);
                            notificationMgr.notify(R.string.app_name, notify);
                        }
                    }
                    break;
                case "noPlaying":
                    btn_playing.setBackgroundResource(R.drawable.ic_play_stop);
                    if (notificationMgr != null) {
                        remoteViews.setImageViewResource(R.id.nofbtnplay, R.drawable.ic_play_stop);
                        notificationMgr.notify(R.string.app_name, notify);
                    }

                    break;
                case "conSuccess":
                    musicCon = new MainActivity.mConnection2();//建立新连接对象
                    bindService(musicServiceIntent, musicCon, BIND_AUTO_CREATE);//建立和service连接
                    break;
                case "noServiceToOpenMain":
                    unService("onPlay");
                    break;
                case "isServiceNext":
                    unService("next");
                    break;
                case "isServicePre":
                    unService("pre");
                    break;
            }
        }
    }

    private HeadsetPlugReceiver headsetPlugReceiver;

    private void registerHeadsetPlugReceiver() {
        headsetPlugReceiver = new HeadsetPlugReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.HEADSET_PLUG");
        registerReceiver(headsetPlugReceiver, intentFilter);

    }

    public void initDate() {
        File file = new File(this.getFilesDir() + "/歌曲清单.txt");
        FileReader reader;
        if (file.exists()) {
            try {
                reader = new FileReader(file.getAbsoluteFile());
                BufferedReader buf = new BufferedReader(reader);
                String s;
                filePathBuild = new StringBuilder();
                while ((s = buf.readLine()) != null) {
                    filePathBuild.append(s);
                }
                iniData(filePathBuild.toString(), false);
                reader.close();
                buf.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        List<String> cofList = writeCof.readFileLine(MainActivity.this.getFilesDir() + "/config.txt");
        if (cofList != null && cofList.size() > 0) {
            if (!writeCof.isServiceWork(MainActivity.this, "com.example.musicplayer.service.MusicService")) {
                position = Integer.valueOf(cofList.get(6));
                nowMusic = writeCof.readFileLine(MainActivity.this.getFilesDir() + "/config.txt").get(4);
                noPage(position, nowMusic, false);
            } else {
                musicCon = new MainActivity.mConnection2();//建立新连接对象
                bindService(musicServiceIntent, musicCon, BIND_AUTO_CREATE);
            }

        }
    }

    public void noPage(int position, String nowMusic, boolean isSer) {
        LinearLayoutManager manager = new LinearLayoutManager(MainActivity.this);
        int firstItem = manager.findFirstVisibleItemPosition();
        int lastItem = manager.findLastVisibleItemPosition();
        if (position <= firstItem) {
            music_list.scrollToPosition(position);
        } else if (position <= lastItem) {
            int top = music_list.getChildAt(position - firstItem).getTop();
            music_list.scrollBy(0, top);
        } else {
            music_list.scrollToPosition(position);
        }
        String musicName, sigName;
        try {
            musicName = nowMusic.substring(0, nowMusic.indexOf("-"));
            String sigNamepo = nowMusic.substring(nowMusic.indexOf("-")).substring(1);
            sigName = sigNamepo;
        } catch (StringIndexOutOfBoundsException s) {
            musicName = "unknow";
            sigName = "unknow";
        }
        tvbom.setText(musicName);
        tvtop.setText(sigName.trim());
        if (notificationMgr != null) {
            //歌曲名
            remoteViews.setTextViewText(R.id.musicTitleTextView2, musicName);
            //歌手名
            remoteViews.setTextViewText(R.id.musicTitleTextView, sigName.trim());
            //发送通知
            notificationMgr.notify(R.string.app_name, notify);
        }
        Bitmap bitmap = writeCof.getAlbumPicture(musicListSet.get(position).getPath());
        if (bitmap != null) {
            bottomCircle.setImageBitmap(bitmap);
        } else {
            bottomCircle.setImageResource(R.drawable.bg_gradientramp_default);
        }
        musicAdapter.changeSelected(position);
        if (isSer) {
            btn_playing.setBackgroundResource(R.drawable.ic_play_stop);
            if (notificationMgr != null) {
                remoteViews.setImageViewResource(R.id.nofbtnplay, R.drawable.ic_play_stop);
                notificationMgr.notify(R.string.app_name, notify);
            }

        }
    }

    public void initView() {
        music_list = findViewById(R.id.music_list);
        open_folder = findViewById(R.id.open_folder);
        bottomCircle = findViewById(R.id.ci);
        tvbom = findViewById(R.id.tvbom);
        tvtop = findViewById(R.id.tvtop);
        exitpage = findViewById(R.id.exitpage);
        btn_playing = (Button) findViewById(R.id.btn_playing);
        btn_left = (Button) findViewById(R.id.btn_left);
        btn_right = (Button) findViewById(R.id.btn_right);
    }

    public void iniData(String filePathtwo, boolean ft) {
        if (ft == true) {
            if (filePathBuild != null) {
                if (filePathtwo.equals(filePathBuild.toString())) {
                    Toast.makeText(MainActivity.this, "该目录已被选择", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            mustList(filePathtwo);
            if (musicListSet.size() == 0) {
                musicAdapter.notifyDataSetChanged();
            }
            File file = new File(this.getFilesDir() + "/歌曲清单.txt");
            try {
                file.createNewFile();
                FileOutputStream fos = new FileOutputStream(file, false);
                fos.write(filePath.getBytes("UTF-8"));
                fos.close();
                filePathBuild = new StringBuilder();
                filePathBuild.append(filePathtwo);
                selectFile(filePathtwo);
                Toast.makeText(MainActivity.this, "成功选择", Toast.LENGTH_SHORT).show();
                folderDialog.dismiss();
                folderDialog = null;
            } catch (Exception e) {
                Log.e("Log", e + "");
            }
        } else {
            mustList(filePathtwo);
        }
    }

    public void mustList(String paths) {
        musicListSet = new ArrayList<>();
        musicPathList = new ArrayList<>();
        musicNameList = new ArrayList<>();
        File[] getData = new File(paths).listFiles();
        for (int i = 0; i < getData.length; i++) {
            if (getData[i].getName().toUpperCase().endsWith(".MP3") || getData[i].getName().toUpperCase().endsWith(".FLAC")) {
                musicListSet.add(new MusicData(getData[i].getName(), paths + "/" + getData[i].getName()));
                musicPathList.add(paths + "/" + getData[i].getName());
                musicNameList.add(getData[i].getName());
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
                music_list.setLayoutManager(linearLayoutManager);
                musicAdapter = new MusicAdapter(musicListSet, this);
                music_list.setAdapter(musicAdapter);
            }
        }
        if (musicListSet.size() == 0) {
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            music_list.setLayoutManager(linearLayoutManager);
            musicAdapter = new MusicAdapter(musicListSet, this);
            music_list.setAdapter(musicAdapter);
        }
        musicAdapter.setOnItemClickListener(new MusicAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position2) {
                if (position2 != position) {
                    musicAdapter.changeSelected(position2);
                    String oldMusicName;
                    String newMusicName;
                    try {
                        oldMusicName = musicListSet.get(position2).getMusicName().substring(0, musicListSet.get(position2).getMusicName().indexOf("-"));
                        String naet = musicListSet.get(position2).getMusicName().substring(musicListSet.get(position2).getMusicName().indexOf("-"));
                        newMusicName = naet.substring(1, naet.indexOf("."));
                    } catch (StringIndexOutOfBoundsException s) {
                        oldMusicName = "unknow";
                        newMusicName = "unknow";
                    }
                    tvbom.setText(oldMusicName);
                    tvtop.setText(newMusicName.trim());
                    if (notificationMgr != null) {
                        //歌曲名
                        remoteViews.setTextViewText(R.id.musicTitleTextView2, oldMusicName);
                        //歌手名
                        remoteViews.setTextViewText(R.id.musicTitleTextView, newMusicName.trim());
                        //发送通知
                        notificationMgr.notify(R.string.app_name, notify);
                    }
                    position = position2;
                    Bitmap bitmap = writeCof.getAlbumPicture(musicListSet.get(position).getPath());
                    if (bitmap != null) {
                        bottomCircle.setImageBitmap(bitmap);
                    } else {
                        bottomCircle.setImageResource(R.drawable.bg_gradientramp_default);
                    }
                    unService("maintoplay");
                }
            }
        });
        musicAdapter.setOntvClickListener(new MusicAdapter.OnTvClickListener() {
            @Override
            public void onClick(final int position) {
                AlertDialog.Builder abs = new AlertDialog.Builder(MainActivity.this);
                abs.setMessage(musicListSet.get(position).getPath())
                        .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
            }
        });
    }

    public void selectFile(String path) {
        filePath = path;
        File directory = new File(path);
        File[] files = directory.listFiles();
        readFile(files);
    }

    private boolean isFirstLoad() {
        SharedPreferences shared = getSharedPreferences("mainActivity", MODE_PRIVATE);
        boolean isFirstLoad = shared.getBoolean("isFirstLoad", true);
        SharedPreferences.Editor editor = shared.edit();
        if (isFirstLoad) {
            editor.putBoolean("isFirstLoad", false);
            editor.commit();
        }
        return isFirstLoad;
    }

    public boolean isChinese(char c) {
        return c >= 0x4E00 && c <= 0x9FA5;// 根据字节码判断
    }

    public boolean isChinese2(String str) {
        if (str == null) {
            return false;
        }
        for (char c : str.toCharArray()) {
            if (isChinese(c)) {
                return true;// 有一个中文字符就返回
            }
        }
        return false;
    }

    public void px(File[] files2) {
        Collections.sort(Arrays.asList(files2), new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                //文件夹排前面
                if (o1.isDirectory() && o2.isFile()) {
                    return -1;
                }
                if (o1.isFile() && o2.isDirectory()) {
                    return 1;
                }
                //其次文本
                if (o1.getName().endsWith(".txt") && !o2.getName().endsWith(".txt")) {
                    return -1;
                }
                if (!o1.getName().endsWith(".txt") && o2.getName().endsWith(".txt")) {
                    return 1;
                }
                //其次图片
                if (o1.getName().endsWith(".png") && !o2.getName().endsWith(".png")) {
                    return -1;
                }
                if (!o1.getName().endsWith(".png") && o2.getName().endsWith(".png")) {
                    return 1;
                }
                if (o1.getName().endsWith(".jpg") && !o2.getName().endsWith(".jpg")) {
                    return -1;
                }
                if (!o1.getName().endsWith(".jpg") && o2.getName().endsWith(".jpg")) {
                    return 1;
                }
                //其次音乐
                if (o1.getName().endsWith(".mp3") && !o2.getName().endsWith(".mp3")) {
                    return -1;
                }
                if (!o1.getName().endsWith(".mp3") && o2.getName().endsWith(".mp3")) {
                    return 1;
                }
                String[] zm = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};
                for (int q = 0; q < zm.length; q++) {
                    if (o1.getName().substring(0, 1).equalsIgnoreCase(zm[q]) && !o2.getName().substring(0, 1).equalsIgnoreCase(zm[q])) {
                        return -1;
                    }
                    if (!o1.getName().substring(0, 1).equalsIgnoreCase(zm[q]) && o2.getName().substring(0, 1).equalsIgnoreCase(zm[q])) {
                        return 1;
                    }
                }
                String[] sz = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
                for (int q = 0; q < sz.length; q++) {
                    if (o1.getName().substring(0, 1).equalsIgnoreCase(sz[q]) && !o2.getName().substring(0, 1).equalsIgnoreCase(sz[q])) {
                        return -1;
                    }
                    if (!o1.getName().substring(0, 1).equalsIgnoreCase(sz[q]) && o2.getName().substring(0, 1).equalsIgnoreCase(sz[q])) {
                        return 1;
                    }
                }
                //其次中文
                if (isChinese2(o1.getName().substring(0, 1)) && !isChinese2(o2.getName().substring(0, 1))) {
                    return -1;
                }
                if (!isChinese2(o1.getName().substring(0, 1)) && isChinese2(o2.getName().substring(0, 1))) {
                    return 1;
                }
                //其次符号
                String[] fh = {"!", "@", "#", "$", "%", "^", "&", "(", ")", "_", "-", "=", "+", "[", "]", "{", "}", ",", "，", ".", "。", "-", "、", ";", "；", "“", "”", "'", "‘", "’"};
                for (int u = 0; u < fh.length; u++) {
                    if (o1.getName().substring(0, 1).equalsIgnoreCase(fh[u]) && !o2.getName().substring(0, 1).equalsIgnoreCase(fh[u])) {
                        return -1;
                    }
                    if (!o1.getName().substring(0, 1).equalsIgnoreCase(fh[u]) && o2.getName().substring(0, 1).equalsIgnoreCase(fh[u])) {
                        return 1;
                    }
                }
                //其次名称长度
                if (o1.getName().length() < o2.getName().length()) {
                    return -1;
                }
                if (o1.getName().length() > o2.getName().length()) {
                    return 1;
                }
                return o1.getName().compareTo(o2.getName());
            }
        });
    }

    private void readFile(final File[] fi) {
        px(fi);
        loses = new ArrayList<>();
        fileMap = new ArrayList<>();
        for (int i = 0; i < fi.length; i++) {
            Map<String, Object> map = new HashMap<>();
            final File f = fi[i];
            if (f.isDirectory()) {
                loses.add(f);
                map.put("image", R.drawable.ic_folder);
            }
            map.put("text", fi[i].getName().trim());
            fileMap.add(map);
        }

        String[] from = {"image", "text"};
        int[] to = {R.id.image2, R.id.text2};
        filegv_adapter = new FileAdapter(MainActivity.this, fileMap, R.layout.dialog_file_item, from, to, filePath, null);
        //配置适配器parent.getItemAtPosition(position)
        View contentView = View.inflate(MainActivity.this, R.layout.dialog_file, null);
        image5 = (Button) contentView.findViewById(R.id.image5);
        image6 = (Button) contentView.findViewById(R.id.image6);
        image3 = (Button) contentView.findViewById(R.id.image3);
        image7 = (Button) contentView.findViewById(R.id.image7);
        filegv = (GridView) contentView.findViewById(R.id.filexsgv);
        image5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String filePath2 = new File(filePath).getParentFile().toString();
                if (!filePath2.equals(Environment.getExternalStorageDirectory().getParentFile().getAbsolutePath())) {
                    //获取文件夹下所有文件
                    selectFile(filePath2);
                }
            }
        });
        image3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                selectFile(path);
            }
        });
        image6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (folderDialog != null) {
                    folderDialog.dismiss();
                    folderDialog = null;
                }
            }
        });
        image7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iniData(filePath, true);
            }
        });

        filegv.setAdapter(filegv_adapter);
        filegv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String rt = filePath + "/" + fileMap.get(position).get("text").toString();
                File fe = new File(rt);
                if (fe.isDirectory()) {
                    //获取文件夹下所有文件
                    selectFile(rt);
                }
            }
        });

        if (folderDialog == null) {
            folderDialog = new Dialog(MainActivity.this);
            folderDialog.setCancelable(true);
            folderDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    folderDialog = null;
                }
            });
            folderDialog.show();
        }
        folderDialog.setContentView(contentView);
    }

    private void initRemoteViews() {
        //注册点击事件
        remoteViews = new RemoteViews(this.getPackageName(), R.layout.view_notify);
        Intent intentPrev = new Intent(PREV);
        PendingIntent prevPendingIntent = PendingIntent.getBroadcast(this, 0, intentPrev, 0);
        remoteViews.setOnClickPendingIntent(R.id.ione, prevPendingIntent);
        Intent intentPlay = new Intent(PLAY);
        PendingIntent playPendingIntent = PendingIntent.getBroadcast(this, 0, intentPlay, 0);
        remoteViews.setOnClickPendingIntent(R.id.nofbtnplay, playPendingIntent);
        Intent intentNext = new Intent(NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(this, 0, intentNext, 0);
        remoteViews.setOnClickPendingIntent(R.id.nofnext, nextPendingIntent);
        Intent intentClose = new Intent(CLOSE);
        PendingIntent closePendingIntent = PendingIntent.getBroadcast(this, 0, intentClose, 0);
        remoteViews.setOnClickPendingIntent(R.id.audio_close_btn, closePendingIntent);
        String musicName, sigName;
        List<String> cofList = writeCof.readFileLine(MainActivity.this.getFilesDir() + "/config.txt");
        if (cofList != null && cofList.size() > 0) {
            nowMusic = writeCof.readFileLine(MainActivity.this.getFilesDir() + "/config.txt").get(4);
            try {
                musicName = nowMusic.substring(0, nowMusic.indexOf("-"));
                String sigNamepo = nowMusic.substring(nowMusic.indexOf("-")).substring(1);
                sigName = sigNamepo;
            } catch (StringIndexOutOfBoundsException s) {
                musicName = "unknow";
                sigName = "unknow";
            }
            remoteViews.setTextViewText(R.id.musicTitleTextView, sigName);
            remoteViews.setTextViewText(R.id.musicTitleTextView2, musicName);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("NotificationTrampoline")
    private void initNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createCountNotifyChannel(MainActivity.this, "State", "状态", NotificationManager.IMPORTANCE_HIGH);
        }
        Intent clickIntent = new Intent(getApplicationContext(), NotificationClickReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0,
                clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder = new Notification.Builder(this, "play_control");
        builder.setContentIntent(pendingIntent)
                .setAutoCancel(false)//是否允许自动清除
                .setSmallIcon(R.drawable.ic_action_name)//最顶部的小图标
                .setWhen(System.currentTimeMillis())//推送时间
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_name))//通知栏中的大图标
                .setCustomContentView(remoteViews)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setChannelId("State");//渠道Id
        notify = builder.build();
        notificationMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //推送
        notificationMgr.notify(R.string.app_name, notify);
    }

    private void UIControl(String state, String tag) {
        Log.e("特点2", state);
        switch (state) {
            case PLAY:
                unService("onPlay");
                playPageSer();
                break;
            case PREV:
                unService("pre");
                playPageSer();
                break;
            case NEXT:
                unService("next");
                playPageSer();
                break;
            case CLOSE:
                closeNotification();
                break;
            default:
                break;
        }

    }

    public void playPageSer() {
        Intent mainIntentBroadcastReceiverpl = new Intent();
        mainIntentBroadcastReceiverpl.putExtra("sendMain", "sOkay");
        mainIntentBroadcastReceiverpl.setAction(PlayActive.ACTION_SERVICE_NEED);
        sendBroadcast(mainIntentBroadcastReceiverpl);
    }

    public void closeNotification() {
        writeCof.writeCof(mBinder, MainActivity.this);
        ClearContent.getInstance().exit();
        if (notificationMgr != null) {
            notificationMgr.cancelAll();
        }
        if (musicCon != null) {
            unbindService(musicCon);
        }
        if (musicReceiver != null) {
            unregisterReceiver(musicReceiver);
        }
        stopService(musicServiceIntent);
        System.exit(0);
    }

    private void registerMusicReceiver() {
        musicReceiver = new MusicReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PLAY);
        intentFilter.addAction(PREV);
        intentFilter.addAction(NEXT);
        intentFilter.addAction(CLOSE);
        registerReceiver(musicReceiver, intentFilter);
    }

    public class MusicReceiver extends BroadcastReceiver {

        public static final String TAG = "MusicReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            //UI控制
            UIControl(intent.getAction(), TAG);
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    public static void createCountNotifyChannel(Context context, String channelId, String channelName, int importance) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        channel.setSound(null, null);
        channel.enableLights(true);//设置桌面图标右上角红点
        channel.setLightColor(Color.RED);//红点颜色
        channel.setShowBadge(true);
        NotificationManager notificationMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationMgr.createNotificationChannel(channel);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.e("得我", requestCode + "");
        switch (requestCode) {
            case RECODEGETMUSI:
                Log.e("得我5", requestCode + "");
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                    selectFile(path);
                }
                break;
            case RECODEBACKFILL:
                Log.e("得我4", requestCode + "");
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initDate();
                }
                break;
            case RECODEPRE:
                Log.e("得我3", requestCode + "");
                if (isFirstLoad()) {
                    Log.e("得我2", requestCode + "");
                    checkNotifySetting();
                }
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Intent home = new Intent(Intent.ACTION_MAIN);
            home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            home.addCategory(Intent.CATEGORY_HOME);
            startActivity(home);
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ClearContent.setCurrentActivity(MainActivity.this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        ClearContent.setCurrentActivity(this);
    }

    class mConnection2 implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = (MusicService.SimpleBinder) service;
            position = mBinder.getNow();
            noPage(mBinder.getNow(), mBinder.getName(), true);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_ON:
                            Log.e("TAG", "TURNING_ON");
                            break;
                        case BluetoothAdapter.STATE_ON:
                            bluetoothStatus = "on";
                            Log.e("TAG", "STATE_ON");
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            Log.e("TAG", "STATE_TURNING_OFF");
                            break;
                        case BluetoothAdapter.STATE_OFF:
                            bluetoothStatus = "off";
                            Log.e("TAG", "STATE_OFF");
                            break;
                    }
                    break;
            }
        }
    };
}