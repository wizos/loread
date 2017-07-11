package me.wizos.loread.presenter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.util.ArrayMap;

import com.socks.library.KLog;
import com.tencent.smtt.sdk.DownloadListener;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import me.wizos.loread.App;
import me.wizos.loread.bean.Img;
import me.wizos.loread.data.WithDB;
import me.wizos.loread.data.WithSet;
import me.wizos.loread.utils.Tool;

//import android.webkit.WebView;
//import android.webkit.DownloadListener;
//import android.webkit.WebSettings;
//import android.webkit.WebViewClient;

/**
 * Created by Wizos on 2017/7/3.
 */

public class X5WebView extends WebView {
    @SuppressLint("SetJavaScriptEnabled")
//    WeakReference<Activity> WRArticle = new WeakReference<>(activity);
    public X5WebView(Activity activity) {
        super(activity);
        initWebViewSettings();
        Tool.setBackgroundColor(this); // 实现 webview 的背景颜色与当前主题色一致
        this.addJavascriptInterface(activity, "imagelistner");
        this.setWebViewClient(new WebViewClientX(activity));
        this.setDownloadListener(new WebViewDownLoadListener());
//        this.getView().setClickable(true);
    }

    private void initWebViewSettings() {
        WebSettings webSetting = this.getSettings();
        webSetting.setJavaScriptEnabled(true);
        // 设置使用 宽 的 Viewpoint,默认是false
        // Android browser以及chrome for Android的设置是`true`，而WebView的默认设置是`false`
        // 如果设置为`true`,那么网页的可用宽度为`980px`,并且可以通过 meta data来设置，如果设置为`false`,那么可用区域和WebView的显示区域有关.
        webSetting.setUseWideViewPort(false);// 设置此属性，可任意比例缩放
        webSetting.setLoadWithOverviewMode(true); // 缩放至屏幕的大小，如果webview内容宽度大于显示区域的宽度,那么将内容缩小,以适应显示区域的宽度, 默认是false
        webSetting.setSupportZoom(false);// 设置可以支持缩放
        webSetting.setBuiltInZoomControls(false); // 默认的缩放控制器
        webSetting.setDisplayZoomControls(false); // 默认的+/-缩放控制
        webSetting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN); // NARROW_COLUMNS 适应内容大小 ， SINGLE_COLUMN 自适应屏幕
        // 设置渲染线程的优先级。该方法在 Api 18之后被废弃,优先级由WebView自己管理。不过任然建议将其设置为 HIGH,来提高页面渲染速度
        webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSetting.setJavaScriptCanOpenWindowsAutomatically(false); // 支持通过js打开新的窗口
//        webSetting.setAllowFileAccess(true); // 允许访问文件
//        webSetting.setSupportMultipleWindows(true);
//        webSetting.setGeolocationEnabled(true);
//        webSetting.setAppCacheEnabled(true); // 支持H5的 application cache 的功能
//        webSetting.setAppCacheMaxSize(Long.MAX_VALUE);
//        webSetting.setDomStorageEnabled(true); // Dom Storage（Web Storage）存储，临时简单的缓存
//        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null); // 硬件加速
        // webSetting.setDatabaseEnabled(true); // 支持javascript读,写db
        // webSetting.setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);
        webSetting.setCacheMode(WebSettings.LOAD_NO_CACHE);
//        webSetting.setPluginState(WebSettings.PluginState.ON_DEMAND);
        // this.getSettingsExtension().setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);//extension
    }

    private class WebViewDownLoadListener implements DownloadListener {
        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
            KLog.i("tag", "url=" + url + "   userAgent=" + userAgent + "   contentDisposition=" + contentDisposition);
            KLog.i("tag", "mimetype=" + mimetype);
            KLog.i("tag", "contentLength=" + contentLength);
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            App.i().startActivity(intent);
        }
    }

    private class WebViewClientX extends WebViewClient {
        //        WeakReference<Activity> weakReferenceActivity;
        Context context;

        WebViewClientX(Activity activity) {
//            this.weakReferenceActivity = new WeakReference<>(activity);
            context = activity;
        }

        @Override
        //  重写此方法表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            KLog.d("==========" + WithSet.i().isSysBrowserOpenLink());
            if (WithSet.i().isSysBrowserOpenLink()) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse(url);
                intent.setData(content_url);
                context.startActivity(intent);
                return true;
            }
            return super.shouldOverrideUrlLoading(view, url);
        }

        /**
         * 在每次页面加载完数据的时候调用（ loadingPageData 函数）
         * 你永远无法确定当 WebView 调用这个方法的时候，网页内容是否真的加载完毕了。
         * 当前正在加载的网页产生跳转的时候这个方法可能会被多次调用，StackOverflow上有比较具体的解释（How to listen for a Webview finishing loading a URL in Android?）， 但其中列举的解决方法并不完美。
         * 所以当你的WebView需要加载各种各样的网页并且需要在页面加载完成时采取一些操作的话，可能WebChromeClient.onProgressChanged()比WebViewClient.onPageFinished()都要靠谱一些。、
         * <p>
         * OnPageFinished 事件会在 Javascript 脚本执行完成之后才会触发。如果在页面中使 用JQuery，会在处理完 DOM 对象，执行完 $(document).ready(function() {}); 事件自会后才会渲染并显示页面。而同样的页面在 iPhone 上却是载入相当的快，因为 iPhone 是显示完页面才会触发脚本的执行。所以我们这边的解决方案延迟 JS 脚本的载入
         */
        @Override
        public void onPageFinished(WebView webView, String url) {
            super.onPageFinished(webView, url);
            KLog.e("WebView", "onPageFinished" + (System.currentTimeMillis() - App.time));
            webView.loadUrl("javascript:initImgClick()"); // 初始化图片的点击事件
            initImgPlace(webView);
        }
