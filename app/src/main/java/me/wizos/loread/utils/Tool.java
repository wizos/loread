package me.wizos.loread.utils;

import android.view.View;

import com.socks.library.KLog;

import me.wizos.loread.App;
import me.wizos.loread.BuildConfig;
import me.wizos.loread.R;
import me.wizos.loread.data.WithPref;
import me.wizos.loread.view.WebViewS;

/**
 * 一些比较杂的工具函数
 * Created by Wizos on 2016/11/1.
 */

public class Tool {

    public static void showShort(String msg) {
        if (BuildConfig.DEBUG) {
            KLog.e(msg);
            ToastUtil.showShort(msg);
        }
    }


    /**
     * 能否下载图片分以下几种情况：
     * 1，开启省流量 & Wifi 可用
     * 2，开启省流量 & Wifi 不可用
     * 3，关闭省流量 & 网络 可用
     * 4，关闭省流量 & 网络 不可用
     *
     * @return
     */
    public static boolean canDownImg() {
        if (!WithPref.i().isDownImgWifi() && !HttpUtil.isNetworkAvailable()) {
//            ToastUtil.showShort(App.i().getString(R.string.toast_not_network));
            return false;
        }
        return !(WithPref.i().isDownImgWifi() && !HttpUtil.isWiFiUsed());
    }


    public static void showLong(String msg) {
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

    public static void setWebViewsBGColor() {
        if (WithPref.i().getThemeMode() == App.Theme_Night) {
            for (WebViewS webViewS : App.i().mWebViewCaches) {
                webViewS.setBackgroundColor(App.i().getResources().getColor(R.color.article_dark_background));
            }
        } else {
            for (WebViewS webViewS : App.i().mWebViewCaches) {
                webViewS.setBackgroundColor(App.i().getResources().getColor(R.color.white));
            }
        }
    }


    /**
     * dp转换成px
     *
     * @param dp dp
     * @return px值
     */
    public static int dp2px(float dp) {
        final float scale = App.i().getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
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
