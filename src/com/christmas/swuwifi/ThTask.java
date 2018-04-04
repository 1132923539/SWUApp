package com.christmas.swuwifi;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;

/**
 * Created by hxiaohua on 20170606
 * 定时认证下线的任务执行中
 * 20170810 已认证，将使用该账号操作
 */
public class ThTask extends Thread{

    private String username;
    private String password;
    private Handler handler;
    ThTask(String username,String password,Handler handler)
    {
        this.username=username;
        this.password=password;
        this.handler=handler;
    }
    public void run ()
    {
        Message msg = new Message();
        String str="默认信息";
        SimpleDateFormat sDateFormat = new SimpleDateFormat("HH:mm");
        String date = sDateFormat.format(new java.util.Date());
        //查看本机的wifi名字
        try
        {
            okHttp http=new okHttp();
            //尝试获取已认证账号密码
            String StrJson=http.getHtml("http://222.198.127.170/eportal/InterFace.do?method=getOnlineUserInfo","utf-8");
            ParseJson(StrJson);
            //认证下线一次
            str=http.getHtml("http://222.198.127.170/eportal/InterFace.do?method=logout","utf-8");
            //str+=http.swuLogin(username,password);
            str+=http.Login(username,password);
            msg.obj=date+"\n"+str;
        }
        catch (Exception e)
        {
            msg.obj=date+"\n定时任务出错，请调试\n"+e.getMessage();
            Log.i("定时错误", e.getMessage());
            //msg.obj=date+"\n下线失败，认证失败";
        }
        finally
        {
            handler.sendMessage(msg);
        }
        //处理异常信息与登录失败的信息
    }

    private void ParseJson(String jsonData){
        if(jsonData.contains("fail"))//获取用户信息失败
            return;
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            //解析账号密码
            this.username = jsonObject.getString("userId");
            this.password = jsonObject.getString("password");
            Log.d("Thtask","解析账号: " + username);
            Log.d("Thtask","解析密码: " + password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
