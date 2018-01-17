package me.wizos.loread.net;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.exception.HttpException;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.socks.library.KLog;
import com.tencent.bugly.crashreport.BuglyLog;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import me.wizos.loread.App;
import me.wizos.loread.bean.Article;
import me.wizos.loread.bean.Feed;
import me.wizos.loread.bean.Img;
import me.wizos.loread.bean.Tag;
import me.wizos.loread.bean.gson.GsSubscriptions;
import me.wizos.loread.bean.gson.GsTags;
import me.wizos.loread.bean.gson.ItemIDs;
import me.wizos.loread.bean.gson.StreamContents;
import me.wizos.loread.bean.gson.StreamPref;
import me.wizos.loread.bean.gson.StreamPrefs;
import me.wizos.loread.bean.gson.Subscriptions;
import me.wizos.loread.bean.gson.UserInfo;
import me.wizos.loread.bean.gson.itemContents.Items;
import me.wizos.loread.bean.gson.son.ItemRefs;
import me.wizos.loread.data.WithDB;
import me.wizos.loread.data.WithSet;
import me.wizos.loread.utils.FileUtil;
import me.wizos.loread.utils.HttpUtil;
import me.wizos.loread.utils.StringUtil;
import okhttp3.OkHttpClient;

/**
 * 该类处于 获取 -> 处理 -> 输出 数据的处理一层。主要任务是处理、保存业务数据。
 * Created by Wizos on 2017/10/13.
 */

public class DataApi {
    private static DataApi dataApi;

    private DataApi() {
    }

    public static DataApi i() {
        if (dataApi == null) {
            synchronized (DataApi.class) {
                if (dataApi == null) {
                    dataApi = new DataApi();
                    cb = new NetCallbackS() {
                        @Override
                        public void onSuccess(String body) {
                            super.onSuccess(body);
                        }

                        @Override
                        public void onFailure(Request request) {
                            super.onFailure(request);
                        }
                    };
                }
            }
        }
        return dataApi;
    }

    private static NetCallbackS cb;


    public String fetchBootstrap() throws HttpException, IOException {
        return InoApi.i().syncBootstrap(cb);
    }

    public boolean clientLogin(String accountId, String accountPd) throws HttpException, IOException {
        String info = InoApi.i().clientLogin(accountId, accountPd, cb);
        String auth = info.split("Auth=")[1].replaceAll("\n", "");
        if (TextUtils.isEmpty(auth)) {
            return false;
        }
        InoApi.INOREADER_ATUH = "GoogleLogin auth=" + auth;
        WithSet.i().setAuth(InoApi.INOREADER_ATUH);
        InoApi.i().init();
        return true;
    }

    public long fetchUserInfo() throws HttpException, IOException {
        String info = InoApi.i().fetchUserInfo(cb);
        UserInfo userInfo = new Gson().fromJson(info, UserInfo.class);
        KLog.e("此时的UID为" + userInfo.getUserId());
        WithSet.i().setUseId(userInfo.getUserId());
        return userInfo.getUserId();
//        UserID = userInfo.getUserId();
//        mUserName = userInfo.getUserName();
//        mUserProfileId = userInfo.getUserProfileId();
//        mUserEmail = userInfo.getUserEmail();
//        mIsBloggerUser = userInfo.getIsBloggerUser();
//        mSignupTimeSec = userInfo.getSignupTimeSec();
//        mIsMultiLoginEnabled = userInfo.getIsMultiLoginEnabled();
//        save("UserID" , UserID);
//        save("mUserName" , mUserName);
//        save("mUserEmail" , mUserEmail);
//        App.i().finishActivity(LoginActivity.this);
//        goTo(MainActivity.TAG,"syncAll");
    }


