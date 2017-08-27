package me.wizos.loread.presenter.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.socks.library.KLog;

import java.util.ArrayList;
import java.util.List;

import me.wizos.loread.App;
import me.wizos.loread.activity.ArticleActivity;
import me.wizos.loread.bean.Article;
import me.wizos.loread.data.WithSet;
import me.wizos.loread.utils.FileUtil;
import me.wizos.loread.utils.StringUtil;
import me.wizos.loread.view.X5WebView;

//import android.webkit.WebSettings;
//import android.webkit.WebView;

/**
 * Created by Wizos on 2017/6/4.
 * ArticleActivity 内 ViewPager 的适配器
 */

public class ViewPagerAdapter extends PagerAdapter { //  implements ViewPager.OnPageChangeListener
    private ArticleActivity activity;
    private List<Article> dataList;
    private List<X5WebView> mViewHolderList = new ArrayList<>();
//    private ViewPager viewPager;

    public ViewPagerAdapter(ArticleActivity context, ViewPager viewPager, List<Article> dataList, ArticleActivity.ArtHandler artHandler) {
        if (null == dataList || dataList.isEmpty()) return;
        this.activity = context;
        this.dataList = dataList;
//        this.viewPager = viewPager;
    }


//    /**
//     * 这个方法会在屏幕滚动过程中不断被调用。
//     *
//     * @param pagePosition        当用手指滑动时，如果手指按在页面上不动，position和当前页面index是一致的；
//     *                             如果手指向左拖动时（页面向右翻动），position大部分时间和“当前页面”一致，只有翻页成功的情况下最后一次调用才会变为“目标页面”；
//     *                             如果手指向右拖动时（页面向左翻动），position大部分时间和“目标页面”一致，只有翻页不成功的情况下最后一次调用才会变为“原页面”。
//     *                             <p>
//     *                             当直接设置setCurrentItem翻页时，如果是相邻的情况（比如现在是第二个页面，跳到第一或者第三个页面）：
//     *                             如果页面向右翻动，大部分时间是和当前页面是一致的，只有最后才变成目标页面；
//     *                             如果页面向左翻动，position和目标页面是一致的。这和用手指拖动页面翻动是基本一致的。
//     *                             如果不是相邻的情况，比如我从第一个页面跳到第三个页面，position先是0，然后逐步变成1，然后逐步变成2；
//     *                             我从第三个页面跳到第一个页面，position先是1，然后逐步变成0，并没有出现为2的情况。
//     * @param positionOffset       当前页面滑动比例，如果页面向右翻动，这个值不断变大，最后在趋近1的情况后突变为0。如果页面向左翻动，这个值不断变小，最后变为0。
//     * @param positionOffsetPixels 当前页面滑动像素，变化情况和positionOffset一致。
//     */
//    @Override
//    public void onPageScrolled(int pagePosition, float positionOffset, int positionOffsetPixels) {
////        judgeDirection(pagePosition); // TEST:
//    }
////    private void judgeDirection(final int pagePosition) {
////        KLog.e("方向", "滑动" + pagePosition + "==" + currentPosition);
////        if (pagePosition < currentPosition || position == -1) {
////            KLog.e("方向", "左滑");
////            position = pagePosition;
////        } else {
////            KLog.e("方向", "右滑");
////            position = pagePosition + 1;
////        }
////    }
//    //state 有三种取值： 0.什么都没做；1.开始滑动；2.滑动结束； 滑动过程的顺序，从滑动开始依次为：(1,2,0)
//    @Override
//    public void onPageScrollStateChanged(int state) {
//    }


