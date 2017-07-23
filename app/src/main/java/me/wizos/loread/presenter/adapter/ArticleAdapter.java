package me.wizos.loread.presenter.adapter;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;

import com.socks.library.KLog;

import java.util.ArrayList;
import java.util.List;

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
        KLog.e("【onPageSelected】 " + "当前position：" + pagePosition + "  之前position：" + currentPosition + viewPager.findViewById(pagePosition)); //+ this.map.size()
//        KLog.i( dataList.get(pagePosition).getImgState() );
        currentPosition = pagePosition;
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
     * @param pagePosition        当用手指滑动时，如果手指按在页面上不动，position和当前页面index是一致的；
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
        KLog.e("方向", "===================================滑动" + pagePosition + "==" + currentPosition);
        judgeDirection(pagePosition); // TEST:
    }

    private int position = -1;
    public SparseIntArray map = new SparseIntArray();

    private void judgeDirection(final int pagePosition) {
        KLog.e("方向", "滑动" + pagePosition + "==" + currentPosition);

        if (pagePosition < currentPosition || position == -1) {
            KLog.e("方向", "左滑");
            position = pagePosition;
        } else {
            KLog.e("方向", "右滑");
            position = pagePosition + 1;
        }
        if (viewPager.findViewById(position) == null) { // && dataList.get(pagePosition).getImgState()==null
            KLog.e("【judgeDirection】" + "webView 为空");
            artHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    judgeDirection(pagePosition);
                }
            }, 50);
            return;
        }

        if (map.get(position) != 1 && map.get(position) != 2) { //  pagePosition!= currentPosition &&
            KLog.e("加载页面数据" + position + "--" + currentPosition);
            artHandler.post(new Runnable() {
                @Override
                public void run() {
                    X5WebView webView = (X5WebView) viewPager.findViewById(position);
                    webView.loadDataWithBaseURL(FileUtil.getAbsoluteDir(dataList.get(position).getSaveDir()), StringUtil.getHtmlHeader() + StringUtil.getArticleHtml(dataList.get(position)), "text/html", "utf-8", null);
                }
            });
            map.put(position, 1);
        }

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
            KLog.e("webView：" + webView);
        }
        webView.setId(position); // 方便在其他地方调用 viewPager.findViewById 来找到 webView
        webView.setTag(dataList.get(position).getId()); // 方便在webView onPageFinished 的时候调用
        container.addView(webView);
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
        map.put(position, 0);
    }

    @Override
    public int getCount() {
        return (null == dataList) ? 0 : dataList.size();
    }


}
