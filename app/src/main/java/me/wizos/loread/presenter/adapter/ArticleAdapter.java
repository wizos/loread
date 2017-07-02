package me.wizos.loread.presenter.adapter;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.socks.library.KLog;

import java.util.ArrayList;
import java.util.List;

import me.wizos.loread.App;
import me.wizos.loread.activity.ArticleActivity;
import me.wizos.loread.bean.Article;
import me.wizos.loread.presenter.WebViewClientX;
import me.wizos.loread.utils.Tool;
import me.wizos.loread.utils.UFile;
import me.wizos.loread.utils.UString;

/**
 * Created by Wizos on 2017/6/4.
 */

public class ArticleAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener {
    private ArticleActivity activity;

    private List<Article> dataList;
    //    public SparseArray<WebView> map = new SparseArray();
    private List<WebView> mViewHolderList = new ArrayList<>();
    private ViewPager viewPager;

    public ArticleAdapter(ArticleActivity context, ViewPager viewPager, List<Article> dataList) {
        if (null == dataList || dataList.isEmpty()) return;
        this.activity = context;
        this.dataList = dataList;

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
     * 这个函数会比 InstantiateItem 函数先执行
     */
    @Override
    public void onPageSelected(int pagePosition) {
        KLog.i("【onPageSelected】 " + "当前position：" + pagePosition + "  大小："); //+ this.map.size()
        currentPosition = pagePosition;
//        activity.showingPageData( dataList.get(pagePosition), (WebView) viewPager.findViewById(pagePosition) , pagePosition ); // this.map.get(currentPosition)
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
    public Object instantiateItem(ViewGroup container, int position) {
        KLog.i("【instantiateItem】 " + "实例position：" + position);
        WebView webView;
        if (mViewHolderList.size() > 0) {
            webView = mViewHolderList.get(0);
            mViewHolderList.remove(0);
            webView.setId(position);
            container.addView(webView);
            webView.loadDataWithBaseURL(UFile.getAbsoluteDir(dataList.get(position).getSaveDir()), UString.getHtmlHeader() + UString.getArticleHtml(dataList.get(position)), "text/html", "utf-8", null);
        } else {
            webView = getView();
            webView.setId(position);
            container.addView(webView);
            webView.loadDataWithBaseURL(UFile.getAbsoluteDir(dataList.get(position).getSaveDir()), UString.getHtmlHeader() + UString.getArticleHtml(dataList.get(position)), "text/html", "utf-8", null);
            if (position == currentPosition) {
                activity.showingPageData(dataList.get(position), webView, position);
            }
        }
////        WebView webView = getView();
//        webView.setId(position);
//        container.addView( webView );
//        webView.loadDataWithBaseURL(UFile.getAbsoluteDir(dataList.get(position).getSaveDir()), UString.getHtmlHeader() + UString.getArticleHtml(dataList.get(position)), "text/html", "utf-8", null);
////        this.map.put(position, webView);
////         该函数目的是想要在实例话当前页的时候，去展示当前页的数据（本来是可以直接放在onPageSelected中的，不过这样第一次进viewpager的时候，不会调用onPageSelected函数从而执行showingPageData函数）

        return webView;
    }

    // Note: 销毁预加载以外的view对象, 会把需要销毁的对象的索引位置传进来就是position
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        KLog.i("【destroyItem】 " + "当前position：" + position);
        container.removeView((View) object);
        WebView webView = (WebView) object;
        webView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
//        webView.removeAllViews();
        webView.clearHistory();
//        webView.setWebViewClient(null);
//        webView.destroy();
        mViewHolderList.add(webView);

//        if ( this.map.get(position)  != null) {
//            this.map.get(position).loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
//            this.map.get(position).removeAllViews();
//            this.map.get(position).clearHistory();
//            this.map.get(position).setWebViewClient(null);
//            this.map.get(position).destroy();
//            this.map.remove(position);
//        }
    }


    /**
     * get View
     */
    private WebView getView() {
//        KLog.i( "【getView】 " + position );
        WebView webView = new WebView(App.getInstance());
        webView.getSettings().setUseWideViewPort(false);// 设置此属性，可任意比例缩放
        webView.getSettings().setDisplayZoomControls(false); //隐藏webview缩放按钮
        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN); // 就是这句使自适应屏幕
        webView.getSettings().setLoadWithOverviewMode(true);// 缩放至屏幕的大小
        webView.getSettings().setJavaScriptEnabled(true);

        // 实现 webview 的背景颜色与当前主题色一致
        Tool.setBackgroundColor(webView);

//        webView.setPictureListener();
        // 添加js交互接口类，并起别名 imagelistner
        webView.addJavascriptInterface(activity, "imagelistner");
        // WebViewClient 用于帮助WebView处理各种通知、请求事件(shouldOverrideUrlLoading，onPageStart，onPageFinish，onReceiveError)
        webView.setWebViewClient(new WebViewClientX(activity));
//        webView.setWebChromeClient( new MyWebChromeClient() );
        return webView;
    }


    @Override
    public int getCount() {
        return (null == dataList) ? 0 : dataList.size();
    }

//    int currentPosition = 0;
//    private void judgeDirection(int pagePosition) {
//        if (pagePosition > currentPosition) {
//            KLog.e("方向", "右滑");
//        } else if (pagePosition < currentPosition) {
//            KLog.e("方向", "左滑");
//        }
//        currentPosition = pagePosition;
//    }
}
