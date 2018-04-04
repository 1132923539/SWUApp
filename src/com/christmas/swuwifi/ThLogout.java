package com.christmas.swuwifi;

import android.os.Handler;
import android.os.Message;

/*
 * Created by hxiaohua on 2016/10/13.
 *用于退出登录线程
 */
public class ThLogout extends Thread{

    private String username;
    private String password;
    private Handler handler;
    ThLogout(String username,String password,Handler handler)
    {
        this.username=username;
        this.password=password;
        this.handler=handler;
    }
    public void run ()
    {
        Message msg = new Message();
        try
        {
            okHttp Http=new okHttp();
            msg.obj=Http.Logout(username,password);
        } catch (Exception e)
        {
            msg.obj="下线失败，网络异常，请尝试网页版";
        }
        finally
        {
            handler.sendMessage(msg);
        }
        //处理异常信息与登录失败的信息
    }

}
