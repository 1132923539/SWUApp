package com.christmas.swuwifi;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;

/**
 * Created by chris on 2016/6/4.
 */
public class queryFee {
    httpclient w = new httpclient();// http请求发送对象
    HttpInfo httpinfo = new HttpInfo();// http请求相关信息

    String text = "";
    String phoneNumber = "";
    String passWd = (int) ((Math.random() * 9 + 1) * 100000) + "";
    String dynamicSmsCode = "";

    public void setPhoneNumber(String phoneNumber){
        this.phoneNumber=phoneNumber;
    }

    public String getPhoneNumber(){
        return phoneNumber;
    }

    public void updatePassWD(){
        passWd = (int) ((Math.random() * 9 + 1) * 100000) + "";
    }

    public String getPassWd(){
        return passWd;
    }

    public void setPassWd(String passWd){
        this.passWd= passWd;
    }

    public void setDynamicSmsCode(String dynamicSmsCode){
        this.dynamicSmsCode=dynamicSmsCode;
    }

    public boolean getYZM() {
        //获取验证码
        httpinfo.url = "http://www.189.cn/dqmh/uamGetCheckCodeAction.do?method=getSMSDynamicCode&phoneNumber=" + phoneNumber + "&ikl=1";
        httpinfo.PostData = "method=getSMSDynamicCode&phoneNumber=" + phoneNumber + "&ikl=1";
        text="";
        text = w.Post(httpinfo);
        if(text.contains("验证码获取成功"))
            return true;
        else
            return false;
    }

    public String changPassWd() {
        //修改密码
        httpinfo.url = "http://www.189.cn/dqmh/managePwdAction.do?method=resetPassWord";
        httpinfo.PostData = "itermIsOk=0&usedType=1&PasswordType=00&phoneNumber=" + phoneNumber + "&dynamicSmsCode=" + dynamicSmsCode + "&newpassword=" + passWd + "&passwordQD=" + passWd;
        text="";
        text = w.Post(httpinfo);
        System.out.println(text);

        if (text.contains("密码重置成功"))
            return("修改密码成功");
        else if (text.contains("验证码"))
           return("验证码错误");
        else
            return("查询失败");
    }

    public String getYE()
    {
        String yue="";
        // 连接地址（通过阅读html源代码获得，即为登陆表单提交的URL）
        String surl = "http://wapcq.189.cn/otherLogin.shtml";

        /**
         * 首先要和URL下的URLConnection对话。 URLConnection可以很容易的从URL得到。比如： // Using
         * java.net.URL and //java.net.URLConnection
         */
        URL url = null;
        try {
            url = new URL(surl);
        } catch (MalformedURLException e) {
            yue="WRONG";
            // TODO Auto-generated catch block
            e.printStackTrace();
            return yue;
        }
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
        } catch (IOException e) {
            yue="WRONG";
            // TODO Auto-generated catch block
            e.printStackTrace();
            return yue;
        }

