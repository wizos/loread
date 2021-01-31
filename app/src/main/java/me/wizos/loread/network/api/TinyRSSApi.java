package me.wizos.loread.network.api;

import android.text.TextUtils;

import com.elvishew.xlog.XLog;
import com.google.gson.GsonBuilder;
import com.hjq.toast.ToastUtils;
import com.jeremyliao.liveeventbus.LiveEventBus;
import com.lzy.okgo.exception.HttpException;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import me.wizos.loread.App;
import me.wizos.loread.Contract;
import me.wizos.loread.R;
import me.wizos.loread.activity.login.LoginResult;
import me.wizos.loread.bean.feedly.input.EditFeed;
import me.wizos.loread.bean.ttrss.request.GetCategories;
import me.wizos.loread.bean.ttrss.request.GetFeeds;
import me.wizos.loread.bean.ttrss.request.GetHeadlines;
import me.wizos.loread.bean.ttrss.request.Login;
import me.wizos.loread.bean.ttrss.request.SubscribeToFeed;
import me.wizos.loread.bean.ttrss.request.UnsubscribeFeed;
import me.wizos.loread.bean.ttrss.request.UpdateArticle;
import me.wizos.loread.bean.ttrss.result.ArticleItem;
import me.wizos.loread.bean.ttrss.result.CategoryItem;
import me.wizos.loread.bean.ttrss.result.FeedItem;
import me.wizos.loread.bean.ttrss.result.SubscribeToFeedResult;
import me.wizos.loread.bean.ttrss.result.TTRSSLoginResult;
import me.wizos.loread.bean.ttrss.result.TinyResponse;
import me.wizos.loread.bean.ttrss.result.UpdateArticleResult;
import me.wizos.loread.db.Article;
import me.wizos.loread.db.Category;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.Feed;
import me.wizos.loread.db.FeedCategory;
import me.wizos.loread.network.HttpClientManager;
import me.wizos.loread.network.SyncWorker;
import me.wizos.loread.network.callback.CallbackX;
import me.wizos.loread.utils.StringUtils;
import me.wizos.loread.utils.TriggerRuleUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static me.wizos.loread.utils.StringUtils.getString;

/**
 * Created by Wizos on 2019/2/8.
 */

public class TinyRSSApi extends AuthApi implements ILogin {
    private TinyRSSService service;

