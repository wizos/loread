package me.wizos.loread.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.ArrayMap;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.didichuxing.doraemonkit.DoraemonKit;
import com.elvishew.xlog.XLog;
import com.hjq.toast.ToastUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import me.wizos.loread.App;
import me.wizos.loread.Contract;
import me.wizos.loread.R;
import me.wizos.loread.config.HeaderRefererConfig;
import me.wizos.loread.config.HostBlockConfig;
import me.wizos.loread.config.SaveDirectory;
import me.wizos.loread.config.Test;
import me.wizos.loread.config.article_extract.ArticleExtractConfig;
import me.wizos.loread.config.header_useragent.HeaderUserAgentConfig;
import me.wizos.loread.config.header_useragent.UserAgentConfig;
import me.wizos.loread.config.url_rewrite.UrlRewriteConfig;
import me.wizos.loread.db.Article;
import me.wizos.loread.db.ArticleTag;
import me.wizos.loread.db.Category;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.Feed;
import me.wizos.loread.db.FeedCategory;
import me.wizos.loread.db.Tag;
import me.wizos.loread.db.User;
import me.wizos.loread.network.Getting;
import me.wizos.loread.network.SyncWorker;
import me.wizos.loread.network.api.BaseApi;
import me.wizos.loread.network.api.FeverApi;
import me.wizos.loread.network.api.LocalApi;
import me.wizos.loread.network.api.TinyRSSApi;
import me.wizos.loread.utils.ArticleUtils;
import me.wizos.loread.utils.BackupUtils;
import me.wizos.loread.utils.EncryptUtils;
import me.wizos.loread.utils.FileUtils;
import me.wizos.loread.utils.InputStreamCache;
import me.wizos.loread.utils.PagingUtils;
import me.wizos.loread.utils.StringUtils;
import me.wizos.loread.utils.UriUtils;
import okhttp3.Request;

import static androidx.work.ExistingPeriodicWorkPolicy.KEEP;
import static me.wizos.loread.Contract.SCHEMA_HTTP;
import static me.wizos.loread.Contract.SCHEMA_HTTPS;


