package me.wizos.loread.network.api;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;

import com.google.gson.GsonBuilder;
import com.hjq.toast.ToastUtils;
import com.jeremyliao.liveeventbus.LiveEventBus;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.exception.HttpException;
import com.socks.library.KLog;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.activity.login.LoginResult;
import me.wizos.loread.bean.feedly.input.EditFeed;
import me.wizos.loread.bean.ttrss.request.GetArticles;
import me.wizos.loread.bean.ttrss.request.GetCategories;
import me.wizos.loread.bean.ttrss.request.GetFeeds;
import me.wizos.loread.bean.ttrss.request.GetHeadlines;
import me.wizos.loread.bean.ttrss.request.GetSavedItemIds;
import me.wizos.loread.bean.ttrss.request.GetUnreadItemIds;
import me.wizos.loread.bean.ttrss.request.LoginParam;
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
import me.wizos.loread.config.ArticleActionConfig;
import me.wizos.loread.db.Article;
import me.wizos.loread.db.Category;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.Feed;
import me.wizos.loread.db.FeedCategory;
import me.wizos.loread.network.HttpClientManager;
import me.wizos.loread.network.SyncWorker;
import me.wizos.loread.network.callback.CallbackX;
import me.wizos.loread.utils.StringUtils;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static me.wizos.loread.utils.StringUtils.getString;

/**
 * Created by Wizos on 2019/2/8.
 */

public class LoreadApi extends AuthApi<Feed, me.wizos.loread.bean.feedly.CategoryItem> implements LoginInterface{
    private LoreadService service;
    private static final String EXAMPLE_BASE_URL = "https://example.com";
    private String tempBaseUrl;

    public LoreadApi() {
        this(App.i().getUser().getHost());
    }

