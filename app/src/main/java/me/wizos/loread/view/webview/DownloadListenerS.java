package me.wizos.loread.view.webview;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.webkit.DownloadListener;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;

import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.utils.FileUtil;
import me.wizos.loread.utils.Tool;
import me.wizos.loread.utils.UriUtil;

import static android.content.Context.DOWNLOAD_SERVICE;

/**
 * @author by Wizos on 2018/6/20.
 */

public class DownloadListenerS implements DownloadListener {
    private Activity context;
    private EditText fileNameEditor;
    private WebView webView;

    public DownloadListenerS(Activity context) {
        this.context = context;
    }

    public DownloadListenerS setWebView(WebView webView) {
        this.webView = webView;
        return this;
    }

    @Override
    public void onDownloadStart(final String url, final String userAgent, final String contentDisposition, final String mimeType, final long contentLength) {
        String neutralText = App.i().getString(R.string.copy_download_url);
        if (!TextUtils.isEmpty(mimeType) && webView != null) {
            if (mimeType.toLowerCase().startsWith("video")) {
                neutralText = App.i().getString(R.string.play_the_video);
            } else if (mimeType.toLowerCase().startsWith("audio")&& webView != null) {
                neutralText = App.i().getString(R.string.play_the_audio);
            }
        }

        // KLog.e("下载1：" + url + " , " + userAgent + " , "  + contentDisposition);
        // KLog.e("下载4：", contentDisposition); // attachment; filename=com.android36kr.app_7.4.2_18060821.apk
        // KLog.e("下载2：" + mimeType); //  application/vnd.android.package-archive
        // KLog.e("下载3：" + contentLength);

        MaterialDialog downloadDialog = new MaterialDialog.Builder(context)
                .title(R.string.do_you_want_to_download_files)
                .customView(R.layout.config_download_view, true)
                .neutralText(neutralText)
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (!TextUtils.isEmpty(mimeType)) {
                            if (mimeType.toLowerCase().startsWith("video")&& webView != null) {
                                playVideo(url);
                            } else if (mimeType.toLowerCase().startsWith("audio")&& webView != null) {
                                playAudio(url);
                            } else {
                                copyUrl(url);
                            }
                        }
                    }
                })
                .negativeText(android.R.string.cancel)
                .positiveText(R.string.agree)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                        KLog.e("输入框内容的是：" + fileNameEditor.getText());
                        downloadBySystem(url, fileNameEditor.getText().toString());
                    }
                })
                .show();

        String fileName = UriUtil.guessFileName(url, contentDisposition, mimeType);
        String fileSize = Tool.getNetFileSizeDescription(context, contentLength);

        fileNameEditor = (EditText) downloadDialog.findViewById(R.id.file_name_edit);
        fileNameEditor.setText(fileName);

        TextView fileSizeView = (TextView) downloadDialog.findViewById(R.id.file_size);
        fileSizeView.setText(context.getString(R.string.file_size, fileSize));
    }


    // 作者：落英坠露 ,链接：https://www.jianshu.com/p/6e38e1ef203a
    private void downloadBySystem(String url, String fileName) {
        // 方法1：跳转浏览器下载
        // final Intent intent = new Intent(Intent.ACTION_VIEW);
        // intent.addCategory(Intent.CATEGORY_BROWSABLE);
        // intent.setData(Uri.parse(url));
        // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // 方法2、使用系统的下载服务
        // 指定下载地址
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        // 允许媒体扫描，根据下载的文件类型被加入相册、音乐等媒体库
        request.allowScanningByMediaScanner();
        // 设置通知的显示类型，下载进行时和完成后显示通知
        // Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED 表示在下载过程中通知栏会一直显示该下载的Notification，在下载完成后该Notification会继续显示，直到用户点击该Notification或者消除该Notification。
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        // 设置通知栏的标题，如果不设置，默认使用文件名
//        request.setTitle("This is title");
        // 设置通知栏的描述
//        request.setDescription("This is description");
        // 允许该记录在下载管理界面可见
        request.setVisibleInDownloadsUi(true);
        // 允许在计费流量下下载
        request.setAllowedOverMetered(true);
        // 允许漫游时下载
        request.setAllowedOverRoaming(true);
        // 允许下载的网路类型
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);

        // 设置下载文件保存的路径和文件名。
        // Content-disposition 是 MIME 协议的扩展，MIME 协议指示 MIME 用户代理如何显示附加的文件。当 Internet Explorer 接收到头时，它会激活文件下载对话框，它的文件名框自动填充了头中指定的文件名。（请注意，这是设计导致的；无法使用此功能将文档保存到用户的计算机上，而不向用户询问保存位置。）
//        KLog.e("下载", "文件名：" + fileName);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, FileUtil.getSaveableName(fileName));
//        另外可选一下方法，自定义下载路径
//        request.setDestinationUri()
//        request.setDestinationInExternalFilesDir()
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        // 添加一个下载任务
        downloadManager.enqueue(request);
//        KLog.e("下载", "下载id为：" + downloadId);
    }

    private void playVideo(String url) {
        webView.loadDataWithBaseURL(
                "",
                "<!DOCTYPE html><html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1.0,user-scalable=no'><title>视频</title></head><body><video src='" + url + "' preload='metadata' width='100%' height='auto' controls>你的浏览器不支持HTMl5，无法播放该视频</video></body></html>",
                "text/html",
                "UTF-8",
                null);
    }

    private void playAudio(String url) {
        webView.loadDataWithBaseURL(
                "",
                "<!DOCTYPE html><html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1.0,user-scalable=no'><title>音频</title></head>" +
                        "<body><audio src='" + url + "' preload='metadata' width='100%' height='auto' controls>你的浏览器不支持HTMl5，无法播放该音频</audio></body></html>",
                "text/html",
                "UTF-8",
                null);
    }

    private void copyUrl(String url) {
        // 获取剪贴板管理器
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        // 创建普通字符型ClipData
        ClipData mClipData = ClipData.newPlainText("url", url);
        // 将ClipData内容放到系统剪贴板里。
        cm.setPrimaryClip(mClipData);
        ToastUtils.show("复制成功");
    }
}
