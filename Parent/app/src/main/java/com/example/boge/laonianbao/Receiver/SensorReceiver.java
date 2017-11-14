package com.example.boge.laonianbao.Receiver;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.example.boge.laonianbao.sensor.Fall;
import com.example.boge.laonianbao.sensor.FallSensorManager;

/**
 * Created by liubo on 2017/11/10.
 */

public class SensorReceiver extends Service {
    private FallSensorManager fallSensorManager;
    public Fall fall;
    //    private final int TIME = 1;
    private boolean running = false;
    private DetectThread detectThread;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        fallSensorManager = new FallSensorManager(this);
        fallSensorManager.initSensor();
        fallSensorManager.registerSensor();
        fall = new Fall();
        fall.setThresholdValue(25,5);

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        running = true;
        detectThread = new DetectThread();
        detectThread.start();
        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public void onDestroy() {
        fallSensorManager.unregisterSensor();
        super.onDestroy();
    }
    class DetectThread extends Thread{
        @Override
        public void run() {
            fall.fallDetection();

            while (running) {
                if (fall.isFell()) {
                    running = false;
                    //fall.setFell(false);
                    fall.cleanData();


                }
            }
        }
    }
}
