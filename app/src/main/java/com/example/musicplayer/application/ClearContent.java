package com.example.musicplayer.application;

import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.musicplayer.receiver.MediaReceiver;
import com.example.musicplayer.receiver.TaskListReceiver;
import com.example.musicplayer.tool.ExceptionCaugh;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

public class ClearContent extends Application {
    private List<Activity> activityList = new LinkedList<Activity>();
    private static ClearContent instance;
    public static Context context;
    private TaskListReceiver taskListReceiver;
    private Activity nowActivity = null;
    public static int count = 0;
    public static BluetoothDevice device;

    public static ClearContent getInstance() {
        if (null == instance) {
            instance = new ClearContent();
        }
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        ExceptionCaugh exceptionCaugh = ExceptionCaugh.getInstance();
        exceptionCaugh.init(getApplicationContext());

        taskListReceiver = new TaskListReceiver();
        registerReceiver(taskListReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        ComponentName mComponent = new ComponentName(getPackageName(), MediaReceiver.class.getName());
        mAudioManager.registerMediaButtonEventReceiver(mComponent);

        this.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                nowActivity = activity;
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {

            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {

            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {

            }
        });
        needBlue();
    }


    public void needBlue() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        int a2dp = bluetoothAdapter.getProfileConnectionState(BluetoothProfile.A2DP);
        int headset = bluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET);
        int health = bluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEALTH);

        int flag = -1;
        if (a2dp == BluetoothProfile.STATE_CONNECTED) {
            flag = a2dp;
        } else if (headset == BluetoothProfile.STATE_CONNECTED) {
            flag = headset;
        } else if (health == BluetoothProfile.STATE_CONNECTED) {
            flag = health;
        }
        if (flag != -1) {
            bluetoothAdapter.getProfileProxy(ClearContent.context, new BluetoothProfile.ServiceListener() {
                @Override
                public void onServiceDisconnected(int profile) {

                }

                @Override
                public void onServiceConnected(int profile, BluetoothProfile proxy) {
                    List<BluetoothDevice> mDevices = proxy.getConnectedDevices();
                    if (mDevices != null && mDevices.size() > 0) {
                        for (BluetoothDevice device : mDevices) {
                            ClearContent.device = device;
                            Log.i("getdevice", "device name: " + device.getName());
                        }
                    } else {
                        Log.i("uncondevice", "mDevices is null");
                    }
                }
            }, flag);
        }
    }

    public void addActivity(Activity activity) {
        activityList.add(activity);
    }

    public void exit() {
        for (Activity activity : activityList) {
            activity.finish();
        }
        activityList.clear();
    }

    public Activity getActivity() {
        return nowActivity;
    }


    public static Activity getCurrentActivity() {
        Activity currentActivity = null;
        synchronized (activityUpdateLock) {
            if (activityWeakReference != null) {
                currentActivity = activityWeakReference.get();
            }
        }
        return currentActivity;
    }

    private static WeakReference<Activity> activityWeakReference;
    private static Object activityUpdateLock = new Object();

    public static void setCurrentActivity(Activity activity) {
        synchronized (activityUpdateLock) {
            activityWeakReference = new WeakReference<Activity>(activity);
        }

    }


}

