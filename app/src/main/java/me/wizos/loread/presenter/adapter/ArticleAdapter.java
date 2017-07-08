package me.wizos.loread.presenter.adapter;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.socks.library.KLog;

import java.util.ArrayList;
import java.util.List;

import me.wizos.loread.App;
import me.wizos.loread.activity.ArticleActivity;
import me.wizos.loread.bean.Article;
import me.wizos.loread.presenter.X5WebView;
import me.wizos.loread.utils.FileUtil;
import me.wizos.loread.utils.StringUtil;
import me.wizos.loread.utils.Tool;

//import android.webkit.WebSettings;
//import android.webkit.WebView;

/**
 * Created by Wizos on 2017/6/4.
 * ArticleActivity 内 ViewPager 的适配器
 */

public class ArticleAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener {
    private ArticleActivity activity;
    private List<Article> dataList;
    //    public SparseArray<WebView> map = new SparseArray();
    private List<X5WebView> mViewHolderList = new ArrayList<>();
    private ViewPager viewPager;
    private ArticleActivity.ArtHandler artHandler;

    public ArticleAdapter(ArticleActivity context, ViewPager viewPager, List<Article> dataList, ArticleActivity.ArtHandler artHandler) {
        if (null == dataList || dataList.isEmpty()) return;
        this.activity = context;
        this.dataList = dataList;
        this.artHandler = artHandler;

        Tool.setBackgroundColor(viewPager);
        viewPager.clearOnPageChangeListeners();
        viewPager.addOnPageChangeListener(this);
        this.viewPager = viewPager;
    }

    private int currentPosition = 0;

    /**
     * 参数position，代表哪个页面被选中。
     * 当用手指滑动翻页的时候，如果翻动成功了（滑动的距离够长），手指抬起来就会立即执行这个方法，position就是当前滑动到的页面。
     * 如果直接setCurrentItem翻页，那position就和setCurrentItem的参数一致，这种情况在onPageScrolled执行方法前就会立即执行。
     * 泪奔，当 setCurrentItem (0) 的时候，不会调用该函数
     *
     * 点击进入 viewpager 时，该函数比 InstantiateItem 函数先执行。（之后滑动时，InstantiateItem 已经创建好了 view）
     * 所以，为了能在运行完 InstantiateItem ，有了 webView 之后再去执行 showingPageData，onPageSelected 在首次进入时都不执行，放到 InstantiateItem 中。
     */
    @Override
    public void onPageSelected(final int pagePosition) {
        KLog.i("【onPageSelected】 " + "当前position：" + pagePosition + "  之前position：" + currentPosition); //+ this.map.size()
//        KLog.i( dataList.get(pagePosition).getImgState() );
        currentPosition = pagePosition;
//        WithDB.i().getArticle( dataList.get(pagePosition).getId()).getImgState();
        if (viewPager.findViewById(pagePosition) == null) { // && dataList.get(pagePosition).getImgState()==null
            artHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    onPageSelected(pagePosition);
                }
            }, 100);
            return;
        }
        activity.showingPageData(dataList.get(pagePosition), (X5WebView) viewPager.findViewById(pagePosition), pagePosition);
    }


    /**
     * 这个方法会在屏幕滚动过程中不断被调用。
     *
     * @param pagePosition         当用手指滑动时，如果手指按在页面上不动，position和当前页面index是一致的；
     *                             如果手指向左拖动时（页面向右翻动），position大部分时间和“当前页面”一致，只有翻页成功的情况下最后一次调用才会变为“目标页面”；
     *                             如果手指向右拖动时（页面向左翻动），position大部分时间和“目标页面”一致，只有翻页不成功的情况下最后一次调用才会变为“原页面”。
     *                             <p>
     *                             当直接设置setCurrentItem翻页时，如果是相邻的情况（比如现在是第二个页面，跳到第一或者第三个页面）：
     *                             如果页面向右翻动，大部分时间是和当前页面是一致的，只有最后才变成目标页面；
     *                             如果页面向左翻动，position和目标页面是一致的。这和用手指拖动页面翻动是基本一致的。
     *                             如果不是相邻的情况，比如我从第一个页面跳到第三个页面，position先是0，然后逐步变成1，然后逐步变成2；
     *                             我从第三个页面跳到第一个页面，position先是1，然后逐步变成0，并没有出现为2的情况。
     * @param positionOffset       当前页面滑动比例，如果页面向右翻动，这个值不断变大，最后在趋近1的情况后突变为0。如果页面向左翻动，这个值不断变小，最后变为0。
     * @param positionOffsetPixels 当前页面滑动像素，变化情况和positionOffset一致。
     */
    @Override
    public void onPageScrolled(int pagePosition, float positionOffset, int positionOffsetPixels) {
    }


    /**
     * state 有三种取值：
     * 0：什么都没做
     * 1：开始滑动
     * 2：滑动结束
     * 滑动过程的顺序，从滑动开始依次为：(1,2,0)
     */
    @Override
    public void onPageScrollStateChanged(int state) {
//        KLog.e( "==============滑动状态改变：" +  state );
//        if( state == 0){
//            // Note 这段代码原本想放在 viewPager 加载完成之后。因为那时 webview 已经有了。如果放在 onPageSelected 函数中，因为 webview 是在其之后产生的，所以会报错。
//            activity.showingPageData( dataList.get(currentPosition), this.map.get(currentPosition), currentPosition );
//        }
    }


    // 功能：该函数用来判断instantiateItem(ViewGroup, int)函数所返回来的Key与一个页面视图是否是代表的同一个视图(即它俩是否是对应的，对应的表示同一个View)
    // Note: 判断出去的view是否等于进来的view 如果为true直接复用
    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
