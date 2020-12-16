package me.wizos.loread.network.api;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArraySet;

import com.elvishew.xlog.XLog;
import com.google.gson.GsonBuilder;
import com.hjq.toast.ToastUtils;
import com.jeremyliao.liveeventbus.LiveEventBus;
import com.lzy.okgo.exception.HttpException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import me.wizos.loread.App;
import me.wizos.loread.R;
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
import me.wizos.loread.config.ArticleActionConfig;
import me.wizos.loread.config.LinkRewriteConfig;
import me.wizos.loread.db.Article;
import me.wizos.loread.db.Category;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.Feed;
import me.wizos.loread.db.FeedCategory;
import me.wizos.loread.db.User;
import me.wizos.loread.network.HttpClientManager;
import me.wizos.loread.network.SyncWorker;
import me.wizos.loread.network.callback.CallbackX;
import me.wizos.loread.utils.StringUtils;
import okhttp3.FormBody;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static me.wizos.loread.utils.StringUtils.getString;

/**
 * Created by Wizos on 2019/2/8.
 */

public class FeedlyApi extends OAuthApi<Feed, CategoryItem> {
    private static final String APP_ID = "palabre";
    private static final String APP_KEY = "FE01H48LRK62325VQVGYOZ24YFZL";
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
        String baseUrl = LinkRewriteConfig.i().getRedirectUrl(FeedlyApi.OFFICIAL_BASE_URL);
        if(StringUtils.isEmpty(baseUrl)){
            baseUrl = FeedlyApi.OFFICIAL_BASE_URL;
        }
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl + "/") // 设置网络请求的Url地址, 必须以/结尾
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().setLenient().create()))  // 设置数据解析器
                .client(HttpClientManager.i().feedlyHttpClient())
                .build();
        service = retrofit.create(FeedlyService.class);
    }

    @Override
    public void setAuthorization(String authorization) {
        super.setAuthorization(authorization);
    }

    public String getOAuthUrl() {
        String baseUrl = LinkRewriteConfig.i().getRedirectUrl(OFFICIAL_BASE_URL);
        if(StringUtils.isEmpty(baseUrl)){
            baseUrl = OFFICIAL_BASE_URL;
        }

        if (!baseUrl.endsWith("/")) {
            baseUrl = baseUrl + "/";
        }
        return baseUrl + "auth/auth?response_type=code&client_id=palabre&scope=https://cloud.feedly.com/subscriptions&redirect_uri=palabre://feedlyauth&state=/profile";
//        String redirectUri = "loread://oauth";
//        String url = "https://cloud.feedly.com/v3/auth/auth?response_type=code&client_id=" + clientId + "&scope=https://cloud.feedly.com/subscriptions&redirect_uri=" + redirectUri + "&state=/profile";
//        return HOST + "/auth/auth?response_type=code&client_id=" + APP_ID + "&redirect_uri=" + redirectUri + "&state=loread&scope=https://cloud.feedly.com/subscriptions";
    }
    public void getAccessToken(String authorizationCode,CallbackX cb) {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("grant_type", "authorization_code");
        builder.add("code", authorizationCode);
        builder.add("redirect_uri", REDIRECT_URI);
        builder.add("client_id", APP_ID);
        builder.add("client_secret", APP_KEY);

        service.getAccessToken("authorization_code", REDIRECT_URI,APP_ID,APP_KEY,authorizationCode).enqueue(new retrofit2.Callback<Token>() {
            @Override
            public void onResponse(retrofit2.Call<Token> call, Response<Token> response) {
                if(response.isSuccessful()){
                    cb.onSuccess(response.body());
                }else {
                    cb.onFailure("失败：" + response.message());
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Token> call, Throwable t) {
                cb.onFailure("失败：" + t.getMessage());
            }
        });
    }


    public void refreshingAccessToken(String refreshToken, CallbackX cb) {
        service.refreshingAccessToken("refresh_token",refreshToken,APP_ID,APP_KEY).enqueue(new retrofit2.Callback<Token>() {
            @Override
            public void onResponse(retrofit2.Call<Token> call, Response<Token> response) {
                if(response.isSuccessful() && response.body()!=null){
                    if (TextUtils.isEmpty(response.body().getRefresh_token())) {
                        response.body().setRefresh_token(refreshToken);
                    }
                    User user = App.i().getUser();
                    if (user != null) {
                        user.setToken(response.body());
                        CoreDB.i().userDao().insert(user);
                    }
                    // 更新缓存中的授权
                    ((FeedlyApi) App.i().getApi()).setAuthorization(App.i().getUser().getAuth());

                    cb.onSuccess(response.body());
                }else {
                    cb.onFailure("失败：" + response.message());
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Token> call, Throwable t) {
                cb.onFailure("失败：" + t.getMessage());
            }
        });
    }
    public String refreshingAccessToken(String refreshToken) throws IOException {
        Token token = service.refreshingAccessToken("refresh_token",refreshToken,APP_ID,APP_KEY).execute().body();
        if (TextUtils.isEmpty(token.getRefresh_token())) {
            token.setRefresh_token(refreshToken);
        }
        User user = App.i().getUser();
        if (user != null) {
            user.setToken(token);
            CoreDB.i().userDao().insert(user);
        }
        // 更新缓存中的授权
        ((FeedlyApi) App.i().getApi()).setAuthorization(App.i().getUser().getAuth());
        return token.getAuth();
    }

    public void fetchUserInfo(CallbackX cb){
        service.getUserInfo(getAuthorization()).enqueue(new retrofit2.Callback<Profile>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<Profile> call,@NonNull Response<Profile> response) {
                XLog.e("获取资料：" + response.isSuccessful() );
                if( response.isSuccessful()){
                    cb.onSuccess(response.body().getUser());
                }else {
                    cb.onFailure("获取失败：" + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<Profile> call,@NonNull Throwable t) {
                cb.onFailure("获取失败：" + t.getMessage());
            }
        });
    }

    @Override
    public void sync() {
        try {
            long syncTimeMillis = System.currentTimeMillis();
            String uid = App.i().getUser().getId();

            XLog.e("3 - 同步订阅源信息");
            LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post(getString(R.string.sync_feed_info, "1."));

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
            LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post(getString(R.string.sync_article_refs, "2."));

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
            fetchArticle(allSize, 0, new ArrayList<>(refsList.get(0)), new ArticleChanger() {
                @Override
                public Article change(Article article) {
                    article.setCrawlDate(System.currentTimeMillis());
                    article.setReadStatus(App.STATUS_UNREAD);
                    article.setStarStatus(App.STATUS_UNSTAR);
                    article.setUid(uid);
                    return article;
                }
            });
            // 抓取【已读、已加星】文章
            fetchArticle(allSize, refsList.get(0).size(), new ArrayList<>(refsList.get(1)), new ArticleChanger() {
                @Override
                public Article change(Article article) {
                    article.setCrawlDate(System.currentTimeMillis());
                    article.setReadStatus(App.STATUS_READED);
                    article.setStarStatus(App.STATUS_STARED);
                    article.setUid(uid);
                    return article;
                }
            });

            // 抓取【未读、已加星】文章
            fetchArticle(allSize, refsList.get(0).size() + refsList.get(1).size(), new ArrayList<>(refsList.get(2)), new ArticleChanger() {
                @Override
                public Article change(Article article) {
                    article.setCrawlDate(System.currentTimeMillis());
                    article.setReadStatus(App.STATUS_UNSTAR);
                    article.setStarStatus(App.STATUS_STARED);
                    article.setUid(uid);
                    return article;
                }
            });

            LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post(getString(R.string.clear_article));
            deleteExpiredArticles();
            handleDuplicateArticles();
            handleCrawlDate();
            updateCollectionCount();

            // 获取文章全文
            LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post(getString(R.string.fetch_article_full_content));
            fetchReadability(uid, syncTimeMillis);

            // 执行文章自动处理脚本
            ArticleActionConfig.i().exeRules(uid, syncTimeMillis);
            // 清理无文章的tag
            //clearNotArticleTags(uid);

            LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post( null );
            // 提示更新完成
            LiveEventBus.get(SyncWorker.NEW_ARTICLE_NUMBER).post(allSize);
        } catch (HttpException e) {
            XLog.e("同步时产生HttpException：" + e.message());
            e.printStackTrace();
            handleException(e);
        } catch (ConnectException e) {
            XLog.e("同步时产生异常ConnectException");
            e.printStackTrace();
            handleException(e);
        } catch (SocketTimeoutException e) {
            XLog.e("同步时产生异常SocketTimeoutException");
            e.printStackTrace();
            handleException(e);
        } catch (IOException e) {
            XLog.e("同步时产生异常IOException");
            e.printStackTrace();
            handleException(e);
        } catch (RuntimeException e) {
            XLog.e("同步时产生异常RuntimeException");
            e.printStackTrace();
            handleException(e);
        }
    }

    private void handleException(Exception e) {
        if (e instanceof HttpException) {
            ToastUtils.show("网络异常：" + e.getMessage());
        } else {
            updateCollectionCount();
        }

        LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post( null );
    }

    public void renameTag(String tagId, String targetName, CallbackX cb) {
        editTag(tagId, targetName,  cb);
    }

    private void editTag(@Nullable String tagId, @Nullable String targetName, CallbackX cb) {
        EditCollection editCollection = new EditCollection(tagId);
        if (!TextUtils.isEmpty(tagId)) {
            editCollection.setId(tagId);
        }
        if (!TextUtils.isEmpty(targetName)) {
            editCollection.setLabel(targetName);
        }

        service.editCollections(getAuthorization(),editCollection).enqueue(new retrofit2.Callback<List<Collection>>() {
            @Override
            public void onResponse(retrofit2.Call<List<Collection>> call, retrofit2.Response<List<Collection>> response) {
                if (response.isSuccessful()) {
                    XLog.e("修改成功" + response.body().toString());
                    cb.onSuccess(null);
                } else {
                    cb.onFailure(null);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<List<Collection>> call, Throwable t) {
                cb.onFailure("修改失败" + t.getMessage());
                XLog.e("修改失败");
            }
        });
    }

    @Override
    public void addFeed(EditFeed editFeed, CallbackX cb) {
        service.editFeed(getAuthorization(),editFeed).enqueue(new retrofit2.Callback<List<EditFeed>>() {
            @Override
            public void onResponse(retrofit2.Call<List<EditFeed>> call, retrofit2.Response<List<EditFeed>> response) {
                if (response.isSuccessful()) {
                    XLog.e("添加成功" + response.body().toString());
                    cb.onSuccess(null);
                } else {
                    cb.onFailure(null);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<List<EditFeed>> call, Throwable t) {
                cb.onFailure(t.getMessage());
                XLog.e("添加失败");
            }
        });
    }

    @Override
    public void renameFeed(String feedId, String feedTitle, CallbackX cb) {
        //editFeed(feedId, renamedTitle, null, cb);
        EditFeed editFeed = new EditFeed(feedId);
        if (!TextUtils.isEmpty(feedTitle)) {
            editFeed.setTitle(feedTitle);
        }

        service.editFeed(getAuthorization(),editFeed).enqueue(new retrofit2.Callback<List<EditFeed>>() {
            @Override
            public void onResponse(retrofit2.Call<List<EditFeed>> call, retrofit2.Response<List<EditFeed>> response) {
                if(!response.isSuccessful()){
                    cb.onFailure("修改失败");
                }else {
                    cb.onSuccess(response);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<List<EditFeed>> call, Throwable t) {
                cb.onFailure(t.getMessage());
            }
        });
    }

    @Override
    public void editFeedCategories(List<CategoryItem> lastCategoryItems, EditFeed editFeed, CallbackX cb) {
        service.editFeed(getAuthorization(),editFeed).enqueue(new retrofit2.Callback<List<EditFeed>>() {
            @Override
            public void onResponse(retrofit2.Call<List<EditFeed>> call, retrofit2.Response<List<EditFeed>> response) {
                if(!response.isSuccessful()){
                    cb.onFailure("修改失败");
                }else {
                    cb.onSuccess(null);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<List<EditFeed>> call, Throwable t) {
                cb.onFailure(t.getMessage());
            }
        });
    }

    public void unsubscribeFeed(String feedId, CallbackX cb) {
        ArrayList<String> feedIds = new ArrayList<>();
        feedIds.add(feedId);
        service.delFeed(getAuthorization(),feedIds).enqueue(new retrofit2.Callback<String>() {
            @Override
            public void onResponse(retrofit2.Call<String> call, retrofit2.Response<String> response) {
                if (response.isSuccessful() ){
                    String msg = response.body();
                    if( "[]".equals(msg) ){
                        cb.onSuccess(null);
                    }else {
                        cb.onFailure(msg);
                    }
                }else {
                    cb.onFailure("修改失败：原因未知");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<String> call, Throwable t) {

            }
        });
    }

    private void markArticles(String action, java.util.Collection<String> ids, CallbackX cb) {
        MarkerAction markerAction = new MarkerAction();
        markerAction.setAction(action);
        markerAction.setType(MarkerAction.TYPE_ENTRIES);
        markerAction.setEntryIds(ids);
        // 成功不返回信息
        service.markers(getAuthorization(),markerAction).enqueue(new retrofit2.Callback<String>() {
            @Override
            public void onResponse(retrofit2.Call<String> call, retrofit2.Response<String> response) {
                if (response.isSuccessful() ){
                    cb.onSuccess(null);
                }else {
                    cb.onFailure("修改失败：原因未知");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<String> call, Throwable t) {
                cb.onFailure("修改失败：原因未知");
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
        XLog.e("标记已读F：" );
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


    private void fetchArticle(int allSize, int syncedSize, List<String> subIds, ArticleChanger articleChanger) throws IOException{
        int needFetchCount = subIds.size();
        int hadFetchCount = 0;

        while (needFetchCount > 0) {
            int fetchUnit = Math.min(needFetchCount, fetchContentCntForEach);
            List<Entry> items = service.getItemContents(getAuthorization(), subIds.subList(hadFetchCount, hadFetchCount = hadFetchCount + fetchUnit)).execute().body();
            List<Article> tempArticleList = new ArrayList<>(fetchUnit);
            for (Entry item : items) {
                tempArticleList.add(item.convert(articleChanger));
            }
            CoreDB.i().articleDao().insert(tempArticleList);
            LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post( App.i().getString(R.string.sync_article_content, "1.", syncedSize = syncedSize + fetchUnit, allSize) );
            needFetchCount = subIds.size() - hadFetchCount;
        }
    }

    // private HashSet<String> handleUnreadRefs(List<String> ids) {
    //     //List<Article> localUnreadArticles = WithDB.i().getArtsUnreadNoOrder();
    //     List<Article> localUnreadArticles = CoreDB.i().articleDao().getUnreadNoOrder(App.i().getUser().getId());
    //     Map<String, Article> localUnreadArticlesMap = new ArrayMap<>(localUnreadArticles.size());
    //     List<Article> changedArticles = new ArrayList<>();
    //     // 筛选下来，最终要去云端获取内容的未读Refs的集合
    //     HashSet<String> tempUnreadIds = new HashSet<>(ids.size());
    //     // 数据量大的一方
    //     for (Article article : localUnreadArticles) {
    //         localUnreadArticlesMap.put(article.getId(), article);
    //     }
    //     // 数据量小的一方
    //     Article article;
    //     for (String articleId : ids) {
    //         article = localUnreadArticlesMap.get(articleId);
    //         if (article != null) {
    //             localUnreadArticlesMap.remove(articleId);
    //         } else {
    //             article = CoreDB.i().articleDao().getById(App.i().getUser().getId(), articleId);
    //             if (article != null && article.getReadStatus() == App.STATUS_READED) {
    //                 article.setReadStatus(App.STATUS_UNREAD);
    //                 changedArticles.add(article);
    //             } else {
    //                 // 本地无，而云端有，加入要请求的未读资源
    //                 tempUnreadIds.add(articleId);
    //             }
    //         }
    //     }
    //     for (Map.Entry<String, Article> entry : localUnreadArticlesMap.entrySet()) {
    //         if (entry.getKey() != null) {
    //             article = localUnreadArticlesMap.get(entry.getKey());
    //             // 本地未读设为已读
    //             article.setReadStatus(App.STATUS_READED);
    //             changedArticles.add(article);
    //         }
    //     }
    //
    //     CoreDB.i().articleDao().update(changedArticles);
    //     return tempUnreadIds;
    // }

    // private HashSet<String> handleStaredRefs(List<String> streamIds) {
    //     List<Article> localStarredArticles = CoreDB.i().articleDao().getStaredNoOrder(App.i().getUser().getId());
    //     Map<String, Article> localStarredArticlesMap = new ArrayMap<>(localStarredArticles.size());
    //     List<Article> changedArticles = new ArrayList<>();
    //     HashSet<String> tempStarredIds = new HashSet<>(streamIds.size());
    //
    //     // 第1步，遍历数据量大的一方A，将其比对项目放入Map中
    //     for (Article article : localStarredArticles) {
    //         localStarredArticlesMap.put(article.getId(), article);
    //     }
    //
    //     // 第2步，遍历数据量小的一方B。到Map中找，是否含有b中的比对项。有则XX，无则YY
    //     Article article;
    //     for (String articleId : streamIds) {
    //         article = localStarredArticlesMap.get(articleId);
    //         if (article != null) {
    //             localStarredArticlesMap.remove(articleId);
    //         } else {
    //             article = CoreDB.i().articleDao().getById(App.i().getUser().getId(), articleId);
    //             if (article != null) {
    //                 article.setStarStatus(App.STATUS_STARED);
    //                 changedArticles.add(article);
    //             } else {
    //                 // 本地无，而云远端有，加入要请求的未读资源
    //                 tempStarredIds.add(articleId);
    //             }
    //         }
    //     }
    //
    //     for (Map.Entry<String, Article> entry : localStarredArticlesMap.entrySet()) {
    //         if (entry.getKey() != null) {
    //             article = localStarredArticlesMap.get(entry.getKey());
    //             article.setStarStatus(App.STATUS_UNSTAR);
    //             changedArticles.add(article);// 取消加星
    //         }
    //     }
    //
    //     CoreDB.i().articleDao().update(changedArticles);
    //     return tempStarredIds;
    // }

    /**
     * 将 未读资源 和 加星资源，去重分为3组
     *
     * @param tempUnreadIds
     * @param tempStarredIds
     * @return
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
