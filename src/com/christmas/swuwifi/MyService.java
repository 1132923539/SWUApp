package com.christmas.swuwifi;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;


public class MyService extends Service {
	
	private static final int TOAST = 0;
	private String toastText;
	private Thread mThread;
	
	private String userName = "";
	private String passWord = "";
	
	private boolean inLogin = false;
	private boolean isHasLogout =true;
	private boolean setAutologin = false;
	private boolean setAutoLogout= false;
	private boolean SetNotice=true;
	
	Notification notification;
	Intent notificationIntent;  
    PendingIntent pendingIntent;  
    
    WifiManager wifiManager;  
    WifiInfo wifiInfo;

    public void onCreate() {  
        super.onCreate();  
        Log.i("log_info", "Myservice onCreateCommand()");
        load();
        registerReceiver();
		Context context=getApplicationContext();
		SharedPreferences sp = context.getSharedPreferences("chris", Context.MODE_PRIVATE);
		SetNotice=sp.getBoolean("Notice",true);
        notification = new Notification(R.drawable.ic_launcher,  
        		null, System.currentTimeMillis());
        notificationIntent = new Intent(this, MainActivity.class);
        pendingIntent = PendingIntent.getActivity(this, 0,notificationIntent, 0);
        notification.setLatestEventInfo(this, "初始化…", "初始化…",
				pendingIntent);
        notification.flags |= notification.FLAG_ONGOING_EVENT; //将此通知放到通知栏的"Ongoing"即"正在运行"组中
        notification.flags |= notification.FLAG_NO_CLEAR; //表明在点击了通知栏中的"清除通知"后，此通知不清除
		if(SetNotice)
			startForeground(1, notification);
		//当通知栏开关开启时，启动通知栏
    }
	//注册广播
    private void registerReceiver(){
        IntentFilter filter = new IntentFilter();  
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");  
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");  
        filter.addAction("android.net.wifi.RSSI_CHANGED"); 
        filter.addAction("android.net.wifi.STATE_CHANGE");
		filter.addAction("com.christmas.swuwifi.CHANGELOGIN");
		filter.addAction("com.christmas.swuwifi.LOGIN");
		filter.addAction("com.christmas.swuwifi.LOGOUT");
		//添加定时任务的通知
		filter.addAction("hxh.fail");
		filter.addAction("hxh.sucess");
        registerReceiver(WifiActionReceiver, filter); 
    }
   //通过能否访问百度判断网络状态
    private boolean isLogin()
	{
		//判断网络状态需要修改
		try
		{
			okHttp myht=new okHttp();
			String html=myht.getHtml("http://www.baidu.com/","utf-8");
			if(html.contains("222.198.127.170"))
				return false;//如果跳转到认证页面说明，没有网络连接
			else
				return true;//正常访问百度，已经认证
		}
		catch (Exception exp)
		{
			return true;
		}
    }

