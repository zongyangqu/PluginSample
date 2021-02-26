package com.example.mymn_2020_12_212;

import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import android.content.Context;
import android.content.Intent;
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

    File apk;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e("MN-------->",getClassLoader().getClass().getName());
        apk = new File(getCacheDir() + "/plugin.apk");
    }

    public void getPluginMethod(View view) {
        LoadUtil.loadClass(this);
        //proxyTest();
//        getPluginClass();
        jumpActivity();


    }

    public void jumpActivity(){
        try {
            //跳转Activity要做的事情
            // 1.AMS要检查目的地的Activity是否进行了清单注册
            // 2.AMS要通知ActivityThread来创建目的地的类 然后去启动生命周期
            Class<?> aClass = getClassLoader().loadClass("com.example.login.LoginActivity");
            startActivity(new Intent(this,aClass));
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

    public void download(final View v) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://xiaohe-online.oss-cn-beijing.aliyuncs.com/Emulation/audios/homework/plugin.apk")
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
                            FileOutputStream fos = new FileOutputStream(apk);
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
