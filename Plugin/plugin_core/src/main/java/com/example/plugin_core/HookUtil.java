package com.example.plugin_core;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import androidx.annotation.NonNull;

/**
 * author : quzongyang
 * e-mail : quzongyang@xiaohe.com
 * time   : 2021/02/23
 * desc   : 让我们用Hook实现没有注册的Activity也能被启动
 *   1.首先要反射到AMS的实例，然后创建一个动态代理对象
 */


public class HookUtil {

    private Context context;

    //用来欺骗系统跳转的Activity
    private Class<? extends Activity> mProxyActivityClass;

    public static final String EXTRA_ORIGIN_INTENT = "EXTRA_ORIGIN_INTENT";

    public HookUtil(Context context, Class<? extends Activity> mProxyActivity) {
        this.context = context;
        this.mProxyActivityClass = mProxyActivity;
    }

    /**
     * Hook  AMS
     * 得到AMS的实例
     * 然后生成AMS的代理对象
     * 然后拦截它的startActivity方法
     */
    public void hookStartActivity() throws Exception {
        //首先  获取到ActivityManagerNative这个类的class对象
        Class<?> amnClass = Class.forName("android.app.ActivityManagerNative");
        //获取到ActivityManagerNative的gDefault的成员变量
        Field gDefault = amnClass.getDeclaredField("gDefault");
        gDefault.setAccessible(true);
        //获取到gDefault这个静态变量的值
        Object gDefaultValue = gDefault.get(null);

        //获取到Singleton的类对象
        Class<?> singletonClass = Class.forName("android.util.Singleton");
        //获取到的是mInstance的成员变量
        Field mInstance = singletonClass.getDeclaredField("mInstance");
        mInstance.setAccessible(true);
        //获取到AMS
        Object amsObject = mInstance.get(gDefaultValue);

        //创建AMS的代理对象
        //首先要获取到它的接口的class对象
        Class<?> IActivityManagerClass = Class.forName("android.app.IActivityManager");
        Object amsProxy = Proxy.newProxyInstance(HookUtil.class.getClassLoader(),
                new Class[]{IActivityManagerClass},new StartActivityInvocationHandler(amsObject));
        //通过反射  将代理对象替换原来的ams
        mInstance.set(gDefaultValue,amsProxy);
    }

    public class StartActivityInvocationHandler implements InvocationHandler{

        //持有要代理的对象的对象  AMS
        private Object ams;

        public StartActivityInvocationHandler(Object ams){
            this.ams = ams;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//            Log.e("MN---------->",method.getName());
            if(method.getName().equals("startActivity")){
                int position = 0;
                for(int i = 0 ; i<args.length ; i++){
                    if(args[i] instanceof Intent){
                        position = i;
                    }
                }
                //真实目标Intent
                Intent oldIntent = (Intent) args[position];
                //创建一个新的意图
                Intent newIntent = new Intent(context,mProxyActivityClass);
                //将旧的意图放入到新的意图中
                newIntent.putExtra(EXTRA_ORIGIN_INTENT,oldIntent);
                //将新意图放入参数里边
                args[position] = newIntent;
            }
            //调用了AMS的startActivity方法
            return method.invoke(ams,args);
        }
    }

    public void hookLaunchActivity() throws Exception {
        //首先获取到ActivityThread的对象
        Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        //获取到ActivityThread这个类的实例 sCurrentActivityThread
        Field sCurrentActivityThread = activityThreadClass.getDeclaredField("sCurrentActivityThread");
        sCurrentActivityThread.setAccessible(true);
        //获取到ActivityThread的实例  sCurrentActivityThread是静态的直接获取即可
        Object activityThreadValue = sCurrentActivityThread.get(null);

        //获取的mh （本质就是Handler） 的成员变量
        Field mHField = activityThreadClass.getDeclaredField("mH");
        mHField.setAccessible(true);
        //获取到mh在ActivityThread中的值
        Object mHValue = mHField.get(activityThreadValue);

        //获取到ActivityThread里边用来发送消息的Handler
        Class<?> handlerClass = Class.forName("android.os.Handler");
        //获取到Handler中用来处理消息的mCallback变量
        Field mCallBackField = handlerClass.getDeclaredField("mCallback");
        mCallBackField.setAccessible(true);
        //重新赋值
        mCallBackField.set(mHValue,new HandlerCallBack());
    }

    private class HandlerCallBack implements Handler.Callback{
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            //msg.what == 100  是startActivity方法的常量值
            if(msg.what == 100){
                handlerLaunchActivity(msg);
            }
            return false;
        }

        private void handlerLaunchActivity(Message message){
            try {
                //获取到ActivityClientRecord  这个对象里边记录着Intent数据
                Object r = message.obj;
                Field intentField = r.getClass().getDeclaredField("intent");
                intentField.setAccessible(true);
                //从r 实例中将intent这个变量的值取出来
                Intent newIntent = (Intent) intentField.get(r);
                //从这个newIntent把我们真正要跳转的Intent取出来
                Intent oldIntent = newIntent.getParcelableExtra(EXTRA_ORIGIN_INTENT);
                if(oldIntent != null){
                    //重新把旧意图设置到 r 变量中
                    intentField.set(r,oldIntent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
