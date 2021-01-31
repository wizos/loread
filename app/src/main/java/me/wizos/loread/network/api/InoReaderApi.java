package me.wizos.loread.network.api;

import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;
import androidx.collection.ArraySet;

import com.elvishew.xlog.XLog;
import com.hjq.toast.ToastUtils;
import com.jeremyliao.liveeventbus.LiveEventBus;
import com.lzy.okgo.exception.HttpException;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.bean.Token;
import me.wizos.loread.bean.feedly.CategoryItem;
import me.wizos.loread.bean.feedly.input.EditFeed;
import me.wizos.loread.bean.inoreader.ItemIds;
import me.wizos.loread.bean.inoreader.ItemRefs;
import me.wizos.loread.bean.inoreader.LoginResult;
import me.wizos.loread.bean.inoreader.SubCategories;
import me.wizos.loread.bean.inoreader.Subscription;
import me.wizos.loread.bean.inoreader.UserInfo;
import me.wizos.loread.bean.inoreader.itemContents.Item;
import me.wizos.loread.db.Article;
import me.wizos.loread.db.Category;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.Feed;
import me.wizos.loread.db.FeedCategory;
import me.wizos.loread.db.User;
import me.wizos.loread.network.HttpClientManager;
import me.wizos.loread.network.StringConverterFactory;
import me.wizos.loread.network.SyncWorker;
import me.wizos.loread.network.callback.CallbackX;
import me.wizos.loread.utils.StringUtils;
import me.wizos.loread.utils.TriggerRuleUtils;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static me.wizos.loread.utils.StringUtils.getString;

//import okhttp3.Call;

/**
 * 本接口对接 InoReader 服务，从他那获取数据
 * implements LoginInterface
 * @author Wizos on 2019/2/15.
 */

public class InoReaderApi extends OAuthApi implements ILogin {
    public static final String APP_ID = "1000001277";
    public static final String APP_KEY = "8dByWzO4AYi425yx5glICKntEY2g3uJo";
    public static final String OFFICIAL_BASE_URL = "https://www.innoreader.com/";
    public static final String REDIRECT_URI_SCHEMA = "loread://";
    public static final String REDIRECT_URI = "loread://oauth_inoreader";

    // public static final String CLIENTLOGIN = "/accounts/ClientLogin";
    // public static final String USER_INFO = "/reader/api/0/user-info";
    // public static final String ITEM_IDS = "/reader/api/0/stream/items/ids"; // 获取所有文章的id
    // public static final String ITEM_CONTENTS = "/reader/api/0/stream/items/contents"; // 获取流的内容
    // public static final String EDIT_TAG = "/reader/api/0/edit-tag";
    // public static final String RENAME_TAG = "/reader/api/0/rename-tag";
    // public static final String EDIT_FEED = "/reader/api/0/subscription/edit";
    // public static final String ADD_FEED = "/reader/api/0/subscription/quickadd";
    // public static final String SUSCRIPTION_LIST = "/reader/api/0/subscription/list"; // 这个不知道现在用在了什么地方
    // public static final String TAG_LIST = "/reader/api/0/tag/list";
    // public static final String STREAM_PREFS = "/reader/api/0/preference/stream/list";
    // public static final String UNREAD_COUNTS = "/reader/api/0/unread-count";
    // public static final String STREAM_CONTENTS = "/reader/api/0/stream/contents/";
    // public static final String Stream_Contents_Atom = "/reader/atom";
    // public static final String Stream_Contents_User = "/reader/api/0/stream/contents/user/";

    // 系统默认的分类
    // public static final String READING_LIST = "/state/com.google/reading-list";
    // public static final String NO_LABEL = "/state/com.google/no-label";
    // public static final String STARRED = "/state/com.google/starred";
    // public static final String UNREAND = "/state/com.google/unread";

    // Code Description
    // 200 	Request OK
    // 400 	Mandatory parameter(s) missing
    // 401 	End-user not authorized
    // 403 	You are not sending the correct AppID and/or AppSecret
    // 404 	Method not implemented
    // 429 	Daily limit reached for this zone
    // 503 	Service unavailable