        /**
         * 然后把连接设为输出模式。URLConnection通常作为输入来使用，比如下载一个Web页。
         * 通过把URLConnection设为输出，你可以把数据向你个Web页传送。下面是如何做：
         */
        connection.setDoOutput(true);  //打开输出，向服务器输出参数（POST方式、字节）（写参数之前应先将参数的字节长度设置到配置"Content-Length"<字节长度>）
        connection.setDoInput(true);//打开输入，从服务器读取返回数据
        try {
            connection.setRequestMethod("POST");
        } catch (ProtocolException e) {
            yue="WRONG";
            // TODO Auto-generated catch block
            e.printStackTrace();
            return yue;
        } //设置登录模式为POST（字符串大写）
        connection.setInstanceFollowRedirects(false);
        try {
            connection.connect();
        } catch (IOException e) {
            yue="WRONG";
            // TODO Auto-generated catch block
            e.printStackTrace();
            return yue;
        }
        /**
         * 最后，为了得到OutputStream，简单起见，把它约束在Writer并且放入POST信息中，例如： ...
         */
        OutputStreamWriter out;
        try {
            out = new OutputStreamWriter(connection
                    .getOutputStream(), "utf-8");

            //其中的loginName和loginPassword也是阅读html代码得知的，即为表单中对应的参数名称
            out.write("j_code="+phoneNumber+"&flag=2&acceptType=3&j_password="+passWd+"&type=null&channel=null"); // post的关键所在！
            //remember to clean up
            out.flush();
            out.close();
        } catch (UnsupportedEncodingException e) {
            yue="WRONG";
            // TODO Auto-generated catch block
            e.printStackTrace();
            return yue;
        } catch (IOException e) {
            yue="WRONG";
            // TODO Auto-generated catch block
            e.printStackTrace();
            return yue;
        }
        //取得cookie，相当于记录了身份，供下次访问时使用
        //HttpURLConnection.getHeaderFields()).get("Set-Cookie")用于迭代读取Cookie，为以后使用
        //HttpURLConnection.getHeaderField("Set-Cookie")也可用于读取Cookie，但不一定能读取完全
        String cookieVal = connection.getHeaderField("Set-Cookie");  //格式:JSESSIONID=541884418E77E7F07363CCEE91D4FF7E; Path=/
        connection.disconnect();

        //登陆成功后，即可访问其他URL了。
        String s = "http://wapcq.189.cn/v2/index.shtml";
        //重新打开一个连接
        try {
            url = new URL(s);
        } catch (MalformedURLException e) {
            yue="WRONG";
            // TODO Auto-generated catch block
            e.printStackTrace();
            return yue;
        }
        HttpURLConnection resumeConnection = null;
        try {
            resumeConnection = (HttpURLConnection) url
                    .openConnection();
            resumeConnection.setConnectTimeout(10000);
            resumeConnection.setReadTimeout(10000);
        } catch (IOException e) {
            yue="WRONG";
            // TODO Auto-generated catch block
            e.printStackTrace();
            return yue;
        }
        if (cookieVal != null) {
            System.out.println(cookieVal);
            //发送cookie信息上去，以表明自己的身份，否则会被认为没有权限
            resumeConnection.setRequestProperty("Cookie", cookieVal);//设置登陆配置
        }
        try {
            resumeConnection.connect();
        } catch (IOException e) {
            yue="WRONG";
            // TODO Auto-generated catch block
            e.printStackTrace();
            return yue;
        }
        InputStream urlStream = null;
        try {
            urlStream = resumeConnection.getInputStream();
        } catch (IOException e) {
            yue="WRONG";
            // TODO Auto-generated catch block
            e.printStackTrace();
            return yue;
        }
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(urlStream));
        String ss = null;
        String total = "";
        try {
            while ((ss = bufferedReader.readLine()) != null) {
                //System.err.println(ss);
                total += ss;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            yue="WRONG";
            return yue;
        }
        System.out.println(total);
        try {
            bufferedReader.close();
        } catch (IOException e) {

            // TODO Auto-generated catch block
            e.printStackTrace();
            yue="WRONG";
            return yue;
        }

        if(!(total.equals("")) && total.contains("</span> 元</td>"))
        {
            String tem = "您的话费余额：<span class='word12_ora'>";
            int start = total.indexOf(tem)+tem.length();
            int end = total.indexOf("</span> 元</td>", start);
            yue=total.substring(start, end);
        }else{
            yue="WRONG";
        }
        return yue;
    }

    //查询校园网余额功能
    public String gethourYE(String userName,String passWord)
    {
        String re="网络异常，查询失败";
        try
        {
            okHttp http=new okHttp();
            re= http.GetYue(userName,passWord);
        }
        catch (Exception exp)
        {
            re="网络连接失败！";
        }
        finally
        {
            return re;
        }
    }
}
