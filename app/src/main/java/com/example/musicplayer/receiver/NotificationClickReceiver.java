package com.example.musicplayer.receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.musicplayer.activity.MainActivity;
import com.example.musicplayer.activity.PlayActive;
import com.example.musicplayer.application.ClearContent;
import com.example.musicplayer.tool.WriteCof;

import java.util.ArrayList;
import java.util.List;

/**
 * 通知点击广播接收器  跳转到栈顶的Activity ,而不是new 一个新的Activity
 *
 * @author llw
 */
public class NotificationClickReceiver extends BroadcastReceiver {

    public static final String TAG = "NotificationClickReceiver";
    WriteCof writeCof;
    @Override
    public void onReceive(Context context, Intent intent) {
        writeCof = new WriteCof();
        Activity currentActivity = ClearContent.getCurrentActivity();
        List<String> cofList = writeCof.readFileLine(ClearContent.context.getFilesDir() + "/config.txt");
        if (!writeCof.isServiceWork(ClearContent.context, "com.example.musicplayer.service.MusicService")){
            if (cofList != null && cofList.size() > 0){
                if (currentActivity.getClass().getName().equals("com.example.musicplayer.activity.PlayActive")){
                    String muchtime = writeCof.readFileLine(ClearContent.context.getFilesDir() + "/config.txt").get(0);//获取上次播放的音乐进度
                    String playtime = writeCof.readFileLine(ClearContent.context.getFilesDir() + "/config.txt").get(2);//获取上次播放的音乐总时长
                    String playMod = writeCof.readFileLine(ClearContent.context.getFilesDir() + "/config.txt").get(8);//播放模式
                    String musicName = writeCof.readFileLine(ClearContent.context.getFilesDir() + "/config.txt").get(4);//音乐名称
                    int position = Integer.valueOf(writeCof.readFileLine(ClearContent.context.getFilesDir() + "/config.txt").get(6));
                    Intent playIntent = new Intent(ClearContent.context, PlayActive.class);
                    playIntent.putStringArrayListExtra("musicUrlList", (ArrayList<String>) MainActivity.musicPathList);
                    playIntent.putStringArrayListExtra("musicNameList", (ArrayList<String>) MainActivity.musicNameList);
                    playIntent.putExtra("playtime", playtime);
                    playIntent.putExtra("muchtime", muchtime);
                    playIntent.putExtra("musicName", musicName);
                    playIntent.putExtra("playMode", playMod);
                    playIntent.putExtra("id", position);
                    playIntent.putExtra("zhuanji", MainActivity.musicListSet.get(position).getMusicImg());
                    playIntent.putExtra("musicState", "unStartService");
                    playIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ClearContent.context.startActivity(playIntent);
                    return;
                }
            }
        }
        intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setClass(context, currentActivity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        context.startActivity(intent);
    }
}