    private BroadcastReceiver WifiActionReceiver = new BroadcastReceiver(){
		public void onReceive(Context context, Intent intent) {

			//新增通知栏开关设置
			if(!SetNotice)
				return;
			//主账号设置/更改的广播
			if(intent.getAction().equals("com.christmas.swuwifi.CHANGELOGIN")) {
				load();
				Log.i("log_info", "收到广播CHANGELOGIN");
				Log.i("log_info",userName+"---"+passWord);
			//程序发出的认证广播
			}else if(intent.getAction().equals("com.christmas.swuwifi.LOGIN"))
			{
				setNoti("当前状态：已认证","WIFI名称:"+wifiManager.getConnectionInfo().getSSID());
				isHasLogout=false;
			//程序发出的下线广播
			}else if(intent.getAction().equals("com.christmas.swuwifi.LOGOUT"))
			{
				setNoti("当前状态：未认证", "正在等待网络认证");
			}
			else if(intent.getAction().equals("hxh.sucess"))
			{
				SimpleDateFormat sDateFormat = new SimpleDateFormat("HH:mm");
				String date = sDateFormat.format(new java.util.Date());
				setNoti("10min定时任务成功","时间："+date);
			}
			else if(intent.getAction().equals("hxh.fail"))
			{
				SimpleDateFormat sDateFormat = new SimpleDateFormat("HH:mm");
				String date = sDateFormat.format(new java.util.Date());
				setNoti("10min Task Failed","Time："+date);
			}
			//Toast.makeText(MyService.this, "一个测试 ", Toast.LENGTH_SHORT).show();
			wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);

			//WIFI信号强度改变的广播
	        if(intent.getAction().equals(WifiManager.RSSI_CHANGED_ACTION))
			{
	        	wifiInfo = wifiManager.getConnectionInfo();
	        	Log.i("log_info", "信号强度" + wifiInfo.getRssi() + wifiInfo.getSSID());
			//如果信号强度大于-80 自动登录被勾选 则自动认证
	        	if(!inLogin && wifiInfo.getRssi()>-80 && isWifiConnected() && setAutologin==true)
	        	{
	        		inLogin=true;
	        		if(mThread == null) {
        				mThread = new Thread(runnable); //自动认证 
        				mThread.start();//线程启动
        			}else{
        				mThread = null;
						mThread = new Thread(runnable);//自动认证
						mThread.start();//线程启动  	
					}
				//如果信号强度小于等于-80 自动退出被勾选 则自动下线
	        	}else if(isHasLogout ==false&& !inLogin && wifiInfo.getRssi()<=-80 && isWifiConnected() && setAutoLogout==true)
	        	{
					Log.i("log_info", "信号强度" + wifiInfo.getRssi() + wifiInfo.getSSID());
					Log.i("log_info", "开始自动下线");
	        		inLogin=true;
	        		if(mThread == null) {
        				mThread = new Thread(runnable2);
        				mThread.start();//线程启动  
        				//Log.i("log_info", "自动下线  线程启动 ID:"+String.valueOf(mThread.getId()) );	
        			}else{
        				mThread = null;
						mThread = new Thread(runnable2);  
						mThread.start();//线程启动  
		    			//Log.i("log_info", "自动下线  线程启动2 ID:"+String.valueOf(mThread.getId()) );	
					}
	        	}
	        }else if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){//wifi连接上与否  
	            //Log.i("log_info","网络状态改变");
	            
	            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
	            //wifi断开连接，但已开启
	            if(info.getState().equals(NetworkInfo.State.DISCONNECTED)){  
	            	Log.i("log_info","wifi网络连接断开");
            		setNoti("等待连接WIFI", "WIFI已开启，但未连接");
            	//wifi已连接
	            }else if(info.getState().equals(NetworkInfo.State.CONNECTED)){
	            	
	            	wifiInfo = wifiManager.getConnectionInfo();
	                //获取当前wifi名称   
	                Log.i("log_info","连接到网络 " + wifiInfo.getSSID());
	                if(!wifiInfo.getSSID().equals(null))
	                {
						setNoti("当前状态：未认证", "正在等待认证");
	                }
	            }  
	              
	        }else if(intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)){//wifi打开与否  
	            int wifistate = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_DISABLED);  
				//wifi状态：关闭
	            if(wifistate == WifiManager.WIFI_STATE_DISABLED){  
	            	Log.i("log_info","系统关闭wifi");
	            	setNoti("WIFI已关闭", "WIFI已关闭");
	            }
				//wifi状态：开启
	            else if(wifistate == WifiManager.WIFI_STATE_ENABLED){  
	            	Log.i("log_info","系统开启wifi");
	            	setNoti("等待连接WIFI", "WIFI已开启，但未连接");
	            }  
	        }  
	    }
	};

	//设置通知栏的方法
	public void setNoti(String title,String text){
		notification.setLatestEventInfo(getApplicationContext(), title, text, pendingIntent);
		startForeground(1, notification);
	}
    
	private Handler mHandler = new Handler() {  
        public void handleMessage (Message msg) {//此方法在ui线程运行   
            switch(msg.what) {
            case TOAST:
            	Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
            	break;
            }
        }
    };
	//自动认证的线程
	Runnable runnable = new Runnable() {  
        public void run() {//run()在新的线程中运行  
        	if(isLogin())
        	{
        		Log.i("log_info", "isLogin=true");
        		setNoti("当前状态：已认证", "WIFI名称:" + wifiManager.getConnectionInfo().getSSID());
				isHasLogout=false;
        		inLogin=false;
        	}else{
        		Log.i("log_info", "isLogin=false");
        		setNoti("当前状态：正在自动认证","WIFI名称:"+wifiManager.getConnectionInfo().getSSID());
        		
        		String returnText="";
        		int trytime;
        		//*****************************************
        		//下线
        		trytime=0;

        		while(trytime<=5){
        			trytime++;
		        	//Toast.makeText(getApplicationContext(), "尝试第" + trytime + "次下线", Toast.LENGTH_SHORT).show();
        			if(trytime==1){
						Log.i("log_info", "尝试下线");
						toastText="尝试下线";
						//mHandler.obtainMessage(TOAST).sendToTarget();
					}else{
						Log.i("log_info", "尝试第"+trytime+"次下线");
						toastText="尝试第"+trytime+"次下线";
						//mHandler.obtainMessage(TOAST).sendToTarget();
					}
        			returnText = logout();
		        	if(!returnText.contains("失败"))
		        		break;
        		}
        		if(returnText.contains("失败"))
        		{
		        	//Toast.makeText(getApplicationContext(), "自动认证失败，网络不好", Toast.LENGTH_SHORT).show();
        			Log.i("log_info", "自动认证失败，网络不好");
        			toastText="自动认证失败，网络不好";
		            mHandler.obtainMessage(TOAST).sendToTarget();
        			setNoti("自动认证失败","网络不好");
        			inLogin=false;
        			return;
        		}else if(returnText.contains("成功"))
        		{
		        	//Toast.makeText(getApplicationContext(), "账号下线成功，开始连接", Toast.LENGTH_SHORT).show();
        			Log.i("log_info", "账号下线成功，开始连接");
        			toastText="账号下线成功，开始连接";
		            mHandler.obtainMessage(TOAST).sendToTarget();
        			returnText = login();
        		}else if(returnText.contains("有误"))
        		{
        			setNoti("自动认证失败","账号或密码有误");
		        	//Toast.makeText(getApplicationContext(), "自动认证失败,账号或密码有误", Toast.LENGTH_SHORT).show();
        			Log.i("log_info","自动认证失败,账号或密码有误");
        			toastText="自动认证失败,账号或密码有误";
		            mHandler.obtainMessage(TOAST).sendToTarget();
		            inLogin=false;
		            return;
        		}
        		
        		//******************************************
        		//认证
        		trytime=0;
	        	
        		while(trytime<=5){
        			trytime++;
		        	//Toast.makeText(getApplicationContext(), "尝试第"+trytime+"次认证", Toast.LENGTH_SHORT).show();
					if(trytime==1){
						Log.i("log_info", "尝试认证");
						toastText="尝试认证";
						//mHandler.obtainMessage(TOAST).sendToTarget();
					}else{
						Log.i("log_info", "尝试第"+trytime+"次认证");
						toastText="尝试第"+trytime+"次认证";
						//mHandler.obtainMessage(TOAST).sendToTarget();
					}
        			returnText = login();
		        	if(!returnText.contains("失败"))
		        		break;
        		}
        		if(returnText.contains("失败"))
        		{
		        	//Toast.makeText(getApplicationContext(), "自动认证失败，网络不好", Toast.LENGTH_SHORT).show();
        			Log.i("log_info", "自动认证失败，网络不好");
        			toastText="自动认证失败，网络不好";
		            mHandler.obtainMessage(TOAST).sendToTarget();
        			setNoti("自动认证失败","网络不好");
        			inLogin=false;
        			return;
        		}else if(returnText.contains("成功"))	
        		{
		        	//Toast.makeText(getApplicationContext(), "自动认证成功", Toast.LENGTH_SHORT).show();
        			Log.i("log_info", "自动认证成功");
        			toastText="自动认证成功";
		            mHandler.obtainMessage(TOAST).sendToTarget();
        			wifiInfo = wifiManager.getConnectionInfo();
		        	setNoti("当前状态：已认证","WIFI名称:"+wifiInfo.getSSID());
					isHasLogout=false;
        		}else if(returnText.contains("余额"))
        		{
        			setNoti("自动认证失败","账号余额不足");
		        	//Toast.makeText(getApplicationContext(), "自动认证失败,账号余额不足", Toast.LENGTH_SHORT).show();
        			Log.i("log_info", "自动认证失败,账号余额不足");
        			toastText= "自动认证失败,账号余额不足";
		            mHandler.obtainMessage(TOAST).sendToTarget();
        		}
        		inLogin=false;
        	}
            //mHandler.obtainMessage(ISLOGIN).sendToTarget();//成功，向ui线程发送MSG_SUCCESS标识   
            return; 
        }  
    };
	//自动下线线程
    Runnable runnable2 = new Runnable() {
        public void run() {

        	int trytime;
    		//*****************************************
    		//下线
    		trytime=0;
    		String returnText="";
    		while(trytime<=5){
    			trytime++;
	        	//Toast.makeText(getApplicationContext(), "尝试第" + trytime + "次下线", Toast.LENGTH_SHORT).show();
				if(trytime==1){
					Log.i("log_info", "尝试下线");
					toastText="尝试下线";
					mHandler.obtainMessage(TOAST).sendToTarget();
				}else{
					Log.i("log_info", "尝试第"+trytime+"次下线");
					toastText="尝试第"+trytime+"次下线";
					mHandler.obtainMessage(TOAST).sendToTarget();
				}
    			returnText = logout();
	        	if(!returnText.contains("失败"))
	        		break;
    		}
    		if(returnText.contains("失败"))
    		{
	        	//Toast.makeText(getApplicationContext(), "自动认证失败，网络不好", Toast.LENGTH_SHORT).show();
    			Log.i("log_info", "自动下线失败，网络不好");
    			toastText="自动认证失败，网络不好";
	            mHandler.obtainMessage(TOAST).sendToTarget();
    			setNoti("自动认证失败","网络不好");
    			inLogin=false;
    			return;
    		}else if(returnText.contains("成功"))
    		{
	        	//Toast.makeText(getApplicationContext(), "账号下线成功，开始连接", Toast.LENGTH_SHORT).show();
    			Log.i("log_info", "账号下线成功");
    			toastText="账号下线成功";
				setNoti("自动下线成功","等待信号变好后连接");
				isHasLogout = true;
	            mHandler.obtainMessage(TOAST).sendToTarget();
    		}else if(returnText.contains("有误"))
    		{
    			setNoti("自动下线失败","账号或密码有误");
	        	//Toast.makeText(getApplicationContext(), "自动认证失败,账号或密码有误", Toast.LENGTH_SHORT).show();
    			Log.i("log_info","自动下线失败,账号或密码有误");
    			toastText="自动下线失败,账号或密码有误";
	            mHandler.obtainMessage(TOAST).sendToTarget();
	            inLogin=false;
	            return;
    		}
    		inLogin=false;
    		return;
        }
    };

	//二次封装login 返回值为toast的提示信息
	public String login(){
		try{
			okHttp http = new okHttp();
			return http.Login(userName, passWord);
		}catch (Exception e){
			return "失败";
		}
	}

	public String logout(){
		try{
			okHttp http = new okHttp();
			String html=http.Logout(userName, passWord);

			if(html.contains("下线成功")||html.contains("没有登录"))
				return "成功";
			else
				return "失败";
		}catch (Exception e){
			return "失败";
		}
	}
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {  
        Log.i("log_info", "onStartCommand() executed");
        return super.onStartCommand(intent, flags, startId);
    }  
      
    @Override  
    public void onDestroy() {  
        super.onDestroy();  
        Log.i("log_info", "onDestroy() executed");  
    }  
  
    @Override  
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    private void load(){
    	Context ctx =getApplicationContext();
        SharedPreferences sp =ctx.getSharedPreferences("chris", MODE_PRIVATE);
        userName = sp.getString("username0" , "");
        passWord = sp.getString("password0", "");
		Log.i("log_info",userName+"---"+passWord);
		setAutologin = sp.getBoolean("auto",false);
		setAutoLogout  = sp.getBoolean("autoLogout",false);
	}

	public  boolean isWifiConnected()
    {
		Context context =getApplicationContext();
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if(wifiNetworkInfo.isConnected())
        {
            return true ;
        }
        return false ;
    }
}
