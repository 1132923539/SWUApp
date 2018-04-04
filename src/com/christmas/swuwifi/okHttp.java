package com.christmas.swuwifi;

import android.util.Log;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hxiaohua on 2016/10/13.
 * 新版认证请求方式更新，封装的所有Http请求
 * 2016.12.16
 * 修改下线功能为本机下线，因为有感应下线功能
 */
public class okHttp {

    private String okURL;
    private String okCookie;

    //get请求
    public String getHtml(String burl,String code) throws Exception {
        URL url = new URL(burl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        InputStream is = conn.getInputStream();
        return StreamToString(is, code);
    }

    private String getCookieHtml(String burl,String Cookie,String code) throws Exception {
        URL url = new URL(burl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Cookie",Cookie);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        InputStream is = conn.getInputStream();
        return StreamToString(is, code);
    }
    //Stream转换成String
    private String StreamToString(InputStream is, String encoding) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, encoding));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line + "\n");
        }
        return sb.toString();
    }
    //post请求
    private  String PostHtml(String data,String burl,String code)throws Exception
    {
        byte[] Data = data.getBytes();
        URL url=new URL(burl);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        //设置连接与读取时间过期返回异常
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        OutputStream outStream = conn.getOutputStream();
        outStream.write(Data);
        outStream.flush();
        outStream.close();
        //Cookie=conn.getHeaderField("Set-Cookie");
        InputStream is=conn.getInputStream();
        return StreamToString(is,code);
    }
    //post携带Cookie
    private  String postCookieHtml(String data,String Cookie,String code)throws Exception
    {
        String burl="http://service2.swu.edu.cn/selfservice/module/userself/web/userself_ajax.jsf?methodName=indexBean.kickUserBySelfForAjax";
        byte[] Data = data.getBytes();
        URL url=new URL(burl);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        //设置连接与读取时间过期返回异常
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setRequestProperty("Cookie",Cookie);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        OutputStream outStream = conn.getOutputStream();
        outStream.write(Data);
        outStream.flush();
        outStream.close();
        //Cookie=conn.getHeaderField("Set-Cookie");
        InputStream is=conn.getInputStream();
        return StreamToString(is,code);
    }
    private  String GetCookie(String data,String burl,String code)throws Exception
    {
        byte[] Data = data.getBytes();
        URL url=new URL(burl);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        //设置连接与读取时间过期返回异常
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        OutputStream outStream = conn.getOutputStream();
        outStream.write(Data);
        outStream.flush();
        outStream.close();
        String Cookie=conn.getHeaderField("Set-Cookie");
       // InputStream is=conn.getInputStream();
        //return StreamToString(is,code);
        return Cookie;
    }
    //认证上网
    public String Login(String uid,String pwd)throws Exception
    {
        //二次更新，首先取得本机的信息，构造Cookie用途
        String re="";//存贮返回结果
        String html=getHtml("http://www.2345.com/?k811836659","gbk");//222.198.127.170/
        //未登录会跳转到js页面
        String p="jsp\\?(.+?)'</script>";
        Pattern reg = Pattern.compile(p);
        Matcher m=reg.matcher(html);
        String QueStr="";
        if(m.find())
        {
            //构造Cookie中的本地信息
            QueStr=m.group(1);
            //return html;
        }
        else {
            re="网络已连接，无需重复认证";
            html=getHtml("http://222.198.127.170/eportal/InterFace.do?method=getOnlineUserInfo","utf-8");
            String userid=okJson.getStr(html,"userId");
            userid=userid.trim();
            if(userid!=uid)
            {
                re="网络已认证，在线账号：" +userid;
            }
            return re;
        }
        //Query需要编码,utf-8
        QueStr=URLEncoder.encode(QueStr,"utf-8");
        String data = "userId="+uid+"&password="+pwd+"&service=%25E9%25BB%2598%25E8%25AE%25A4&queryString="+QueStr+"&operatorPwd=&operatorUserId=&validcode=";
        String url = "http://222.198.127.170/eportal/InterFace.do?method=login";
        html=PostHtml(data,url,"utf-8");
        return  html;
    }

    //智能下线功能的实现
    public String Logout(String uid,String pwd)throws Exception
    {
        String url = "http://222.198.127.170/eportal/InterFace.do?method=logout";
        String ht = getHtml(url,"utf-8");
        String re="";
        if (ht.contains("success"))
            re = "本机设备，下线成功";
        else if (ht.contains("fail"))
        {
            re= "本机没有连接";
            //本机没有连接，下线其他地方的设备登录
            re=SignOut(uid,pwd);//注释远端设备下线
            //如果需要启用远端设备，去掉上行的注释
            // 等待更新实现
        }
        else
           re= "下线失败，请尝试网页";
        return re;
    }

    //定时认证下线，只用本地下线
    public String Logout2(String uid,String pwd)throws Exception
    {
        String url = "http://222.198.127.170/eportal/InterFace.do?method=logout";
        String ht = getHtml(url,"utf-8");
        String re="";
        if (ht.contains("success"))
            re = "本机设备，下线成功";
        else if (ht.contains("fail"))
        {
            re= "本机没有连接";
            //本机没有连接，下线其他地方的设备登录
            //re=SignOut(uid,pwd);注释远端设备下线
            //如果需要启用远端设备，去掉上行的注释
            // 等待更新实现
        }
        else
            re= "下线失败，请尝试网页";
        return re;
    }

    //需要密码的下线功能
    public String SignOut(String uid,String pwd)throws Exception
    {
        String re="";
        String data ="name="+uid+"&password="+pwd;
        String url= "http://service2.swu.edu.cn/selfservice/module/scgroup/web/login_judge.jsf";
        String Cookie=GetCookie(data,url,"gbk");
        //取得第一次的Cookie，进行后续构造
        Cookie=String.format(Cookie+" rmbUser=true; userName=%s; passWord=%s; oldpassWord=%s;", uid,pwd,pwd);
        // //第一次打开，查看登录没有，是否有设备在线
        String listurl= "http://service2.swu.edu.cn/selfservice/module/webcontent/web/onlinedevice_list.jsf";
        String html=getCookieHtml(listurl,Cookie,"gbk");
        if(html.contains("您还未登录或会话过期"))
            return "账号或密码不正确";
        re="账号没有登录，无需下线";
        //登录管理界面成功，进行强制退出
        //第一步，找到局域网IP
        String p = "<span id=\"a1\">IP : (.+?)</span >";
        Pattern reg = Pattern.compile(p);
        Matcher m=reg.matcher(html);
        if(m.find())
        {
            //执行下线操作，其他设备
            url = "http://service2.swu.edu.cn/selfservice/module/userself/web/userself_ajax.jsf?methodName=indexBean.kickUserBySelfForAjax";
            data = "key=" + uid + ":" +m.group(1);
            html = postCookieHtml(data,Cookie, "gbk");
            if (html.contains("true:下线成功"))
                re="下线成功，可以重新认证";
            else
                re="其他设备，下线失败，请尝试网页版";
        }
        return re;
    }

    //校园余额查询功能
    public String GetYue(String uid,String pwd)throws Exception
    {
        String re="不是校园网用户";
        String data ="name="+uid+"&password="+pwd;
        String url= "http://service2.swu.edu.cn/selfservice/module/scgroup/web/login_judge.jsf";
        String Cookie=GetCookie(data,url,"gbk");
        //取得第一次的Cookie，进行后续构造
        Cookie=String.format(Cookie+" rmbUser=true; userName=%s; passWord=%s; oldpassWord=%s;", uid,pwd,pwd);
        // 打开查询余额
        String yueurl= "http://service2.swu.edu.cn/selfservice/module/userself/web/consume.jsf";
        String html=getCookieHtml(yueurl,Cookie,"gbk");
        if(html.contains("您还未登录或会话过期"))
            return "账号或密码不正确";
        //正则匹配结果，得到用户余额信息，仅限校园网，其他不支持
        String p = "<td class=\"contextDate yaheibold\" style=\"border: 0px;\">(.+?)</td>";
        Pattern reg = Pattern.compile(p);
        Matcher m=reg.matcher(html);
        if(m.find())
        {
            re=m.group(1);
        }
        return re;
    }

    //定时功能
    public String swuLogin(String uid,String pwd)throws Exception
    {
        String QueStr="wlanuserip=997306e3b47e50d51050b2920090177d&wlanacname=d3fd3003b8a92e5a0d9c924efc713c22&ssid=&nasip=f2267a27304a35841a4b2f3bbdcef5e6&snmpagentip=&mac=1542f2dfbe7d83c26ffa044074e1c568&t=wireless-v2&url=a8d66a9a8d98455be490d9712a6f57a2bcfc3e89908ba3b7b9899cf4dd6e61f4f0080ae1d9725d2a4cc5b1fc0fdbd04e93e40e9c1519e5f21bb11ac3459a6b90cff2d5b792339a42e7b06b3bf9e83a35d29929234f768cb7727104103b0deca4eca4f2e7dcf5c3668e67c744440016fea6da7264af01f41e1691db2745347d48e1efe61e2f9ae4515823db9fcccfb8103d211a4629247cae00c661652af5c2376bdc026bb79f90132df27ff748d21e9b266d3f0dbed3bd6ed4d29c0f3fda794cc02561c38c58c0d5fc6866c90f50975a1fc88326ea5c02f79e8ec1dea1efd21947c48f80e3ce65b437cc0c409e2524fad8f879970fd1ee5092753311cb8cf14f868fb9e4cdf6648682c044dcee2d118549e8a8fada66a3792e3e4e9fc34b8ea76fcaaa337d6bcfa7bf4f5af77b8f995b45857e307162fb9e8aa08af193d8e16d5b87cb573f32b588f6a38692266bbedd24ba292ff8189f8c67abbac5792f018885c1ff2d8432bc09fa80d84809bf7adb08c011ed463a6afe085851c7ae8fdbf041e16fab0453c920f81eb5d9131bb73f40acfe3500d460da4eb66977e7c50ce3ec53b62f93060dc8733826323fdcbdbf4a22ac57792c8421200fe5bca6ff61d245279d4b382ae3e05b427caf4f142d8387f5cc619f6addbe16c342ca09301d622b42110ba43c59a0aacdc64e5a5dd04dcae664eb7dc30ab489e5e31d307c8087536d3a0686a58c5a7938b7c3d34445560d9dfba19b694e3849cf2eebdd5cafba9d94d0bedbb3f7faddd12067c4d943a618afd5dee68e37cbd6b9d54647e2a5797ad8ffc0df4d638b1104a00756d5d9f6e76d41c6c39b8cd156ed33e01f8feed24d95c7bd294602a260c678df4088a7bc9649de7b79629f3be5048442a127a60fb950f3355ed9040dcfdb61108745e6cac107d8f11f961c926ceaab4bb781852e6571699308dd27f4c77a0a176978223ccb7399f67d8543aa4aace4faa6772f34b421adba0575b99cedd8dba804c143f621fb6fc6ecba98c25650c399fc8d5df74a6354e6998829c12d83c6512d7a6815b11d8e5187f14ea7ee24e61d7ab4c7bdff4bf1d6afb0e2f470367c6fa1ca4785db357bd482956c6904422b55fb596a56ed50ca53d3b96bf3a256bd0909aee54c1e11f4e6d1b851f677eb5f96f174ec2f33f1748c47cbcebdbe6c577f129cf8b17e55b44dc960414b&apmac=&nasid=d3fd3003b8a92e5a0d9c924efc713c22&vid=01ee82557990c7e7&port=b7937cfdf5aa03ef&nas";
        //Query需要编码,utf-8
        QueStr=URLEncoder.encode(QueStr,"utf-8");
        String data = "userId="+uid+"&password="+pwd+"&service=%25E9%25BB%2598%25E8%25AE%25A4&queryString="+QueStr+"&operatorPwd=&operatorUserId=&validcode=";
        String url = "http://222.198.127.170/eportal/InterFace.do?method=login";
        String html=PostHtml(data,url,"utf-8");
        return  html;
    }
}
