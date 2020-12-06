package com.carlt.networklibs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.carlt.networklibs.annotation.NetWork;
import com.carlt.networklibs.utils.Constants;
import com.carlt.networklibs.utils.NetworkUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Description:
 * Company    : carlt
 * Author     : zhanglei
 * Date       : 2019/2/26 15:52
 */
public class NetworkStateReceiver extends BroadcastReceiver {
    private NetType                          netType;
    private Map<Object, List<MethodManager>> networkList;

    public NetworkStateReceiver() {
        netType = NetType.NONE;
        networkList = new HashMap<>();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            //            throw new RuntimeException("");
            Log.e(Constants.LOG_TAG, "intent or intent.getAction() is null");
            return;

        }
        if (intent.getAction().equalsIgnoreCase(Constants.ANDROID_NET_ACTION)) {
            //            Log.e(Constants.LOG_TAG, "网络发生改变");
            netType = NetworkUtils.getNetType();
//            if (NetworkUtils.isAvailable()) {
//                Log.e(Constants.LOG_TAG, "网络连接成功");
//            } else {
//                Log.e(Constants.LOG_TAG, "没有网络连接");
//            }
            post(netType);
        }
    }

    private void post(NetType netType) {
        Set<Object> set = networkList.keySet();
        for (final Object object : set) {
            List<MethodManager> methodManagers = networkList.get(object);
            if (methodManagers != null) {
                for (final MethodManager method : methodManagers) {
                    if (method.getType().isAssignableFrom(netType.getClass())) {
                        switch (method.getNetType()) {
                            case AUTO:
                                invoke(method, object, netType);
                                break;
                            case WIFI:
                                if (netType == NetType.WIFI || netType == NetType.NONE) {
                                    invoke(method, object, netType);
                                }
                                break;
                            case CMWAP:
                                if (netType == NetType.CMWAP || netType == NetType.NONE) {
                                    invoke(method, object, netType);
                                }
                                break;
                            case CMNET:
                                if (netType == NetType.CMNET || netType == NetType.NONE) {
                                    invoke(method, object, netType);
                                }
                                break;
                            case NONE:
                                break;
                            default:
                                break;
                        }
                    }

                }
            }
        }
    }

    private void invoke(MethodManager method, Object object, NetType netType) {
        Method me = method.getMethod();
        try {
            me.invoke(object, netType);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    public void registerObserver(Object register) {
        List<MethodManager> methodManagers = networkList.get(register);
        if (methodManagers == null) {
            methodManagers = findAnnotation(register);
            networkList.put(register, methodManagers);
        }
    }

    private List<MethodManager> findAnnotation(Object register) {
        List<MethodManager> methodManagers = new ArrayList<>();
        Class<?> aClass = register.getClass();
        Method[] methods = aClass.getMethods();
        for (Method method : methods) {
            NetWork annotation = method.getAnnotation(NetWork.class);
            if (annotation == null) {
                continue;
            }
            Type genericReturnType = method.getGenericReturnType();
            if (!genericReturnType.toString().equals("void")) {
                throw new RuntimeException(method.getName() + "Method return must be void");
            }
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length != 1) {
                throw new RuntimeException(method.getName() + "Method can only have one parameter");
            }

            MethodManager methodManager = new MethodManager(parameterTypes[0], annotation.netType(), method);
            methodManagers.add(methodManager);
        }
        return methodManagers;
    }

    public void unRegisterObserver(Object register) {
        if (!networkList.isEmpty()) {
            networkList.remove(register);
        }
    }

    public void unRegisterAllObserver() {
        if (!networkList.isEmpty()) {
            networkList.clear();
        }
        NetworkManager.getInstance().getApplication().unregisterReceiver(this);
        networkList = null;
    }
}
