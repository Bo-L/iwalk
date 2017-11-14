package com.example.boge.laonianbao.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by liubo on 2017/11/10.
 */

public class FallSensorManager {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Context context;
    private float accX, accY, accZ;
    private float svm;
    public Fall fall;

    public FallSensorManager(Context context) {
        this.context = context;
        fall = new Fall();
    }

    /*
    加载传感器
     */
    public void initSensor() {
        //获取SensorManager，系统的传感器管理服务
        sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        //获取accelerometer加速度传感器
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }
    /*
    注册传感器
     */
    public void registerSensor(){
        sensorManager.registerListener(sensorEventListener,
                accelerometer, SensorManager.SENSOR_DELAY_GAME);

    }
    public void unregisterSensor(){
        sensorManager.unregisterListener(sensorEventListener);

    }
    public SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    accX = event.values[0];
                    accY = event.values[1];
                    accZ = event.values[2];
                    svm = (float) Math.sqrt(accX * accX + accY * accY + accZ * accZ);
//                    Log.d(TAG,accX + "  " + accY + "  " + accZ );
                    Fall.svmCollector(svm);
                    Fall.setSvmFilteringData();
                    break;
            }
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
}
