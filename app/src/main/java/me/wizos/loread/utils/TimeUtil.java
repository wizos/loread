package me.wizos.loread.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * 关于时间操作的工具类
 * Created by xdsjs on 2015/10/14.
 */
public class TimeUtil {
//    public static String formatReadability(long timestamp)  {
//        // 如果给定的时间小于昨天凌晨，则直接用标准的 yyyy-MM-dd HH:mm 格式
//    }

    /**
     * 时间差
     *
     * @param date
     * @return
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
        long r = 0;
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


    /** * 用于显示时间 */
    public static final String TODAY = "今天";
    public static final String YESTERDAY = "昨天";

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
        Date date = null;
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

    public static int getCurrentHour() {
        Calendar currentDate = new GregorianCalendar(Locale.CHINA);
        return currentDate.get(Calendar.HOUR_OF_DAY);
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

    /**
     * 获取当天的开始时间
     */
    public static long getTimeOfDay() {
        Calendar currentDate = new GregorianCalendar();
        currentDate.set(Calendar.HOUR_OF_DAY, 0);
        currentDate.set(Calendar.MINUTE, 0);
        currentDate.set(Calendar.SECOND, 0);
        return currentDate.getTime().getTime();
    }

    /**
     * 获取当前周的第一天的开始时间
     */
    public static long getFirstDayTimeOfWeek() {
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
    public static long getFirstDayTimeOfMonth() {
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
    public static long getFirstDayTimeOfYear() {
        Calendar currentDate = new GregorianCalendar();
        currentDate.setFirstDayOfWeek(Calendar.MONDAY);
        currentDate.set(Calendar.HOUR_OF_DAY, 0);
        currentDate.set(Calendar.MINUTE, 0);
        currentDate.set(Calendar.SECOND, 0);
        currentDate.set(Calendar.DAY_OF_YEAR, 0);
        return currentDate.getTime().getTime();
    }
}