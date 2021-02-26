package com.example.mymn_2020_12_212;

import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.mymn_2020_12_212.proxy_test.InvocationHandlerImpl;
import com.example.mymn_2020_12_212.proxy_test.ProxyInterface;
import com.example.mymn_2020_12_212.proxy_test.ProxyTest;
import com.example.plugin_core.LoadUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class MainActivity extends AppCompatActivity {

    File apkLogin;
    File apkMerber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Log.e("MN-------->",getClassLoader().getClass().getName());
        apkLogin = new File(getCacheDir() + "/plugin_login.apk");
        apkMerber = new File(getCacheDir() + "/plugin_member.apk");
    }

    public void jumpLogin(View view) {
        loadPluginApk(getCacheDir() + "/plugin_login.apk");
    }

    public void jumpMember(View view) {
        loadPluginApk(getCacheDir() + "/plugin_member.apk");
    }

    public void loadPluginApk(String apkPath){
        //去加载member的插件
        LoadUtil loadUtil = new LoadUtil();
        loadUtil.loadClass(getApplicationContext(),apkPath);
        //用一个资源对象去管理member模块的资源
        Resources resources = loadUtil.loadPluginResource(getApplicationContext());
        MyApplication myApplication = (MyApplication) getApplication();
        myApplication.setResources(resources);
        //获取到插件中清单文件中的第一个Activity  也可以写全类名 如“com.example.login.LoginActivity
        ActivityInfo[] activities = loadUtil.getPackageInfo().activities;
        String activityName = activities[0].name;
        //跳转到插件中的Activity里面去
        jumpActivity(activityName);
    }


    public void jumpActivity(String activityName){
        try {
            //跳转Activity要做的事情
            // 1.AMS要检查目的地的Activity是否进行了清单注册
            // 2.AMS要通知ActivityThread来创建目的地的类 然后去启动生命周期
            Class<?> aClass = getClassLoader().loadClass(activityName);
            Intent intent = new Intent(this, aClass);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void proxyTest(){
        ProxyTest proxyTest = new ProxyTest();
        ProxyInterface proxyInterface = (ProxyInterface) Proxy.newProxyInstance(this.getClassLoader(),new Class[]{
                ProxyInterface.class
        },new InvocationHandlerImpl(proxyTest));
        proxyInterface.getLog("我在MainActivity中调用了getLog()");
    }

    public void getPluginClass(){
        try {
            Class<?> aClass = getClassLoader().loadClass("com.example.login.Test");
            Method getToast = aClass.getDeclaredMethod("getToast", Context.class);
            getToast.setAccessible(true);
            getToast.invoke(aClass.newInstance(),getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void downloadLogin(View view) {
        downloadPluginApk(view,"https://xiaohe-online.oss-cn-beijing.aliyuncs.com/Emulation/audios/homework/plugin_login.apk",apkLogin);
    }

    public void downloadMenber(View view) {
        downloadPluginApk(view,"https://xiaohe-online.oss-cn-beijing.aliyuncs.com/Emulation/audios/homework/plugin_member.apk",apkMerber);
    }

    public void downloadPluginApk(final View v, String apkUrl, final File apkFile) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(apkUrl)
               // .url("https://xiaohe-online.oss-cn-beijing.aliyuncs.com/Emulation/audios/homework/plugin.apk")
                .get()
                .build();
        client.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        v.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "出错啦", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            FileOutputStream fos = new FileOutputStream(apkFile);
                            fos.write(response.body().bytes());
                            fos.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        v.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "成功啦", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
    }


}