    // 功能：该函数用来判断instantiateItem(ViewGroup, int)函数所返回来的Key与一个页面视图是否是代表的同一个视图(即它俩是否是对应的，对应的表示同一个View)
    // Note: 判断出去的view是否等于进来的view 如果为true直接复用
    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }


    // 这个函数的功能是创建指定位置的页面视图
    // viewpager预加载是这样产生的：在PagerAdapter里的instantiateItem方法中，如果有加载数据的逻辑，则viewpager就会预加载。
    // 所以加载数据的逻辑不能放在PagerAdapter里的instantiateItem方法中。我们可以将加载数据逻辑放到页面切换监听中。
    // Note: 做了两件事，第一：将当前视图添加到container中，第二：返回当前View
    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        final X5WebView webView;
        if (mViewHolderList.size() > 0) {
            webView = mViewHolderList.get(0);
            mViewHolderList.remove(0);
        } else {
            webView = new X5WebView(activity);
            webView.setWebViewClient(new WebViewClientX(activity));
            KLog.e("webView：" + webView);
        }
        KLog.i("【instantiateItem】 " + "实例position：" + position + "==" + webView);
        webView.setId(position); // 方便在其他地方调用 viewPager.findViewById 来找到 webView
        webView.setTag(dataList.get(position).getId()); // 方便在webView onPageFinished 的时候调用
        container.addView(webView);

        webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                webView.loadDataWithBaseURL(FileUtil.getAbsoluteDir(dataList.get(position).getSaveDir()), StringUtil.getHtmlHeader() + StringUtil.getArticleHtml(dataList.get(position)) + StringUtil.getFooter(), "text/html", "utf-8", null);
            }
        }, 500);

        return webView;
    }


    // Note: 销毁预加载以外的view对象, 会把需要销毁的对象的索引位置传进来就是position
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        KLog.i("【destroyItem】 " + "当前position：" + position);
        container.removeView((View) object);
        ((X5WebView) object).clear();
        mViewHolderList.add((X5WebView) object);
    }

    @Override
    public int getCount() {
        return (null == dataList) ? 0 : dataList.size();
    }


//    @Override
//    public Object instantiateItem(ViewGroup container,final int position) {
////        TextView webView = new TextView(activity);
////        webView.setText( "页面：" + position );
    //        webView.loadDataWithBaseURL( "", "<html max-width:100%;'><head>TTTT</head><body><p>页面：" + position + "</p></body></html>", "text/html", "utf-8", null);
//        container.addView(webView);
//        return webView;
//    }
//
//    @Override
//    public void destroyItem(ViewGroup container, int position, Object object) {
//        KLog.i("【destroyItem】 " + "当前position：" + position);
//        container.removeView((View) object);
//    }


    private class WebViewClientX extends WebViewClient {
        Context context;

        WebViewClientX(Activity activity) {
            context = activity;
        }

        @Override
        //  重写此方法表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            KLog.d("loread", "shouldOverrideUrlLoading" + WithSet.i().isSysBrowserOpenLink());
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
         * OnPageFinished 事件会在 Javascript 脚本执行完成之后才会触发。
         * 如果在页面中使 用JQuery，会在处理完 DOM 对象，执行完 $(document).ready(function() {}); 事件自会后才会渲染并显示页面。
         * 而同样的页面在 iPhone 上却是载入相当的快，因为 iPhone 是显示完页面才会触发脚本的执行。所以我们这边的解决方案延迟 JS 脚本的载入
         */
        @Override
        public void onPageFinished(WebView webView, String url) {
            super.onPageFinished(webView, url);
            KLog.e("WebView", "onPageFinished" + (System.currentTimeMillis() - App.time) + "====" + webView.getProgress());
            webView.loadUrl("javascript:initImgClick()"); // 初始化图片的点击事件
        }

//        @Override
//        public void onPageStarted(WebView webView, String var2, Bitmap var3) {
//            super.onPageStarted(webView,var2,var3);
//        }
//        @Override
//        public void onLoadResource(WebView var1, String var2) {
//        }
//        @Override
//        public void onReceivedError(WebView var1, int var2, String var3, String var4) {
//            super.onReceivedError(var1,var2,var3,var4);
//        }
//        @Override
//        public void onReceivedHttpError(WebView var1, WebResourceRequest var2, WebResourceResponse var3) {
//        }
    }

}
