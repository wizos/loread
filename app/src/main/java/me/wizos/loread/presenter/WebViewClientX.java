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

public class WebViewClientX extends WebViewClient {
    private Context context;

    public WebViewClientX(Context context) {
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
        webView.getSettings().setBlockNetworkImage(false);
        KLog.d("页面加载完成A");
        // Page渲染完成之后，添加图片的Js点击监听函数
        webView.loadUrl("javascript:initImgClick()"); // 初始化图片的点击事件
//        webView.loadUrl("javascript:initImgPlaceholder()"); // 初始化占位图
//            handleWebView();
    }

}
