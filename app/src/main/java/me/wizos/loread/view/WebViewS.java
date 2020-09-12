package me.wizos.loread.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebSettings;

import com.socks.library.KLog;

import me.wizos.loread.App;
import me.wizos.loread.view.webview.NestedScrollWebView;

/**
 * @author Wizos on 2017/7/3.
 * NestedScroll
 */


public class WebViewS extends NestedScrollWebView {
    private boolean isReadability = false;
//    private int downX,downY;

    @SuppressLint("NewApi")
    public WebViewS(final Context context) {
        // 传入 application activity 来防止 activity 引用被滥用。
        // 创建 WebView 传的是 Application ， Application 本身是无法弹 Dialog 的 。 所以只能无反应 ！
        // 这个问题解决方案只要你创建 WebView 时候传入 Activity ， 或者 自己实现 onJsAlert 方法即可。
        super(context);
        initSettingsForWebPage();
        // 获取手指点击事件的xy坐标
//        setOnTouchListener( new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View arg0, MotionEvent arg1) {
//                downX = (int) arg1.getX();
//                downY = (int) arg1.getY();
//                return false;
//            }
//        });
//        作者：Wing_Li
//        链接：https://www.jianshu.com/p/3fcf8ba18d7f
//        setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                if (!BuildConfig.DEBUG) {
//                    return false;
//                }
//                final HitTestResult status = ((WebViewS) v).getHitTestResult();
//                if (null == status) {
//                    return false;
//                }
//                int type = status.getType();
//                if (type == WebView.HitTestResult.UNKNOWN_TYPE) {
//                    return false;
//                }
//
//                // 这里可以拦截很多类型，我们只处理图片类型就可以了
//                switch (type) {
//                    case WebView.HitTestResult.PHONE_TYPE: // 处理拨号
//                        Tool.show("长按手机号");
//                        break;
//                    case WebView.HitTestResult.EMAIL_TYPE: // 处理Email
//                        Tool.show("长按邮件");
//                        break;
//                    case WebView.HitTestResult.GEO_TYPE: // 地图类型
//                        Tool.show("长按地图");
//                        break;
//                    case WebView.HitTestResult.SRC_ANCHOR_TYPE: // 超链接
//                        final LongClickPopWindow webViewLongClickedPopWindow = new LongClickPopWindow(context,WebView.HitTestResult.SRC_ANCHOR_TYPE, ScreenUtil.dp2px(context,120), ScreenUtil.dp2px(context,90));
//                        webViewLongClickedPopWindow.showAtLocation(v, Gravity.TOP|Gravity.LEFT, downX, downY + 10);
//
//                        webViewLongClickedPopWindow.getView(R.id.webview_copy_link)
//                                .setOnClickListener(new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View v) {
//                                        webViewLongClickedPopWindow.dismiss();
//                                        //获取剪贴板管理器：
//                                        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
//                                        // 创建普通字符型ClipData
//                                        ClipData mClipData = ClipData.newPlainText("url", status.getExtra());
//                                        // 将ClipData内容放到系统剪贴板里。
//                                        cm.setPrimaryClip(mClipData);
//                                        ToastUtil.showLong("复制成功");
//                                    }
//                                });
//                        webViewLongClickedPopWindow.getView(R.id.webview_share_link)
//                                .setOnClickListener(new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View v) {
//                                        webViewLongClickedPopWindow.dismiss();
//                                        Intent sendIntent = new Intent(Intent.ACTION_SEND);
//                                        sendIntent.setType("text/plain");
//                                        sendIntent.putExtra(Intent.EXTRA_TEXT, status.getExtra());
//                                        sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                        context.startActivity(Intent.createChooser(sendIntent, "分享到"));
//                                    }
//                                });
//
//
////                        SnackbarUtil.Long(WebViewS.this.getRootView(), "链接：" + status.getExtra())
////                                .setAction("复制", new View.OnClickListener() {
////                                    @Override
////                                    public void onClick(View v) {
////                                        //获取剪贴板管理器：
////                                        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
////                                        // 创建普通字符型ClipData
////                                        ClipData mClipData = ClipData.newPlainText("url", status.getExtra());
////                                        // 将ClipData内容放到系统剪贴板里。
////                                        cm.setPrimaryClip(mClipData);
////                                        ToastUtil.showLong("复制成功");
////                                    }
////                                }).show();
//                        break;
//                    case WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE: // 一个SRC_IMAGE_ANCHOR_TYPE类型表明了一个拥有图片为子对象的超链接。
//                        Tool.show("长按图片：" + status.getExtra());
//                        break;
//                    case WebView.HitTestResult.IMAGE_TYPE: // 处理长按图片的菜单项
//                        // 获取图片的路径
//                        String saveImgUrl = status.getExtra();
//                        Tool.show("长按图片：" + saveImgUrl);
//                        // 跳转到图片详情页，显示图片
//                        break;
//                    default:
//                        break;
//                }
//                return true;
//            }
//        });
    }