public class LabActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lab);
    }

    private MaterialDialog materialDialog;

    public void onClickBackup(View view) {
        new MaterialDialog.Builder(this)
                .title("确定要备份吗？")
                .canceledOnTouchOutside(true)
                .positiveText(R.string.confirm)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        materialDialog = new MaterialDialog.Builder(LabActivity.this)
                                .title("正在处理")
                                .content("请耐心等待下")
                                .progress(true, 0)
                                .canceledOnTouchOutside(false)
                                .progressIndeterminateStyle(false)
                                .show();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                BackupUtils.db(LabActivity.this, BackupUtils.BACKUP);
                            }
                        }).start();
                        materialDialog.dismiss();
                    }
                })
                .show();
    }

    public void onClickRestore(View view) {
        // materialDialog = new MaterialDialog.Builder(this)
        //         .title("正在处理")
        //         .content("请耐心等待下")
        //         .progress(true, 0)
        //         .canceledOnTouchOutside(false)
        //         .progressIndeterminateStyle(false)
        //         .onPositive(new MaterialDialog.SingleButtonCallback() {
        //             @Override
        //             public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
        //                 OkGo.getInstance().cancelAll();
        //                 new Thread(new Runnable() {
        //                     @Override
        //                     public void run() {
        //                         BackupUtils.db(LabActivity.this, BackupUtils.RESTORE);
        //                         materialDialog.dismiss();
        //                     }
        //                 }).start();
        //             }
        //         })
        //         .show();
        new MaterialDialog.Builder(this)
                .title("确定要恢复吗？")
                .canceledOnTouchOutside(true)
                .positiveText(R.string.confirm)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        materialDialog = new MaterialDialog.Builder(LabActivity.this)
                                .title("正在处理")
                                .content("请耐心等待下")
                                .progress(true, 0)
                                .canceledOnTouchOutside(false)
                                .progressIndeterminateStyle(false)
                                .show();

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                BackupUtils.db(LabActivity.this, BackupUtils.RESTORE);
                            }
                        }).start();
                        materialDialog.dismiss();
                    }
                })
                .show();
    }


    public void onClickReadConfig(View view) {
        Test.i().reset();
        HostBlockConfig.i().reset();
        UrlRewriteConfig.i().reset();
        HeaderRefererConfig.i().reset();
        HeaderUserAgentConfig.i().reset();
        UserAgentConfig.i().reset();
        ArticleExtractConfig.i().reset();
        SaveDirectory.i().reset();
        ToastUtils.show("已读取");
    }



    public void startSyncWorkManager(View view) {
        ToastUtils.show("开始 同步WorkManager");
        // Constraints 指明工作何时可以运行
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        PeriodicWorkRequest syncRequest = new PeriodicWorkRequest.Builder(SyncWorker.class, 15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(SyncWorker.TAG,KEEP,syncRequest);
    }

    public void stopWorkManager(View view) {
        ToastUtils.show("取消 WorkManager");
        WorkManager.getInstance(this).cancelAllWorkByTag(SyncWorker.TAG);
    }

    public void switchDoraemonKit(View view){
        if(DoraemonKit.isShow()){
            DoraemonKit.hide();
        }else {
            DoraemonKit.show();
        }
    }
    public void openLink(View view){
        EditText editText = findViewById(R.id.lab_enter_edittext);
        String url = editText.getText().toString();
        if(StringUtils.isEmpty(url)){
            ToastUtils.show("未输入网址，请检查");
            return;
        }
        Intent intent;
        intent = new Intent(this, WebActivity.class);
        intent.setData(Uri.parse(url));
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    public void openLinkInNewPage(View view){
        EditText editText = findViewById(R.id.lab_enter_edittext);
        String url = editText.getText().toString();
        if(StringUtils.isEmpty(url)){
            ToastUtils.show("未输入网址，请检查");
            return;
        }
        Intent intent;
        intent = new Intent(this, Web2Activity.class);
        intent.setData(Uri.parse(url));
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }


    public void openActivity2(View view){
        EditText editText = findViewById(R.id.lab_enter_edittext);
        String url = editText.getText().toString();
        XLog.e( "获取的url：" + url );
        int enterSize = getMatchActivitiesSize(url);
        int wizosSize = getMatchActivitiesSize("https://wizos.me");
        Intent intent;
        if( App.i().getUser().isOpenLinkBySysBrowser() && (url.startsWith(SCHEMA_HTTP) || url.startsWith(SCHEMA_HTTPS)) && enterSize == wizosSize){
            intent = new Intent(LabActivity.this, WebActivity.class);
            intent.setData(Uri.parse(url));
            intent.putExtra("theme", App.i().getUser().getThemeMode());
        }else {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        }
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    public void openActivity1(View view) { // openUrl
        EditText editText = findViewById(R.id.lab_enter_edittext);
        String url = editText.getText().toString();
        if(StringUtils.isEmpty(url)){
            url = "https://blog.wizos.me";
        }
        XLog.e( "获取的url：" + url );
        Intent intent;
        // 使用内置浏览器
        if( App.i().getUser().isOpenLinkBySysBrowser() && (url.startsWith(SCHEMA_HTTP) || url.startsWith(SCHEMA_HTTPS))){
            intent = new Intent(LabActivity.this, WebActivity.class);
            intent.setData(Uri.parse(url));
            //指定类打开
            //intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
            intent.putExtra("theme", App.i().getUser().getThemeMode());
        }else{
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            List<ResolveInfo> activities = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            List<ResolveInfo> activitiesToHide = getPackageManager().queryIntentActivities(new Intent(Intent.ACTION_VIEW, Uri.parse("https://wizos.me")), PackageManager.MATCH_DEFAULT_ONLY);
            XLog.e("数量：" + activities.size() +" , " + activitiesToHide.size());

            if( activities.size() != activitiesToHide.size()){
                HashSet<String> hideApp = new HashSet<>();
                //hideApp.add("com.kingsoft.moffice_pro");
                for (ResolveInfo currentInfo : activitiesToHide) {
                    hideApp.add(currentInfo.activityInfo.packageName);
                    XLog.e("内容1：" + currentInfo.activityInfo.packageName);
                }
                ArrayList<Intent> targetIntents = new ArrayList<>();
                for (ResolveInfo currentInfo : activities) {
                    String packageName = currentInfo.activityInfo.packageName;
                    if (!hideApp.contains(packageName)) {
                        Intent targetIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        targetIntent.setPackage(packageName);
                        targetIntents.add(targetIntent);
                    }
                    XLog.e("内容2：" + packageName);
                }
                if(targetIntents.size() > 0) {
                    intent = Intent.createChooser(targetIntents.remove(0),  getString(R.string.open_with));
                    intent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetIntents.toArray(new Parcelable[] {}));
                } else {
                    ToastUtils.show("No app found");
                }
            }else {
                intent = new Intent(LabActivity.this, WebActivity.class);
                intent.setData(Uri.parse(url));
                intent.putExtra("theme", App.i().getUser().getThemeMode());
            }
        }
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    public void openActivity(View view){
        EditText editText = findViewById(R.id.lab_enter_edittext);
        String url = editText.getText().toString();
        if(StringUtils.isEmpty(url)){
            url = "https://blog.wizos.me";
        }
        XLog.e( "获取的url：" + url );
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        List<ResolveInfo> activitiesToHide = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo currentInfo : activitiesToHide) {
            XLog.e("【MATCH_DEFAULT_ONLY】" + currentInfo.activityInfo.packageName);
        }

        activitiesToHide = getPackageManager().queryIntentActivities(intent, PackageManager.GET_DISABLED_UNTIL_USED_COMPONENTS);
        for (ResolveInfo currentInfo : activitiesToHide) {
            XLog.e("【GET_DISABLED_UNTIL_USED_COMPONENTS】" + currentInfo.activityInfo.packageName);
        }

        activitiesToHide = getPackageManager().queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER);
        for (ResolveInfo currentInfo : activitiesToHide) {
            XLog.e("【GET_RESOLVED_FILTER】" + currentInfo.activityInfo.packageName);
        }


        activitiesToHide = getPackageManager().queryIntentActivities(intent, PackageManager.GET_DISABLED_UNTIL_USED_COMPONENTS);
        for (ResolveInfo currentInfo : activitiesToHide) {
            XLog.e("【GET_DISABLED_UNTIL_USED_COMPONENTS】" + currentInfo.activityInfo.packageName);
        }
    }

    private int getMatchActivitiesSize(String url){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo:list) {
            XLog.e( "适配的包名：" + resolveInfo.activityInfo.packageName );
        }
        return list.size();
    }


    public void onClickClearTags(View view) {
        CoreDB.i().tagDao().clear(App.i().getUser().getId());
        CoreDB.i().articleTagDao().clear(App.i().getUser().getId());
    }

    public void onClickGenTags(View view) {
        String uid = App.i().getUser().getId();
        List<Article> articles = CoreDB.i().articleDao().getNotTagStar(uid,0);
        List<ArticleTag> articleTags = new ArrayList<>();
        XLog.e("设置 没有tag的 数据：" + articles.size() );
        Set<String> tagTitleSet = new HashSet<>();
        for (Article article: articles){
            XLog.e("article feedId：" + article.getFeedId() );
            if(StringUtils.isEmpty(article.getFeedId())){
                continue;
            }
            XLog.e("article 数据：" + article);
            List<Category> categories = CoreDB.i().categoryDao().getByFeedId(uid,article.getFeedId());
            for (Category category:categories) {
                tagTitleSet.add(category.getTitle());
                ArticleTag articleTag = new ArticleTag(uid, article.getId(), category.getTitle());
                articleTags.add(articleTag );
                XLog.e("设置 articleTag 数据：" + articleTag);
            }
        }

        List<Tag> tags = new ArrayList<>(tagTitleSet.size());
        for (String title:tagTitleSet) {
            Tag tag = new Tag();
            tag.setUid(uid);
            tag.setId(title);
            tag.setTitle(title);
            tags.add(tag);
            XLog.e("设置 Tag 数据：" + tag);
        }
        CoreDB.i().tagDao().insert(tags);
        CoreDB.i().articleTagDao().insert(articleTags);
    }

    public void onClickEditHost(View view) {
        User user = App.i().getUser();
        if(user==null){
            ToastUtils.show("当前用户不存在");
            return;
        }

        EditText editText = findViewById(R.id.lab_enter_edittext);
        String url = editText.getText().toString();

        if(!UriUtils.isHttpOrHttpsUrl(url)){
            ToastUtils.show(R.string.invalid_url_hint);
            return;
        }

        user.setHost(url);
        CoreDB.i().userDao().update(user);

        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                // 如果是 ttrss 服务，还得修改uid。
                if(App.i().getApi() instanceof TinyRSSApi || App.i().getApi() instanceof FeverApi){
                    String oldUid = user.getId();
                    String newUid = Contract.PROVIDER_TINYRSS + "_" + (url + "_" + user.getUserId()).hashCode();
                    XLog.i("uid 更新：" + oldUid + " -> " + newUid);

                    App.i().getUser().setId(newUid);
                    CoreDB.i().userDao().update(user);

                    List<Category> categories = CoreDB.i().categoryDao().getAll(oldUid);
                    for (Category ca: categories) {
                        ca.setUid(newUid);
                    }
                    CoreDB.i().categoryDao().update(categories);

                    List<Feed> feeds = CoreDB.i().feedDao().getAll(oldUid);
                    for (Feed feed: feeds) {
                        feed.setUid(newUid);
                    }
                    CoreDB.i().feedDao().update(feeds);

                    List<FeedCategory> feedCategories = CoreDB.i().feedCategoryDao().getAll(oldUid);
                    for (FeedCategory feedCategory: feedCategories) {
                        feedCategory.setUid(newUid);
                    }
                    CoreDB.i().feedCategoryDao().update(feedCategories);

                    List<Article> articles = CoreDB.i().articleDao().getAll(oldUid);
                    for (Article article: articles) {
                        article.setUid(newUid);
                    }
                    CoreDB.i().articleDao().update(articles);

                    List<Tag> tags = CoreDB.i().tagDao().getAll(oldUid);
                    for (Tag tag: tags) {
                        tag.setUid(newUid);
                    }
                    CoreDB.i().tagDao().update(tags);

                    List<ArticleTag> articleTags = CoreDB.i().articleTagDao().getAll(oldUid);
                    for (ArticleTag articleTag: articleTags) {
                        articleTag.setUid(newUid);
                    }
                    CoreDB.i().articleTagDao().update(articleTags);
                    ToastUtils.show("已修改服务器及其数据为：" + url);
                }else {
                    ToastUtils.show("已修改服务器网址为：" + url);
                }
            }
        });
    }



    public void getPlainTextByOkHttp(View view){
        EditText editText = findViewById(R.id.lab_enter_edittext);
        String url = editText.getText().toString();
        if(!UriUtils.isHttpOrHttpsUrl(url)){
            ToastUtils.show("并非url");
        }else {
            EditText headerEdit = findViewById(R.id.lab_enter2_edittext);
            String headerTxt = headerEdit.getText().toString();

            Request.Builder builder = new Request.Builder();
            builder.url(url);
            if(!StringUtils.isEmpty(headerTxt)){
                String[] lines = headerTxt.split("\n");
                for (String line:lines){
                    String[] dict = line.split(":");
                    if(dict.length != 2){
                        continue;
                    }
                    builder.addHeader(dict[0], dict[1]);
                }
            }

            Getting getting = new Getting(url, new Getting.Listener() {
                @Override
                public void onResponse(InputStreamCache inputStreamCache) {
                    show(inputStreamCache.getSting());
                }

                @Override
                public void onFailure(String msg) {
                    ToastUtils.show(msg);
                }
            });
            getting.request(builder.build());
            getting.policy(Getting.ONLY_OKHTTP);
            getting.start();
        }
    }


    public void getPlainTextByWebview(View view){
        EditText editText = findViewById(R.id.lab_enter_edittext);
        String url = editText.getText().toString();
        if(!UriUtils.isHttpOrHttpsUrl(url)){
            ToastUtils.show("并非url");
        }else {
            Getting getting = new Getting(url, new Getting.Listener() {
                @Override
                public void onResponse(InputStreamCache inputStreamCache) {
                    show(inputStreamCache.getSting());
                }

                @Override
                public void onFailure(String msg) {
                    ToastUtils.show(msg);
                }
            });
            getting.policy(Getting.ONLY_WEBVIEW);
            getting.start();
        }
    }

    private void show(String text){
        new MaterialDialog.Builder(this)
                .title("获取的内容")
                .content(text)
                .show();
    }

    public void exeSQL(View view){
        EditText editText = findViewById(R.id.lab_enter_edittext);
        String sql = editText.getText().toString();
        if(StringUtils.isEmpty(sql)){
            ToastUtils.show("SQL语句为空");
        }else {
            // String[] lines = sql.split(";");
            AsyncTask.SERIAL_EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {
                    // for (String line: lines){
                    // }
                    long time = System.currentTimeMillis();
                    CoreDB.i().articleDao().exeSQL(new SimpleSQLiteQuery(sql));
                    XLog.i("耗时：" + (System.currentTimeMillis() - time));
                    ToastUtils.show("SQL耗时：" +  (System.currentTimeMillis() - time));
                }
            });
        }
    }


    public void deleteExpiredArticles(View view) {
        materialDialog = new MaterialDialog.Builder(this)
                .title("正在处理")
                .content("请耐心等待下")
                .progress(true, 0)
                .canceledOnTouchOutside(false)
                .progressIndeterminateStyle(false)
                .show();
        AsyncTask.SERIAL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                App.i().getApi().deleteExpiredArticles();
                materialDialog.dismiss();
            }
        });
    }

    public void deleteRepeatedGuidArticles(View view){
        if(!(App.i().getApi() instanceof LocalApi)){
            return;
        }
        materialDialog = new MaterialDialog.Builder(this)
                .title("正在处理")
                .content("请耐心等待下")
                .progress(true, 0)
                .canceledOnTouchOutside(false)
                .progressIndeterminateStyle(false)
                .show();
        AsyncTask.SERIAL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                List<Article> articles = CoreDB.i().articleDao().getAll(App.i().getUser().getId());
                Set<Article> articleSet = new HashSet<>();

                List<String> idSet = new ArrayList<>();
                List<String> idMd5Set = new ArrayList<>();

                for (Article article : articles) {
                    if(!StringUtils.isEmpty(article.getLink()) && !article.getId().equalsIgnoreCase(EncryptUtils.MD5(article.getLink()))){
                        articleSet.add(article);
                    }
                }

                PagingUtils.slice(new ArrayList<>(articleSet), 50, new PagingUtils.PagingListener<Article>() {
                    @Override
                    public void onPage(@NotNull List<Article> childList) {
                        for (Article article : childList) {
                            if(article.getSaveStatus() == App.STATUS_TO_BE_FILED){
                                if(article.getStarStatus() == App.STATUS_STARED){
                                    article.setSaveStatus(App.STATUS_IS_FILED);
                                    String dir = "/" + SaveDirectory.i().getSaveDir(article.getFeedId(), article.getId()) + "/";
                                    ArticleUtils.saveArticle(App.i().getUserStorePath() + dir, article);
                                }else {
                                    article.setSaveStatus(App.STATUS_IS_FILED);
                                    String dir = SaveDirectory.i().getSaveDir(article.getFeedId(), article.getId()) + "/";
                                    //XLog.d("保存目录：" + dir);
                                    ArticleUtils.saveArticle(App.i().getUserBoxPath() + dir, article);
                                }
                            }
                            if(article.getStarStatus() == App.STATUS_UNSTAR){
                                idSet.add(article.getId());
                                idMd5Set.add(EncryptUtils.MD5(article.getId()));
                            }
                        }
                    }
                });

                FileUtils.deleteHtmlDirList(idMd5Set);
                PagingUtils.slice(idSet, 100, childList -> CoreDB.i().articleDao().delete(App.i().getUser().getId(), childList));

                materialDialog.dismiss();
                ToastUtils.show("清理完成");
            }
        });
    }

    public void deleteMissingHtmlDir(View view) {
        materialDialog = new MaterialDialog.Builder(this)
                .title("正在处理")
                .content("请耐心等待下")
                .progress(true, 0)
                .canceledOnTouchOutside(false)
                .progressIndeterminateStyle(false)
                .show();
        AsyncTask.SERIAL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                List<Article> articles = CoreDB.i().articleDao().getAll(App.i().getUser().getId());
                ArrayMap<String, String> temp = new ArrayMap<>(articles.size());

                for (Article article : articles) {
                    temp.put(EncryptUtils.MD5(article.getId()), "1");
                }

                File dir = new File(App.i().getUserCachePath());
                File[] arts = dir.listFiles();
                XLog.e("文件数量：" + arts.length);
                String x;
                for (File sourceFile : arts) {
                    x = temp.get(sourceFile.getName());
                    if (null == x) {
                        XLog.e("移动文件名：" + "   " + sourceFile.getName());
                        FileUtils.moveDir(sourceFile.getAbsolutePath(), App.i().getUserFilesDir() + "/move/" + sourceFile.getName());
                    }
                }
                materialDialog.dismiss();
                ToastUtils.show("清理完成");
            }
        });
    }
    public void trimCount(View view) {
        long time = System.currentTimeMillis();
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                BaseApi.updateCollectionCount(App.i().getUser().getId());
                XLog.i("整理耗时：" + (System.currentTimeMillis() - time));
            }
        });
    }

    public void trimArticlesCrawlDate(View view) {
        long time = System.currentTimeMillis();
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                CoreDB.i().articleDao().updateCrawlDateToPubDate(App.i().getUser().getId());
                XLog.i("整理耗时：" + (System.currentTimeMillis() - time));
            }
        });
    }

    public void trimUnsubscribedArticles(View view){
        User user = App.i().getUser();
        if(user==null){
            ToastUtils.show("当前用户不存在");
            return;
        }

        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                List<Article> articles = CoreDB.i().articleDao().getUnsubscribed(user.getId());
                for (Article article: articles){
                    Uri uri = Uri.parse(article.getLink());
                    String host = uri.getHost();
                    if(StringUtils.isEmpty(host)){
                        continue;
                    }
                    List<Feed> feeds = CoreDB.i().feedDao().getAllByFeedUrlLike(user.getId(), "%" + host + "%");
                    if(feeds == null || feeds.size() != 1){
                        feeds = CoreDB.i().feedDao().getAllByTitleLike(user.getId(), "%" + article.getFeedTitle() + "%");
                    }

                    if(feeds == null || feeds.size() != 1){
                        feeds = CoreDB.i().feedDao().getAllByTitleLike(user.getId(), "%" + article.getAuthor() + "%");
                    }

                    if(feeds == null || feeds.size() != 1){
                        continue;
                    }

                    Feed feed = feeds.get(0);
                    article.setFeedId(feed.getId());
                    article.setFeedUrl(feed.getFeedUrl());
                    article.setFeedTitle(feed.getTitle());
                }
                CoreDB.i().articleDao().insert(articles);
                ToastUtils.show("处理完成");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}