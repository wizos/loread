package me.wizos.loread.network.api;

import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.collection.ArraySet;

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
import java.util.Iterator;
import java.util.List;

import me.wizos.loread.App;
import me.wizos.loread.BuildConfig;
import me.wizos.loread.Contract;
import me.wizos.loread.R;
import me.wizos.loread.bean.FeedEntries;
import me.wizos.loread.bean.Token;
import me.wizos.loread.bean.feedly.CategoryItem;
import me.wizos.loread.bean.feedly.Collection;
import me.wizos.loread.bean.feedly.Entry;
import me.wizos.loread.bean.feedly.FeedItem;
import me.wizos.loread.bean.feedly.Profile;
import me.wizos.loread.bean.feedly.StreamIds;
import me.wizos.loread.bean.feedly.input.EditCollection;
import me.wizos.loread.bean.feedly.input.EditFeed;
import me.wizos.loread.bean.feedly.input.MarkerAction;
import me.wizos.loread.config.url_rewrite.UrlRewriteConfig;
import me.wizos.loread.db.Article;
import me.wizos.loread.db.Category;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.Feed;
import me.wizos.loread.db.FeedCategory;
import me.wizos.loread.db.User;
import me.wizos.loread.gson.NullOnEmptyConverterFactory;
import me.wizos.loread.network.HttpClientManager;
import me.wizos.loread.network.SyncWorker;
import me.wizos.loread.network.callback.CallbackX;
import me.wizos.loread.utils.Converter;
import me.wizos.loread.utils.StringUtils;
import me.wizos.loread.utils.Tool;
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

public class FeedlyApi extends OAuthApi {
    // private static final String APP_ID = "palabre";
    // private static final String APP_KEY = "FE01H48LRK62325VQVGYOZ24YFZL";
    public static final String OFFICIAL_BASE_URL = "https://feedly.com/v3";
    public static final String REDIRECT_URI = "palabre://feedlyauth";

    // 系统默认的分类
    // user/12cc057f-9891-4ab3-99da-86f2dee7f2f5/category/global.must
    // user/12cc057f-9891-4ab3-99da-86f2dee7f2f5/category/global.uncategorized
    // user/12cc057f-9891-4ab3-99da-86f2dee7f2f5/category/global.all
    // user/12cc057f-9891-4ab3-99da-86f2dee7f2f5/tag/global.unsaved // 取消稍后读
    // user/12cc057f-9891-4ab3-99da-86f2dee7f2f5/tag/global.saved // 稍后读（加星）
    // user/12cc057f-9891-4ab3-99da-86f2dee7f2f5/tag/杂 // tag

    private Retrofit retrofit;
    private FeedlyService service;

