package com.example.xuyulin.myvideoproject.dns;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.xuyulin.myvideoproject.R;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 作者： xuyulin on 2018/5/17.
 * 邮箱： xuyulin@yixia.com
 * 描述：
 */

public class RealmToIpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Thread(new Runnable() {
            @Override
            public void run() {
//                String regex = "(http).{1,100}(.tv)";
//                Pattern mpattern = Pattern.compile(regex);
//                Matcher mmatcher = mpattern.matcher("http://txcdn.f01.xiaoka.tv/live/dfsdfgasdgfdsg.flv");
//                if (mmatcher.find()) {
//                    Log.e("xyl", "街区" + mmatcher.group());
//                }
                String domainname = "wscdn.fm01.xiaoka.tv";//输入要解析的域名
                //60.222.200.136
                //101.26.37.91
                //218.12.233.229
                //218.26.75.138
                //124.167.236.98
                //124.167.218.207
//                String domainname = "txcdn.f01.xiaoka.tv";//输入要解析的域名
                try {
                    Log.e("xyl", "总共ip个数：" + InetAddress.getAllByName(domainname).length);
                    InetAddress[] inetadd = InetAddress.getAllByName(domainname);
                    //遍历所有的ip并输出
                    for (int i = 0; i < inetadd.length; i++) {
                        Log.e("xyl", "第一步" + inetadd[i]);
                        Log.e("xyl", "第二步" + inetadd[i].getHostAddress());
                    }

                } catch (UnknownHostException e) {
                    Log.e("xyl", "获取网站" + domainname + "的IP地址失败！没有对应的IP！");
                }
            }
        }).start();
    }
}
