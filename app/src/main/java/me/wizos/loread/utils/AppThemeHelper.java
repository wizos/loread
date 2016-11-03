//package me.wizos.loread.utils;
//
//import android.content.Context;
//import android.content.SharedPreferences;
//
///**
// * Created by Wizos on 2016/11/1.
// */
//
//public class AppThemeHelper {
//    private final static String FILE_NAME = "settings";
//    private final static String MODE = "day_night_mode";
//
//    private SharedPreferences mSharedPreferences;
//
//    public AppThemeHelper(Context context) {
//        this.mSharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
//    }
//
//    /**
//     * 保存模式设置
//     *
//     * @param mode
//     * @return
//     */
//    public boolean setMode(DayNight mode) {
//        SharedPreferences.Editor editor = mSharedPreferences.edit();
//        editor.putString(MODE, mode.getName());
//        return editor.commit();
//    }
//
//    /**
//     * 夜间模式
//     *
//     * @return
//     */
//    public boolean isNight() {
//        String mode = mSharedPreferences.getString(MODE, DayNight.DAY.getName());
//        if (DayNight.NIGHT.getName().equals(mode)) {
//            return true;
//        } else {
//            return false;
//        }
//    }
//
//    /**
//     * 日间模式
//     *
//     * @return
//     */
//    public boolean isDay() {
//        String mode = mSharedPreferences.getString(MODE, DayNight.DAY.getName());
//        if (DayNight.DAY.getName().equals(mode)) {
//            return true;
//        } else {
//            return false;
//        }
//    }
//
//    private class DayNight{
//        private int modeName = 1;
//        private final int dayMode = 1;
//        private final int nightMode = 0;
//        public void setDayMode() {
//            this.modeName = dayMode;
//        }
//        public void setNightMode() {
//            this.modeName = nightMode;
//        }
//        public boolean isDay() {
//            return modeName == dayMode;
//        }
//
//    }
//}