    public boolean isReadability() {
        return isReadability;
    }

    public void setReadability(boolean readability) {
        isReadability = readability;
    }

    // 忽略 SetJavaScriptEnabled 的报错
    @SuppressLint("SetJavaScriptEnabled")
    private void initSettingsForWebPage() {
        setHorizontalScrollBarEnabled(false);
        setVerticalScrollBarEnabled(false);
        setScrollbarFadingEnabled(false);

        // 先设置背景色为tranaparent 透明色
        setBackgroundColor(0);
        WebSettings webSettings = this.getSettings();

        webSettings.setTextZoom(100);
        // 设置最小的字号，默认为8
        webSettings.setMinimumFontSize(10);
        // 设置最小的本地字号，默认为8
        webSettings.setMinimumLogicalFontSize(10);

        // 设置自适应屏幕，两者合用
        // 设置使用 宽 的 Viewpoint,默认是false
        // Android browser以及chrome for Android的设置是`true`，而WebView的默认设置是`false`
        // 如果设置为`true`,那么网页的可用宽度为`980px`,并且可以通过 meta data来设置，如果设置为`false`,那么可用区域和WebView的显示区域有关.
        // 设置此属性，可任意比例缩放
        webSettings.setUseWideViewPort(true);
        // 缩放至屏幕的大小：如果webview内容宽度大于显示区域的宽度,那么将内容缩小,以适应显示区域的宽度, 默认是false
        webSettings.setLoadWithOverviewMode(true);
        // NARROW_COLUMNS 适应内容大小 ， SINGLE_COLUMN 自适应屏幕
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);

        //缩放操作
        webSettings.setSupportZoom(true); //支持缩放，默认为true。是下面那个的前提。
        webSettings.setBuiltInZoomControls(true); //设置内置的缩放控件。若为false，则该WebView不可缩放
        webSettings.setDisplayZoomControls(false); //隐藏原生的缩放控件


        webSettings.setDefaultTextEncodingName("UTF-8");

        webSettings.setJavaScriptEnabled(true);
        // 支持通过js打开新的窗口
        webSettings.setJavaScriptCanOpenWindowsAutomatically(false);

        /* 缓存 */
        webSettings.setDomStorageEnabled(true); // 临时简单的缓存（必须保留，否则无法播放优酷视频网页，其他的可以）
        webSettings.setAppCacheEnabled(true); // 支持H5的 application cache 的功能
        webSettings.setDatabaseEnabled(true);  // 支持javascript读写db
        /* 根据cache-control获取数据 */
//        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        // 通过 file url 加载的 Javascript 读取其他的本地文件 .建议关闭
        webSettings.setAllowFileAccessFromFileURLs(false);
        // 允许通过 file url 加载的 Javascript 可以访问其他的源，包括其他的文件和 http，https 等其他的源
        webSettings.setAllowUniversalAccessFromFileURLs(false);
        // 允许访问文件
        webSettings.setAllowFileAccess(true);
        // 保存密码数据
        webSettings.setSavePassword(true);
        // 保存表单数据
        webSettings.setSaveFormData(true);

        CookieManager instance = CookieManager.getInstance();
        instance.setAcceptCookie(true);
        instance.setAcceptThirdPartyCookies(this, true);
        // 允许在Android 5.0上 Webview 加载 Http 与 Https 混合内容。作者：Wing_Li，链接：https://www.jianshu.com/p/3fcf8ba18d7f
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);


        webSettings.setMediaPlaybackRequiresUserGesture(true);

        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);

        setLayerType(View.LAYER_TYPE_HARDWARE, null);
//        setLayerType(View.LAYER_TYPE_SOFTWARE, null);//开启软件加速（在我的MX5上，滑动时会卡顿）

        // 设置WebView是否应加载图像资源。请注意，此方法控制所有图像的加载，包括使用数据URI方案嵌入的图像。
        webSettings.setLoadsImagesAutomatically(true);
        // 将图片下载阻塞，然后在浏览器的OnPageFinished事件中设置webView.getSettings().setBlockNetworkImage(false);
        // 通过图片的延迟载入，让网页能更快地显示。但是会造成懒加载失效，因为懒加载的脚本执行更早，所以认为所有未显示的图片都在同一屏幕内，要开始加载。
