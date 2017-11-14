package com.example.boge.laonianbao.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.boge.laonianbao.MainActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

/**
 * Created by Boge on 2017/4/26.
 */

public class BluetoothService {
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private Context context;
    //蓝牙适配器
    private BluetoothAdapter mAdapter;
    private Handler mHandler;

    //当前传感器设备的个数，即要开启的线程个数，用于设置线程数组的大小
    public static final int SENSEOR_NUM = 2;
    private ConnectThread mConnectThread;// 连接一个设备的进程
    public ConnectedThread[] mConnectedThread = new ConnectedThread[SENSEOR_NUM];// 已经连接之后的管理进程

    private int mState;                             //当前状态
    // 指明连接状态的常量
    public static final int STATE_NONE = 0;         //没有连接
    public static final int STATE_LISTEN = 1;       //等待连接
    public static final int STATE_CONNECTING = 2;   //正在连接
    public static final int STATE_CONNECTED = 3;    //已经连接

    public BluetoothService(Context context, Handler mHandler) {
        this.context = context;
        this.mHandler = mHandler;
        mAdapter = BluetoothAdapter.getDefaultAdapter();//获取蓝牙适配器
        mState = STATE_NONE; //当前连接状态：未连接
    }

    // 参数 index 是 硬件设备的id ，随便设的，目的在于当 同时连接多个硬件设备的时候，根据此id进行区分
    public synchronized void connect(BluetoothDevice device, int index) {

        //连接一个蓝牙时，将该设备 的蓝牙连接线程关闭，如果有的话
        if (mConnectedThread[index - 1] != null) {
            mConnectedThread[index - 1].cancel();
            mConnectedThread[index - 1] = null;
        }
        mConnectThread = new ConnectThread(device, index);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }


    //连接失败
    private void connectionFailed(int index) {
        setState(STATE_LISTEN);
        // Send a failure message back to the Activity
        Message msg =new Message();
        msg.what=MainActivity.MESSAGE_TOAST;
        Bundle bundle = new Bundle();
        bundle.putString("toast", "未能连接设备" + index);
        bundle.putInt("device_id", index);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    // 连接丢失
    private void connectionLost(int index) {
        setState(STATE_LISTEN);
        Message msg = new Message();
        msg.what=MainActivity.MESSAGE_TOAST;
        Bundle bundle = new Bundle();
        bundle.putString("toast", "设备丢失" + index);
        bundle.putInt("device_id", index);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    //用于 蓝牙连接的Activity onResume()方法
    public synchronized void start() {
        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        setState(STATE_LISTEN);
    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device, int index) {
        Log.d("MAGIKARE", "连接到线程" + index);
        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread[index - 1] = new ConnectedThread(socket, index);

        mConnectedThread[index - 1].start();

        // Send the name of the connected device back to the UI Activity
        Message msg = new Message();
        msg.what=MainActivity.MESSAGE_DEVICE_NAME;
        Bundle bundle = new Bundle();
        bundle.putString("device_name", device.getName() + " " + index);

        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }

    private synchronized void setState(int state) {
        mState = state;
        // Give the new state to the Handler so the UI Activity can update
       // mHandler.obtainMessage(MainActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    public synchronized int getState() {
        return mState;
    }

    public synchronized void stop() {
        if (mConnectedThread != null) {
            for (int i = 0; i < mConnectedThread.length; i++) {
                mConnectedThread[i].cancel();
            }
            mConnectedThread = null;
        }

        setState(STATE_NONE);
    }
    /**
     * 连接线程
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private int index;

        public ConnectThread(BluetoothDevice device, int index) {
            mmDevice = device;
            this.index = index;
            BluetoothSocket tmp = null;
            try {
                tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);// Get a BluetoothSocket for a connection with the given BluetoothDevice
            } catch (IOException e) {
                Log.i("LB", "UUID 出错");
            }
            mmSocket = tmp;
        }

        public void run() {

            setName("ConnectThread");
            //当连接成功，取消蓝牙适配器搜索蓝牙设备的操作，因为搜索操作非常耗时
           // mAdapter.cancelDiscovery();// Always cancel discovery because it will slow down a connection

            try {
                mmSocket.connect();// This is a blocking call and will only return on a successful connection or an exception
            } catch (IOException e) {
                Log.i("LB", "连接出错");
                connectionFailed(this.index);
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                }

                BluetoothService.this.start();// 引用来说明要调用的是外部类的方法 run
                return;
            }

            synchronized (BluetoothService.this) {// 当连接到了  设线程为空
                mConnectThread = null;
            }
            connected(mmSocket, mmDevice, index);// Start the connected thread
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * 连接之后的线程
     */
    class ConnectedThread extends Thread {
        private BluetoothSocket mmSocket;
        private InputStream mmInStream;
        private int index;

        //构造方法
        public ConnectedThread(BluetoothSocket socket, int index) {
            mmSocket = socket;
            InputStream tmpIn = null;
            this.index = index;
            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {

            }

            mmInStream = tmpIn;
        }

        @Override
        public void run() {
            while (true) {
                final byte[] bytes = new byte[1024];// 缓冲数据流
                List<Byte> list= new ArrayList<Byte>();
                int count;// 返回读取到的数据
                int k=1;
               String out="";
                // 监听输入流
                try {
                while (mmInStream != null && (count = mmInStream.read(bytes)) != -1) {
                //获取有效部分
                 byte[] content=new byte[count];
                 for (int i = 0; i < count; i++) {
                  //list.add(bytes[i]);
                     content[i]=bytes[i];
                     list.add(content[i]);
                     if(list.size()==14){
                     // Log.i("Test1",new String(ObjectToByte(list)));
                      Message msg = new Message();
                      msg.what=MainActivity.MESSAGE_READ;
                      Bundle bundle = new Bundle();
                      bundle.putString("index",index+"");
                      out=new String(ObjectToByte(list));
                      bundle.putString("data",out);
                      msg.setData(bundle);
                      mHandler.sendMessage(msg);
                      list.clear();
                     }
                  // Log.i("Test1",k+"----->"+count);

                 }
                  out=new String(content);
                    // Log.i("Test1",k+"----->"+count+"-->"+out);
                 }
                 } catch (Exception e) {
                 e.printStackTrace();
                    connectionFailed(this.index);
                 }


            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
        final protected  char[] hexArray = "0123456789ABCDEF".toCharArray();
        public  String bytesToHex(byte[] bytes) {
            char[] hexChars = new char[bytes.length * 2];
            for ( int j = 0; j < bytes.length; j++ ) {
                int v = bytes[j] & 0xFF;
                hexChars[j * 2] = hexArray[v >>> 4];
                hexChars[j * 2 + 1] = hexArray[v & 0x0F];
            }
            return new String(hexChars);
        }
        public byte[] ObjectToByte(List<Byte> list){
            byte[] bytes=new byte[14];
            for(int i=0;i<14;i++){
                bytes[i]=list.get(i);
            }
            return bytes;
        }
    }
}

