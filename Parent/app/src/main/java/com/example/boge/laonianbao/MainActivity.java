package com.example.boge.laonianbao;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.design.widget.NavigationView;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.boge.laonianbao.HealthTips.HealthTipsActivity;
import com.example.boge.laonianbao.Music.MusicActivity;
import com.example.boge.laonianbao.PersonInfo.PersonInfo;
import com.example.boge.laonianbao.Receiver.LocationReceiver;
import com.example.boge.laonianbao.Receiver.SensorReceiver;
import com.example.boge.laonianbao.VoiceAndWord.WordToVoice;
import com.example.boge.laonianbao.bluetooth.BluetoothService;
import com.example.boge.laonianbao.global.KDXFinit;
import com.example.boge.laonianbao.help.HelpUse;
import com.example.boge.laonianbao.login.Login;
import com.example.boge.laonianbao.map.map;
import com.example.boge.laonianbao.news.NewsActivity;
import com.example.boge.laonianbao.personFeature.feature_stand;
import com.example.boge.laonianbao.sensor.Fall;
import com.example.boge.laonianbao.wheelview.set.Setting;
import com.example.boge.laonianbao.step.activity.StepActivity;
import com.example.boge.laonianbao.weather.SearchWeather;
import com.example.boge.laonianbao.zpNUM.Data;
import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.Inflater;

