package me.wizos.loread.network.api;

import android.net.Uri;
import android.text.TextUtils;

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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import me.wizos.loread.App;
import me.wizos.loread.Contract;
import me.wizos.loread.R;
import me.wizos.loread.activity.login.LoginResult;
import me.wizos.loread.bean.FeedEntries;
import me.wizos.loread.bean.feedly.CategoryItem;
import me.wizos.loread.bean.feedly.input.EditFeed;
import me.wizos.loread.bean.fever.Feeds;
import me.wizos.loread.bean.fever.FeverResponse;
import me.wizos.loread.bean.fever.Group;
import me.wizos.loread.bean.fever.GroupFeeds;
import me.wizos.loread.bean.fever.Groups;
import me.wizos.loread.bean.fever.Item;
import me.wizos.loread.bean.fever.Items;
import me.wizos.loread.bean.fever.SavedItemIds;
import me.wizos.loread.bean.fever.UnreadItemIds;
import me.wizos.loread.bean.ttrss.request.Login;
import me.wizos.loread.bean.ttrss.request.SubscribeFeed;
import me.wizos.loread.bean.ttrss.request.UnsubscribeFeed;
import me.wizos.loread.bean.ttrss.request.UpdateArticle;
import me.wizos.loread.bean.ttrss.result.SubscribeFeedResult;
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
import me.wizos.loread.utils.Converter;
import me.wizos.loread.utils.EncryptUtils;
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
public class FeverTinyRSSApi extends AuthApi implements ILogin {
    private FeverService feverService;
    private TinyRSSService tinyRSSService;
    int fetchContentCntForEach = 50; // 每次获取内容的数量

