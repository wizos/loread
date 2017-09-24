package me.wizos.loread.net;

import android.support.v4.util.ArrayMap;
import android.text.Html;

import com.google.gson.Gson;
import com.socks.library.KLog;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.wizos.loread.App;
import me.wizos.loread.bean.Article;
import me.wizos.loread.bean.Feed;
import me.wizos.loread.bean.Statistic;
import me.wizos.loread.bean.Tag;
import me.wizos.loread.bean.gson.GsSubscriptions;
import me.wizos.loread.bean.gson.GsTags;
import me.wizos.loread.bean.gson.GsUnreadCount;
import me.wizos.loread.bean.gson.ItemIDs;
import me.wizos.loread.bean.gson.StreamContents;
import me.wizos.loread.bean.gson.StreamPref;
import me.wizos.loread.bean.gson.StreamPrefs;
import me.wizos.loread.bean.gson.Sub;
import me.wizos.loread.bean.gson.SubCategories;
import me.wizos.loread.bean.gson.UnreadCounts;
import me.wizos.loread.bean.gson.UserInfo;
import me.wizos.loread.bean.gson.itemContents.Items;
import me.wizos.loread.bean.gson.son.ItemRefs;
import me.wizos.loread.data.WithDB;
import me.wizos.loread.data.WithSet;
import me.wizos.loread.utils.FileUtil;
import me.wizos.loread.utils.StringUtil;

/**
 * Created by Wizos on 2016/3/10.
 */
public class Parser {
    private static Parser parser;

    private Gson gson;

    private Parser(){
        if(gson==null){
            gson = new Gson();
        }
    }

    // 懒汉式的单例模式：在调用 i 的时候才会创建实例
    public static Parser instance(){
        if (parser == null) { // 双重锁定，只有在 parser 还没被初始化的时候才会进入到下一行，然后加上同步锁
            synchronized (Parser.class) { // 同步锁，避免多线程时可能 new 出两个实例的情况
                if ( parser == null ) {
                    parser = new Parser();
                }
            }
        }
        return parser;
    }


    public long parseUserInfo(String info){
        UserInfo userInfo = gson.fromJson(info, UserInfo.class);
//        System.out.println("【parseUserInfo】" + userInfo.toString());
        WithSet.i().setUseId(userInfo.getUserId());
        return userInfo.getUserId();
//        mUserID = userInfo.getUserId();
//        mUserName = userInfo.getUserName();
//        mUserProfileId = userInfo.getUserProfileId();
//        mUserEmail = userInfo.getUserEmail();
//        mIsBloggerUser = userInfo.getIsBloggerUser();
//        mSignupTimeSec = userInfo.getSignupTimeSec();
//        mIsMultiLoginEnabled = userInfo.getIsMultiLoginEnabled();
//        save("mUserID" , mUserID);
//        save("mUserName" , mUserName);
//        save("mUserEmail" , mUserEmail);
    }


//    public void parseReadingList(String info){
//        GsItemContents readingList = gson.fromJson(info, GsItemContents.class);
//    }


    /*
     *  返回的格式
{
    "tags": [
        {
            "id": "user/1006097346/state/com.google/starred",
            "sortid": "FFFFFFFF"
        },
        {
            "id": "user/1006097346/state/com.google/broadcast",
            "sortid": "FFFFFFFE"
        },
        {
            "id": "user/1006097346/state/com.google/blogger-following",
            "sortid": "FFFFFFFD"
        },
        {
            "id": "user/1006097346/label/1_博谈",
            "sortid": "BECB1381"
        }
    ]
}
     *
     */

    private Map<String, Tag> tagMap; // key为tag的sortid（排序ID），value为tag
    private ArrayList<Tag> reTagList;
    private ArrayList<Tag> tempTagList;

    public void parseTagList2(String info) {
        Gson gson = new Gson();
        String[] array;
        String tagTitle;

        tagMap = new HashMap<>();
        tempTagList = gson.fromJson(info, GsTags.class).getTags();
        tempTagList.remove(0);
        tempTagList.remove(0);
        tempTagList.remove(0);

        KLog.d("【parseTagList 1】" + tempTagList.get(0).getSortid() + info);
        for (Tag tag : tempTagList) {
//            tagIdArray.add(tag.getId());
            array = tag.getId().split("/");
            tagTitle = array[array.length-1];
            tag.setTitle(tagTitle);
            tagMap.put(tag.getSortid(), tag);
        }
        KLog.d("【tagId】" + "== tagTitle");
    }


