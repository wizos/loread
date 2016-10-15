package me.wizos.loread.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * 关于时间操作的工具类
 * Created by xdsjs on 2015/10/14.
 */
public class UTime {

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

    /**
     * 获取当前时间，并转换为数据库次数表中需要的时间
     */




    public static int getCurrentTime() {
        Calendar currentDate = new GregorianCalendar();
        int hour = currentDate.get(Calendar.HOUR_OF_DAY);
        if (hour >= 2 && hour < 6)
            return 0;
        if (hour >= 6 && hour < 7)
            return 1;
        if (hour >= 7 && hour < 8)
            return 2;
        if (hour >= 8 && hour < 9)
            return 3;
        if (hour >= 9 && hour < 11)
            return 4;
        if (hour >= 11 && hour < 13)
            return 5;
        if (hour >= 13 && hour < 14)
            return 6;
        if (hour >= 14 && hour < 16)
            return 7;
        if (hour >= 16 && hour < 17)
            return 8;
        if (hour >= 17 && hour < 19)
            return 9;
        if (hour >= 19 && hour < 21)
            return 10;
        if (hour >= 21 && hour < 23)
            return 11;
        if (hour >= 23 && hour < 2)
            return 12;
        return 0;
    }

//    public static String getPrettyTime(long time) {
//        Date date = new Date(time);
//        SimpleDateFormat sdf2 = new SimpleDateFormat("MM.dd");
//        return sdf2.format(date);
//    }
    public static String getCurrentDateID(int position) {
        Date dateID = new Date(System.currentTimeMillis() + position*24*3600*1000L); // 因为后面算的数目太大，超出其格式 int 的范围，所以加 L 使用 Long 类型
        SimpleDateFormat dateYMD = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        SimpleDateFormat dateHMS = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);
        SimpleDateFormat dateYMDHMS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        return dateYMDHMS.format(dateID);
    }

    public static Date getDateTime(int position){
        Date dateID = new Date(System.currentTimeMillis() + position*24*3600*1000L); // 因为后面算的数目太大，超出其格式 int 的范围，所以加 L 使用 Long 类型
        return dateID;
    }
    public static String getDate(int position){
        SimpleDateFormat dateYMD = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        return dateYMD.format(getDateTime(position));
    }
    public static String getNow() {
        SimpleDateFormat dateHMS = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);
        System.out.println("【当前】");
        return dateHMS.format(getDateTime(0));
    }

    public static String getFormatDate(Date date){
        SimpleDateFormat dateYMD = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        return dateYMD.format(date);
    }

    public static String getFormatDate(Long stringDate){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.CHINA);
        Date date = new Date(stringDate);
        return dateFormat.format(date);
    }
}
