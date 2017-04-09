package me.wizos.loread.presenter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.socks.library.KLog;

import me.wizos.loread.data.WithSet;

/**
 * Created by Wizos on 2017/4/9.
 */

public class MWebViewClient extends WebViewClient {
    private Context context;

    public MWebViewClient(Context context) {
        this.context = context;
    }

    @Override
    //  重写此方法表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        KLog.d("==========" + WithSet.getInstance().isSysBrowserOpenLink());
        if (WithSet.getInstance().isSysBrowserOpenLink()) {
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
     * 所以当你的WebView需要加载各种各样的网页并且需要在页面加载完成时采取一些操作的话，可能WebChromeClient.onProgressChanged()比WebViewClient.onPageFinished()都要靠谱一些。
     */
    @Override
    public void onPageFinished(WebView webView, String url) {
        super.onPageFinished(webView, url);
        KLog.d("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        webView.loadUrl("javascript:initImgClick()"); // 初始化图片的点击事件
        KLog.d("页面加载完成");
//            // html加载完成之后，添加监听图片的点击js函数
//            KLog.d("加载完成诸如js函数" + webView + "===" + ArticleActivity.this.webView);
//            webView.loadUrl("javascript:initImgClick()"); // 初始化图片的点击事件
//
//            if (article.getImgState() == null) {
//                webView.loadUrl("javascript:initImgPlaceholder()");
//                KLog.d("初始化所有图片占位符a");
//            } else if (!article.getImgState().equals("")) { // 有图且打开过
//                KLog.d("初始化所有图片占位符b" + article.getImgState() );
//                if (lossSrcList != null && lossSrcList.size() != 0) {
//                    numOfImgs = lossSrcList.size();
////                    obtainSrcList = new ArrayMap<>(numOfImgs);
//                    StringBuilder imgNoArray = new StringBuilder("");
//                    for (int i = 0; i < numOfImgs; i++) {
//                        imgNoArray.append(lossSrcList.keyAt(i) - 1); // imgState 里的图片下标是从1开始的
//                        imgNoArray.append( "_" );
//                    }
//                    imgNoArray.deleteCharAt(imgNoArray.length()-1);
//                    KLog.d("传递的值" + imgNoArray);
//                    webView.loadUrl("javascript:appointImgPlaceholder("+ "\"" + imgNoArray.toString() + "\"" +")");
//                }
//                mNeter.downImgs( articleID, lossSrcList, UFile.getRelativeDir(article.getSaveDir()) + fileTitle + "_files" + File.separator);
//            }
    }
//        @Override
//        public void onPageStarted(WebView view, String url, Bitmap favicon) {
//            super.onPageStarted(view, url, favicon);
//        }
}
