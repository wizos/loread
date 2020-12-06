package me.wizos.loread.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.MutableContextWrapper;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.carlt.networklibs.NetType;
import com.carlt.networklibs.utils.NetworkUtils;
import com.elvishew.xlog.XLog;
import com.hjq.toast.ToastUtils;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.shinichi.library.ImagePreview;
import cc.shinichi.library.view.listener.OnBigImageLongClickListener;
import me.wizos.loread.App;
import me.wizos.loread.BuildConfig;
import me.wizos.loread.Contract;
import me.wizos.loread.R;
import me.wizos.loread.bridge.ArticleBridge;
import me.wizos.loread.config.AdBlock;
import me.wizos.loread.config.ArticleTags;
import me.wizos.loread.config.LinkRewriteConfig;
import me.wizos.loread.config.NetworkRefererConfig;
import me.wizos.loread.config.NetworkUserAgentConfig;
import me.wizos.loread.config.SaveDirectory;
import me.wizos.loread.db.Article;
import me.wizos.loread.db.ArticleTag;
import me.wizos.loread.db.Category;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.Feed;
import me.wizos.loread.db.Tag;
import me.wizos.loread.extractor.Distill;
import me.wizos.loread.network.HttpClientManager;
import me.wizos.loread.network.callback.CallbackX;
import me.wizos.loread.utils.ArticleUtil;
import me.wizos.loread.utils.EncryptUtil;
import me.wizos.loread.utils.FileUtil;
import me.wizos.loread.utils.ImageUtil;
import me.wizos.loread.utils.ScreenUtil;
import me.wizos.loread.utils.SnackbarUtil;
import me.wizos.loread.utils.StringUtils;
import me.wizos.loread.utils.UriUtil;
import me.wizos.loread.view.IconFontView;
import me.wizos.loread.view.SwipeRefreshLayoutS;
import me.wizos.loread.view.WebViewS;
import me.wizos.loread.view.colorful.Colorful;
import me.wizos.loread.view.slideback.SlideBack;
import me.wizos.loread.view.slideback.SlideLayout;
import me.wizos.loread.view.slideback.callback.SlideCallBack;
import me.wizos.loread.view.webview.DownloadListenerS;
import me.wizos.loread.view.webview.LongClickPopWindow;
import me.wizos.loread.view.webview.SlowlyProgressBar;
import me.wizos.loread.view.webview.VideoImpl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import top.zibin.luban.CompressionPredicate;
import top.zibin.luban.InputStreamProvider;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;
import top.zibin.luban.OnRenameListener;

import static me.wizos.loread.Contract.SCHEMA_FILE;
import static me.wizos.loread.Contract.SCHEMA_HTTP;
import static me.wizos.loread.Contract.SCHEMA_HTTPS;


/**
 * @author Wizos on 2017
 */
@SuppressWarnings("unchecked")
@SuppressLint("SetJavaScriptEnabled")
public class ArticleActivity extends BaseActivity implements ArticleBridge {
    private static final String TAG = "ArticleActivity";
    private SwipeRefreshLayoutS swipeRefreshLayoutS;
    private SlowlyProgressBar slowlyProgressBar;
    private IconFontView starView, readView, saveView, readabilityView;
    private WebViewS selectedWebView;
    // private MirrorSwipeBackLayout entryView;
    // private RefreshLayout entryView;
    private FrameLayout entryView;
    private SlideLayout slideLayout;
    private Toolbar toolbar;
    private RelativeLayout bottomBar;
    private VideoImpl video;

    private Article selectedArticle;
    private int articleNo;
    private String articleId;

    private OkHttpClient imgHttpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        Bundle bundle;
        if (savedInstanceState != null) {
            bundle = savedInstanceState;
            App.i().articleProgress.put(bundle.getString("articleId"), bundle.getInt("articleProgress"));
        } else {
            bundle = getIntent().getExtras();
        }
        // setSelection 没有滚动效果，直接跳到指定位置。smoothScrollToPosition 有滚动效果的
        // 文章在列表中的位置编号，下标从 0 开始
        articleNo = bundle.getInt("articleNo");
        // 列表中所有的文章数目
        // articleCount = bundle.getInt("articleCount");
        articleId = bundle.getString("articleId");

