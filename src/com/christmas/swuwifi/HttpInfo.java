package com.christmas.swuwifi;

public class HttpInfo {
	String url="";//链接
	String Header="";//协议头
	String PostData="";//POST专用，提交信息（实际上Cookies也会加入到这里）
	String Cookie="";//提交Cookies,本参数传递变量时会自动回传返回的Cookies
}