//        KLog.i( "【isViewFromObject】 " + arg1 );
        return arg0 == arg1;
    }

    // 这个函数的功能是创建指定位置的页面视图
    // Note: 做了两件事，第一：将当前视图添加到container中，第二：返回当前View
    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        KLog.i("【instantiateItem】 " + "实例position：" + position);
        final X5WebView webView;
        if (mViewHolderList.size() > 0) {
            webView = mViewHolderList.get(0);
            mViewHolderList.remove(0);
        } else {
            webView = new X5WebView(activity);
            KLog.e("webView：：：" + webView);
        }
        webView.setId(position); // 方便在其他地方调用 viewPager.findViewById 来找到 webView
        webView.setTag(dataList.get(position).getId());
        container.addView(webView);
        artHandler.post(new Runnable() {
            @Override
            public void run() {
                webView.loadDataWithBaseURL(FileUtil.getAbsoluteDir(dataList.get(position).getSaveDir()), StringUtil.getHtmlHeader() + StringUtil.getArticleHtml(dataList.get(position)), "text/html", "utf-8", null);
            }
        });

        KLog.i("生成WebView耗时：" + (System.currentTimeMillis() - App.time));
        return webView;
    }

    // Note: 销毁预加载以外的view对象, 会把需要销毁的对象的索引位置传进来就是position
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        KLog.i("【destroyItem】 " + "当前position：" + position);
        container.removeView((View) object);
        X5WebView webView = (X5WebView) object;
        webView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
        webView.clearHistory();
        mViewHolderList.add(webView);
    }


//    /**
//     * get View
//     */
//    private WebView getView() {
//        WeakReference<BaseActivity> WRArticle = new WeakReference<BaseActivity>(activity);
////        KLog.i( "【getView】 " + position );
//        WebView webView = new WebView( WRArticle.get() );
//        webView.getSettings().setUseWideViewPort(false);// 设置此属性，可任意比例缩放
//        webView.getSettings().setDisplayZoomControls(false); //隐藏webview缩放按钮
//        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN); // 就是这句使自适应屏幕
//        webView.getSettings().setLoadWithOverviewMode(true);// 缩放至屏幕的大小
//        webView.getSettings().setJavaScriptEnabled(true);
//        webView.getSettings().setDomStorageEnabled(true); // Dom Storage（Web Storage）存储，临时简单的缓存
//        webView.getSettings().setAllowFileAccess(true);
//        webView.getSettings().setSupportMultipleWindows(false);
////        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null); // 硬件加速
//
//        // 实现 webview 的背景颜色与当前主题色一致
//        Tool.setBackgroundColor(webView);
//        // webView.setPictureListener();
//        // 添加js交互接口类，并起别名 imagelistner
//        webView.addJavascriptInterface( WRArticle.get(), "imagelistner");
//        // WebViewClient 用于帮助WebView处理各种通知、请求事件(shouldOverrideUrlLoading，onPageStart，onPageFinish，onReceiveError)
//        webView.setWebViewClient( new WebViewClientX() );
//        webView.setDownloadListener( new WebViewDownLoadListener());
////        webView.setWebChromeClient( new MyWebChromeClient() );
//        return webView;
//    }