import me.drakeet.materialdialog.MaterialDialog;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    TextView weather;
    TextView train;
    TextView step;
    TextView healthTips;
    TextView news;
    TextView music;
    MaterialDialog mMaterialDialog;
    View view;
    List<String> list;
    BluetoothAdapter bluetoothAdapter;
    ArrayAdapter<String> arrayAdapter;
    final int REQUEST_ENABLE_BLE=3;
    int x;
    private BluetoothService bluetoothService=null;
    public static final int MESSAGE_STATE_CHANGE = 1; // 状态改变
    public static final int MESSAGE_READ = 2;          // 读取数据
    public static final int MESSAGE_DEVICE_NAME = 3;  // 设备名字
    public static final int MESSAGE_TOAST = 4;         // Toast
    //传感器
    public static final int LEFT_BLUETOOTH=1; //左脚蓝牙
    public static final int RIGHT_BLUETOOTH=2; //右脚蓝牙

    public static float[] m_receive_data_left;
    public static float[] m_receive_data_right;
    private String mConnectedDeviceName = null;
    private boolean firstAdapter=true;
    private boolean first_stand=true;
    private boolean warning=false;
    long starttime=0;
    long endtime=0;
    boolean first=true;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        XGPushConfig.enableDebug(this, false);
        Context context = getApplicationContext();
        Log.i("XPush", PersonInfo.getPersonInfo().getAccount());
        XGPushManager.registerPush(context, PersonInfo.getPersonInfo().getAccount(),new XGIOperateCallback() {
            @Override
            public void onSuccess(Object o, int i) {
                Log.i("XPush","注册成功 token:"+o);
            }

            @Override
            public void onFail(Object o, int i, String s) {
                Log.i("XPush","注册失败 "+"错误代码"+i+"错误原因:"+s);

            }
        });
        weather = (TextView) findViewById(R.id.weather);
        weather.setClickable(true);
        weather.setOnClickListener(this);

        step = (TextView) findViewById(R.id.step);
        step.setClickable(true);
        step.setOnClickListener(this);

        healthTips = (TextView) findViewById(R.id.healthTips);
        healthTips.setClickable(true);
        healthTips.setOnClickListener(this);

        news = (TextView) findViewById(R.id.news);
        news.setClickable(true);
        news.setOnClickListener(this);

        music = (TextView) findViewById(R.id.MovieMusic);
        music.setClickable(true);
        music.setOnClickListener(this);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);    ///header view

        startService(new Intent(MainActivity.this, LocationReceiver.class));
        startService(new Intent(MainActivity.this, SensorReceiver.class));


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_selfInfo) {
            view= LayoutInflater.from(MainActivity.this).inflate(R.layout.blue_tooth,null);
            ListView listView=(ListView)view.findViewById(R.id.listview);
            list=new ArrayList<String>();
            arrayAdapter=new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_expandable_list_item_1,list);
            listView.setAdapter(arrayAdapter);
            listView.setOnItemClickListener(mDeviceClickListener);
            if(firstAdapter) {
                initBlueTooth();  //需要判断 是否是第一次点击
                firstAdapter=false;
            }
             mMaterialDialog = new MaterialDialog(this)
                    .setTitle("MaterialDialog")
                    .setMessage("Hello world!")
                    .setPositiveButton("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mMaterialDialog.dismiss();

                        }
                    })
                    .setNegativeButton("CANCEL", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mMaterialDialog.dismiss();

                        }
                    }).setView(view);

            mMaterialDialog.show();
            doDiscovery();



        } else if (id == R.id.menu_timeSpeaker) {
            Calendar calendar = Calendar.getInstance();
            int hourofday = calendar.get(Calendar.HOUR_OF_DAY);
            int hour = calendar.get(Calendar.HOUR);
            int minute = calendar.get(Calendar.MINUTE);
            String morningorafternoon;
            if (hourofday < 12) morningorafternoon = "上午";
            else morningorafternoon = "下午";
            String message = "现在是" + morningorafternoon + hour + "时" + minute + "分";

            WordToVoice wordToVoice = new WordToVoice(MainActivity.this, message,Data.getB());
            wordToVoice.GetVoiceFromWord();
        } else if (id == R.id.menu_sos) {
            //急救电话
            String phoneNum = "13547933655";
            try {
                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNum)));
            } catch (SecurityException e) {
                Toast.makeText(MainActivity.this, "未获得通话权限", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.menu_call) {
            //子女电话
            String phoneNum = "13466933690";
            try {
                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNum)));
            } catch (SecurityException e) {
                Toast.makeText(MainActivity.this, "未获得通话权限", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.menu_help) {
            startActivity(new Intent(MainActivity.this, HelpUse.class));
        } else if (id == R.id.menu_location) {
            startActivity(new Intent(MainActivity.this,map.class));
        } else if (id == R.id.menu_setting) {
            startActivity(new Intent(MainActivity.this, Setting.class));
        } else if (id == R.id.menu_exit) {
            finish();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.weather:
                startActivity(new Intent(MainActivity.this, SearchWeather.class));
                break;
            case R.id.step:
                startActivity(new Intent(MainActivity.this, StepActivity.class));
                break;
            case R.id.healthTips:
                startActivity(new Intent(MainActivity.this, HealthTipsActivity.class));
                break;
            case R.id.news:
                startActivity(new Intent(MainActivity.this, NewsActivity.class));
                break;
            case R.id.MovieMusic:
                startActivity(new Intent(MainActivity.this, MusicActivity.class));

        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    ////////////////地址选取  需要修改
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            bluetoothAdapter.cancelDiscovery();//选中一个Device就取消搜寻
            String info = ((TextView) view).getText().toString();
            StringTokenizer tokenizer=new StringTokenizer(info,"\n");
            String name=tokenizer.nextToken();
            String address =tokenizer.nextToken();
            BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
            Log.i("LB",address);
            bluetoothService.connect(bluetoothDevice, RIGHT_BLUETOOTH);

        }
    };
    private void initBlueTooth(){
            /**
            * 判断手机是否支持蓝牙
             */
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            Toast.makeText(this, "该手机不支持蓝牙", Toast.LENGTH_SHORT).show();
            finish();
        }
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "该手机不支持蓝牙", Toast.LENGTH_SHORT).show();
            finish();

        }
        bluetoothService=new BluetoothService(MainActivity.this,mhandler);
        /**
         *
         * 开启蓝牙
         *
         */
        if (!bluetoothAdapter.isEnabled()) {
            Intent mBleIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            this.startActivityForResult(mBleIntent, REQUEST_ENABLE_BLE);
        } /** end of if (!mBleAdapter.isEnabled()) */

        /**
         * 如果蓝牙自动寻找蓝牙  关闭*/
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        /**
         * 注册广播
         */
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        IntentFilter filter1 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter1);


    }

    /**
     * 广播
     */
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //找到设备
            if  (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    list.add(device.getName() + "\n" + device.getAddress());
                    arrayAdapter.notifyDataSetChanged();
                }else {
                    list.add(device.getName() + "\n" + device.getAddress());
                    arrayAdapter.notifyDataSetChanged();
                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (list.size()==0) {
                    String noDevices = "未发现设备";
                }
            }
        }
    };
    Handler mhandler=new Handler(new Handler.Callback() {


        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case MESSAGE_READ:
                    try {
                        String str=msg.getData().getString("index");
                        int index=Integer.valueOf(str);
                        switch (index)
                        {
                            //获取到蓝牙传输过来的数据
                            case LEFT_BLUETOOTH:
                               // m_receive_data_left=msg.getData().getFloatArray("Data");
                                //break;
                            //实际只用到这个case ，因为demo只连接了一个硬件设备
                            case RIGHT_BLUETOOTH:
                               String mess=msg.getData().getString("data");

                                double A=Double.parseDouble(mess.substring(0,4));
                                double B=Double.parseDouble(mess.substring(5,9));
                                double C=Double.parseDouble(mess.substring(10,14));
                                Log.i("Test","A:"+A+"B:"+B+"C:"+C);
                                if(judgeFall(A)&&judgeFall(B)&&judgeFall(C)&&first){
                                    starttime=System.currentTimeMillis();
                                    first=false;
                                }else if(judgeFall(A)&&judgeFall(B)&&judgeFall(C)&&!first){
                                    endtime=System.currentTimeMillis();
                                }else if(!judgeFall(A) || !judgeFall(B) || !judgeFall(C)){
                                    starttime=0;
                                    endtime=0;
                                    first=true;
                                }else {

                                }
                                if((endtime-starttime)/1000 >=4&& Fall.isFell()&&!warning) //4s则为跌倒
                                {
                                    Log.i("Time",(endtime-starttime)/1000+"---->warning");
                                    showWarning();          //跌倒
                                    warning=true;
                                }else if((endtime-starttime)/1000 >=4&&!Fall.isFell()){ //硬件误判条件
                                    starttime=0;
                                    endtime=0;
                                    first=true;
                                }else if((endtime-starttime)/1000 <4&&Fall.isFell()){ //加速度传感器误判
                                    Fall.setFell(false);
                                    startService(new Intent(MainActivity.this, SensorReceiver.class));
                                }else{

                                }
                                /*if(first_stand) {
                                    startActivity(new Intent(MainActivity.this, feature_stand.class));
                                    first_stand=false;
                                }*/

                                break;
                        }
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                    break;
                case MESSAGE_STATE_CHANGE:
//                    连接状态
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            break;
                        case BluetoothService.STATE_LISTEN:
                            break;
                        case BluetoothService.STATE_NONE:
                            break;
                    }
                    break;
                case MESSAGE_DEVICE_NAME:
                    mConnectedDeviceName = msg.getData().getString("device_name");
                    Log.i("bluetooth","成功连接到:"+mConnectedDeviceName);
                    Toast.makeText(getApplicationContext(),"成功连接到设备" + mConnectedDeviceName,Toast.LENGTH_SHORT).show();
                    mMaterialDialog.dismiss();

                    break;
                case MESSAGE_TOAST:
                    int index=msg.getData().getInt("device_id");
                    Toast.makeText(getApplicationContext(),msg.getData().getString("toast"), Toast.LENGTH_SHORT).show();
                    //当失去设备或者不能连接设备时，重新连接
                    Log.d("Magikare","当失去设备或者不能连接设备时，重新连接");
                    break;
            }
            return false;
        }
    });
    void doDiscovery()
    {
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        bluetoothAdapter.startDiscovery();
    }
    private void showWarning(){

        final Vibrator vibrator=(Vibrator)getSystemService(Service.VIBRATOR_SERVICE);
        final TextView textView=new TextView(this);
        final Timer timer=new Timer();
        textView.setTextColor(getResources().getColor(R.color.red));
        textView.setTextSize(30);
        textView.setGravity(Gravity.CENTER);
        final MaterialDialog materialDialog1=new MaterialDialog(KDXFinit.getActivity());
        materialDialog1.setTitle("是否取消警报")
                .setMessage("是否取消警报")
                .setContentView(textView)
                .setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        starttime=0;
                        endtime=0;
                        warning=false;
                        Fall.setFell(false);
                        startService(new Intent(MainActivity.this, SensorReceiver.class));
                        materialDialog1.dismiss();
                        vibrator.cancel();
                        timer.cancel();
                        first=true;
                    }
                });
        materialDialog1.show();
        vibrator.vibrate(new long[]{ 0,4000,1000,4000,1000,4000,1000,4000,1000,4000,1000} , -1);
        x=30;
        final Handler handler=new Handler(){
            public void handleMessage(Message msg) {
                if(msg.what == 1){
                    textView.setText(x+"");
                    if(x==0){
                        //向子女发送报警信息
                        materialDialog1.dismiss();
                        timer.cancel();
                        Toast.makeText(KDXFinit.getAppContext(),"发送求救短信...",Toast.LENGTH_LONG).show();
                        SmsManager manager = SmsManager.getDefault();
                        String ms="您的亲属（账号为："+PersonInfo.getPersonInfo().getAccount()+")发生跌倒，请尽快登陆App查看亲属位置";
                        ArrayList<String> list = manager.divideMessage(ms);  //因为一条短信有字数限制，因此要将长短信拆分
                        for(String text:list){
                            manager.sendTextMessage(PersonInfo.getPersonInfo().getTel_number_contact1(), null, text, null, null);
                        }
                    }
                }
            }
        };


        TimerTask timerTask=new TimerTask() {
            @Override
            public void run() {
                if(x>0){
                    Message message=new Message();
                    message.what=1;
                    handler.sendMessage(message);
                    x--;
                }
            }
        };
        timer.schedule(timerTask,1000,1000);
    }
    private boolean judgeFall(double number){
        if(number<=3.35&&number>=3.20)
            return  true;
        else
            return false;

    }
}

