package me.wizos.loread.net;

import android.text.Html;

import com.google.gson.Gson;
import com.socks.library.KLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.wizos.loread.bean.Article;
import me.wizos.loread.bean.Tag;
import me.wizos.loread.dao.WithDB;
import me.wizos.loread.dao.WithSet;
import me.wizos.loread.gson.GsItemContents;
import me.wizos.loread.gson.GsStreamContents;
import me.wizos.loread.gson.GsSubscriptions;
import me.wizos.loread.gson.GsTags;
import me.wizos.loread.gson.GsUnreadCount;
import me.wizos.loread.gson.ItemIDs;
import me.wizos.loread.gson.ItemRefs;
import me.wizos.loread.gson.StreamPref;
import me.wizos.loread.gson.StreamPrefs;
import me.wizos.loread.gson.Sub;
import me.wizos.loread.gson.UnreadCounts;
import me.wizos.loread.gson.UserInfo;
import me.wizos.loread.gson.itemContents.Items;
import me.wizos.loread.utils.UFile;
import me.wizos.loread.utils.UString;

/**
 * Created by Wizos on 2016/3/10.
 */
public class Parser {

//    public GsStreamContents sContents;
//    public String mTagsOrder;
//    public ArrayList<Item> itemArray;
//    public String mUserName;
//    public String mUserProfileId;
//    public String mUserEmail;
//    public Boolean mIsBloggerUser;
//    public long mSignupTimeSec;
//    public Boolean mIsMultiLoginEnabled;


    private Gson gson;

    public Parser(){
//        long uu = System.currentTimeMillis(); // 使用取得数据库的未读/加星的数目为初始容量时，耗时 70，改为数字后，耗时 0
//        allStarredRefs = new ArrayList<>( 500 );
//        allUnreadRefs = new ArrayList<>( 1000 );
//        long gg = System.currentTimeMillis() - uu ;
//        KLog.d("用时：" , gg );
        if(gson==null){
            gson = new Gson();
        }
    }

