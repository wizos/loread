package me.wizos.loread.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.elvishew.xlog.XLog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Objects;

import me.wizos.loread.App;
import me.wizos.loread.R;


public class TimeHandler extends BroadcastReceiver {
    private static TimeHandler instance;
    private TimeHandler(Context context){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        intentFilter.addAction(Intent.ACTION_DATE_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED);//设置了系统时间
        context.registerReceiver(this, intentFilter);
        initMillis();
    }

    public static TimeHandler init(Context context){
        if (instance == null) { // 双重锁定，只有在 withDB 还没被初始化的时候才会进入到下一行，然后加上同步锁
            synchronized (TimeHandler.class) { // 同步锁，避免多线程时可能 new 出两个实例的情况
                if (instance == null) {
                    instance = new TimeHandler(context);
                }
            }
        }
        return instance;
    }

    public static TimeHandler i(){
        if(instance == null){
            throw new RuntimeException("请现在app中初始化");
        }
        return instance;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        switch (Objects.requireNonNull(intent.getAction())) {
            case Intent.ACTION_DATE_CHANGED:
                //设置了系统时间
                XLog.d("1天过去了");
                initMillis();
                break;
            case Intent.ACTION_TIME_CHANGED:
                //设置了系统时间
                XLog.d("设置了系统时间");
                initMillis();
                break;
            case Intent.ACTION_TIMEZONE_CHANGED:
                //设置了系统时区的action
                XLog.d("设置了系统时区");
                initMillis();
                break;
        }
    }

    long yesterdayStartMillis, toadyStartMillis, tomorrowStartMillis;
    public void initMillis(){
        // 如果“今天0点 <= 时间 < 今天24点59分”，则格式为“今天 HH:mm”
        // 如果“昨天0点 <= 时间 < 昨天24点59分”，则格式为“昨天 HH:mm”
        Calendar calendar = GregorianCalendar.getInstance(Locale.getDefault());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        toadyStartMillis = calendar.getTimeInMillis();

        calendar.add(Calendar.DATE, -1); // 明天的就是1，昨天是负1
        yesterdayStartMillis = calendar.getTimeInMillis();

        calendar.add(Calendar.DATE, 2);
        tomorrowStartMillis = calendar.getTimeInMillis();
    }

    public String readability(long timestamp) {
        Date date = new Date(timestamp);
        String displayTime;
        if(timestamp < yesterdayStartMillis){
            displayTime = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(date);
        }else if(timestamp < toadyStartMillis){
            displayTime = App.i().getString(R.string.yesterday_time, new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date));
        }else if(timestamp < tomorrowStartMillis){
            displayTime = App.i().getString(R.string.today_time, new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date));
        }else {
            displayTime = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(date);
        }
        return displayTime;
    }
}
