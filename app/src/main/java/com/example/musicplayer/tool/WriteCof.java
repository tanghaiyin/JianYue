package com.example.musicplayer.tool;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import com.example.musicplayer.service.MusicService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class WriteCof {

    public void writeCof(MusicService.SimpleBinder mBinder, Context mcontext) {
        if (isServiceWork(mcontext, "com.example.musicplayer.service.MusicService")) {
            if (mBinder != null) {
                StringBuilder configsb = new StringBuilder();
                String nowPlayTime = "";
                String muchPlayTime = "";
                nowPlayTime = mBinder.getCurrentPosition() + "\r\n";//音乐当前进度
                muchPlayTime = mBinder.getDuration() + "\r\n";//音乐总时长
                String muchName = mBinder.getName() + "\r\n";
                String npl = mBinder.getNow() + "\r\n";
                String mod = mBinder.getPlayMod() + "\r\n";
                configsb.append(nowPlayTime + "\r\n");
                configsb.append(muchPlayTime + "\r\n");
                configsb.append(muchName + "\r\n");
                configsb.append(npl + "\r\n");
                configsb.append(mod + "\r\n");
                File file = new File(mcontext.getFilesDir() + "/config.txt");//没有就创建
                try {
                    file.createNewFile();
                    FileOutputStream fos = new FileOutputStream(file, false);
                    fos.write(configsb.toString().getBytes("UTF-8"));
                    fos.close();
                } catch (Exception e) {
                    Log.e("Log", e + "");
                }
            }
        }
    }

    public boolean isServiceWork(Context mContext, String serviceName) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(40);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName().toString();
            if (mName.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }

    public List<String> readFileLine(String strFilePath) {
        //判断文件是否存在
        if (new File(strFilePath).exists()) {
            List<String> txtList = new ArrayList<>();
            File file = new File(strFilePath);
            if (file.isDirectory()) {
                Log.d("TAG", "The File doesn't not exist.");
            } else {
                try {
                    InputStream instream = new FileInputStream(file);
                    if (instream != null) {
                        InputStreamReader inputreader = new InputStreamReader(instream);
                        BufferedReader buffreader = new BufferedReader(inputreader);
                        String line;
                        //逐行读取
                        while ((line = buffreader.readLine()) != null) {
                            txtList.add(line);
                        }
                        instream.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return txtList;
        } else {
            return null;
        }

    }

    public Bitmap getAlbumPicture(String dataPath) {
        android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(dataPath);
        byte[] data = mmr.getEmbeddedPicture();
        Bitmap albumPicture = null;
        if (data != null) {
            albumPicture = BitmapFactory.decodeByteArray(data, 0, data.length);
        }
        return albumPicture;
    }

    private static final String CHECK_OP_NO_THROW = "checkOpNoThrow";
    private static final String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";

    @SuppressLint("NewApi")
    public static boolean isNotificationEnabled(Context context) {

        AppOpsManager mAppOps =
                (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);

        ApplicationInfo appInfo = context.getApplicationInfo();
        String pkg = context.getApplicationContext().getPackageName();
        int uid = appInfo.uid;
        Class appOpsClass = null;
        /* Context.APP_OPS_MANAGER */
        try {
            appOpsClass = Class.forName(AppOpsManager.class.getName());

            Method checkOpNoThrowMethod =
                    appOpsClass.getMethod(CHECK_OP_NO_THROW,
                            Integer.TYPE, Integer.TYPE, String.class);

            Field opPostNotificationValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION);
            int value = (Integer) opPostNotificationValue.get(Integer.class);

            return ((Integer) checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg) ==
                    AppOpsManager.MODE_ALLOWED);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