    /**
     * 获取分组、订阅源的排序
     *
     * @param info    返回体
     * @param mUserID 用户ID
     */
    public void parseStreamPrefList( String info,long mUserID) {
//        if( tagIdArray.size() ==0 ){return;}
        KLog.e("执行 parseStreamPrefList" + tempTagList.size());
        if (tempTagList.size() == 0){return;}

        if(mUserID == 0) {
            mUserID = Long.valueOf(tempTagList.get(0).getId().split("/")[1]);
            WithSet.i().setUseId(mUserID);
        }
        Gson gson = new Gson();
        StreamPrefs streamPrefs = gson.fromJson(info, StreamPrefs.class);

        ArrayList<StreamPref> preferences;
        reTagList = new ArrayList<>(tempTagList.size());

        // 由 tags 的排序字符串，生成一个新的 reTags
        preferences = streamPrefs.getStreamPrefsMaps().get("user/" + mUserID + "/state/com.google/root");
        ArrayList<String> mTagsOrderArray = getOrderArray(preferences.get(0).getValue());
        // 这种双循环不好，我觉得效率低。改用Map去获取
//        for( String sortID:mTagsOrderArray ){
//            for (Tag tag:tempTagList){
//                if ( sortID.equals(tag.getSortid()) ){
//                    reTagList.add(tag);
//                }
//            }
//        }
        Tag tempTag;
        for (String sortID : mTagsOrderArray) { // 由于获取到的排序字符串中，可能会包含feed的排序id，造成从tagMap获取不到对应的tag的现象。
            tempTag = tagMap.get(sortID);
            if (tempTag != null) {
                reTagList.add(tempTag );
            }
        }
    }

    /**
     * 将记录排序规则的string转为array
     *
     * @param orderingString 记录排序规则的字符串
     * @return 记录排序规则的数组
     */
    private ArrayList<String> getOrderArray(String orderingString) {
        int num = orderingString.length() / 8;
        ArrayList<String> orderingArray = new ArrayList<>( num );
        for (int i = 0; i < num; i++) {
            orderingArray.add(orderingString.substring(i * 8, (i * 8) + 8));
        }
        return orderingArray;
    }

    public void orderTags(){
        // 排序,通过泛型和匿名类来实现
        // <? super T>表示包括T在内的任何T的父类，<? extends T>表示包括T在内的任何T的子类。http://www.cnblogs.com/friends-wf/p/3582841.html
//        <? extends T> 表示类型的上界，表示参数化类型的可能是T 或是 T的子类
//        <? super T> 表示类型下界（Java Core中叫超类型限定），表示参数化类型是此类型的超类型（父类型），直至Object
        Collections.sort(tempTagList, new Comparator<Tag>() {
            public int compare(Tag o1, Tag o2) {
                return o1.getTitle().compareTo(o2.getTitle());
            }
        });
//        for (Tag tag:tempTagList){
//            KLog.d("【orderTags】" +tag.getTitle());
//        }
        reTagList = tempTagList;
        WithDB.i().saveTagList(reTagList);
    }



    public void parseUnreadCounts(String info){
        Gson gson = new Gson();
        ArrayList<UnreadCounts> unreadCountList = gson.fromJson(info, GsUnreadCount.class).getUnreadcounts();

//        int numOfTags = reTagList.size();
//        int numOfUnreads = unreadCountList.size();
//        String temp;
//        for (int i=0; i<numOfTags; i++){
//            temp = reTagList.get(i).getId(); // 获取 tag 的 id
//            for (int t=0; t<numOfUnreads; t++){
//                if(temp.equals(unreadCountList.get(t).getId())){
//                    reTagList.get(i).setUnreadcount(unreadCountList.get(t).getCount());
////                    KLog.d("【次数】" + unreadCountList.get(t).getCount() );
//                    break;
//                }
//            }
//        }
        Map<String, UnreadCounts> map = new ArrayMap<>();
        for (UnreadCounts unreadCounts : unreadCountList) {
            map.put(unreadCounts.getId(), unreadCounts);
        }
        // 尝试写一下 Count 相关的
        ArrayList<Statistic> statisticList = new ArrayList<>();
        for (Tag tag : reTagList) {
            UnreadCounts unreadCounts = map.get(tag.getId());
            Statistic count = new Statistic();
            count.setId(unreadCounts.getId());
            count.setUnread(unreadCounts.getCount());
            count.setNewestItemTimestampUsec(unreadCounts.getNewestItemTimestampUsec());
            statisticList.add(count);
        }
        List<Feed> feeds = WithDB.i().getFeeds();

        for (Feed feed : feeds) {
            UnreadCounts unreadCounts = map.get(feed.getId());
            Statistic count = new Statistic();
            count.setId(unreadCounts.getId());
            count.setUnread(unreadCounts.getCount());
            count.setNewestItemTimestampUsec(unreadCounts.getNewestItemTimestampUsec());
            statisticList.add(count);
        }


        WithDB.i().saveStatisticList(statisticList);

        unreadCounts = unreadCountList.get(0).getCount();
        starredCounts = unreadCountList.get(1).getCount();
        remoteUnreadRefs = new ArrayList<>(unreadCounts);
        remoteStarredRefs = new ArrayList<>(starredCounts);
    }


