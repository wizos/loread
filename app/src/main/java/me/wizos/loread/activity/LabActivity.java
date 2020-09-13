package me.wizos.loread.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.socks.library.KLog;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.config.AdBlock;
import me.wizos.loread.config.ArticleActionConfig;
import me.wizos.loread.config.LinkRewriteConfig;
import me.wizos.loread.config.NetworkRefererConfig;
import me.wizos.loread.config.NetworkUserAgentConfig;
import me.wizos.loread.config.TestConfig;
import me.wizos.loread.db.Article;
import me.wizos.loread.db.ArticleTag;
import me.wizos.loread.db.Category;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.Tag;
import me.wizos.loread.db.User;
import me.wizos.loread.network.SyncWorker;
import me.wizos.loread.utils.EncryptUtil;
import me.wizos.loread.utils.FileUtil;
import me.wizos.loread.utils.StringUtils;

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
        materialDialog = new MaterialDialog.Builder(this)
                .title("正在处理")
                .content("请耐心等待下")
                .progress(true, 0)
                .canceledOnTouchOutside(false)
                .progressIndeterminateStyle(false)
                .show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileUtil.backup();
                materialDialog.dismiss();
            }
        }).start();

    }

    public void onClickRestore(View view) {
        materialDialog = new MaterialDialog.Builder(this)
                .title("正在处理")
                .content("请耐心等待下")
                .progress(true, 0)
                .canceledOnTouchOutside(false)
                .progressIndeterminateStyle(false)
                .show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileUtil.restore();
                materialDialog.dismiss();
            }
        }).start();
    }


    public void onClickReadConfig(View view) {
        materialDialog = new MaterialDialog.Builder(this)
                .content("正在读取")
                .progress(true, 0)
                .canceledOnTouchOutside(false)
                .progressIndeterminateStyle(false)
                .show();
        TestConfig.i().reset();
        AdBlock.i().reset();
        LinkRewriteConfig.i().reset();
        NetworkRefererConfig.i().reset();
        NetworkUserAgentConfig.i().reset();
        // UserConfig.i().reset();
        materialDialog.dismiss();
    }


    public void onClickArrangeCrawlDateArticle(View view) {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                List<Article> articles = CoreDB.i().articleDao().getAllNoOrder(App.i().getUser().getId());
                for (Article article:articles) {
                    article.setCrawlDate(article.getPubDate());
                }
                CoreDB.i().articleDao().update(articles);
                KLog.i("整理完成：" + articles.size());
            }
        });
    }

    public void onClickClearHtmlDir(View view) {
        AsyncTask.SERIAL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                clearHtmlDir();
            }
        });
    }

    /**
     * 将某些没有被清理到的缓存文件夹给清理掉
     */
    private void clearHtmlDir() {
        //List<Article> articles = WithDB.i().getArtsAllNoOrder();
        List<Article> articles = CoreDB.i().articleDao().getAllNoOrder(App.i().getUser().getId());
        ArrayMap<String, String> temp = new ArrayMap<>(articles.size());

        for (Article article : articles) {
            temp.put(EncryptUtil.MD5(article.getId()), "1");
        }


        File dir = new File(App.i().getUserCachePath());
        File[] arts = dir.listFiles();
        KLog.e("文件数量：" + arts.length);
        String x = "";
        for (File sourceFile : arts) {
            x = temp.get(sourceFile.getName());
            if (null == x) {
                KLog.e("移动文件名：" + "   " + sourceFile.getName());
                FileUtil.moveDir(sourceFile.getAbsolutePath(), App.i().getUserFilesDir() + "/move/" + sourceFile.getName());
            }
        }
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
        WorkManager.getInstance(this).cancelAllWork();
    }


    public void openActivity(View view){
        EditText editText = findViewById(R.id.lab_enter_edittext);
        String url = editText.getText().toString();
        KLog.e( "获取的url：" + url );
        int enterSize = getMatchActivitiesSize(url);
        int wizosSize = getMatchActivitiesSize("https://wizos.me");
        Intent intent;
        if( !App.i().getUser().isOpenLinkBySysBrowser() && (url.startsWith(SCHEMA_HTTP) || url.startsWith(SCHEMA_HTTPS)) && enterSize == wizosSize){
            intent = new Intent(LabActivity.this, WebActivity.class);
            intent.setData(Uri.parse(url));
            intent.putExtra("theme", App.i().getUser().getThemeMode());
        }else {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        }
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private int getMatchActivitiesSize(String url){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo:list) {
            KLog.e( "适配的包名：" + resolveInfo.activityInfo.packageName );
        }
        return list.size();
    }