        // XLog.e("开始初始化数据2" + articleNo + "==" + articleCount + "==" + articleId + " == " + articleIDs );
        initToolbar();
        initView(); // 初始化界面上的 View，将变量映射到布局上。
        initSelectedArticle(articleNo);
        imgHttpClient = HttpClientManager.i().imageHttpClient();
    }

    public static Handler articleHandler = new Handler();

    @Override
    public void onResume() {
        selectedWebView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        selectedWebView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        saveArticleProgress();
        OkGo.cancelAll(imgHttpClient);
        if(distill != null){
            distill.cancel();
        }
        // XLog.e("onDestroy：" + selectedWebView);
        // 如果参数为null的话，会将所有的Callbacks和Messages全部清除掉。
        // 这样做的好处是在 Acticity 退出的时候，可以避免内存泄露。因为 handler 内可能引用 Activity ，导致 Activity 退出后，内存泄漏。
        articleHandler.removeCallbacksAndMessages(null);
        entryView.removeAllViews();
        selectedWebView.destroy();
        selectedWebView = null;
        super.onDestroy();
    }

    public int saveArticleProgress() {
        if (selectedWebView == null) {
            return 0;
        }
        int scrollY = selectedWebView.getScrollY();
        App.i().articleProgress.put(articleId, scrollY);
        return scrollY;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("articleId", articleId);
        outState.putInt("articleNo", articleNo);
        outState.putInt("articleCount", 1);
        outState.putInt("articleProgress", saveArticleProgress());
        outState.putInt("theme", App.i().getUser().getThemeMode());
        //XLog.i("自动保存：" + articleNo + "==" + "==" + articleId);
        super.onSaveInstanceState(outState);
    }


    @JavascriptInterface
    @Override
    public void log(String paramString) {
        XLog.d("【log】" + paramString);
    }

    @JavascriptInterface
    @Override
    public void show(String msg){
        ToastUtils.show(msg);
    }

    @JavascriptInterface
    @Override
    public void readImage(String articleId, String imgId, String originalUrl) {
        String cacheUrl = FileUtil.readCacheFilePath(EncryptUtil.MD5(articleId), originalUrl);
        XLog.d("加载图片 - 缓存地址：" + cacheUrl);
        articleHandler.post(new Runnable() {
            @Override
            public void run() {
                if (TextUtils.isEmpty(cacheUrl)) {
                    if (!NetworkUtils.isAvailable()) {
                        selectedWebView.loadUrl("javascript:setTimeout( onImageLoadFailed('" + imgId + "'),1 )");
                    } else if (App.i().getUser().isDownloadImgOnlyWifi() && !NetworkUtils.getNetType().equals(NetType.WIFI)) {
                        selectedWebView.loadUrl("javascript:setTimeout( onImageLoadNeedClick('" + imgId + "'),1 )");
                    }else {
                        selectedWebView.loadUrl("javascript:setTimeout( onImageLoading('" + imgId + "'),1 )");
                        downImage(articleId, imgId, originalUrl, false);
                    }
                }else {
                    if(ImageUtil.isImgOrSvg(new File(cacheUrl))){
                        selectedWebView.loadUrl("javascript:setTimeout( onImageLoadSuccess('" + imgId + "','" + cacheUrl + "'),1)");
                    }else {
                        selectedWebView.loadUrl("javascript:setTimeout( onImageError('" + imgId + "'),1 )");
                        XLog.d("加载图片 - 缓存文件读取失败：不是图片");
                    }
                }
            }
        });
    }

    @JavascriptInterface
    @Override
    public String read(String articleId, String imgId, String originalUrl) {
        String cacheUrl = FileUtil.readCacheFilePath(EncryptUtil.MD5(articleId), originalUrl);
        if (TextUtils.isEmpty(cacheUrl)) {
            if (!NetworkUtils.isAvailable()) {
                return "IMAGE_HOLDER_LOAD_FAILED_URL";
            } else if (App.i().getUser().isDownloadImgOnlyWifi() && !NetworkUtils.getNetType().equals(NetType.WIFI)) {
                return "IMAGE_HOLDER_CLICK_TO_LOAD_URL";
            }else {
                downImage(articleId, imgId, originalUrl, false);
                return "IMAGE_HOLDER_LOADING_URL";
            }
        }else {
            if(ImageUtil.isImgOrSvg(new File(cacheUrl))){
                return cacheUrl;
            }else {
                XLog.e("加载图片", "缓存文件读取失败：不是图片");
                return "IMAGE_HOLDER_IMAGE_ERROR_URL";
            }
        }
    }

    @JavascriptInterface
    @Override
    public void openImage(String articleId, String imageFilePath) {
        XLog.d("打开图片：" + imageFilePath + "  " );
        // 如果是 svg 格式的图片则点击无反应
        if(imageFilePath.endsWith(".svg")){
            return;
        }

        // 如果传入的是缩略图的文件地址，则替换为大图的
        Pattern P_COMPRESSED = Pattern.compile(this.getPackageName() + "/files/(.*?)/compressed/", Pattern.CASE_INSENSITIVE);
        Matcher m = P_COMPRESSED.matcher(imageFilePath);
        if (m.find()) {
            String id = m.group(1);
            imageFilePath = m.replaceFirst(this.getPackageName() + "/files/" + id + "/original/");
        }

        final String imgUri = imageFilePath;

        ImagePreview.getInstance()
                // 上下文，必须是activity，不需要担心内存泄漏，本框架已经处理好；
                .setContext(ArticleActivity.this)
                // 设置从第几张开始看（索引从0开始）
                .setIndex(0)
                //=================================================================================================
                // 有三种设置数据集合的方式，根据自己的需求进行三选一：
                // 1：第一步生成的imageInfo List
                // .setImageInfoList(imageInfoList)
                // 2：直接传url List
                //.setImageList(List<String> imageList)
                // 3：只有一张图片的情况，可以直接传入这张图片的url
                .setImage(imgUri)

                // 保存的文件夹名称，会在SD卡根目录进行文件夹的新建。
                // (你也可设置嵌套模式，比如："BigImageView/Download"，会在SD卡根目录新建BigImageView文件夹，并在BigImageView文件夹中新建Download文件夹)
                .setFolderName(getString(R.string.app_name))
                .setLoadStrategy(ImagePreview.LoadStrategy.AlwaysOrigin)
                // 缩放动画时长，单位ms
                // .setZoomTransitionDuration(300)
                // 是否启用上拉/下拉关闭。默认不启用
                .setEnableDragClose(true)
                // 长按回调
                .setBigImageLongClickListener(new OnBigImageLongClickListener() {
                    @Override
                    public boolean onLongClick(Activity activity, View view, int position) {
                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_SEND);
                        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(imgUri));
                        shareIntent.setType("image/*");

                        //shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_img));
                        //shareIntent.putExtra(Intent.EXTRA_TEXT,getString(R.string.share_img));
                        //shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_img)));
                        return false;
                    }
                })
                // 开启预览
                .start();
    }

    private static class MyCompressionPredicate implements CompressionPredicate {
        @Override
        public boolean apply(String preCompressedPath, InputStreamProvider path) {
            // XLog.e("检测是否要压缩图片：" + preCompressedPath);
            try {
                if (preCompressedPath.toLowerCase().endsWith(".gif")) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 1;
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(path.open(), null, options);
                    // XLog.d("压缩图片，忽略压缩：" + preCompressedPath + options.outWidth );
                    return options.outWidth >= 300 || options.outHeight >= 300;
                } else {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    private static class DownFileCallback extends FileCallback {
        private WeakReference<Context> weakReferenceContext;
        private WeakReference<WebViewS> selectedWebView;
        private String originalFileDir;
        private String fileNameExt;
        private String compressedFileDir;
        private String imgId;

        private String imageUrl;
        private String articleUrl;
        private boolean guessReferer;

        DownFileCallback(String destFileDir, String destFileName) {
            super(destFileDir, destFileName + ".tmp");
            this.originalFileDir = destFileDir;
            this.imgId = destFileName;
        }

        void setParam(Context context, WebViewS webView, String compressedFileDir, String fileNameExt, boolean guessReferer) {
            this.weakReferenceContext = new WeakReference<Context>(context);
            this.selectedWebView = new WeakReference<WebViewS>(webView);
            this.compressedFileDir = compressedFileDir;
            this.fileNameExt = fileNameExt;
            this.guessReferer = guessReferer;
        }

        void setRefererParam(String originalUrl, String articleUrl, boolean guessReferer) {
            this.imageUrl = originalUrl;
            this.articleUrl = articleUrl;
            this.guessReferer = guessReferer;
        }

        @Override
        public void onSuccess(Response<File> response) {
            MediaType mediaType = response.getRawResponse().body().contentType();
            boolean renameToFileNameForSvg = false;
            if( mediaType != null && mediaType.subtype().contains("svg") && !fileNameExt.endsWith(".svg") ){
                fileNameExt = fileNameExt + ".svg";
                renameToFileNameForSvg = true;
            }
            File tmpOriginalFile = response.body();
            if(!ImageUtil.isImgOrSvg(tmpOriginalFile)){
                tmpOriginalFile.delete();
                // tmpOriginalFile.renameTo(new File(originalFileDir + imgId + ".error"));
                if (selectedWebView.get() != null && !selectedWebView.get().isDestroyed()) {
                    selectedWebView.get().loadUrl("javascript:setTimeout( onImageError('" + imgId + "'),1)");
                }
                return;
            }else if(guessReferer){ // 当是根据系统自动猜得的referer而成功下载到图片时，保存自动识别的refer而规则
                NetworkRefererConfig.i().addReferer(imageUrl, articleUrl);
            }

            if(renameToFileNameForSvg){
                File targetOriginalFile = new File(originalFileDir + fileNameExt);
                // 可能存在图片的文件名相同，但是实际是不同图片的情况。
                if(targetOriginalFile.exists() && tmpOriginalFile.length() != targetOriginalFile.length()){
                   fileNameExt = imgId + "_" + fileNameExt;
                   targetOriginalFile = new File(originalFileDir + fileNameExt);
                }

                if(tmpOriginalFile.renameTo(targetOriginalFile)){
                    // XLog.i("改名成功：" + tmpOriginalFile.getAbsolutePath() + ", " + targetOriginalFile.getAbsolutePath() );
                    tmpOriginalFile = targetOriginalFile;
                }
            }else {
                File targetOriginalFile = new File(originalFileDir + imgId);
                if(tmpOriginalFile.renameTo(targetOriginalFile)){
                    tmpOriginalFile = targetOriginalFile;
                }
            }

            File downloadedOriginalFile = tmpOriginalFile;

            // XLog.i("下载图片成功，准备压缩：" + originalFileDir + fileNameExt + " or " + imgId + " = " + imageUrl );

            Luban.with(App.i())
                    //.load(targetOriginalFile)
                    .load(downloadedOriginalFile)
                    .ignoreBy(100) // 忽略100kb以下的文件
                    // 缓存压缩图片路径
                    // .setTargetPath(compressedFileDir + fileNameExt)
                    .setTargetDir(compressedFileDir)
                    .setMaxSiz(App.i().screenWidth, App.i().screenHeight)
                    // 设置开启压缩条件。当路径为空或者为gif时，不压缩
                    // 压缩后会改变文件地址，所以改回来
                    .setRenameListener(new OnRenameListener() {
                        @Override
                        public String rename(String filePath) {
                            return imgId;
                        }
                    })
                    .filter(new MyCompressionPredicate())
                    .setCompressListener(new OnCompressListener() {
                        @Override
                        public void onStart() {
                        }

                        @Override
                        public void onUnChange(final File file) {
                            articleHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (selectedWebView.get() != null && !selectedWebView.get().isDestroyed()) {
                                        selectedWebView.get().loadUrl("javascript:setTimeout( onImageLoadSuccess('" + imgId + "','" + file.getPath() + "'),1 )");
                                    }
                                }
                            });
                        }

                        @Override
                        public void onSuccess(final File file) {
                            ImageUtil.mergeBitmap(weakReferenceContext, file, new ImageUtil.OnMergeListener() {
                                @Override
                                public void onSuccess() {
                                    // XLog.d("图片合成成功" + Thread.currentThread());
                                    articleHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (selectedWebView.get() != null && !selectedWebView.get().isDestroyed()) {
                                                selectedWebView.get().loadUrl("javascript:setTimeout( onImageLoadSuccess('" + imgId + "','" + file.getPath() + "'),1 )");
                                            }
                                        }
                                    });
                                }

                                @Override
                                public void onError(Throwable e) {
                                    XLog.w("合成图片报错：" + e);
                                    e.printStackTrace();
                                    articleHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (selectedWebView.get() != null && !selectedWebView.get().isDestroyed()) {
                                                selectedWebView.get().loadUrl("javascript:setTimeout( onImageLoadSuccess('" + imgId + "','" + downloadedOriginalFile.getPath() + "'),1)");
                                            }
                                         }
                                    });
                                }
                            });
                        }

                        @Override
                        public void onError(Throwable e) {
                            XLog.d("压缩图片报错" + Thread.currentThread() );
                            // selectedWebView.loadUrl("javascript:onImageLoadSuccess('" + originalUrl + "','" + originalFileDir + fileNameExt + "')");
                            articleHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (selectedWebView.get() != null && !selectedWebView.get().isDestroyed()) {
                                        selectedWebView.get().loadUrl("javascript:setTimeout( onImageLoadSuccess('" + imgId + "','" + downloadedOriginalFile.getPath() + "'),1)");
                                    }
                                }
                            });
                        }
                    }).launch();
        }

        // 该方法执行在主线程中
        @Override
        public void onError(Response<File> response) {
            new File(originalFileDir + imgId).delete();
            XLog.d("下载图片失败：" + imageUrl + "','" + response.code() + "  " + response.getException());
            articleHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (selectedWebView.get() != null && !selectedWebView.get().isDestroyed()) {
                        selectedWebView.get().loadUrl("javascript:setTimeout( onImageLoadFailed('" + imgId + "'),1)");
                    }
                }
            });
        }
    }


    @JavascriptInterface
    @Override
    public void downImage(String articleId, String imgId, String originalUrl, boolean guessReferer) {
        String articleIdInMD5 = EncryptUtil.MD5(articleId);
        String originalFileDir = App.i().getUserCachePath() + articleIdInMD5 + "/original/";
        String fileNameExt = UriUtil.guessFileNameExt(originalUrl);

        // 下载时的过渡名称为 imgId
        if (new File(originalFileDir + imgId).exists() || new File(originalFileDir + fileNameExt).exists()) {
            return;
        }

        String compressedFileDir = App.i().getUserCachePath() + articleIdInMD5 + "/compressed/";
        DownFileCallback fileCallback = new DownFileCallback(originalFileDir, imgId);
        fileCallback.setParam(App.i(), selectedWebView, compressedFileDir, fileNameExt, guessReferer);
        fileCallback.setRefererParam(originalUrl, selectedArticle.getLink(), guessReferer);

        Request request = OkGo.<File>get(originalUrl)
                .tag(articleId)
                .client(imgHttpClient);

        if( guessReferer ){
            request.headers(Contract.REFERER, StringUtils.urlEncode(selectedArticle.getLink()));
        }else {
            String referer = NetworkRefererConfig.i().guessRefererByUrl(originalUrl);
            // referer = StringUtils.urlEncode(referer);
            if (!StringUtils.isEmpty(referer)) {
                request.headers(Contract.REFERER, referer);
            }
        }

        request.execute(fileCallback);
        //XLog.d("下载图片：" + originalUrl);
    }
    @JavascriptInterface
    @Override
    public void downFile(String url){
        DownloadListenerS downloadListener = new DownloadListenerS(this).setWebView(selectedWebView);
        // 请求文件大小
        // okhttp3.Request request = new okhttp3.Request.Builder().url(url).head().tag(TAG).build();
        // Call call = HttpClientManager.i().simpleClient().newCall(request);
        // call.enqueue(new Callback() {
        //     @Override
        //     public void onFailure(@NotNull Call call, @NotNull IOException e) {
        //     }
        //     @Override
        //     public void onResponse(@NotNull Call call, @NotNull okhttp3.Response response) throws IOException {
        //     }
        // });
        articleHandler.post(new Runnable() {
            @Override
            public void run() {
                downloadListener.onDownloadStart(url, NetworkUserAgentConfig.i().guessUserAgentByUrl(url),"", "video/*", -1);
            }
        });
    }

    @JavascriptInterface
    @Override
    public void openLink(String url) {
        Intent intent;
        // 使用内置浏览器
        if( App.i().getUser().isOpenLinkBySysBrowser() && (url.startsWith(SCHEMA_HTTP) || url.startsWith(SCHEMA_HTTPS))){
            intent = new Intent(ArticleActivity.this, WebActivity.class);
            intent.setData(Uri.parse(url));
            intent.putExtra("theme", App.i().getUser().getThemeMode());
        }else{
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            List<ResolveInfo> activities = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            List<ResolveInfo> activitiesToHide = getPackageManager().queryIntentActivities(new Intent(Intent.ACTION_VIEW, Uri.parse("https://wizos.me")), PackageManager.MATCH_DEFAULT_ONLY);
            XLog.d("数量：" + activities.size() +" , " + activitiesToHide.size());

            if( activities.size() != activitiesToHide.size()){
                HashSet<String> hideApp = new HashSet<>();
                hideApp.add("com.kingsoft.moffice_pro");
                for (ResolveInfo currentInfo : activitiesToHide) {
                    hideApp.add(currentInfo.activityInfo.packageName);
                    XLog.d("内容1：" + currentInfo.activityInfo.packageName);
                }
                ArrayList<Intent> targetIntents = new ArrayList<Intent>();
                for (ResolveInfo currentInfo : activities) {
                    String packageName = currentInfo.activityInfo.packageName;
                    if (!hideApp.contains(packageName)) {
                        Intent targetIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        targetIntent.setPackage(packageName);
                        targetIntents.add(targetIntent);
                    }
                    XLog.d("内容2：" + packageName);
                }
                if(targetIntents.size() > 0) {
                    intent = Intent.createChooser(targetIntents.remove(0),  getString(R.string.open_with));
                    intent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetIntents.toArray(new Parcelable[] {}));
                } else {
                    intent = new Intent(ArticleActivity.this, WebActivity.class);
                    intent.setData(Uri.parse(url));
                    intent.putExtra("theme", App.i().getUser().getThemeMode());
                }
            }else {
                intent = new Intent(ArticleActivity.this, WebActivity.class);
                intent.setData(Uri.parse(url));
                intent.putExtra("theme", App.i().getUser().getThemeMode());
            }
        }
        // 添加这一句表示对目标应用临时授权该Uri所代表的文件
        // intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED );
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private boolean useInnerBrowser(Intent intent){
        return getMatchActivitiesSize(intent) == getMatchActivitiesSize(new Intent(Intent.ACTION_VIEW, Uri.parse("https://wizos.me")));
    }
    private int getMatchActivitiesSize(Intent intent){
        return getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size();
    }


    @JavascriptInterface
    @Override
    public void openAudio(String url) {
        Intent intent = new Intent(this, MusicActivity.class);
        intent.putExtra("title", selectedArticle.getTitle());
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    private static boolean videoIsPortrait = false;
    @JavascriptInterface
    @Override
    public void postVideoPortrait(boolean isPortrait) {
        videoIsPortrait = isPortrait;
        articleHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                videoIsPortrait = false;
            }
        }, ViewConfiguration.getDoubleTapTimeout());
    }


    private void initView() {
        starView = findViewById(R.id.article_bottombar_star);
        starView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                editFavorites(App.i().getUser().getId());
                return true;
            }
        });
        readView = findViewById(R.id.article_bottombar_read);
        saveView = findViewById(R.id.article_bottombar_save);
        readabilityView = findViewById(R.id.article_bottombar_readability);
        swipeRefreshLayoutS = findViewById(R.id.art_swipe_refresh);
        swipeRefreshLayoutS.setEnabled(false);
        if (BuildConfig.DEBUG) {
            saveView.setVisibility(View.VISIBLE);
            saveView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickSaveIcon(view);
                }
            });
        }
        entryView = findViewById(R.id.slide_arrow_layout);
        slideLayout = findViewById(R.id.art_slide_layout);

        // XLog.e("子数量" + slideLayout.getChildCount() );

        int color;
        if (App.i().getUser().getThemeMode() == App.THEME_DAY) {
            color = Color.BLACK;
        } else {
            color = Color.WHITE;
        }
        slideLayout.edgeMode(SlideBack.EDGE_BOTH).arrowColor(color).callBack(new SlideCallBack() {
            @Override
            public void onSlide(int edgeFrom) {
                if (edgeFrom == SlideBack.EDGE_LEFT) {
                    onLeftBack();
                    entryView.scrollBy(0, 0);
                } else {
                    onRightBack();
                    entryView.scrollBy(0, 0);
                }
            }

            @Override
            public void onViewSlide(int edgeFrom, int offset) {
                //XLog.e("拖动方向：" + edgeFrom + " , " + offset);
                if (edgeFrom == SlideBack.EDGE_LEFT) {
                    entryView.scrollTo(-offset, 0);
                } else {
                    entryView.scrollTo(offset, 0);
                }
            }
        }).register();
    }

    private void initToolbar() {
        bottomBar = findViewById(R.id.art_bottombar);
        toolbar = findViewById(R.id.art_toolbar);
        setSupportActionBar(toolbar);
        // 这个小于4.0版本是默认为true，在4.0及其以上是false。该方法的作用：决定左上角的图标是否可以点击(没有向左的小图标)，true 可点
        getSupportActionBar().setHomeButtonEnabled(true);
        // 决定左上角图标的左侧是否有向左的小箭头，true 有小箭头
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        slowlyProgressBar = new SlowlyProgressBar((ProgressBar) findViewById(R.id.article_progress_bar));
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (articleHandler.hasMessages(App.MSG_DOUBLE_TAP) && selectedWebView != null) {
                    articleHandler.removeMessages(App.MSG_DOUBLE_TAP);
                    selectedWebView.scrollTo(0, 0);
                } else {
                    articleHandler.sendEmptyMessageDelayed(App.MSG_DOUBLE_TAP, ViewConfiguration.getDoubleTapTimeout());
                }
            }
        });
    }

    public void onLeftBack() {
        if (App.i().articlesAdapter == null || articleNo - 1 < 0) {
            ToastUtils.show("没有文章了");
            return;
        }
        saveArticleProgress();
        articleNo = articleNo - 1;
        initSelectedArticle(articleNo);
    }

    public void onRightBack() {
        if (App.i().articlesAdapter == null || articleNo + 1 >= App.i().articlesAdapter.getItemCount()) {
            ToastUtils.show("没有文章了");
            return;
        }
        saveArticleProgress();
        articleNo = articleNo + 1;
        initSelectedArticle(articleNo);
    }

    public void initSelectedArticle(int position) {
        swipeRefreshLayoutS.setRefreshing(false);
        // 取消之前那篇文章的图片下载(但是如果回到之前那篇文章，怎么恢复下载呢？)
        OkGo.cancelTag(imgHttpClient, articleId);
        // OkGo.cancelAll(imgHttpClient);
        // 取消之前的获取全文
        if(distill != null){
            distill.cancel();
        }
        articleNo = position;

        if (App.i().articlesAdapter != null && position < App.i().articlesAdapter.getItemCount()) {
            //XLog.e("重置文章状态");
            articleId = App.i().articlesAdapter.get(position).getId();
        }
        selectedArticle = CoreDB.i().articleDao().getById(App.i().getUser().getId(), articleId);
        initIconState();
        initWebViewContent();
    }


    private int downX, downY;
    // WebView在实例化后，可能还在渲染html，不一定能执行js
    @SuppressLint("ClickableViewAccessibility")
    private void initWebViewContent() {
        if (selectedWebView == null) {
            selectedWebView = new WebViewS(new MutableContextWrapper(App.i()));
            entryView.removeAllViews();
            entryView.addView(selectedWebView);
            // 初始化视频处理类
            video = new VideoImpl(ArticleActivity.this, selectedWebView);
            selectedWebView.setWebChromeClient(new WebChromeClientX(video, new WeakReference<SlowlyProgressBar>(slowlyProgressBar)));
            selectedWebView.setWebViewClient(new WebViewClientX());
            // 原本想放在选择 webview 页面的时候去加载，但可能由于那时页面内容已经加载所以无法设置下面这个JSInterface？
            selectedWebView.addJavascriptInterface(ArticleActivity.this, ArticleBridge.TAG);
            //selectedWebView.addJavascriptObject(new JsApi(), null);
            selectedWebView.setDownloadListener(new DownloadListenerS(this));
            selectedWebView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View arg0, MotionEvent arg1) {
                    downX = (int) arg1.getX();
                    downY = (int) arg1.getY();
                    return false;
                }
            });

            // 作者：Wing_Li，链接：https://www.jianshu.com/p/3fcf8ba18d7f
            selectedWebView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View webView) {
                    WebView.HitTestResult result = ((WebView) webView).getHitTestResult();
                    if (null == result) {
                        return false;
                    }
                    int type = result.getType();
                    if (type == WebView.HitTestResult.UNKNOWN_TYPE) {
                        return false;
                    }

                    // 这里可以拦截很多类型，我们只处理超链接就可以了
                    new LongClickPopWindow(ArticleActivity.this, (WebView) webView, ScreenUtil.dp2px(ArticleActivity.this, 120), ScreenUtil.dp2px(ArticleActivity.this, 130), downX, downY + 10);
                    // webViewLongClickedPopWindow.showAtLocation(webView, Gravity.TOP|Gravity.LEFT, downX, downY + 10);
                    return true;
                }
            });
        }

        // 检查该订阅源默认显示什么。【RSS，已读，保存的网页，原始网页】
        // XLog.e("要加载的位置为：" + position + "  " + selectedArticle.getTitle());
        Feed feed = CoreDB.i().feedDao().getById(App.i().getUser().getId(), selectedArticle.getFeedId());
        if (feed != null) {
            toolbar.setTitle(feed.getTitle());
            if(feed.getDisplayMode() == App.OPEN_MODE_LINK){
                selectedWebView.loadUrl(selectedArticle.getLink());
                // 判断是要在加载的时候获取还是同步的时候获取
            } else {
                // selectedWebView.loadData(ArticleUtil.getPageForDisplay(selectedArticle));
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        String content = ArticleUtil.getPageForDisplay(selectedArticle);
                        articleHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                selectedWebView.loadData(content);
                            }
                        });
                    }
                });
            }
        } else {
            // selectedWebView.loadData(ArticleUtil.getPageForDisplay(selectedArticle));
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    String content = ArticleUtil.getPageForDisplay(selectedArticle);
                    articleHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            selectedWebView.loadData(content);
                        }
                    });
                }
            });
        }
        selectedWebView.requestFocus();
    }


    private static class WebChromeClientX extends WebChromeClient {
        VideoImpl video;
        WeakReference<SlowlyProgressBar> slowlyProgressBar;

        WebChromeClientX(VideoImpl video, WeakReference<SlowlyProgressBar> progressBar) {
            this.video = video;
            this.slowlyProgressBar = progressBar;
        }

        @Override
        public void onProgressChanged(WebView webView, int progress) {
            // 增加Javascript异常监控，不能增加，会造成页面卡死
            // CrashReport.setJavascriptMonitor(webView, true);
            if (slowlyProgressBar.get() != null) {
                slowlyProgressBar.get().onProgressChange(progress);
            }
        }

        // 表示进入全屏的时候
        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            super.onShowCustomView(view,callback);
            // XLog.i("进入全屏" + videoIsPortrait + " , " + (System.currentTimeMillis() - time));
            if (video != null) {
                video.onShowCustomView(view, videoIsPortrait, callback);
            }
            videoIsPortrait = false;
        }

        //表示退出全屏的时候
        @Override
        public void onHideCustomView() {
            super.onHideCustomView();
            // XLog.i("退出全屏");
            if (video != null) {
                video.onHideCustomView();
            }
        }
    }

    private class WebViewClientX extends WebViewClient {
        // 通过重写WebViewClient的onReceivedSslError方法来接受所有网站的证书，忽略SSL错误。
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            XLog.e("SSL错误");
            handler.proceed(); // 忽略SSL证书错误，继续加载页面
        }

        @Deprecated
        @SuppressLint("NewApi")
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, final WebResourceRequest request) {
            // 有广告的请求数据，我们直接返回空数据，注：不能直接返回null
            if ( AdBlock.i().isAd(request.getUrl().toString()) ) {
                return new WebResourceResponse(null, null, null);
            }
            // String url = request.getUrl().toString().toLowerCase();
            // 此处有2个方案来实现替换图片请求为本地下载好的图片
            // 【1】无法将 图片链接 通过重定向到 file:/storage/emulated/0 以及 content://me.wizos.loread
            // 【2】可以通过拦截 图片请求，直接返回本地图片流 WebResourceResponse。但是囿于以下2个问题，导致很复杂：
            //      1.html 中的图片链接还是 http 或 https 协议的，导致点击打开图片时，打开的src链接是http链接，只能在html中js中遍历所有图片，找到本地有的图片，给其加个本地图片地址的属性。
            //      2.本地没有的图片无法被替换成占位图，否则会导致<video>标签的封面也会被接管替换成占位图。只能在html中js中遍历所有图片，找到本地无的图片替换为占位图。这样还是会导致图片加载的闪烁。
            // 不能通过此处来接管网页图片的加载，会导致<video>标签的封面也会被接管
            // if (url.startsWith("data:") || url.endsWith(".css") || url.endsWith(".js") || url.endsWith(".woff") || url.endsWith(".ttf")){
            //     return super.shouldInterceptRequest(view, request);
            // }
            // String cacheUrl = FileUtil.readCacheFilePath(EncryptUtil.MD5(selectedArticle.getId()), url);
            // XLog.e("【请求加载资源】" + url  + " , " + cacheUrl);
            // try {
            //     if (cacheUrl != null) {
            //         return new WebResourceResponse("image/png", "UTF-8", new FileInputStream(cacheUrl));
            //     } else {
            //         return new WebResourceResponse( "image/png", "UTF-8", getAssets().open("image/image_holder.png"));
            //     }
            // } catch (IOException e) {
            //     e.printStackTrace();
            // }
            return super.shouldInterceptRequest(view, request);
        }

        /**
         * @param webView
         * @param url
         * @return
         * 返回 true 表示你已经处理此次请求。
         * 返回 false 表示由webview自行处理（一般都是把此url加载出来）。
         * 返回 super.shouldOverrideUrlLoading(view, url); 这个返回的方法会调用父类方法，也就是跳转至手机浏览器
         */
        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, String url) {
            XLog.e("url为：" + url);
            // 判断重定向的方式一
            // 作者：胡几手，链接：https://www.jianshu.com/p/7dfb8797f893
            // 解决在webView第一次加载的url重定向到了另一个地址时，也会走shouldOverrideUrlLoading回调的问题
            WebView.HitTestResult hitTestResult = webView.getHitTestResult();
            if (hitTestResult == null) {
                return false;
            } else if (hitTestResult.getType() == WebView.HitTestResult.UNKNOWN_TYPE) {
                return false;
            }

            if (TextUtils.isEmpty(url) || url.startsWith(SCHEMA_FILE)) {
                return true;
            }

            String newUrl = LinkRewriteConfig.i().getRedirectUrl( url );
            if (!TextUtils.isEmpty(newUrl)) {
                url = newUrl;
            }
            openLink(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView webView, String url, Bitmap favicon) {
            super.onPageStarted(webView, url, favicon);
            //XLog.e("页面加载开始");
            if (slowlyProgressBar != null) {
                slowlyProgressBar.onProgressStart();
            }
        }

        /**
         * 不能直接在这里就初始化setupImage，因为在viewpager中预加载而生成webview的时候，这里的懒加载就被触发了
         * webView.loadUrl("javascript:setTimeout(\"setupImage()\",100)");
         */
        @Override
        public void onPageFinished(WebView webView, String url) {
            super.onPageFinished(webView, url);
            webView.getSettings().setBlockNetworkImage(false);
            Integer process = App.i().articleProgress.get(articleId);
            if (process != null && selectedWebView != null) {
                selectedWebView.scrollTo(0, process);
            }
        }
    }

    private void initIconState() {
        if (selectedArticle.getReadStatus() == App.STATUS_UNREAD) {
            readView.setText(getString(R.string.font_readed));
            selectedArticle.setReadStatus(App.STATUS_READED);
            CoreDB.i().articleDao().update(selectedArticle);
            App.i().getApi().markArticleReaded(selectedArticle.getId(), new CallbackX() {
                @Override
                public void onSuccess(Object result) { }

                @Override
                public void onFailure(Object error) {
                    selectedArticle.setReadStatus(App.STATUS_UNREAD);
                    CoreDB.i().articleDao().update(selectedArticle);
                    ToastUtils.show(getString(R.string.mask_fail));
                }
            });
        } else if (selectedArticle.getReadStatus() == App.STATUS_READED) {
            readView.setText(getString(R.string.font_readed));
        } else if (selectedArticle.getReadStatus() == App.STATUS_UNREADING) {
            readView.setText(getString(R.string.font_unread));
        }

        if (selectedArticle.getStarStatus() == App.STATUS_UNSTAR) {
            starView.setText(getString(R.string.font_unstar));
        } else {
            starView.setText(getString(R.string.font_stared));
        }
        if (App.STATUS_NOT_FILED == selectedArticle.getSaveStatus()) {
            saveView.setText(getString(R.string.font_unsave));
        } else {
            saveView.setText(getString(R.string.font_saved));
        }

        if(App.i().oldArticles.containsKey(selectedArticle.getId())){
            readabilityView.setText(R.string.font_article_readability);
        }else {
            readabilityView.setText(R.string.font_article_original);
        }

        final Feed feed = CoreDB.i().feedDao().getById(App.i().getUser().getId(), selectedArticle.getFeedId());
        //final View feedConfigView = findViewById(R.id.article_feed_config);
        if( feedMenuItem != null ){
            if (feed != null) {
                feedMenuItem.setVisible(true);
            }else {
                feedMenuItem.setVisible(false);
            }
        }
    }

    public void onClickReadIcon(View view) {
        //XLog.e("loread", "被点击的是：" + selectedArticle.getTitle());
        if (selectedArticle.getReadStatus() == App.STATUS_READED) {
            readView.setText(getString(R.string.font_unread));
            selectedArticle.setReadStatus(App.STATUS_UNREADING);
            CoreDB.i().articleDao().update(selectedArticle);
            App.i().getApi().markArticleUnread(selectedArticle.getId(), new CallbackX() {
                @Override
                public void onSuccess(Object result) {
                }

                @Override
                public void onFailure(Object error) {
                    selectedArticle.setReadStatus(App.STATUS_READED);
                    CoreDB.i().articleDao().update(selectedArticle);
                    ToastUtils.show(getString(R.string.mask_fail));
                }
            });
        } else {
            readView.setText(getString(R.string.font_readed));
            selectedArticle.setReadStatus(App.STATUS_READED);
            CoreDB.i().articleDao().update(selectedArticle);

            App.i().getApi().markArticleReaded(selectedArticle.getId(), new CallbackX() {
                @Override
                public void onSuccess(Object result) {
                }

                @Override
                public void onFailure(Object error) {
                    selectedArticle.setReadStatus(App.STATUS_UNREAD);
                    CoreDB.i().articleDao().update(selectedArticle);
                    ToastUtils.show(getString(R.string.mask_fail));
                }
            });

        }
    }

    public void onClickStarIcon(View view) {
        String uid = App.i().getUser().getId();
        if (selectedArticle.getStarStatus() == App.STATUS_UNSTAR) {
            starView.setText(getString(R.string.font_stared));
            selectedArticle.setStarStatus(App.STATUS_STARED);
            CoreDB.i().articleDao().update(selectedArticle);
            App.i().getApi().markArticleStared(selectedArticle.getId(), new CallbackX() {
                @Override
                public void onSuccess(Object result) {
                }

                @Override
                public void onFailure(Object error) {
                    selectedArticle.setStarStatus(App.STATUS_UNSTAR);
                    CoreDB.i().articleDao().update(selectedArticle);
                    ToastUtils.show(getString(R.string.mask_fail));
                }
            });

            List<Category> categories = CoreDB.i().categoryDao().getByFeedId(uid,selectedArticle.getFeedId());
            String msg = null;
            String action = null;
            if(categories == null || StringUtils.isEmpty(categories)){
                msg = getString(R.string.star_marked);
                action = getString(R.string.add_to_favorites);
            }else if(categories.size() == 1){
                msg = getString(R.string.star_marked_to_favorites,categories.get(0).getTitle());
                action = getString(R.string.edit_favorites);
            }else {
                msg = getString(R.string.star_marked_to_favorites,categories.get(0).getTitle() + getString(R.string.etc));
                action = getString(R.string.edit_favorites);
            }

            SnackbarUtil.Long(swipeRefreshLayoutS, bottomBar, msg).setAction(action, v -> editFavorites(uid)).show();
        } else {
            starView.setText(getString(R.string.font_unstar));
            selectedArticle.setStarStatus(App.STATUS_UNSTAR);
            CoreDB.i().articleDao().update(selectedArticle);
            App.i().getApi().markArticleUnstar(selectedArticle.getId(), new CallbackX() {
                @Override
                public void onSuccess(Object result) {
                }

                @Override
                public void onFailure(Object error) {
                    selectedArticle.setStarStatus(App.STATUS_STARED);
                    CoreDB.i().articleDao().update(selectedArticle);
                    ToastUtils.show(getString(R.string.mask_fail));
                }
            });
            CoreDB.i().articleTagDao().deleteByArticleId(uid,selectedArticle.getId());
            ArticleTags.i().removeArticle(selectedArticle.getId());
            ArticleTags.i().save();
        }
    }
    private void editFavorites(String uid){
        // 找出当前用户有的所有tags
        List<Tag> tags = CoreDB.i().tagDao().getAll(uid);
        // 找出当前用户该文章的tags
        List<ArticleTag> originalArticleTags = CoreDB.i().articleTagDao().getByArticleId(uid, selectedArticle.getId());

        Integer[] preSelectedIndices = null;
        int index = 0;
        if(originalArticleTags!=null && originalArticleTags.size() > 0){
            preSelectedIndices = new Integer[]{originalArticleTags.size()};
        }
        String[] tagTitles;
        if( tags != null ){
            tagTitles = new String[tags.size()];
            for (int i = 0, size = tags.size(); i < size; i++) {
                String title = tags.get(i).getTitle();
                tagTitles[i] = title;
                //XLog.e("标题ss：" + title + " , " + originalArticleTags + "  " + index + "  " + i );
                if(preSelectedIndices!=null){
                    for (ArticleTag articleTag:originalArticleTags) {
                        //XLog.e("标题：" + title + " , " + articleTag.getTagId() + "  " + index + "  " + i );
                        if(title.equals(articleTag.getTagId())){
                            preSelectedIndices[index] = i;
                            index ++;
                        }
                    }
                }
            }

            ArrayList<ArticleTag> selectedArticleTags = new ArrayList<>();
            new MaterialDialog.Builder(ArticleActivity.this)
                    .title(getString(R.string.select_tag))
                    .items(tagTitles)
                    .itemsCallbackMultiChoice(preSelectedIndices, (dialog, which, text) -> {
                        selectedArticleTags.clear();
                        ArticleTag articleTag;
                        for (int i : which) {
                            articleTag = new ArticleTag(uid, selectedArticle.getId(), tags.get(i).getId() );
                            selectedArticleTags.add(articleTag);
                        }
                        XLog.e("已选择收藏夹：" + Arrays.toString(text));
                        return true;
                    })
                    .positiveText(R.string.confirm)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            if(selectedArticleTags.size() > 0){
                                CoreDB.i().articleTagDao().deleteByArticleId(selectedArticle.getUid(),selectedArticle.getId());
                                CoreDB.i().articleTagDao().insert(selectedArticleTags);
                                ArticleTags.i().removeArticle(selectedArticle.getId());
                                ArticleTags.i().addArticleTags(selectedArticleTags);
                                ArticleTags.i().save();
                            }
                            dialog.dismiss();
                        }
                    })
                    .neutralText(getString(R.string.new_favorites))
                    .onNeutral(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            newFavorites(uid,dialog);
                        }
                    })
                    .alwaysCallMultiChoiceCallback() // the callback will always be called, to check if selection is still allowed
                    .show();
        }else {
            newFavorites(uid,null);
        }
    }
    private void newFavorites(String uid,@Nullable MaterialDialog lastDialog){
        new MaterialDialog.Builder(ArticleActivity.this)
                .title(R.string.new_favorites)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .inputRange(1, 16)
                .input(getString(R.string.new_favorites), null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        Tag tag = new Tag();
                        tag.setUid(uid);
                        tag.setId(input.toString());
                        tag.setTitle(input.toString());
                        CoreDB.i().tagDao().insert(tag);
                        dialog.dismiss();
                        if(lastDialog != null){
                            lastDialog.dismiss();
                        }
                        editFavorites(uid);
                        XLog.e("正在新建收藏夹：" + input.toString());
                    }
                })
                .positiveText(R.string.confirm)
                .negativeText(android.R.string.cancel)
                .show();
    }

    public void onClickSaveIcon(View view) {
        if (selectedArticle.getSaveStatus() == App.STATUS_NOT_FILED) {
            saveView.setText(getString(R.string.font_saved));
            selectedArticle.setSaveStatus(App.STATUS_TO_BE_FILED);
            addToSaveDirectory(App.i().getUser().getId());
        } else if (selectedArticle.getSaveStatus() == App.STATUS_TO_BE_FILED){
            saveView.setText(getString(R.string.font_unsave));
            selectedArticle.setSaveStatus(App.STATUS_NOT_FILED);
            SaveDirectory.i().setArticleDirectory(selectedArticle.getId(),null);
        } else if (selectedArticle.getSaveStatus() == App.STATUS_IS_FILED){
            saveView.setText(getString(R.string.font_saved));
            ToastUtils.show(getString(R.string.is_filed_cannot_edit));
        }
        CoreDB.i().articleDao().update(selectedArticle);
    }
    private void clearDirectory(String uid){
        SaveDirectory.i().setArticleDirectory(selectedArticle.getId(),null);
    }
    private void addToSaveDirectory(String uid){
        String dir = SaveDirectory.i().getSaveDir(selectedArticle.getFeedId(),selectedArticle.getId());
        String msg;
        if (StringUtils.isEmpty(dir)) {
            msg = getString(R.string.saved_to_root_directory);
        }else {
            msg = getString(R.string.saved_to_directory,dir);
        }

        SnackbarUtil.Long(swipeRefreshLayoutS, bottomBar, msg)
                .setAction(R.string.edit_directory, v -> editDirectory(uid)).show();
    }
    private void editDirectory(String uid){
        String[] savedFoldersTitle = SaveDirectory.i().getDirectoriesOptionName();
        List<String> savedFoldersValue = SaveDirectory.i().getDirectoriesOptionValue();
        new MaterialDialog.Builder(this)
                .title(getString(R.string.edit_directory))
                .items(savedFoldersTitle)
                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        SaveDirectory.i().setArticleDirectory(selectedArticle.getId(),savedFoldersValue.get(which));
                        SaveDirectory.i().save();
                        XLog.e("被选择的目录为：" + text.toString());
                        return true;
                    }
                })
               // .neutralText(getString(R.string.new_directory))
               // .onNeutral(new MaterialDialog.SingleButtonCallback() {
               //     @Override
               //     public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
               //         newDirectory(uid,dialog);
               //     }
               // })
                .show();
    }

    // public void newDirectory(String uid,@Nullable MaterialDialog lastDialog){
    //     new MaterialDialog.Builder(this)
    //             .title(R.string.new_directory)
    //             .inputType(InputType.TYPE_CLASS_TEXT)
    //             .inputRange(1, 16)
    //             .input(getString(R.string.new_directory), null, new MaterialDialog.InputCallback() {
    //                 @Override
    //                 public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
    //                     SaveDirectory.i().newDirectory(input.toString());
    //                     SaveDirectory.i().save();
    //                     if(lastDialog != null){
    //                         lastDialog.dismiss();
    //                     }
    //                     editDirectory(uid);
    //                     XLog.e("正在新建收藏夹：" + input.toString());
    //                 }
    //             })
    //             .positiveText(R.string.confirm)
    //             .negativeText(android.R.string.cancel)
    //             .show();
    // }

    public void openOriginalArticle(View view) {
        //Intent intent = new Intent(ArticleActivity.this, WebActivity.class);
        //intent.setData(Uri.parse(selectedArticle.getLink()));
        //intent.putExtra("theme", App.i().getUser().getThemeMode());
        //startActivity(intent);
        //overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        openLink(selectedArticle.getLink());
    }

    // public void switchReadabilityArticle2(View view) {
    //     if(swipeRefreshLayoutS.isRefreshing()){
    //         OkGo.cancelTag(HttpClientManager.i().simpleClient(),"Readability");
    //         swipeRefreshLayoutS.setRefreshing(false);
    //         return;
    //     }
    //     saveArticleProgress();
    //
    //     Article oldArticle = null;
    //     if(App.i().oldArticles != null){
    //         oldArticle = App.i().oldArticles.get(selectedArticle.getId());
    //     }
    //
    //     if(oldArticle != null){
    //         selectedArticle.setContent(oldArticle.getContent());
    //         selectedArticle.setSummary(oldArticle.getSummary());
    //         selectedArticle.setImage(oldArticle.getImage());
    //         App.i().oldArticles.remove(selectedArticle.getId());
    //         ToastUtils.show(getString(R.string.cancel_readability));
    //         selectedWebView.loadData(ArticleUtil.getPageForDisplay(selectedArticle));
    //         CoreDB.i().articleDao().update(selectedArticle);
    //         readabilityView.setText(getString(R.string.font_article_original));
    //     }else {
    //         ToastUtils.show(getString(R.string.get_readability_ing));
    //         swipeRefreshLayoutS.setRefreshing(true);
    //
    //         okhttp3.Request request = new okhttp3.Request.Builder().url(selectedArticle.getLink()).tag("Readability").build();
    //         Call call = HttpClientManager.i().simpleClient().newCall(request);
    //         call.enqueue(new Callback() {
    //             @Override
    //             public void onFailure(@NotNull Call call, @NotNull IOException e) {
    //                 articleHandler.post(new Runnable() {
    //                     @Override
    //                     public void run() {
    //                         if (swipeRefreshLayoutS == null) {
    //                             return;
    //                         }
    //                         swipeRefreshLayoutS.setRefreshing(false);
    //                         ToastUtils.show(getString(R.string.get_readability_failure));
    //                     }
    //                 });
    //             }
    //
    //
    //             // 这是因为OkHttp对于异步的处理仅仅是开启了一个线程，并且在线程中处理响应，所以不能再其中操作UI。
    //             // OkHttp是一个面向于Java应用而不是特定平台(Android)的框架，那么它就无法在其中使用Android独有的Handler机制。
    //             @Override
    //             public void onResponse(@NotNull Call call, @NotNull okhttp3.Response response) throws IOException {
    //                 if(response.isSuccessful()){
    //                     App.i().oldArticles.put(selectedArticle.getId(),(Article)selectedArticle.clone());
    //                     ArticleUtil.getReadabilityArticle(selectedArticle, response.body());
    //                     CoreDB.i().articleDao().update(selectedArticle);
    //                     articleHandler.post(new Runnable() {
    //                         @Override
    //                         public void run() {
    //                             if (swipeRefreshLayoutS == null ||selectedWebView == null) {
    //                                 return;
    //                             }
    //                             swipeRefreshLayoutS.setRefreshing(false);
    //                             ToastUtils.show(getString(R.string.get_readability_success));
    //                             readabilityView.setText(getString(R.string.font_article_readability));
    //                             selectedWebView.loadData(ArticleUtil.getPageForDisplay(selectedArticle));
    //                         }
    //                     });
    //                 }else {
    //                     articleHandler.post(new Runnable() {
    //                         @Override
    //                         public void run() {
    //                             if (swipeRefreshLayoutS == null) {
    //                                 return;
    //                             }
    //                             swipeRefreshLayoutS.setRefreshing(false);
    //                             ToastUtils.show(getString(R.string.get_readability_failure));
    //                         }
    //                     });
    //                 }
    //             }
    //         });
    //     }
    // }

    private Distill distill;
    public void switchReadabilityArticle(View view) {
        if(swipeRefreshLayoutS.isRefreshing()){
            swipeRefreshLayoutS.setRefreshing(false);
            if(distill != null){
                distill.cancel();
            }
            return;
        }
        saveArticleProgress();

        Article oldArticle = null;
        if(App.i().oldArticles != null){
            oldArticle = App.i().oldArticles.get(selectedArticle.getId());
        }

        if(oldArticle != null){
            selectedArticle.setContent(oldArticle.getContent());
            selectedArticle.setSummary(oldArticle.getSummary());
            selectedArticle.setImage(oldArticle.getImage());
            App.i().oldArticles.remove(selectedArticle.getId());
            ToastUtils.show(getString(R.string.cancel_readability));
            // selectedWebView.loadData(ArticleUtil.getPageForDisplay(selectedArticle));
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    String content = ArticleUtil.getPageForDisplay(selectedArticle);
                    articleHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            selectedWebView.loadData(content);
                        }
                    });
                }
            });
            CoreDB.i().articleDao().update(selectedArticle);
            readabilityView.setText(getString(R.string.font_article_original));
        }else {
            ToastUtils.show(getString(R.string.get_readability_ing));

            String keyword;
            if( App.i().articleFirstKeyword.containsKey(selectedArticle.getId()) ){
                keyword = App.i().articleFirstKeyword.get(selectedArticle.getId());
            }else {
                keyword = ArticleUtil.getKeyword(selectedArticle.getContent());
                App.i().articleFirstKeyword.put(selectedArticle.getId(),keyword);
            }

            swipeRefreshLayoutS.setRefreshing(true);
            distill = new Distill(selectedArticle.getLink(), keyword, new Distill.Listener() {
                @Override
                public void onResponse(String content) {
                    App.i().oldArticles.put(selectedArticle.getId(),(Article)selectedArticle.clone());

                    selectedArticle.updateContent(content);

                    CoreDB.i().articleDao().update(selectedArticle);
                    articleHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (swipeRefreshLayoutS == null ||selectedWebView == null) {
                                return;
                            }
                            swipeRefreshLayoutS.setRefreshing(false);
                            ToastUtils.show(getString(R.string.get_readability_success));
                            readabilityView.setText(getString(R.string.font_article_readability));
                            // selectedWebView.loadData(ArticleUtil.getPageForDisplay(selectedArticle));
                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    String content = ArticleUtil.getPageForDisplay(selectedArticle);
                                    articleHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            selectedWebView.loadData(content);
                                        }
                                    });
                                }
                            });
                        }
                    });
                }

                @Override
                public void onFailure(String msg) {
                    articleHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (swipeRefreshLayoutS == null) {
                                return;
                            }
                            swipeRefreshLayoutS.setRefreshing(false);
                            ToastUtils.show(getString(R.string.get_readability_failure, msg));
                        }
                    });
                }
            });
            distill.getContent();
        }
    }

    private void showArticleInfo() {
        // XLog.e("文章信息");
        if (!BuildConfig.DEBUG) {
            return;
        }
        Document document = Jsoup.parseBodyFragment(ArticleUtil.getPageForDisplay(selectedArticle));
        document.outputSettings().prettyPrint(true);
        String info = selectedArticle.getTitle() + "\n" +
                "ID=" + selectedArticle.getId() + "\n" +
                "ID-MD5=" + EncryptUtil.MD5(selectedArticle.getId()) + "\n" +
                "ReadState=" + selectedArticle.getReadStatus() + "\n" +
                "ReadUpdated=" + selectedArticle.getReadUpdated() + "\n" +
                "StarState=" + selectedArticle.getStarStatus() + "\n" +
                "StarUpdated=" + selectedArticle.getStarUpdated() + "\n" +
                "SaveStatus=" + selectedArticle.getSaveStatus() + "\n" +
                "SaveStatus=" + selectedArticle.getSaveStatus() + "\n" +
                "Pubdate=" + selectedArticle.getPubDate() + "\n" +
                "Crawldate=" + selectedArticle.getCrawlDate() + "\n" +
                "Author=" + selectedArticle.getAuthor() + "\n" +
                "FeedId=" + selectedArticle.getFeedId() + "\n" +
                "Image=" + selectedArticle.getImage() + "\n" +
                "Enclosure=" + selectedArticle.getEnclosure() + "\n" +
                "【Link】" + selectedArticle.getLink() + "\n" +
                "【Summary】" + selectedArticle.getSummary() + "\n\n" +
                "【Content】" + document.outerHtml() + "\n";

        new MaterialDialog.Builder(this)
                .title(R.string.article_info)
                .content(info)
                .positiveText(R.string.agree)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //获取剪贴板管理器：
                        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        // 创建普通字符型ClipData
                        ClipData mClipData = ClipData.newPlainText("ArticleContent", ArticleUtil.getPageForDisplay(selectedArticle));
                        // 将ClipData内容放到系统剪贴板里。
                        cm.setPrimaryClip(mClipData);
                        ToastUtils.show("已复制文章内容");
                    }
                })

                .positiveColorRes(R.color.material_red_400)
                .titleGravity(GravityEnum.CENTER)
                .titleColorRes(R.color.material_red_400)
                .contentColorRes(android.R.color.white)
                .backgroundColorRes(R.color.material_blue_grey_800)
                .dividerColorRes(R.color.material_teal_a400)