    public ArrayList<Sub> parseSubscriptionList(String info) {
        ArrayList<Sub> subs = gson.fromJson(info, GsSubscriptions.class).getSubscriptions();
        List<Feed> feeds = new ArrayList<>(subs.size());
        for (Sub sub : subs) {
            Feed feed = new Feed();
            feed.setId(sub.getId());
            feed.setTitle(sub.getTitle());
            try {
                feed.setCategoryid(sub.getCategories().get(0).getId());
                feed.setCategorylabel(sub.getCategories().get(0).getLabel());
            } catch (Exception e) {
                feed.setCategoryid(null);
                feed.setCategorylabel(null);
            }
            feed.setSortid(sub.getSortid());
            feed.setFirstitemmsec(sub.getFirstitemmsec());
            feed.setHtmlurl(sub.getHtmlUrl());
            feed.setIconurl(sub.getIconUrl());
            feeds.add(feed);
        }
        WithDB.i().saveAllFeeds(feeds);
        return subs;
    }

    private int unreadCounts;
    private int starredCounts;
    private ArrayList<ItemRefs> remoteUnreadRefs;
    private ArrayList<ItemRefs> remoteStarredRefs;
    public String parseItemIDsStarred(String info){
        Gson gson = new Gson();
        ItemIDs itemIDs = gson.fromJson(info, ItemIDs.class);
        ArrayList<ItemRefs> partStarredRefs = itemIDs.getItemRefs();
        if(partStarredRefs!=null){
            remoteStarredRefs.addAll(partStarredRefs);
        }
        return itemIDs.getContinuation();
    }
    public String parseItemIDsUnread(String info){
        Gson gson = new Gson();
        ItemIDs itemIDs = gson.fromJson(info, ItemIDs.class);
        ArrayList<ItemRefs> partUnreadRefs = itemIDs.getItemRefs();
        if(partUnreadRefs!=null){
            remoteUnreadRefs.addAll(partUnreadRefs);
        }
        return itemIDs.getContinuation();
    }

    /**
     * 同步云端与本地的未读/加星的状态 的 整体思路：
     * 1，先加载本地未读 A ，再网络获取到未读 B。去重得到 本地 readList 与 云端 UnreadRefs
     * 2，同理得到加星的 本地 starList 与 云端 staredRefs
     * 3，去重 readList 与 starList 得到 reReadStaredRefs，reReadUnstarRefs，reUnreadStaredRefs
     */

