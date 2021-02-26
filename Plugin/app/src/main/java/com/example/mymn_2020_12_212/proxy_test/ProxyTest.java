package com.example.mymn_2020_12_212.proxy_test;

import android.util.Log;

/**
 * author : quzongyang
 * e-mail : quzongyang@xiaohe.com
 * time   : 2021/02/23
 * desc   :
 * version: 1.0
 */


public class ProxyTest implements ProxyInterface {

    @Override
    public void getLog(String message) {
        Log.e("MN------------",message);
    }
}
