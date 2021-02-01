package me.wizos.loread.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import me.wizos.loread.App;
import me.wizos.loread.R;

/**
 * 关于时间操作的工具类
 * @author xdsjs, wizos
 * @date 2015/10/14
 */
public class TimeUtils {
    /**
     * 将 时间戳 转为指定的 格式
     * @param timestamp 时间戳（毫秒）
     * @param pattern 要转为的格式（例如 yyyy-MM-dd HH:mm:ss）
     * @return 格式化的时间
     */
    public static String format(long timestamp, String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
        Date date = new Date(timestamp);
        return dateFormat.format(date);
    }

    public static String readability(long timestamp) {
        // 如果“今天0点 <= 时间 < 今天24点59分”，则格式为“今天 HH:mm”
        // 如果“昨天0点 <= 时间 < 昨天24点59分”，则格式为“昨天 HH:mm”
        Calendar calendar = GregorianCalendar.getInstance(Locale.getDefault());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long toadyStartMillis = calendar.getTimeInMillis();

        calendar.add(Calendar.DATE, -1); // 明天的就是1，昨天是负1
        long yesterdayStartMillis = calendar.getTimeInMillis();

        calendar.add(Calendar.DATE, 2);
        long tomorrowStartMillis = calendar.getTimeInMillis();

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

    /**
     * 时间差
     */
    public static String getTimeFormatText(Date date) {
        long minute = 60 * 1000;// 1分钟
        long hour = 60 * minute;// 1小时
        long day = 24 * hour;// 1天
        long month = 31 * day;// 月
        long year = 12 * month;// 年

        if (date == null) {
            return null;
        }
        long diff = new Date().getTime() - date.getTime();
        long r;
        if (diff > year) {
            r = (diff / year);
            return r + "年前";
        }
        if (diff > month) {
            r = (diff / month);
            return r + "个月前";
        }
        if (diff > day) {
            r = (diff / day);
            return r + "天前";
        }
        if (diff > hour) {
            r = (diff / hour);
            return r + "小时前";
        }
        if (diff > minute) {
            r = (diff / minute);
            return r + "分钟前";
        }
        return "刚刚";
    }

    public static String getToday(String time)  {
        Calendar pre = Calendar.getInstance();
        Date predate = new Date(System.currentTimeMillis());
        pre.setTime(predate);

        Calendar cal = Calendar.getInstance();
        Date date = new Date(Long.parseLong(time) * 1000);
        cal.setTime(date);

        if (cal.get(Calendar.YEAR) == (pre.get(Calendar.YEAR))) {
            int diffDay = cal.get(Calendar.DAY_OF_YEAR) - pre.get(Calendar.DAY_OF_YEAR);
            return showDateDetail(diffDay, time);
        }
        return time;
    }

    /** * 用于显示时间 */
    public static final String TODAY = "今天";
    public static final String YESTERDAY = "昨天";
    /** * 将日期差显示为今天、明天或者星期 * @param diffDay * @param time * @return */
    private static String showDateDetail(int diffDay, String time){
        switch(diffDay){
            case -1:
                return YESTERDAY;
            case 0:
                return TODAY;
            default:
                return  getWeek(time);
        }
    }
    /** * 计算周几 */
    public static String getWeek(String data) {
        SimpleDateFormat sdr = new SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒");
        int i = Integer.parseInt(data);
        String times = sdr.format(new Date(i * 1000L));
        Date date;
        int mydate = 0;
        String week = "";
        try {
            date = sdr.parse(times);
            Calendar cd = Calendar.getInstance();
            cd.setTime(date);
            mydate = cd.get(Calendar.DAY_OF_WEEK);
            // 获取指定日期转换成星期几
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (mydate == 1) {
            week = "星期日";
        } else if (mydate == 2) {
            week = "星期一";
        } else if (mydate == 3) {
            week = "星期二";
        } else if (mydate == 4) {
            week = "星期三";
        } else if (mydate == 5) {
            week = "星期四";
        } else if (mydate == 6) {
            week = "星期五";
        } else if (mydate == 7) {
            week = "星期六";
        }
        return week;
    }

    public static StringBuilder getTime(int time) {
        if (time < 0) {
            time = 0;
        }
        int cache = time / 1000;
        int second = cache % 60;
        cache = cache / 60;
        int minute = cache % 60;
        int hour = cache / 60;
        StringBuilder timeStamp = new StringBuilder();
        if (hour > 0) {
            timeStamp.append(hour);
            timeStamp.append(":");
        }
        if (minute < 10) {
            timeStamp.append("0");
        }
        timeStamp.append(minute);
        timeStamp.append(":");

        if (second < 10) {
            timeStamp.append("0");
        }
        timeStamp.append(second);
        return timeStamp;
    }

    public static int getCurrentHour() {
        Calendar currentDate = new GregorianCalendar(Locale.getDefault());
        return currentDate.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * 获取当天 0 点的时间戳
     */
    public static long getTimestampOfDay() {
        Calendar currentDate = new GregorianCalendar();
        currentDate.set(Calendar.HOUR_OF_DAY, 0);
        currentDate.set(Calendar.MINUTE, 0);
        currentDate.set(Calendar.SECOND, 0);
        return currentDate.getTime().getTime();
    }

    /**
     * 获取当前周的第一天 0 点的时间戳
     */
    public static long getFirstDayTimestampOfWeek() {
        Calendar currentDate = new GregorianCalendar();
        currentDate.setFirstDayOfWeek(Calendar.SUNDAY);
        currentDate.set(Calendar.HOUR_OF_DAY, 0);
        currentDate.set(Calendar.MINUTE, 0);
        currentDate.set(Calendar.SECOND, 0);
        currentDate.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        return currentDate.getTime().getTime();
    }

    /**
     * 获取当前月的第一天的开始时间
     */
    public static long getFirstDayTimestampOfMonth() {
        Calendar currentDate = new GregorianCalendar();
        currentDate.setFirstDayOfWeek(Calendar.MONDAY);
        currentDate.set(Calendar.HOUR_OF_DAY, 0);
        currentDate.set(Calendar.MINUTE, 0);
        currentDate.set(Calendar.SECOND, 0);
        currentDate.set(Calendar.DAY_OF_MONTH, 0);
        return currentDate.getTime().getTime();
    }

    /**
     * 获取当前年的第一天的开始时间
     */
    public static long getFirstDayTimestampOfYear() {
        Calendar currentDate = new GregorianCalendar();
        currentDate.setFirstDayOfWeek(Calendar.MONDAY);
        currentDate.set(Calendar.HOUR_OF_DAY, 0);
        currentDate.set(Calendar.MINUTE, 0);
        currentDate.set(Calendar.SECOND, 0);
        currentDate.set(Calendar.DAY_OF_YEAR, 0);
        return currentDate.getTime().getTime();
    }
}