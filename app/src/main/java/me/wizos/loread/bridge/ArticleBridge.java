package me.wizos.loread.bridge;

/**
 * 设置图片的默认加载行为
 * <p>
 * native 需要实现的接口有:
 * String readCacheFilePath(String url);
 * boolean isAutoLoadImage();
 * void loadImage(String url);
 * void openImage(String urls, int index);
 * <p>
 * native 可以调用的方法有:
 * void onImageLoadFailed(String url);
 * void onImageLoadSuccess(String url, String localUrl);
 *
 * @author by Wizos on 2018/3/4.
 */

public interface ArticleBridge {
    String TAG = "ArticleBridge";

    void log(String paramString);

    void show(String msg);

    /**
     * 当图片快出现在屏幕上是，调用这个方法去加载图片
     * 能否下载图片分以下几种情况：
     * 1.网络不可用 → 返回失败占位图
     * 2，开启省流量 & 蜂窝模式 → 返回点击下载占位图
     * 3，开启省流量 & Wifi模式 → 返回正在下载占位图，开始下载
     * 3，关闭省流量 & 蜂窝模式 → 返回正在下载占位图，开始下载
     * 3，关闭省流量 & Wifi模式 → 返回正在下载占位图，开始下载
     */
    void readImage(String articleId, String imgHashCode, String originalUrl);

    String read(String articleId, String imgHashCode, String originalUrl);

    void downImage(String articleId, String imgHashCode, String originalUrl, boolean guessReferer);

    void downFile(String url);

    void openImage(String articleId, String url);

    void openLink(String link);

    void openAudio(String link);

    // String get(String url) throws IOException;
}
