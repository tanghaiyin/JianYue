package com.example.musicplayer.receiver;

import static com.example.musicplayer.application.ClearContent.device;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.musicplayer.activity.MainActivity;
import com.example.musicplayer.activity.PlayActive;
import com.example.musicplayer.application.ClearContent;
import com.example.musicplayer.service.MusicService;

public class BluetoothMonitorReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_ON:
                            Log.e("蓝牙正在打开", "蓝牙正在打开");
                            break;
                        case BluetoothAdapter.STATE_ON:
                            Log.e("蓝牙已经打开", "蓝牙已经打开");
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            Log.e("蓝牙正在关闭", "蓝牙正在关闭");
                            break;
                        case BluetoothAdapter.STATE_OFF:
                            startBiuest();
                            Log.e("蓝牙已经关闭", "蓝牙已经关闭");
                            break;
                    }
                    break;
                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    BluetoothClass bluetoothClass = device.getBluetoothClass();
                    Log.e("蓝牙设备已连接", "蓝牙设备已连接");
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    startBiuest();
                    Log.e("蓝牙设备已断开", "蓝牙设备已断开");
                    break;
            }

        }
    }

    public void startBiuest() {
        if (device != null) {
            BluetoothClass bluetoothClass = device.getBluetoothClass();
//                                final int deviceClass = bluetoothClass.getDeviceClass(); //设备类型（音频、手机、电脑、音箱等等）
            final int majorDeviceClass = bluetoothClass.getMajorDeviceClass();//具体的设备类型（例如音频设备又分为音箱、耳机、麦克风等等）
            Log.e("deviceClass23", majorDeviceClass + "===" + BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES);
            if (majorDeviceClass == 1024) {
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
            }
        }
    }
}