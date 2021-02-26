package com.example.mymn_2020_12_212.proxy_test;

import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * author : quzongyang
 * e-mail : quzongyang@xiaohe.com
 * time   : 2021/02/23
 * desc   :
 * version: 1.0
 */


public class InvocationHandlerImpl implements InvocationHandler {

    //要代理的对象
    private ProxyInterface proxy;

    public InvocationHandlerImpl(ProxyInterface proxy){
        this.proxy = proxy;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        for(Object object : args){
            Log.e("Object",object.toString());
        }
        //通过动态代理改变原来要打印的信息
        String message = "这是通过动态代理改变之后的打印信息";
        args[0] = message;
        Object invoke = method.invoke(proxy,args);
        return invoke;
    }
}