    public long parseUserInfo(String info){
        UserInfo userInfo = gson.fromJson(info, UserInfo.class);
//        System.out.println("【parseUserInfo】" + userInfo.toString());
        WithSet.getInstance().setUseId(Long.valueOf( userInfo.getUserId() ));
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

    public void parseStreamContents(String info){
        GsStreamContents sContents = gson.fromJson(info, GsStreamContents.class);
        API.itemlist = sContents.getItems();
    }

    public void parseReadingList(String info){
        GsItemContents readingList = gson.fromJson(info, GsItemContents.class);
    }

    private ArrayList<Tag> tagList ;
    private ArrayList<String> tagIdArray;
    public void parseTagList(String info){
        Gson gson = new Gson();
        tagList = gson.fromJson(info, GsTags.class).getTags();
        KLog.d("【parseTagList 1】" + tagList.get(0).getSortid() + info);

        String tagId;
        String[] array;
        String tagTitle;
        ArrayList<Tag> tags = new ArrayList<>( tagList );
        tags.remove(1);
        tags.remove(1);

        int num = tags.size();
        tagIdArray = new ArrayList<>(num);

        tags.get(0).setTitle( "加星" );
        tagId = tags.get(0).getId();
        tagIdArray.add(tagId);
        for ( int i = 1; i < num ; i++ ) {
            tagId = tags.get(i).getId();
            array = tagId.split("/");
            tagTitle = array[array.length-1];
            tags.get(i).setTitle(tagTitle);
            tagIdArray.add(tagId);
            KLog.d("【tagId】" + tagId );
            KLog.d("【tagTitle】" + tagTitle );
        }
        tagList = tags;
    }

    private ArrayList<Tag> reTagList;
    public void parseStreamPrefList( String info,long mUserID){
        if(mUserID == 0){
            mUserID = Long.valueOf(tagIdArray.get(0).split("/")[1]);
            WithSet.getInstance().setUseId(Long.valueOf(mUserID));
        }
        Gson gson = new Gson();
        StreamPrefs streamPrefs = gson.fromJson(info, StreamPrefs.class);
        int y = tagIdArray.size();
        if(y==0){return;}
        ArrayList<StreamPref> preferences;
        reTagList = new ArrayList<>(tagList.size());
        // 由 tags 的排序字符串，生成一个新的 reTags
        preferences = streamPrefs.getStreamPrefsMaps().get("user/" + mUserID + "/state/com.google/root");
        ArrayList<String> mTagsOrderArray = getOrderArray(preferences.get(0).getValue());
        for( String sortID:mTagsOrderArray ){
//            sortID = mTagsOrderArray.get(i);
            for (Tag tag:tagList){
                if ( sortID.equals(tag.getSortid()) ){
                    reTagList.add(tag);
                }
            }
        }
    }
    private ArrayList<String> getOrderArray(String subOrdering){
        int num = subOrdering.length() / 8;
        ArrayList<String> orderingArray = new ArrayList<>( num );
        for (int i = 0; i < num; i++) {
            orderingArray.add(subOrdering.substring(i * 8, (i * 8) + 8));
        }
        return orderingArray;
    }
    private void saveReTags(){
        WithDB.getInstance().saveTagList(reTagList);
    }
    public void orderTags(){
        // 排序,通过泛型和匿名类来实现
        // <? super T>表示包括T在内的任何T的父类，<? extends T>表示包括T在内的任何T的子类。http://www.cnblogs.com/friends-wf/p/3582841.html
//        <? extends T> 表示类型的上界，表示参数化类型的可能是T 或是 T的子类
//        <? super T> 表示类型下界（Java Core中叫超类型限定），表示参数化类型是此类型的超类型（父类型），直至Object
        Collections.sort(tagList, new Comparator<Tag>() {
            public int compare(Tag o1, Tag o2) {
                return o1.getTitle().compareTo(o2.getTitle());
            }
        });
//        for (Tag tag:tagList){
//            KLog.d("【orderTags】" +tag.getTitle());
//        }
        reTagList = tagList;
        WithDB.getInstance().saveTagList( reTagList );
    }



    public void parseUnreadCounts(String info){
        Gson gson = new Gson();
        ArrayList<UnreadCounts> unreadCountList = gson.fromJson(info, GsUnreadCount.class).getUnreadcounts();
//        int unreadCountMax = gsUnreadCount.getMax();
        int numOfTagList = reTagList.size();
        int numOfUnreadList = unreadCountList.size();
        String temp;
        for (int i=0; i<numOfTagList; i++){
            temp = reTagList.get(i).getId();
            for (int t=0; t<numOfUnreadList; t++){
                if(temp.equals(unreadCountList.get(t).getId())){
                    reTagList.get(i).setUnreadcount(unreadCountList.get(t).getCount());
//                    System.out.println("【次数】" + unreadCountList.get(t).getCount() );
                    break;
                }
            }
        }
        saveReTags();
    }


    public void parseSubscriptionList(String info){
        ArrayList<Sub> subs = gson.fromJson(info, GsSubscriptions.class).getSubscriptions();
    }


    private ArrayList<ItemRefs> allStarredRefs = new ArrayList<>( 500 );
    private ArrayList<ItemRefs> allUnreadRefs = new ArrayList<>( 1000 );
    public String parseItemIDsStarred(String info){
        Gson gson = new Gson();
        ItemIDs itemIDs = gson.fromJson(info, ItemIDs.class);
        ArrayList<ItemRefs> partStarredRefs = itemIDs.getItemRefs();
        allStarredRefs.addAll( partStarredRefs );
        return itemIDs.getContinuation();
    }
    public String parseItemIDsUnread(String info){
        Gson gson = new Gson();
        ItemIDs itemIDs = gson.fromJson(info, ItemIDs.class);
        ArrayList<ItemRefs> partUnreadRefs = itemIDs.getItemRefs();
        allUnreadRefs.addAll( partUnreadRefs );
        return itemIDs.getContinuation();
    }

    /**
     * 同步云端与本地的未读/加星的状态 的 整体思路：
     * 1，先加载本地未读 A ，再网络获取到未读 B。去重得到 本地 readList 与 云端 UnreadRefs
     * 2，同理得到加星的 本地 starList 与 云端 staredRefs
     * 3，去重 readList 与 starList 得到 reReadStaredRefs，reReadUnstarRefs，reUnreadStaredRefs
     */
    // 【以下为错误】因为参数不能重复添加一个。服务器没有直接给出 unreadRefs
    // 我想 获取 UnreadRefs StarredRefs，再分出 reUnreadRefs reStarredRefs reUnreadStarredRefs。以保证保存到数据库的文章的初始属性不一致，其实完全没有必要
    // 只要在获取 Refs 时加上参数，就可分出了。

    /**
     * 本地与云端去重的 2 种思路的测试比对：比较字符串是否相等很费时间
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
        List<Article> beforeArticleArray = WithDB.getInstance().loadReadList(API.LIST_UNREAD, "");
        Map<String,Integer> map = new HashMap<>( beforeArticleArray.size() + allUnreadRefs.size() );
        Map<String,Article> mapArticle = new HashMap<>( beforeArticleArray.size() );
        ArrayList<Article> readList =  new ArrayList<>( beforeArticleArray.size() );
        ArrayList<ItemRefs> unreadRefs = new ArrayList<>( allUnreadRefs.size() );

        KLog.d("【reUnreadRefs】"+  beforeArticleArray.size() + "==" + allUnreadRefs.size() );

        for ( Article item : beforeArticleArray ) {
            String articleId = item.getId();
            map.put(articleId, 1);
            mapArticle.put(articleId,item);
        }
        for ( ItemRefs item : allUnreadRefs) {
            String articleId = UString.toLongID(item.getId());
            Integer cc = map.get( articleId );
            if(cc!=null) {
                map.put( articleId , ++cc);  // 1，去掉“本地有，状态为未读”的
            }else {
                // FIXME: 2016/5/1 这里对数据库一条条的查询也可以优化
                Article article = WithDB.getInstance().getArticle( articleId );
                if(article!=null){
                    article.setReadState( API.ART_UNREAD );// 2，去掉“本地有，状态为已读”的
                    readList.add(article);
                }else {
                    unreadRefs.add(item);// 3，就剩云端的，要请求的未读资源
                }
            }
        }
        for( Map.Entry<String, Integer> entry: map.entrySet()) {
            if(entry.getValue()==1) {
                Article article = mapArticle.get(entry.getKey());
                article.setReadState(API.ART_READ); // 本地未读设为已读
                readList.add(article);
            }
        }

        long yy = System.currentTimeMillis() - xx;

        KLog.d("【reUnreadRefs】测试"+ yy + " - " + beforeArticleArray.size() + "==" + allUnreadRefs.size() +"=="+ readList.size()  + "==" + unreadRefs.size() );
        WithDB.getInstance().saveArticleList(readList);
        allUnreadRefs =  new ArrayList<>();
        return unreadRefs;
    }

    public ArrayList<ItemRefs> reStarredRefs(){
        List<Article> beforeStarredList = WithDB.getInstance().loadStarAll();
//        WithDB.getInstance().loadStarAllOrder();
        Map<String,Integer> map = new HashMap<>( beforeStarredList.size() + allStarredRefs.size());
        Map<String,Article> mapArticle = new HashMap<>( beforeStarredList.size() );
        ArrayList<Article> starList =  new ArrayList<>( beforeStarredList.size() );
        ArrayList<ItemRefs> starredRefs = new ArrayList<>( allStarredRefs.size() );

        for ( Article item : beforeStarredList ) {
            String articleId = item.getId();
            map.put(articleId, 1);
            mapArticle.put(articleId,item);
        }
        for ( ItemRefs item : allStarredRefs ) {
            String articleId = UString.toLongID(item.getId());
            Integer cc = map.get( articleId );
            if(cc!=null) {
                map.put( articleId , ++cc);// 1，去掉“本地有，状态为加星”的
            }else {
                Article article = WithDB.getInstance().getArticle( articleId );
                if(article!=null){
                    article.setStarState(API.ART_STAR);// 2，去掉“本地有，状态为未加星”的
                    starList.add(article);
                }else {
                    starredRefs.add(item);// 3，就剩云端的，要请求的加星资源（但是还是含有一些要请求的未读资源）
                }
            }
        }
        for( Map.Entry<String, Integer> entry: map.entrySet()) {
            if(entry.getValue()==1) {
                Article article = mapArticle.get(entry.getKey());
                article.setStarState(API.ART_UNSTAR);
                starList.add(article);// 取消加星
            }
        }
        System.out.println("【reStarredList】" + beforeStarredList.size() + "==" + allStarredRefs.size() +"==" + starList.size() +"=="+ starredRefs.size());
        WithDB.getInstance().saveArticleList(starList);
        allStarredRefs = new ArrayList<>();
        return starredRefs;
    }


    public ArrayList<ItemRefs> reUnreadUnstarRefs;
    public ArrayList<ItemRefs> reUnreadStarredRefs;
    public ArrayList<ItemRefs> reReadStarredRefs;
    public int reRefs( ArrayList<ItemRefs> unreadRefs,ArrayList<ItemRefs> starredRefs){
        int arrayCapacity = 0;
        if(unreadRefs.size() > starredRefs.size()){
            arrayCapacity = starredRefs.size();
        }else {
            arrayCapacity = unreadRefs.size();
        }
        reUnreadUnstarRefs = new ArrayList<>( unreadRefs.size() );
        reUnreadStarredRefs = new ArrayList<>(arrayCapacity);
        reReadStarredRefs = new ArrayList<>( starredRefs.size() );
        Map<String,Integer> map = new HashMap<>( unreadRefs.size() + starredRefs.size() );
        Map<String,ItemRefs> mapArray = new HashMap<>( unreadRefs.size() );
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

//    public interface CheckResult<T,S>{
//        void listA(T a);
//        void listB(S b);
////        void listX(U u);
//        Object compareAAttr(T a);
//        Object compareBAttr(S b);
//    }
//    public <T,S>  void checkRepeat(List<T> listA,List<S> listB ,CheckResult<? super T,S> result){
//        if (listA.getClass() == ArrayList.class && listB.getClass() == ArrayList.class) {
//            Map<Object,Integer> map = new HashMap<>( listA.size() + listB.size() );
//            Map<Object,Object> mapA = new HashMap<>( listA.size() + listB.size() );
//            for (T a:listA){
//                map.put( result.compareAAttr(a) ,1 );
//                mapA.put(result.compareAAttr(a),a );
//            }
//            for (S b:listB){
//                Integer cc = map.get( result.compareBAttr(b) );
//                if( cc!=null ) {
//                    map.put( result.compareBAttr(b) ,+cc );
//                }else {
//                    result.listB(b);
//                }
//            }
//            for( Map.Entry<Object, Integer> entry: map.entrySet()) {
//                if(entry.getValue()==1) {
//                    result.listA((T) mapA.get(entry.getKey()));
//                }
//            }
//        }
//    }
//    private ArrayList<Article> listA;
//    private ArrayList<ItemRefs> listB;
//    public ArrayList<ItemRefs> reStarredRefsB(){
//        List<Article> beforeStarredList = WithDB.getInstance().loadStarAll();
//        listA =  new ArrayList<>( beforeStarredList.size() );
//        listB = new ArrayList<>( allStarredRefs.size() );
//
//        checkRepeat( beforeStarredList, allStarredRefs, new CheckResult<Article, ItemRefs>() {
//            @Override
//            public void listA(Article article) {
//                article.setStarState(API.ART_UNSTAR);
//                listA.add(article);// 取消加星
//            }
//            @Override
//            public void listB(ItemRefs itemRefs) {
//                Article article = WithDB.getInstance().getArticle( UString.toLongID(itemRefs.getId()) );
//                if(article!=null){
//                    article.setStarState(API.ART_STAR);// 2，去掉“本地有，状态为未加星”的
//                    listA.add(article);
//                }else {
//                    listB.add(itemRefs);// 3，就剩云端的，要请求的加星资源（但是还是含有一些要请求的未读资源）
//                }
//            }
//            @Override
//            public Object compareAAttr(Article attr) {
//                return attr.getId();
//            }
//            @Override
//            public Object compareBAttr(ItemRefs attr) {
//                return attr.getId();
//            }
//        });
////        System.out.println("【reStarredList】" + beforeStarredList.size() + "==" + allStarredRefs.size() +"==" + starList.size() +"=="+ starredRefs.size());
//        WithDB.getInstance().saveArticleList(listA);
//        allStarredRefs = new ArrayList<>();
//        return listB;
//    }

//    public void parseItemContentsUnreadUnstar(String info){
//        if(!beforeParseContents(info)){return;}
//        for ( Items items: currentItemsArray  ) {
//            betweenParseContents(items);
//            article.setReadState(API.ART_UNREAD);
//            article.setStarState(API.ART_UNSTAR);
//            saveList.add(article);
//        }
//        afterParseContents();
//    }
//    public void parseItemContentsUnreadStarred(String info){
//        if(!beforeParseContents(info)){return;}
//        for ( Items items: currentItemsArray  ) {
//            betweenParseContents(items);
//            article.setReadState(API.ART_UNREAD);
//            article.setStarState(API.ART_STAR);
//            saveList.add(article);
//        }
//        afterParseContents();
//    }
//    public void parseItemContentsReadStarred(String info){
//        if(!beforeParseContents(info)){return;}
//        for ( Items items: currentItemsArray  ) {
//            betweenParseContents(items);
//            article.setReadState(API.ART_READ);
//            article.setStarState(API.ART_STAR);
//            saveList.add(article);
//        }
//        afterParseContents();
//    }
//
//    public String parseStreamContentsStarred(String info){
//        if(!beforeParseContents(info)){return "";}
//        for ( Items items: currentItemsArray  ) {
//            betweenParseContents(items);
//            article.setReadState(API.ART_READ);
//            article.setStarState(API.ART_STAR);
//            saveList.add(article);
//        }
//        afterParseContents();
//        return gsItemContents.getContinuation();
//    }
//    private GsItemContents gsItemContents;
//    private ArrayList<Items> currentItemsArray;
//    private ArrayList<Article> saveList;
//    private Article article;
//    private String summary = "",html = "";
//    private boolean beforeParseContents(String info){
//        if(info==null || info.equals("")){return false;} // 如果返回 null 会与正常获取到流末端时返回 continuation = null 相同，导致调用该函数的那端误以为是正常的 continuation = null
//        Gson gson = new Gson();
//        gsItemContents = gson.fromJson(info, GsItemContents.class);
//        currentItemsArray = gsItemContents.getItems();
//        saveList = new ArrayList<>( currentItemsArray.size() ) ;
//        return true;
//    }
//    private void betweenParseContents(Items items){
//        article = new Article();
//        article.setId(items.getId());
//        article.setCrawlTimeMsec(items.getCrawlTimeMsec());
//        article.setTimestampUsec(items.getTimestampUsec());
//        article.setCategories(items.getCategories().toString());
//        article.setTitle(items.getTitle());
//        article.setPublished(items.getPublished());
//        article.setUpdated(items.getUpdated());
//        article.setCanonical(items.getCanonical().get(0).getHref());
//        article.setAlternate(items.getAlternate().toString());
//        article.setAuthor(items.getAuthor());
//
//        html = items.getSummary().getContent();
//        summary = Html.fromHtml(html).toString();
//        if(summary.length()>92){
//            article.setSummary(summary.substring(0,92));
//        }else {
//            article.setSummary(summary.substring(0,summary.length()));
//        }
//        article.setOrigin(items.getOrigin().toString());
//        UFile.saveHtml(UString.stringToMD5(article.getId()), html);
//    }
//    private void afterParseContents(){
//        WithDB.getInstance().saveArticleList(saveList);
//        gsItemContents = null;
//        currentItemsArray = null;
//        saveList = null;
//        article = null;
//        summary = "";
//        html = "";
//
//        Html.fromHtml(summary,null,null);
//    }



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
                article.setStarState(API.ART_STAR);
                return article;
            }
        });
    }
    public void parseItemContentsReadStarred(String info){
        parseItemContents(info, new ArticleChanger() {
            @Override
            public Article change(Article article) {
                article.setReadState(API.ART_READ);
                article.setStarState(API.ART_STAR);
                return article;
            }
        });
    }
    public String parseStreamContentsStarred(String info){
        return parseItemContents(info, new ArticleChanger() {
            @Override
            public Article change(Article article) {
                article.setReadState(API.ART_READ);
                article.setStarState(API.ART_STAR);
                return article;
            }
        });
    }
    public interface ArticleChanger{
        Article change(Article article);
    }
    /**
     * 这里用两种方法来实现了函数 A B C 共用一个主函数 X ，但各自在主函数中的某些语句又不同
     * 1.是采用分割主函数为多个函数 X[]，再在要在具体的函数 A B C 内拼接调用 X[]。
     * 2.是采用接口类作为主函数 X 的参数传递，在调用具体的函数 A B C 时，将各自要不同的语句在该接口内实现
     * 古老的原始代码是函数 A B C 都各自再写一遍共用函数
     * 使用接口类作为参数传递，实际上是让调用者来实现具体语句
     * @param info
     */
    public String parseItemContents(String info,ArticleChanger ArticleChanger){
        if(info==null || info.equals("")){return "";}// 如果返回 null 会与正常获取到流末端时返回 continuation = null 相同，导致调用该函数的那端误以为是正常的 continuation = null
        Gson gson = new Gson();
        GsItemContents gsItemContents = gson.fromJson(info, GsItemContents.class);
        ArrayList<Items> currentItemsArray = gsItemContents.getItems();
        ArrayList<Article> saveList = new ArrayList<>( currentItemsArray.size() ) ;
        String summary = "",html = "";
        for ( Items items: currentItemsArray  ) {
            Article article = new Article();
            article.setId(items.getId());
            article.setCrawlTimeMsec(items.getCrawlTimeMsec());
            article.setTimestampUsec(items.getTimestampUsec());
            article.setCategories(items.getCategories().toString());
            article.setTitle(items.getTitle());
            article.setPublished(items.getPublished());
            article.setUpdated(items.getUpdated());
            article.setCanonical(items.getCanonical().get(0).getHref());
            article.setAlternate(items.getAlternate().toString());
            article.setAuthor(items.getAuthor());
            article.setOrigin(items.getOrigin().toString());

            html = items.getSummary().getContent();
            summary = Html.fromHtml(html).toString();
            if(summary.length()>92){
                article.setSummary(summary.substring(0,92));
            }else {
                article.setSummary(summary.substring(0,summary.length()));
            }
            article = ArticleChanger.change(article);

            UFile.saveHtml(UString.stringToMD5(article.getId()), html);
            saveList.add(article);
        }
        WithDB.getInstance().saveArticleList(saveList);
        return gsItemContents.getContinuation();
    }

    public void parseArticleContents(String info){
        Gson gson = new Gson();
        ArrayList<Items> itemArticles = gson.fromJson(info, GsItemContents.class).getItems();
        Items items = itemArticles.get(0);
        if( itemArticles.size()!=0 && WithDB.getInstance().getArticle( items.getId()) != null ){
            UFile.saveHtml(UString.stringToMD5(items.getId()), items.getSummary().getContent());
        }
    }



}