//    public void loginAccount(){
//        // TODO: 2020/4/14 开始模拟登录
//        if(!Config.i().enableAuth){
//            return;
//        }
//        handleAccount();
//
//        Account account = new Account(getString(R.string.app_name),ACCOUNT_TYPE);
//        // 帐户密码和信息这里用null演示
//        mAccountManager.addAccountExplicitly(account, null, null);
//        // 自动同步
//        Bundle bundle= new Bundle();
//        ContentResolver.setIsSyncable(account, AccountProvider.AUTHORITY, 1);
//        ContentResolver.setSyncAutomatically(account, AccountProvider.AUTHORITY,true);
//        ContentResolver.addPeriodicSync(account, AccountProvider.AUTHORITY,bundle, 30);    // 间隔时间为30秒
//        // 手动同步
////        ContentResolver.requestSync(account, AccountProvider.AUTHORITY, bundle);
////        finish();
//    }

    public void onClickClearTags(View view) {
        CoreDB.i().tagDao().clear(App.i().getUser().getId());
        CoreDB.i().articleTagDao().clear(App.i().getUser().getId());
    }

    public void onClickGenTags(View view) {
        String uid = App.i().getUser().getId();
        List<Article> articles = CoreDB.i().articleDao().getNotTagStar(uid,0);
        List<ArticleTag> articleTags = new ArrayList<>();
        KLog.e("设置 没有tag的 数据：" + articles.size() );
        Set<String> tagTitleSet = new HashSet<>();
        for (Article article: articles){
            KLog.e("article feedId：" + article.getFeedId() );
            if(StringUtils.isEmpty(article.getFeedId())){
                continue;
            }
            KLog.e("article 数据：" + article);
            List<Category> categories = CoreDB.i().categoryDao().getByFeedId(uid,article.getFeedId());
            for (Category category:categories) {
                tagTitleSet.add(category.getTitle());
                ArticleTag articleTag = new ArticleTag(uid, article.getId(), category.getId());
                articleTags.add(articleTag );
                KLog.e("设置 articleTag 数据：" + articleTag);
            }
        }

        List<Tag> tags = new ArrayList<>(tagTitleSet.size());
        for (String title:tagTitleSet) {
            Tag tag = new Tag();
            tag.setUid(uid);
            tag.setId(title);
            tag.setTitle(title);
            tags.add(tag);
            KLog.e("设置 Tag 数据：" + tag);
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
        user.setHost(url);
        CoreDB.i().userDao().insert(user);
    }

    public void onClickSearch(View view) {
        EditText editText = findViewById(R.id.lab_enter_edittext);
        String text = editText.getText().toString();
        if(StringUtils.isEmpty(text)){
            ToastUtils.show("请输入关键词");
            return;
        }


        List<Article> articles = CoreDB.i().articleDao().search(App.i().getUser().getId(), text);
        if( articles!=null){
            KLog.e("搜索结果1：" + articles.size());
            KLog.e("搜索结果1：" + articles);
        }
    }

    public void actionArticle(View view){
        User user = App.i().getUser();
        if(user==null){
            ToastUtils.show("当前用户不存在");
            return;
        }
        ArticleActionConfig.i().exeRules(App.i().getUser().getId(), 0);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}