    /**
     * 本地与云端去重的 2 种思路的测试比对：（比较字符串是否相等很费时间）
     * 1，循环本地未读 AList 放入 map，循环云端未读 BRefs 内每项在 map 内是否存在：存在 value+1，不在的再查该条是否存在于数据中否则放入 CRefs 。循环 map 内 value = 1 的
     * 2，循环本地所有 AList 放入 map，循环云端未读 BList 内每项在 map 内是否存在：存在再查数据库取出 Article 并判断状态 （状态字符串是否相等）。
     *
     * 1，81  毫秒（661  本地未读、791 云端未读、130 更改本地、260 获取云端未读）
     * 2，111 毫秒（1219 本地所有、791 云端未读、0   更改本地、260 获取云端未读）
     *
     * 1，107 毫秒（661  本地未读、804 云端未读、130 更改本地、273 获取云端未读）
     * 2，115 毫秒（1219 本地所有、804 云端未读、0   更改本地、273 获取云端未读）
     * */
    public ArrayList<ItemRefs> reUnreadRefs(){
        long xx = System.currentTimeMillis();
        List<Article> localUnreadArticles = WithDB.i().getArt(API.LIST_UNREAD);
        Map<String, Integer> map = new ArrayMap<>(localUnreadArticles.size() + remoteUnreadRefs.size());
        Map<String, Article> mapArticle = new ArrayMap<>(localUnreadArticles.size());
        ArrayList<Article> readList = new ArrayList<>(localUnreadArticles.size());// 筛选下来，根据云端的Refs的状态，需要要变本地文章状态的集合
        ArrayList<ItemRefs> unreadRefs = new ArrayList<>(remoteUnreadRefs.size());// 筛选下来，最终要去云端获取内容的未读Refs的集合

        KLog.d("【reUnreadRefs】" + localUnreadArticles.size() + "==" + remoteUnreadRefs.size());

        // 数据量大的一方
//        String articleId;
//        Article article;
        for (Article item : localUnreadArticles) {
            String articleId = item.getId();
            map.put(articleId, 1);
            mapArticle.put(articleId,item);
        }
        // 数据量小的一方
        String articleId;
        Integer cc;
        for (ItemRefs item : remoteUnreadRefs) {
            articleId = StringUtil.toLongID(item.getId());
            cc = map.get(articleId);
            if (cc != null) { // 本地和远端重复的
                map.put(articleId , ++cc);
            } else {
                unreadRefs.add(item);// 本地无，而云远端有，加入要请求的未读资源
            }
        }
        for( Map.Entry<String, Integer> entry: map.entrySet()) {
            if(entry.getValue()==1) {
                Article article = mapArticle.get(entry.getKey());
                article.setReadState(API.ART_READED); // 本地未读设为已读
                readList.add(article);
            }
        }

        long yy = System.currentTimeMillis() - xx;
        KLog.d("【reUnreadRefs】测试" + yy + " - " + localUnreadArticles.size() + "==" + remoteUnreadRefs.size() + "==" + readList.size() + "==" + unreadRefs.size());
        WithDB.i().saveArticleList(readList);
        return unreadRefs;
    }