    private InoReaderService service;

    // 首次登录/授权时，根据前端传递对的host实例化api
    public InoReaderApi(String baseUrl) {
        if (TextUtils.isEmpty(baseUrl)) {
            tempBaseUrl = InoReaderApi.OFFICIAL_BASE_URL;
        }else {
            tempBaseUrl = baseUrl;
        }
        if (!tempBaseUrl.endsWith("/")) {
            tempBaseUrl = tempBaseUrl + "/";
        }
        XLog.i(" tempBaseUrl 地址：" + tempBaseUrl + " - " + baseUrl );
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(tempBaseUrl) // 设置网络请求的Url地址, 必须以/结尾
                .addConverterFactory(StringConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())  // 设置数据解析器
                .client(HttpClientManager.i().inoreaderHttpClient())
                .build();
        service = retrofit.create(InoReaderService.class);
    }

    public static String tempBaseUrl;
    public String getTempBaseUrl() {
        return tempBaseUrl;
    }

    public void login(String account, String password, CallbackX cb){
        service.login(account, password).enqueue(new retrofit2.Callback<String>() {
            @Override
            public void onResponse(retrofit2.Call<String> call, Response<String> response) {
                if(response.isSuccessful()){
                    String result = response.body();
                    LoginResult loginResult = new LoginResult(result);
                    XLog.i("登录结果：" + result + " , " + loginResult);
                    if (!loginResult.success) {
                        cb.onFailure(loginResult.getError());
                    }else {
                        cb.onSuccess(loginResult.getAuth());
                    }
                }else {
                    cb.onFailure(response.message());
                }
            }

            @Override
            public void onFailure(retrofit2.Call<String> call, Throwable t) {
                cb.onFailure(t.getMessage());
            }
        });
    }


    @Override
    public void setAuthorization(String authorization) {
        super.setAuthorization(authorization);
    }

    public static String getOAuthUrl(String baseUrl) {
        if(StringUtils.isEmpty(baseUrl)){
            baseUrl = OFFICIAL_BASE_URL;
        }
        // String redirectUri;
        // redirectUri = "/" + redirectUri + Uri.parse(baseUrl).getHost();
        return baseUrl + "oauth2/auth?response_type=code&client_id=" + APP_ID + "&redirect_uri=" + REDIRECT_URI +  "&state=loread&lang=" + Locale.getDefault();
    }

    // public String getOAuthUrl2() {
    //     return OFFICIAL_BASE_URL + "oauth2/auth?response_type=code&client_id=" + APP_ID + "&redirect_uri=" + REDIRECT_URI + "&state=loread&lang=" + Locale.getDefault();
    // }
    public String getOAuthUrl() {
        String baseUrl;
        if(!StringUtils.isEmpty(tempBaseUrl)){
            baseUrl = tempBaseUrl;
        }else {
            baseUrl = OFFICIAL_BASE_URL;
        }
        return baseUrl + "oauth2/auth?response_type=code&client_id=" + APP_ID + "&redirect_uri=" + REDIRECT_URI + "&state=loread&lang=" + Locale.getDefault();
    }

    public void getAccessToken(String authorizationCode,CallbackX cb) {
        String redirectUri;
        if(!StringUtils.isEmpty(tempBaseUrl)){
            redirectUri = Uri.parse(tempBaseUrl).getHost();
        }else {
            redirectUri = Uri.parse(OFFICIAL_BASE_URL).getHost();
        }
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("grant_type", "authorization_code");
        builder.add("code", authorizationCode);
        builder.add("redirect_uri", REDIRECT_URI + "/" + redirectUri);
        builder.add("client_id", APP_ID);
        builder.add("client_secret", APP_KEY);

        service.getAccessToken("authorization_code", REDIRECT_URI, APP_ID,APP_KEY, authorizationCode).enqueue(new Callback<Token>() {
            @Override
            public void onResponse(@NotNull Call<Token> call, @NotNull Response<Token> response) {
                if(response.isSuccessful()){
                    cb.onSuccess(response.body());
                }else {
                    cb.onFailure("失败：" + response.message());
                }
            }

            @Override
            public void onFailure(@NotNull Call<Token> call, @NotNull Throwable t) {
                cb.onFailure("失败：" + t.getMessage());
            }
        });
    }

