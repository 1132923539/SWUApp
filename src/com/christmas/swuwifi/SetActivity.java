package com.christmas.swuwifi;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Set;

/*20170607 Created By hxiaohua
*这里是设置页面，主要加入一些常用开关设置
* 注意，用户勾选定时认证下线
 */


public class SetActivity extends Activity {

    private Button bt2;
    private CheckBox checkBox1;
    private CheckBox checkBox2;
    private CheckBox checkBox3;
    private CheckBox checkBox4;
    private Boolean TimeTask;
    private Boolean SetAutoLogin;
    private Boolean SetAutoStart;
    private Boolean SetNotice;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.setting);
        initView();
    }
    //销毁进程之前，需要保存用户信息和服务的处理
    protected void OnDestroy(){
        SaveSet();
        super.onDestroy();
    }
    //控件初始化
    public void initView()
    {
        bt2=(Button)findViewById(R.id.button2);
        checkBox1=(CheckBox)findViewById(R.id.checkBox1);
        checkBox2=(CheckBox)findViewById(R.id.checkBox2);
        checkBox3=(CheckBox)findViewById(R.id.checkBox3);
        checkBox4=(CheckBox)findViewById(R.id.checkBox4);
        Context ctx = SetActivity.this;
        SharedPreferences sp = ctx.getSharedPreferences("chris", MODE_PRIVATE);
        checkBox1.setChecked(sp.getBoolean("TimeTask",true));
        checkBox2.setChecked(sp.getBoolean("auto",true));
        checkBox3.setChecked(sp.getBoolean("AutoStart",true));
        checkBox4.setChecked(sp.getBoolean("Notice",true));
    }
    public void SaveSetting(View view)
    {
        SaveSet();
    }
    public void SaveSet()
    {
        TimeTask=checkBox1.isChecked();
        SetAutoLogin=checkBox2.isChecked();
        SetAutoStart=checkBox3.isChecked();
        SetNotice=checkBox4.isChecked();
        Context ctx = SetActivity.this;
        SharedPreferences sp = ctx.getSharedPreferences("chris", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("TimeTask",TimeTask);
        editor.putBoolean("auto",SetAutoLogin);
        editor.putBoolean("AutoStart",SetAutoStart);
        editor.putBoolean("Notice", SetNotice);
        editor.commit();
        Toast.makeText(this, "设置已保存", Toast.LENGTH_SHORT).show();
        DoSetting();
    }
    public void DoSetting()
    {
        if(TimeTask)
        {
            Intent i = new Intent(this, HorizonService.class);
            startService(i);
        }else
        {
            Intent i = new Intent(this, HorizonService.class);
            stopService(i);
        }
        if(SetAutoStart)
        {
            Intent i = new Intent(this, MyService.class);
            startService(i);
        }else
        {
            Intent i = new Intent(this, MyService.class);
            stopService(i);
        }

    }
    public void CheckUpdate(View view)
    {
        Toast.makeText(SetActivity.this, "检查更新功能实现中", Toast.LENGTH_SHORT).show();
    }
    //Go bo back,finish this process
    public void goBack(View view){
        finish();
    }

    //判断后台服务状态
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
            Log.i("所有服务", mName);
            if (mName.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }
}