//    private class WebViewDownLoadListener implements DownloadListener {
//        @Override
//        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
//            KLog.i("tag", "url="+url + "   userAgent="+userAgent + "   contentDisposition="+contentDisposition);
//            KLog.i("tag", "mimetype="+mimetype);
//            KLog.i("tag", "contentLength="+contentLength);
//            Uri uri = Uri.parse(url);
//            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//            App.i().startActivity(intent);
//        }
//    }


//    private class WebViewClientX extends WebViewClient {
//        @Override
//        //  重写此方法表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边
//        public boolean shouldOverrideUrlLoading(WebView view, String url) {
//            KLog.d("==========" + WithSet.i().isSysBrowserOpenLink());
//            if (WithSet.i().isSysBrowserOpenLink()) {
//                Intent intent = new Intent();
//                intent.setAction("android.intent.action.VIEW");
//                Uri content_url = Uri.parse(url);
//                intent.setData(content_url);
//                activity.startActivity(intent);
//                return true;
//            }
//            return super.shouldOverrideUrlLoading(view, url);
//        }

//        /**
//         * 在每次页面加载完数据的时候调用（ loadingPageData 函数）
//         * 你永远无法确定当 WebView 调用这个方法的时候，网页内容是否真的加载完毕了。
//         * 所以当你的WebView需要加载各种各样的网页并且需要在页面加载完成时采取一些操作的话，可能WebChromeClient.onProgressChanged()比WebViewClient.onPageFinished()都要靠谱一些。
//         *
//         * OnPageFinished 事件会在 Javascript 脚本执行完成之后才会触发。如果在页面中使 用JQuery，会在处理完 DOM 对象，执行完 $(document).ready(function() {}); 事件自会后才会渲染并显示页面。而同样的页面在 iPhone 上却是载入相当的快，因为 iPhone 是显示完页面才会触发脚本的执行。所以我们这边的解决方案延迟 JS 脚本的载入
//         */
//        @Override
//        public void onPageFinished(WebView webView, String url) {
//            super.onPageFinished(webView, url);
//            KLog.e("WebView", "onPageFinished   " + (System.currentTimeMillis() - App.time) + webView);
////        webView.getSettings().setBlockNetworkImage(false);
//            // Page渲染完成之后，添加图片的Js点击监听函数
//            webView.loadUrl("javascript:initImgClick()"); // 初始化图片的点击事件
//            initImgPlace( webView );
//        }

//        public void initImgPlace(WebView webView) {
//            final ArrayMap<Integer, Img> imgMap = WithDB.i().getImgs( (String)webView.getTags() );
//            final ArrayMap<Integer, Img> lossImgMap = WithDB.i().getLossImgs( (String)webView.getTags() ); // dataList.get(webView.getId()).getId()
//            int lossImgSize = lossImgMap.size();
//
//            if (lossImgSize == imgMap.size()) {
//                webView.loadUrl("javascript:initImgPlaceholder()"); // 初始化占位图
//            } else {
//                StringBuilder imgNoArray = new StringBuilder("");
//                for (int i = 0; i < lossImgSize; i++) {
//                    imgNoArray.append(lossImgMap.keyAt(i) - 1); // imgState 里的图片下标是从1开始的
//                    imgNoArray.append("_");
//                }
//                KLog.i("传递的值" + imgNoArray + webView);
//                webView.loadUrl("javascript:appointImgPlaceholder(" + "\"" + imgNoArray.toString() + "\"" + ")");
//            }
//        }
//    }


    @Override
    public int getCount() {
        return (null == dataList) ? 0 : dataList.size();
    }

//    private void judgeDirection(int pagePosition) {
//        if (pagePosition > currentPosition) {
//            KLog.e("方向", "右滑");
//        } else if (pagePosition < currentPosition) {
//            KLog.e("方向", "左滑");
//        }
//        currentPosition = pagePosition;
//    }
}
