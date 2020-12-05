package me.wizos.loread.network.api;

import android.text.TextUtils;

import androidx.collection.ArrayMap;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import me.wizos.loread.App;
import me.wizos.loread.Contract;
import me.wizos.loread.R;
import me.wizos.loread.activity.login.LoginResult;
import me.wizos.loread.bean.feedly.CategoryItem;
import me.wizos.loread.bean.feedly.input.EditFeed;
import me.wizos.loread.bean.fever.BaseResponse;
import me.wizos.loread.bean.fever.Feeds;
import me.wizos.loread.bean.fever.Group;
import me.wizos.loread.bean.fever.GroupFeeds;
import me.wizos.loread.bean.fever.Groups;
import me.wizos.loread.bean.fever.Item;
import me.wizos.loread.bean.fever.Items;
import me.wizos.loread.bean.fever.MarkAction;
import me.wizos.loread.bean.fever.SavedItemIds;
import me.wizos.loread.bean.fever.UnreadItemIds;
import me.wizos.loread.bean.ttrss.request.Login;
import me.wizos.loread.config.ArticleActionConfig;
import me.wizos.loread.db.Article;
import me.wizos.loread.db.Category;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.Feed;
import me.wizos.loread.db.FeedCategory;
import me.wizos.loread.network.HttpClientManager;
import me.wizos.loread.network.SyncWorker;
import me.wizos.loread.network.callback.CallbackX;
import me.wizos.loread.utils.EncryptUtil;
import me.wizos.loread.utils.StringUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static me.wizos.loread.utils.StringUtils.getString;

/**
 * Created by Wizos on 2019/2/8.
 */
public class FeverApi extends AuthApi<Feed, CategoryItem> implements ILogin {
    private FeverService service;
    int fetchContentCntForEach = 50; // 每次获取内容的数量

    // public FeverApi() {
    //     this(App.i().getUser().getHost());
    // }