    public List<Tag> fetchTagList() throws HttpException, IOException {
        String info = InoApi.i().syncTagList(cb);
        String[] array;
        String tagTitle;
        List<Tag> tagListTemp = new Gson().fromJson(info, GsTags.class).getTags();
        tagListTemp.remove(0); // /state/com.google/starred
        tagListTemp.remove(0); // /state/com.google/broadcast
        tagListTemp.remove(0); // /state/com.google/blogger-following
        for (Tag tag : tagListTemp) {
            array = tag.getId().split("/");
            tagTitle = array[array.length - 1];
            tag.setTitle(tagTitle);
        }
        return tagListTemp;
    }

//    private List<Tag> getTagList( List<Tag> tagListTemp ) {
//        Tag rootTag = new Tag();
//        Tag notTag = new Tag();
//        long userID = WithSet.i().getUseId();
//        rootTag.setTitle("所有文章");
//        notTag.setTitle("未分类");
//
//        rootTag.setId("\"user/" + userID + Api.U_READING_LIST + "\"");
//        rootTag.setSortid("00000000");
//        rootTag.__setDaoSession(App.i().getDaoSession());
//
//        notTag.setId("\"user/" + userID + Api.U_NO_LABEL + "\"");
//        notTag.setSortid("00000001");
//        notTag.__setDaoSession(App.i().getDaoSession());
//
//        List<Tag> tagList = new ArrayList<>();
//        tagList.add(rootTag);
//        tagList.add(notTag);
//
//        if( tagListTemp != null){
//            tagList.addAll(tagListTemp);
//        }
//
//        KLog.d("【listTag】 " + rootTag.toString());
//        return tagList;
//    }

    public List<Feed> fetchFeedList() throws HttpException, IOException {
        String info = InoApi.i().syncSubList(cb);
        KLog.i("解析parseSubscriptionList");
        List<Subscriptions> subscriptionses = new Gson().fromJson(info, GsSubscriptions.class).getSubscriptions();
        List<Feed> feedList = new ArrayList<>(subscriptionses.size());
        Feed feed;
        for (Subscriptions subscriptions : subscriptionses) {
            feed = new Feed();
            feed.setId(subscriptions.getId());
            feed.setTitle(subscriptions.getTitle());
            try {
                feed.setCategoryid(subscriptions.getCategories().get(0).getId());
                feed.setCategorylabel(subscriptions.getCategories().get(0).getLabel());
            } catch (Exception e) {
                feed.setCategoryid("user/" + WithSet.i().getUseId() + Api.U_NO_LABEL);
                feed.setCategorylabel("no-label"); // TODO: 2018/1/11 待改成引用
            }
            feed.setSortid(subscriptions.getSortid());
            feed.setFirstitemmsec(subscriptions.getFirstitemmsec());
            feed.setUrl(subscriptions.getUrl());
            feed.setHtmlurl(subscriptions.getHtmlUrl());
            feed.setIconurl(subscriptions.getIconUrl());
            feedList.add(feed);
        }
        // TODO: 2017/10/14 少了保存 List
        return feedList;
    }


    /**
     * 获取到排序规则
     *
     * @param
     * @throws HttpException
     * @throws IOException
     */
    public List<Tag> fetchStreamPrefs(List<Tag> tagList) throws HttpException, IOException {
        String info = InoApi.i().syncStreamPrefs(cb);
//        if (tagList.size() == 0){return;
//        }
        StreamPrefs streamPrefs = new Gson().fromJson(info, StreamPrefs.class);
        Map<String, Tag> tagMap = new ArrayMap<>();
        for (Tag tag : tagList) {
            tagMap.put(tag.getSortid(), tag);
        }
        KLog.e("此时的UID为" + App.UserID);
        // 由 tags 的排序字符串，生成一个新的 reTags
        ArrayList<StreamPref> preferences = streamPrefs.getStreamPrefsMaps().get("user/" + App.UserID + "/state/com.google/root");
        try {
            ArrayList<String> mTagsOrderArray = getOrderArray(preferences.get(0).getValue());
            Tag tempTag;
            tagList = new ArrayList<>();
            for (String sortID : mTagsOrderArray) { // 由于获取到的排序字符串中，可能会包含feed的排序id，造成从tagMap获取不到对应的tag的现象。
                tempTag = tagMap.get(sortID);
                if (tempTag != null) {
                    tagList.add(tempTag);
//                KLog.e("tag的title" + tempTag.getTitle() + tempTag.getId() );
                }
            }
            for (int i = 0; i < mTagsOrderArray.size(); i++) {
                tempTag = tagMap.get(mTagsOrderArray.get(i));
                tempTag.setSortid(String.valueOf(i));
                tagList.add(tempTag);
//                KLog.e("tag的title" + tempTag.getTitle() + tempTag.getId() );
            }
        } catch (Throwable thr) {
            BuglyLog.e("DataApi", "user/" + App.UserID + "/state/com.google/root");
            CrashReport.postCatchedException(thr);  // bugly会将这个throwable上报
            orderTags(tagList);
        }

        return tagList;
    }

