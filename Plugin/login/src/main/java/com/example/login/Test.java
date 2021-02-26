package com.example.login;

import android.content.Context;
import android.widget.Toast;

/**
 * author : quzongyang
 * e-mail : quzongyang@xiaohe.com
 * time   : 2021/02/22
 * desc   :
 * version: 1.0
 */


public class Test {

    public void getToast(Context context){
        Toast.makeText(context,"我是插件中的类方法，我被加载并调用了",Toast.LENGTH_SHORT).show();
    }
}
