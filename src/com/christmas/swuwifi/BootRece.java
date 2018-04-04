package com.christmas.swuwifi;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

/**
 * Created by hxiaohua on 2016/11/23.
 * 用于实现开机进程自动启动、后台自启
 */
public class BootRece extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {

        SharedPreferences sp = context.getSharedPreferences("chris", Context.MODE_PRIVATE);
        if(!sp.getBoolean("AutoStart",true))
            return;
        //开机启动两个进程，自动登录与定时登录
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
        {
            Intent ServiceIntent = new Intent(context, MyService.class);
            context.startService(ServiceIntent);
            Intent i = new Intent(context, HorizonService.class);
            context.startService(i);
        }
        else
        if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION))
        {
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if(info.getState().equals(NetworkInfo.State.CONNECTING)){
                //Toast.makeText(context, "连接到wifi网络", Toast.LENGTH_SHORT).show();
                if (!isServiceWork(context, "com.christmas.swuwifi.MyService")) {
                   Intent startIntent = new Intent(context, MyService.class);
                   context.startService(startIntent);
               }
                if (!isServiceWork(context, "com.christmas.swuwifi.HorizonService")) {
                    Intent i = new Intent(context, HorizonService.class);
                    context.startService(i);
                    //Toast.makeText(context, "绿色守护定时启动", Toast.LENGTH_SHORT).show();
                }
           }
        }
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
            if (mName.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }
}