    public TinyRSSApi(String baseUrl) {
        String tempBaseUrl;
        if (!TextUtils.isEmpty(baseUrl)) {
            tempBaseUrl = baseUrl;
        }else {
            tempBaseUrl = "https://example.com";
            ToastUtils.show(R.string.empty_site_url_hint);
        }

        if (!tempBaseUrl.endsWith("/")) {
            tempBaseUrl = tempBaseUrl + "/";
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(tempBaseUrl) // 设置网络请求的Url地址, 必须以/结尾
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().setLenient().create()))  // 设置数据解析器
                .client(HttpClientManager.i().ttrssHttpClient())
                .build();
        service = retrofit.create(TinyRSSService.class);
    }

    public LoginResult login(String account, String password) throws IOException {
        Login loginParam = new Login();
        loginParam.setUser(account);
        loginParam.setPassword(password);
        TinyResponse<TTRSSLoginResult> loginResultResponse = service.login(loginParam).execute().body();
        LoginResult loginResult = new LoginResult();
        if (loginResultResponse.isSuccessful()) {
            return loginResult.setSuccess(true).setData(loginResultResponse.getContent().getSession_id());
        } else {
            return loginResult.setSuccess(false).setData(loginResultResponse.getContent().getSession_id());
        }
    }

    public void login(String account, String password, CallbackX cb){
        Login loginParam = new Login();
        loginParam.setUser(account);
        loginParam.setPassword(password);
        service.login(loginParam).enqueue(new retrofit2.Callback<TinyResponse<TTRSSLoginResult>>() {
            @Override
            public void onResponse(retrofit2.Call<TinyResponse<TTRSSLoginResult>> call, Response<TinyResponse<TTRSSLoginResult>> response) {
                if(response.isSuccessful()){
                    TinyResponse<TTRSSLoginResult> loginResultResponse = response.body();
                    if(loginResultResponse != null &&  loginResultResponse.isSuccessful()){
                        cb.onSuccess(loginResultResponse.getContent().getSession_id());
                        return;
                    }
                    cb.onFailure(App.i().getString(R.string.login_failed_reason, loginResultResponse.toString()));
                }else {
                    cb.onFailure(App.i().getString(R.string.login_failed_reason, response.message()));
                }
            }

            @Override
            public void onFailure(retrofit2.Call<TinyResponse<TTRSSLoginResult>> call, Throwable t) {
                cb.onFailure(App.i().getString(R.string.login_failed_reason, t.getMessage()));
            }
        });
    }

    public void fetchUserInfo(CallbackX cb){
        cb.onFailure(App.i().getString(R.string.temporarily_not_supported));
    }

    @Override
    public void sync() {
        try {
            long startSyncTimeMillis = System.currentTimeMillis() + 3600_000;
            // TinyResponse<ApiLevel> apiLevelTinyResponse = service.getApiLevel(new GetApiLevel(getAuthorization())).execute().body();
            // if (!apiLevelTinyResponse.isSuccessful()) {
            //     throw new HttpException(apiLevelTinyResponse.getMsg());
            // }
            // int apiVersion = apiLevelTinyResponse.getContent().getLevel();
            // XLog.i("同步 - 获取服务器api等级：" + apiVersion);
            String uid = App.i().getUser().getId();

            XLog.i("同步 - 获取分类");
            LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post(getString(R.string.sync_feed_info, "2."));

            // 获取分类
            TinyResponse<List<CategoryItem>> categoryItemsResponse = service.getCategories( new GetCategories(getAuthorization()) ).execute().body();
            // XLog.v("分类请求响应：" + categoryItemsTTRSSResponse);
            if (!categoryItemsResponse.isSuccessful()) {
                throw new HttpException("获取分类失败 - " + categoryItemsResponse.getMsg());
            }

            Iterator<CategoryItem> categoryItemsIterator = categoryItemsResponse.getContent().iterator();
            CategoryItem categoryItem;
            ArrayList<Category> categories = new ArrayList<>();
            while (categoryItemsIterator.hasNext()) {
                categoryItem = categoryItemsIterator.next();
                if (Integer.parseInt(categoryItem.getId()) < 1) {
                    continue;
                }
                categories.add(categoryItem.convert());
            }

            // 获取feed
            XLog.i("同步 - 获取订阅源");
            TinyResponse<List<FeedItem>> feedItemsResponse = service.getFeeds(new GetFeeds(getAuthorization())).execute().body();
            if (!feedItemsResponse.isSuccessful()) {
                throw new HttpException(feedItemsResponse.getMsg());
            }

            Iterator<FeedItem> feedItemsIterator = feedItemsResponse.getContent().iterator();
            FeedItem feedItem;
            ArrayList<Feed> feeds = new ArrayList<>();
            ArrayList<FeedCategory> feedCategories = new ArrayList<>(feedItemsResponse.getContent().size());
            FeedCategory feedCategoryTmp;

            while (feedItemsIterator.hasNext()) {
                feedItem = feedItemsIterator.next();
                Feed feed = feedItem.convert();
                feeds.add(feed);
                feedCategoryTmp = new FeedCategory(uid, String.valueOf(feedItem.getId()), String.valueOf(feedItem.getCatId()));
                feedCategories.add(feedCategoryTmp);
            }

            // 如果在获取到数据的时候就保存，那么到这里同步断了的话，可能系统内的文章就找不到响应的分组，所有放到这里保存。
            // 覆盖保存，只会保留最新一份。（比如在云端将文章移到的新的分组）
            coverSaveFeeds(feeds);
            coverSaveCategories(categories);
            coverFeedCategory(feedCategories);

            int hadFetchCount = 0;

            LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post( App.i().getString(R.string.sync_article_start, "1.") );
            GetHeadlines getHeadlines = new GetHeadlines();
            getHeadlines.setSid(getAuthorization());
            int sinceId = CoreDB.i().articleDao().getLastArticleId(uid);
            getHeadlines.setSince_id(sinceId + "");

            TinyResponse<List<ArticleItem>> articleItemsResponse;
            ArrayList<Article> articleList;
            int fetchArticlesUnit;
            do{
                XLog.i("1 - 同步文章 " + uid + " , Since_id = " + sinceId);
                getHeadlines.setSkip(hadFetchCount);
                articleItemsResponse = service.getHeadlines(getHeadlines).execute().body();
                if (!articleItemsResponse.isSuccessful()) {
                    throw new HttpException("获取文章失败 - " + articleItemsResponse.getMsg());
                }
                List<ArticleItem> items = articleItemsResponse.getContent();
                articleList = new ArrayList<>(items.size());
                // long syncTimeMillis = System.currentTimeMillis();
                for (ArticleItem item : items) {
                    articleList.add(item.convert(new ArticleChanger() {
                        @Override
                        public Article change(Article article) {
                            article.setCrawlDate(startSyncTimeMillis);
                            article.setUid(uid);
                            return article;
                        }
                    }));
                }
                fetchArticlesUnit = articleList.size();
                hadFetchCount = hadFetchCount + fetchArticlesUnit;
                CoreDB.i().articleDao().insert(articleList);
                LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post( App.i().getString(R.string.sync_article_size, "1.", hadFetchCount) );
            }while (fetchArticlesUnit == fetchContentCntForEach);

            LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post(getString(R.string.clear_article));
            deleteExpiredArticles();

            // 获取文章全文
            LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post(getString(R.string.fetch_article_full_content));
            fetchReadability(uid, startSyncTimeMillis);
            // 执行文章自动处理脚本
            TriggerRuleUtils.exeAllRules(uid,startSyncTimeMillis);

            // 提示更新完成
            LiveEventBus.get(SyncWorker.NEW_ARTICLE_NUMBER).post(hadFetchCount);
        }catch (IllegalStateException e){
            handleException(e, e.getMessage());
        }catch (HttpException e) {
            handleException(e, e.message());
        } catch (ConnectException e) {
            handleException(e, "同步失败：Connect异常");
        } catch (SocketTimeoutException e) {
            handleException(e, "同步失败：Socket超时");
        } catch (IOException e) {
            handleException(e, "同步失败：IO异常");
        } catch (RuntimeException e) {
            handleException(e, "同步失败：Runtime异常");
        }

        handleDuplicateArticles();
        handleCrawlDate2();
        updateCollectionCount();
        LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post( null );
    }

    private void handleException(Exception e, String msg) {
        XLog.e("同步失败：" + e.getClass() + " = " + msg);
        e.printStackTrace();
        if(Contract.NOT_LOGGED_IN.equalsIgnoreCase(msg)){
            ToastUtils.show(getString(R.string.not_logged_in));
        }else {
            ToastUtils.show(msg);
        }
    }

    @Override
    public void renameTag(String tagId, String targetName, CallbackX cb) {
        cb.onFailure(App.i().getString(R.string.server_api_not_supported, Contract.PROVIDER_TINYRSS));
    }

    public void addFeed(EditFeed editFeed, CallbackX cb) {
        SubscribeToFeed subscribeToFeed = new SubscribeToFeed(getAuthorization());
        subscribeToFeed.setFeed_url(editFeed.getId());
        if (editFeed.getCategoryItems() != null && editFeed.getCategoryItems().size() != 0) {
            subscribeToFeed.setCategory_id(editFeed.getCategoryItems().get(0).getId());
        }
        service.subscribeToFeed(subscribeToFeed).enqueue(new retrofit2.Callback<TinyResponse<SubscribeToFeedResult>>() {
            @Override
            public void onResponse(@NotNull retrofit2.Call<TinyResponse<SubscribeToFeedResult>> call, @NotNull Response<TinyResponse<SubscribeToFeedResult>> response) {
                if (response.isSuccessful() && response.body().isSuccessful()) {
                    XLog.v("添加成功" + response.body().toString());
                    cb.onSuccess("添加成功");
                } else {
                    cb.onFailure("响应失败");
                }
            }

            @Override
            public void onFailure(@NotNull retrofit2.Call<TinyResponse<SubscribeToFeedResult>> call, @NotNull Throwable t) {
                cb.onFailure("添加失败");
                XLog.v("添加失败");
            }
        });
    }

    @Override
    public void renameFeed(String feedId, String renamedTitle, CallbackX cb) {
        cb.onFailure(App.i().getString(R.string.server_api_not_supported, Contract.PROVIDER_TINYRSS));
    }

    // /**
    //  * 订阅，编辑feed
    //  *
    //  * @param feedId
    //  * @param feedTitle
    //  * @param categoryItems
    //  * @param cb
    //  */
    // public void editFeed(@NonNull String feedId, @Nullable String feedTitle, @Nullable ArrayList<me.wizos.loread.bean.feedly.CategoryItem> categoryItems, StringCallback cb) {
    // }


    @Override
    public void editFeedCategories(List<me.wizos.loread.bean.feedly.CategoryItem> lastCategoryItems, EditFeed editFeed, CallbackX cb) {
        cb.onFailure(App.i().getString(R.string.server_api_not_supported, Contract.PROVIDER_TINYRSS));
    }

    public void unsubscribeFeed(String feedId, CallbackX cb) {
        UnsubscribeFeed unsubscribeFeed = new UnsubscribeFeed(getAuthorization());
        unsubscribeFeed.setFeedId(Integer.parseInt(feedId));
        service.unsubscribeFeed(unsubscribeFeed).enqueue(new retrofit2.Callback<TinyResponse<Map>>() {
            @Override
            public void onResponse(@NotNull retrofit2.Call<TinyResponse<Map>> call, @NotNull Response<TinyResponse<Map>> response) {
                if(response.isSuccessful() && null != response.body() && null != response.body().getContent() && "OK".equals(response.body().getContent().get("status"))){
                    if(cb!=null){
                        cb.onSuccess(null);
                    }
                }else {
                    if(cb!=null){
                        cb.onFailure(response.body());
                    }
                }
            }

            @Override
            public void onFailure(@NotNull retrofit2.Call<TinyResponse<Map>> call, @NotNull Throwable t) {
                cb.onFailure(t.getMessage());
            }
        });
    }


    private void markArticles(int field, int mode, String articleIds, CallbackX cb) {
        UpdateArticle updateArticle = new UpdateArticle(getAuthorization());
        updateArticle.setArticle_ids(articleIds);
        updateArticle.setField(field);
        updateArticle.setMode(mode);
        service.updateArticle(updateArticle).enqueue(new Callback<TinyResponse<UpdateArticleResult>>() {
            @Override
            public void onResponse(@NotNull Call<TinyResponse<UpdateArticleResult>> call, @NotNull Response<TinyResponse<UpdateArticleResult>> response) {
                if (response.isSuccessful() ){
                    if(cb!=null){
                        cb.onSuccess(null);
                    }
                }else {
                    if(cb!=null){
                        cb.onFailure(getString(R.string.response_fail));
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<TinyResponse<UpdateArticleResult>> call, @NotNull Throwable t) {
                if(cb!=null){
                    cb.onFailure(t.getMessage());
                }
            }
        });
    }


    public void markArticleListReaded(Collection<String> articleIds, CallbackX cb) {
        markArticles(2, 0, StringUtils.join(",", articleIds), cb);
    }

    public void markArticleReaded(String articleId, CallbackX cb) {
        markArticles(2, 0, articleId, cb);
    }

    public void markArticleUnread(String articleId, CallbackX cb) {
        markArticles(2, 1, articleId, cb);
    }

    public void markArticleStared(String articleId, CallbackX cb) {
        markArticles(0, 1, articleId, cb);
    }

    public void markArticleUnstar(String articleId,CallbackX cb) {
        markArticles(0, 0, articleId, cb);
    }
}
