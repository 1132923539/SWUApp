package com.christmas.swuwifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * Created by hxiaohua on 2017/6/7.
 *结合HorizonService来使用，
 * 相当于闹钟一样，定时广播，定时启动服务
 */
public class AlarmReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {

        SharedPreferences sp = context.getSharedPreferences("chris", Context.MODE_PRIVATE);
        if(!sp.getBoolean("TimeTask",true))
            return;
        Intent i = new Intent(context, HorizonService.class);
        context.startService(i);
    }
}
