package com.example.boge.laonianbao.personFeature;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.boge.laonianbao.R;

import java.util.Timer;
import java.util.TimerTask;

public class feature_stand extends AppCompatActivity {

    private Button button;
    int i=10;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feature_stand);
        button=(Button)findViewById(R.id.button);
        final Handler handler=new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                button.setText(i+"");
                i--;
                if(i==1){
                    startActivity(new Intent(feature_stand.this,feature_sit.class));
                    finish();
                }
                return false;
            }
        });
        final Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i=0;i<10;i++) {
                    Message message = new Message();
                    handler.sendMessage(message);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                thread.start();
                button.setEnabled(false);
            }
        });



    }
}
