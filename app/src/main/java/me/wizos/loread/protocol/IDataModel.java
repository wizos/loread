//package me.wizos.loread.protocol;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import me.wizos.loread.bean.Article;
//import me.wizos.loread.bean.Feed;
//import me.wizos.loread.bean.RequestLog;
//import me.wizos.loread.bean.Tag;
//
///**
// * Created by Wizos on 2016/3/12.
// */
//interface IDataModel {
//
//    IDataModel getInstance();
//
//
//    void saveTag(Tag tag);
//    void saveTagList(ArrayList<Tag> tags);
//    List<Tag> loadTags();
//    boolean hasTag(String id);
//
//    void saveFeed(Feed feed);
//    boolean hasFeed(String url);
//
//    void saveArticle(Article article);
//    void saveArticleList(ArrayList<Article> articleList);
//    Article getArticle(String articleId);
//    void saveRequestLogList(ArrayList<RequestLog> requestLogList);
//    List<RequestLog> loadRequestListAll();
//
//    void delRequestListAll();
//    void delRequest(RequestLog requestLog);
//
//    void delArtList(ArrayList<String> arrayList);
//    void delArtAll(List<Article> articles);
//
//    List<Article> loadArtAll();
//    List<Article> loadArtAllOrder();
//    List<Article> loadArtsBeforeTime(long time);
//
//    List<Article> loadReadList(String readState, String listTag);
//    List<Article> loadReadAll(String readState);
//    List<Article> loadReadNoLabel();
//    List<Article> loadReadListHasLabel(String readState,long userId);
//
//
//    List<Article> loadStarList(String listTag);
//    List<Article> loadStarAll();
//    List<Article> loadStarAllOrder();
//    List<Article> loadStarNoLabel();
//    List<Article> loadStarListHasLabel(long userId);
//
//}
