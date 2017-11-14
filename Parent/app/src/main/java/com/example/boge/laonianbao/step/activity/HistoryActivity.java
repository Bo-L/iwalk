package com.example.boge.laonianbao.step.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.List;

import com.example.boge.laonianbao.R;
import com.example.boge.laonianbao.step.adapter.CommonAdapter;
import com.example.boge.laonianbao.step.adapter.CommonViewHolder;
import com.example.boge.laonianbao.step.curve.StepCurve;
import com.example.boge.laonianbao.step.step.bean.StepData;
import com.example.boge.laonianbao.step.step.utils.DbUtils;

/**
 * Created by yuandl on 2016-10-18.
 */

public class HistoryActivity extends AppCompatActivity {
   // private LinearLayout layout_titlebar;
    private ImageView iv_left;
    private ListView lv;
    private TextView iv_right;
    private String[] date={"2017/11/6 星期一","2017/11/7 星期二","2017/11/8 星期三","2017/11/9 星期四","2017/11/10 星期五","2017/11/11 星期六","2017/11/12 星期日"};
    private String[] step_number={"20000","26322","54000","10000","23000","6000","0"};
    private ArrayListAdapter arrayListAdapter=null;
    private void assignViews() {
      //  layout_titlebar = (RelativeLayout) findViewById(R.id.layout_titlebar);
        iv_left = (ImageView) findViewById(R.id.iv_left);
        iv_right = (TextView) findViewById(R.id.iv_right);
        lv = (ListView) findViewById(R.id.lv);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_history);
        assignViews();
        iv_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        iv_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HistoryActivity.this, StepCurve.class));
            }
        });
        arrayListAdapter=new ArrayListAdapter(date,step_number);
        lv.setAdapter(arrayListAdapter);
        //initData();
    }

    private void initData() {
        setEmptyView(lv);
        if(DbUtils.getLiteOrm()==null){
            DbUtils.createDb(this, "jingzhi");
        }
        List<StepData> stepDatas =DbUtils.getQueryAll(StepData.class);
        lv.setAdapter(new CommonAdapter<StepData>(this,stepDatas,R.layout.item) {
            @Override
            protected void convertView(View item, StepData stepData) {
                TextView tv_date= CommonViewHolder.get(item,R.id.tv_date);
                TextView tv_step= CommonViewHolder.get(item,R.id.tv_step);
                tv_date.setText(stepData.getToday());
                tv_step.setText(stepData.getStep()+"步");
            }
        });
    }

    protected <T extends View> T setEmptyView(ListView listView) {
        TextView emptyView = new TextView(this);
        emptyView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        emptyView.setText("暂无数据！");
        emptyView.setGravity(Gravity.CENTER);
        emptyView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        emptyView.setVisibility(View.GONE);
        ((ViewGroup) listView.getParent()).addView(emptyView);
        listView.setEmptyView(emptyView);
        return (T) emptyView;
    }
    private class ArrayListAdapter extends BaseAdapter{

        String[] date=null;
        String[] step_number=null;
        ArrayListAdapter(String[] date,String[] step_number){
            this.date=date;
            this.step_number=step_number;
        }
        @Override
        public int getCount() {
            return date.length;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            MyViewHolder myViewHolder=null;
            if(view==null){
                myViewHolder=new MyViewHolder();
                view= LayoutInflater.from(getApplicationContext()).inflate(R.layout.item,null);
                myViewHolder.textView=(TextView)view.findViewById(R.id.tv_date);
                myViewHolder.textView2=(TextView)view.findViewById(R.id.tv_step);
                view.setTag(myViewHolder);
            }else {
                myViewHolder=(MyViewHolder)view.getTag();
            }
            myViewHolder.textView.setText(date[i]);
            myViewHolder.textView2.setText(step_number[i]);
            return view;
        }
    }
    private  class MyViewHolder{
        TextView textView;
        TextView textView2;
    }
}
