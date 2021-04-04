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
import me.wizos.loread.BuildConfig;
import me.wizos.loread.Contract;
import me.wizos.loread.R;
import me.wizos.loread.bean.FeedEntries;
import me.wizos.loread.bean.Token;
import me.wizos.loread.bean.feedly.CategoryItem;
import me.wizos.loread.bean.feedly.input.EditFeed;
import me.wizos.loread.bean.inoreader.GsTag;
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
import me.wizos.loread.utils.Converter;
import me.wizos.loread.utils.PagingUtils;
import me.wizos.loread.utils.StringUtils;
import me.wizos.loread.utils.Tool;
import me.wizos.loread.utils.TriggerRuleUtils;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static me.wizos.loread.utils.StringUtils.getString;


/**
 * 本接口对接 InoReader 服务，从他那获取数据
 * implements LoginInterface
 * @author Wizos on 2019/2/15.
 */

public class InoReaderApi extends OAuthApi implements ILogin {
    // public static final String APP_ID = "1000001277";
    // public static final String APP_KEY = "8dByWzO4AYi425yx5glICKntEY2g3uJo";
    public static final String OFFICIAL_BASE_URL = "https://www.innoreader.com/";

    private static final String REDIRECT_URI = "loread://oauth_inoreader";
    private final static String STREAM_ID_READING_LIST = "user/-/state/com.google/reading-list";
    private final static String STREAM_ID_READ_LIST = "user/-/state/com.google/read";
    private final static String STREAM_ID_STAR_LIST = "user/-/state/com.google/starred";

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
        service.login(account, password).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                if(!response.isSuccessful()){
                    cb.onFailure(App.i().getString(R.string.response_code, response.code()));
                    return;
                }
                String result = response.body();
                LoginResult loginResult = new LoginResult(result);
                XLog.i("登录结果：" + result + " , " + loginResult);
                if (!loginResult.success) {
                    cb.onFailure(loginResult.getError());
                }else {
                    cb.onSuccess(loginResult.getAuth());
                }
            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                cb.onFailure(t.getLocalizedMessage());
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
        return baseUrl + "oauth2/auth?response_type=code&client_id=" + BuildConfig.INOREADER_APP_ID + "&redirect_uri=" + REDIRECT_URI +  "&state=loread&lang=" + Locale.getDefault();
    }

    @Override
    public String getOAuthUrl() {
        String baseUrl;
        if(!StringUtils.isEmpty(tempBaseUrl)){
            baseUrl = tempBaseUrl;
        }else {
            baseUrl = OFFICIAL_BASE_URL;
        }
        return baseUrl + "oauth2/auth?response_type=code&client_id=" + BuildConfig.INOREADER_APP_ID + "&redirect_uri=" + REDIRECT_URI + "&state=loread&lang=" + Locale.getDefault();
    }

    @Override
    public void getAccessToken(String authorizationCode, CallbackX cb) {
        service.getAccessToken("authorization_code", REDIRECT_URI, BuildConfig.INOREADER_APP_ID, BuildConfig.INOREADER_APP_KEY, authorizationCode).enqueue(new Callback<Token>() {
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
        service.refreshingAccessToken("refresh_token",refreshToken, BuildConfig.INOREADER_APP_ID, BuildConfig.INOREADER_APP_KEY).enqueue(new Callback<Token>() {
            @Override
            public void onResponse(@NotNull Call<Token> call, @NotNull Response<Token> response) {
                if(!response.isSuccessful()){
                    cb.onFailure(App.i().getString(R.string.response_code, response.code()));
                    return;
                }
                Token token = response.body();
                if(token==null){
                    cb.onFailure(App.i().getString(R.string.return_data_exception));
                    return;
                }
                // if (TextUtils.isEmpty(token.getRefreshToken())) {
                //     token.setRefreshToken(refreshToken);
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

    @Override
    public String refreshingAccessToken(String refreshToken) throws IOException {
        Token token = service.refreshingAccessToken("refresh_token", refreshToken, BuildConfig.INOREADER_APP_ID, BuildConfig.INOREADER_APP_KEY).execute().body();
        if(token == null){
            return "";
        }
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

    /**
     * 一般用在首次登录的时候，去获取用户基本资料
     */
    @Override
    public void fetchUserInfo(CallbackX cb){
        service.getUserInfo(getAuthorization()).enqueue(new Callback<UserInfo>() {
            @Override
            public void onResponse(@NonNull Call<UserInfo> call,@NonNull Response<UserInfo> response) {
                XLog.i("获取响应" + getAuthorization() + response.message() );
                if( response.isSuccessful()){
                    XLog.i("获取响应成功" );
                    cb.onSuccess(response.body().convert());
                }else {
                    XLog.i("获取响应失败");
                    cb.onFailure(App.i().getString(R.string.response_code, response.code()));
                }
            }
            @Override
            public void onFailure(@NonNull Call<UserInfo> call,@NonNull Throwable t) {
                XLog.i("响应失败");
                cb.onFailure(t.getLocalizedMessage());
            }
        });
    }

    @Override
    public void sync() {
        long startSyncTimeMillis = App.i().getLastShowTimeMillis();
        String uid = App.i().getUser().getId();
        try {
            XLog.i("3 - 同步订阅源信息");
            LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post( App.i().getString(R.string.step_sync_feed_info, "3.") );

            // 获取分类
            // List<Category> categories = service.getCategoryItems(getAuthorization()).execute().body().getCategories();
            List<GsTag> tags = service.getCategoryItems(getAuthorization()).execute().body().getCategories();
            // 去除分类中的的一些无用分类
            if (tags.size() > 0 && tags.get(0).getId().endsWith("/state/com.google/starred")) {
                tags.remove(0); // /state/com.google/starred
            }
            if (tags.size() > 0 && tags.get(0).getId().endsWith("/state/com.google/broadcast")) {
                tags.remove(0); // /state/com.google/broadcast
            }
            if (tags.size() > 0 && tags.get(0).getId().endsWith("/state/com.google/blogger-following")) {
                tags.remove(0); // /state/com.google/blogger-following
            }

            List<Category> categories = new ArrayList<>();
            for (GsTag tag:tags) {
                categories.add(Converter.from(uid, tag));
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
            LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post( App.i().getString(R.string.step_sync_article_refs, "2.") );
            // 获取未读资源
            ArraySet<String> unreadRefsList = fetchUnreadRefs();
            // 获取加星资源
            ArraySet<String> staredRefsList = fetchStaredRefs();

            XLog.i("1 - 同步文章内容");
            ArrayList<ArraySet<String>> refsList = splitRefs(unreadRefsList, staredRefsList);
            int allSize = refsList.get(0).size() + refsList.get(1).size() + refsList.get(2).size();

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

        handleDuplicateArticles(startSyncTimeMillis);
        updateCollectionCount();
        handleArticleInfo();
        LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post( null );
    }

    private void handleException(Exception e, String msg) {
        XLog.w("同步失败：" + e.getClass() + " = " + e.getMessage());
        Tool.printCallStack(e);
        e.printStackTrace();
        if("401".equalsIgnoreCase(msg)){
            ToastUtils.show(getString(R.string.plz_login_again));
        }else {
            ToastUtils.show(msg);
        }
    }

    private void fetchArticle(int allSize,final int syncedSize, List<String> subIds, Converter.ArticleConvertListener articleConverter) throws IOException{
        // int needFetchCount = subIds.size();
        // int hadFetchCount = 0;
        //
        // while (needFetchCount > 0) {
        //     int fetchUnit = Math.min(needFetchCount, fetchContentCntForEach);
        //     List<Item> items = service.getItemContents(getAuthorization(), genRequestBody(subIds.subList(hadFetchCount, hadFetchCount = hadFetchCount + fetchUnit))).execute().body().getItems();
        //     List<Article> tempArticleList = new ArrayList<>(fetchUnit);
        //     for (Item item : items) {
        //         tempArticleList.add(Converter.from(item, articleConverter));
        //     }
        //     CoreDB.i().articleDao().insert(tempArticleList);
        //     LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post( App.i().getString(R.string.step_sync_article_content, "1.", syncedSize = syncedSize + fetchUnit, allSize) );
        //     needFetchCount = subIds.size() - hadFetchCount;
        // }

        PagingUtils.slice(subIds, 20, new PagingUtils.PagingListener<String>() {
            int sync = syncedSize;
            @Override
            public void onPage(@NotNull List<String> childList) {
                try{
                    List<Item> items = service.getItemContents(getAuthorization(), genRequestBody(childList)).execute().body().getItems();
                    List<Article> tempArticleList = new ArrayList<>(childList.size());
                    for (Item item : items) {
                        tempArticleList.add(Converter.from(item, articleConverter));
                    }
                    CoreDB.i().articleDao().insert(tempArticleList);
                    LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post( App.i().getString(R.string.step_sync_article_content, "1.", sync = sync + childList.size(), allSize) );
                }catch (IOException e){
                    XLog.e("获取文章失败：" + e.getLocalizedMessage());
                    e.printStackTrace();
                }
            }
        });
    }


    private ArraySet<String> fetchUnreadRefs() throws IOException {
        List<ItemRefs> itemRefs = new ArrayList<>();
        ItemIds tempItemIds = new ItemIds();
        // 由于 inoreader 限制，免费版最多只能获取到前 1000 个，即使 Continuation 还存在
        tempItemIds = service.getStreamItemsIds(getAuthorization(),STREAM_ID_READING_LIST, STREAM_ID_READ_LIST, 1000, false, tempItemIds.getContinuation()).execute().body();
        if(tempItemIds!=null){
            itemRefs.addAll(tempItemIds.getItemRefs());
        }

        // int i = 0;
        // do {
        //     tempItemIds = service.getStreamItemsIds(getAuthorization(),STREAM_ID_READING_LIST, STREAM_ID_READ_LIST, 1000, false, tempItemIds.getContinuation()).execute().body();
        //     i++;
        //     itemRefs.addAll(tempItemIds.getItemRefs());
        // } while (tempItemIds.getContinuation() != null && i < 3);
        Collections.reverse(itemRefs); // 倒序排列

        ArrayList<String> ids = new ArrayList<>(itemRefs.size());
        for (ItemRefs item : itemRefs) {
            ids.add(item.getLongId());
        }
        return handleUnreadRefs(ids);
    }

    private ArraySet<String> fetchStaredRefs() throws HttpException, IOException {
        List<ItemRefs> itemRefs = new ArrayList<>();
        ItemIds tempItemIds = new ItemIds();

        // 由于 inoreader 限制，免费版最多只能获取到前 1000 个，即使 Continuation 还存在
        tempItemIds = service.getStreamItemsIds(getAuthorization(),STREAM_ID_STAR_LIST, null, 1000, false, tempItemIds.getContinuation()).execute().body();
        if(tempItemIds!=null){
            itemRefs.addAll(tempItemIds.getItemRefs());
        }
        // int i = 0;
        // do {
        //     tempItemIds = service.getStreamItemsIds(getAuthorization(),STREAM_ID_STAR_LIST, null, 1000, false, tempItemIds.getContinuation()).execute().body();
        //     i++;
        //     itemRefs.addAll(tempItemIds.getItemRefs());
        // } while (tempItemIds.getContinuation() != null && i < 5);
        Collections.reverse(itemRefs); // 倒序排列

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

    @Override
    public void deleteCategory(String categoryId, CallbackX cb) {
        cb.onFailure(App.i().getString(R.string.server_api_not_supported, Contract.PROVIDER_INOREADER));
    }

    public void renameCategory(String categoryId, String targetName, CallbackX cb) {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("s", categoryId);
        builder.add("dest", targetName);
        service.renameTag(getAuthorization(), builder.build()).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                if( response.isSuccessful()){
                    String msg = response.body();
                    if("OK".equals(msg)){
                        final String newCategoryId = "user/" + App.i().getUser().getUserId() + "/" + targetName;
                        CoreDB.i().categoryDao().updateId(App.i().getUser().getId(), categoryId, newCategoryId);
                        CoreDB.i().feedCategoryDao().updateCategoryId(App.i().getUser().getId(), categoryId, newCategoryId);
                        cb.onSuccess(null);
                    }else if( !StringUtils.isEmpty(msg) && msg.contains("Tag not found!")){
                        cb.onFailure(App.i().getString(R.string.tag_not_found));
                    }else{
                        cb.onFailure(App.i().getString(R.string.unknown_cause));
                    }
                }else {
                    cb.onFailure(App.i().getString(R.string.response_code, response.code()));
                }
            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                cb.onFailure(t.getLocalizedMessage());
            }
        });
    }

    public void unsubscribeFeed(String feedId, CallbackX cb) {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("ac", "unsubscribe");
        builder.add("s", feedId);
        service.editFeed(getAuthorization(), builder.build()).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                if (response.isSuccessful() ){
                    String msg = response.body();
                    if( !TextUtils.isEmpty(msg) && msg.contains("OK")){
                        cb.onSuccess(null);
                    }else {
                        cb.onFailure(App.i().getString(R.string.return_data_exception));
                        XLog.w("返回数据异常：" + msg);
                    }
                }else {
                    cb.onFailure(App.i().getString(R.string.response_code, response.code()));
                }
            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                cb.onFailure(t.getLocalizedMessage());
            }
        });
    }

    @Override
    public void addFeed(@NonNull FeedEntries feedEntries, CallbackX cb) {
        EditFeed editFeed = Converter.from(feedEntries);
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("ac", "subscribe");
        builder.add("s", editFeed.getId());
        List<CategoryItem> categoryItemList = editFeed.getCategoryItems();
        for (CategoryItem categoryItem : categoryItemList) {
            builder.add("a", categoryItem.getId());
        }
        service.editFeed(getAuthorization(), builder.build()).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                if (response.isSuccessful()) {
                    XLog.i("添加成功" + response.body());
                    cb.onSuccess(App.i().getString(R.string.subscribe_success_plz_sync));
                } else {
                    cb.onFailure(App.i().getString(R.string.response_code, response.code()));
                }
            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                cb.onFailure(t.getLocalizedMessage());
            }
        });
    }

    @Override
    public void renameFeed(String feedId, String targetName, CallbackX cb) {
        FormBody.Builder builder = new FormBody.Builder();
        // builder.add("ac", "edit"); // 可省略
        builder.add("s", feedId);
        builder.add("t", targetName);
        service.editFeed(getAuthorization(), builder.build()).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful() ){
                    String msg = response.body();
                    if( !TextUtils.isEmpty(msg) && msg.contains("OK")){
                        cb.onSuccess(null);
                    }else {
                        cb.onFailure(App.i().getString(R.string.return_data_exception));
                        XLog.w("返回数据异常：" + msg);
                    }
                }else {
                    cb.onFailure(App.i().getString(R.string.response_code, response.code()));
                }
            }
            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                cb.onFailure(t.getLocalizedMessage());
            }
        });
    }

    @Override
    public void importOPML(Uri uri, CallbackX cb) {
        cb.onFailure(App.i().getString(R.string.server_api_not_supported, Contract.PROVIDER_INOREADER));
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
        service.editFeed(getAuthorization(), builder.build()).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                if (response.isSuccessful() ){
                    String msg = response.body();
                    if( !TextUtils.isEmpty(msg) && msg.equalsIgnoreCase("ok")){
                        cb.onSuccess(null);
                    }else {
                        cb.onFailure(App.i().getString(R.string.return_data_exception));
                        XLog.w("返回数据异常：" + msg);
                    }
                }else {
                    cb.onFailure(App.i().getString(R.string.response_code, response.code()));
                }
            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                cb.onFailure(t.getLocalizedMessage());
            }
        });
    }


    public void markArticleListReaded(Collection<String> articleIDs, CallbackX cb) {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("a", STREAM_ID_READ_LIST);
        for (String articleID : articleIDs) {
            builder.add("i", articleID);
        }
        markArticle(builder.build(), cb);
    }

    @Override
    public void markArticleReaded(String articleID, CallbackX cb) {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("a", STREAM_ID_READ_LIST);
        builder.add("i", articleID);
        markArticle(builder.build(), cb);
    }

    public void markArticleUnread(String articleID,CallbackX cb) {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("r", STREAM_ID_READ_LIST);
        builder.add("i", articleID);
        markArticle(builder.build(), cb);
    }


    public void markArticleStared(String articleID,CallbackX cb) {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("a", STREAM_ID_STAR_LIST);
        builder.add("i", articleID);
        markArticle(builder.build(), cb);
    }

    public void markArticleUnstar(String articleID,CallbackX cb) {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("r", STREAM_ID_STAR_LIST);
        builder.add("i", articleID);
        markArticle(builder.build(), cb);
    }

    public void markArticle(FormBody formBody, CallbackX cb) {
        service.markArticle(getAuthorization(), formBody).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() ){
                    String msg = response.body();
                    if( !TextUtils.isEmpty(msg) && msg.equalsIgnoreCase("ok")){
                        cb.onSuccess(null);
                    }else {
                        cb.onFailure(App.i().getString(R.string.return_data_exception));
                        XLog.w("返回数据异常：" + msg);
                    }
                }else {
                    cb.onFailure(App.i().getString(R.string.response_code, response.code()));
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
     */
    private ArrayList<ArraySet<String>> splitRefs(ArraySet<String> tempUnreadIds, ArraySet<String> tempStarredIds) {
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