    public ArrayList<ItemRefs> reStarredRefs(){
        List<Article> localStarredArticles = WithDB.i().getStaredArt();
//        Map<String, Integer> map = new ArrayMap<>(localStarredArticles.size() + remoteStarredRefs.size());
        Map<String, Article> map = new ArrayMap<>(localStarredArticles.size());
        ArrayList<Article> starList = new ArrayList<>(localStarredArticles.size());
        ArrayList<ItemRefs> starredRefs = new ArrayList<>(remoteStarredRefs.size());

        int i = 0;
        // 数据量大的一方
        String articleId;
        // 第1步，遍历数据量大的一方A，将其比对项目放入Map中，计数为1
        for (Article article : localStarredArticles) {
            articleId = article.getId(); // String
            map.put(articleId, article);
//            mapArticle.put(articleId,item);
            KLog.i("【本地star文章】" + articleId);
        }
        KLog.d(WithDB.i().getStaredArt().size() + "个");
        // 第2步，遍历数据量小的一方B。到Map中找，是否含有b中的比对项。有则XX，无则YY
        for (ItemRefs item : remoteStarredRefs) {
            articleId = StringUtil.toLongID(item.getId()); // String
//            Integer cc = map.get( articleId );
            Article article = map.get(articleId);
            if (article != null) {
                if (article.getTimestampUsec() != item.getTimestampUsec()) {
                    article.setStarred(item.getTimestampUsec()); // 由于位数不一样，不能用
                }
                map.remove(articleId);
            } else {
                starredRefs.add(item);// 3，就剩云端的，要请求的加星资源（但是还是含有一些要请求的未读资源）
            }
        }

        for (Map.Entry<String, Article> entry : map.entrySet()) {
            if (entry.getValue() != null) {
                Article article = map.get(entry.getKey());
                article.setStarState(API.ART_UNSTAR);
                starList.add(article);// 取消加星
            }
        }
        KLog.d("嘉欣的数量" + localStarredArticles.size());
        KLog.d("【reStarredList】" + localStarredArticles.size() + "==" + remoteStarredRefs.size() + "==" + starList.size() + "==" + starredRefs.size());
        WithDB.i().saveArticleList(starList);
        return starredRefs;
    }
    // Note:  用新方法写的去重函数，暂时未启用。
//    public List<ItemRefs> reUnreadRefs2(){
//        Doubler< List<ItemRefs>,List<Article> > doubler = new Doubler(remoteUnreadRefs, WithDB.i().getArt(API.LIST_UNREAD) );
//        Doubler.DoubleListener<ItemRefs,Article > doubleListener = new Doubler.DoubleListener<ItemRefs, Article>() {
//            @Override
//            public Object onListANext(ItemRefs itemA) {
//                return StringUtil.toLongID(itemA.getId());
//            }
//
//            @Override
//            public Object onListBNext(Article itemB) {
//                return itemB.getId();
//            }
//        };
//        doubler.setDoubleListener(doubleListener);
//        Map<String,Object> returnValue = doubler.startDeDouble();
//        List<ItemRefs> unreadRefs = (List<ItemRefs>) returnValue.get("partItemA");
//        List<Article> readList = (List<Article>)returnValue.get("partItemB");
//        for (Article article:readList){
//            article.setReadState(API.ART_READED);
//        }
//        WithDB.i().saveArticleList(readList);
//        return unreadRefs;
//    }
//
//
//
//    public List<ItemRefs> reStarredRefs2(){
//        Doubler< List<ItemRefs>,List<Article> > doubler = new Doubler(remoteStarredRefs, WithDB.i().getStaredArt() );
//        Doubler.DoubleListener<ItemRefs,Article > doubleListener = new Doubler.DoubleListener<ItemRefs, Article>() {
//            @Override
//            public Object onListANext(ItemRefs itemA) {
//                return StringUtil.toLongID(itemA.getId());
//            }
//
//            @Override
//            public Object onListBNext(Article itemB) {
//                return itemB.getId();
//            }
//        };
//        doubler.setDoubleListener(doubleListener);
//        Map<String,Object> returnValue = doubler.startDeDouble();
//        List<ItemRefs> starredRefs = (List<ItemRefs>) returnValue.get("partItemA");
//        List<Article> starList = (List<Article>)returnValue.get("partItemB");
//        for (Article article:starList){
//            article.setReadState(API.ART_UNSTAR);
//        }
//        WithDB.i().saveArticleList(starList);
//        return starredRefs;
//    }

    public ArrayList<ItemRefs> reUnreadUnstarRefs;
    public ArrayList<ItemRefs> reUnreadStarredRefs;
    public ArrayList<ItemRefs> reReadStarredRefs;


//    public ArrayList<ItemRefs> getReUnreadUnstarRefs() {
//        return reUnreadUnstarRefs;
//    }
//
//    public ArrayList<ItemRefs> getReUnreadStarredRefs() {
//        return reUnreadStarredRefs;
//    }
//
//    public ArrayList<ItemRefs> getReReadStarredRefs() {
//        return reReadStarredRefs;
//    }

    /**
     * 整理从云端获取的未读、加星的资源，与本期去重。
     *
     * @return 要从云端获取的数量
     */
//    public int reRefsWithLocal(){
//        List<Article> localArticles = WithDB.i().getAllArt();
//        Map<String,Article> map = new ArrayMap<>( localArticles.size() );
//
//
//        for (Article article : localArticles) {
//            map.put(article.getId(),article);
////            KLog.i("【本地star文章】" + article.getId());
//        }
//        KLog.i("本地文章共" + localArticles.size() + "个");
//        String articleId;
//        // 第2步，遍历数据量小的一方B。到Map中找，是否含有b中的比对项。有则XX，无则YY
//        for (ItemRefs item : reUnreadUnstarRefs) {
//            articleId = StringUtil.toLongID(item.getId());
//            if( map.get( articleId ) != null && map.get(articleId).getTimestampUsec() == item.getTimestampUsec() ) { // 本地和云端都有。再看时间戳是否一致。不一致说明云端该项有变动（加星等）
//                KLog.e("本地和云端是一致的：" + map.get(articleId).getTitle() );
//                map.remove( articleId );
//            }else {
//                starredRefs.add(item);// 3，就剩云端的，要请求的加星资源（但是还是含有一些要请求的未读资源）
//            }
//        }
//
//        for( Map.Entry<String, Integer> entry: map.entrySet()) {
//            if(entry.getValue()==1) {
//                Article article = mapArticle.get(entry.getKey());
//                article.setStarState(API.ART_UNSTAR);
//                starList.add(article);// 取消加星
//            }
//        }
//
//    }


