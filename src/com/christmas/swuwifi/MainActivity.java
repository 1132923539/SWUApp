package com.christmas.swuwifi;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity implements OnClickListener {

    private Button bt1;
    private Button bt2;
    private Button bt3;
    private Button bt4;
    private Button bt5;
    private EditText et1;
    private EditText et2;
    private CheckBox auto;
    private CheckBox auto2;

    private View mMoreView;
    private ImageView mMoreImage;
    private View mMoreMenuView;
    private View textView_View;
    private TextView textView;
    private boolean mShowMenu = true;

    private ImageView iv;
    private RelativeLayout rl;
    private PopupWindow popupWindow;
    private ListView lv;
    private List<String> list_un = new ArrayList<String>();
    private List<String> list_pw = new ArrayList<String>();
    private List<String> list_queryFee = new ArrayList<String>();
    private MyAdapter adapter;
    private boolean list_add;

    private String returnText;
    private Thread mThread;

    private static final int TOAST = 0;
    private static final int SAVE_LOAD = 1;
    private static final int GONE = 2;

    WifiManager wifiManager;
    WifiInfo wifiInfo;

    queryFee queryfee = new queryFee();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initListView();// 初始化ListView
        wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        //显示通知栏
        //if (auto.isChecked()) {
            if (!isServiceWork(this, "MyService")) {
                Intent startIntent = new Intent(MainActivity.this, MyService.class);
                startService(startIntent);
            }
        //}
    }

    public boolean isServiceWork(Context mContext, String serviceName) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningServiceInfo> myList = myAM.getRunningServices(40);
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

    @Override
    protected void onPause() {
        super.onPause();
        save();
    }

    @Override
    protected void onStop() {
        super.onStop();
        save();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        save();
    }

    @Override
    protected void onResume() {
        super.onResume();
        list_un.clear();
        list_pw.clear();
        list_queryFee.clear();
        load_start();
        adapter.notifyDataSetChanged();
    }

    //初始化view
    public void initView() {
        mMoreView = findViewById(R.id.more);
        mMoreMenuView = findViewById(R.id.moremenu);
        //textView_View = findViewById(R.id.textView_View);
        //textView = (TextView) findViewById(R.id.textView);
        mMoreImage = (ImageView) findViewById(R.id.more_image);
        mMoreView.setOnClickListener(this);
        bt1 = (Button) findViewById(R.id.button1);
        bt1.setOnClickListener(this);
        bt2 = (Button) findViewById(R.id.button2);
        bt2.setOnClickListener(this);
        bt3 = (Button) findViewById(R.id.button3);
        bt3.setOnClickListener(this);
        bt4 = (Button) findViewById(R.id.button4);
        bt4.setOnClickListener(this);
        bt5 = (Button) findViewById(R.id.button5);
        bt5.setOnClickListener(this);

        rl = (RelativeLayout) findViewById(R.id.rl);
        et1 = (EditText) findViewById(R.id.et1);
        et2 = (EditText) findViewById(R.id.et2);
        auto2 = (CheckBox) findViewById(R.id.auto2);
        auto2.setOnClickListener(new CheckBox.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (auto2.isChecked()) {
                    Toast.makeText(MainActivity.this, "部分教学楼wifi不稳定，该功能可能失效", Toast.LENGTH_SHORT).show();
                    save_load();
                    Intent i = new Intent("com.christmas.swuwifi.CHANGELOGIN");
                    sendBroadcast(i);
                } else {
                    save_load();
                    Intent i = new Intent("com.christmas.swuwifi.CHANGELOGIN");
                    sendBroadcast(i);
                }
            }

        });
        auto = (CheckBox) findViewById(R.id.auto);
        auto.setOnClickListener(new CheckBox.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (auto.isChecked()) {
                    if (et1.getText().toString().length() != 0 && et2.getText().toString().length() != 0) {
                        save_load();
                        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                        alertDialog.show();
                        Window window = alertDialog.getWindow();
                        window.setContentView(R.layout.dialog2);
                        Intent i = new Intent("com.christmas.swuwifi.CHANGELOGIN");
						sendBroadcast(i);
                    } else {
                        auto.setChecked(false);
                        Toast.makeText(MainActivity.this, "请填写账号密码", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    save_load();
                    Intent i = new Intent("com.christmas.swuwifi.CHANGELOGIN");
                    sendBroadcast(i);
                }
            }

        });
        iv = (ImageView) findViewById(R.id.iv);
        iv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null == popupWindow || !popupWindow.isShowing()) {
                    popupWindow = new PopupWindow(MainActivity.this);
                    popupWindow.setContentView(lv);
                    // PopupWindow必须设置高度和宽度
                    popupWindow.setTouchable(true);
                    popupWindow.setFocusable(true);

                    //防止虚拟软键盘被弹出菜单遮住
                    popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                    popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.list_bg));
                    popupWindow.setOutsideTouchable(true);
                    popupWindow.setTouchInterceptor(new View.OnTouchListener() {
                        public boolean onTouch(View v, MotionEvent event) {
                            if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                                popupWindow.dismiss();
                                return true;
                            }
                            return false;
                        }
                    });
                    popupWindow.setWidth(rl.getWidth()); // 设置宽度
                    popupWindow.setHeight(LayoutParams.WRAP_CONTENT); // 设置popWin 高度
                    popupWindow.showAsDropDown(rl, 0, 0);
                    //Log.e("log_info", "click1");
                } else if (null != popupWindow && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                    // Log.e("log_info", "click2");
                }
            }
        });
    }

    /**
     * 初始化ListView
     */
    private void initListView() {
        list_un.clear();
        list_pw.clear();
        list_queryFee.clear();
        load();
        lv = new ListView(this);
        if (adapter == null) {
            adapter = new MyAdapter();
            lv.setAdapter(adapter);
        } else {

            adapter.notifyDataSetChanged();
        }
        //lv.setBackgroundResource(R.drawable.login_input);
        lv.setDividerHeight(0);
        lv.setVerticalScrollBarEnabled(false);
    }

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return list_un.size();
        }

        @Override
        public Object getItem(int position) {
            return list_un.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view;
            ViewHolder holder;

            if (convertView == null) {
                //Log.i("log_info","here1");
                view = View.inflate(MainActivity.this, R.layout.list_item, null);
                holder = new ViewHolder();
                holder.iv = (ImageView) view.findViewById(R.id.iv_listitem_delete);
                holder.tv = (TextView) view.findViewById(R.id.tv_listitem_content);
                holder.ivsetMain = (ImageView) view.findViewById(R.id.iv_listitem_setMain);
                //Log.i("log_info", "list个数" + list_un.size());
                if ((position != 0 || list_add) && !(list_un.size() == 1)) {
                    //Log.i("log_info","position:"+position);
                    list_add = false;
                } else {
                    //Log.i("log_info","first");
                   // holder.iv2 = (ImageView) view.findViewById(R.id.iv_listitem_index);
                   // holder.iv2.setVisibility(View.VISIBLE);
                    holder.ivsetMain.setVisibility(View.GONE);
                    holder.iv.setVisibility(View.INVISIBLE);
                }
                view.setTag(holder);
            } else {
                //Log.i("log_info","使用上次");
                view = convertView;
                holder = (ViewHolder) view.getTag();
            }
            /*总共调用三次，每次都要保证正确初始化*/
            Log.i("log_info", position + ":内容" + list_un.get(position));
            if (!list_un.get(position).equals(list_un.get(0))) {
                Log.i("log_info", "" + list_un.get(position));
            }
            //Log.i("log_info","size:"+list_un.size());
            //Log.i("log_info","可见："+holder.ivsetMain.getVisibility());
            //if(holder.ivsetMain.getVisibility()==View.VISIBLE){
            //Log.i("log_info", "count:"+parent.getChildCount());
            if (!list_un.get(position).equals(list_un.get(0))) {
                holder.iv.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 从List中移除
                        //Log.i("log_info","now click:"+position);
                        if (et1.getText().toString().equals(list_un.get(position))) {
                            et1.setText("");
                            et2.setText("");
                        }
                        list_un.remove(position);
                        list_pw.remove(position);
                        list_queryFee.remove(position);
                        adapter.notifyDataSetChanged();
                    }
                });

                holder.ivsetMain.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 换成第一个
                        Log.i("log_info", "now click:" + position);
                        String tem1, tem2, tem3;
                        tem1 = list_un.get(0);
                        tem2 = list_pw.get(0);
                        tem3 = list_queryFee.get(0);
                        list_un.set(0, list_un.get(position));
                        list_pw.set(0, list_pw.get(position));
                        list_un.set(position, tem1);
                        list_pw.set(position, tem2);
                        list_queryFee.set(0, list_queryFee.get(position));
                        list_queryFee.set(position, tem3);
                        save_load_start();
                        adapter.notifyDataSetChanged();
                        Intent i = new Intent("com.christmas.swuwifi.CHANGELOGIN");
                        sendBroadcast(i);
                        popupWindow.dismiss();
                    }
                });

            }

            holder.tv.setText(list_un.get(position));
            holder.tv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("log_info", "now click:" + position);
                    et1.setText(list_un.get(position));
                    et2.setText(list_pw.get(position));
                    popupWindow.dismiss();
                }
            });
            return view;
        }

    }

    private class ViewHolder {
        private TextView tv;
        private ImageView iv;
        private ImageView iv2;
        private ImageView ivsetMain;
    }

    /**
     * 初始化ListView END*END*END*END*END*END*END*END*END*END*END*END*
     */
    public void save_load() {
        save();//注意这要只存储下面是否打钩
        list_un.clear();
        list_pw.clear();
        list_queryFee.clear();
        load();
        adapter.notifyDataSetChanged();
    }

    public void save_load_start() {
        save();//注意这要只存储下面是否打钩
        list_un.clear();
        list_pw.clear();
        list_queryFee.clear();
        load_start();
        adapter.notifyDataSetChanged();
    }

    //底部菜单收缩判断
    public void showMoreView(boolean bShow) {
        if (bShow) {//关闭动作
            adapter.notifyDataSetChanged();
            mMoreMenuView.setVisibility(View.GONE);
            mMoreImage.setImageResource(R.drawable.login_more_up);
            mShowMenu = true;
        } else {//打开动作
            mMoreMenuView.setVisibility(View.VISIBLE);
            mMoreImage.setImageResource(R.drawable.login_more);
            mShowMenu = false;
        }
    }

    public void netType() {
        //////////////////判断上网方式//////////////////////
        if (!list_un.isEmpty()) {//不是第一次使用，账号列表不为空
            int i = list_un.indexOf(et1.getText().toString());
            String[] query = list_queryFee.get(i).split(";");
            if (query[0].equals("0")) {//未查询过
                final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).create();
                dialog.show();
                dialog.getWindow().setContentView(R.layout.dialog_1);
                Button rb1 = (Button) dialog.getWindow().findViewById(R.id.radioButton1);
                rb1.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Context ctx = MainActivity.this;
                        SharedPreferences sp = ctx.getSharedPreferences("chris", MODE_PRIVATE);
                        Editor editor = sp.edit();
                        int i = list_un.indexOf(et1.getText().toString());
                        list_queryFee.set(i, "2;2;2");
                        editor.putString("queryFee" + i, "2;2;2");
                        editor.commit();
                        dialog.dismiss();
                        RenZhen();
                    }
                });
                Button rb2 = (Button) dialog.getWindow().findViewById(R.id.radioButton2);
                rb2.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Context ctx = MainActivity.this;
                        SharedPreferences sp = ctx.getSharedPreferences("chris", MODE_PRIVATE);
                        Editor editor = sp.edit();
                        int i = list_un.indexOf(et1.getText().toString());
                        list_queryFee.set(i, "1;1;1");
                        editor.putString("queryFee" + i, "1;1;1");
                        editor.commit();
                        dialog.dismiss();
                        RenZhen();
                    }
                });
            }
        }
    }

    //点击事件，响应用户的入口
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.more:  //底部按钮
                showMoreView(!mShowMenu);
                break;
            case R.id.button1:
                //点击了认证按钮
                ButtonLogin();
                break;
            case R.id.button2:
                //下线功能开发
                ButtonLogout();
                break;
            case R.id.button3:
                //跳转到帮助页面
                Intent intent1 = new Intent(this, HelpActivity.class);
                startActivity(intent1);
                break;
            case R.id.button4:
                //查询余额
                if (TextUtils.isEmpty(et1.getText())) {
                    Toast.makeText(MainActivity.this, "请输入账号~", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(et2.getText())) {
                    Toast.makeText(MainActivity.this, "请输入密码~", Toast.LENGTH_SHORT).show();
                    return;
                }
                save_load();
                if (!list_un.isEmpty()) {//不是第一次使用，账号列表不为空
                    int i = list_un.indexOf(et1.getText().toString());
                    String[] query = list_queryFee.get(i).split(";");
                    System.out.println(et1.getText().toString() + "->" + list_queryFee.get(i));
                    if (query[0].equals("0")) {//未查询过
                        final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).create();
                        dialog.show();
                        dialog.getWindow().setContentView(R.layout.dialog_1);
                        Button rb1 = (Button) dialog.getWindow().findViewById(R.id.radioButton1);
                        rb1.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //Toast.makeText(MainActivity.this, "radioButton1", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                showDialog2();
                            }
                        });
                        Button rb2 = (Button) dialog.getWindow().findViewById(R.id.radioButton2);
                        rb2.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //Toast.makeText(MainActivity.this, "radioButton2", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                Context ctx = MainActivity.this;
                                SharedPreferences sp = ctx.getSharedPreferences("chris", MODE_PRIVATE);
                                Editor editor = sp.edit();
                                int i = list_un.indexOf(et1.getText().toString());
                                list_queryFee.set(i, "1;1;1");
                                editor.putString("queryFee" + i, "1;1;1");
                                editor.commit();

                                if (mThread == null) {
                                    mThread = new Thread(runnableHourYE);
                                    mThread.start();//线程启动
                                    Log.e(null, "线程启动 ID:" + String.valueOf(mThread.getId()));
                                } else {
                                    mThread = null;
                                    mThread = new Thread(runnableHourYE);
                                    mThread.start();//线程启动
                                    Log.e(null, "线程启动2 ID:" + String.valueOf(mThread.getId()));
                                }
                            }
                        });
                    } else if (query[0].equals("1")) {//按小时
                        Toast.makeText(MainActivity.this, "正在查询，请稍后...", Toast.LENGTH_SHORT).show();
                        if (mThread == null) {
                            mThread = new Thread(runnableHourYE);
                            mThread.start();//线程启动
                            Log.e(null, "线程启动 ID:" + String.valueOf(mThread.getId()));
                        } else {
                            mThread = null;
                            mThread = new Thread(runnableHourYE);
                            mThread.start();//线程启动
                            Log.e(null, "线程启动2 ID:" + String.valueOf(mThread.getId()));
                        }
                    } else if (query[0].equals("2")) {//包月宽带
                        if (!query[1].equals("2")) {
                            Toast.makeText(MainActivity.this, "正在查询，请稍后...手机号:" + query[1] + " 密码:" + query[2], Toast.LENGTH_SHORT).show();
                            queryfee.setPhoneNumber(query[1]);
                            queryfee.setPassWd(query[2]);
                            if (mThread == null) {
                                mThread = new Thread(runnableYE);
                                mThread.start();//线程启动
                                Log.e(null, "线程启动 ID:" + String.valueOf(mThread.getId()));
                            } else {
                                mThread = null;
                                mThread = new Thread(runnableYE);
                                mThread.start();//线程启动
                                Log.e(null, "线程启动2 ID:" + String.valueOf(mThread.getId()));
                            }
                        } else {
                            showDialog2();
                        }
                    }
                } else {
                    Toast.makeText(MainActivity.this, "请输入账号", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.button5:
                //高级功能限制
                SuperHelp();
                break;
            default:
                break;
        }
    }

    //判断用户是否wifi在线并连接，是就返回true
    public boolean IsWifi(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if(wifiNetworkInfo.isConnected())
        {
            return true ;
        }
        return false ;
    }
    //点击高级功能
    public void SuperHelp() {
        Intent intentset = new Intent(this, SetActivity.class);
        startActivity(intentset);

    }

    //点击了登录认证按钮，进行操作
    public void ButtonLogin()
    {
        if(TextUtils.isEmpty(et1.getText()))
        {
            Toast.makeText(MainActivity.this, "请您输入账号~", Toast.LENGTH_SHORT).show();
            return;
        }else if(TextUtils.isEmpty(et2.getText()))
        {
            Toast.makeText(MainActivity.this, "请您输入密码~", Toast.LENGTH_SHORT).show();
            return;
        }
        save_load();
        if (IsWifi(this))//判断wifi是否连接
        {
            if (!list_un.isEmpty())
            {//不是第一次使用，账号列表不为空
                int i = list_un.indexOf(et1.getText().toString());
                String[] query = list_queryFee.get(i).split(";");
                if (query[0].equals("0"))//0;0;0选择上网方式
                    netType();
                else if (query[0].equals("1"))//1;1;1添加计时
                {
                    RenZhen();
                } else if (query[0].equals("2"))//2;xx;xxx
                    RenZhen();
                }
            }
            else Toast.makeText(MainActivity.this, "请先连接无线网", Toast.LENGTH_SHORT).show();

    }
    //认证函数
    public void RenZhen()
    {
        String username=et1.getText().toString();
        String password=et2.getText().toString();
        final Dialog dia=CustomProgress.show(this, "认证中", true, null);
        Handler handler= new Handler()
        {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.obj.toString().isEmpty()) {
                    dia.dismiss();
                    return;
                }
                dia.dismiss();
                //更新主线程UI，解析json字符串，主要是两个
                if (msg.obj.toString().contains("success")) {
                    Intent i = new Intent("com.christmas.swuwifi.LOGIN");
                    sendBroadcast(i);
                    Toast.makeText(MainActivity.this, "认证成功，网络已连接", Toast.LENGTH_SHORT).show();
                }
                else if(msg.obj.toString().contains("fail")){
                    try {//失败，则提示具体原因
                        JSONObject jsonObject = new JSONObject(msg.obj.toString());
                        String message = jsonObject.getString("message");
                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else Toast.makeText(MainActivity.this,msg.obj.toString(), Toast.LENGTH_SHORT).show();
                //计数 到10次弹出捐赠
                Context ctx = MainActivity.this;
                SharedPreferences sp = ctx.getSharedPreferences("chris", MODE_PRIVATE);
                Editor editor = sp.edit();
                int login_time = sp.getInt("login_time", 0);
                //Log.i("log_info","login_time->"+login_time);
                if (login_time >= 10) {
                    donate();
                    editor.putInt("login_time", -1);
                    editor.commit();
                } else if (login_time >= 0) {
                    login_time += 1;
                    editor.putInt("login_time", login_time);
                    editor.commit();
                }
            }
        };
        new ThLogin(username,password,handler).start();
        changeMainAccount();
    }
    //弹出支付宝捐赠界面，加入取消功能
    public void donate() {
        final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).create();
        dialog.show();
        dialog.getWindow().setContentView(R.layout.donate);
        Button rb1 = (Button) dialog.getWindow().findViewById(R.id.radioButton1);
        rb1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    String url="alipayqr://platformapi/startapp?saId=10000007&qrcode=https://qr.alipay.com/apee174vwb3m01xs0d";
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                }
                catch(Exception e)
                {
                    Toast.makeText(getApplicationContext(), "调用支付宝接口失败，谢谢你的支持", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });
        Button rb2 = (Button) dialog.getWindow().findViewById(R.id.button_cancel);
        rb2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }
    //认证成功后将该账号设置为主账号
    public void changeMainAccount(){
        int position = list_un.indexOf(et1.getText().toString());
        Log.i("log_info", "你要登录的账号原位置为:" + position);
        String tem1, tem2, tem3;
        tem1 = list_un.get(0);
        tem2 = list_pw.get(0);
        tem3 = list_queryFee.get(0);
        list_un.set(0, list_un.get(position));
        list_pw.set(0, list_pw.get(position));
        list_un.set(position, tem1);
        list_pw.set(position, tem2);
        list_queryFee.set(0, list_queryFee.get(position));
        list_queryFee.set(position, tem3);
        save_load_start();
        adapter.notifyDataSetChanged();
        Intent i = new Intent("com.christmas.swuwifi.CHANGELOGIN");
        sendBroadcast(i);
    }
    //账号退出函数
    public void ButtonLogout(){
        if (TextUtils.isEmpty(et1.getText())) {
            Toast.makeText(MainActivity.this, "请输入账号~", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(et2.getText())) {
            Toast.makeText(MainActivity.this, "请输入密码~", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!IsWifi(this))//判断wifi是否连接
        {
            //没有连接WIFI，到此止步
            Toast.makeText(MainActivity.this, "请先连接无线网", Toast.LENGTH_SHORT).show();
            return;
        }
        save_load();
        String userName = et1.getText().toString();
        String passWord = et2.getText().toString();
        final Dialog dia=CustomProgress.show(this, "下线中~", true, null);
        Handler handler= new Handler()
        {
            public void handleMessage(Message msg)
            {
                super.handleMessage(msg);
                if(msg.obj.toString().isEmpty())
                {
                    dia.dismiss();
                    return;
                }
                dia.dismiss();
                //更新主线程UI
                Toast.makeText(MainActivity.this,msg.obj.toString(),Toast.LENGTH_SHORT).show();
                if(msg.obj.toString().contains("下线成功")||msg.obj.toString().contains("没有登录"))
                {
                    Intent i = new Intent("com.christmas.swuwifi.LOGOUT");
                    sendBroadcast(i);
                }
            }
        };
        new ThLogout(userName,passWord,handler).start();
    }

    public void showDialog2() {
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        View textEntryView = inflater.inflate(R.layout.dialog_2, null);
        final AlertDialog mDialog = new AlertDialog.Builder(MainActivity.this).create();
        mDialog.setView(((Activity) MainActivity.this).getLayoutInflater().inflate(R.layout.dialog_2, null));
        mDialog.show();
        mDialog.getWindow().setContentView(textEntryView);

        final EditText et1 = (EditText) mDialog.getWindow().findViewById(R.id.dialog2_et1);
        final EditText et2 = (EditText) mDialog.getWindow().findViewById(R.id.dialog2_et2);
        final Button bt1 = (Button) mDialog.getWindow().findViewById(R.id.dialog2_bt1);
        final Button bt2 = (Button) mDialog.getWindow().findViewById(R.id.dialog2_bt2);

        bt1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!et1.getText().toString().equals("")) {
                    queryfee.setPhoneNumber(et1.getText().toString());
                    et1.setVisibility(View.GONE);
                    bt1.setVisibility(View.GONE);

                    et2.setVisibility(View.VISIBLE);
                    bt2.setVisibility(View.VISIBLE);

                    bt2.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (!et1.getText().toString().equals("") && !et2.getText().toString().equals("")) {
                                queryfee.setPhoneNumber(et1.getText().toString());
                                queryfee.setDynamicSmsCode(et2.getText().toString());
                                if (mThread == null) {
                                    mThread = new Thread(runnableChangPassWd);
                                    mThread.start();//线程启动
                                    Log.e(null, "线程启动 ID:" + String.valueOf(mThread.getId()));
                                } else {
                                    mThread = null;
                                    mThread = new Thread(runnableChangPassWd);
                                    mThread.start();//线程启动
                                    Log.e(null, "线程启动2 ID:" + String.valueOf(mThread.getId()));
                                }
                                Toast.makeText(MainActivity.this, "正在查询中，请稍后...", Toast.LENGTH_SHORT).show();
                                mDialog.dismiss();
                            } else {
                                Toast.makeText(MainActivity.this, "请输入短信中的验证码", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    Toast.makeText(MainActivity.this, "发送验证码至:" + et1.getText().toString(), Toast.LENGTH_SHORT).show();
                    if (mThread == null) {
                        mThread = new Thread(runnableYzm);
                        mThread.start();//线程启动
                        Log.e(null, "线程启动 ID:" + String.valueOf(mThread.getId()));
                    } else {
                        mThread = null;
                        mThread = new Thread(runnableYzm);
                        mThread.start();//线程启动
                        Log.e(null, "线程启动2 ID:" + String.valueOf(mThread.getId()));
                    }
                } else {
                    Toast.makeText(MainActivity.this, "请输入绑定的电信手机号", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    Runnable runnableYzm = new Runnable() {
        public void run() {//run()在新的线程中运行
            if (queryfee.getYZM())
                returnText = "验证码发送成功，请接收";
            else {
                returnText = "验证码发送失败，请检查网络";
            }
            mHandler.obtainMessage(TOAST).sendToTarget();//发送TOAST);
            return;
        }
    };

    Runnable runnableHourYE = new Runnable() {
        public void run() {//run()在新的线程中运行
            returnText = queryfee.gethourYE(et1.getText().toString(), et2.getText().toString());
            if (!returnText.contains("内网"))
                returnText = "查询结果：" + returnText;
            //显示查询结果，您的余额为
            mHandler.obtainMessage(TOAST).sendToTarget();//发送TOAST);
            return;
        }
    };

    Runnable runnableChangPassWd = new Runnable() {
        public void run() {//run()在新的线程中运行
            queryfee.updatePassWD();
            if (queryfee.changPassWd().contains("成功")) {
                returnText = "新密码为:" + queryfee.getPassWd();
                mHandler.obtainMessage(TOAST).sendToTarget();
                queryfee.setPassWd(queryfee.getPassWd());

                Context ctx = MainActivity.this;
                SharedPreferences sp = ctx.getSharedPreferences("chris", MODE_PRIVATE);
                Editor editor = sp.edit();
                int i = list_un.indexOf(et1.getText().toString());
                list_queryFee.set(i, "2;" + queryfee.getPhoneNumber() + ";" + queryfee.getPassWd());
                editor.putString("queryFee" + i, "2;" + queryfee.getPhoneNumber() + ";" + queryfee.getPassWd());
                editor.commit();

                if (mThread == null) {
                    mThread = new Thread(runnableYE);
                    mThread.start();//线程启动
                    Log.e(null, "线程启动 ID:" + String.valueOf(mThread.getId()));
                } else {
                    mThread = null;
                    mThread = new Thread(runnableYE);
                    mThread.start();//线程启动
                    Log.e(null, "线程启动2 ID:" + String.valueOf(mThread.getId()));
                }
            }
            return;
        }
    };

    Runnable runnableYE = new Runnable() {
        public void run() {//run()在新的线程中运行
            String yue = queryfee.getYE();
            if (!yue.equals("WRONG"))
                returnText = "您的余额为：" + yue;
            else
                returnText = "网络不好，查询失败";
            mHandler.obtainMessage(TOAST).sendToTarget();//发送TOAST);
            return;
        }
    };




    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {//此方法在ui线程运行
            switch (msg.what) {
                case TOAST:
                    Toast.makeText(MainActivity.this, returnText, Toast.LENGTH_SHORT).show();
                    returnText = null;
                    break;
                case SAVE_LOAD:
                    save_load();
                    break;
                case GONE:
                    break;
            }
        }
    };

    private void save() {
        Context ctx = MainActivity.this;
        SharedPreferences sp = ctx.getSharedPreferences("chris", MODE_PRIVATE);
        Editor editor = sp.edit();
        if (et1.getText().toString().length() != 0 && et2.getText().toString().length() != 0) {
            boolean flag = false;
            for (int i = 0; i < list_un.size(); i++) {
                String un, pw;
                un = et1.getText().toString();
                pw = et2.getText().toString();
                if (list_un.get(i).equals(un)) {//若编辑框账号在list中存在，则为真
                    list_pw.set(i, pw);//存在的话更新密码
                    flag = true;
                }
            }
            if (flag == false) {//若编辑框账号在list中不存在，则将其加入list
                list_un.add(et1.getText().toString());
                list_pw.add(et2.getText().toString());
                list_queryFee.add("0;0;0");
                list_add = true;
            }

            editor.putInt("size", list_un.size());
            for (int i = 0; i < list_un.size(); i++) {
                editor.putString("username" + i, list_un.get(i));
                editor.putString("password" + i, list_pw.get(i));
                editor.putString("queryFee" + i, list_queryFee.get(i));
            }

        }
        if (auto.isChecked())
            editor.putBoolean("auto", true);
        else
            editor.putBoolean("auto", false);
        if (auto2.isChecked())
            editor.putBoolean("autoLogout", true);
        else
            editor.putBoolean("autoLogout", false);
        editor.commit();
    }

    private void load_start() {
        Context ctx = MainActivity.this;
        SharedPreferences sp = ctx.getSharedPreferences("chris", MODE_PRIVATE);
        int size;
        size = sp.getInt("size", 0);
        if (size != 0) {
            et1.setText(sp.getString("username0", ""));
            et2.setText(sp.getString("password0", ""));
            for (int i = 0; i < size; i++) {
                list_un.add(sp.getString("username" + i, ""));
                list_pw.add(sp.getString("password" + i, ""));
                list_queryFee.add(sp.getString("queryFee" + i, "0;0;0"));
            }
        }

        if (sp.getBoolean("auto", false))
            auto.setChecked(true);
        else
            auto.setChecked(false);

        if (sp.getBoolean("autoLogout", false))
            auto2.setChecked(true);
        else
            auto2.setChecked(false);
    }

    private void load() {
        Context ctx = MainActivity.this;
        SharedPreferences sp = ctx.getSharedPreferences("chris", MODE_PRIVATE);
        int size;
        size = sp.getInt("size", 0);
        if (size != 0) {

            for (int i = 0; i < size; i++) {
                list_un.add(sp.getString("username" + i, ""));
                list_pw.add(sp.getString("password" + i, ""));
                list_queryFee.add(sp.getString("queryFee" + i, "0;0;0"));
            }
        }

        if (sp.getBoolean("auto", false))
            auto.setChecked(true);
        else
            auto.setChecked(false);

        if (sp.getBoolean("autoLogout", false))
            auto2.setChecked(true);
        else
            auto2.setChecked(false);
    }

}