//                .btnSelector(R.drawable.md_btn_selector_custom, DialogAction.POSITIVE)
                .positiveColor(Color.WHITE)
                .negativeColorAttr(android.R.attr.textColorSecondaryInverse)
                .theme(Theme.DARK)
                .show();
    }

    /**
     * 不能使用 onBackPressed，会导致overridePendingTransition转场动画失效
     * event.getRepeatCount() 后者为短期内重复按下的次数
     *
     * @return 返回真表示返回键被屏蔽掉
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (video != null && video.isFullScreen()) {
                video.onHideCustomView();
                return true;
            }
            Intent data = new Intent();
            data.putExtra("articleNo", articleNo);
            //注意下面的RESULT_OK常量要与回传接收的Activity中onActivityResult()方法一致
            this.setResult(App.ActivityResult_ArtToMain, data);
            this.finish();
            overridePendingTransition(R.anim.fade_in, R.anim.out_from_bottom);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected Colorful.Builder buildColorful(Colorful.Builder mColorfulBuilder) {
        mColorfulBuilder
                .backgroundColor(R.id.article_root, R.attr.root_view_bg)
                // 设置 toolbar
                .backgroundColor(R.id.art_toolbar, R.attr.topbar_bg)
                //.textColor(R.id.art_toolbar_num, R.attr.topbar_fg)
                // 设置中屏和底栏之间的分割线
                .backgroundColor(R.id.article_bottombar_divider, R.attr.bottombar_divider)
                // 设置 bottombar
                .backgroundColor(R.id.art_bottombar, R.attr.bottombar_bg)
                .textColor(R.id.article_bottombar_read, R.attr.bottombar_fg)
                .textColor(R.id.article_bottombar_star, R.attr.bottombar_fg)
                .textColor(R.id.article_feed_config, R.attr.topbar_fg)
                .textColor(R.id.article_bottombar_open_link, R.attr.bottombar_fg)
                .textColor(R.id.article_bottombar_save, R.attr.bottombar_fg);
        return mColorfulBuilder;
    }



    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_article, menu);
        feedMenuItem = menu.findItem(R.id.article_menu_feed);
        final Feed feed = CoreDB.i().feedDao().getById(App.i().getUser().getId(), selectedArticle.getFeedId());
        if (feed != null) {
            feedMenuItem.setVisible(true);
        }else {
            feedMenuItem.setVisible(false);
        }
        if(!BuildConfig.DEBUG){
            MenuItem speak = menu.findItem(R.id.article_menu_speak);
            speak.setVisible(false);
            MenuItem articleInfo = menu.findItem(R.id.article_menu_article_info);
            articleInfo.setVisible(false);
            MenuItem editContent = menu.findItem(R.id.article_menu_edit_content);
            editContent.setVisible(false);
        }
        return true;
    }

    MenuItem feedMenuItem;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //监听左上角的返回箭头
            case android.R.id.home:
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;
            case R.id.article_menu_feed:
                final Feed feed = CoreDB.i().feedDao().getById(App.i().getUser().getId(), selectedArticle.getFeedId());
                //final View feedConfigView = findViewById(R.id.article_feed_config);
                if (feed != null) {
                    Intent intent = new Intent(ArticleActivity.this, FeedActivity.class);
                    intent.putExtra("feedId", selectedArticle.getFeedId());
                    startActivity(intent);
                } else {
                    ToastUtils.show("该订阅源已退订，无法编辑");
                }
                break;
            case R.id.article_menu_speak:
                Intent intent = new Intent(ArticleActivity.this, TTSActivity.class);
                intent.putExtra("articleNo", articleNo);
                startActivity(intent);
                break;
            case R.id.article_menu_article_info:
                showArticleInfo();
                break;
            case R.id.article_menu_edit_content:
                new MaterialDialog.Builder(ArticleActivity.this)
                        .title("修改文章内容")
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .inputRange(1, 5600000)
                        .input(getString(R.string.site_remark), selectedArticle.getContent(), new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                selectedArticle.setContent(input.toString());
                                CoreDB.i().articleDao().update(selectedArticle);
                            }
                        })
                        .positiveText(R.string.save)
                        .negativeText(android.R.string.cancel)
                        .show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_article_activity, menu);
//        return true;
//    }

//    private void openMode(){
    // 调用系统默认的图片查看应用
//        Intent intentImage = new Intent(Intent.ACTION_VIEW);
//        intentImage.addCategory(Intent.CATEGORY_DEFAULT);
//        File file = new File(imageFilePath);
//        intentImage.setDataAndType(Uri.fromFile(file), "image/*");
//        startActivity(intentImage);

    // 每次都要选择打开方式
//        startActivity(Intent.createChooser(intentImage, "请选择一款"));

    // 调起系统默认的图片查看应用（带有选择为默认）
//        if(BuildConfig.DEBUG){
//            Intent openImageIntent = new Intent(Intent.ACTION_VIEW);
//            openImageIntent.addCategory(Intent.CATEGORY_DEFAULT);
//            openImageIntent.setDataAndType(Uri.fromFile(new File(imageFilePath)), "image/*");
//            getDefaultActivity(openImageIntent);
//        }
//    }
//    // 获取默认的打开方式
//    public void getDefaultActivity(Intent intent) {
//        PackageManager pm = this.getPackageManager();
//        ResolveInfo info = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
//        // 如果本应用没有询问过是否要选择默认打开方式，并且没有默认的打开方式，打开默认方式选择狂
//        if (!WithPref.i().hadAskImageOpenMode() || info.activityInfo.packageName.equals("android")) {
//            WithPref.i().setHadAskImageOpenMode(true);
//            intent.setComponent(new ComponentName("android", "com.android.internal.app.ResolverActivity"));
//        }
//        startActivity(intent);
//        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
//        XLog.i("打开方式", "默认打开方式信息 = " + info + ";pkgName = " + info.activityInfo.packageName);
//    }

    // 打开选择默认打开方式的弹窗
//    public void startChooseDialog() {
//        Intent intent = new Intent();
//        intent.setAction("android.intent.action.VIEW");
//        intent.addCategory(Intent.CATEGORY_DEFAULT);
//        intent.setData(Uri.fromFile(new File(imageFilePath)));
//        intent.setComponent(new ComponentName("android","com.android.internal.app.ResolverActivity"));
//        startActivity(intent);
//    }
}