    public FeverTinyRSSApi(@NotNull String baseUrl) {
        String tempBaseUrl;
        if (!TextUtils.isEmpty(baseUrl)) {
            tempBaseUrl = baseUrl;
        } else {
            tempBaseUrl = "https://example.com";
            ToastUtils.show(R.string.empty_site_url_hint);
        }

        if (!tempBaseUrl.endsWith("/")) {
            tempBaseUrl = tempBaseUrl + "/";
        }

        Retrofit feverRetrofit = new Retrofit.Builder()
                .baseUrl(tempBaseUrl) // 设置网络请求的Url地址, 必须以/结尾
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().setLenient().create()))  // 设置数据解析器
                .client(HttpClientManager.i().feverHttpClient())
                .build();
        feverService = feverRetrofit.create(FeverService.class);

        Pattern pattern = Pattern.compile("/plugins.*?$");
        tempBaseUrl = pattern.matcher(tempBaseUrl).replaceFirst("/");
        XLog.v("修改后的baseUrl：" + tempBaseUrl);
        Retrofit tinyRSSRetrofit = new Retrofit.Builder()
                .baseUrl(tempBaseUrl) // 设置网络请求的Url地址, 必须以/结尾
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().setLenient().create()))  // 设置数据解析器
                .client(HttpClientManager.i().feverTinyRSSHttpClient())
                .build();
        tinyRSSService = tinyRSSRetrofit.create(TinyRSSService.class);
    }


    public LoginResult login(String accountId, String accountPd) throws IOException {
        Login loginParam = new Login();
        loginParam.setUser(accountId);
        loginParam.setPassword(accountPd);
        TinyResponse<me.wizos.loread.bean.ttrss.result.LoginResult> tinyLoginResultResponse = tinyRSSService.login(loginParam).execute().body();

        LoginResult loginResult = new LoginResult();
        if(tinyLoginResultResponse != null && tinyLoginResultResponse.isSuccessful()){
            String auth = EncryptUtils.MD5(accountId + ":" + accountPd);
            FeverResponse feverLoginResultResponse = feverService.login(auth).execute().body();
            if (feverLoginResultResponse != null && feverLoginResultResponse.isSuccessful()) {
                return loginResult.setSuccess(true).setData(tinyLoginResultResponse.getContent().getSessionId());
            } else {
                return loginResult.setSuccess(false).setData(getString(R.string.login_failure_please_check_account));
            }
        }else {
            return loginResult.setSuccess(false).setData(getString(R.string.login_failure_please_check_account));
        }
    }

    public void login(String account, String password, CallbackX cb){
        Login loginParam = new Login();
        loginParam.setUser(account);
        loginParam.setPassword(password);
        tinyRSSService.login(loginParam).enqueue(new retrofit2.Callback<TinyResponse<me.wizos.loread.bean.ttrss.result.LoginResult>>() {
            @Override
            public void onResponse(retrofit2.Call<TinyResponse<me.wizos.loread.bean.ttrss.result.LoginResult>> call, Response<TinyResponse<me.wizos.loread.bean.ttrss.result.LoginResult>> response) {
                if(response.isSuccessful()){
                    TinyResponse<me.wizos.loread.bean.ttrss.result.LoginResult> loginResultResponse = response.body();
                    if(loginResultResponse != null &&  loginResultResponse.isSuccessful()){
                        cb.onSuccess(loginResultResponse.getContent().getSessionId());
                        return;
                    }
                    cb.onFailure(App.i().getString(R.string.login_failed_reason, loginResultResponse.toString()));
                }else {
                    cb.onFailure(App.i().getString(R.string.login_failed_reason, response.message()));
                }
            }

            @Override
            public void onFailure(retrofit2.Call<TinyResponse<me.wizos.loread.bean.ttrss.result.LoginResult>> call, Throwable t) {
                cb.onFailure(App.i().getString(R.string.login_failed_reason, t.getMessage()));
            }
        });
    }

    public void fetchUserInfo(CallbackX cb) {
        cb.onFailure(App.i().getString(R.string.server_api_not_supported, Contract.PROVIDER_FEVER));
    }

    @Override
    public void sync() {
        long startSyncTimeMillis = System.currentTimeMillis() + 3600_000;
        String uid = App.i().getUser().getId();
        try {

            String auth = EncryptUtils.MD5(App.i().getUser().getUserId() + ":" + App.i().getUser().getUserPassword());
            XLog.i("同步 - 获取分类");
            LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post(getString(R.string.step_sync_feed_info,"3."));

            Groups groupsResponse = feverService.getGroups(auth).execute().body();
            if (groupsResponse == null || !groupsResponse.isSuccessful()) {
                throw new HttpException("获取失败");
            }

            Iterator<Group> groupsIterator = groupsResponse.getGroups().iterator();
            Group group;
            Category category;
            FeedCategory feedCategoryTmp;
            String[] feedIds;
            ArrayList<Category> categories = new ArrayList<>();
            ArrayList<FeedCategory> feedCategories = new ArrayList<>();
            while (groupsIterator.hasNext()) {
                group = groupsIterator.next();
                if (group.getId() < 1) {
                    continue;
                }
                category = group.convert();
                categories.add(category);
            }

            Iterator<GroupFeeds> groupFeedsIterator = groupsResponse.getFeedsGroups().iterator();
            GroupFeeds groupFeeds;
            while (groupFeedsIterator.hasNext()) {
                groupFeeds = groupFeedsIterator.next();
                feedIds = groupFeeds.getFeedIds().split(",");
                if (feedIds.length == 0) {
                    continue;
                }
                for (String feedId : feedIds) {
                    feedCategoryTmp = new FeedCategory(uid, feedId, String.valueOf(groupFeeds.getGroupId()));
                    feedCategories.add(feedCategoryTmp);
                }
            }

            Feeds feedsResponse = feverService.getFeeds(auth).execute().body();
            if (!feedsResponse.isSuccessful()) {
                throw new HttpException("获取失败");
            }

            Iterator<me.wizos.loread.bean.fever.Feed> feedsIterator = feedsResponse.getFeeds().iterator();
            me.wizos.loread.bean.fever.Feed feedItem;
            ArrayList<Feed> feeds = new ArrayList<>();
            while (feedsIterator.hasNext()) {
                feedItem = feedsIterator.next();
                feeds.add(feedItem.convert());
            }

            // 如果在获取到数据的时候就保存，那么到这里同步断了的话，可能系统内的文章就找不到响应的分组，所有放到这里保存。
            // 覆盖保存，只会保留最新一份。（比如在云端将文章移到的新的分组）
            coverSaveFeeds(feeds);
            coverSaveCategories(categories);
            coverFeedCategory(feedCategories);
            // 获取所有未读的资源
            XLog.i("2 - 同步文章信息");
            LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post(getString(R.string.step_sync_feed_info,"2."));

            // 获取未读资源
            UnreadItemIds unreadItemIdsRes = feverService.getUnreadItemIds(auth).execute().body();
            if (null == unreadItemIdsRes || !unreadItemIdsRes.isSuccessful()) {
                throw new HttpException("获取未读资源失败");
            }
            ArraySet<String> unreadRefsSet = handleUnreadRefs(unreadItemIdsRes.getUnreadItemIds());

            // 获取加星资源
            SavedItemIds savedItemIds = feverService.getSavedItemIds(auth).execute().body();
            if (null == savedItemIds || !savedItemIds.isSuccessful()) {
                throw new HttpException("获取加星资源失败");
            }
            ArraySet<String> staredRefsSet = handleStaredRefs(savedItemIds.getSavedItemIds());

            // 未读资源和加星资源去重合并
            HashSet<String> idRefsSet = new HashSet<>();
            idRefsSet.addAll(unreadRefsSet);
            idRefsSet.addAll(staredRefsSet);


            XLog.i("1 - 同步文章内容");
            XLog.i("文章id资源：" + idRefsSet.size() + " , " + idRefsSet);
            ArrayList<String> ids = new ArrayList<>(idRefsSet);

            int needFetchCount = ids.size();
            int hadFetchCount = 0;
            int num;

            ArrayList<Article> articles;
            Items items;

            while (needFetchCount > 0) {
                num = Math.min(needFetchCount, fetchContentCntForEach);
                items = feverService.getItemsWithIds(auth, StringUtils.join(",", ids.subList(hadFetchCount, hadFetchCount = hadFetchCount + num))).execute().body();

                needFetchCount = ids.size() - hadFetchCount;

                assert items != null;
                if (!items.isSuccessful()) {
                    throw new HttpException("获取失败");
                }
                List<Item> itemList = items.getItems();
                articles = new ArrayList<>(itemList.size());
                for (Item item : itemList) {
                    articles.add(Converter.from(item, new Converter.ArticleConvertListener() {
                        @Override
                        public Article onEnd(Article article) {
                            article.setCrawlDate(startSyncTimeMillis);
                            article.setUid(uid);
                            return article;
                        }
                    }));
                }

                CoreDB.i().articleDao().insert(articles);
                LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post(App.i().getString(R.string.step_sync_article_content,"1.", hadFetchCount, ids.size()));
            }

            LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post(getString(R.string.clear_article));
            deleteExpiredArticles();

            // 获取文章全文
            LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post(getString(R.string.fetch_article_full_content));
            fetchReadability(uid, startSyncTimeMillis);
            // 执行文章自动处理脚本
            TriggerRuleUtils.exeAllRules(uid, startSyncTimeMillis);
            // 清理无文章的tag
            //clearNotArticleTags(uid);

            // 提示更新完成
            LiveEventBus.get(SyncWorker.NEW_ARTICLE_NUMBER).post(hadFetchCount);
        } catch (NullPointerException | IllegalStateException e) {
            handleException(e, e.getMessage());
        } catch (HttpException e) {
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
        App.i().isSyncing = false;

        handleDuplicateArticles(startSyncTimeMillis);
        handleArticleInfo();
        LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post(null);
    }

    private void handleException(Exception e, String msg) {
        XLog.e("同步失败：" + e.getClass() + " = " + msg);
        e.printStackTrace();
        if (Contract.NOT_LOGGED_IN.equalsIgnoreCase(msg)) {
            ToastUtils.show(getString(R.string.not_logged_in));
        } else {
            ToastUtils.show(msg);
        }
    }

    @Override
    public void renameCategory(String tagId, String targetName, CallbackX cb) {
        cb.onFailure(App.i().getString(R.string.server_api_not_supported, Contract.PROVIDER_FEVER));
    }

    @Override
    public void deleteCategory(String categoryId, CallbackX cb) {
        cb.onFailure(App.i().getString(R.string.server_api_not_supported, Contract.PROVIDER_FEVER));
    }
    @Override
    public void renameFeed(String feedId, String renamedTitle, CallbackX cb) {
        cb.onFailure(App.i().getString(R.string.server_api_not_supported, Contract.PROVIDER_FEVER));
    }

    @Override
    public void addFeed(FeedEntries feedEntries, CallbackX cb) {
        SubscribeFeed subscribeToFeed = new SubscribeFeed(getAuthorization());
        subscribeToFeed.setFeedUrl(feedEntries.getFeed().getId());

        List<FeedCategory> feedCategories = feedEntries.getFeedCategories();
        if (feedCategories != null && feedCategories.size() != 0) {
            subscribeToFeed.setCategoryId(feedCategories.get(0).getCategoryId());
        }

        tinyRSSService.subscribeToFeed(subscribeToFeed).enqueue(new Callback<TinyResponse<SubscribeFeedResult>>() {
            @Override
            public void onResponse(@NotNull Call<TinyResponse<SubscribeFeedResult>> call, @NotNull Response<TinyResponse<SubscribeFeedResult>> response) {
                if (response.isSuccessful() && response.body().isSuccessful()) {
                    XLog.v("添加成功" + response.body().toString());
                    cb.onSuccess(App.i().getString(R.string.subscribe_success_plz_sync));
                } else {
                    cb.onFailure(App.i().getString(R.string.response_fail));
                }
            }

            @Override
            public void onFailure(@NotNull Call<TinyResponse<SubscribeFeedResult>> call, @NotNull Throwable t) {
                cb.onFailure(t.getMessage());
                XLog.v("添加失败");
            }
        });
    }

    @Override
    public void importOPML(Uri uri, CallbackX cb) {
        cb.onFailure(App.i().getString(R.string.server_api_not_supported, Contract.PROVIDER_FEVER));
    }
    @Override
    public void editFeedCategories(List<CategoryItem> lastCategoryItems, EditFeed editFeed, CallbackX cb) {
        cb.onFailure(App.i().getString(R.string.server_api_not_supported, Contract.PROVIDER_FEVER));
    }

    public void deleteFeed(String feedId, CallbackX cb) {
        UnsubscribeFeed unsubscribeFeed = new UnsubscribeFeed(getAuthorization());
        unsubscribeFeed.setFeedId(Integer.parseInt(feedId));
        tinyRSSService.unsubscribeFeed(unsubscribeFeed).enqueue(new retrofit2.Callback<TinyResponse<Map>>() {
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
        updateArticle.setArticleIds(articleIds);
        updateArticle.setField(field);
        updateArticle.setMode(mode);
        tinyRSSService.updateArticle(updateArticle).enqueue(new retrofit2.Callback<TinyResponse<UpdateArticleResult>>() {
            @Override
            public void onResponse(@NotNull retrofit2.Call<TinyResponse<UpdateArticleResult>> call, @NotNull Response<TinyResponse<UpdateArticleResult>> response) {
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
            public void onFailure(retrofit2.Call<TinyResponse<UpdateArticleResult>> call, Throwable t) {
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
