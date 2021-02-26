package com.example.mymn_2020_12_212;

import android.app.Application;
import android.content.res.Resources;

import com.example.plugin_core.HookUtil;
import com.example.plugin_core.LoadUtil;
import com.example.plugin_core.ProxyActivity;

/**
 * author : quzongyang
 * e-mail : quzongyang@xiaohe.com
 * time   : 2021/02/23
 * desc   :
 * version: 1.0
 */


public class MyApplication extends Application {

    private Resources resources;
    @Override
    public void onCreate() {
        super.onCreate();
        HookUtil hookUtil = new HookUtil(this, ProxyActivity.class);
        try {
            hookUtil.hookStartActivity();
            hookUtil.hookLaunchActivity();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Resources getResources() {
        //如果插件中的资源对象加载到了，就返回这个插件中的资源对象   否则就返回宿主中的资源对象
        return resources == null?super.getResources():resources;
    }

    public void setResources(Resources resources) {
        this.resources = resources;
    }

}
