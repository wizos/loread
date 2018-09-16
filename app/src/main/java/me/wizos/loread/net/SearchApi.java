package me.wizos.loread.net;

import com.lzy.okgo.callback.StringCallback;

/**
 * Created by Wizos on 2017/12/31.
 */

public class SearchApi {
    //    private static final String Loreead_Feeds_Host = "http://api.wizos.me/search.php";
    private static final String Feedly_Feeds_Host = "http://cloud.feedly.com/v3/search/feeds";
//    private static final String Inoreader_Feeds_Host = "https://www.inoreader.com";
    private static final int page_unit = 10000;
    private static SearchApi searchApi;

    public static SearchApi i() {
        if (searchApi == null) {
            synchronized (InoApi.class) {
                if (searchApi == null) {
                    searchApi = new SearchApi();
                }
            }
        }
        return searchApi;
    }

    public void asyncFetchSearchResult(String searchWord, StringCallback cb) {
//        WithHttp.i().asyncGet(Loreead_Feeds_Host + "?n=" + page_unit + "&q=" + searchWord, null, null, cb );
//        WithHttp.i().asyncGet(Loreead_Feeds_Host + "?n=" + page_unit + "&q=" + searchWord, null, null, cb );

//        if(BuildConfig.DEBUG){
//            FormBody.Builder builder = new FormBody.Builder();
//            builder.add("n", page_unit + "");
//            builder.add("q", searchWord);
//            WithHttp.i().asyncPost(Loreead_Feeds_Host, builder, null, cb);
//        }else {
        WithHttp.i().asyncGet(Feedly_Feeds_Host + "?n=" + page_unit + "&q=" + searchWord, null, null, cb);
//        }
    }

//    public FeedlyFeedsSearchResult fetchSearchResult(String searchWord ) throws IOException{
//        String json =  WithHttp.i().syncGet(Loreead_Feeds_Host + "?n=" + page_unit + "&q=" + searchWord, null, null, null);
//        FeedlyFeedsSearchResult searchResult = new Gson().fromJson(json, FeedlyFeedsSearchResult.class );
//        KLog.e(searchResult.getHint());
//        KLog.e(searchResult.getResults().size());
//        return searchResult;
////        return new FeedlyFeedsSearchResult();
//    }
//    public void syncSearchFeeds(String searchWord,int page_unit, NetCallbackS cb)  throws HttpException, IOException {
//        String json =  WithHttp.i().syncPost(Inoreader_Feeds_Host + "/reader/api/0/directory/search/"+ searchWord  + "?n=" + page_unit , null, null, cb);
//        InoreaderFeedsSearchResult searchFeedsResult = new Gson().fromJson(json, InoreaderFeedsSearchResult.class );
//        ArrayList<InoreaderFeed> feeds = searchFeedsResult.getFeeds();
//        int field = 0;
//        FeedlyFeed feedlyFeed;
//        ArrayList<FeedlyFeed> feedlyFeeds = new ArrayList<>(feeds.size());
//        for (InoreaderFeed feed:feeds){
//            field = searchMap.get("feed/" + feed.getXmlUrl());
//            if( field != 0 ){
//            }else {
//                feedlyFeed = new FeedlyFeed();
//                feedlyFeed.setFeedId( "feed/" + feed.getXmlUrl() );
//                feedlyFeed.setTitle( feed.getTitle() );
//                feedlyFeed.setSubscribers( feed.getSubscribers() );
//                feedlyFeed.setVelocity( feed.getArticlesPerWeek() + "");
//                feedlyFeed.setVisualUrl( feed.getIconUrl() );
//                feedlyFeeds.add(feedlyFeed);
//                searchMap.put( "feed/" + feed.getXmlUrl(),1 );
//            }
//        }
//    }

}
