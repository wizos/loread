package me.wizos.loread.net;

import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.exception.HttpException;
import com.lzy.okgo.request.base.Request;
import com.socks.library.KLog;
import com.tencent.bugly.crashreport.BuglyLog;
import com.tencent.bugly.crashreport.CrashReport;

import org.greenrobot.eventbus.EventBus;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.bean.gson.GsSubscriptions;
import me.wizos.loread.bean.gson.GsTags;
import me.wizos.loread.bean.gson.GsUnreadCount;
import me.wizos.loread.bean.gson.ItemIDs;
import me.wizos.loread.bean.gson.StreamContents;
import me.wizos.loread.bean.gson.StreamPref;
import me.wizos.loread.bean.gson.StreamPrefs;
import me.wizos.loread.bean.gson.Subscriptions;
import me.wizos.loread.bean.gson.UnreadCounts;
import me.wizos.loread.bean.gson.UserInfo;
import me.wizos.loread.bean.gson.itemContents.Items;
import me.wizos.loread.bean.gson.son.ItemRefs;
import me.wizos.loread.data.WithDB;
import me.wizos.loread.data.WithPref;
import me.wizos.loread.db.Article;
import me.wizos.loread.db.Feed;
import me.wizos.loread.db.Tag;
import me.wizos.loread.event.Sync;
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
        return InoApi.i().syncBootstrap();
    }

    public boolean clientLogin(String accountId, String accountPd) throws HttpException, IOException {
        String info = InoApi.i().clientLogin(accountId, accountPd);
        String auth = info.split("Auth=")[1].replaceAll("\n", "");
        if (TextUtils.isEmpty(auth)) {
            return false;
        }
        InoApi.INOREADER_ATUH = "GoogleLogin auth=" + auth;
        WithPref.i().setAuth(InoApi.INOREADER_ATUH);
        InoApi.i().initAuthHeaders();
        return true;
    }

    public void clientLogin2(String accountId, String accountPd, StringCallback cb) {
        InoApi.i().clientLogin2(accountId, accountPd, cb);
    }

    public long fetchUserInfo() throws HttpException, IOException {
        String info = InoApi.i().fetchUserInfo();
        UserInfo userInfo = new Gson().fromJson(info, UserInfo.class);
        KLog.e("此时的UID为" + userInfo.getUserId());
        WithPref.i().setUseId(userInfo.getUserId());
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
    }

    public List<Tag> fetchTagList() throws HttpException, IOException {
        String info = InoApi.i().syncTagList();
        String[] array;
        String tagTitle;
        List<Tag> tagListTemp = new Gson().fromJson(info, GsTags.class).getTags();
        tagListTemp.remove(0); // /state/com.google/starred
        tagListTemp.remove(0); // /state/com.google/broadcast
        tagListTemp.remove(0); // /state/com.google/blogger-following

//        Tag noLabelTag = new Tag();
//        noLabelTag.setTitle(App.i().getString(R.string.main_activity_title_untag));
//        noLabelTag.setId("user/" +  WithPref.i().getUseId() + Api.U_NO_LABEL);
//        noLabelTag.setSortid("00000001");
//        noLabelTag.__setDaoSession(App.i().getDaoSession());
//        tagListTemp.add(0,noLabelTag);

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
//        long userID = WithPref.i().getUseId();
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
        String info = InoApi.i().syncSubList();
        KLog.i("解析SubscriptionList");
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
                feed.setCategoryid("user/" + WithPref.i().getUseId() + Api.U_NO_LABEL);
                feed.setCategorylabel("no-label"); // TODO: 2018/1/11 待改成引用
            }
            feed.setSortid(subscriptions.getSortid());
            feed.setFirstitemmsec(subscriptions.getFirstitemmsec());
            feed.setUrl(subscriptions.getUrl());
            feed.setHtmlurl(subscriptions.getHtmlUrl());
            feed.setIconurl(subscriptions.getIconUrl());
