//package me.wizos.loreadx.net.ApiService;
//
//import androidx.annotation.NonNull;
//
//import java.util.List;
//import java.util.Map;
//
//import me.wizos.loreadx.bean.fever.BaseResponse;
//import me.wizos.loreadx.bean.fever.Feeds;
//import me.wizos.loreadx.bean.fever.Groups;
//import me.wizos.loreadx.bean.fever.Items;
//import me.wizos.loreadx.bean.fever.SavedItemIds;
//import me.wizos.loreadx.bean.fever.UnreadItemIds;
//import me.wizos.loreadx.bean.ttrss.request.GetFeeds;
//import me.wizos.loreadx.bean.ttrss.request.GetHeadlines;
//import me.wizos.loreadx.bean.ttrss.request.SubscribeToFeed;
//import me.wizos.loreadx.bean.ttrss.request.UnsubscribeFeed;
//import me.wizos.loreadx.bean.ttrss.request.UpdateArticle;
//import me.wizos.loreadx.bean.ttrss.result.SubscribeToFeedResult;
//import me.wizos.loreadx.bean.ttrss.result.TTRSSArticleItem;
//import me.wizos.loreadx.bean.ttrss.result.TTRSSFeedItem;
//import me.wizos.loreadx.bean.ttrss.result.TTRSSResponse;
//import me.wizos.loreadx.bean.ttrss.result.UpdateArticleResult;
//import retrofit2.Call;
//import retrofit2.http.Body;
//import retrofit2.http.Field;
//import retrofit2.http.FormUrlEncoded;
//import retrofit2.http.Headers;
//import retrofit2.http.POST;
//import retrofit2.http.Query;
//
///**
// * Created by Wizos on 2019/11/23.
// */
//
//public interface FeverService {
//    // Post请求的文本参数则用注解@Field来声明，同时还必须给方法添加注解@FormUrlEncoded来告知Retrofit参数为表单参数，如果只为参数增加@Field注解，而不给方法添加@FormUrlEncoded注解运行时会抛异常。
//    @FormUrlEncoded
//    @POST("?api")
//    Call<BaseResponse> login(
//            @NonNull @Query("api_key") String apiKey
//    );
//    @FormUrlEncoded
//    @POST("?api&groups")
//    Call<Groups> getCategoryItems(
//            //@NonNull @Field("api_key") String apiKey
//            @NonNull @Query("api_key") String apiKey
//    );
//    @FormUrlEncoded
//    @POST("?api&feeds")
//    Call<Feeds> getFeeds(
//            @NonNull @Query("api_key") String apiKey
//    );
//
//    @FormUrlEncoded
//    @POST("?api&unread_item_ids")
//    Call<UnreadItemIds> getUnreadItemIds(
//            @NonNull @Query("api_key") String apiKey
//    );
//
//    @FormUrlEncoded
//    @POST("?api&saved_item_ids")
//    Call<SavedItemIds> getSavedItemIds(
//            @NonNull @Query("api_key") String apiKey
//    );
//
//
//    /**
//     * 每次最多 50 条
//     */
//    @FormUrlEncoded
//    @POST("?api&items")
//    Call<Items> getItems(
//            @NonNull @Query("api_key") String apiKey,
//            @Query("since_id") String sinceId,
//            @Query("max_id") String maxId,
//            @Query("with_ids") String withIds
//    );
//
//
//
//
//
//    @FormUrlEncoded
//    @POST("api/")
//    Call<TTRSSResponse<UpdateArticleResult>> updateArticle(
//            @NonNull @Body UpdateArticle updateArticle
//    );
//
//
//    @Headers("Accept: application/json")
//    @POST("api/")
//    Call<TTRSSResponse<SubscribeToFeedResult>> subscribeToFeed(
//            @NonNull @Body SubscribeToFeed subscribeToFeed
//    );
//
//    @Headers("Accept: application/json")
//    @POST("api/")
//    Call<TTRSSResponse<Map>> unsubscribeFeed(
//            @NonNull @Body UnsubscribeFeed unsubscribeFeed
//    );
//
////    @Headers("Accept: application/json")
////    @POST("subscriptions")
////    Call<List<EditFeed>> editFeed(
////            @NonNull @Body EditFeed editFeed
////    );
//}
