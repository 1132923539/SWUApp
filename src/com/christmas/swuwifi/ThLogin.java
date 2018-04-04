package com.christmas.swuwifi;

        import android.os.Handler;
        import android.os.Message;
/**
 * Created by hxiaohua on 2016/10/13.
 * 进行网页认证时候使用的线程类
 */
public class ThLogin extends Thread{

    private String username;
    private String password;
    private Handler handler;
    ThLogin(String username,String password,Handler handler)
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
            //登录进行网页认证
            okHttp Http=new okHttp();
            msg.obj=Http.Login(username,password);
        } catch (Exception e)
        {
            msg.obj="认证失败，网络异常，试试网页版";
        }
        finally
        {
            handler.sendMessage(msg);
        }
        //处理异常信息与登录失败的信息
    }

}