    public FeedlyApi() {
        String baseUrl = UrlRewriteConfig.i().getRedirectUrl(FeedlyApi.OFFICIAL_BASE_URL);
        if(StringUtils.isEmpty(baseUrl)){
            baseUrl = FeedlyApi.OFFICIAL_BASE_URL;
        }
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl + "/") // 设置网络请求的Url地址, 必须以/结尾
                .addConverterFactory(new NullOnEmptyConverterFactory()) // 必须在第一个
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().setLenient().create()))  // 设置数据解析器。lenient 为忽略格式错误
                .client(HttpClientManager.i().feedlyHttpClient())
                .build();
        service = retrofit.create(FeedlyService.class);

    }

    @Override
    public void setAuthorization(String authorization) {
        super.setAuthorization(authorization);
    }

    public String getOAuthUrl() {
        String baseUrl = UrlRewriteConfig.i().getRedirectUrl(OFFICIAL_BASE_URL);
        if(StringUtils.isEmpty(baseUrl)){
            baseUrl = OFFICIAL_BASE_URL;
        }

        if (!baseUrl.endsWith("/")) {
            baseUrl = baseUrl + "/";
        }
        return baseUrl + "auth/auth?response_type=code&client_id=palabre&scope=https://cloud.feedly.com/subscriptions&redirect_uri=palabre://feedlyauth&state=/profile";
        // String redirectUri = "loread://oauth";
        // String url = "https://cloud.feedly.com/v3/auth/auth?response_type=code&client_id=" + clientId + "&scope=https://cloud.feedly.com/subscriptions&redirect_uri=" + redirectUri + "&state=/profile";
        // return HOST + "/auth/auth?response_type=code&client_id=" + APP_ID + "&redirect_uri=" + redirectUri + "&state=loread&scope=https://cloud.feedly.com/subscriptions";
    }

    @Override
    public void getAccessToken(String authorizationCode, CallbackX cb) {
        // FormBody.Builder builder = new FormBody.Builder();
        // builder.add("grant_type", "authorization_code");
        // builder.add("code", authorizationCode);
        // builder.add("redirect_uri", REDIRECT_URI);
        // builder.add("client_id", APP_ID);
        // builder.add("client_secret", APP_KEY);
        service.getAccessToken("authorization_code", REDIRECT_URI, BuildConfig.FEEDLY_APP_ID, BuildConfig.FEEDLY_APP_KEY,authorizationCode).enqueue(new Callback<Token>() {
            @Override
            public void onResponse(@NotNull Call<Token> call, @NotNull Response<Token> response) {
                if(!response.isSuccessful()){
                    cb.onFailure(App.i().getString(R.string.response_code, response.code()));
                    return;
                }
                Token token = response.body();
                if(token == null){
                    cb.onFailure(App.i().getString(R.string.return_data_exception));
                    return;
                }
                cb.onSuccess(token);
            }

            @Override
            public void onFailure(@NotNull Call<Token> call, @NotNull Throwable t) {
                cb.onFailure(t.getLocalizedMessage());
            }
        });
    }


    @Override
    public void refreshingAccessToken(String refreshToken, CallbackX cb) {
        service.refreshingAccessToken("refresh_token",refreshToken, BuildConfig.FEEDLY_APP_ID, BuildConfig.FEEDLY_APP_KEY).enqueue(new Callback<Token>() {
            @Override
            public void onResponse(@NotNull Call<Token> call, @NotNull Response<Token> response) {
                if(!response.isSuccessful()){
                    cb.onFailure(App.i().getString(R.string.response_code, response.code()));
                    return;
                }
                Token token = response.body();
                if(token == null){
                    cb.onFailure(App.i().getString(R.string.return_data_exception));
                    return;
                }

                // if(TextUtils.isEmpty(token.getRefresh_token())){
                //     token.setRefresh_token(refreshToken);
                // }
                // User user = App.i().getUser();
                // if (user != null) {
                //     user.setToken(token);
                //     CoreDB.i().userDao().insert(user);
                // }
                // // 更新缓存中的授权
                // ((OAuthApi) App.i().getApi()).setAuthorization(App.i().getUser().getAuth());
                cb.onSuccess(token);
            }

            @Override
            public void onFailure(@NotNull Call<Token> call, @NotNull Throwable t) {
                cb.onFailure(t.getLocalizedMessage());
            }
        });
    }
    public String refreshingAccessToken(String refreshToken) throws IOException {
        Token token = service.refreshingAccessToken("refresh_token", refreshToken, BuildConfig.FEEDLY_APP_ID, BuildConfig.FEEDLY_APP_KEY).execute().body();
        if (TextUtils.isEmpty(token.getRefreshToken())) {
            token.setRefreshToken(refreshToken);
        }
        User user = App.i().getUser();
        if (user != null) {
            user.setToken(token);
            CoreDB.i().userDao().insert(user);
        }
        // 更新缓存中的授权
        ((OAuthApi) App.i().getApi()).setAuthorization(App.i().getUser().getAuth());
        return token.getAuth();
    }

    public void fetchUserInfo(CallbackX cb){
        service.getUserInfo(getAuthorization()).enqueue(new Callback<Profile>() {
            @Override
            public void onResponse(@NonNull Call<Profile> call,@NonNull Response<Profile> response) {
                XLog.e("获取资料：" + response.isSuccessful() );
                if(!response.isSuccessful()){
                    cb.onFailure(App.i().getString(R.string.response_code, response.code()));
                    return;
                }
                Profile profile = response.body();
                if(profile == null){
                    cb.onFailure(App.i().getString(R.string.return_data_exception));
                    return;
                }
                cb.onSuccess(response.body().getUser());
            }
            @Override
            public void onFailure(@NotNull Call<Profile> call, @NotNull Throwable t) {
                cb.onFailure(t.getLocalizedMessage());
            }
        });
    }

    @Override
    public void sync() {
        long startSyncTimeMillis = App.i().getLastShowTimeMillis(); //  + 3600_000
        String uid = App.i().getUser().getId();
        try {
            XLog.e("3 - 同步订阅源信息");
            LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post(getString(R.string.step_sync_feed_info, "3."));

            // 获取分类&feed
            List<Collection> collectionList = service.getCollections(getAuthorization()).execute().body();
            Iterator<Collection> collectionsIterator = collectionList.iterator();
            Collection collection;
            // 本地无该 category
            ArrayList<FeedItem> feedItems;
            ArrayList<Category> categories = new ArrayList<>();
            CategoryItem categoryItem;
            Feed feed;
            Category category;
            ArrayList<FeedCategory> feedCategories = new ArrayList<>();
            ArrayList<Feed> feeds = new ArrayList<>();

            while (collectionsIterator.hasNext()) {
                collection = collectionsIterator.next();
                if (collection.getId().endsWith(App.CATEGORY_MUST)) {
                    collectionsIterator.remove();   //注意这个地方
                    continue;
                }
                categoryItem = collection.getCategoryItem();
                feedItems = collection.getFeedItems();

                category = categoryItem.convert();
                category.setUid(uid);
                categories.add(category);
                for (FeedItem feedItemTmp : feedItems) {
                    feed = feedItemTmp.convert2Feed();
                    feed.setUid(uid);
                    feeds.add(feed);
                    feedCategories.add( new FeedCategory(uid, feedItemTmp.getId(), categoryItem.getId()) );
                }
            }


            // 如果在获取到数据的时候就保存，那么到这里同步断了的话，可能系统内的文章就找不到响应的分组，所有放到这里保存。
            // 覆盖保存，只会保留最新一份。（比如在云端将文章移到的新的分组）
            coverSaveFeeds(feeds);
            coverSaveCategories(categories);
            coverFeedCategory(feedCategories);

            XLog.e(" 2 - 同步未读，加星文章的ids");
            LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post(getString(R.string.step_sync_article_refs, "2."));

            StreamIds tempStreamIds;
            List<String> cloudyRefs;
            // 获取未读资源
            tempStreamIds = new StreamIds();
            cloudyRefs = new ArrayList<>();
            do {
                tempStreamIds = service.getUnreadRefs(getAuthorization(),"user/" + App.i().getUser().getUserId() + App.CATEGORY_ALL, 10000, true, tempStreamIds.getContinuation()).execute().body();
                cloudyRefs.addAll(tempStreamIds.getIds());
            } while (tempStreamIds.getContinuation() != null);
            ArraySet<String> unreadRefsList = handleUnreadRefs(cloudyRefs);

            // 获取加星资源
            tempStreamIds = new StreamIds();
            cloudyRefs = new ArrayList<>();
            do {
                tempStreamIds = service.getStarredRefs(getAuthorization(),"user/" + App.i().getUser().getUserId() + App.CATEGORY_STARED, 10000, tempStreamIds.getContinuation()).execute().body();
                cloudyRefs.addAll(tempStreamIds.getIds());
            } while (tempStreamIds.getContinuation() != null);
            ArraySet<String> staredRefsList = handleStaredRefs(cloudyRefs);


            ArrayList<ArraySet<String>> refsList = splitRefs(unreadRefsList, staredRefsList);
            int allSize = refsList.get(0).size() + refsList.get(1).size() + refsList.get(2).size();

            XLog.e("1 - 同步文章内容");

            // 抓取【未读、未加星】文章
            fetchArticle(allSize, 0, new ArrayList<>(refsList.get(0)), new Converter.ArticleConvertListener() {
                @Override
                public Article onEnd(Article article) {
                    article.setCrawlDate(App.i().getLastShowTimeMillis());
                    article.setReadStatus(App.STATUS_UNREAD);
                    article.setStarStatus(App.STATUS_UNSTAR);
                    article.setUid(uid);
                    return article;
                }
            });
            // 抓取【已读、已加星】文章
            fetchArticle(allSize, refsList.get(0).size(), new ArrayList<>(refsList.get(1)), new Converter.ArticleConvertListener() {
                @Override
                public Article onEnd(Article article) {
                    article.setCrawlDate(App.i().getLastShowTimeMillis());
                    article.setReadStatus(App.STATUS_READED);
                    article.setStarStatus(App.STATUS_STARED);
                    article.setUid(uid);
                    return article;
                }
            });

            // 抓取【未读、已加星】文章
            fetchArticle(allSize, refsList.get(0).size() + refsList.get(1).size(), new ArrayList<>(refsList.get(2)), new Converter.ArticleConvertListener() {
                @Override
                public Article onEnd(Article article) {
                    article.setCrawlDate(App.i().getLastShowTimeMillis());
                    article.setReadStatus(App.STATUS_UNSTAR);
                    article.setStarStatus(App.STATUS_STARED);
                    article.setUid(uid);
                    return article;
                }
            });

            LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post(getString(R.string.clear_article));
            deleteExpiredArticles();

            // 获取文章全文
            LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post(getString(R.string.fetch_article_full_content));
            fetchReadability(uid, startSyncTimeMillis);
            fetchIcon(uid);
            // 执行文章自动处理脚本
            TriggerRuleUtils.exeAllRules(uid, startSyncTimeMillis);
            // 清理无文章的tag
            // clearNotArticleTags(uid);

            // 提示更新完成
            LiveEventBus.get(SyncWorker.NEW_ARTICLE_NUMBER).post(allSize);
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

        handleDuplicateArticles(startSyncTimeMillis);
        updateCollectionCount();
        handleArticleInfo();
        LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post( null );
    }

    private void handleException(Exception e, String msg) {
        XLog.e("同步失败：" + e.getClass() + " = " + msg);
        Tool.printCallStack(e);
        if(Contract.NOT_LOGGED_IN.equalsIgnoreCase(msg)){
            ToastUtils.show(getString(R.string.not_logged_in));
        }else {
            ToastUtils.show(msg);
        }
    }

    public void renameCategory(String categoryId, String targetName, CallbackX cb) {
        editTag(categoryId, targetName,  cb);
    }

    @Override
    public void deleteCategory(String categoryId, CallbackX cb) {
        cb.onFailure(App.i().getString(R.string.server_api_not_supported, Contract.PROVIDER_FEEDLY));
    }
    private void editTag(@NotNull String categoryId, @NotNull String targetName, CallbackX cb) {
        EditCollection editCollection = new EditCollection(categoryId);
        if (!TextUtils.isEmpty(categoryId)) {
            editCollection.setId(categoryId);
        }
        if (!TextUtils.isEmpty(targetName)) {
            editCollection.setLabel(targetName);
        }

        service.editCollections(getAuthorization(), editCollection).enqueue(new Callback<List<Collection>>() {
            @Override
            public void onResponse(Call<List<Collection>> call, Response<List<Collection>> response) {
                if (response.isSuccessful()) {
                    final String newCategoryId = "user/" + App.i().getUser().getUserId() + "/" + targetName;
                    CoreDB.i().categoryDao().updateId(App.i().getUser().getId(), categoryId, newCategoryId);
                    CoreDB.i().feedCategoryDao().updateCategoryId(App.i().getUser().getId(), categoryId, newCategoryId);

                    XLog.i("修改成功：" + response.body().toString());
                    cb.onSuccess(null);
                } else {
                    cb.onFailure(App.i().getString(R.string.response_code, response.code()));
                    XLog.w("修改失败");
                }
            }

            @Override
            public void onFailure(@NotNull Call<List<Collection>> call, @NotNull Throwable t) {
                cb.onFailure(t.getLocalizedMessage());
                XLog.w("修改失败");
            }
        });
    }

    @Override
    public void addFeed(FeedEntries feedEntries, CallbackX cb) {
        service.editFeed(getAuthorization(), Converter.from(feedEntries)).enqueue(new Callback<List<EditFeed>>() {
            @Override
            public void onResponse(@NotNull Call<List<EditFeed>> call, @NotNull Response<List<EditFeed>> response) {
                if (response.isSuccessful()) {
                    XLog.e("添加成功" + response.body().toString());
                    cb.onSuccess(App.i().getString(R.string.subscribe_success_plz_sync));
                } else {
                    cb.onFailure(App.i().getString(R.string.response_code, response.code()));
                }
            }

            @Override
            public void onFailure(@NotNull Call<List<EditFeed>> call, @NotNull Throwable t) {
                cb.onFailure(t.getLocalizedMessage());
                XLog.e("添加失败");
            }
        });
    }

    @Override
    public void renameFeed(String feedId, String targetName, CallbackX cb) {
        //editFeed(feedId, renamedTitle, null, cb);
        EditFeed editFeed = new EditFeed(feedId);
        if (!TextUtils.isEmpty(targetName)) {
            editFeed.setTitle(targetName);
        }

        service.editFeed(getAuthorization(),editFeed).enqueue(new Callback<List<EditFeed>>() {
            @Override
            public void onResponse(Call<List<EditFeed>> call, Response<List<EditFeed>> response) {
                if(!response.isSuccessful()){
                    cb.onFailure(App.i().getString(R.string.response_code, response.code()));
                }else {
                    cb.onSuccess(response);
                }
            }

            @Override
            public void onFailure(Call<List<EditFeed>> call, Throwable t) {
                cb.onFailure(t.getMessage());
            }
        });
    }

    @Override
    public void importOPML(Uri uri, CallbackX cb) {
        cb.onFailure(App.i().getString(R.string.server_api_not_supported, Contract.PROVIDER_FEEDLY));
    }

    @Override
    public void editFeedCategories(List<CategoryItem> lastCategoryItems, EditFeed editFeed, CallbackX cb) {
        service.editFeed(getAuthorization(),editFeed).enqueue(new Callback<List<EditFeed>>() {
            @Override
            public void onResponse(Call<List<EditFeed>> call, Response<List<EditFeed>> response) {
                if(!response.isSuccessful()){
                    cb.onFailure(App.i().getString(R.string.response_code, response.code()));
                }else {
                    cb.onSuccess(null);
                }
            }

            @Override
            public void onFailure(Call<List<EditFeed>> call, Throwable t) {
                cb.onFailure(t.getLocalizedMessage());
            }
        });
    }

    public void unsubscribeFeed(String feedId, CallbackX cb) {
        ArrayList<String> feedIds = new ArrayList<>();
        feedIds.add(feedId);
        service.delFeed(getAuthorization(),feedIds).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() ){
                    String msg = response.body();
                    if( "[]".equals(msg) ){
                        cb.onSuccess(null);
                    }else {
                        cb.onFailure(App.i().getString(R.string.return_data_exception));
                        XLog.w(msg);
                    }
                }else {
                    cb.onFailure(App.i().getString(R.string.response_code, response.code()));
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                cb.onFailure(t.getLocalizedMessage());
            }
        });
    }

    private void markArticles(String action, java.util.Collection<String> ids, CallbackX cb) {
        MarkerAction markerAction = new MarkerAction();
        markerAction.setAction(action);
        markerAction.setType(MarkerAction.TYPE_ENTRIES);
        markerAction.setEntryIds(ids);
        // 成功不返回信息
        service.markers(getAuthorization(), markerAction).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                if (response.isSuccessful() ){
                    cb.onSuccess(null);
                }else {
                    cb.onFailure(App.i().getString(R.string.response_code, response.code()));
                }
            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                cb.onFailure(t.getLocalizedMessage());
                XLog.i("标记失败：" + t.getLocalizedMessage());
            }
        });
    }

    private void markArticle(String action, String articleId, CallbackX cb) {
        List<String> ids = new ArrayList<String>();
        ids.add(articleId);
        markArticles(action, ids,cb);
    }
    @Override
    public void markArticleListReaded(java.util.Collection<String> articleIds, CallbackX cb) {
        markArticles(MarkerAction.MARK_AS_READ, articleIds,cb);
    }


    public void markArticleReaded(String articleId,CallbackX cb) {
        XLog.d("标记已读：" + articleId);
        markArticle(MarkerAction.MARK_AS_READ, articleId, cb);
    }

    public void markArticleUnread(String articleId,CallbackX cb) {
        markArticle(MarkerAction.MARK_AS_UNREAD, articleId, cb);
    }

    public void markArticleStared(String articleId,CallbackX cb) {
        markArticle(MarkerAction.MARK_AS_SAVED, articleId, cb);
    }

    public void markArticleUnstar(String articleId,CallbackX cb) {
        markArticle(MarkerAction.MARK_AS_UNSAVED, articleId, cb);
    }


    private void fetchArticle(int allSize, int syncedSize, List<String> subIds, Converter.ArticleConvertListener articleConverter) throws IOException{
        int needFetchCount = subIds.size();
        int hadFetchCount = 0;

        while (needFetchCount > 0) {
            int fetchUnit = Math.min(needFetchCount, fetchContentCntForEach);
            List<Entry> items = service.getItemContents(getAuthorization(), subIds.subList(hadFetchCount, hadFetchCount = hadFetchCount + fetchUnit)).execute().body();
            if(items == null){
                break;
            }
            List<Article> tempArticleList = new ArrayList<>(fetchUnit);
            for (Entry item : items) {
                tempArticleList.add(Converter.from(item, articleConverter));
            }
            CoreDB.i().articleDao().insert(tempArticleList);
            LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post( App.i().getString(R.string.step_sync_article_content, "1.", syncedSize = syncedSize + fetchUnit, allSize) );
            needFetchCount = subIds.size() - hadFetchCount;
        }
    }

    /**
     * 将 未读资源 和 加星资源，去重分为3组
     */
    private ArrayList<ArraySet<String>> splitRefs(ArraySet<String> tempUnreadIds, ArraySet<String> tempStarredIds) {
        // XLog.d("【reRefs1】云端未读" + tempUnreadIds.size() + "，云端加星" + tempStarredIds.size());
        int total = Math.min(tempUnreadIds.size(), tempStarredIds.size());

        ArraySet<String> reUnreadUnstarRefs;
        ArraySet<String> reReadStarredRefs = new ArraySet<>(tempStarredIds.size());
        ArraySet<String> reUnreadStarredRefs = new ArraySet<>(total);

        for (String id : tempStarredIds) {
            if (tempUnreadIds.contains(id)) {
                tempUnreadIds.remove(id);
                reUnreadStarredRefs.add(id);
            } else {
                reReadStarredRefs.add(id);
            }
        }
        reUnreadUnstarRefs = tempUnreadIds;

        ArrayList<ArraySet<String>> refsList = new ArrayList<>();
        refsList.add(reUnreadUnstarRefs);
        refsList.add(reReadStarredRefs);
        refsList.add(reUnreadStarredRefs);
        // XLog.d("【reRefs2】" + reUnreadUnstarRefs.size() + "--" + reReadStarredRefs.size() + "--" + reUnreadStarredRefs.size());
        return refsList;
    }
}
