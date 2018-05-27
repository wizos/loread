//package me.wizos.loread.adapter;
//
//import android.support.v4.view.PagerAdapter;
//import android.util.ArrayMap;
//import android.view.View;
//import android.view.ViewGroup;
//
//import com.socks.library.KLog;
//
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.jsoup.select.Elements;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.List;
//
//import me.wizos.loread.App;
//import me.wizos.loread.activity.ArticleActivity;
//import me.wizos.loread.data.PrefUtils;
//import me.wizos.loread.data.WithDB;
//import me.wizos.loread.db.Article;
//import me.wizos.loread.net.Api;
//import me.wizos.loread.utils.FileUtil;
//import me.wizos.loread.utils.HttpUtil;
//import me.wizos.loread.utils.StringUtil;
//import me.wizos.loread.utils.Tool;
//import me.wizos.loread.view.WebViewClientX;
//import me.wizos.loread.view.WebViewX;
//
//
///**
// * Created by Wizos on 2017/6/4.
// * ArticleActivity 内 ViewPager 的适配器
// */
//
//public class ViewPagerAdapter extends PagerAdapter { //  implements ViewPager.OnPageChangeListener
//    private ArticleActivity activity;
//    private List<Article> dataList;
//
//    public ViewPagerAdapter(ArticleActivity context, List<Article> dataList) {
//        if (null == dataList || dataList.isEmpty()) {return;}
//        this.activity = context;
//        this.dataList = dataList;
//    }
//
//
////    /**
////     * 这个方法会在屏幕滚动过程中不断被调用。
////     *
////     * @param pagePosition        当用手指滑动时，如果手指按在页面上不动，position和当前页面index是一致的；
////     *                             如果手指向左拖动时（页面向右翻动），position大部分时间和“当前页面”一致，只有翻页成功的情况下最后一次调用才会变为“目标页面”；
////     *                             如果手指向右拖动时（页面向左翻动），position大部分时间和“目标页面”一致，只有翻页不成功的情况下最后一次调用才会变为“原页面”。
////     *                             <p>
////     *                             当直接设置setCurrentItem翻页时，如果是相邻的情况（比如现在是第二个页面，跳到第一或者第三个页面）：
////     *                             如果页面向右翻动，大部分时间是和当前页面是一致的，只有最后才变成目标页面；
////     *                             如果页面向左翻动，position和目标页面是一致的。这和用手指拖动页面翻动是基本一致的。
////     *                             如果不是相邻的情况，比如我从第一个页面跳到第三个页面，position先是0，然后逐步变成1，然后逐步变成2；
////     *                             我从第三个页面跳到第一个页面，position先是1，然后逐步变成0，并没有出现为2的情况。
////     * @param positionOffset       当前页面滑动比例，如果页面向右翻动，这个值不断变大，最后在趋近1的情况后突变为0。如果页面向左翻动，这个值不断变小，最后变为0。
////     * @param positionOffsetPixels 当前页面滑动像素，变化情况和positionOffset一致。
////     */
////    @Override
////    public void onPageScrolled(int pagePosition, float positionOffset, int positionOffsetPixels) {
//////        judgeDirection(pagePosition); // TEST:
////    }
//////    private void judgeDirection(final int pagePosition) {
//////        KLog.e("方向", "滑动" + pagePosition + "==" + currentPosition);
//////        if (pagePosition < currentPosition || position == -1) {
//////            KLog.e("方向", "左滑");
//////            position = pagePosition;
//////        } else {
//////            KLog.e("方向", "右滑");
//////            position = pagePosition + 1;
//////        }
//////    }
////    //state 有三种取值： 0.什么都没做；1.开始滑动；2.滑动结束； 滑动过程的顺序，从滑动开始依次为：(1,2,0)
////    @Override
////    public void onPageScrollStateChanged(int state) {
////    }
//
//
//    // 功能：该函数用来判断instantiateItem(ViewGroup, int)函数所返回来的Key与一个页面视图是否是代表的同一个视图(即它俩是否是对应的，对应的表示同一个View)
//    // Note: 判断出去的view是否等于进来的view 如果为true直接复用
//    @Override
//    public boolean isViewFromObject(View arg0, Object arg1) {
//        return arg0 == arg1;
//    }
//
//
//    // 这个函数的功能是创建指定位置的页面视图
//    // viewpager预加载是这样产生的：在PagerAdapter里的instantiateItem方法中，如果有加载数据的逻辑，则viewpager就会预加载。
//    // 所以加载数据的逻辑不能放在PagerAdapter里的instantiateItem方法中。我们可以将加载数据逻辑放到页面切换监听中。
//    // Note: 做了两件事，第一：将当前视图添加到container中，第二：返回当前View
////    private List<WebViewS> mWebViewCaches = new ArrayList<>();
//
//    // 初始化一个 WebView 有以下几个步骤：
//    // 1.设置 WebSettings
//    // 2.设置 WebViewClient，①重载点击链接的时间，从而控制之后的跳转；②重载页面的开始加载事件和结束加载事件，从而控制图片和点击事件的加载
//    // 3.设置 WebChromeClient，①重载页面的加载改变事件，从而注入bugly的js监控
//    // 4.设置 DownloadListener，从而实现下载文件时使用系统的下载方法
//    // 5.设置 JS通讯
//    @Override
//    public Object instantiateItem(ViewGroup container, final int position) {
//        final WebViewX webView;
//        if (App.i().mWebViewCaches.size() > 0) {
//            webView = App.i().mWebViewCaches.get(0);
//            App.i().mWebViewCaches.remove(0);
//            KLog.e("WebView复用" + webView);
//        } else {
//            webView = new WebViewX(activity);
//            webView.setWebViewClient(new WebViewClientX(activity));
//            KLog.e("WebView创建" + webView);
//        }
//        webView.setWebViewClient(new WebViewClientX(activity));
//        webView.addJavascriptInterface( activity, "ImageBridge");
//        Tool.setBackgroundColor(webView);
//        KLog.e("WebView" + webView);
//        // 方便在其他地方调用 viewPager.findViewById 来找到 webView
//        webView.setId(position);
//        // 方便在webView onPageFinished 的时候调用
//        webView.setTag(dataList.get(position).getId());
//        container.addView(webView);
//
//        // TODO: 2018/3/11 检查该订阅源默认显示什么。同一个webview展示的内容有4种模式
//        // 【RSS，已读，保存的网页，原始网页】
//        // 由于是在 ViewPager 中使用 WebView，WebView 的内容要提前加载好，让ViewPager在左右滑的时候可见到左右的界面的内容。（但是这样是不是太耗资源了）
//
//
//        // TODO: 2018/3/11 兼容历史数据
//        webView.post(new Runnable() {
//            @Override
//            public void run() {
////                webView.loadDataWithBaseURL(FileUtil.getAbsoluteDir(dataList.get(position).getSaveDir()), StringUtil.getArticleHeader(dataList.get(position)) + initContent3( dataList.get(position) ) + StringUtil.getArticleFooter(), "text/html", "utf-8", null);
//                webView.loadDataWithBaseURL(FileUtil.getAbsoluteDir(dataList.get(position).getSaveDir()), StringUtil.getArticleHeader(dataList.get(position)) + initContent3( dataList.get(position) ) + StringUtil.getArticleFooter());
//            }
//        });
//
//        return webView;
//    }
//
//    private String initContent1(Article article ){
//        return article.getHtml();
//    }
//
//    private String initContent2(Article article ){
//        String articleContent = article.getHtml();
//        Document document = Jsoup.parseBodyFragment(articleContent);
//        Elements elements = document.getElementsByTag("img");
//
//        for(Element element : elements){
//            element.attr("original-src", element.attr("src"));
//            element.removeAttr("src");
//        }
//        articleContent = document.body().html();
//        return articleContent;
//    }
//
//    /**
//     * 这里没有直接将原始的文章内容给到 webView 加载，再去 webView 中初始化占位图并懒加载。
//     * 是因为这样 WebView 刚启动时，有的图片因为还没有被 js 替换为占位图，而展示一个错误图。
//     *
//     * 这里直接将内容初始化好，再让 WebView 执行懒加载的 js 去给没有加载本地图的 src 执行下载任务
//     * @param article
//     * @return
//     */
//    private String initContent3(Article article ){
//        String cacheUrl;
//        String originalUrl;
//        String imageHolder;
//        if( !HttpUtil.isNetworkAvailable() ){
//            imageHolder = "file:///android_asset/image/image_holder_load_failed.png";
//            KLog.e("ImageBridge", "没有网络");
//        }else if( PrefUtils.i().isDownImgWifi() && !HttpUtil.isWiFiUsed() ){
//            imageHolder = "file:///android_asset/image/image_holder_click_to_load.png";
//            KLog.e("ImageBridge", "开启省流量，蜂窝模式");
//        }else {
//            imageHolder = "file:///android_asset/image/image_holder_loading.gif";
//        }
//
//        String articleContent = article.getHtml();
//        Document document = Jsoup.parseBodyFragment(articleContent);
//        Elements elements = document.getElementsByTag("img");
//
//        for(Element element : elements){
//            originalUrl = element.attr("src");
//            element.attr("original-src", originalUrl);
//            cacheUrl = FileUtil.readCacheFilePath(article.getId(),originalUrl);
//            if( cacheUrl != null ){
//                element.attr("src", cacheUrl);
//            }else {
//                element.attr("src", imageHolder);
//            }
//        }
//        articleContent = document.body().html();
//        return articleContent;
//    }
//
//    // Note: 销毁预加载以外的view对象, 会把需要销毁的对象的索引位置传进来就是position
//    @Override
//    public void destroyItem(ViewGroup container, int position, Object object) {
//        if (object == null) {
//            return;
//        }
//        container.removeView((View) object);
//        ((WebViewX) object).clear();
//        App.i().mWebViewCaches.add((WebViewX) object);
//    }
//
//    @Override
//    public int getCount() {
//        return (null == dataList) ? 0 : dataList.size();
//    }
//
//
//
////    public void onResume() {
////        if( activity.viewPager.getCurrentItem()){
////
////        }
////        EntryView view = mEntryViews.get(mCurrentPagerPos);
////        if (view != null) {
////            view.onResume();
////        }
////    }
//
//
//    public void onPause() {
//        for (int i = 0; i < App.i().mWebViewCaches.size(); i++) {
//            App.i().mWebViewCaches.get(i).onPause();
//        }
//    }
//
//}
