package com.example.plugin_core;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

import java.io.FileDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

/**
 * author : quzongyang
 * e-mail : quzongyang@xiaohe.com
 * time   : 2021/02/22
 * desc   : 加载插件？ 根据存储APK或aar包路径去加载插件
 * version: 1.0
 */


public class LoadUtil {

    private  static String apkPath;



    public static void loadClass(Context context){
        apkPath = context.getCacheDir() + "/plugin.apk";
        if(context == null){
            return;
        }

        try {
            //第一步   先获取到宿主的dexElments
            PathClassLoader classLoader = (PathClassLoader) context.getClassLoader();
            //获取到BaseDexClassLoader
            Class<?> baseDexClassLoaderClazz = Class.forName("dalvik.system.BaseDexClassLoader");
            //获取到它里边的成员变量pathList
            Field pathListField = baseDexClassLoaderClazz.getDeclaredField("pathList");
            pathListField.setAccessible(true);
            //获取到pathList在宿主类加载器中的值
            Object pathListValue = pathListField.get(classLoader);
            //获取到这个pathList中的dexElement
            Field dexElementField = pathListValue.getClass().getDeclaredField("dexElements");
            dexElementField.setAccessible(true);
            //获取到dexElements在当前宿主类加载器中的值
            Object dexELementsValue = dexElementField.get(pathListValue);

            //第二步 加载插件  然后去获取插件的类加载器中的dexElements
            //apkPath文件路径 、context.getCacheDir().getAbsolutePath() 解压缓存路径、 null so库 、context.getClassLoader()父加载器
            DexClassLoader dexClassLoader = new DexClassLoader(apkPath,
                    context.getCacheDir().getAbsolutePath(),null,context.getClassLoader());
            //获取到插件的pathList
            Object pluginPathListValue = pathListField.get(dexClassLoader);
            //获取到插件的dexElements
            Object pluginDexElementsValue = dexElementField.get(pluginPathListValue);

            //第三步   合并数组
            //获取到两个数组的长度
            int myLength = Array.getLength(dexELementsValue);
            int pluginLength = Array.getLength(pluginDexElementsValue);
            int newLength = myLength + pluginLength;
            //获取到数组的类型
            Class<?> componentType = dexELementsValue.getClass().getComponentType();
            //创建新数组
            Object newArray = Array.newInstance(componentType,newLength);
            System.arraycopy(dexELementsValue,0,newArray,0,myLength);
            System.arraycopy(pluginDexElementsValue,0,newArray,myLength,pluginLength);

            //将新数组    赋值给宿主的类加载器
            dexElementField.set(pathListValue,newArray);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建一个资源对象  让它能够管理到插件的资源的方法
     * @param context
     * @return
     */
    public static Resources loadPluginResource(Context context){
        apkPath = context.getCacheDir() + "/plugin.apk";
        Resources resources = null;
        try {
            AssetManager assetManager = AssetManager.class.newInstance();
            //通过反射获取到 addAssetPath
            Method addAssetPath = assetManager.getClass().getDeclaredMethod("addAssetPath",String.class);
            addAssetPath.setAccessible(true);
            //执行这个方法
            addAssetPath.invoke(assetManager,apkPath);
            resources = new Resources(assetManager,context.getResources().getDisplayMetrics(),context.getResources().getConfiguration());
            return resources;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
