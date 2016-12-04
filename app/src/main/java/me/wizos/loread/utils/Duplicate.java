//package me.wizos.loread.utils;
//
//import android.support.v4.util.ArrayMap;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//import me.wizos.loread.bean.Article;
//import me.wizos.loread.bean.gson.ItemRefs;
//import me.wizos.loread.data.WithDB;
//
///**
// * Created by Wizos on 2016/12/4.
// */
//
//public class Duplicate {
//
//    public static <T,E> boolean deDuplicate( List<T> bigA, List<E> smallB) {
//
//        // only A， only B ，Duplicate
//        Map<String,Integer> map = new ArrayMap<>( refs.size() + articles.size());
//        Map<String,Article> articleMap = new ArrayMap<>( articles.size() );
//        ArrayList<Article> articleList =  new ArrayList<>( articles.size() );
//        ArrayList<ItemRefs> articleRefs = new ArrayList<>( refs.size() );
//
//        for ( T item : bigA ) {
//            String articleId = item.getId();
//            map.put(articleId, 1);
//            articleMap.put( articleId,item );
//        }
//        for ( E item : smallB ) {
//            String articleId = UString.toLongID(item.getId());
//            Integer cc = map.get( articleId );
//            if( cc!=null ) {
//                map.put( articleId , ++cc);// 存在重复
//            }else {
//                Article article = WithDB.getInstance().getArticle( articleId );// 必须保留
//                if(article!= null){ // 2，去掉“本地有，但是非此状态”的
//                    article = changer1.change(article);
////                    article.setStarState(API.ART_STAR);
////                    article.setStarState( state1 );
//                    articleList.add(article);
//                }else {
//                    articleRefs.add(item);// 3，就剩云端的，要请求的资源（但是还是含有一些要请求的未读资源）
//                }
//            }
//        }
//        for( Map.Entry<String, Integer> entry: map.entrySet()) {
//            if(entry.getValue()==1) {
//                Article article = articleMap.get(entry.getKey());
//                article = changer2.change(article);
////                article.setStarState( state2 );
////                article.setStarState(API.ART_UNSTAR);
//                articleList.add(article);// 取消加星
//            }
//        }
//        return true;
//    }
//}
