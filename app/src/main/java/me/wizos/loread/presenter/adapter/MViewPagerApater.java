package me.wizos.loread.presenter.adapter;

/**
 * Created by Wizos on 2017/4/9.
 */

//public class MViewPagerApater extends PagerAdapter implements ViewPager.OnPageChangeListener {
//    private int articleCount;
//    private ArticleActivity context;
//    private MViewPager viewPager;
//    private SparseIntArray lastPageDataIndexMap; //（保存 page 和 data 的对应关系，防止在下次重新加载数据时重复）
//    // 保存每个页面，上次展示的数据编号（如果相同，那么在 loadingPageData() 时就不改变）
//    private int pageIndex, dataIndex, lastPagePosition = 1;
//    private List<Article> dataList;
//    private List<WebView> pageList;
//
//    MViewPagerApater(ArticleActivity context, MViewPager viewPager, List<Article> dataList, int articleNo) {
//        this.context = context;
//        this.viewPager = viewPager;
//        this.dataList = dataList;
//        dataIndex = articleNo - 1;
//        viewPager.clearOnPageChangeListeners();
//        viewPager.addOnPageChangeListener(this);
//        if ( null == dataList || dataList.isEmpty()) return;
//        initPages();
//        loadingPageData(1, dataIndex);
//    }
//
//    /**
//     * 初始化 pager 控件（根据data的数量，设置Page的数量）
//     */
//    private void initPages() {
//        KLog.d("根据data的数量，设置Page的数量" + ", dataSize=" + dataList.size());
//        int pageSize, dataSize = dataList.size();
//        if (dataSize == 0) {
//            // TODO: 2017/2/26 显示无文章占位图
//            return;
//        } else if (dataSize == 1) {
//            pageSize = dataSize;
//        } else if (dataSize == 2) {
//            pageSize = dataSize;
//        } else {
//            pageSize = 5;
//        }
//
//        pageList = new ArrayList<>(pageSize);
//        lastPageDataIndexMap = new SparseIntArray(pageSize);
//        for (int i = 0; i < pageSize; i++) {
//            pageList.add(getView());
//            lastPageDataIndexMap.put(i, -1);
//        }
//        KLog.e("视图的数量2：" + pageList.size());
//    }
//
//    /**
//     * get View
//     */
//    private WebView getView() {
//        WebView webView = new WebView(context);
//        webView.getSettings().setUseWideViewPort(false);// 设置此属性，可任意比例缩放
//        webView.getSettings().setDisplayZoomControls(false); //隐藏webview缩放按钮
//        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN); // 就是这句使自适应屏幕
//        webView.getSettings().setLoadWithOverviewMode(true);// 缩放至屏幕的大小
//        webView.getSettings().setJavaScriptEnabled(true);
//
//        // 添加js交互接口类，并起别名 imagelistner
//        webView.addJavascriptInterface(context, "imagelistner");
//        webView.setWebViewClient( new MWebViewClient( context ) );
////            webView.setWebChromeClient( new MyWebChromeClient() );
//        return webView;
//    }
//
//
//    /**
//     * 这个方法会在屏幕滚动过程中不断被调用。
//     *
//     * @param pagePosition         当用手指滑动时，如果手指按在页面上不动，position和当前页面index是一致的；
//     *                              如果手指向左拖动时（页面向右翻动），position大部分时间和“当前页面”一致，只有翻页成功的情况下最后一次调用才会变为“目标页面”；
//     *                              如果手指向右拖动时（页面向左翻动），position大部分时间和“目标页面”一致，只有翻页不成功的情况下最后一次调用才会变为“原页面”。
//     *                              <p>
//     *                              当直接设置setCurrentItem翻页时，如果是相邻的情况（比如现在是第二个页面，跳到第一或者第三个页面）：
//     *                              如果页面向右翻动，大部分时间是和当前页面是一致的，只有最后才变成目标页面；
//     *                              如果页面向左翻动，position和目标页面是一致的。这和用手指拖动页面翻动是基本一致的。
//     *                              如果不是相邻的情况，比如我从第一个页面跳到第三个页面，position先是0，然后逐步变成1，然后逐步变成2；
//     *                              我从第三个页面跳到第一个页面，position先是1，然后逐步变成0，并没有出现为2的情况。
//     * @param positionOffset       当前页面滑动比例，如果页面向右翻动，这个值不断变大，最后在趋近1的情况后突变为0。如果页面向左翻动，这个值不断变小，最后变为0。
//     * @param positionOffsetPixels 当前页面滑动像素，变化情况和positionOffset一致。
//     */
//    @Override
//    public void onPageScrolled(int pagePosition, float positionOffset, int positionOffsetPixels) {
////            KLog.e( "onPageScrolled" , "position=" +  pagePosition + ", dataIndex=" + dataIndex  + "   " + currentPosition);
//    }
//    private int currentPosition = 0;
//    private void judgeDirection(int pagePosition) {
//        if (pagePosition > currentPosition) {
//            KLog.e("方向", "右滑");
//        } else if (pagePosition < currentPosition) {
//            KLog.e("方向", "左滑");
//        }
//        currentPosition = pagePosition;
//    }
//
//    /**
//     * 加载页面的数据（指定数据到指定的页面）
//     * @param viewIndex 页面的索引（真正那几个页面的索引）
//     * @param dataIndex 数据的索引
//     */
//    private void loadingPageData(int viewIndex, int dataIndex) {
//        KLog.e("加载页面数据loadingPageData：" + viewIndex + "   lastPagePosition=" + lastPageDataIndexMap.get(viewIndex) + "   dataIndex=" + dataIndex);
//        KLog.d("加载文章：" + dataList.get(dataIndex).getTitle() );
//
//        if (lastPageDataIndexMap.get(viewIndex) == dataIndex || dataIndex < 0 || dataIndex > dataList.size() - 1) {
//            KLog.e("相同数据，不更改：" + viewIndex + "   lastPagePosition=" + lastPageDataIndexMap.get(viewIndex) + "   dataIndex=" + dataIndex);
//            return;
//        } else {
//            lastPageDataIndexMap.put(viewIndex, dataIndex);
//        }
//        KLog.i( viewIndex +  "--- " + dataIndex  + "浏览器" +  pageList.get(viewIndex));
////            loadArticle(dataList.get(dataIndex));
////            webView = views.get(viewIndex);
//        String nnn = UString.getHtmlHeader() + getArticleHtml(dataList.get(dataIndex));
//        KLog.e( nnn );
//        pageList.get(viewIndex).loadDataWithBaseURL(UFile.getAbsoluteDir(dataList.get(dataIndex).getSaveDir()), nnn, "text/html", "utf-8", null);
//    }
//
//
//    /**
//     * 获取文章正文（并修饰）
//     * @param article
//     * @return
//     */
//    private String getArticleHtml(Article article) {
//        if (article == null) {
//            // TODO: 2017/2/19  加载没有正文的占位画面
//            UToast.showShort("Article为null");
//            return "";
//        }
//        if (article.getSummary().length() == 0) {
//            // TODO: 2017/2/19  加载没有正文的占位画面
//            UToast.showShort("正文内容为空");
//            return "";
//        }
//
//        // 获取 文章的 fileTitle
//        String fileTitle;
//        if (article.getSaveDir().equals(API.SAVE_DIR_CACHE)) {
//            fileTitle = UString.stringToMD5(article.getId());
//        } else {
//            fileTitle = article.getTitle();
//        }
//
//        String articleHtml = UFile.readHtml(UFile.getRelativeDir(article.getSaveDir()) + fileTitle + ".html");
//
//        if ( articleHtml.length() == 0 ) {
//            // TODO: 2017/2/19  加载等待获取正文的占位画面
//            // TODO: 2017/4/8 重新获取文章正文
//            mNeter.postArticleContents(article.getId());
//        } else if ( article.getImgState() == null ) { // 文章没有被打开过
//            ArrayMap<Integer,Img> lossSrcList = UString.getListOfSrcAndHtml( article.getId(), articleHtml, fileTitle );
//            articleHtml = lossSrcList.get(0).getSrc();
//            articleHtml = UString.getModHtml(article, articleHtml);
//            lossSrcList.remove(0);
//            if ( lossSrcList.size() != 0 ) {
//                article.setCoverSrc(UFile.getAbsoluteDir(API.SAVE_DIR_CACHE) + fileTitle + "_files" + File.separator + lossSrcList.get(1).getName());
//                WithDB.getInstance().saveImgs( lossSrcList );
//                article.setImgState( "0" );
//            } else {
//                article.setImgState("");
//                KLog.d( "为空"  );
//            }
//            KLog.d( "获取文章正文getArticleHtml：" + article.getId()  + lossSrcList );
//            article.setTitle(UString.handleSpecialChar(article.getTitle()));
//
//            String summary = Html.fromHtml(articleHtml).toString(); // 可以去掉标签
//            article.setSummary(UString.getSummary(summary));
//
//            UFile.saveCacheHtml(fileTitle, articleHtml);
//            WithDB.getInstance().saveArticle(article);
//        }
//        return articleHtml;
//    }
//
//    /**
//     * 参数position，代表哪个页面被选中。
//     * 当用手指滑动翻页的时候，如果翻动成功了（滑动的距离够长），手指抬起来就会立即执行这个方法，position就是当前滑动到的页面。
//     * 如果直接setCurrentItem翻页，那position就和setCurrentItem的参数一致，这种情况在onPageScrolled执行方法前就会立即执行。
//     */
//
//    @Override
//    public void onPageSelected(int pagePosition) {
//        KLog.d("==================================================================================================");
//        KLog.d("页面被选中" );
//        KLog.d("当前要展示的页面位置" + pagePosition + "   lastPagePosition=" + lastPagePosition + "   当前要展示的文章序号：" + dataIndex );
//
//        pageIndex = pagePosition;
//        dataIndex = dataIndex + pagePosition - lastPagePosition;
//        lastPagePosition = pagePosition;
//        if (pagePosition == 0) {
//            loadingPageData(2, dataIndex - 1);
//            loadingPageData(4, dataIndex + 1);
//            // 当视图在第一个时，将页面号设置为图片的最后一张。
//            pageIndex = 3;
//            lastPagePosition = 2;
//        } else if (pagePosition == 1) {
//            loadingPageData(0, dataIndex - 1);
//            loadingPageData(2, dataIndex + 1);
//            loadingPageData(3, dataIndex - 1);
//        } else if (pagePosition == 2) {
//            loadingPageData(1, dataIndex - 1);
//            loadingPageData(3, dataIndex + 1);
//        } else if (pagePosition == 3) {
//            loadingPageData(1, dataIndex + 1);
//            loadingPageData(2, dataIndex - 1);
//            loadingPageData(4, dataIndex + 1);
//        } else if (pagePosition == 4) {
//            loadingPageData(0, dataIndex - 1);
//            loadingPageData(2, dataIndex + 1);
//            // 当视图在最后一个是,将页面号设置为图片的第一张。
//            pageIndex = 1;
//            lastPagePosition = 0;
//        }
//        if (pagePosition != pageIndex) {  // 产生跳转了
//            lastPagePosition = pageIndex;
//            viewPager.setCurrentItem(pageIndex, false);
//            KLog.d("lastPagePosition：" + lastPagePosition + "   pageIndex：" + pageIndex);
//        }
//        // 设置页面在第一个数据和最后一个数据时，不可右滑和左滑
//        if (dataIndex == 0) {
//            viewPager.setCallScrollToRight(false);
//        } else if (dataIndex == dataList.size() - 1) {
//            viewPager.setCallScrollToLeft(false);
//        } else {
//            viewPager.setCallScrollToRight(true);
//            viewPager.setCallScrollToLeft(true);
//        }
//        showingPageData( dataList.get(dataIndex), pageList.get(pageIndex) );
////            ArticleActivity.this.webView = views.get(pageIndex);
////            KLog.d(  "fileTitle：" +  fileTitle + "   fileTitle：" + fileTitle   );
//        KLog.d("pagePosition：" + pagePosition + "   pageIndex：" + pageIndex);
//    }
//
//
//    /**
//     * state 有三种取值：
//     * 0：什么都没做
//     * 1：开始滑动
//     * 2：滑动结束
//     * 滑动过程的顺序，从滑动开始依次为：(1,2,0)
//     */
//    @Override
//    public void onPageScrollStateChanged(int state) {
////            KLog.e( "onPageScrollStateChanged" ,  "currentPage=" + currentPage  );
//    }
//
//    @Override
//    public int getCount() {
//        return (null == pageList) ? 0 : pageList.size();
//    }
//
//    /**
//     * 判断出去的view是否等于进来的view 如果为true直接复用
//     */
//    @Override
//    public boolean isViewFromObject(View arg0, Object arg1) {
//        return arg0 == arg1;
//    }
//
//    /**
//     * 做了两件事，第一：将当前视图添加到container中，第二：返回当前View
//     */
//    @Override
//    public Object instantiateItem(ViewGroup container, int position) {
////        KLog.d("【instantiateItem】适配器总数：" + getCount() + "，View 总数：" +  views.size()  + "，当前position："  + position  + "，index："  + "，view对象：" + views.get(position) );
//        if (null != pageList.get(position).getParent()) {
//            ViewGroup viewGroup = (ViewGroup) pageList.get(position).getParent();
//            viewGroup.removeView(pageList.get(position));
//        }
//        container.addView(pageList.get(position));
//        return pageList.get(position);
//    }
//
//    /**
//     * 销毁预加载以外的view对象, 会把需要销毁的对象的索引位置传进来就是position
//     */
//    @Override
//    public void destroyItem(ViewGroup container, int position, Object object) {
////        KLog.d("【destroyItem】适配器总数：" + getCount() + "，View 总数：" +  views.size()  + "，当前position："  + position  + "，index："  + index + "，view对象：" + views.get(position) );
//        container.removeView((View) object);
//    }
//}
