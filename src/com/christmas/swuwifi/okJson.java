package com.christmas.swuwifi;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.json.JSONObject;

/**
 * Created by hxh on 2017/10/8.
 * 提供对json格式数据的解析，获取用户账号密码等
 */
public class okJson {

    public static String getStr(String jsonData,String key)
    {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            //解析账号
            String str= jsonObject.getString("userId");
            return str;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    public static String  getConnectWifiSsid(Context context){

        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        Log.d("wifiInfo", wifiInfo.toString());
        Log.d("SSID",wifiInfo.getSSID());
        return wifiInfo.getSSID();
    }
}
