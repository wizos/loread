/*
 * Copyright 2015 Anthony Restaino
 * Copyright 2012-2016 Nathan Freitas

 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.wizos.loread.network.proxy;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Proxy;
import android.os.Build;
import android.os.Parcelable;
import android.util.ArrayMap;
import android.util.Log;

import androidx.annotation.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 类似：
 * https://stackoverflow.com/questions/4488338/webview-android-proxy
 * https://stackoverflow.com/questions/25272393/android-webview-set-proxy-programmatically-on-android-l/25485747#25485747
 * https://gist.github.com/madeye/2297083
 */
public class ProxyWebkit {

    private ProxyWebkit() {
        // this is a utility class with only static methods
        // 这是仅具有静态方法的实用程序类
    }

    /**
     * webview在loadUrl之前，调用如下函数，设置代理
     */
    public static boolean setHttpProxy(Context appContext, String host, int port){
        setSystemPropertiesHttp(host, Integer.toString(port));
        return setWebkitProxyLollipop(appContext, host, port);
    }

    public static boolean setSocksProxy(Context appContext, String host, int port){
        setSystemPropertiesSocks(host, Integer.toString(port));
        return setWebkitProxyLollipop(appContext, host, port);
    }

    /**
     * 在关闭、或者销毁webview之前，调用如下函数，取消代理
     * @param appContext
     * @return
     */
    public static boolean resetHttpProxy(Context appContext) {
        setSystemPropertiesHttp("", "");
        return resetLollipopProxy(appContext);
    }
    public static boolean resetSocksProxy(Context appContext) {
        setSystemPropertiesSocks("", "");
        return resetLollipopProxy(appContext);
    }

    private static void setSystemPropertiesHttp(String host, String port) {
        System.setProperty("proxyHost", host);
        System.setProperty("proxyPort", port);

        System.setProperty("http.proxyHost", host);
        System.setProperty("http.proxyPort", port);

        System.setProperty("https.proxyHost", host);
        System.setProperty("https.proxyPort", port);
    }

    private static void setSystemPropertiesSocks(String host, String port) {
        System.setProperty("socks.proxyHost", host);
        System.setProperty("socks.proxyPort", port);

        System.setProperty("socksProxyHost", host);
        System.setProperty("socksProxyPort", port);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean resetLollipopProxy(Context appContext) {
        return setWebkitProxyLollipop(appContext, null, 0);
    }

    // http://stackanswers.com/questions/25272393/android-webview-set-proxy-programmatically-on-android-l
    @TargetApi(Build.VERSION_CODES.LOLLIPOP) // for android.util.ArrayMap methods
    @SuppressWarnings("rawtypes")
    private static boolean setWebkitProxyLollipop(Context appContext, String host, int port) {
        try {
            Class applictionClass = Class.forName("android.app.Application");
            Field mLoadedApkField = applictionClass.getDeclaredField("mLoadedApk");
            mLoadedApkField.setAccessible(true);
            Object mloadedApk = mLoadedApkField.get(appContext);
            Class loadedApkClass = Class.forName("android.app.LoadedApk");
            Field mReceiversField = loadedApkClass.getDeclaredField("mReceivers");
            mReceiversField.setAccessible(true);
            ArrayMap receivers = (ArrayMap) mReceiversField.get(mloadedApk);
            for (Object receiverMap : receivers.values()) {
                for (Object receiver : ((ArrayMap) receiverMap).keySet()) {
                    Class clazz = receiver.getClass();
                    if (clazz.getName().contains("ProxyChangeListener")) {
                        Method onReceiveMethod = clazz.getDeclaredMethod("onReceive", Context.class, Intent.class);
                        Intent intent = new Intent(Proxy.PROXY_CHANGE_ACTION);
                        Object proxyInfo = null;
                        if (host != null) {
                            final String CLASS_NAME = "android.net.ProxyInfo";
                            Class cls = Class.forName(CLASS_NAME);
                            Method buildDirectProxyMethod = cls.getMethod("buildDirectProxy", String.class, Integer.TYPE);
                            proxyInfo = buildDirectProxyMethod.invoke(cls, host, port);
                        }
                        intent.putExtra("proxy", (Parcelable) proxyInfo);
                        onReceiveMethod.invoke(receiver, appContext, intent);
                    }
                }
            }
            return true;
        } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException | IllegalAccessException | InvocationTargetException e) {
            Log.d("ProxySettings", "Exception setting WebKit proxy on Lollipop through ProxyChangeListener: " + e.toString());
        }
        return false;
    }


    @Nullable
    public static Object getRequestQueue(Context ctx) throws Exception {
        Object ret = null;
        Class networkClass = Class.forName("android.webkit.Network");
        if (networkClass != null) {
            Object networkObj = invokeMethod(networkClass, "getInstance", new Object[]{
                    ctx
            }, Context.class);
            if (networkObj != null) {
                ret = getDeclaredField(networkObj, "mRequestQueue");
            }
        }
        return ret;
    }

    private static Object getDeclaredField(Object obj, String name)
            throws NoSuchFieldException, IllegalAccessException {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        Object out = f.get(obj);
        return out;
    }

    private static void setDeclaredField(Object obj, String name, Object value)
            throws NoSuchFieldException, IllegalAccessException {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(obj, value);
    }

    private static Object invokeMethod(Object object, String methodName, Object[] params, Class... types) throws Exception {
        Object out = null;
        Class c = object instanceof Class ? (Class) object : object.getClass();
        if (types != null) {
            Method method = c.getMethod(methodName, types);
            out = method.invoke(object, params);
        } else {
            Method method = c.getMethod(methodName);
            out = method.invoke(object);
        }
        return out;
    }
}