    public LoreadApi(String baseUrl) {
        if (!TextUtils.isEmpty(baseUrl)) {
            tempBaseUrl = baseUrl;
        }else {
            tempBaseUrl = EXAMPLE_BASE_URL;
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
        service = retrofit.create(LoreadService.class);
    }

    public LoginResult login(String accountId, String accountPd) throws IOException {
        LoginParam loginParam = new LoginParam();
        loginParam.setUser(accountId);
        loginParam.setPassword(accountPd);
        TinyResponse<TTRSSLoginResult> loginResultTTRSSResponse = service.login(loginParam).execute().body();
        LoginResult loginResult = new LoginResult();
        if (loginResultTTRSSResponse.isSuccessful()) {
            return loginResult.setSuccess(true).setData(loginResultTTRSSResponse.getContent().getSession_id());
        } else {
            return loginResult.setSuccess(false).setData(loginResultTTRSSResponse.getContent().getSession_id());
        }
    }

    public void login(String accountId, String accountPd,CallbackX cb){
        LoginParam loginParam = new LoginParam();
        loginParam.setUser(accountId);
        loginParam.setPassword(accountPd);
        service.login(loginParam).enqueue(new retrofit2.Callback<TinyResponse<TTRSSLoginResult>>() {
            @Override
            public void onResponse(retrofit2.Call<TinyResponse<TTRSSLoginResult>> call, Response<TinyResponse<TTRSSLoginResult>> response) {
                if(response.isSuccessful()){
                    TinyResponse<TTRSSLoginResult> loginResultTTRSSResponse = response.body();
                    if( loginResultTTRSSResponse.isSuccessful()){
                        cb.onSuccess(loginResultTTRSSResponse.getContent().getSession_id());
                        return;
                    }
                    cb.onFailure("登录失败：原因未知" + loginResultTTRSSResponse.toString());
                }else {
                    cb.onFailure("登录失败：原因未知" + response.message());
                }
            }

            @Override
            public void onFailure(retrofit2.Call<TinyResponse<TTRSSLoginResult>> call, Throwable t) {
                cb.onFailure("登录失败：" + t.getMessage());
            }
        });
    }

    public void fetchUserInfo(CallbackX cb){
        cb.onFailure("暂时不支持");
    }

    @Override
    public void sync() {
        try {
            long startSyncTimeMillis = System.currentTimeMillis();
            String uid = App.i().getUser().getId();

            KLog.e("3 - 同步订阅源信息：获取分类");
            LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post(getString(R.string.sync_feed_info));

            // 获取分类
            TinyResponse<List<CategoryItem>> categoryItemsTTRSSResponse = service.getCategories(getAuthorization(),new GetCategories(getAuthorization())).execute().body();
            KLog.e("获取回应：" + categoryItemsTTRSSResponse);
            if (!categoryItemsTTRSSResponse.isSuccessful()) {
                throw new HttpException("获取失败");
            }

            Iterator<CategoryItem> categoryItemsIterator = categoryItemsTTRSSResponse.getContent().iterator();
            CategoryItem TTRSSCategoryItem;
            ArrayList<Category> categories = new ArrayList<>();
            while (categoryItemsIterator.hasNext()) {
                TTRSSCategoryItem = categoryItemsIterator.next();
                if (Integer.parseInt(TTRSSCategoryItem.getId()) < 1) {
                    continue;
                }
                categories.add(TTRSSCategoryItem.convert());
            }

            // 获取feed
            TinyResponse<List<FeedItem>> feedItemsTTRSSResponse = service.getFeeds(getAuthorization(),new GetFeeds(getAuthorization())).execute().body();
            if (!feedItemsTTRSSResponse.isSuccessful()) {
                throw new HttpException("获取失败");
            }

            Iterator<FeedItem> feedItemsIterator = feedItemsTTRSSResponse.getContent().iterator();
            FeedItem ttrssFeedItem;
            ArrayList<Feed> feeds = new ArrayList<>();
            ArrayList<FeedCategory> feedCategories = new ArrayList<>(feedItemsTTRSSResponse.getContent().size());
            FeedCategory feedCategoryTmp;
            while (feedItemsIterator.hasNext()) {
                ttrssFeedItem = feedItemsIterator.next();
                Feed feed = ttrssFeedItem.convert2Feed();
                feed.setUid(uid);
                feeds.add(feed);
                feedCategoryTmp = new FeedCategory(uid, String.valueOf(ttrssFeedItem.getId()), String.valueOf(ttrssFeedItem.getCatId()));
                feedCategories.add(feedCategoryTmp);
            }

            // 如果在获取到数据的时候就保存，那么到这里同步断了的话，可能系统内的文章就找不到响应的分组，所有放到这里保存。
            // 覆盖保存，只会保留最新一份。（比如在云端将文章移到的新的分组）
            coverSaveFeeds(feeds);
            coverSaveCategories(categories);
            coverFeedCategory(feedCategories);

            // 获取所有未读的资源
            KLog.e("2 - 同步文章信息");
            LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post(getString(R.string.sync_article_refs));

            GetHeadlines getHeadlines = new GetHeadlines();
            getHeadlines.setSid(getAuthorization());
            Article article = CoreDB.i().articleDao().getLastArticle(uid);
            if (null != article) {
                getHeadlines.setSince_id(article.getId());
            }


            TinyResponse<String> idsResponse;
            // 获取未读资源
            idsResponse = service.getUnreadItemIds(getAuthorization(),new GetUnreadItemIds(getAuthorization())).execute().body();
            if (!idsResponse.isSuccessful()) {
                throw new HttpException("获取失败");
            }
            KLog.e("未读" + idsResponse.getContent());
            HashSet<String> unreadRefsSet = handleUnreadRefs( idsResponse.getContent().split(",") );

            // 获取加星资源
            GetSavedItemIds getSavedItemIds = new GetSavedItemIds(getAuthorization());
            idsResponse = service.getSavedItemIds(getAuthorization(),getSavedItemIds).execute().body();
            if (!idsResponse.isSuccessful()) {
                throw new HttpException("获取失败");
            }
            HashSet<String> staredRefsSet = handleStaredRefs( idsResponse.getContent().split(",") );


            HashSet<String> idRefsSet = new HashSet<>();
            idRefsSet.addAll(unreadRefsSet);
            idRefsSet.addAll(staredRefsSet);

            KLog.i("文章id资源：" + idRefsSet );
            ArrayList<String> ids = new ArrayList<>(idRefsSet);

            int hadFetchCount, needFetchCount, num;
            ArrayMap<String, ArrayList<Article>> classArticlesMap = new ArrayMap<String, ArrayList<Article>>();

            needFetchCount = ids.size();
            hadFetchCount = 0;

            GetArticles getArticles = new GetArticles(getAuthorization());
            KLog.e("1 - 同步文章内容" + needFetchCount + "   " );

            TinyResponse<List<ArticleItem>> ttrssArticleItemsResponse;
            ArrayList<Article> articles;

            while (needFetchCount > 0) {
                num = Math.min(needFetchCount, fetchContentCntForEach);
                getArticles.setArticleIds( ids.subList(hadFetchCount, hadFetchCount = hadFetchCount + num) );
                ttrssArticleItemsResponse = service.getArticles(getAuthorization(),getArticles).execute().body();
                if (!ttrssArticleItemsResponse.isSuccessful()) {
                    throw new HttpException("获取失败");
                }
                List<ArticleItem> items = ttrssArticleItemsResponse.getContent();
                articles = new ArrayList<>(items.size());
                long syncTimeMillis = System.currentTimeMillis();
                for (ArticleItem item : items) {
                    articles.add(item.convert(new ArticleChanger() {
                        @Override
                        public Article change(Article article) {
                            article.setCrawlDate(syncTimeMillis);
                            article.setUid(uid);
                            return article;
                        }
                    }));
                }

                CoreDB.i().articleDao().insert(articles);
                needFetchCount = ids.size() - hadFetchCount;
                LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post( App.i().getString(R.string.sync_article_content, hadFetchCount, ids.size()) );
            }

            LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post(getString(R.string.clear_article));
            deleteExpiredArticles();
            handleDuplicateArticles();
            handleCrawlDate();
            updateCollectionCount();

            // 获取文章全文
            LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post(getString(R.string.fetch_article_full_content));
            fetchReadability(uid, startSyncTimeMillis);

            // 执行文章自动处理脚本
            ArticleActionConfig.i().exeRules(uid,startSyncTimeMillis);
            // 清理无文章的tag
            //clearNotArticleTags(uid);

            LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post( null );
            // 提示更新完成
            LiveEventBus.get(SyncWorker.NEW_ARTICLE_NUMBER).post(ids.size());
        }catch (IllegalStateException e){
            KLog.e("同步时产生IllegalStateException：" + e.getMessage());
            e.printStackTrace();
            handleException( "同步时产生IllegalStateException：" + e.getMessage() );
        }catch (HttpException e) {
            KLog.e("同步时产生HttpException：" + e.message());
            e.printStackTrace();
            handleException("同步时产生HttpException：" + e.message());
        } catch (ConnectException e) {
            KLog.e("同步时产生异常ConnectException");
            e.printStackTrace();
            handleException("同步时产生异常ConnectException");
        } catch (SocketTimeoutException e) {
            KLog.e("同步时产生异常SocketTimeoutException");
            e.printStackTrace();
            handleException("同步时产生异常SocketTimeoutException");
        } catch (IOException e) {
            KLog.e("同步时产生异常IOException");
            e.printStackTrace();
            handleException("同步时产生异常IOException");
        } catch (RuntimeException e) {
            KLog.e("同步时产生异常RuntimeException");
            e.printStackTrace();
            handleException("同步时产生异常RuntimeException");
        }
    }

    private void handleException(String msg) {
        ToastUtils.show(msg);
        LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post( null );
    }

    @Override
    public void renameTag(String tagId, String targetName, CallbackX cb) {
        cb.onFailure("暂时不支持");
    }

    public void addFeed(EditFeed editFeed, CallbackX cb) {
        SubscribeToFeed subscribeToFeed = new SubscribeToFeed(getAuthorization());
        subscribeToFeed.setFeed_url(editFeed.getId());
        if (editFeed.getCategoryItems() != null && editFeed.getCategoryItems().size() != 0) {
            subscribeToFeed.setCategory_id(editFeed.getCategoryItems().get(0).getId());
        }
        service.subscribeToFeed(getAuthorization(),subscribeToFeed).enqueue(new retrofit2.Callback<TinyResponse<SubscribeToFeedResult>>() {
            @Override
            public void onResponse(@NotNull retrofit2.Call<TinyResponse<SubscribeToFeedResult>> call, @NotNull Response<TinyResponse<SubscribeToFeedResult>> response) {
                if (response.isSuccessful() && response.body().isSuccessful()) {
                    KLog.e("添加成功" + response.body().toString());
                    cb.onSuccess("添加成功");
                } else {
                    cb.onFailure("响应失败");
                }
            }

            @Override
            public void onFailure(@NotNull retrofit2.Call<TinyResponse<SubscribeToFeedResult>> call, @NotNull Throwable t) {
                cb.onFailure("添加失败");
                KLog.e("添加失败");
            }
        });
    }

    @Override
    public void renameFeed(String feedId, String renamedTitle, CallbackX cb) {
        cb.onFailure("暂时不支持");
    }

    /**
     * 订阅，编辑feed
     *
     * @param feedId
     * @param feedTitle
     * @param categoryItems
     * @param cb
     */
    public void editFeed(@NonNull String feedId, @Nullable String feedTitle, @Nullable ArrayList<me.wizos.loread.bean.feedly.CategoryItem> categoryItems, StringCallback cb) {
    }


    @Override
    public void editFeedCategories(List<me.wizos.loread.bean.feedly.CategoryItem> lastCategoryItems, EditFeed editFeed, CallbackX cb) {
        cb.onFailure("暂时不支持");
    }

    public void unsubscribeFeed(String feedId,CallbackX cb) {
        UnsubscribeFeed unsubscribeFeed = new UnsubscribeFeed(getAuthorization());
        unsubscribeFeed.setFeedId(Integer.parseInt(feedId));
        service.unsubscribeFeed(getAuthorization(),unsubscribeFeed).enqueue(new retrofit2.Callback<TinyResponse<Map>>() {
            @Override
            public void onResponse(@NotNull retrofit2.Call<TinyResponse<Map>> call, @NotNull Response<TinyResponse<Map>> response) {
                if(response.isSuccessful() && null != response.body() && null != response.body().getContent() && "OK".equals(response.body().getContent().get("status"))){
                    cb.onSuccess("退订成功");
                }else {
                    cb.onFailure("退订失败" + response.body());
                }
            }

            @Override
            public void onFailure(@NotNull retrofit2.Call<TinyResponse<Map>> call, @NotNull Throwable t) {
                cb.onSuccess("退订失败" + t.getMessage());
            }
        });
    }


    private void markArticles(int field, int mode, List<String> ids,CallbackX cb) {
        UpdateArticle updateArticle = new UpdateArticle(getAuthorization());
        updateArticle.setArticle_ids(StringUtils.join(",", ids));
        updateArticle.setField(field);
        updateArticle.setMode(mode);
        service.updateArticle(getAuthorization(),updateArticle).enqueue(new retrofit2.Callback<TinyResponse<UpdateArticleResult>>() {
            @Override
            public void onResponse(@NotNull retrofit2.Call<TinyResponse<UpdateArticleResult>> call, @NotNull Response<TinyResponse<UpdateArticleResult>> response) {
                if (response.isSuccessful() ){
                    cb.onSuccess(null);
                }else {
                    cb.onFailure("修改失败，原因未知");
                }
            }

            @Override
            public void onFailure(@NotNull retrofit2.Call<TinyResponse<UpdateArticleResult>> call, @NotNull Throwable t) {
                cb.onFailure("修改失败，原因未知");
            }
        });
    }

    private void markArticle(int field, int mode, String articleId,CallbackX cb) {
        UpdateArticle updateArticle = new UpdateArticle(getAuthorization());
        updateArticle.setArticle_ids(articleId);
        updateArticle.setField(field);
        updateArticle.setMode(mode);
        service.updateArticle(getAuthorization(),updateArticle).enqueue(new retrofit2.Callback<TinyResponse<UpdateArticleResult>>() {
            @Override
            public void onResponse(@NotNull retrofit2.Call<TinyResponse<UpdateArticleResult>> call, @NotNull Response<TinyResponse<UpdateArticleResult>> response) {
                if (response.isSuccessful() ){
                    cb.onSuccess(null);
                }else {
                    cb.onFailure("修改失败，原因未知");
                }
            }

            @Override
            public void onFailure(@NotNull retrofit2.Call<TinyResponse<UpdateArticleResult>> call, @NotNull Throwable t) {
                cb.onFailure("修改失败，原因未知");
            }
        });
    }

    public void markArticleListReaded(List<String> articleIds,CallbackX cb) {
        markArticles(2, 0, articleIds, cb);
    }

    public void markArticleReaded(String articleId, CallbackX cb) {
        markArticle(2, 0, articleId, cb);
    }

    public void markArticleUnread(String articleId, CallbackX cb) {
        markArticle(2, 1, articleId, cb);
    }

    public void markArticleStared(String articleId, CallbackX cb) {
        markArticle(0, 1, articleId, cb);
    }

    public void markArticleUnstar(String articleId,CallbackX cb) {
        markArticle(0, 0, articleId, cb);
    }

    private HashSet<String> handleUnreadRefs(String[] ids) {
        KLog.i("处理未读资源：" + ids.length );
        String uid = App.i().getUser().getId();
        List<Article> localUnreadArticles = CoreDB.i().articleDao().getUnreadNoOrder(uid);

        Map<String, Article> localUnreadArticlesMap = new ArrayMap<>(localUnreadArticles.size());
        List<Article> changedArticles = new ArrayList<>();
        // 筛选下来，最终要去云端获取内容的未读Refs的集合
        HashSet<String> tempUnreadIds = new HashSet<>(ids.length);
        // 数据量大的一方
        for (Article article : localUnreadArticles) {
            localUnreadArticlesMap.put(article.getId(), article);
        }
        // 数据量小的一方
        Article article;
        for (String articleId : ids) {
            article = localUnreadArticlesMap.get(articleId);
            if (article != null) {
                localUnreadArticlesMap.remove(articleId);
            } else {
                article = CoreDB.i().articleDao().getById(uid,articleId);
                if (article != null && article.getReadStatus() == App.STATUS_READED) {
                    article.setReadStatus(App.STATUS_UNREAD);
                    changedArticles.add(article);
                } else {
                    // 本地无，而云端有，加入要请求的未读资源
                    tempUnreadIds.add(articleId+"");
                }
            }
        }
        for (Map.Entry<String, Article> entry : localUnreadArticlesMap.entrySet()) {
            if (entry.getKey() != null) {
                article = localUnreadArticlesMap.get(entry.getKey());
                // 本地未读设为已读
                article.setReadStatus(App.STATUS_READED);
                changedArticles.add(article);
            }
        }

        CoreDB.i().articleDao().update(changedArticles);
        return tempUnreadIds;
    }

    private HashSet<String> handleStaredRefs(String[] ids) {
        KLog.i("处理加薪资源：" + ids.length);
        String uid = App.i().getUser().getId();
        List<Article> localStarredArticles = CoreDB.i().articleDao().getStaredNoOrder(uid);

        Map<String, Article> localStarredArticlesMap = new ArrayMap<>(localStarredArticles.size());
        List<Article> changedArticles = new ArrayList<>();
        HashSet<String> tempStarredIds = new HashSet<>(ids.length);

        // 第1步，遍历数据量大的一方A，将其比对项目放入Map中
        for (Article article : localStarredArticles) {
            localStarredArticlesMap.put(article.getId(), article);
        }

        // 第2步，遍历数据量小的一方B。到Map中找，是否含有b中的比对项。有则XX，无则YY
        Article article;
        for (String articleId : ids) {
            article = localStarredArticlesMap.get(articleId);
            if (article != null) {
                localStarredArticlesMap.remove(articleId);
            } else {
                article = CoreDB.i().articleDao().getById(uid,articleId);
                if (article != null) {
                    article.setStarStatus(App.STATUS_STARED);
                    changedArticles.add(article);
                } else {
                    // 本地无，而云远端有，加入要请求的未读资源
                    tempStarredIds.add(articleId);
                }
            }
        }

        for (Map.Entry<String, Article> entry : localStarredArticlesMap.entrySet()) {
            if (entry.getKey() != null) {
                article = localStarredArticlesMap.get(entry.getKey());
                article.setStarStatus(App.STATUS_UNSTAR);
                changedArticles.add(article);// 取消加星
            }
        }

        CoreDB.i().articleDao().update(changedArticles);
        return tempStarredIds;
    }
}
