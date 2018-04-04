package com.christmas.swuwifi;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

public class HelpActivity extends Activity {

    //帮助活动启动，设置txt
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.more_help);
        TextView t3 = (TextView) findViewById(R.id.helptext);
        String HelpStr=
                "Hello 西瓜wifi</font><br><br>"
                +"一款简洁的西南大学认证下线app<br>"
                +"·免费软件，欢迎分享<br>"
                +"·一键认证，智能下线<br>"
                +"·自动认证下线功能<br>"
                +"·同步支持windows平台<br>"
                + "·更多特性正在来袭<br>"

                        +"<br>自动、定时认证下线强势来袭~<br><br>"
                        +"1、更多设置，高级功能勾选开关。<br>"
                        +"2、给予开机自启和后台自启权限。<br>"
                        +"3、加入安全软件白名单，待机休眠白名单。<br>"

                + "<br>我们还有些话想说<br><br>"
                +"我们不想让你麻烦的网页认证，<br>"
                +"更不愿意让你麻烦的下线账号。<br>"
                        +"我们不想假装懂你，<br>"
                        +"只把你想要的放到你面前。<br>"
                +"我们希望你能享受本该拥有的轻快简洁的用户体验。<br>"

                        +"<br>当前版本V3.3定时内测<br>"
                        //测试修改，发布注意修改哈
                +"<br>关于·我们<br><br>"
                        +"·官方网站 http://ok121.cn<br>"
                        +"@雪夜圣诞 http://weibo.com/u/2820421454<br>"
                        +"@源头活水 http://weibo.com/hxh95<br><br>"
                        +"我们利用有限的业余时间设计了它，虽然它并不那么美好，但正努力前行。<br>"
                +"如果你喜欢我们的作品，可以捐赠来支持我们。<br>"
                +"所有的捐赠都将用来：提升我们的环境配置及积极性。"
                ;
        t3.setText(Html.fromHtml(HelpStr));
    }

    //结束帮助活动
    public void FinishHelp(View view)
    {
        finish();
    }
    //调用支付宝扫码实现转账
    public void Donate(View view)
    {
        String codeurl="https://qr.alipay.com/tsx09193ekwjxekkxsr7c74";
        //原先https://qr.alipay.com/apee174vwb3m01xs0d
        try{
            String url="alipayqr://platformapi/startapp?saId=10000007&qrcode="+codeurl;
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        }
        catch(Exception e)
        {
            Toast.makeText(getApplicationContext(), "打开支付宝失败，谢谢支持", Toast.LENGTH_SHORT).show();
        }
    }
    //打开网页，托管平台网页
    public void NewClick(View view)
    {
        try
        {
            String url="http://ok121.cn";
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        }
        catch(Exception e)
        {
            Toast.makeText(getApplicationContext(), "打开浏览器失败，请手动访问", Toast.LENGTH_SHORT).show();
        }
    }
}