//            feed.setDisplayRouter(Api.DISPLAY_RSS);
            feedList.add(feed);
        }
        // TODO: 2017/10/14 少了保存 List
        return feedList;
    }


    /**
     * 获取到排序规则
     *
     * @param
     * @throws IOException
     */
    public List<Tag> fetchStreamPrefs(List<Tag> tagList) throws IOException {
        String info = InoApi.i().syncStreamPrefs();
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
            @Override
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

    public List<UnreadCounts> fetchUnreadCounts() throws HttpException, IOException {
        String info = InoApi.i().syncUnreadCounts();
        List<UnreadCounts> unreadCountList = new Gson().fromJson(info, GsUnreadCount.class).getUnreadcounts();
        return unreadCountList;
    }

//    public List<ItemRefs> fetchUnreadRefs() throws HttpException, IOException {
//        ItemIDs itemIDs = new ItemIDs();
//        do {
//            String info = InoApi.i().syncUnReadRefs(itemIDs.getContinuation());
//            ItemIDs tempItemIDs = new Gson().fromJson(info, ItemIDs.class);
//            itemIDs.addItemRefs(tempItemIDs.getItemRefs());
//            itemIDs.setContinuation(tempItemIDs.getContinuation());
//        } while (itemIDs.getContinuation() != null);
//
//
//        List<Article> localUnreadArticles = WithDB.i().getArtsUnread();
//        List<Article> changedArticles = new ArrayList<>(); // // TODO: 2017/10/15 待保存
//        Map<String, Article> map = new ArrayMap<>(localUnreadArticles.size());
//        ArrayList<ItemRefs> tempUnreadRefs = new ArrayList<>(itemIDs.getItemRefs().size());// 筛选下来，最终要去云端获取内容的未读Refs的集合
//
//        // 数据量大的一方
//        String articleId;
//        for (Article article : localUnreadArticles) {
//            articleId = article.getId();
//            map.put(articleId, article);
////            KLog.e("文章的" + article.getId());
//        }
//        // 数据量小的一方
//        Article article;
//        for (ItemRefs item : itemIDs.getItemRefs()) {
//            articleId = item.getLongId();
//            article = map.get(articleId);
////            KLog.e("获取到的" + item.getId() + "   " + item.getLongId() );
//            if (article != null) {
//                map.remove(articleId);
////                KLog.e("本地有" );
//            } else {
//                article = WithDB.i().getArticle(articleId);
//                if (article != null && article.getReadState().equals(Api.ART_READED)) {
////                    KLog.e("本地有B" );
//                    article.setReadState(Api.ART_UNREAD);
//                    changedArticles.add(article);
//                } else {
////                    KLog.e("本地无" );
//                    tempUnreadRefs.add(item);// 本地无，而云远端有，加入要请求的未读资源
//                }
//            }
//        }
//        for (Map.Entry<String, Article> entry : map.entrySet()) {
////            KLog.e("最终" + entry.getKey() );
//            if (entry.getKey() != null) {
//                article = map.get(entry.getKey());
//                article.setReadState(Api.ART_READED); // 本地未读设为已读
//                changedArticles.add(article);
//            }
//        }
//
//        WithDB.i().saveArticles(changedArticles);
//        return tempUnreadRefs;
//    }
//    public List<ItemRefs> fetchStaredRefs() throws HttpException, IOException {
//        ItemIDs itemIDs = new ItemIDs();
//        do {
//            String info = InoApi.i().syncStarredRefs(itemIDs.getContinuation());
//            ItemIDs tempItemIDs = new Gson().fromJson(info, ItemIDs.class);
//            itemIDs.addItemRefs(tempItemIDs.getItemRefs());
//            itemIDs.setContinuation(tempItemIDs.getContinuation());
//        } while (itemIDs.getContinuation() != null);
//
//        List<Article> localStarredArticles = WithDB.i().getArtsStared();
//        List<Article> changedArticles = new ArrayList<>(); // // TODO: 2017/10/15 待保存
//        Map<String, Article> map = new ArrayMap<>(localStarredArticles.size());
//        ArrayList<ItemRefs> tempStarredRefs = new ArrayList<>(itemIDs.getItemRefs().size());
//
//
//        String articleId;
//        // 第1步，遍历数据量大的一方A，将其比对项目放入Map中，计数为1
//        for (Article article : localStarredArticles) {
//            articleId = article.getId(); // String
//            map.put(articleId, article);
//        }
//        // 第2步，遍历数据量小的一方B。到Map中找，是否含有b中的比对项。有则XX，无则YY
//        Article article;
//        for (ItemRefs item : itemIDs.getItemRefs()) {
////            articleId = StringUtil.toLongID(item.getLongId());
//            articleId = item.getLongId();
//            article = map.get(articleId);
//            if (article != null) {
//                map.remove(articleId);
//            } else {
//                article = WithDB.i().getArticle(articleId);
//                if (article != null) {
//                    article.setStarState(Api.ART_STARED);
//                    changedArticles.add(article);
//                } else {
//                    tempStarredRefs.add(item);// 本地无，而云远端有，加入要请求的未读资源
//                }
////                starredRefs.add(item);// 3，就剩云端的，要请求的加星资源（但是还是含有一些要请求的未读资源，和一些本地是已读的文章）
//            }
//        }
//
//        for (Map.Entry<String, Article> entry : map.entrySet()) {
//            if (entry.getKey() != null) {
//                article = map.get(entry.getKey());
//                article.setStarState(Api.ART_UNSTAR);
//                changedArticles.add(article);// 取消加星
//            }
//        }
//
//        WithDB.i().saveArticles(changedArticles);
//        return tempStarredRefs;
//    }

    public HashSet<String> fetchUnreadRefs2() throws HttpException, IOException {
        List<ItemRefs> itemRefs = new ArrayList<>();
        String info;
        ItemIDs tempItemIDs = new ItemIDs();
        do {
            info = InoApi.i().syncUnReadRefs(tempItemIDs.getContinuation());
            tempItemIDs = new Gson().fromJson(info, ItemIDs.class);
            itemRefs.addAll(tempItemIDs.getItemRefs());
        } while (tempItemIDs.getContinuation() != null);

        List<Article> localUnreadArticles = WithDB.i().getArtsUnreadNoOrder();
        Map<String, Article> localUnreadArticlesMap = new ArrayMap<>(localUnreadArticles.size());
        // TODO: 2017/10/15 待保存
        List<Article> changedArticles = new ArrayList<>();
        // 筛选下来，最终要去云端获取内容的未读Refs的集合
        HashSet<String> tempUnreadIds = new HashSet<>(itemRefs.size());
        // 数据量大的一方
        String articleId;
        for (Article article : localUnreadArticles) {
            articleId = article.getId();
            localUnreadArticlesMap.put(articleId, article);
        }
        // 数据量小的一方
        Article article;
        for (ItemRefs item : itemRefs) {
            articleId = item.getLongId();
            article = localUnreadArticlesMap.get(articleId);
            if (article != null) {
                localUnreadArticlesMap.remove(articleId);
            } else {
                article = WithDB.i().getArticle(articleId);
//                if (article != null && article.getReadState().equals(Api.ART_READED)) {
//                    article.setReadState(Api.ART_UNREAD);
                if (article != null && article.getReadStatus() == Api.READED) {
                    article.setReadStatus(Api.UNREAD);
                    changedArticles.add(article);
                } else {
                    // 本地无，而云端有，加入要请求的未读资源
                    tempUnreadIds.add(articleId);
                }
            }
        }
        for (Map.Entry<String, Article> entry : localUnreadArticlesMap.entrySet()) {
            if (entry.getKey() != null) {
                article = localUnreadArticlesMap.get(entry.getKey());
                // 本地未读设为已读
//                article.setReadState(Api.ART_READED);
                article.setReadStatus(Api.READED);
                changedArticles.add(article);
            }
        }

        WithDB.i().saveArticles(changedArticles);
        return tempUnreadIds;
    }


    public HashSet<String> fetchStaredRefs2() throws HttpException, IOException {
        List<ItemRefs> itemRefs = new ArrayList<>();
        String info;
        ItemIDs tempItemIDs = new ItemIDs();
        do {
            info = InoApi.i().syncStarredRefs(tempItemIDs.getContinuation());
            tempItemIDs = new Gson().fromJson(info, ItemIDs.class);
            itemRefs.addAll(tempItemIDs.getItemRefs());
        } while (tempItemIDs.getContinuation() != null);

        List<Article> localStarredArticles = WithDB.i().getArtsStared();
        Map<String, Article> localStarredArticlesMap = new ArrayMap<>(localStarredArticles.size());
        List<Article> changedArticles = new ArrayList<>();
        HashSet<String> tempStarredIds = new HashSet<>(itemRefs.size());

        String articleId;
        // 第1步，遍历数据量大的一方A，将其比对项目放入Map中
        for (Article article : localStarredArticles) {
            articleId = article.getId();
            localStarredArticlesMap.put(articleId, article);
        }

        // 第2步，遍历数据量小的一方B。到Map中找，是否含有b中的比对项。有则XX，无则YY
        Article article;
        for (ItemRefs item : itemRefs) {
            articleId = item.getLongId();
            article = localStarredArticlesMap.get(articleId);
            if (article != null) {
                localStarredArticlesMap.remove(articleId);
            } else {
                article = WithDB.i().getArticle(articleId);
                if (article != null) {
//                    article.setStarState(Api.ART_STARED);
                    article.setStarStatus(Api.STARED);
                    changedArticles.add(article);
                } else {
                    tempStarredIds.add(articleId);// 本地无，而云远端有，加入要请求的未读资源
                }
            }
        }

        for (Map.Entry<String, Article> entry : localStarredArticlesMap.entrySet()) {
            if (entry.getKey() != null) {
                article = localStarredArticlesMap.get(entry.getKey());
//                article.setStarState(Api.ART_UNSTAR);
                article.setStarStatus(Api.UNSTAR);
                changedArticles.add(article);// 取消加星
            }
        }

        WithDB.i().saveArticles(changedArticles);
        return tempStarredIds;
    }

//    public ArrayList<List<ItemRefs>> splitRefs(List<ItemRefs> tempUnreadRefs, List<ItemRefs> tempStarredRefs) {
//        KLog.e("【reRefs1】云端未读" + tempUnreadRefs.size() + "，云端加星" + tempStarredRefs.size());
//        int arrayCapacity = tempUnreadRefs.size() > tempStarredRefs.size() ? tempStarredRefs.size() : tempUnreadRefs.size();
//
//        ArrayList<ItemRefs> reUnreadUnstarRefs = new ArrayList<>(tempUnreadRefs.size());
//        ArrayList<ItemRefs> reReadStarredRefs = new ArrayList<>(tempStarredRefs.size());
//        ArrayList<ItemRefs> reUnreadStarredRefs = new ArrayList<>(arrayCapacity);
//
//        Map<String, ItemRefs> mapArray = new ArrayMap<>(tempUnreadRefs.size());
//        for (ItemRefs item : tempUnreadRefs) {
//            mapArray.put(item.getId(), item);
//        }
//        ItemRefs tempItem;
//        for (ItemRefs item : tempStarredRefs) {
//            tempItem = mapArray.get(item.getId());
//            if (tempItem != null) {
//                mapArray.remove(item.getId());
//                reUnreadStarredRefs.add(item);
//            } else {
//                reReadStarredRefs.add(item);
//            }
//        }
//        for (Map.Entry<String, ItemRefs> entry : mapArray.entrySet()) {
//            if (entry.getKey() != null) {
//                reUnreadUnstarRefs.add(mapArray.get(entry.getKey()));
//            }
//        }
//        ArrayList<List<ItemRefs>> refsList = new ArrayList<>();
//        refsList.add(reUnreadUnstarRefs);
//        refsList.add(reReadStarredRefs);
//        refsList.add(reUnreadStarredRefs);
//        KLog.e("【reRefs2】" + reUnreadUnstarRefs.size() + "--" + reReadStarredRefs.size() + "--" + reUnreadStarredRefs.size());
//        return refsList;
//    }

    public ArrayList<HashSet<String>> splitRefs2(HashSet<String> tempUnreadIds, HashSet<String> tempStarredIds) {
//        KLog.e("【reRefs1】云端未读" + tempUnreadIds.size() + "，云端加星" + tempStarredIds.size());
        int arrayCapacity = tempUnreadIds.size() > tempStarredIds.size() ? tempStarredIds.size() : tempUnreadIds.size();

        HashSet<String> reUnreadUnstarRefs = new HashSet<>(tempUnreadIds.size());
        HashSet<String> reReadStarredRefs = new HashSet<>(tempStarredIds.size());
        HashSet<String> reUnreadStarredRefs = new HashSet<>(arrayCapacity);

        for (String id : tempStarredIds) {
            if (tempUnreadIds.contains(id)) {
                tempUnreadIds.remove(id);
                reUnreadStarredRefs.add(id);
            } else {
                reReadStarredRefs.add(id);
            }
        }
        reUnreadUnstarRefs = tempUnreadIds;

        ArrayList<HashSet<String>> refsList = new ArrayList<>();
        refsList.add(reUnreadUnstarRefs);
        refsList.add(reReadStarredRefs);
        refsList.add(reUnreadStarredRefs);
//        KLog.e("【reRefs2】" + reUnreadUnstarRefs.size() + "--" + reReadStarredRefs.size() + "--" + reUnreadStarredRefs.size());
        return refsList;
    }

    public ArrayList<Article> fetchContentsUnreadUnstar2(List<String> ids) throws HttpException, IOException {
        return parseItemContents2(InoApi.i().syncItemContents(ids), new ArticleChanger() {
            @Override
            public Article change(Article article) {
//                article.setReadState(Api.ART_UNREAD);
//                article.setStarState(Api.ART_UNSTAR);
                article.setReadStatus(Api.UNREAD);
                article.setStarStatus(Api.UNSTAR);
                return article;
            }
        });
    }

    public ArrayList<Article> fetchContentsUnreadStarred2(List<String> ids) throws HttpException, IOException {
        return parseItemContents2(InoApi.i().syncItemContents(ids), new ArticleChanger() {
            @Override
            public Article change(Article article) {
//                article.setReadState(Api.ART_UNREAD);
//                article.setStarState(Api.ART_STARED);
                article.setReadStatus(Api.UNREAD);
                article.setStarStatus(Api.STARED);
                return article;
            }
        });
    }

    public ArrayList<Article> fetchContentsReadStarred2(List<String> ids) throws HttpException, IOException {
        return parseItemContents2(InoApi.i().syncItemContents(ids), new ArticleChanger() {
            @Override
            public Article change(Article article) {
//                article.setReadState(Api.ART_READED);
//                article.setStarState(Api.ART_STARED);
                article.setReadStatus(Api.READED);
                article.setStarStatus(Api.STARED);
                return article;
            }
        });
    }

    public void fetchAllStaredStreamContent2() throws HttpException, IOException {
        String continuation = null;
        StreamContents streamContents;
        int count = 0;
        do {
            EventBus.getDefault().post(new Sync(Sync.DOING, App.i().getString(R.string.main_toolbar_hint_sync_all_stared_content, count)));
            String res = InoApi.i().syncStaredStreamContents(continuation);
            streamContents = new Gson().fromJson(res, StreamContents.class);
            WithDB.i().saveArticles(parseItemContents3(streamContents.getItems(), new ArticleChanger() {
                @Override
                public Article change(Article article) {
//                    article.setReadState(Api.ART_READED);
//                    article.setStarState(Api.ART_STARED);
                    article.setReadStatus(Api.READED);
                    article.setStarStatus(Api.STARED);
                    return article;
                }
            }));
            count = count + streamContents.getItems().size();
            continuation = streamContents.getContinuation();
        } while (streamContents.getContinuation() != null);
    }


    public interface ArticleChanger {
        Article change(Article article);
    }

    public ArrayList<Article> parseItemContents(String info, ArticleChanger articleChanger) {
        // 如果返回 null 会与正常获取到流末端时返回 continuation = null 相同，导致调用该函数的那端误以为是正常的 continuation = null
        if (info == null || info.equals("")) {
            return null;
        }
        Gson gson = new Gson();
        StreamContents gsItemContents = gson.fromJson(info, StreamContents.class);
        return parseItemContents(gsItemContents.getItems(), articleChanger);
    }

    public ArrayList<Article> parseItemContents(List<Items> itemsList, ArticleChanger articleChanger) {
        // 如果返回 null 会与正常获取到流末端时返回 continuation = null 相同，导致调用该函数的那端误以为是正常的 continuation = null
        if (itemsList == null || itemsList.size() <= 0) {
            return null;
        }
        ArrayList<Article> articleList = new ArrayList<>(itemsList.size());
        String summary = "", html = "", audioHtml;
        Article article;
        Gson gson = new Gson();
        Elements elements;
        for (Items items : itemsList) {
//            if (WithDB.i().getArticleEchoes(items.getTitle(), items.getCanonical().get(0).getHref()) != 0) {
//                KLog.e("重复文章：" + items.getTitle() );
//                continue;
//            }
//            KLog.e("文章标题："+ items.getTitle() );

            article = new Article();
            // 返回的字段
            article.setId(items.getId());
            article.setCrawlTimeMsec(items.getCrawlTimeMsec());
            article.setTimestampUsec(items.getTimestampUsec());
            article.setCategories(gson.toJson(items.getCategories()));
            article.setTitle(items.getTitle().replace("\r", "").replace("\n", ""));
            article.setPublished(items.getPublished());
            // 设置被加星的时间
            article.setStarred(items.getStarred());
            // items.getCanonical().get(0).getHref()
            article.setCanonical(items.getCanonical().get(0).getHref());
            // items.getAlternate().toString()
            article.setAlternate(gson.toJson(items.getCanonical()));
            article.setAuthor(items.getAuthor());
            article.setOriginStreamId(items.getOrigin().getStreamId());
            article.setOriginHtmlUrl(items.getOrigin().getHtmlUrl());
            article.setOriginTitle(items.getOrigin().getTitle());

            html = items.getSummary().getContent();
            html = StringUtil.getOptimizedContent(html);

            if (items.getEnclosure() != null && items.getEnclosure().size() > 0 && (items.getEnclosure().get(0).getType().startsWith("audio"))) {
                html = html + "<br><audio src=\"" + items.getEnclosure().get(0).getHref() + "\" preload=\"auto\" type=\"" + items.getEnclosure().get(0).getType() + "\" controls></audio>";
            }

            summary = StringUtil.getOptimizedSummary(html);
            article.setSummary(summary);
            article.setContent(html);

//            if (items.getEnclosure() != null && items.getEnclosure().size() > 0 && (items.getEnclosure().get(0).getType().startsWith("image") || items.getEnclosure().get(0).getType().startsWith("parsedImg"))) {
//                article.setCoverSrc(items.getEnclosure().get(0).getHref());
//            }
            // 获取第1个图片作为封面
            elements = Jsoup.parseBodyFragment(article.getContent() == null ? "" : article.getContent()).getElementsByTag("img");
            if (elements.size() > 0) {
                article.setCoverSrc(elements.attr("src"));
            }


            // 自己设置的字段
//            KLog.i("【增加文章】" + article.getId());
            article.setSaveDir(Api.SAVE_DIR_CACHE);
            article = articleChanger.change(article);
            articleList.add(article);
        }
//        WithDB.i().saveArticles(articleList);
        return articleList;
    }

    private ArrayList<Article> parseItemContents2(String info, ArticleChanger articleChanger) {
        // 如果返回 null 会与正常获取到流末端时返回 continuation = null 相同，导致调用该函数的那端误以为是正常的 continuation = null
        if (info == null || info.equals("")) {
            return null;
        }
        Gson gson = new Gson();
        StreamContents gsItemContents = gson.fromJson(info, StreamContents.class);
        return parseItemContents3(gsItemContents.getItems(), articleChanger);
    }

    private ArrayList<Article> parseItemContents3(ArrayList<Items> itemsList, ArticleChanger articleChanger) {
        // 如果返回 null 会与正常获取到流末端时返回 continuation = null 相同，导致调用该函数的那端误以为是正常的 continuation = null
        if (itemsList == null || itemsList.size() <= 0) {
            return null;
        }
        ArrayList<Article> articleList = new ArrayList<>(itemsList.size());
        String summary = "", html = "", audioHtml;
        Article article;
        Gson gson = new Gson();
        Elements elements;
        for (Items items : itemsList) {
//            if (WithDB.i().getArticleEchoes(items.getTitle(), items.getCanonical().get(0).getHref()) != 0) {
//                KLog.e("重复文章：" + items.getTitle() );
//                continue;
//            }
//            KLog.e("文章标题："+ items.getTitle() );

            article = new Article();
            // 返回的字段
            article.setId(items.getId());
            article.setCrawlTimeMsec(items.getCrawlTimeMsec());
            article.setTimestampUsec(items.getTimestampUsec());
            article.setCategories(gson.toJson(items.getCategories()));
            article.setTitle(items.getTitle().replace("\r", "").replace("\n", ""));
            article.setPublished(items.getPublished());
            article.setUpdated(System.currentTimeMillis());
            // 设置被加星的时间
            article.setStarred(items.getStarred());
            // items.getCanonical().get(0).getHref()
            article.setCanonical(items.getCanonical().get(0).getHref());
            // items.getAlternate().toString()
            article.setAlternate(gson.toJson(items.getCanonical()));
            article.setAuthor(items.getAuthor());
            article.setOriginStreamId(items.getOrigin().getStreamId());
            article.setOriginHtmlUrl(items.getOrigin().getHtmlUrl());
            article.setOriginTitle(items.getOrigin().getTitle());

            html = items.getSummary().getContent();
            html = StringUtil.getOptimizedContent(html);

            if (items.getEnclosure() != null && items.getEnclosure().size() > 0 && (items.getEnclosure().get(0).getType().startsWith("audio"))) {
                html = html + "<br><audio src=\"" + items.getEnclosure().get(0).getHref() + "\" preload=\"auto\" type=\"" + items.getEnclosure().get(0).getType() + "\" controls></audio>";
            }

            summary = StringUtil.getOptimizedSummary(html);
            article.setSummary(summary);
            article.setContent(html);

//            if (items.getEnclosure() != null && items.getEnclosure().size() > 0 && (items.getEnclosure().get(0).getType().startsWith("image") || items.getEnclosure().get(0).getType().startsWith("parsedImg"))) {
//                article.setCoverSrc(items.getEnclosure().get(0).getHref());
//            }
            // 获取第1个图片作为封面
            elements = Jsoup.parseBodyFragment(article.getContent() == null ? "" : article.getContent()).getElementsByTag("img");
            if (elements.size() > 0) {
                article.setCoverSrc(elements.attr("src"));
            }


            // 自己设置的字段
//            KLog.i("【增加文章】" + article.getId());
            article.setSaveDir(Api.SAVE_DIR_CACHE);
            article = articleChanger.change(article);
            articleList.add(article);
        }
//        WithDB.i().saveArticles(articleList);
        return articleList;
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

    public void renameFeed(OkHttpClient httpClient, String feedId, String renamedTitle, StringCallback cb) {
        InoApi.i().renameFeed(httpClient, feedId, renamedTitle, cb);
    }
    public void unsubscribeFeed(String feedId, StringCallback cb) {
        InoApi.i().unsubscribeFeed(feedId, cb);
    }

    public void unsubscribeFeed(OkHttpClient httpClient, String feedId, StringCallback cb) {
        InoApi.i().unsubscribeFeed(httpClient, feedId, cb);
    }

    public void markArticleListReaded(List<String> articleIDs, StringCallback cb) {
        InoApi.i().markArticleListReaded(articleIDs, cb);
    }

    public void markArticleReaded(String articleID, StringCallback cb) {
        InoApi.i().markArticleReaded(articleID, cb);
    }

    public void markArticleReaded(OkHttpClient httpClient, String articleID, StringCallback cb) {
        InoApi.i().markArticleReaded(httpClient, articleID, cb);
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


}