    public int reRefs(final ArrayList<ItemRefs> unreadRefs, final ArrayList<ItemRefs> starredRefs){
        if (!checkCounts()){return -1;}
        remoteUnreadRefs = new ArrayList<>();
        remoteStarredRefs = new ArrayList<>();

        int arrayCapacity = 0;
        if(unreadRefs.size() > starredRefs.size()){
            arrayCapacity = starredRefs.size();
        }else {
            arrayCapacity = unreadRefs.size();
        }
        reUnreadUnstarRefs = new ArrayList<>( unreadRefs.size() );
        reReadStarredRefs = new ArrayList<>( starredRefs.size() );
        reUnreadStarredRefs = new ArrayList<>( arrayCapacity );
        Map<String,Integer> map = new ArrayMap<>( unreadRefs.size() + starredRefs.size() );
        Map<String,ItemRefs> mapArray = new ArrayMap<>( unreadRefs.size() );
        for ( ItemRefs item : unreadRefs ) {
            map.put( item.getId() ,1 ); //  String articleId = item.getId();
            mapArray.put( item.getId() ,item );
        }
        for ( ItemRefs item : starredRefs ) {
            Integer cc = map.get( item.getId() );
            if( cc!=null ) {
                map.put( item.getId() ,+cc );
                reUnreadStarredRefs.add(item);
            }else {
                reReadStarredRefs.add(item);
            }
        }
        for( Map.Entry<String, Integer> entry: map.entrySet()) {
            if(entry.getValue()==1) {
                reUnreadUnstarRefs.add( mapArray.get( entry.getKey() ));
            }
        }
        KLog.d("【reRefs】测试" + reUnreadUnstarRefs.size() + "--" + reReadStarredRefs.size() + "--" + reUnreadStarredRefs.size() );
        return reUnreadUnstarRefs.size() + reReadStarredRefs.size() + reUnreadStarredRefs.size();
    }

//
//    public boolean classify(){
//        if (!checkCounts()){return false;}
//
//        Map<String,Integer> map = new ArrayMap<>( unreadCounts + starredCounts );
//
//        reUnreadUnstarRefs = new ArrayList<>( unreadCounts + starredCounts );
//        reReadStarredRefs = new ArrayList<>( unreadCounts + starredCounts );
//        reUnreadStarredRefs = new ArrayList<>( unreadCounts + starredCounts );
//
//        // 1，先将 Refs 去重
//        Map<String,ItemRefs> starredMap = new ArrayMap<>( allStarredRefs.size() );
//        for ( ItemRefs item : allStarredRefs ) {
//            map.put( item.getId() ,1 ); //  String articleId = item.getId();
//            starredMap.put( item.getId() ,item );
//        }
//        for ( ItemRefs item : allUnreadRefs ) {
//            Integer cc = map.get( item.getId() );
//            if( cc!=null ) {
//                map.put( item.getId() ,+cc );
//                reUnreadStarredRefs.add(item);
//            }else {
//                reUnreadUnstarRefs.add(item);
//            }
//        }
//        for( Map.Entry<String, Integer> entry: map.entrySet()) {
//            if(entry.getValue()==1) {
//                reReadStarredRefs.add( starredMap.get( entry.getKey() ));
//            }
//        }
//
//        // 2，再将 Refs 与 本地 Articles 去重
//        reUnreadUnstarRefs = deDuplicate(reUnreadUnstarRefs, WithDB.i().loadUnreadUnstarred(), new ArticleChanger() {
//            @Override
//            public Article change(Article article) {
//                article.setReadState(API.ART_UNREAD);
//                article.setStarState(API.ART_UNSTAR);
//                return article;
//            }
//        }, new ArticleChanger() {
//            @Override
//            public Article change(Article article) {
//                article.setReadState(API.ART_READED);
//                article.setStarState(API.ART_STARED);
//                return article;
//            }
//        });
//
//        reUnreadStarredRefs = deDuplicate(reUnreadStarredRefs, WithDB.i().loadUnreadStarred(), new ArticleChanger() {
//            @Override
//            public Article change(Article article) {
//                article.setReadState(API.ART_UNREAD);
//                article.setStarState(API.ART_STARED);
//                return article;
//            }
//        }, new ArticleChanger() {
//            @Override
//            public Article change(Article article) {
//                article.setReadState(API.ART_READED);
//                article.setStarState(API.ART_UNSTAR);
//                return article;
//            }
//        });
//        reReadStarredRefs = deDuplicate(reReadStarredRefs, WithDB.i().loadReadUnstarred(), new ArticleChanger() {
//            @Override
//            public Article change(Article article) {
//                article.setReadState(API.ART_READED);
//                article.setStarState(API.ART_STARED);
//                return article;
//            }
//        }, new ArticleChanger() {
//            @Override
//            public Article change(Article article) {
//                article.setReadState(API.ART_UNREAD);
//                article.setStarState(API.ART_UNSTAR);
//                return article;
//            }
//        });
//        return true;
//    }


