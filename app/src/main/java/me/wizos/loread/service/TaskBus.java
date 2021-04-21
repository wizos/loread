/*
 * Copyright (c) 2021 wizos
 * 项目：loread
 * 邮箱：wizos@qq.com
 * 创建时间：2021-04-21 09:06:24
 */

package me.wizos.loread.service;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import androidx.annotation.NonNull;

import com.elvishew.xlog.XLog;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import me.wizos.loread.utils.PagingUtils;

public class TaskBus<T> {
    private int triggerSize = 100;
    private int triggerTime = 1_000; // 毫秒
    private Trigger<T> trigger;
    private Handler handler;
    private List<T> data;

    private TaskBus() {
    }

    public void start(){
        data = new ArrayList<>();
        HandlerThread handlerThread = new HandlerThread("LightTaskThread");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                if(msg.what != triggerTime){
                    return false; //返回true 不对msg进行进一步处理
                }
                trigger.execute(data);
                data.clear();
                return true;
            }
        });
        handler.sendEmptyMessageDelayed(triggerTime, triggerTime);
    }

    public void add1(List<T> subData){
        data.addAll(subData);
        if(data.size() >= triggerSize){
            PagingUtils.slice(data, triggerSize, new PagingUtils.PagingListener<T>() {
                @Override
                public void onPage(@NotNull List<T> childList) {
                    trigger.execute(childList);
                }
            });
            data.clear();
        }else {
            if(handler.hasMessages(triggerTime)){
                handler.removeMessages(triggerTime);
            }
            handler.sendEmptyMessageDelayed(triggerTime, triggerTime);
        }
    }

    public void add(List<T> subData){
        XLog.i("当前线程1：" + Thread.currentThread());
        handler.post(new Runnable() {
            @Override
            public void run() {
                add2(subData);
            }
        });
    }
    private synchronized void add2(List<T> subData){
        XLog.i("当前线程2：" + Thread.currentThread());
        data.addAll(subData);
        if(data.size() >= triggerSize){
            PagingUtils.slice(data, triggerSize, new PagingUtils.PagingListener<T>() {
                @Override
                public void onPage(@NotNull List<T> childList) {
                    trigger.execute(childList);
                }
            });
            data.clear();
        }else {
            if(handler.hasMessages(triggerTime)){
                handler.removeMessages(triggerTime);
            }
            handler.sendEmptyMessageDelayed(triggerTime, triggerTime);
        }
    }


    public static class Builder<T>{
        int triggerSize = 100;
        int triggerTime = 1_000;
        Trigger<T> trigger;
        public Builder<T> triggerSize(int triggerSize){
            this.triggerSize = triggerSize;
            return this;
        }
        public Builder<T> triggerTime(int triggerTime){
            this.triggerTime = triggerTime;
            return this;
        }
        public Builder<T> trigger(Trigger<T> trigger){
            this.trigger = trigger;
            return this;
        }
        public TaskBus<T> build(){
            TaskBus<T> taskBus = new TaskBus<T>();
            taskBus.triggerSize = triggerSize;
            taskBus.triggerTime = triggerTime;
            taskBus.trigger = trigger;
            return taskBus;
        }
    }

    public interface Trigger<T>{
        void execute(List<T> childList);
    }
}