//    @Override
//    public void onLoadResource(WebView var1, String var2) {
//        KLog.e("WebView","onLoadResource"  + var1 + " == "+ ( System.currentTimeMillis() - App.time ) );
//    }
//    @Override
//    public void onPageStarted(WebView var1, String var2, Bitmap var3) {
//        KLog.e("【WebView】","onPageStarted"  + var1 + " == "+ ( System.currentTimeMillis() - App.time ) );
//        super.onPageStarted(var1,var2,var3);
//    }
//    @Override
//    public void onReceivedError(WebView var1, int var2, String var3, String var4) {
//        super.onReceivedError(var1,var2,var3,var4);
//        KLog.e("WebView","onReceivedError"  + ( System.currentTimeMillis() - App.time ) );
//    }
//    @Override
//    public void onReceivedHttpError(WebView var1, WebResourceRequest var2, WebResourceResponse var3) {
//        KLog.e("WebView","onReceivedHttpError"  + ( System.currentTimeMillis() - App.time ) );
//    }
    }


    private void initImgPlace(WebView webView) {
        final ArrayMap<Integer, Img> imgMap = WithDB.i().getImgs((String) webView.getTag());
        final ArrayMap<Integer, Img> lossImgMap = WithDB.i().getLossImgs((String) webView.getTag()); // dataList.get(webView.getId()).getId()
        int lossImgSize = lossImgMap.size();

        if (lossImgSize == imgMap.size()) {
            webView.loadUrl("javascript:initImgPlaceholder()"); // 初始化占位图
        } else {
            StringBuilder imgNoArray = new StringBuilder("");
            for (int i = 0; i < lossImgSize; i++) {
                imgNoArray.append(lossImgMap.keyAt(i) - 1); // imgState 里的图片下标是从1开始的
                imgNoArray.append("_");
            }
            KLog.i("传递的值" + imgNoArray + webView);
            webView.loadUrl("javascript:appointImgPlaceholder(" + "\"" + imgNoArray.toString() + "\"" + ")");
        }
    }
}