    private boolean checkCounts(){
        return (unreadCounts <= remoteUnreadRefs.size()) && (starredCounts <= remoteStarredRefs.size());
    }

    public void parseItemContentsUnreadUnstar(String info){
        parseItemContents(info, new ArticleChanger() {
            @Override
            public Article change(Article article) {
                article.setReadState(API.ART_UNREAD);
                article.setStarState(API.ART_UNSTAR);
                return article;
            }
        });
    }
    public void parseItemContentsUnreadStarred(String info){
        parseItemContents(info, new ArticleChanger() {
            @Override
            public Article change(Article article) {
                article.setReadState(API.ART_UNREAD);
                article.setStarState(API.ART_STARED);
                return article;
            }
        });
    }
    public void parseItemContentsReadStarred(String info){
        parseItemContents(info, new ArticleChanger() {
            @Override
            public Article change(Article article) {
                article.setReadState(API.ART_READED);
                article.setStarState(API.ART_STARED);
                return article;
            }
        });
    }
    public String parseStreamContentsStarred(String info){
        return parseItemContents(info, new ArticleChanger() {
            @Override
            public Article change(Article article) {
                article.setReadState(API.ART_READED);
                article.setStarState(API.ART_STARED);
                return article;
            }
        });
    }
    private interface ArticleChanger{
        Article change(Article article);
    }
    /**
     * 这里有两种方法来实现了函数 A B C 共用一个主函数 X ，但各自在主函数中的某些语句又不同
     * 1.是采用分割主函数为多个函数 X[]，再在要在具体的函数 A B C 内拼接调用 X[]。
     * 2.（目前）是采用接口类作为主函数 X 的参数传递，在调用具体的函数 A B C 时，将各自要不同的语句在该接口内实现
     * 之前的代码是函数 A B C 都各自再写一遍共用函数
     * 使用接口类作为参数传递，实际上是让调用者来实现具体语句
     * @param info 获得的响应体
     * @param articleChanger 回调，用于修改 Article 对象
     */
    private String parseItemContents( String info, ArticleChanger articleChanger ){
        // 如果返回 null 会与正常获取到流末端时返回 continuation = null 相同，导致调用该函数的那端误以为是正常的 continuation = null
        if (info == null || info.equals("")) {
            return "";
        }
        Gson gson = new Gson();
        StreamContents gsItemContents = gson.fromJson(info, StreamContents.class);
        ArrayList<Items> currentItemsArray = gsItemContents.getItems();
        ArrayList<Article> saveList = new ArrayList<>( currentItemsArray.size() ) ;
        String summary = "",html = "";
        for ( Items items: currentItemsArray  ) {
            if (WithDB.i().getStarredArticle(items.getId()) != null) {
                continue;
            }
            Article article = new Article();
            // 返回的字段
            article.setId(items.getId());
            article.setCrawlTimeMsec(items.getCrawlTimeMsec());
            article.setTimestampUsec(items.getTimestampUsec());
            article.setCategories(items.getCategories().toString());
            article.setTitle(items.getTitle().replace(File.separator, "-").replace("\r", "").replace("\n", ""));
            article.setPublished(items.getPublished());
            article.setUpdated(items.getUpdated());
            article.setStarred(items.getStarred());// 设置被加星的时间
            article.setCanonical(items.getCanonical().get(0).getHref());
            article.setAlternate(items.getAlternate().toString());
            article.setAuthor(items.getAuthor());
            article.setOriginStreamId(items.getOrigin().getStreamId());
            article.setOriginHtmlUrl(items.getOrigin().getHtmlUrl());
            article.setOriginTitle(items.getOrigin().getTitle());

            // 自己设置的字段
            article.setSaveDir(API.SAVE_DIR_CACHE);

            KLog.i("【增加文章】" + article.getId());
            html = items.getSummary().getContent();
            summary = Html.fromHtml(html).toString();
            if(summary.length()>92){
                article.setSummary(summary.substring(0,92));
            }else {
                article.setSummary(summary.substring(0,summary.length()));
            }
            article = articleChanger.change(article);

            FileUtil.saveCacheHtml(StringUtil.stringToMD5(article.getId()), html);
            saveList.add(article);
        }
        WithDB.i().saveArticleList(saveList);
        return gsItemContents.getContinuation();
    }

