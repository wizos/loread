package me.wizos.loread.network.api;

import androidx.annotation.NonNull;

import java.util.List;

import me.wizos.loread.bean.Token;
import me.wizos.loread.bean.feedly.Collection;
import me.wizos.loread.bean.feedly.Entry;
import me.wizos.loread.bean.feedly.FeedItem;
import me.wizos.loread.bean.feedly.Profile;
import me.wizos.loread.bean.feedly.StreamContents;
import me.wizos.loread.bean.feedly.StreamIds;
import me.wizos.loread.bean.feedly.input.EditCollection;
import me.wizos.loread.bean.feedly.input.EditFeed;
import me.wizos.loread.bean.feedly.input.MarkerAction;
import me.wizos.loread.bean.search.SearchFeeds;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Wizos on 2019/4/13.
 */

public interface FeedlyService {
    // Post请求的文本参数则用注解@Field来声明，同时还必须给方法添加注解@FormUrlEncoded来告知Retrofit参数为表单参数，如果只为参数增加@Field注解，而不给方法添加@FormUrlEncoded注解运行时会抛异常。
    @FormUrlEncoded
    @POST("auth/token")
    Call<Token> getAccessToken(
            @Field("grant_type") String grantType,
            @Field("redirect_uri") String redirectUri,
            @Field("client_id") String clientId,
            @Field("client_secret") String clientSecret,
            @Field("code") String code
    );

    @FormUrlEncoded
    @POST("auth/token")
    Call<Token> refreshingAccessToken(
            @Field("grant_type") String grantType,
            @Field("refresh_token") String refreshToken,
            @Field("client_id") String clientId,
            @Field("client_secret") String clientSecret
    );

    @GET("profile")
    Call<Profile> getUserInfo(
            @Header("authorization") String authorization
    );

    @GET("collections")
    Call<List<Collection>> getCollections(
            @Header("authorization") String authorization
    );

    @GET("collections")
    Call<List<Collection>> editCollections(
            @Header("authorization") String authorization,
            @Body EditCollection editCollection
    );

    @GET("streams/ids")
    Call<StreamIds> getUnreadRefs(
            @Header("authorization") String authorization,
            @Query("streamId") String streamId,
            @Query("count") int count,
            @Query("unreadOnly") boolean unreadOnly,
            @Query("continuation") String continuation
    );

    @GET("streams/ids")
    Call<StreamIds> getStarredRefs(
            @Header("authorization") String authorization,
            @Query("streamId") String streamId,
            @Query("count") int count,
            @Query("continuation") String continuation
    );

    @Headers("Accept: application/json")
    @POST("entries/.mget")
    Call<List<Entry>> getItemContents(
            @Header("authorization") String authorization,
            @NonNull @Body List<String> ids
    );


    @Headers("Accept: application/json")
    @POST("feeds/.mget")
    Call<List<FeedItem>> getFeedsMeta(@NonNull @Body List<String> feeds);

    @GET("feeds/{feedId}")
    Call<FeedItem> getFeedMeta(@Path("feedId") String feedId);


    @GET("streams/{feedId}/contents")
    Call<StreamContents> getStreamContent(
            @Header("authorization") String authorization,
            @Path("feedId") String feedId, @Query("count") int count, @Query("continuation") String continuation);

    @GET("search/feeds")
    Call<SearchFeeds> getSearchFeeds(@Query("q") String keyWord, @Query("n") int count);

    @Headers("Accept: application/json")
    @POST("subscriptions")
    Call<List<EditFeed>> editFeed(
            @Header("authorization") String authorization,
            @NonNull @Body EditFeed editFeed
    );

    // 使用retrofit进行delete请求时，发现其并不支持向服务器传body。https://www.jianshu.com/p/940fd77961db
    //@DELETE("subscriptions/.mdelete")
    @Headers("Accept: application/json")
    @HTTP(method = "DELETE", path = "subscriptions/.mdelete", hasBody = true)
    Call<String> delFeed(
            @Header("authorization") String authorization,
            @Body List<String> feedIds
    );

    // 成功不返回信息
    // 使用@body标签时不能用@FormUrlEncoded标签，不然会报以下异常
    @Headers("Accept: application/json")
    @POST("markers")
    Call<String> markers(
            @Header("authorization") String authorization,
            @NonNull @Body MarkerAction markerAction
    );

}