    public FeverApi(@NotNull String baseUrl) {
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

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(tempBaseUrl) // 设置网络请求的Url地址, 必须以/结尾
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().setLenient().create()))  // 设置数据解析器
                .client(HttpClientManager.i().feverHttpClient())
                .build();
        service = retrofit.create(FeverService.class);
    }

    public LoginResult login(String accountId, String accountPd) throws IOException {
        Login loginParam = new Login();
        loginParam.setUser(accountId);
        loginParam.setPassword(accountPd);
        String auth = EncryptUtil.MD5(accountId + ":" + accountPd);
        BaseResponse loginResultResponse = service.login(auth).execute().body();
        LoginResult loginResult = new LoginResult();
        if (loginResultResponse != null && loginResultResponse.getAuth() == 1) {
            return loginResult.setSuccess(true).setData(auth);
        } else {
            return loginResult.setSuccess(false).setData("登录失败");
        }
    }

    public void login(String account, String password, CallbackX cb) {
        Login loginParam = new Login();
        loginParam.setUser(account);
        loginParam.setPassword(password);
        String auth = EncryptUtil.MD5(account + ":" + password);
        service.login(auth).enqueue(new retrofit2.Callback<BaseResponse>() {
            @Override
            public void onResponse(retrofit2.Call<BaseResponse> call, Response<BaseResponse> response) {
                if (response.isSuccessful()) {
                    BaseResponse loginResponse = response.body();
                    if (loginResponse != null && loginResponse.isSuccessful()) {
                        cb.onSuccess(auth);
                        return;
                    }
                    cb.onFailure(App.i().getString(R.string.login_failed_reason, loginResponse.toString()));
                } else {
                    cb.onFailure(App.i().getString(R.string.login_failed_reason, response.message()));
                }
            }

            @Override
            public void onFailure(retrofit2.Call<BaseResponse> call, Throwable t) {
                cb.onFailure(App.i().getString(R.string.login_failed_reason, t.getMessage()));
            }
        });
    }

    public void fetchUserInfo(CallbackX cb) {
        cb.onFailure(App.i().getString(R.string.server_api_not_supported, Contract.PROVIDER_FEVER));
    }

    @Override
    public void sync() {
        try {
            long startSyncTimeMillis = System.currentTimeMillis();
            String uid = App.i().getUser().getId();

            XLog.i("同步 - 获取分类");
            LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post(getString(R.string.sync_feed_info));

            Groups groupsResponse = service.getGroups(getAuthorization()).execute().body();
            if (groupsResponse == null || !groupsResponse.isSuccessful()) {
                throw new HttpException("获取失败");
            }

            // Map<Integer,String> groupFeedsMap = groupsResponse.getFeedsGroupsMap();

            Iterator<Group> groupsIterator = groupsResponse.getGroups().iterator();
            Group group;
            Category category;
            FeedCategory feedCategoryTmp;
            String[] feedIds;
            String tmp;
            ArrayList<Category> categories = new ArrayList<>();
            ArrayList<FeedCategory> feedCategories = new ArrayList<>();
            while (groupsIterator.hasNext()) {
                group = groupsIterator.next();
                if (group.getId() < 1) {
                    continue;
                }
                category = group.convert();
                categories.add(category);

                // if(!groupFeedsMap.containsKey(group.getId())){
                //     continue;
                // }
                // tmp = groupFeedsMap.get(group.getId());
                // if(TextUtils.isEmpty(tmp)){
                //     continue;
                // }
                // feedIds = tmp.split(",");
                // if (feedIds.length == 0) {
                //     continue;
                // }
                // for (String feedId : feedIds) {
                //     feedCategoryTmp = new FeedCategory(uid, feedId, category.getId());
                //     feedCategories.add(feedCategoryTmp);
                // }
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

            Feeds feedsResponse = service.getFeeds(getAuthorization()).execute().body();
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

            // updateFeedUnreadCount();

            // 获取所有未读的资源
            XLog.e("2 - 同步文章信息");
            LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post(getString(R.string.sync_article_refs));

            // 获取未读资源
            UnreadItemIds unreadItemIdsRes = service.getUnreadItemIds(getAuthorization()).execute().body();
            if (null == unreadItemIdsRes || !unreadItemIdsRes.isSuccessful()) {
                throw new HttpException("获取未读资源失败");
            }
            HashSet<String> unreadRefsSet = handleUnreadRefs(unreadItemIdsRes.getUnreadItemIds());

            // 获取加星资源
            SavedItemIds savedItemIds = service.getSavedItemIds(getAuthorization()).execute().body();
            if (null == savedItemIds || !savedItemIds.isSuccessful()) {
                throw new HttpException("获取加星资源失败");
            }
            HashSet<String> staredRefsSet = handleStaredRefs(savedItemIds.getSavedItemIds());

            // 未读资源和加星资源去重合并
            HashSet<String> idRefsSet = new HashSet<>();
            idRefsSet.addAll(unreadRefsSet);
            idRefsSet.addAll(staredRefsSet);


            XLog.e("1 - 同步文章内容");
            XLog.i("文章id资源：" + idRefsSet.size() + " , " + idRefsSet);
            ArrayList<String> ids = new ArrayList<>(idRefsSet);

            int needFetchCount = ids.size();
            int hadFetchCount = 0;
            int num;

            ArrayList<Article> articles;
            Items items;

            while (needFetchCount > 0) {
                num = Math.min(needFetchCount, fetchContentCntForEach);
                items = service.getItemsWithIds(getAuthorization(), StringUtils.join(",", ids.subList(hadFetchCount, hadFetchCount = hadFetchCount + num))).execute().body();

                needFetchCount = ids.size() - hadFetchCount;

                assert items != null;
                if (!items.isSuccessful()) {
                    throw new HttpException("获取失败");
                }
                List<Item> itemList = items.getItems();
                articles = new ArrayList<>(itemList.size());
                long syncTimeMillis = System.currentTimeMillis();
                for (Item item : itemList) {
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
                LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post(App.i().getString(R.string.sync_article_content, hadFetchCount, ids.size()));
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
            ArticleActionConfig.i().exeRules(uid, startSyncTimeMillis);
            // 清理无文章的tag
            //clearNotArticleTags(uid);

            LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post(null);
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
    }

    private void handleException(Exception e, String msg) {
        XLog.e("同步失败：" + e.getClass() + " = " + msg);
        e.printStackTrace();
        if (Contract.NOT_LOGGED_IN.equalsIgnoreCase(msg)) {
            ToastUtils.show(getString(R.string.not_logged_in));
        } else {
            ToastUtils.show(msg);
        }
        updateCollectionCount();
        LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post(null);
    }

    @Override
    public void renameTag(String tagId, String targetName, CallbackX cb) {
        cb.onFailure(App.i().getString(R.string.server_api_not_supported, Contract.PROVIDER_FEVER));
    }

    public void addFeed(EditFeed editFeed, CallbackX cb) {
        cb.onFailure(App.i().getString(R.string.server_api_not_supported, Contract.PROVIDER_FEVER));
    }

    @Override
    public void renameFeed(String feedId, String renamedTitle, CallbackX cb) {
        cb.onFailure(App.i().getString(R.string.server_api_not_supported, Contract.PROVIDER_FEVER));
    }

    @Override
    public void editFeedCategories(List<CategoryItem> lastCategoryItems, EditFeed editFeed, CallbackX cb) {
        cb.onFailure(App.i().getString(R.string.server_api_not_supported, Contract.PROVIDER_FEVER));
    }

    public void unsubscribeFeed(String feedId, CallbackX cb) {
        cb.onFailure(App.i().getString(R.string.server_api_not_supported, Contract.PROVIDER_FEVER));
    }


    private void markArticles(String ids, String action, CallbackX cb) {
        service.markItemsByIds(getAuthorization(), ids, action).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NotNull Call<BaseResponse> call, @NotNull Response<BaseResponse> response) {
                if (response.isSuccessful()) {
                    cb.onSuccess(null);
                } else {
                    cb.onFailure("修改失败，原因未知");
                }
            }

            @Override
            public void onFailure(@NotNull Call<BaseResponse> call, @NotNull Throwable t) {
                cb.onFailure("修改失败，原因未知");
            }
        });
    }


    public void markArticleListReaded(Collection<String> articleIds, CallbackX cb) {
        markArticles(StringUtils.join(",", articleIds), MarkAction.READ, cb);
    }

    public void markArticleReaded(String articleId, CallbackX cb) {
        markArticles(articleId, MarkAction.READ, cb);
    }

    public void markArticleUnread(String articleId, CallbackX cb) {
        markArticles(articleId, MarkAction.UNREAD, cb);
    }

    public void markArticleStared(String articleId, CallbackX cb) {
        markArticles(articleId, MarkAction.SAVED, cb);
    }

    public void markArticleUnstar(String articleId, CallbackX cb) {
        markArticles(articleId, MarkAction.UNSAVED, cb);
    }

    private HashSet<String> handleUnreadRefs(String[] ids) {
        XLog.i("处理未读资源：" + ids.length);
        String uid = App.i().getUser().getId();

        List<Article> localUnreadArticles = CoreDB.i().articleDao().getUnreadNoOrder(uid);
        Map<String, Article> localUnreadMap = new ArrayMap<>(localUnreadArticles.size());
        for (Article article : localUnreadArticles) {
            localUnreadMap.put(article.getId(), article);
        }

        List<Article> localReadArticles = CoreDB.i().articleDao().getReadNoOrder(uid);
        Map<String, Article> localReadMap = new ArrayMap<>(localReadArticles.size());
        for (Article article : localReadArticles) {
            localReadMap.put(article.getId(), article);
        }

        List<Article> changedArticles = new ArrayList<>();
        // 筛选下来，最终要去云端获取内容的未读Refs的集合
        HashSet<String> tempUnreadIds = new HashSet<>(ids.length);
        // 数据量小的一方
        Article article;
        ArraySet<String> articleIds = new ArraySet<>(Arrays.asList(ids));
        for (String articleId : articleIds) {
            article = localUnreadMap.get(articleId);
            if (article == null) {
                article = localReadMap.get(articleId);
                if (article == null) {
                    // 本地无，而云端有，加入要请求的未读资源
                    tempUnreadIds.add(articleId);
                } else {
                    article.setReadStatus(App.STATUS_UNREAD);
                    changedArticles.add(article);
                    localReadMap.remove(articleId);
                }
            } else {
                localUnreadMap.remove(articleId);
            }
        }
        for (Map.Entry<String, Article> entry : localUnreadMap.entrySet()) {
            if (entry.getKey() != null) {
                article = localUnreadMap.get(entry.getKey());
                // 本地未读设为已读
                article.setReadStatus(App.STATUS_READED);
                changedArticles.add(article);
            }
        }

        CoreDB.i().articleDao().update(changedArticles);
        return tempUnreadIds;
    }


    private HashSet<String> handleStaredRefs(String[] ids) {
        String uid = App.i().getUser().getId();

        List<Article> localStarArticles = CoreDB.i().articleDao().getStaredNoOrder(uid);
        ArrayMap<String, Article> localStarMap = new ArrayMap<>(localStarArticles.size());
        // 第1步，遍历数据量大的一方A，将其比对项目放入Map中
        for (Article article : localStarArticles) {
            localStarMap.put(article.getId(), article);
        }

        List<Article> localUnstarArticles = CoreDB.i().articleDao().getUnStarNoOrder(uid);
        ArrayMap<String, Article> localUnstarMap = new ArrayMap<>(localUnstarArticles.size());
        for (Article article : localUnstarArticles) {
            localUnstarMap.put(article.getId(), article);
        }

        List<Article> changedArticles = new ArrayList<>();
        HashSet<String> tempStarredIds = new HashSet<>(ids.length);
        // 第2步，遍历数据量小的一方B。到Map中找，是否含有b中的比对项。有则XX，无则YY
        Article article;
        ArraySet<String> articleIds = new ArraySet<>(Arrays.asList(ids));
        for (String articleId : articleIds) {
            article = localStarMap.get(articleId);
            if (article == null) {
                article = localUnstarMap.get(articleId);
                if (article == null) {
                    // 本地无，而云远端有，加入要请求的加星资源
                    tempStarredIds.add(articleId);
                } else {
                    article.setStarStatus(App.STATUS_STARED);
                    changedArticles.add(article);
                    localUnstarMap.remove(articleId);
                }
            } else {
                localStarMap.remove(articleId);
            }
        }

        for (Map.Entry<String, Article> entry : localStarMap.entrySet()) {
            if (entry.getKey() != null) {
                article = localStarMap.get(entry.getKey());
                article.setStarStatus(App.STATUS_UNSTAR);
                changedArticles.add(article);// 取消加星
            }
        }

        CoreDB.i().articleDao().update(changedArticles);
        return tempStarredIds;
    }
}