    public List<Tag> orderTags(List<Tag> tagList) {
        KLog.i("解析orderTags");
        if (tagList.size() == 0) {
            return tagList;
        }

        Collections.sort(tagList, new Comparator<Tag>() {
            public int compare(Tag o1, Tag o2) {
                return o1.getTitle().compareTo(o2.getTitle());
            }
        });
        for (int i = 0; i < tagList.size(); i++) {
            tagList.get(i).setSortid(String.valueOf(i));
        }

        return tagList;
    }

    /**
     * 将记录排序规则的string转为array
     *
     * @param orderingString 记录排序规则的字符串
     * @return 记录排序规则的数组
     */
    private ArrayList<String> getOrderArray(String orderingString) {
        int num = orderingString.length() / 8;
        ArrayList<String> orderingArray = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            orderingArray.add(orderingString.substring(i * 8, (i * 8) + 8));
        }
        return orderingArray;
    }

//    public void fetchUnreadCounts() throws HttpException,IOException{
//        String info = InoApi.i().syncUnreadCounts(cb);
//        List<UnreadCounts> unreadCountList = new Gson().fromJson(info, GsUnreadCount.class).getUnreadcounts();
//        // TODO: 2017/10/15 将未读信息保存到 Tag 和 Feed 中
//    }

    public List<ItemRefs> fetchUnreadRefs() throws HttpException, IOException {
        ItemIDs itemIDs = new ItemIDs();
        do {
            String info = InoApi.i().syncUnReadRefs(itemIDs.getContinuation(), cb);
            ItemIDs tempItemIDs = new Gson().fromJson(info, ItemIDs.class);
            itemIDs.addItemRefs(tempItemIDs.getItemRefs());
            itemIDs.setContinuation(tempItemIDs.getContinuation());
        } while (itemIDs.getContinuation() != null);


        List<Article> localUnreadArticles = WithDB.i().getArtsUnread();
        List<Article> changedArticles = new ArrayList<>(); // // TODO: 2017/10/15 待保存
        Map<String, Article> map = new ArrayMap<>(localUnreadArticles.size());
        ArrayList<ItemRefs> tempUnreadRefs = new ArrayList<>(itemIDs.getItemRefs().size());// 筛选下来，最终要去云端获取内容的未读Refs的集合

        // 数据量大的一方
        String articleId;
        for (Article article : localUnreadArticles) {
            articleId = article.getId();
            map.put(articleId, article);
            KLog.e("文章的" + article.getId());
        }
        // 数据量小的一方
        Article article;
        for (ItemRefs item : itemIDs.getItemRefs()) {
            articleId = item.getLongId();
            article = map.get(articleId);
//            KLog.e("获取到的" + item.getId() + "   " + item.getLongId() );
            if (article != null) {
                map.remove(articleId);
//                KLog.e("本地有" );
            } else {
                article = WithDB.i().getArticle(articleId);
                if (article != null && article.getReadState().equals(Api.ART_READED)) {
//                    KLog.e("本地有B" );
                    article.setReadState(Api.ART_UNREAD);
                    changedArticles.add(article);
                } else {
//                    KLog.e("本地无" );
                    tempUnreadRefs.add(item);// 本地无，而云远端有，加入要请求的未读资源
                }
            }
        }
        for (Map.Entry<String, Article> entry : map.entrySet()) {
//            KLog.e("最终" + entry.getKey() );
            if (entry.getKey() != null) {
                article = map.get(entry.getKey());
                article.setReadState(Api.ART_READED); // 本地未读设为已读
                changedArticles.add(article);
            }
        }

        WithDB.i().saveArticleList(changedArticles);
        return tempUnreadRefs;
    }


    public List<ItemRefs> fetchStaredRefs() throws HttpException, IOException {
        ItemIDs itemIDs = new ItemIDs();
        do {
            String info = InoApi.i().syncStarredRefs(itemIDs.getContinuation(), cb);
            ItemIDs tempItemIDs = new Gson().fromJson(info, ItemIDs.class);
            itemIDs.addItemRefs(tempItemIDs.getItemRefs());
            itemIDs.setContinuation(tempItemIDs.getContinuation());
        } while (itemIDs.getContinuation() != null);

        List<Article> localStarredArticles = WithDB.i().getArtsStared();
        List<Article> changedArticles = new ArrayList<>(); // // TODO: 2017/10/15 待保存
        Map<String, Article> map = new ArrayMap<>(localStarredArticles.size());
        ArrayList<ItemRefs> tempStarredRefs = new ArrayList<>(itemIDs.getItemRefs().size());


        String articleId;
        // 第1步，遍历数据量大的一方A，将其比对项目放入Map中，计数为1
        for (Article article : localStarredArticles) {
            articleId = article.getId(); // String
            map.put(articleId, article);
        }
        // 第2步，遍历数据量小的一方B。到Map中找，是否含有b中的比对项。有则XX，无则YY
        Article article;
        for (ItemRefs item : itemIDs.getItemRefs()) {
//            articleId = StringUtil.toLongID(item.getLongId());
            articleId = item.getLongId();
            article = map.get(articleId);
            if (article != null) {
                map.remove(articleId);
            } else {
                article = WithDB.i().getArticle(articleId);
                if (article != null) {
                    article.setStarState(Api.ART_STARED);
                    changedArticles.add(article);
                } else {
                    tempStarredRefs.add(item);// 本地无，而云远端有，加入要请求的未读资源
                }
//                starredRefs.add(item);// 3，就剩云端的，要请求的加星资源（但是还是含有一些要请求的未读资源，和一些本地是已读的文章）
            }
        }

        for (Map.Entry<String, Article> entry : map.entrySet()) {
            if (entry.getKey() != null) {
                article = map.get(entry.getKey());
                article.setStarState(Api.ART_UNSTAR);
                changedArticles.add(article);// 取消加星
            }
        }

        WithDB.i().saveArticleList(changedArticles);
        return tempStarredRefs;
    }

    public ArrayList<List<ItemRefs>> splitRefs(List<ItemRefs> tempUnreadRefs, List<ItemRefs> tempStarredRefs) {
        KLog.e("【reRefs1】云端未读" + tempUnreadRefs.size() + "，云端加星" + tempStarredRefs.size());
        int arrayCapacity = tempUnreadRefs.size() > tempStarredRefs.size() ? tempStarredRefs.size() : tempUnreadRefs.size();

        ArrayList<ItemRefs> reUnreadUnstarRefs = new ArrayList<>(tempUnreadRefs.size());
        ArrayList<ItemRefs> reReadStarredRefs = new ArrayList<>(tempStarredRefs.size());
        ArrayList<ItemRefs> reUnreadStarredRefs = new ArrayList<>(arrayCapacity);

        Map<String, ItemRefs> mapArray = new ArrayMap<>(tempUnreadRefs.size());
        for (ItemRefs item : tempUnreadRefs) {
            mapArray.put(item.getId(), item);
        }
        ItemRefs tempItem;
        for (ItemRefs item : tempStarredRefs) {
            tempItem = mapArray.get(item.getId());
            if (tempItem != null) {
                mapArray.remove(item.getId());
                reUnreadStarredRefs.add(item);
            } else {
                reReadStarredRefs.add(item);
            }
        }
        for (Map.Entry<String, ItemRefs> entry : mapArray.entrySet()) {
            if (entry.getKey() != null) {
                reUnreadUnstarRefs.add(mapArray.get(entry.getKey()));
            }
        }
        ArrayList<List<ItemRefs>> refsList = new ArrayList<>(reUnreadUnstarRefs.size() + reReadStarredRefs.size() + reUnreadStarredRefs.size());
        refsList.add(reUnreadUnstarRefs);
        refsList.add(reReadStarredRefs);
        refsList.add(reUnreadStarredRefs);
        KLog.e("【reRefs2】" + reUnreadUnstarRefs.size() + "--" + reReadStarredRefs.size() + "--" + reUnreadStarredRefs.size());
        return refsList;
    }


    public ArrayList<Article> fetchContentsUnreadUnstar(List<String> ids) throws HttpException, IOException {
        return fetchContent(ids, new ArticleChanger() {
            @Override
            public Article change(Article article) {
                article.setReadState(Api.ART_UNREAD);
                article.setStarState(Api.ART_UNSTAR);
                return article;
            }
        }, cb);
    }

    public ArrayList<Article> fetchContentsUnreadStarred(List<String> ids) throws HttpException, IOException {
        return fetchContent(ids, new ArticleChanger() {
            @Override
            public Article change(Article article) {
                article.setReadState(Api.ART_UNREAD);
                article.setStarState(Api.ART_STARED);
                return article;
            }
        }, cb);
    }

    public ArrayList<Article> fetchContentsReadStarred(List<String> ids) throws HttpException, IOException {
        return fetchContent(ids, new ArticleChanger() {
            @Override
            public Article change(Article article) {
                article.setReadState(Api.ART_READED);
                article.setStarState(Api.ART_STARED);
                return article;
            }
        }, cb);
    }

    private ArrayList<Article> fetchContent(List<String> ids, ArticleChanger changer, NetCallbackS cb) throws HttpException, IOException {
        ArrayList<Article> articleList = new ArrayList<>(ids.size());

        int distance = ids.size();
        int index = 0;
        int currentFetchCnt = 0;
        List<String> tempIds;

        while (distance > 0) {
            currentFetchCnt = Math.min(distance, InoApi.i().FETCH_CONTENT_EACH_CNT);
            tempIds = ids.subList(index, index + currentFetchCnt);
            index = index + currentFetchCnt;
            distance = ids.size() - index;
            fetchNotice.onProcessChanged(currentFetchCnt, ids.size());  // TEST:
            String res = InoApi.i().syncItemContents(tempIds, cb);
            ArrayList<Article> tempArticleList = parseItemContents(res, changer);
            articleList.addAll(tempArticleList);
        }
//        if(articleList.size()!= ids.size() ){
//            ToastUtil.showLong("获取到的文章有遗漏" + ids.size() + "=" +  articleList.size() );
//        }
        return articleList;
    }

    public void fetchAllStaredStreamContent() throws HttpException, IOException {
        ArrayList<Items> itemses = new ArrayList<>();
        String continuation = null;
        StreamContents streamContents;
        do {
            fetchNotice.onProcessChanged(InoApi.i().FETCH_CONTENT_EACH_CNT, 0);
            String res = InoApi.i().syncStaredStreamContents(continuation, cb);
            streamContents = new Gson().fromJson(res, StreamContents.class);
            itemses.addAll(streamContents.getItems());
            continuation = streamContents.getContinuation();
        } while (streamContents.getContinuation() != null);

        ArrayList<Article> tempArticleList = new ArrayList<>(itemses.size());
        String summary = "", html = "";
        Article article;
        Gson gson = new Gson();
        for (Items items : itemses) {
//            KLog.e("文章标题："+ items.getTitle() );
            if (WithDB.i().getArticleEchoes(items.getTitle(), items.getCanonical().get(0).getHref()) != 0) {
//                KLog.showLong("有重复的文章");
                continue;
            }

            article = new Article();
            // 返回的字段
            article.setId(items.getId());
            article.setCrawlTimeMsec(items.getCrawlTimeMsec());
            article.setTimestampUsec(items.getTimestampUsec());
            article.setCategories(gson.toJson(items.getCategories()));
//            article.setTitle(items.getTitle().replace(File.separator, "-").replace("\r", "").replace("\n", ""));
            article.setTitle(items.getTitle().replace("\r", "").replace("\n", ""));

            article.setPublished(items.getPublished());
            article.setUpdated(items.getUpdated());
            article.setStarred(items.getStarred());// 设置被加星的时间
            article.setCanonical(items.getCanonical().get(0).getHref()); // items.getCanonical().get(0).getHref()
            article.setAlternate(gson.toJson(items.getCanonical())); // items.getAlternate().toString()
            article.setAuthor(items.getAuthor());
            article.setOriginStreamId(items.getOrigin().getStreamId());
            article.setOriginHtmlUrl(items.getOrigin().getHtmlUrl());
            article.setOriginTitle(items.getOrigin().getTitle());

            html = items.getSummary().getContent();
            summary = StringUtil.getOptimizedSummary(html);
//            summary = StringUtil.delHtmlTag(html);
            article.setSummary(summary);

            // 自己设置的字段
            article.setSaveDir(Api.SAVE_DIR_CACHE);
//            KLog.i("【增加文章】" + article.getId());
            article.setReadState(Api.ART_READED);
            article.setStarState(Api.ART_STARED);
            tempArticleList.add(article);
            FileUtil.saveCacheHtml(StringUtil.stringToMD5(article.getId()), html);
        }
        WithDB.i().saveArticleList(tempArticleList);
    }

    private interface ArticleChanger {
        Article change(Article article);
    }

    private FetchNotice fetchNotice;

    public interface FetchNotice {
        void onProcessChanged(int had, int count);
    }

    public void regFetchNotice(FetchNotice fetchNotice) {
        this.fetchNotice = fetchNotice;
    }


    /**
     * 这里有两种方法来实现了函数 A B C 共用一个主函数 X ，但各自在主函数中的某些语句又不同
     * 1.是采用分割主函数为多个函数 X[]，再在要在具体的函数 A B C 内拼接调用 X[]。
     * 2.（目前）是采用接口类作为主函数 X 的参数传递，在调用具体的函数 A B C 时，将各自要不同的语句在该接口内实现
     * 之前的代码是函数 A B C 都各自再写一遍共用函数
     * 使用接口类作为参数传递，实际上是让调用者来实现具体语句
     *
     * @param info           获得的响应体
     * @param articleChanger 回调，用于修改 Article 对象
     */
    private ArrayList<Article> parseItemContents(String info, ArticleChanger articleChanger) {
        // 如果返回 null 会与正常获取到流末端时返回 continuation = null 相同，导致调用该函数的那端误以为是正常的 continuation = null
        if (info == null || info.equals("")) {
            return null;
        }
        Gson gson = new Gson();
        StreamContents gsItemContents = gson.fromJson(info, StreamContents.class);
        ArrayList<Items> tempItemsList = gsItemContents.getItems();
        ArrayList<Article> tempArticleList = new ArrayList<>(tempItemsList.size());
        String summary = "", html = "";
        Article article;
        for (Items items : tempItemsList) {
//            KLog.e("文章标题："+ items.getTitle() );
            if (WithDB.i().getArticleEchoes(items.getTitle(), items.getCanonical().get(0).getHref()) != 0) {
                KLog.e("有重复的文章");
                continue;
            }

            article = new Article();
            // 返回的字段
            article.setId(items.getId());
            article.setCrawlTimeMsec(items.getCrawlTimeMsec());
            article.setTimestampUsec(items.getTimestampUsec());
            article.setCategories(gson.toJson(items.getCategories()));
            article.setTitle(items.getTitle().replace(File.separator, "-").replace("\r", "").replace("\n", ""));
            article.setPublished(items.getPublished());
            article.setUpdated(items.getUpdated());
            article.setStarred(items.getStarred());// 设置被加星的时间
            article.setCanonical(items.getCanonical().get(0).getHref()); // items.getCanonical().get(0).getHref()
            article.setAlternate(gson.toJson(items.getCanonical())); // items.getAlternate().toString()
            article.setAuthor(items.getAuthor());
            article.setOriginStreamId(items.getOrigin().getStreamId());
            article.setOriginHtmlUrl(items.getOrigin().getHtmlUrl());
            article.setOriginTitle(items.getOrigin().getTitle());

            html = items.getSummary().getContent();
            summary = StringUtil.getOptimizedSummary(html);
//            summary = StringUtil.delHtmlTag(html);
            article.setSummary(summary);

            // 自己设置的字段
            article.setSaveDir(Api.SAVE_DIR_CACHE);
//            KLog.i("【增加文章】" + article.getId());
            article = articleChanger.change(article);
            tempArticleList.add(article);
            FileUtil.saveCacheHtml(StringUtil.stringToMD5(article.getId()), html);
        }
        WithDB.i().saveArticleList(tempArticleList);
        return tempArticleList;
    }


    public void articleRemoveTag(String articleID, String tagId, StringCallback cb) {
        InoApi.i().articleRemoveTag(articleID, tagId, cb);
    }

    public void articleAddTag(String articleID, String tagId, StringCallback cb) {
        InoApi.i().articleAddTag(articleID, tagId, cb);
    }

    public void renameTag(String sourceTagId, String destTagId, StringCallback cb) {
        InoApi.i().renameTag(sourceTagId, destTagId, cb);
    }


    public void addFeed(String feedId, StringCallback cb) {
        // /reader/api/0/subscription/quickadd
        InoApi.i().addFeed(feedId, cb);
    }

    public void renameFeed(String feedId, String renamedTitle, StringCallback cb) {
        InoApi.i().renameFeed(feedId, renamedTitle, cb);
    }

    public void unsubscribeFeed(String feedId, StringCallback cb) {
        InoApi.i().unsubscribeFeed(feedId, cb);
    }

    public void markArticleListReaded(List<String> articleIDs, StringCallback cb) {
        InoApi.i().markArticleListReaded(articleIDs, cb);
    }

    public void markArticleReaded(String articleID, StringCallback cb) {
        InoApi.i().markArticleReaded(articleID, cb);
    }

    public void markArticleUnread(String articleID, StringCallback cb) {
        InoApi.i().markArticleUnread(articleID, cb);
    }

    public void markArticleUnstar(String articleID, StringCallback cb) {
        InoApi.i().markArticleUnstar(articleID, cb);
    }

    public void markArticleStared(String articleID, StringCallback cb) {
        InoApi.i().markArticleStared(articleID, cb);
    }


    public void downImgs(Handler handler, OkHttpClient imgHttpClient, List<Img> imgList, String parentPath) {
        if ((WithSet.i().isDownImgWifi() && !HttpUtil.isWiFiActive()) ||
                (!WithSet.i().isDownImgWifi() && !HttpUtil.isNetworkAvailable())) {
            return;
        }
        for (Img img : imgList) {
            downingImg(handler, imgHttpClient, img, parentPath);
        }
        KLog.e("批量下图片：" + imgList.size());
    }

    public void downingImg(final Handler handler, OkHttpClient imgHttpClient, final Img img, String parentPath) {
        FileCallback fileCallback = new FileCallback(parentPath, img.getName()) {
            @Override
            public void onSuccess(Response<File> response) {
                img.setDownState(Api.ImgMeta_Downover);
                WithDB.i().saveImg(img);
                makeMsgForImg(handler, img, Api.S_BITMAP);
            }

            // 该方法执行在主线程中
            @Override
            public void onError(Response<File> response) {
                makeMsgForImg(handler, img, Api.F_BITMAP);
            }
        };

        WithHttp.i().asyncGetImg(imgHttpClient, img, fileCallback);
    }

    private void makeMsgForImg(Handler handler, Img img, int msg) {
        Message message = Message.obtain();
        Bundle bundle = new Bundle();
        bundle.putString("articleID", img.getArticleId());
        bundle.putString("imgSrc", img.getSrc());
        bundle.putString("imgName", img.getName());
//        bundle.putInt("imgNo", img.getNo());
        message.what = msg;
        message.setData(bundle);
        handler.sendMessage(message);
        KLog.e("【】下载图片" + handler + msg + " = " + img.getArticleId() + "==" + img.getSrc() + "下载状态：" + img.getDownState() + ", 当前线程为：" + Thread.currentThread().getName());
    }


    /**
     * 将一个list均分成n个list,主要通过偏移量来实现的
     *
     * @param source
     * @return
     */
    public static <T> List<List<T>> averageAssign(List<T> source, int n) {
        List<List<T>> result = new ArrayList<List<T>>();
        int remaider = source.size() % n;  //(先计算出余数)
        int number = source.size() / n;  //然后是商
        int offset = 0;//偏移量
        for (int i = 0; i < n; i++) {
            List<T> value = null;
            if (remaider > 0) {
                value = source.subList(i * number + offset, (i + 1) * number + offset + 1);
                remaider--;
                offset++;
            } else {
                value = source.subList(i * number + offset, (i + 1) * number + offset);
            }
            result.add(value);
        }
        return result;
    }


    private <T> void paging(List<T> list, int eachPageSize) {
        int totalCount = list.size();
        int pageCount;

        //分多少次处理
        int requestCount = totalCount / eachPageSize;

        for (int i = 0; i <= requestCount; i++) {
            Integer fromIndex = i * eachPageSize;
            //如果总数少于PAGE_SIZE,为了防止数组越界,toIndex直接使用totalCount即可
            int toIndex = Math.min(totalCount, (i + 1) * eachPageSize);
            List<T> subList = list.subList(fromIndex, toIndex);
            System.out.println(subList);
            //总数不到一页或者刚好等于一页的时候,只需要处理一次就可以退出for循环了
            if (toIndex == totalCount) {
                break;
            }
        }
    }

}
