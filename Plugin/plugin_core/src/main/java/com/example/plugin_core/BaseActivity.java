package com.example.plugin_core;

import android.content.res.Resources;

import androidx.appcompat.app.AppCompatActivity;

/**
 * author : quzongyang
 * e-mail : quzongyang@xiaohe.com
 * time   : 2021/02/25
 * desc   :
 * version: 1.0
 */


public class BaseActivity extends AppCompatActivity {
    @Override
    public Resources getResources() {
        if(getApplication()!=null && getApplication().getResources()!=null){
            return getApplication().getResources();
        }
        return super.getResources();
    }
}