    public void refreshingAccessToken(String refreshToken, CallbackX cb) {
        service.refreshingAccessToken("refresh_token",refreshToken,APP_ID,APP_KEY).enqueue(new Callback<Token>() {
            @Override
            public void onResponse(@NotNull Call<Token> call, @NotNull Response<Token> response) {
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
            public void onFailure(Call<Token> call, Throwable t) {
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

    /**
     * 一般用在首次登录的时候，去获取用户基本资料
     *
     * @return
     * @throws IOException
     */
    public void fetchUserInfo( CallbackX cb){
        service.getUserInfo(getAuthorization()).enqueue(new Callback<UserInfo>() {
            @Override
            public void onResponse(@NonNull Call<UserInfo> call,@NonNull Response<UserInfo> response) {
                XLog.i("获取响应" + getAuthorization() + response.message() );
                XLog.i("获取响应" + response );
                if( response.isSuccessful()){
                    XLog.i("获取响应成功" );
                    cb.onSuccess(response.body().getUser());
                }else {
                    XLog.i("获取响应失败");
                    cb.onFailure("获取失败：" + response.message());
                }
            }
            @Override
            public void onFailure(@NonNull Call<UserInfo> call,@NonNull Throwable t) {
                XLog.i("响应失败");
                cb.onFailure("获取失败：" + t.getMessage() + t.toString());
                t.printStackTrace();
            }
        });
    }

    @Override
    public void sync() {
        try {
            long startSyncTimeMillis = System.currentTimeMillis();
            String uid = App.i().getUser().getId();

            XLog.i("3 - 同步订阅源信息");
            LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post( App.i().getString(R.string.sync_feed_info, "3.") );

            // 获取分类
            List<Category> categories = service.getCategoryItems(getAuthorization()).execute().body().getCategories();
            // 去除分类中的的一些无用分类
            if (categories != null && categories.size() > 0 && categories.get(0).getId().endsWith("/state/com.google/starred")) {
                categories.remove(0); // /state/com.google/starred
            }
            if (categories != null && categories.size() > 0 && categories.get(0).getId().endsWith("/state/com.google/broadcast")) {
                categories.remove(0); // /state/com.google/broadcast
            }
            if (categories != null && categories.size() > 0 && categories.get(0).getId().endsWith("/state/com.google/blogger-following")) {
                categories.remove(0); // /state/com.google/blogger-following
            }
            String[] array;
            String tagTitle;
            for (Category category : categories) {
                array = category.getId().split("/");
                tagTitle = array[array.length - 1];
                category.setTitle(tagTitle);
            }

            // 获取feed
            List<Subscription> subscriptions = service.getFeeds(getAuthorization()).execute().body().getSubscriptions();
            List<Feed> feeds = new ArrayList<>(subscriptions.size());
            Feed feed;
            List<FeedCategory> feedCategories = new ArrayList<>();
            FeedCategory feedCategory;
            for (Subscription subscription : subscriptions) {
                feed = subscription.convert2Feed();
                feed.setUid(uid);
                feeds.add(feed);
                for (SubCategories subCategories : subscription.getCategories()) {
                    feedCategory = new FeedCategory(uid, subscription.getId(), subCategories.getId());
                    feedCategories.add(feedCategory);
                }
            }

            // 如果在获取到数据的时候就保存，那么到这里同步断了的话，可能系统内的文章就找不到响应的分组，所有放到这里保存。
            // （比如在云端将文章移到的新的分组）
            coverSaveFeeds(feeds);
            coverSaveCategories(categories);
            coverFeedCategory(feedCategories);

            XLog.i("2 - 同步文章信息");
            LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post( App.i().getString(R.string.sync_article_refs, "2.") );
            // 获取未读资源
            ArraySet<String> unreadRefsList = fetchUnreadRefs();
            // 获取加星资源
            ArraySet<String> staredRefsList = fetchStaredRefs();

            XLog.i("1 - 同步文章内容");
            ArrayList<ArraySet<String>> refsList = splitRefs(unreadRefsList, staredRefsList);
            int allSize = refsList.get(0).size() + refsList.get(1).size() + refsList.get(2).size();

            // 抓取【未读、未加星】文章
            fetchArticle(allSize, 0, new ArrayList<>(refsList.get(0)), new ArticleChanger() {
                @Override
                public Article change(Article article) {
                    // article.setCrawlDate(0);
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
                    // article.setCrawlDate(0);
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
                    // article.setCrawlDate(0);
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
            // 执行文章自动处理脚本
            TriggerRuleUtils.exeAllRules(uid,startSyncTimeMillis);

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

        handleDuplicateArticles();
        handleCrawlDate();
        updateCollectionCount();
        LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post( null );
    }

    private void handleException(Exception e, String msg) {
        XLog.e("同步失败：" + e.getClass() + " = " + msg);
        e.printStackTrace();
        if("401".equalsIgnoreCase(msg)){
            ToastUtils.show(getString(R.string.plz_login_again));
        }else {
            ToastUtils.show(msg);
        }
    }

    private void fetchArticle(int allSize, int syncedSize, List<String> subIds, ArticleChanger articleChanger) throws IOException{
        int needFetchCount = subIds.size();
        int hadFetchCount = 0;

        while (needFetchCount > 0) {
            int fetchUnit = Math.min(needFetchCount, fetchContentCntForEach);
            List<Item> items = service.getItemContents(getAuthorization(), genRequestBody(subIds.subList(hadFetchCount, hadFetchCount = hadFetchCount + fetchUnit))).execute().body().getItems();
            List<Article> tempArticleList = new ArrayList<>(fetchUnit);
            for (Item item : items) {
                tempArticleList.add(item.convert(articleChanger));
            }
            CoreDB.i().articleDao().insert(tempArticleList);
            LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post( App.i().getString(R.string.sync_article_content, "1.", syncedSize = syncedSize + fetchUnit, allSize) );
            needFetchCount = subIds.size() - hadFetchCount;
        }
    }




    private ArraySet<String> fetchUnreadRefs() throws IOException {
        List<ItemRefs> itemRefs = new ArrayList<>();
        // String info;
        ItemIds tempItemIds = new ItemIds();
        int i = 0;
        do {
            tempItemIds = service.getStreamItemsIds(getAuthorization(),"user/-/state/com.google/reading-list", "user/-/state/com.google/read", 1000, false, tempItemIds.getContinuation()).execute().body();
            i++;
            itemRefs.addAll(tempItemIds.getItemRefs());
        } while (tempItemIds.getContinuation() != null && i < 5);
        Collections.reverse(itemRefs); // 倒序排列

        // List<Article> localUnreadArticles = CoreDB.i().articleDao().getUnreadNoOrder(App.i().getUser().getId());
        // Map<String, Article> localUnreadArticlesMap = new ArrayMap<>(localUnreadArticles.size());
        // List<Article> changedArticles = new ArrayList<>();
        // // 筛选下来，最终要去云端获取内容的未读Refs的集合
        // HashSet<String> tempUnreadIds = new HashSet<>(itemRefs.size());
        // // 数据量大的一方
        // String articleId;
        // for (Article article : localUnreadArticles) {
        //     articleId = article.getId();
        //     localUnreadArticlesMap.put(articleId, article);
        // }
        // // 数据量小的一方
        // Article article;
        // for (ItemRefs item : itemRefs) {
        //     articleId = item.getLongId();
        //     article = localUnreadArticlesMap.get(articleId);
        //     if (article != null) {
        //         localUnreadArticlesMap.remove(articleId);
        //     } else {
        //         article = CoreDB.i().articleDao().getById(App.i().getUser().getId(), articleId);
        //         if (article != null && article.getReadStatus() == App.STATUS_READED) {
        //             article.setReadStatus(App.STATUS_UNREAD);
        //             changedArticles.add(article);
        //         } else {
        //             // 本地无，而云端有，加入要请求的未读资源
        //             tempUnreadIds.add(articleId);
        //         }
        //     }
        // }
        // for (Map.Entry<String, Article> entry : localUnreadArticlesMap.entrySet()) {
        //     if (entry.getKey() != null) {
        //         article = localUnreadArticlesMap.get(entry.getKey());
        //         // 本地未读设为已读
        //         article.setReadStatus(App.STATUS_READED);
        //         changedArticles.add(article);
        //     }
        // }
        //
        // CoreDB.i().articleDao().update(changedArticles);
        // return tempUnreadIds;

        ArrayList<String> ids = new ArrayList<>(itemRefs.size());
        for (ItemRefs item : itemRefs) {
            ids.add(item.getLongId());
        }
        return handleUnreadRefs(ids);
    }

    private ArraySet<String> fetchStaredRefs() throws HttpException, IOException {
        List<ItemRefs> itemRefs = new ArrayList<>();
        String info;
        ItemIds tempItemIds = new ItemIds();
        int i = 0;
        do {
            tempItemIds = service.getStreamItemsIds(getAuthorization(),"user/-/state/com.google/starred", null, 1000, false, tempItemIds.getContinuation()).execute().body();
            i++;
            itemRefs.addAll(tempItemIds.getItemRefs());
        } while (tempItemIds.getContinuation() != null && i < 5);
        Collections.reverse(itemRefs); // 倒序排列

        // List<Article> localStarredArticles = CoreDB.i().articleDao().getStaredNoOrder(App.i().getUser().getId());
        // Map<String, Article> localStarredArticlesMap = new ArrayMap<>(localStarredArticles.size());
        // List<Article> changedArticles = new ArrayList<>();
        // HashSet<String> tempStarredIds = new HashSet<>(itemRefs.size());
        //
        // String articleId;
        // // 第1步，遍历数据量大的一方A，将其比对项目放入Map中
        // for (Article article : localStarredArticles) {
        //     articleId = article.getId();
        //     localStarredArticlesMap.put(articleId, article);
        // }
        //
        // // 第2步，遍历数据量小的一方B。到Map中找，是否含有b中的比对项。有则XX，无则YY
        // Article article;
        // for (ItemRefs item : itemRefs) {
        //     articleId = item.getLongId();
        //     article = localStarredArticlesMap.get(articleId);
        //     if (article != null) {
        //         localStarredArticlesMap.remove(articleId);
        //     } else {
        //         article = CoreDB.i().articleDao().getById(App.i().getUser().getId(), articleId);
        //         if (article != null) {
        //             article.setStarStatus(App.STATUS_STARED);
        //             changedArticles.add(article);
        //         } else {
        //             // 本地无，而云远端有，加入要请求的未读资源
        //             tempStarredIds.add(articleId);
        //         }
        //     }
        // }
        //
        // for (Map.Entry<String, Article> entry : localStarredArticlesMap.entrySet()) {
        //     if (entry.getKey() != null) {
        //         article = localStarredArticlesMap.get(entry.getKey());
        //         article.setStarStatus(App.STATUS_UNSTAR);
        //         changedArticles.add(article);// 取消加星
        //     }
        // }
        //
        // CoreDB.i().articleDao().update(changedArticles);
        // return tempStarredIds;

        ArrayList<String> ids = new ArrayList<>(itemRefs.size());
        for (ItemRefs item : itemRefs) {
            ids.add(item.getLongId());
        }
        return handleStaredRefs(ids);
    }

    private RequestBody genRequestBody(List<String> ids) {
        FormBody.Builder builder = new FormBody.Builder();
        for (String id : ids) {
            builder.add("i", id);
        }
        return builder.build();
    }

    public void renameTag(String sourceTagId, String targetName, CallbackX cb) {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("s", sourceTagId);
        builder.add("dest", targetName);
        //WithHttp.i().asyncPost(HOST + "/reader/api/0/rename-tag", builder, authHeaders, cb);
        service.renameTag(getAuthorization(), builder.build()).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if( response.isSuccessful()){
                    String msg = response.body();
                    if("OK".equals(msg)){
                        cb.onSuccess("修改成功");
                    }else if( msg.contains("Tag not found!")){
                        cb.onFailure("修改失败：要修改分类不存在");
                    }
                }else {
                    cb.onFailure("修改失败：未知原因");
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                cb.onFailure("修改失败：" + t.getMessage());
            }
        });
    }

    public void unsubscribeFeed(String feedId, CallbackX cb) {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("ac", "unsubscribe");
        builder.add("s", feedId);
        service.editFeed(getAuthorization(), builder.build()).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() ){
                    String msg = response.body();
                    if( !TextUtils.isEmpty(msg) && msg.contains("OK")){
                        cb.onSuccess(null);
                    }else {
                        cb.onFailure(msg);
                    }
                }else {
                    cb.onFailure("修改失败：原因未知");
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                cb.onFailure(t.getMessage());
            }
        });
    }

    public void addFeed(@NonNull EditFeed editFeed, CallbackX cb) {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("ac", "subscribe");
        builder.add("s", editFeed.getId());
        List<CategoryItem> categoryItemList = editFeed.getCategoryItems();
        for (CategoryItem categoryItem : categoryItemList) {
            builder.add("a", categoryItem.getId());
        }
        service.editFeed(getAuthorization(), builder.build()).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    XLog.i("添加成功" + response.body().toString());
                    cb.onSuccess("添加成功");
                } else {
                    cb.onFailure("响应失败");
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                cb.onFailure(t.getMessage());
            }
        });
    }

    public void renameFeed(String feedId, String renamedTitle, CallbackX cb) {
        FormBody.Builder builder = new FormBody.Builder();
//        builder.add("ac", "edit"); // 可省略
        builder.add("s", feedId);
        builder.add("t", renamedTitle);
        service.editFeed(getAuthorization(), builder.build()).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful() ){
                    String msg = response.body();
                    if( !TextUtils.isEmpty(msg) && msg.contains("OK")){
                        cb.onSuccess(null);
                    }else {
                        cb.onFailure(msg);
                    }
                }else {
                    cb.onFailure("修改失败：原因未知");
                }
            }
            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                cb.onFailure(t.getMessage());
            }
        });
    }

    @Override
    public void editFeedCategories(List<CategoryItem> lastCategoryItems, EditFeed editFeed, CallbackX cb) {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("ac", "edit");
        builder.add("s", editFeed.getId());

        ArrayList<CategoryItem> selectedCategoryItems = editFeed.getCategoryItems();
        ArrayMap<String, CategoryItem> lastCategoryItemsMap = new ArrayMap<>(lastCategoryItems.size());
        for (CategoryItem categoryItem : lastCategoryItems) {
            lastCategoryItemsMap.put(categoryItem.getId(), categoryItem);
        }
        for (CategoryItem categoryItem : selectedCategoryItems) {
            if (lastCategoryItemsMap.get(categoryItem.getId()) == null) {
                builder.add("a", categoryItem.getId());
                lastCategoryItemsMap.remove(categoryItem);
            }
        }
        for (Map.Entry<String, CategoryItem> entry : lastCategoryItemsMap.entrySet()) {
            builder.add("r", entry.getKey());
        }
        //WithHttp.i().asyncPost(HOST + "/reader/api/0/subscription/edit", builder, authHeaders, cb);
        service.editFeed(getAuthorization(), builder.build()).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() ){
                    String msg = response.body();
                    if( !TextUtils.isEmpty(msg) && msg.equalsIgnoreCase("ok")){
                        cb.onSuccess(null);
                    }else {
                        cb.onFailure(msg);
                    }
                }else {
                    cb.onFailure("修改失败：原因未知");
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                cb.onFailure(t.getMessage());
            }
        });
    }


    public void markArticleListReaded(Collection<String> articleIDs, CallbackX cb) {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("a", "user/-/state/com.google/read");
        for (String articleID : articleIDs) {
            builder.add("i", articleID);
        }
        service.markArticle(getAuthorization(), builder.build()).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                if (response.isSuccessful() ){
                    String msg = response.body();
                    if( !TextUtils.isEmpty(msg) && msg.equalsIgnoreCase("ok")){
                        cb.onSuccess(null);
                    }else {
                        cb.onFailure(msg);
                    }
                }else {
                    cb.onFailure(getString(R.string.response_fail));
                }
            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                cb.onFailure(t.getMessage());
            }
        });
    }

    @Override
    public void markArticleReaded(String articleID, CallbackX cb) {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("a", "user/-/state/com.google/read");
        builder.add("i", articleID);
        service.markArticle(getAuthorization(), builder.build()).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() ){
                    String msg = response.body();
                    if( !TextUtils.isEmpty(msg) && msg.equalsIgnoreCase("ok")){
                        cb.onSuccess(null);
                    }else {
                        cb.onFailure(msg);
                    }
                }else {
                    cb.onFailure("修改失败：原因未知");
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                cb.onFailure(t.getMessage());
            }
        });
    }

    public void markArticleUnread(String articleID,CallbackX cb) {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("r", "user/-/state/com.google/read");
        builder.add("i", articleID);
        service.markArticle(getAuthorization(), builder.build()).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() ){
                    String msg = response.body();
                    if( !TextUtils.isEmpty(msg) && msg.equalsIgnoreCase("ok")){
                        cb.onSuccess(null);
                    }else {
                        cb.onFailure(msg);
                    }
                }else {
                    cb.onFailure("修改失败：原因未知");
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                cb.onFailure(t.getMessage());
            }
        });
    }


    public void markArticleStared(String articleID,CallbackX cb) {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("a", "user/-/state/com.google/starred");
        builder.add("i", articleID);
        service.markArticle(getAuthorization(), builder.build()).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() ){
                    String msg = response.body();
                    if( !TextUtils.isEmpty(msg) && msg.equalsIgnoreCase("ok")){
                        cb.onSuccess(null);
                    }else {
                        cb.onFailure(msg);
                    }
                }else {
                    cb.onFailure("修改失败：原因未知");
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                cb.onFailure(t.getMessage());
            }
        });
    }


    public void markArticleUnstar(String articleID,CallbackX cb) {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("r", "user/-/state/com.google/starred");
        builder.add("i", articleID);
        service.markArticle(getAuthorization(), builder.build()).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() ){
                    String msg = response.body();
                    if( !TextUtils.isEmpty(msg) && msg.equalsIgnoreCase("ok")){
                        cb.onSuccess(null);
                    }else {
                        cb.onFailure(msg);
                    }
                }else {
                    cb.onFailure("修改失败：原因未知");
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                cb.onFailure(t.getMessage());
            }
        });
    }

    /**
     * 将 未读资源 和 加星资源，去重分为3组
     *
     * @param tempUnreadIds
     * @param tempStarredIds
     * @return
     */
    public ArrayList<ArraySet<String>> splitRefs(ArraySet<String> tempUnreadIds, ArraySet<String> tempStarredIds) {
        // XLog.i("【reRefs1】云端未读" + tempUnreadIds.size() + "，云端加星" + tempStarredIds.size());
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
        // XLog.i("【reRefs2】" + reUnreadUnstarRefs.size() + "--" + reReadStarredRefs.size() + "--" + reUnreadStarredRefs.size());
        return refsList;
    }
}
