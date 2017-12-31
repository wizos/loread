package me.wizos.loread.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.socks.library.KLog;

import java.util.ArrayList;
import java.util.List;

import me.wizos.loread.activity.ArticleActivity;
import me.wizos.loread.bean.Article;
import me.wizos.loread.data.WithSet;
import me.wizos.loread.utils.FileUtil;
import me.wizos.loread.utils.StringUtil;
import me.wizos.loread.view.WebViewS;


/**
 * Created by Wizos on 2017/6/4.
 * ArticleActivity 内 ViewPager 的适配器
 */

public class ViewPagerAdapter extends PagerAdapter { //  implements ViewPager.OnPageChangeListener
    private ArticleActivity activity;
    private List<Article> dataList;
    private List<WebViewS> mViewHolderList = new ArrayList<>();

    public ViewPagerAdapter(ArticleActivity context, List<Article> dataList) {
        if (null == dataList || dataList.isEmpty()) return;
        this.activity = context;
        this.dataList = dataList;
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
        final WebViewS webView;
//        if (container.getChildCount() == 1 && !WithSet.i().isLeftRightSlideArticle()) {
//            return null;
//        }
        if (mViewHolderList.size() > 0) {
            webView = mViewHolderList.get(0);
            mViewHolderList.remove(0);
        } else {
            webView = new WebViewS(activity);
            webView.setWebViewClient(new WebViewClientX(activity));
        }
        KLog.e("WebView" + webView);
        webView.setId(position); // 方便在其他地方调用 viewPager.findViewById 来找到 webView
        webView.setTag(dataList.get(position).getId()); // 方便在webView onPageFinished 的时候调用
        container.addView(webView);
        // TODO: 2017/10/15 文章文件被删时候，重新去获取
//        String articleHtml = StringUtil.getArticleHtml(dataList.get(position));
//        if( articleHtml==null){
//            InoApi.i().asyncItemContents(dataList.get(position).getId(), new StringCallback() {
//                @Override
//                public void onSuccess(Response<String> response) {
//                    StreamContents gsItemContents = new Gson().fromJson(response.body().toString(), StreamContents.class);
//                    if( gsItemContents.getItems().size()>0){
//                        Message message = Message.obtain();
//                        Bundle bundle = new Bundle();
//                        bundle.putString("res", gsItemContents.getItems().get(0).getSummary().getContent());
//                        message.what = 121212;
//                        message.setData(bundle);
//                        App.artHandler.sendMessage(message);
//
//                        String fileTitle;
//                        if (dataList.get(position).getSaveDir().equals(Api.SAVE_DIR_CACHE)) {
//                            fileTitle = StringUtil.stringToMD5(dataList.get(position).getId());
//                        } else {
//                            fileTitle = dataList.get(position).getTitle();
//                        }
//                        FileUtil.saveHtml(FileUtil.getRelativeDir(dataList.get(position).getSaveDir()) + fileTitle + ".html", gsItemContents.getItems().get(0).getSummary().getContent());
//                    }
//                }
//            });
//        }
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.loadDataWithBaseURL(FileUtil.getAbsoluteDir(dataList.get(position).getSaveDir()), StringUtil.getHtmlHeader() + StringUtil.getArticleHtml(dataList.get(position)) + StringUtil.getFooter(), "text/html", "utf-8", null);
            }
        });
        return webView;
    }


    // Note: 销毁预加载以外的view对象, 会把需要销毁的对象的索引位置传进来就是position
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (object == null) {
            return;
        }
        container.removeView((View) object);
        ((WebViewS) object).clear();
        mViewHolderList.add((WebViewS) object);
    }

    @Override
    public int getCount() {
        return (null == dataList) ? 0 : dataList.size();
    }


    private class WebViewClientX extends WebViewClient {
        Context context;

        WebViewClientX(Activity activity) {
            context = activity;
        }

        @Override
        //  重写此方法表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (WithSet.i().isSysBrowserOpenLink()) {
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                context.startActivity(Intent.createChooser(intent, "选择打开方式")); // 目前无法坐那那种可以选择打开方式的
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
        public void onPageFinished(final WebView webView, String url) {
            super.onPageFinished(webView, url);
            webView.post(new Runnable() {
                @Override
                public void run() {
                    webView.loadUrl("javascript:initImgClick()"); // 初始化图片的点击事件
                }
            });
//            List<Img> lossImgs = WithDB.i().getLossImgs((String) webView.getTag());
//            for (final Img img:lossImgs) {
//                webView.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        webView.loadUrl("javascript:onImageLoadHolder('" + img.getSrc() + "');");
////                        webView.loadUrl("javascript:initImgClick2DownEvent('" + img.getSrc() + "');");
//                    }
//                });
//            }

            webView.getSettings().setBlockNetworkImage(false);
        }

        @Override
        public void onPageStarted(WebView webView, String var2, Bitmap var3) {
            super.onPageStarted(webView, var2, var3);
            webView.getSettings().setBlockNetworkImage(true);
        }
    }

}