    public void parseStreamContents(String info) {
        StreamContents sContents = gson.fromJson(info, StreamContents.class);
        API.itemlist = sContents.getItems();
    }

    // 这个使用在单独去获取某一篇文章的内容后，来解析json
    public void parseArticleContents(String info){
        Gson gson = new Gson();
        ArrayList<Items> itemArticles = gson.fromJson(info, StreamContents.class).getItems();
        Items items = itemArticles.get(0);
        if (itemArticles.size() != 0 && WithDB.i().getArticle(items.getId()) != null) {
            FileUtil.saveCacheHtml(StringUtil.stringToMD5(items.getId()), items.getSummary().getContent());
        }
    }


    /**
     * 更新所有的已保存文章的分组等信息
     * @param subs
     */
    public void updateArticles(ArrayList<Sub> subs){
        List<Article> allStarArts = WithDB.i().getStaredArt();
        List<Tag> allTags = WithDB.i().getTags();
        Map<String,Sub> mapSub = new ArrayMap<>(subs.size());
        Map<String,String> mapTag = new ArrayMap<>(allTags.size());
        // 此处比较是否存在有个性能疑问，是用字符串是否包含还是map是否包含来判断呢？
        for (Sub sub:subs){
            mapSub.put(sub.getId(),sub);
        }
        for(Tag tag:allTags){
            mapTag.put(tag.getTitle(),tag.getId());
        }

        for ( Article article : allStarArts ){
            String streamIdOfArticle = article.getOriginStreamId();

            if ( mapSub.containsKey( streamIdOfArticle )){ // 判断是否还订阅着这篇文章的站点
                // 情况1，还在订阅着，但是云端分组名已变（一个订阅源可能属于多个分组）
//                subscription = mapSub.get( streamIdOfArticle );
//                artCategories = m.replaceFirst( subscription.getCategories().get(0).getId() );

                // 构建没有 label 的 分类String
                StringBuilder newCategories = new StringBuilder( article.getCategories().length() );
                String[] categories = article.getCategories().replace("]","").replace("[","").split(", ");
                for (String cateId:categories ){
                    if (!cateId.contains("user/" + App.mUserID + "/label/")) {
                        newCategories.append(cateId);
                        newCategories.append(", ");
                    }else {
                        break;
                    }
                }
                ArrayList<SubCategories> newSubCategories = mapSub.get( streamIdOfArticle ).getCategories();
                for( SubCategories cate: newSubCategories ){
                    newCategories.append( cate.getId() );
                    newCategories.append(", ");
                }
                newCategories.deleteCharAt(newCategories.length()-2);
                newCategories.append("]");
                newCategories.insert(0,"[");
                KLog.d("【==】" + newCategories );
                article.setCategories( newCategories.toString() );
            }else {
                // 情况2，该文章的源站点已经退订
                StringBuilder newCategories = new StringBuilder(  article.getCategories().length()  );
                String[] categories = article.getCategories().replace("]","").replace("[","").split(", ");
                for (String cate:categories ){
                    if (!cate.contains("user/" + App.mUserID + "/label/")) {
                        newCategories.append(cate);
                        newCategories.append(", ");
                    }else if( mapTag.containsValue(cate) ) {
                        newCategories.append(cate);
                        newCategories.append(", ");
                    }
                }
                newCategories.deleteCharAt(newCategories.length()-2);
                article.setCategories( newCategories.toString() );
            }
            WithDB.i().saveArticle(article);
        }
    }





}
