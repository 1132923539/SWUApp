package com.christmas.swuwifi;

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

/*
 *开始的封装的请求，已经过时，等待，更新
 */
public class httpclient {
	// 0=GET;1=POST
	String visit(HttpInfo httpinfo, int type) {
		String SetCookie = "";
		String resultData = "";
		URL url = null;
		try {
			url = new URL(httpinfo.url);
		} catch (MalformedURLException e) {
		}
		if (url != null) {
			try {

				HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
				urlConn.setConnectTimeout(10000);
				urlConn.setReadTimeout(10000);
				urlConn.setDoOutput(true);
				urlConn.setDoInput(true);
				if (type == 1)
					urlConn.setRequestMethod("POST");
				else
					urlConn.setRequestMethod("GET");
				urlConn.setUseCaches(false);
				urlConn.setInstanceFollowRedirects(true);
				String strHeader = httpinfo.Header;
				//去掉头文件中无用干扰字符
				strHeader = strHeader.replace(" ", "");
				strHeader = strHeader.replace("\r", "");
				String[] tem = strHeader.split("\n");
				 for(int i=0;i<tem.length;i++)
				 {
					 if(tem[i].contains(":"))
					 {
						 String[] tem2=tem[i].split(":");
						 //因为要用:拆分，而Referer的网址的http中含有:，所以将:用@代替
						 if(tem2[1].contains("@"))
							 tem2[1]=tem2[1].replace("@", ":");
						 urlConn.setRequestProperty(tem2[0],tem2[1]);
					 }
					 
				 }
				if (!strHeader.contains("Referer:")) {
					strHeader = strHeader + "\r\n" + "Referer:" + httpinfo.url;
					urlConn.setRequestProperty("Referer", httpinfo.url);
				}
				if (!strHeader.contains("Accept:")) {
					strHeader = strHeader + "\r\n" + "Accept: */*";
					urlConn.setRequestProperty("Accept", "*/*");
				}
				if (!strHeader.contains("Accept-Language:")) {
					strHeader = strHeader + "\r\n" + "Accept-Language: zh-cn";
					urlConn.setRequestProperty("Accept-Language", "zh-cn");
				}
				if (!strHeader.contains("Content-Type:")) {
					strHeader = strHeader + "\r\n" + "Content-Type: application/x-www-form-urlencoded";
					urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				}
				if (!httpinfo.Cookie.isEmpty()) {
					strHeader = strHeader + "\r\n" + "Cookie: " + httpinfo.Cookie;
					urlConn.setRequestProperty("Cookie", httpinfo.Cookie);
				}
				if ((!strHeader.contains("Content-Length:")) && type == 1) {
					String tmp;
					tmp = String.valueOf(httpinfo.PostData.length());
					strHeader = strHeader + "\r\n" + "Content-Length: " + tmp;
					urlConn.setRequestProperty("Content-Length", tmp);
				}
				urlConn.connect();
				DataOutputStream out = new DataOutputStream(urlConn.getOutputStream());
				if (type == 1) {
					String content = httpinfo.PostData;
					out.writeBytes(content);
					out.flush();
					out.close();
				}
				InputStreamReader in = new InputStreamReader(urlConn.getInputStream(), "utf-8");
				BufferedReader buffer = new BufferedReader(in);
				String inputLine = null;
				while (((inputLine = buffer.readLine()) != null)) {
					resultData += inputLine;
				}
				in.close();
				if(urlConn.getHeaderField("Set-Cookie")!=null)
				{
					SetCookie = urlConn.getHeaderField("Set-Cookie");
					//将旧的cookie中的信息(新cookie没有的)加入到新的cookie中
					//httpinfo.Cookie = UpdateCookie(SetCookie,httpinfo.Cookie);
					httpinfo.Cookie = SetCookie;
				}
			} catch (MalformedURLException e) {
			} catch (ProtocolException e) {
			} catch (ConnectException e) {
			} catch (SocketTimeoutException e) {
			} catch (IOException e) {
			}
		}
		return resultData;

	}

	String Get(HttpInfo httpinfo) {
		return visit(httpinfo, 0);
	}

	String Post(HttpInfo httpinfo) {
		return visit(httpinfo, 1);

	}

	 static String UpdateCookie(String New, String Old) {
		 String text="";
		 New.replace(" ", "");
		 Old.replace(" ", "");
		 
		 if(Old.contains("="))
		 {
			 //将新cookie拆分
			 String[] temNew = New.split(";");
			 String cookieNew[][] = new String[temNew.length][2];
			 for(int i=0;i<temNew.length;i++)
			 {
				 String[] temNew2=temNew[i].split("=");
				 cookieNew[i][0]=temNew2[0];
				 cookieNew[i][1]=temNew2[1];
			 }
			 //将旧cookie拆分
			 String[] temOld = Old.split(";");
			 String cookieOld[][] = new String[temOld.length][2];
			 for(int i=0;i<temOld.length;i++)
			 {
				 String[] temOld2=temOld[i].split("=");
				 cookieOld[i][0]=temOld2[0];
				 cookieOld[i][1]=temOld2[1];
			 }
			 //将旧的cookie中的信息(新cookie没有的)加入到新的cookie中
			 for(int i=0;i<temOld.length;i++)
			 {
				 boolean exist =false;
				 for(int j=0;j<temNew.length;j++)
				 {
					 if(cookieOld[i][0].equals(cookieNew[j][0]))
					 {
						 exist=true;//存在 把新的换成旧的
						 New = New.replace(cookieNew[j][1], cookieOld[i][1]);
					 }
				 }
				 if(!exist)//不存在 用旧的
				 {
					 text+=cookieOld[i][0]+"="+cookieOld[i][1]+";";
				 }
			 }
			 text+=New;
			 return text;
		 }else{
			 //无旧的，直接返回新的
			 return New;
		 }
	 }
	 //获取莫一特定cookie的值
	 String getCertainCookie(String name,String cookie){
		 String[] tem = cookie.split(";");
		 for(int i=0;i<tem.length;i++)
		 {
			 String[] tem2=tem[i].split("=");
			 if(tem2[0].equals(name))
				 return tem2[1];
		 }
		 return "WRONG";
	 }
}