//        webSettings.setBlockNetworkImage(true);

//        webSettings.setSupportMultipleWindows(true);
//        webSettings.setGeolocationEnabled(true);
//        webSettings.setAppCacheMaxSize(Long.MAX_VALUE);
//        webSettings.setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);
//        webSettings.setPluginState(WebSettings.PluginState.ON_DEMAND);
        // this.getSettingsExtension().setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);//extension
//        this.evaluateJavascript();//  Android 4.4之后使用evaluateJavascript调用有返回值的JS方法
    }


    @Override
    public void destroy() {
        // 链接：http://www.jianshu.com/p/3e8f7dbb0dc7
        // 如果先调用destroy()方法，则会命中if (isDestroyed()) return;这一行代码，需要先onDetachedFromWindow()，再 destory()。
        // 在关闭了Activity时，如果Webview的音乐或视频，还在播放。就必须销毁Webview。
        // 但注意：webview调用destory时，仍绑定在Activity上，这是由于webview构建时传入了Activity对象。
        // 因此需要先从父容器中移除webview，然后再销毁webview。

//        作者：听话哥
//        链接：https://www.jianshu.com/p/9293505c7f71
//        如果你在调用webview.destory();的时候，如果webview里面还有别的线程在操作，就会导致当前这个webview为空。这时候我们需要结束相应线程。例如我们项目中有一个广告拦截是通过在
//        public void onPageFinished(final WebView view, String url)
//        里面启用一个runnable去执行一串的js脚本，如果用户在你脚本没执行完成的时候就关闭了当前界面，系统就会抛出空指针异常。这时候就需要通过去onPageFinished获取webview对象

//        setVisibility(View.GONE);
        try {
            stopLoading();
            onPause();
            CookieManager.getInstance().flush();

            // 退出时调用此方法，移除绑定的服务，否则某些特定系统会报错
            getSettings().setJavaScriptEnabled(false);
//            clearFormData(); // 清除自动完成填充的表单数据。需要注意的是，该方法仅仅清除当前表单域自动完成填充的表单数据，并不会清除WebView存储到本地的数据。
            clearMatches(); // 清除网页查找的高亮匹配字符。
            clearSslPreferences(); //清除ssl信息。
            clearDisappearingChildren();
            clearAnimation();

            clearHistory();
//            clearCache(true);
//            destroyDrawingCache(); // 貌似影响内存回收
            removeAllViews();
            // removeAllViewsInLayout(); 相比而言, removeAllViews() 也调用了removeAllViewsInLayout(), 但是后面还调用了requestLayout(),这个方法是当View的布局发生改变会调用它来更新当前视图, 移除子View会更加彻底. 所以除非必要, 还是推荐使用removeAllViews()这个方法.
            ViewGroup parent = (ViewGroup) this.getParent();
            if (parent != null) {
                parent.removeView(this);
            }
            setWebChromeClient(null);
            // 将缓存中的cookie数据同步到数据库。此调用将阻塞调用者，直到它完成并可能执行I/O。
        } catch (Exception e) {
            KLog.e("报错");
            e.printStackTrace();
        }
        super.destroy();
    }


    /**
     * 不要将 base url 设为 null
     * 4.4以上版本，使用evaluateJavascript方法调js方法，会有返回值
     */
    public void loadData(String htmlContent) {
//        stopLoading();
//        getSettings().setBlockNetworkImage(true); // 将图片下载阻塞
        loadDataWithBaseURL(App.i().getWebViewBaseUrl(), htmlContent, "text/html", "UTF-8", null);
        isReadability = false;
    }


    @Override
    protected void onScrollChanged(final int l, final int t, final int oldl,
                                   final int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mOnScrollChangedCallback != null) {
            mOnScrollChangedCallback.onScrollY(t - oldt);
        }
    }

    public OnScrollChangedCallback getOnScrollChangedCallback() {
        return mOnScrollChangedCallback;
    }

    public void setOnScrollChangedCallback(
            final OnScrollChangedCallback onScrollChangedCallback) {
        mOnScrollChangedCallback = onScrollChangedCallback;
    }

    private OnScrollChangedCallback mOnScrollChangedCallback;

    /**
     * Impliment in the activity/fragment/view that you want to listen to the webview
     */
    public interface OnScrollChangedCallback {
        void onScrollY(int dy);
    }


}
