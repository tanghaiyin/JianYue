package com.example.musicplayer.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.musicplayer.R;
import com.example.musicplayer.application.ClearContent;
import com.example.musicplayer.service.MusicService;
import com.example.musicplayer.tool.WriteCof;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class PlayActive extends AppCompatActivity {
    public static Context context = null;
    private SeekBar mmusicSeekBar;
    private TextView nowTime, tv_total_time, timetv2;
    private Button btn_pre, btn_play, btn_next;
    private Spinner modelselect;
    private ArrayList<String> modelselectli, musicListGet=null;
    private ImageView xzq;
    public static MusicService.SimpleBinder mBinder = null;
    private mConnection musicCon = null;
    private Intent intent;
    private int position;
    private String playtime, muchtime, musicMode, musicName, selectModel, nowTimeStr;
    private SpinnerAdapter apsAdapter;
    private boolean isSer = false;
    private WriteCof writeCof;
    private Handler handler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message message) {
            super.handleMessage(message);
            Update();
        }
    };
    public static final String ACTION_SERVICE_NEED = "action.PlayActiveService";
    public ServiceNeedBroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        ClearContent.getInstance().addActivity(this);
        ClearContent.setCurrentActivity(this);
        setContentView(R.layout.activity_playpage);
        writeCof = new WriteCof();
        initView();
        getData();
        clickListener();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_SERVICE_NEED);
        broadcastReceiver = new ServiceNeedBroadcastReceiver();
        registerReceiver(broadcastReceiver, filter);
    }

    public void goba(Intent neprIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(neprIntent);
        } else {
            startService(neprIntent);
        }

    }

    public void getData() {
        context = this;
        intent = new Intent(PlayActive.this, MusicService.class);
        if (writeCof.isServiceWork(PlayActive.this, "com.example.musicplayer.service.MusicService")) {
            if (musicCon==null){
                musicCon = new PlayActive.mConnection();//建立新连接对象
                bindService(intent, musicCon, BIND_AUTO_CREATE);//建立和service连接
            }
        } else {
            try {
                playtime = getIntent().getStringExtra("playtime");
                muchtime = getIntent().getStringExtra("muchtime");
                musicMode = getIntent().getStringExtra("playMode");
                musicName = getIntent().getStringExtra("musicName");
                position = getIntent().getIntExtra("id", 0);
                musicListGet = getIntent().getStringArrayListExtra("musicUrlList");
                modelselectli = getIntent().getStringArrayListExtra("musicNameList");
                mmusicSeekBar.setMax(Integer.valueOf(playtime));
                Date dates = new Date(Integer.valueOf(muchtime));
                SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
                nowTimeStr = sdf.format(dates);
                nowTime.setText(nowTimeStr);
                timetv2.setText(musicName);
                Bitmap bitmap = writeCof.getAlbumPicture(musicListGet.get(position));
                if (bitmap != null) {
                    xzq.setImageBitmap(bitmap);
                }

                mmusicSeekBar.setProgress(Integer.valueOf(muchtime));
                Date pldates = new Date(Integer.valueOf(playtime));
                SimpleDateFormat plsdf = new SimpleDateFormat("mm:ss");
                String muchTimeStr = plsdf.format(pldates);
                tv_total_time.setText(muchTimeStr);
                apsAdapter = modelselect.getAdapter();
                int modNum = apsAdapter.getCount();
                for (int i = 0; i < modNum; i++) {
                    if (musicMode.equals(apsAdapter.getItem(i).toString())) {
                        modelselect.setSelection(i, true);
                        break;
                    }
                }
            }catch (Exception n){
                Log.e("Exception","======>"+n.toString());
            }
        }
    }

    public void initView() {
        mmusicSeekBar = findViewById(R.id.mmusicSeekBar);
        nowTime = findViewById(R.id.timetv);
        timetv2 = findViewById(R.id.timetv2);
        xzq = findViewById(R.id.xzq);
        tv_total_time = findViewById(R.id.tv_total_time);
        btn_pre = findViewById(R.id.btn_pre);
        btn_play = findViewById(R.id.btn_play);
        btn_next = findViewById(R.id.btn_next);
        modelselect = findViewById(R.id.modelselect);
    }

    public void clickListener() {
        btn_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unService("onPlay");
            }
        });

        btn_pre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unService("pre");
            }

        });

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unService("next");
            }
        });

        modelselect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] languages = getResources().getStringArray(R.array.play_mode);
                String models = languages[position];
                if (models.equals("顺序播放")) {
                    selectModel = "顺序播放";
                } else if (models.equals("随机播放")) {
                    selectModel = "随机播放";
                } else if (models.equals("单曲循环")) {
                    selectModel = "单曲循环";
                }
                if (mBinder != null) {
                    mBinder.setPlayMod(selectModel);
                } else {
                    isSer = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mmusicSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mmusicSeekBar.setProgress(progress);
                    muchtime = String.valueOf(progress);
                    Date dates = new Date(Integer.valueOf(muchtime));
                    SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
                    nowTimeStr = sdf.format(dates);
                    nowTime.setText(nowTimeStr);
                    if (mBinder != null) {
                        mBinder.setSeekTo(progress);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void unService(String nextOrPre) {
        if (!writeCof.isServiceWork(PlayActive.this, "com.example.musicplayer.service.MusicService")) {
            if (musicListGet!=null){
                intent.putStringArrayListExtra("musicUrlList", (ArrayList<String>) musicListGet);
                intent.putStringArrayListExtra("musicNameList", (ArrayList<String>) modelselectli);
                intent.putExtra("playtime", playtime);
                intent.putExtra("muchtime", muchtime);
                intent.putExtra("playMode", musicMode);
                intent.putExtra("id", position);
                intent.putExtra("nextOrPre", nextOrPre);
                intent.putExtra("musicState", "unStartService");
                startService(intent);
                if (musicCon==null) {
                    musicCon = new PlayActive.mConnection();
                    bindService(intent, musicCon, BIND_AUTO_CREATE);
                }
            }else {
                return;
            }

        } else if (writeCof.isServiceWork(PlayActive.this, "com.example.musicplayer.service.MusicService")) {
            if (musicCon==null){
                musicCon = new mConnection();
                bindService(intent, musicCon, BIND_AUTO_CREATE);
            }
            if (mBinder==null){
                if (MainActivity.mBinder!=null){
                    MainActivity.mBinder.nextPlay(nextOrPre,-1);
                }
            }else {
                mBinder.nextPlay(nextOrPre,-1);
            }
        }

        if (isSer == true) {
            try {
                mBinder.setPlayMod(selectModel);
                isSer = false;
            } catch (Exception e) {
                isSer = true;
            }
        }
        if (nextOrPre.equals("next")||nextOrPre.equals("pre")){
            btn_play.setBackgroundResource(R.drawable.ic_play_stop);
        }
    }

    private Handler handlermu = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x2233:
                    Update();

                    musicListGet = mBinder.getMusicList();
                    modelselectli = mBinder.getNameList();
                    playtime = String.valueOf(mBinder.getDuration());
                    muchtime = String.valueOf(mBinder.getCurrentPosition());
                    musicMode = mBinder.getPlayMod();
                    musicName = mBinder.getName();
                    position = mBinder.getNow();
                    mmusicSeekBar.setMax(Integer.valueOf(playtime));
                    Date dates = new Date(Integer.valueOf(muchtime));
                    SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
                    Bitmap bitmap = writeCof.getAlbumPicture(musicListGet.get(position));
                    if (bitmap != null) {
                        xzq.setImageBitmap(bitmap);
                    }
                    Log.e("定位",bitmap+"234");
                    nowTimeStr = sdf.format(dates);
                    nowTime.setText(nowTimeStr);
                    timetv2.setText(musicName);
                    mmusicSeekBar.setProgress(Integer.valueOf(muchtime));
                    if (mBinder.getPlayingSta()) {
                        btn_play.setBackgroundResource(R.drawable.ic_play_stop);
                    } else {
                        btn_play.setBackgroundResource(R.drawable.ic_play_start);
                    }
                    Date pldates = new Date(Integer.valueOf(playtime));
                    SimpleDateFormat plsdf = new SimpleDateFormat("mm:ss");
                    String muchTimeStr = plsdf.format(pldates);
                    tv_total_time.setText(muchTimeStr);
                    apsAdapter = modelselect.getAdapter();
                    int modNum = apsAdapter.getCount();
                    for (int i = 0; i < modNum; i++) {
                        if (musicMode.equals(apsAdapter.getItem(i).toString())) {
                            modelselect.setSelection(i, true);
                            break;
                        }
                    }
                    break;
            }
        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ClearContent.setCurrentActivity(this);
    }
    @Override
    protected void onStart() {
        super.onStart();
        ClearContent.setCurrentActivity(this);
    }
    public void Update() {
        int currentTime = mBinder.getCurrentPosition();
        mmusicSeekBar.setProgress(currentTime);
        nowTime.setText(new SimpleDateFormat("mm:ss", Locale.getDefault()).format(new Date(currentTime)));
//        if (musicCon==null){
            handler.sendEmptyMessageDelayed(0, 1000);
//        }
    }

    class mConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = (MusicService.SimpleBinder) service;
            Message msg = new Message();
            msg.what = 0x2233;
            handlermu.sendMessage(msg);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    private class ServiceNeedBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent2) {
            String ispl = intent2.getStringExtra("sendMain");
            switch (ispl) {
                case "nextMusic":
                    if (musicListGet!=null){
                        boolean isPlaying = intent2.getBooleanExtra("isPlaying", false);
                        if (!isPlaying) {
                            btn_play.setBackgroundResource(R.drawable.ic_play_start);
                        } else {
                            btn_play.setBackgroundResource(R.drawable.ic_play_stop);
                        }
                        position = intent2.getIntExtra("position", 0);
                        int muplaytime = intent2.getIntExtra("times", 0);
                        muchtime = String.valueOf(muplaytime);
                        timetv2.setText(modelselectli.get(position));
                        Date dates = new Date(muplaytime);
                        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
                        String durTimeStr = sdf.format(dates);
                        tv_total_time.setText(durTimeStr);
                        mmusicSeekBar.setMax(muplaytime);
                        Bitmap bitmap = writeCof.getAlbumPicture(musicListGet.get(position));
                        if (bitmap != null) {
                            xzq.setImageBitmap(bitmap);
                        }else {
                            xzq.setImageResource(R.drawable.ic_action_name);
                        }
                    }
                    break;
                case "Playing":
                    btn_play.setBackgroundResource(R.drawable.ic_play_start);
                    break;
                case "noPlaying":
                    btn_play.setBackgroundResource(R.drawable.ic_play_stop);
                    break;
                    case "sOkay":
                    if (musicCon==null){
                        musicCon = new mConnection();
                        bindService(intent,musicCon,BIND_AUTO_CREATE);
                    }
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

}
