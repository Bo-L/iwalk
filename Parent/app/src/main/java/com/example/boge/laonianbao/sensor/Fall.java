package com.example.boge.laonianbao.sensor;

/**
 * Created by liubo on 2017/11/10.
 */

public class Fall {
    private float highThresholdValue; //高阈值
    private float lowThresholdValue;  //低阈值
    //    private int SENSOR_RATE;
    private static boolean isFell;
    public static float[] svmData;
    public static float[] svmFilteringData;
    public static int svmCount = 0;
    public Fall(){
        svmData = new float[150];
        svmFilteringData = new float[150];
        isFell = false;
    }
    /*
    设置阈值
     */
    public void setThresholdValue(float highThreshold, float lowThreshold){
        this.highThresholdValue = highThreshold;
        this.lowThresholdValue = lowThreshold;

    }
    /*
    跌倒检测
     */
    public  void fallDetection(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean running = true;
                while(running){
                    //阈值法
                    for(int i = 0; i < svmFilteringData.length; i++){
                        if(svmFilteringData[i] <= lowThresholdValue){
                            if(i < svmFilteringData.length-10){
                                for (int j = i; j < i + 10; j++) {
                                    if (svmFilteringData[j] >= highThresholdValue) {
                                        running = false;
                                        setFell(true);
                                    }
                                }
                            }else {
                                for (int j = i; j < svmFilteringData.length; j++) {
                                    if (svmFilteringData[j] >= highThresholdValue) {
                                        running = false;
                                        setFell(true);
                                    }
                                }
                                for (int k = 0; k < (10-(svmFilteringData.length - i)); k++){
                                    if (svmFilteringData[k] >= highThresholdValue) {
                                        running = false;
                                        setFell(true);
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }).start();
    }
    /*
    3s内svm原始数据收集
     */
    public static void svmCollector(float svm){

        if(svmCount < svmData.length){
            svmData[svmCount] = svm;
        }else{
            svmCount = 0;
            svmData[svmCount] = svm;
        }
        svmCount++;
    }
    /*
    svm中值滤波
     */
    public static void setSvmFilteringData(){
        //中值滤波取的三个值
        float s1, s2, s3, temp;
        //冒泡排序
        for (int i = 0; i < svmFilteringData.length-1; i++){
            if(i == 0){
                s1 = svmData[i];
                s2 = svmData[i + 1];
                s3 = svmData[i + 2];
            }else if(i < svmFilteringData.length-2){
                s1 = svmData[i - 1];
                s2 = svmData[i];
                s3 = svmData[i + 1];
            }else{
                s1 = svmData[i - 1];
                s2 = svmData[i];
                s3 = svmData[0];
            }
            if(s1 > s2){
                temp = s1;
                s1 = s2;
                s2 = temp;
            }
            if(s2 > s3){
                temp = s2;
                s2 = s3;
                s3 = temp;
            }
            svmFilteringData[i] = s2;
//            Log.d(TAG, "Fall.setSvmFilteringData" + svmFilteringData[i]);
        }

    }
    public static boolean isFell() {
//        Log.e(TAG, "isFELL" + isFell);
        return isFell;

    }
    public static void setFell(boolean fell) {
        isFell = fell;
//        Log.e(TAG, "setFELL" + isFell);
    }
    public void cleanData(){
        for (int i = 0; i < svmData.length; i++){
            svmData[i] = 0;
        }
        //中值滤波
        for (int i = 0; i < svmFilteringData.length; i++){
            svmFilteringData[i] = 0;
        }
    }

}
