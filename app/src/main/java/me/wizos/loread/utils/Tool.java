package me.wizos.loread.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.view.View;

import com.socks.library.KLog;

import java.text.DecimalFormat;
import java.util.List;

import me.wizos.loread.App;
import me.wizos.loread.BuildConfig;
import me.wizos.loread.R;
import me.wizos.loread.data.WithPref;

/**
 * 一些比较杂的工具函数
 * Created by Wizos on 2016/11/1.
 */

public class Tool {

    public static void show(String msg) {
        if (BuildConfig.DEBUG) {
            KLog.e(msg);
            ToastUtil.showLong(msg);
        }
    }

    public static void printCallStatck() {
        if (!BuildConfig.DEBUG) {
            return;
        }
        Throwable ex = new Throwable();
        StackTraceElement[] stackElements = ex.getStackTrace();
        if (stackElements != null) {
            KLog.e("-----------------------------------");
            for (StackTraceElement stackElement : stackElements) {
                KLog.e(stackElement.getClassName() + "_" + stackElement.getFileName() + "_" + stackElement.getLineNumber() + "_" + stackElement.getMethodName());
            }
            KLog.e("-----------------------------------");
        }
    }

    public static void setBackgroundColor(View object) {
        if (WithPref.i().getThemeMode() == App.Theme_Night) {
            object.setBackgroundColor(App.i().getResources().getColor(R.color.article_dark_background));
        } else {
            object.setBackgroundColor(App.i().getResources().getColor(R.color.white));
        }
    }

//    public static void setWebViewsBGColor() {
//        if (WithPref.i().getThemeMode() == App.Theme_Night) {
//            for (WebViewS webViewS : App.i().mWebViewCaches) {
//                webViewS.setBackgroundColor(App.i().getResources().getColor(R.color.article_dark_background));
//            }
//        } else {
//            for (WebViewS webViewS : App.i().mWebViewCaches) {
//                webViewS.setBackgroundColor(App.i().getResources().getColor(R.color.white));
//            }
//        }
//    }


    public static String getNetFileSizeDescription(Context context, long size) {
        if (context != null && size == -1) {
            return context.getString(R.string.unknow);
        }
        StringBuffer bytes = new StringBuffer();
        DecimalFormat format = new DecimalFormat("###.0");
        if (size >= 1024 * 1024 * 1024) {
            double i = (size / (1024.0 * 1024.0 * 1024.0));
            bytes.append(format.format(i)).append("GB");
        } else if (size >= 1024 * 1024) {
            double i = (size / (1024.0 * 1024.0));
            bytes.append(format.format(i)).append("MB");
        } else if (size >= 1024) {
            double i = (size / (1024.0));
            bytes.append(format.format(i)).append("KB");
        } else if (size < 1024) {
            if (size <= 0) {
                bytes.append("0B");
            } else {
                bytes.append((int) size).append("B");
            }
        }
        return bytes.toString();
    }


    /**
     * 包名判断是否为主进程
     *
     * @param context
     * @return
     */
    public static boolean isMainProcess(Context context) {
        return context.getPackageName().equals(getProcessName(context));
    }

    /**
     * 获取进程名称
     *
     * @param context
     * @return
     */
    public static String getProcessName(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo proInfo : runningApps) {
            if (proInfo.pid == android.os.Process.myPid()) {
                if (proInfo.processName != null) {
                    return proInfo.processName;
                }
            }
        }
        return null;
    }

//    public boolean isDebug() {
//        try {
//            ApplicationInfo info = this.getApplicationInfo();
//            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
//        } catch (Exception e) {
//            return false;
//        }
//    }


//    public void clear(int days) {
//        long clearTime = System.currentTimeMillis() - days * 24 * 3600 * 1000L;
//        List<Article> allArtsBeforeTime = WithDB.i().getArtInReadedUnstarLtTime(clearTime);
//        KLog.i("清除A：" + clearTime + "--" + allArtsBeforeTime.size() + "--" + days);
//        if (allArtsBeforeTime.size() == 0) {
//            return;
//        }
//        ArrayList<String> idListMD5 = new ArrayList<>(allArtsBeforeTime.size());
//        for (Article article : allArtsBeforeTime) {
//            idListMD5.add(StringUtil.str2MD5(article.getId()));
//        }
//        KLog.i("清除B：" + clearTime + "--" + allArtsBeforeTime.size() + "--" + days);
//        FileUtil.deleteHtmlDirList(idListMD5);
//        WithDB.i().delArt(allArtsBeforeTime);
//        WithDB.i().delArticleImgs(allArtsBeforeTime);
//    }
//
//    private void up(){
//        List<Article> articles = WithDB.i().getArtsUnhandle();
//        File file;
//        File files[];
//        for (Article article:articles){
//            file = new File(App.boxReadRelativePath + article.getTitle() + "_files") ;
//            if(file.exists()){
//                files = file.listFiles();
//                article.setReadState(Api.ART_READED);
//                article.setSaveDir(Api.SAVE_DIR_BOXREAD);
//                article.setImgState(Api.ImgState_Over);
//                article.setCoverSrc( "file:" + File.separator + File.separator + files[0].getAbsolutePath() );
//                KLog.e("该文件存在"+  "==" + files[0].getAbsolutePath()  );
//            }
//        }
//        WithDB.i().saveArticles(articles);
//    }
//
//    private void up3(){
//        File files[] = new File(App.boxRelativePath).listFiles();
//        int size = 0;
//        File DirFile;
//
//        String fileName;
//        for (File file:files){
//            if(!file.getName().endsWith(".html")){
//                continue;
//            }
//            fileName = file.getName().replace(".html","");
//            if(WithDB.i().isArticleExists(fileName)){
//                continue;
//            }
//            size++;
//            KLog.e("该文章在数据库中不存在：" + fileName );
//            FileUtil.moveFile(file.getPath(), FileUtil.getRelativeDir("VV") + fileName + ".html" );
////            KLog.e("移动文件：" + file.getPath() + " = " +  FileUtil.getRelativeDir("VV") + fileName + ".html"  );
//            DirFile = new File( App.boxRelativePath + fileName + "_files" );
//            if(DirFile.exists()){
//                FileUtil.moveDir(DirFile.getPath(), FileUtil.getRelativeDir("VV") + fileName + "_files" );
////                KLog.e("移动目录：" + DirFile.getPath() + " = " +  FileUtil.getRelativeDir("VV") + fileName + "_files"  );
//            }
//        }
//        KLog.e("不存在的文件数为：" + size );
//    }
//
//    private void up2(){
//        List<Article> articles = WithDB.i().loadAllArts();
//        for (Article article:articles){
//            article.setReadState(Api.ART_UNREAD);
//            if(!article.getSaveDir().equals(Api.SAVE_DIR_CACHE)){
//                article.setReadState(Api.ART_UNREADING);
//            }
//        }
//        WithDB.i().saveArticles(articles);
//    }
//
//
//    public static InoApi getNetApi(){
//        return InoApi.i();
//    }

}
