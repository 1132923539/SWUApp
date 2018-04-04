package com.christmas.swuwifi;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;


/**
 * Created by hxiaohua on 2017/6/7.
 * http://blog.csdn.net/u014492609/article/details/51475254
 * 完美方案，适用于后台长期执行任务的需要
 */
public class HorizonService extends Service{


    private String userName;
    private String passWord;
    private int time=10*60*1000;//定时任务时间，单位ms

    public void onCreate() {
        super.onCreate();
        load();
    }
    //必须放在OnStartCommand
    public int onStartCommand(Intent intent, int flags, int startId) {

        Context context=getApplicationContext();
        String wifi_name=okJson.getConnectWifiSsid(context);
        if(wifi_name==null||wifi_name.contains("swu-wifi"))
            return 0;
        //20171008 定时认证下线，排除swu-wifi，针对笔记本和宿舍路由器
        final Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String re=msg.obj.toString();
                Intent i;
                if(re.contains("success"))
                    i = new Intent("hxh.sucess");
                else
                    i= new Intent("hxh.fail");
                sendBroadcast(i);
                //Toast.makeText(HorizonService.this,re, Toast.LENGTH_SHORT).show();
                //取消认证提醒在这里
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i("打印时间", "打印时间: " + new Date().toString());
                new ThTask(userName, passWord, handler).start();
            }
        }).start();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        long triggerAtTime = SystemClock.elapsedRealtime() + time;
        Intent i = new Intent(this, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }
    //抽象方法
    public IBinder onBind(Intent intent) {
        return null;
    }
    private void load() {
        Context ctx = getApplicationContext();
        SharedPreferences sp = ctx.getSharedPreferences("chris", MODE_PRIVATE);
        userName = sp.getString("username0", "");
        passWord = sp.getString("password0", "");
    }
}
