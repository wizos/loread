//package me.wizos.loreadx.net.ApiService;
//
//import android.text.TextUtils;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.collection.ArrayMap;
//
//import com.google.gson.GsonBuilder;
//import com.hjq.toast.ToastUtils;
//import com.lzy.okgo.callback.StringCallback;
//import com.lzy.okgo.exception.HttpException;
//import com.socks.library.KLog;
//
//import org.greenrobot.eventbus.EventBus;
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.select.Elements;
//
//import java.io.IOException;
//import java.net.ConnectException;
//import java.net.SocketTimeoutException;
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//
//import me.wizos.loreadx.App;
//import me.wizos.loreadx.R;
//import me.wizos.loreadx.activity.ui.login.LoginResult;
//import me.wizos.loreadx.config.GlobalConfig;
//import me.wizos.loreadx.bean.feedly.CategoryItem;
//import me.wizos.loreadx.bean.feedly.input.EditFeed;
//import me.wizos.loreadx.bean.fever.BaseResponse;
//import me.wizos.loreadx.bean.fever.Feeds;
//import me.wizos.loreadx.bean.fever.Group;
//import me.wizos.loreadx.bean.fever.Groups;
//import me.wizos.loreadx.bean.fever.SavedItemIds;
//import me.wizos.loreadx.bean.fever.UnreadItemIds;
//import me.wizos.loreadx.bean.ttrss.request.GetHeadlines;
//import me.wizos.loreadx.bean.ttrss.request.LoginParam;
//import me.wizos.loreadx.bean.ttrss.request.SubscribeToFeed;
//import me.wizos.loreadx.bean.ttrss.request.UnsubscribeFeed;
//import me.wizos.loreadx.bean.ttrss.request.UpdateArticle;
//import me.wizos.loreadx.bean.ttrss.result.SubscribeToFeedResult;
//import me.wizos.loreadx.bean.ttrss.result.TTRSSArticleItem;
//import me.wizos.loreadx.bean.ttrss.result.TTRSSResponse;
//import me.wizos.loreadx.bean.ttrss.result.UpdateArticleResult;
//import me.wizos.loreadx.content_extractor.Extractor;
//import me.wizos.loreadx.db.WithDB;
//import me.wizos.loreadx.db.Article;
//import me.wizos.loreadx.db.Category;
//import me.wizos.loreadx.db.Feed;
//import me.wizos.loreadx.db.FeedCategory;
//import me.wizos.loreadx.event.Sync;
//import me.wizos.loreadx.net.HttpClientManager;
//import me.wizos.loreadx.net.callback.CallbackX;
//import me.wizos.loreadx.utils.DataUtil;
//import me.wizos.loreadx.utils.StringUtil;
//import me.wizos.loreadx.utils.StringUtils;
//import okhttp3.Call;
//import okhttp3.Callback;
//import okhttp3.Request;
//import retrofit2.Response;
//import retrofit2.Retrofit;
//import retrofit2.converter.gson.GsonConverterFactory;
//
///**
// * Created by Wizos on 2019/2/8.
// */
//
//public class FeverApi extends AuthApi<Feed, CategoryItem> implements LoginInterface{
//    private FeverService service;
//    public static String HOST = "";
//    private String authorization;
//    public int fetchContentCntForEach = 20;
//
//    public FeverApi() {
//        if (TextUtils.isEmpty(HOST)) {
//            FeverApi.HOST = App.i().getUser().getHost();
//        }
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(FeverApi.HOST) // 设置网络请求的Url地址, 必须以/结尾
//                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().setLenient().create()))  // 设置数据解析器
//                .client(HttpClientManager.i().simpleClient())
//                .build();
//        service = retrofit.create(FeverService.class);
//    }
//
//    public static void setHOST(String HOST) {
//        FeverApi.HOST = HOST;
//    }
//
//    public void setAuthorization(String authorization) {
//        this.authorization = authorization;
//    }
//
//    public LoginResult login(String accountId, String accountPd) throws IOException {
//        LoginParam loginParam = new LoginParam();
//        loginParam.setUser(accountId);
//        loginParam.setPassword(accountPd);
//        String auth = StringUtil.MD5(accountId+":"+accountPd);
//        BaseResponse loginResultTTRSSResponse = service.login(auth).execute().body();
//        LoginResult loginResult = new LoginResult();
//        if (loginResultTTRSSResponse!= null && loginResultTTRSSResponse.getAuth() == 1) {
//            return loginResult.setSuccess(true).setData(auth);
//        } else {
//            return loginResult.setSuccess(false).setData("登录失败");
//        }
//    }
//
//    public void login(String accountId, String accountPd,CallbackX cb){
//        LoginParam loginParam = new LoginParam();
//        loginParam.setUser(accountId);
//        loginParam.setPassword(accountPd);
//        String auth = StringUtil.MD5(accountId+":"+accountPd);
//        service.login(auth).enqueue(new retrofit2.Callback<BaseResponse>() {
//            @Override
//            public void onResponse(retrofit2.Call<BaseResponse> call, Response<BaseResponse> response) {
//                if(response.isSuccessful()){
//                    BaseResponse loginResponse = response.body();
//                    if( loginResponse != null && !loginResponse.isSuccessful() ){
//                        cb.onSuccess(auth);
//                        return;
//                    }
//                    cb.onFailure("登录失败：原因未知" + loginResponse.toString());
//                }else {
//                    cb.onFailure("登录失败：原因未知" + response.message());
//                }
//            }
//
//            @Override
//            public void onFailure(retrofit2.Call<BaseResponse> call, Throwable t) {
//                cb.onFailure("登录失败：" + t.getMessage());
//            }
//        });
//    }
//
//    public void fetchUserInfo(CallbackX cb){
//        cb.onFailure("暂时不支持");
//    }
//    private long syncTimeMillis;
//
//    @Override
//    public void sync() {
//        App.i().isSyncing = true;
//        EventBus.getDefault().post(new Sync(Sync.START));
//        try {
//            KLog.e("3 - 同步订阅源信息");
//            EventBus.getDefault().post(new Sync(Sync.DOING, App.i().getString(R.string.main_toolbar_hint_sync_tag_feed)));
//
//            Groups groupsResponse = service.getCategoryItems(authorization).execute().body();
//            if (groupsResponse == null || !groupsResponse.isSuccessful()) {
//                throw new HttpException("获取失败");
//            }
//
//            Iterator<Group> categoryItemsIterator = groupsResponse.getGroups().iterator();
//            Group group;
//            Category category;
//            FeedCategory feedCategoryTmp;
//            String[] feedIds;
//            ArrayList<Category> categories = new ArrayList<>();
//            ArrayList<FeedCategory> feedCategories = new ArrayList<>();
//            while (categoryItemsIterator.hasNext()) {
//                group = categoryItemsIterator.next();
//                if (group.getId() < 1) {
//                    continue;
//                }
//                category = group.getCategry();
//                categories.add(category);
//
//                feedIds = group.getFeedIds();
//                if( null == feedIds || feedIds.length == 0 ){
//                    continue;
//                }
//                for (String feedId:feedIds) {
//                    feedCategoryTmp = new FeedCategory();
//                    feedCategoryTmp.setCategoryId(category.getId());
//                    feedCategoryTmp.setFeedId(feedId);
//                    feedCategories.add(feedCategoryTmp);
//                }
//            }
//
//
//           Feeds feedItemsTTRSSResponse = service.getFeeds(authorization).execute().body();
//            if (!feedItemsTTRSSResponse.isSuccessful()) {
//                throw new HttpException("获取失败");
//            }
//
//            Iterator<me.wizos.loreadx.bean.fever.Feed> feedItemsIterator = feedItemsTTRSSResponse.getFeeds().iterator();
//            me.wizos.loreadx.bean.fever.Feed feedItem;
//            ArrayList<Feed> feeds = new ArrayList<>();
//            while (feedItemsIterator.hasNext()) {
//                feedItem = feedItemsIterator.next();
//                feeds.add(feedItem.convert());
//            }
//
//            // 如果在获取到数据的时候就保存，那么到这里同步断了的话，可能系统内的文章就找不到响应的分组，所有放到这里保存。
//            // 覆盖保存，只会保留最新一份。（比如在云端将文章移到的新的分组）
//            WithDB.i().coverSaveFeeds(feeds);
//            WithDB.i().coverSaveCategories(categories);
//            WithDB.i().coverFeedCategory(feedCategories);
//
//            // updateFeedUnreadCount();
//
//
//
//            syncTimeMillis = System.currentTimeMillis();
//            // 获取所有未读的资源
//            EventBus.getDefault().post(new Sync(Sync.DOING, App.i().getString(R.string.sync_article_refs)));
//            HashSet<String> idRefsSet = new HashSet<>();
//
//            UnreadItemIds unreadItemIdsRes = service.getUnreadItemIds(authorization).execute().body();
//            if( null ==  unreadItemIdsRes || !unreadItemIdsRes.isSuccessful() ){
//                throw new HttpException("获取文章资源失败");
//            }
//            String[] unreadIds = unreadItemIdsRes.getUreadItemIds();
//            if( null != unreadIds && unreadIds.length != 0 ){
//                for (String id:unreadIds) {
//                    idRefsSet.add(id);
//                }
//            }
//
//            SavedItemIds savedItemIds = service.getSavedItemIds(authorization).execute().body();
//            if( null ==  savedItemIds || !savedItemIds.isSuccessful() ){
//                throw new HttpException("获取文章资源失败");
//            }
//            String[] savedIds = savedItemIds.getSavedItemIds();
//            if( null != savedIds && savedIds.length != 0 ){
//                for (String id:savedIds) {
//                    idRefsSet.add(id);
//                }
//            }
//
//
////            ids = new ArrayList<>(refsList.get(0));
////            needFetchCount = ids.size();
////            hadFetchCount = 0;
//            while (idRefsSet.size() > 0){
//                int needFetchCount = Math.min(idRefsSet.size(),fetchContentCntForEach);
//
//            }
//
//
//            GetHeadlines getHeadlines = new GetHeadlines();
//            getHeadlines.setSid(authorization);
//
//            Article article = WithDB.i().getLastArticle();
//            if (null != article) {
//                getHeadlines.setSince_id(article.getId());
//            }
//            TTRSSResponse<List<TTRSSArticleItem>> ttrssArticleItemsResponse;
//            Iterator<TTRSSArticleItem> ttrssArticleItemIterator;
//            ArrayList<Article> articles;
//            TTRSSArticleItem ttrssArticleItem;
//            int hadFetchCountUnit, hadFetchCountTotal;
//
//
//            List<Integer> cloudyRefs;
//
//            // 获取所有未读的资源
//            EventBus.getDefault().post(new Sync(Sync.DOING, App.i().getString(R.string.sync_article_refs)));
//            getHeadlines.setShow_content(false);
//            getHeadlines.setLimit(200);
//            cloudyRefs = new ArrayList<>();
//            hadFetchCountTotal = hadFetchCountUnit = 0;
//            do {
//                hadFetchCountTotal = hadFetchCountTotal + hadFetchCountUnit;
//                getHeadlines.setSkip(hadFetchCountTotal);
//                ttrssArticleItemsResponse = service.getHeadlines(getHeadlines).execute().body();
//                if (!ttrssArticleItemsResponse.isSuccessful()) {
//                    throw new HttpException("获取失败");
//                }
//                ttrssArticleItemIterator = ttrssArticleItemsResponse.getData().iterator();
//                hadFetchCountUnit = ttrssArticleItemsResponse.getData().size();
//                while (ttrssArticleItemIterator.hasNext()) {
//                    cloudyRefs.add(ttrssArticleItemIterator.next().getId());
//                }
//            } while (hadFetchCountUnit > 0);
//            HashSet<String> unreadRefsSet = handleUnreadRefs(cloudyRefs);
//
//
//            // 获取所有加星的资源
//            getHeadlines.setFeed_id("-1");
//            getHeadlines.setView_mode("all_articles");
//            cloudyRefs = new ArrayList<>();
//            hadFetchCountTotal = hadFetchCountUnit = 0;
//            do {
//                hadFetchCountTotal = hadFetchCountTotal + hadFetchCountUnit;
//                getHeadlines.setSkip(hadFetchCountTotal);
//                ttrssArticleItemsResponse = service.getHeadlines(getHeadlines).execute().body();
//                if (!ttrssArticleItemsResponse.isSuccessful()) {
//                    throw new HttpException("获取失败");
//                }
//                ttrssArticleItemIterator = ttrssArticleItemsResponse.getData().iterator();
//                hadFetchCountUnit = ttrssArticleItemsResponse.getData().size();
//                while (ttrssArticleItemIterator.hasNext()) {
//                    cloudyRefs.add(ttrssArticleItemIterator.next().getId());
//                }
//            } while (hadFetchCountUnit > 0);
//            HashSet<String> staredRefsSet = handleStaredRefs(cloudyRefs);
//
////            ArrayList<HashSet<String>> refsList = splitRefs(unreadRefsSet, staredRefsSet);
////            int readySyncArtsCapacity = refsList.get(0).size() + refsList.get(1).size() + refsList.get(2).size();
//
////            List<String> ids;
////            int alreadySyncedArtsNum = 0, hadFetchCount, needFetchCount, num;
////            ArrayList<Article> tempArticleList;
////            ArrayMap<String, ArrayList<Article>> needReadabilityArticles = new ArrayMap<String, ArrayList<Article>>();
////
////            ids = new ArrayList<>(refsList.get(0));
////            needFetchCount = ids.size();
////            hadFetchCount = 0;
////
////            KLog.e("1 - 同步文章内容");
////            //   KLog.e("栈的数量A:" + ids.size());
////            syncTimeMillis = System.currentTimeMillis();
////            while (needFetchCount > 0) {
////                num = Math.min(needFetchCount, fetchContentCntForEach);
////                getHeadlines.setSkip(alreadySyncedArtsNum);
////
////                ids.subList(hadFetchCount, hadFetchCount = hadFetchCount + num);
////
////                ttrssArticleItemsResponse = service.getHeadlines(getHeadlines).execute().body();
////                if (!ttrssArticleItemsResponse.isSuccessful()) {
////                    throw new HttpException("获取失败");
////                }
////
////
////                ttrssArticleItemIterator = ttrssArticleItemsResponse.getData().iterator();
////                hadFetchCountUnit = ttrssArticleItemsResponse.getData().size();
////
////                articles = new ArrayList<>(ttrssArticleItemsResponse.getData().size());
////
////                while (ttrssArticleItemIterator.hasNext()) {
////                    ttrssArticleItem = ttrssArticleItemIterator.next();
////                    articles.add(ttrssArticleItem.convert(unreadArticleChanger));
////                }
////                classArticles = classArticlesByFeedId(classArticles, articles);
////                WithDB.i().saveArticles(articles);
////
////
////                alreadySyncedArtsNum = alreadySyncedArtsNum + num;
////                needFetchCount = ids.size() - hadFetchCount;
////                EventBus.getDefault().post(new Sync(Sync.DOING, App.i().getString(R.string.main_toolbar_hint_sync_article_content, alreadySyncedArtsNum, readySyncArtsCapacity)));
////            }
////
////            do {
////                hadFetchCountTotal = hadFetchCountTotal + hadFetchCountUnit;
////                getHeadlines.setSkip(hadFetchCountTotal);
////                EventBus.getDefault().post(new Sync(Sync.DOING, App.i().getString(R.string.sync_article_content, hadFetchCountTotal)));
////                ttrssArticleItemsResponse = service.getHeadlines(getHeadlines).execute().body();
////                if (!ttrssArticleItemsResponse.isSuccessful()) {
////                    throw new HttpException("获取失败");
////                }
////                ttrssArticleItemIterator = ttrssArticleItemsResponse.getData().iterator();
////                hadFetchCountUnit = ttrssArticleItemsResponse.getData().size();
////
////                articles = new ArrayList<>(ttrssArticleItemsResponse.getData().size());
////
////                while (ttrssArticleItemIterator.hasNext()) {
////                    ttrssArticleItem = ttrssArticleItemIterator.next();
////                    articles.add(ttrssArticleItem.convert(unreadArticleChanger));
////                }
////                classArticles = classArticlesByFeedId(classArticles, articles);
////                WithDB.i().saveArticles(articles);
////            } while (hadFetchCountUnit > 0);
////
////
////
////            ids = new ArrayList<>(refsList.get(2));
////            needFetchCount = ids.size();
////            hadFetchCount = 0;
////            //            KLog.e("栈的数量C:" + ids.size());
////            while (needFetchCount > 0) {
////                num = Math.min(needFetchCount, fetchContentCntForEach);
////                List<Entry> entryList = service.getItemContents(ids.subList(hadFetchCount, hadFetchCount = hadFetchCount + num)).execute().body();
////                tempArticleList = parseItemContents(entryList, new ArticleChanger() {
////                    @Override
////                    public Article change(Article article) {
////                        article.setReadStatus(App.STATUS_UNREAD);
////                        article.setStarStatus(App.STATUS_STARED);
////                        article.setCrawlDate(syncTimeMillis);
////                        return article;
////                    }
////                });
////                WithDB.i().saveArticles(tempArticleList);
////                needReadabilityArticles = classArticlesByFeedId(needReadabilityArticles, tempArticleList);
////                alreadySyncedArtsNum = alreadySyncedArtsNum + num;
////                needFetchCount = ids.size() - hadFetchCount;
////                EventBus.getDefault().post(new Sync(Sync.DOING, App.i().getString(R.string.main_toolbar_hint_sync_article_content, alreadySyncedArtsNum, readySyncArtsCapacity)));
////            }
////
////            ids = new ArrayList<>(refsList.get(1));
////            needFetchCount = ids.size();
////            hadFetchCount = 0;
////            //            KLog.e("栈的数量B:" + ids.size());
////            while (needFetchCount > 0) {
////                num = Math.min(needFetchCount, fetchContentCntForEach);
////                List<Entry> entryList = service.getItemContents(ids.subList(hadFetchCount, hadFetchCount = hadFetchCount + num)).execute().body();
////                tempArticleList = parseItemContents(entryList, new ArticleChanger() {
////                    @Override
////                    public Article change(Article article) {
////                        article.setReadStatus(App.STATUS_READED);
////                        article.setStarStatus(App.STATUS_STARED);
////                        article.setCrawlDate(syncTimeMillis);
////                        return article;
////                    }
////                });
////                WithDB.i().saveArticles(tempArticleList);
////                needReadabilityArticles = classArticlesByFeedId(needReadabilityArticles, tempArticleList);
////                alreadySyncedArtsNum = alreadySyncedArtsNum + num;
////                needFetchCount = ids.size() - hadFetchCount;
////                EventBus.getDefault().post(new Sync(Sync.DOING, App.i().getString(R.string.main_toolbar_hint_sync_article_content, alreadySyncedArtsNum, readySyncArtsCapacity)));
////            }
////
////            updateFeedUnreadCount();
////
////            WithDB.i().handleDuplicateArticle();
////
////            // 获取文章全文
////            EventBus.getDefault().post(new Sync(Sync.DOING, App.i().getString(R.string.main_toolbar_hint_sync_article_readability_content)));
////            fetchReadability(needReadabilityArticles);
////            EventBus.getDefault().post(new Sync(Sync.END));
//
//            ///////////
//
//
//            KLog.e(" 2 - 同步未读文章");
//            ArrayMap<String, ArrayList<Article>> classArticles = new ArrayMap<String, ArrayList<Article>>();
//
//
//            ArticleChanger unreadArticleChanger = new ArticleChanger() {
//                @Override
//                public Article change(Article article) {
//                    article.setCrawlDate(syncTimeMillis);
//                    return article;
//                }
//            };
//            hadFetchCountUnit = 0;
//            hadFetchCountTotal = 0;
//            do {
//                hadFetchCountTotal = hadFetchCountTotal + hadFetchCountUnit;
//                getHeadlines.setSkip(hadFetchCountTotal);
//                EventBus.getDefault().post(new Sync(Sync.DOING, App.i().getString(R.string.sync_article_content, hadFetchCountTotal)));
//                ttrssArticleItemsResponse = service.getHeadlines(getHeadlines).execute().body();
//                if (!ttrssArticleItemsResponse.isSuccessful()) {
//                    throw new HttpException("获取失败");
//                }
//                ttrssArticleItemIterator = ttrssArticleItemsResponse.getData().iterator();
//                hadFetchCountUnit = ttrssArticleItemsResponse.getData().size();
//
//                articles = new ArrayList<>(ttrssArticleItemsResponse.getData().size());
//
//                while (ttrssArticleItemIterator.hasNext()) {
//                    ttrssArticleItem = ttrssArticleItemIterator.next();
//                    articles.add(ttrssArticleItem.convert(unreadArticleChanger));
//                }
//                classArticles = classArticlesByFeedId(classArticles, articles);
//                WithDB.i().saveArticles(articles);
//            } while (hadFetchCountUnit > 0);
//
//
//            getHeadlines.setFeed_id("-1");
//            getHeadlines.setView_mode("all_articles");
//            ArticleChanger staredArticleChanger = new ArticleChanger() {
//                @Override
//                public Article change(Article article) {
//                    article.setStarStatus(App.STATUS_STARED);
//                    article.setCrawlDate(syncTimeMillis);
//                    return article;
//                }
//            };
//            int fetchCountTotal = hadFetchCountTotal;
//            KLog.e(" 1 - 同步加星文章");
//            hadFetchCountUnit = 0;
//            hadFetchCountTotal = 0;
//            do {
//                hadFetchCountTotal = hadFetchCountTotal + hadFetchCountUnit;
//                getHeadlines.setSkip(hadFetchCountTotal);
//                EventBus.getDefault().post(new Sync(Sync.DOING, App.i().getString(R.string.sync_article_content, fetchCountTotal + hadFetchCountTotal)));
//                ttrssArticleItemsResponse = service.getHeadlines(getHeadlines).execute().body();
//                if (!ttrssArticleItemsResponse.isSuccessful()) {
//                    throw new HttpException("获取失败");
//                }
//                ttrssArticleItemIterator = ttrssArticleItemsResponse.getData().iterator();
//                hadFetchCountUnit = ttrssArticleItemsResponse.getData().size();
//
//                articles = new ArrayList<>(ttrssArticleItemsResponse.getData().size());
//
//                while (ttrssArticleItemIterator.hasNext()) {
//                    ttrssArticleItem = ttrssArticleItemIterator.next();
//                    articles.add(ttrssArticleItem.convert(staredArticleChanger));
//                }
//                classArticles = classArticlesByFeedId(classArticles, articles);
//                WithDB.i().saveArticles(articles);
//            } while (hadFetchCountUnit > 0);
//
//            updateFeedUnreadCount();
//
//            WithDB.i().handleDuplicateArticle();
//
//            // 获取文章全文
//            EventBus.getDefault().post(new Sync(Sync.DOING, App.i().getString(R.string.main_toolbar_hint_sync_article_readability_content)));
//            fetchReadability(classArticles);
//            EventBus.getDefault().post(new Sync(Sync.END));
//        } catch (HttpException e) {
//            KLog.e("同步时产生HttpException：" + e.message());
//            e.printStackTrace();
//            handleException(e);
//        } catch (ConnectException e) {
//            KLog.e("同步时产生异常ConnectException");
//            e.printStackTrace();
//            handleException(e);
//        } catch (SocketTimeoutException e) {
//            KLog.e("同步时产生异常SocketTimeoutException");
//            e.printStackTrace();
//            handleException(e);
//        } catch (IOException e) {
//            KLog.e("同步时产生异常IOException");
//            e.printStackTrace();
//            handleException(e);
//        } catch (RuntimeException e) {
//            KLog.e("同步时产生异常RuntimeException");
//            e.printStackTrace();
//            handleException(e);
//        }
//        App.i().isSyncing = false;
//    }
//
//    private void handleException(Exception e) {
//        if (e instanceof HttpException) {
//            ToastUtils.show("网络异常：" + e.getMessage());
//            // EventBus.getDefault().post(new Sync(Sync.NEED_AUTH));
//        } else {
//            updateFeedUnreadCount();
//        }
//
//        App.i().isSyncing = false;
//        EventBus.getDefault().post(new Sync(Sync.ERROR));
//    }
//
//    @Override
//    public void renameTag(String tagId, String targetName, CallbackX cb) {
//        cb.onFailure("暂时不支持");
//    }
//
//    public void addFeed(EditFeed editFeed, CallbackX cb) {
//        SubscribeToFeed subscribeToFeed = new SubscribeToFeed();
//        subscribeToFeed.setSid(authorization);
//        subscribeToFeed.setFeed_url(editFeed.getId().replace("feed/", ""));
//        if (editFeed.getCategoryItems() != null && editFeed.getCategoryItems().size() != 0) {
//            subscribeToFeed.setCategory_id(editFeed.getCategoryItems().get(0).getId());
//        }
//        service.subscribeToFeed(subscribeToFeed).enqueue(new retrofit2.Callback<TTRSSResponse<SubscribeToFeedResult>>() {
//            @Override
//            public void onResponse(retrofit2.Call<TTRSSResponse<SubscribeToFeedResult>> call, Response<TTRSSResponse<SubscribeToFeedResult>> response) {
//                if (response.isSuccessful() && response.body().isSuccessful()) {
//                    KLog.e("添加成功" + response.body().toString());
//                    cb.onSuccess("添加成功");
//                } else {
//                    cb.onFailure("响应失败");
//                }
//            }
//
//            @Override
//            public void onFailure(retrofit2.Call<TTRSSResponse<SubscribeToFeedResult>> call, Throwable t) {
//                cb.onFailure("添加失败");
//                KLog.e("添加失败");
//            }
//        });
//    }
//
////    public CallWrap addFeed(EditFeed editFeed) {
////        SubscribeToFeed subscribeToFeed = new SubscribeToFeed();
////        subscribeToFeed.setSid(authorization);
////        subscribeToFeed.setFeed_url(editFeed.getId().replace("feed/", ""));
////        if (editFeed.getCategoryItems() != null && editFeed.getCategoryItems().size() != 0) {
////            subscribeToFeed.setCategory_id(editFeed.getCategoryItems().get(0).getId());
////        }
////
////        return new CallWrap<>(service.subscribeToFeed(subscribeToFeed), new CallbackWarp<TTRSSResponse<SubscribeToFeedResult>>() {
////            @Override
////            public void onResponse(retrofit2.Call<TTRSSResponse<SubscribeToFeedResult>> call, retrofit2.Response<TTRSSResponse<SubscribeToFeedResult>> response, retrofit2.Callback callbackResult) {
////                if (response.isSuccessful() && response.body().isSuccessful()) {
////                    KLog.e("添加成功" + response.body().toString());
////                    callbackResult.onResponse(call, response);
////                } else {
////                    callbackResult.onFailure(call, new RuntimeException("响应失败"));
////                }
////            }
////
////            @Override
////            public void onFailure(retrofit2.Call<TTRSSResponse<SubscribeToFeedResult>> call, Throwable t, retrofit2.Callback callbackResult) {
////                callbackResult.onFailure(call, t);
////                KLog.e("添加失败");
////            }
////        });
////    }
//
//    @Override
//    public void renameFeed(String feedId, String renamedTitle, CallbackX cb) {
//        cb.onFailure("暂时不支持");
//    }
//
//    /**
//     * 订阅，编辑feed
//     *
//     * @param feedId
//     * @param feedTitle
//     * @param categoryItems
//     * @param cb
//     */
//    public void editFeed(@NonNull String feedId, @Nullable String feedTitle, @Nullable ArrayList<CategoryItem> categoryItems, StringCallback cb) {
//    }
//
//
//    @Override
//    public void editFeedCategories(List<CategoryItem> lastCategoryItems, EditFeed editFeed,CallbackX cb) {
//        cb.onFailure("暂时不支持");
//    }
//
//    public void unsubscribeFeed(String feedId,CallbackX cb) {
//        UnsubscribeFeed unsubscribeFeed = new UnsubscribeFeed();
//        unsubscribeFeed.setSid(authorization);
//        unsubscribeFeed.setFeed_id(Integer.valueOf(feedId));
//        service.unsubscribeFeed(unsubscribeFeed).enqueue(new retrofit2.Callback<TTRSSResponse<Map>>() {
//            @Override
//            public void onResponse(retrofit2.Call<TTRSSResponse<Map>> call, Response<TTRSSResponse<Map>> response) {
//                if(response.isSuccessful() && "OK".equals(response.body().getData().get("status"))){
//                    cb.onSuccess("退订成功");
//                }else {
//                    cb.onFailure("退订失败" + response.body().getData().toString());
//                }
//            }
//
//            @Override
//            public void onFailure(retrofit2.Call<TTRSSResponse<Map>> call, Throwable t) {
//                cb.onSuccess("退订失败" + t.getMessage());
//            }
//        });
//    }
//
//
//    private void markArticles(int field, int mode, List<String> ids,CallbackX cb) {
//        UpdateArticle updateArticle = new UpdateArticle();
//        updateArticle.setSid(authorization);
//        updateArticle.setArticle_ids(StringUtils.join(",", ids));
//        updateArticle.setField(field);
//        updateArticle.setMode(mode);
//        service.updateArticle(updateArticle).enqueue(new retrofit2.Callback<TTRSSResponse<UpdateArticleResult>>() {
//            @Override
//            public void onResponse(retrofit2.Call<TTRSSResponse<UpdateArticleResult>> call, Response<TTRSSResponse<UpdateArticleResult>> response) {
//                if (response.isSuccessful() ){
//                    cb.onSuccess(null);
//                }else {
//                    cb.onFailure("修改失败，原因未知");
//                }
//            }
//
//            @Override
//            public void onFailure(retrofit2.Call<TTRSSResponse<UpdateArticleResult>> call, Throwable t) {
//                cb.onFailure("修改失败，原因未知");
//            }
//        });
//    }
//
//    private void markArticle(int field, int mode, String articleId,CallbackX cb) {
//        UpdateArticle updateArticle = new UpdateArticle();
//        updateArticle.setSid(authorization);
//        updateArticle.setArticle_ids(articleId);
//        updateArticle.setField(field);
//        updateArticle.setMode(mode);
//        service.updateArticle(updateArticle).enqueue(new retrofit2.Callback<TTRSSResponse<UpdateArticleResult>>() {
//            @Override
//            public void onResponse(retrofit2.Call<TTRSSResponse<UpdateArticleResult>> call, Response<TTRSSResponse<UpdateArticleResult>> response) {
//                if (response.isSuccessful() ){
//                    cb.onSuccess(null);
//                }else {
//                    cb.onFailure("修改失败，原因未知");
//                }
//            }
//
//            @Override
//            public void onFailure(retrofit2.Call<TTRSSResponse<UpdateArticleResult>> call, Throwable t) {
//                cb.onFailure("修改失败，原因未知");
//            }
//        });
//    }
//
//    public void markArticleListReaded(List<String> articleIds,CallbackX cb) {
//        markArticles(2, 0, articleIds, cb);
//    }
//
//    public void markArticleReaded(String articleId, CallbackX cb) {
//        markArticle(2, 0, articleId, cb);
//    }
//
//    public void markArticleUnread(String articleId, CallbackX cb) {
//        markArticle(2, 1, articleId, cb);
//    }
//
//    public void markArticleStared(String articleId, CallbackX cb) {
//        markArticle(0, 1, articleId, cb);
//    }
//
//    public void markArticleUnstar(String articleId,CallbackX cb) {
//        markArticle(0, 0, articleId, cb);
//    }
//
////
////    private retrofit2.Call<TTRSSResponse<UpdateArticleResult>> markArticles(int field, int mode, List<String> ids) {
////        UpdateArticle updateArticle = new UpdateArticle();
////        updateArticle.setSid(authorization);
////        updateArticle.setArticle_ids(StringUtils.join(",", ids));
////        updateArticle.setField(field);
////        updateArticle.setMode(mode);
////        return service.updateArticle(updateArticle);
////    }
////
////    private retrofit2.Call<TTRSSResponse<UpdateArticleResult>> markArticle(int field, int mode, String articleId) {
////        UpdateArticle updateArticle = new UpdateArticle();
////        updateArticle.setSid(authorization);
////        updateArticle.setArticle_ids(articleId);
////        updateArticle.setField(field);
////        updateArticle.setMode(mode);
////        return service.updateArticle(updateArticle);
////    }
////
////    public retrofit2.Call markArticleListReaded(List<String> articleIds) {
////        return markArticles(2, 0, articleIds);
////    }
////
////    public CallWrap markArticleReaded(String articleId) {
////        return new CallWrap<>(markArticle(2, 0, articleId), new CallbackWarp() {
////            @Override
////            public void onResponse(retrofit2.Call call, retrofit2.Response response, retrofit2.Callback callbackResult) {
////                if (response.isSuccessful()) {
////                    callbackResult.onResponse(call, response);
////                } else {
////                    callbackResult.onFailure(call, new RuntimeException("响应失败"));
////                }
////            }
////
////            @Override
////            public void onFailure(retrofit2.Call call, Throwable t, retrofit2.Callback callbackResult) {
////                callbackResult.onFailure(call, t);
////            }
////        });
////    }
////
////    public CallWrap markArticleUnread(String articleId) {
////        return new CallWrap(markArticle(2, 1, articleId), new CallbackWarp() {
////            @Override
////            public void onResponse(retrofit2.Call call, retrofit2.Response response, retrofit2.Callback callbackResult) {
////                if (response.isSuccessful()) {
////                    callbackResult.onResponse(call, response);
////                } else {
////                    callbackResult.onFailure(call, new RuntimeException("响应失败"));
////                }
////            }
////
////            @Override
////            public void onFailure(retrofit2.Call call, Throwable t, retrofit2.Callback callbackResult) {
////                callbackResult.onFailure(call, t);
////            }
////        });
////    }
////
////    public CallWrap markArticleStared(String articleId) {
////        return new CallWrap(markArticle(0, 1, articleId), new CallbackWarp() {
////            @Override
////            public void onResponse(retrofit2.Call call, retrofit2.Response response, retrofit2.Callback callbackResult) {
////                if (response.isSuccessful()) {
////                    callbackResult.onResponse(call, response);
////                } else {
////                    callbackResult.onFailure(call, new RuntimeException("响应失败"));
////                }
////            }
////
////            @Override
////            public void onFailure(retrofit2.Call call, Throwable t, retrofit2.Callback callbackResult) {
////                callbackResult.onFailure(call, t);
////            }
////        });
////    }
////
////    public CallWrap markArticleUnstar(String articleId) {
////        return new CallWrap(markArticle(0, 0, articleId), new CallbackWarp() {
////            @Override
////            public void onResponse(retrofit2.Call call, retrofit2.Response response, retrofit2.Callback callbackResult) {
////                if (response.isSuccessful()) {
////                    callbackResult.onResponse(call, response);
////                } else {
////                    callbackResult.onFailure(call, new RuntimeException("响应失败"));
////                }
////            }
////
////            @Override
////            public void onFailure(retrofit2.Call call, Throwable t, retrofit2.Callback callbackResult) {
////                callbackResult.onFailure(call, t);
////            }
////        });
////    }
//
//    private HashSet<String> handleUnreadRefs(List<Integer> ids) {
//        List<Article> localUnreadArticles = WithDB.i().getArtsUnreadNoOrder();
//        Map<String, Article> localUnreadArticlesMap = new ArrayMap<>(localUnreadArticles.size());
//        List<Article> changedArticles = new ArrayList<>();
//        // 筛选下来，最终要去云端获取内容的未读Refs的集合
//        HashSet<String> tempUnreadIds = new HashSet<>(ids.size());
//        // 数据量大的一方
//        for (Article article : localUnreadArticles) {
//            localUnreadArticlesMap.put(article.getId(), article);
//        }
//        // 数据量小的一方
//        Article article;
//        for (Integer articleId : ids) {
//            article = localUnreadArticlesMap.get(articleId+"");
//            if (article != null) {
//                localUnreadArticlesMap.remove(articleId+"");
//            } else {
//                article = WithDB.i().getArticle(articleId+"");
//                if (article != null && article.getReadStatus() == App.STATUS_READED) {
//                    article.setReadStatus(App.STATUS_UNREAD);
//                    changedArticles.add(article);
//                } else {
//                    // 本地无，而云端有，加入要请求的未读资源
//                    tempUnreadIds.add(articleId+"");
//                }
//            }
//        }
//        for (Map.Entry<String, Article> entry : localUnreadArticlesMap.entrySet()) {
//            if (entry.getKey() != null) {
//                article = localUnreadArticlesMap.get(entry.getKey());
//                // 本地未读设为已读
//                article.setReadStatus(App.STATUS_READED);
//                changedArticles.add(article);
//            }
//        }
//
//        WithDB.i().saveArticles(changedArticles);
//        return tempUnreadIds;
//    }
//
//    private HashSet<String> handleStaredRefs(List<Integer> streamIds) {
//        List<Article> localStarredArticles = WithDB.i().getArtsStared();
//        Map<String, Article> localStarredArticlesMap = new ArrayMap<>(localStarredArticles.size());
//        List<Article> changedArticles = new ArrayList<>();
//        HashSet<String> tempStarredIds = new HashSet<>(streamIds.size());
//
//        // 第1步，遍历数据量大的一方A，将其比对项目放入Map中
//        for (Article article : localStarredArticles) {
//            localStarredArticlesMap.put(article.getId(), article);
//        }
//
//        // 第2步，遍历数据量小的一方B。到Map中找，是否含有b中的比对项。有则XX，无则YY
//        Article article;
//        for (Integer articleId : streamIds) {
//            article = localStarredArticlesMap.get(articleId+"");
//            if (article != null) {
//                localStarredArticlesMap.remove(articleId+"");
//            } else {
//                article = WithDB.i().getArticle(articleId+"");
//                if (article != null) {
//                    article.setStarStatus(App.STATUS_STARED);
//                    changedArticles.add(article);
//                } else {
//                    // 本地无，而云远端有，加入要请求的未读资源
//                    tempStarredIds.add(articleId+"");
//                }
//            }
//        }
//
//        for (Map.Entry<String, Article> entry : localStarredArticlesMap.entrySet()) {
//            if (entry.getKey() != null) {
//                article = localStarredArticlesMap.get(entry.getKey());
//                article.setStarStatus(App.STATUS_UNSTAR);
//                changedArticles.add(article);// 取消加星
//            }
//        }
//
//        WithDB.i().saveArticles(changedArticles);
//        return tempStarredIds;
//    }
//
//    /**
//     * 将 未读资源 和 加星资源，去重分为3组
//     *
//     * @param tempUnreadIds
//     * @param tempStarredIds
//     * @return
//     */
//    private ArrayList<HashSet<String>> splitRefs(HashSet<String> tempUnreadIds, HashSet<String> tempStarredIds) {
////        KLog.e("【reRefs1】云端未读" + tempUnreadIds.size() + "，云端加星" + tempStarredIds.size());
//        int total = tempUnreadIds.size() > tempStarredIds.size() ? tempStarredIds.size() : tempUnreadIds.size();
//
//        HashSet<String> reUnreadUnstarRefs;
//        HashSet<String> reReadStarredRefs = new HashSet<>(tempStarredIds.size());
//        HashSet<String> reUnreadStarredRefs = new HashSet<>(total);
//
//        for (String id : tempStarredIds) {
//            if (tempUnreadIds.contains(id)) {
//                tempUnreadIds.remove(id);
//                reUnreadStarredRefs.add(id);
//            } else {
//                reReadStarredRefs.add(id);
//            }
//        }
//        reUnreadUnstarRefs = tempUnreadIds;
//
//        ArrayList<HashSet<String>> refsList = new ArrayList<>();
//        refsList.add(reUnreadUnstarRefs);
//        refsList.add(reReadStarredRefs);
//        refsList.add(reUnreadStarredRefs);
////        KLog.e("【reRefs2】" + reUnreadUnstarRefs.size() + "--" + reReadStarredRefs.size() + "--" + reUnreadStarredRefs.size());
//        return refsList;
//    }
//
//    private ArrayMap<String, ArrayList<Article>> classArticlesByFeedId(ArrayMap<String, ArrayList<Article>> feedIds, ArrayList<Article> articles) {
//        if (null == feedIds) {
//            return new ArrayMap<String, ArrayList<Article>>();
//        }
//        if (articles != null) {
//            ArrayList<Article> arrayList;
//            for (Article article : articles) {
//                arrayList = feedIds.get(article.getFeedId());
//                if (null == arrayList) {
//                    arrayList = new ArrayList<Article>();
//                    arrayList.add(article);
//                    feedIds.put(article.getFeedId(), arrayList);
//                } else {
//                    arrayList.add(article);
//                }
//            }
//        }
//        return feedIds;
//    }
//
//    private void fetchReadability(ArrayMap<String, ArrayList<Article>> feedIds) {
//        if (null == feedIds) {
//            return;
//        }
//        KLog.e("易读，获取到的订阅源：" + feedIds.size());
//
//        ArrayMap<String, ArrayList<Article>> needReadabilityFeedIds = new ArrayMap<String, ArrayList<Article>>();
//        for (Map.Entry<String, ArrayList<Article>> entry : feedIds.entrySet()) {
//            //KLog.e("需要获取易读，Key：" + entry.getKey() + " , "  +  GlobalConfig.i().getDisplayMode(entry.getKey()) );
//            if (App.DISPLAY_READABILITY.equals(GlobalConfig.i().getDisplayMode(entry.getKey()))) {
//                needReadabilityFeedIds.put(entry.getKey(), entry.getValue());
//            }
//        }
//
//        KLog.e("易读，需要获取的数量：" + needReadabilityFeedIds.entrySet().size());
//        for (Map.Entry<String, ArrayList<Article>> entry : needReadabilityFeedIds.entrySet()) {
//            for (final Article article : entry.getValue()) {
//                //KLog.e("====获取：" + entry.getKey() + " , " + article.getTitle() + " , " + article.getLink());
//                if (TextUtils.isEmpty(article.getLink())) {
//                    return;
//                }
//                //KLog.e("====开始请求" );
//                Request request = new Request.Builder().url(article.getLink()).build();
//                Call call = HttpClientManager.i().simpleClient().newCall(request);
//                call.enqueue(new Callback() {
//                    @Override
//                    public void onFailure(Call call, IOException e) {
//                        KLog.e("获取失败");
//                    }
//
//                    @Override
//                    public void onResponse(Call call, okhttp3.Response response) throws IOException {
//                        if (!response.isSuccessful()) {
//                            return;
//                        }
//                        //KLog.e("已保存易读");
//                        Document doc = Jsoup.parse(response.body().byteStream(), DataUtil.getCharsetFromContentType(response.body().contentType().toString()), article.getLink());
//                        String content = Extractor.getData(article.getLink(), doc);
//                        if (TextUtils.isEmpty(content)) {
//                            return;
//                        }
//                        content = StringUtil.getOptimizedContent(article.getLink(), content);
//                        //KLog.e("易读：" + content );
//                        article.setData(content);
//                        String summary = StringUtil.getOptimizedSummary(content);
//                        article.setSummary(summary);
//
//                        // 获取第1个图片作为封面
//                        Elements elements = Jsoup.parseBodyFragment(content).getElementsByTag("img");
//                        if (elements.size() > 0) {
//                            String src = elements.get(0).attr("abs:src");
//                            // article.setEnclosure(src);
//                            article.setImage(src);
//                        }
//                        WithDB.i().updateArticle(article);
//                    }
//                });
//            }
//        }
//    }
//
//
//}
